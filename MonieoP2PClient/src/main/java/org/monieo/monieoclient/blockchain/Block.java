package org.monieo.monieoclient.blockchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;

public class Block extends MonieoDataObject{
	
	public final BlockHeader header;
	public final AbstractTransaction[] transactions;
	
	public Block(BlockHeader header, AbstractTransaction... transactions) {
		super(header.magicn, header.ver);
		this.header = header;
		this.transactions = transactions;
		
	}

	public String serialize() {

		String[] ts = new String[transactions.length+1];
		
		ts[0] = header.serialize();
		
		for (int i = 0; i < transactions.length; i++) {
			
			ts[i+1] = transactions[i].serialize();
			
		}
		
		return String.join("\n", ts);
		
	}

	public static Block deserialize(String s) {
		
		try {

			String[] data = s.split("\n");
			
			AbstractTransaction[] transactions = new AbstractTransaction[data.length-1];
			
			transactions[0] = CoinbaseTransaction.deserialize(data[1]);
			
			for (int i = 1; i < transactions.length; i++) {
				
				transactions[i] = Transaction.deserialize(data[i+1]);
				
			}
			
			//note that returning a block without throwing error does not mean the block is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Block(BlockHeader.deserialize(data[0]), transactions);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

	@Override
	boolean testValidity() {

		if (transactions.length == 0) return false;
		if (!merkle().equals(header.merkleRoot)) return false;
		if (header == null) return false;
		if (!header.validate()) return false;
		for (AbstractTransaction t : transactions) {
			
			if (t == null) return false;
			if (!t.validate()) return false;
			//if (t.expired()) return false;
			
		}
		
		if (serialize().getBytes().length > 1024*1024) return false;
		
		return true;
		
	}
	
	public String merkle() {
		
		List<String> hashes = new ArrayList<String>();
		
		for (int i = 0; i < transactions.length; i++) {
			
			hashes.add(transactions[i].hash());
			
			if (i == transactions.length-1 && (transactions.length % 2 == 1)) {
				
				hashes.add(transactions[i].hash());
				
			}
			
		}
		
		while (hashes.size() != 1) {
			
			List<String> hashesn = new ArrayList<String>();
			
			for (int i = 0; i < hashes.size(); i+=2) {
				
				hashesn.add(Monieo.sha256d(hashes.get(i) + hashes.get(i+1)));
				
			}
			
			hashes = hashesn;
			
		}
	
		return hashes.get(0);
		
	}
	
	public String hash() {
		
		return header.hash();
		
	}
	
	public BlockMetadata getMetadata() {
		
		return new BlockMetadata(new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta"));
		
	}
	
	public boolean hasMetadata() {
		
		return new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta").exists();
		
	}
	
	public void generateMetadata() {
		
		//This method is full of horrible code.
		//Deal with it, or refactor it yourself.
		
		if (!hasMetadata()) {
			
			File blockmetafile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta");
			
			if (!blockmetafile.exists() || this.equals(Monieo.genesis())) {
				
				try {
					blockmetafile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				String ph = header.preHash;
				
				File prevBlockMetaFile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + ph + ".blkmeta");

				List<AbstractTransaction> txclone = new ArrayList<AbstractTransaction>(Arrays.asList(transactions.clone()));
				
				try (FileWriter fw = new FileWriter(blockmetafile)) {
					
					if (!this.equals(Monieo.genesis())) {
						
						if (!prevBlockMetaFile.exists()) {
							
							//we should request this so-called previous block, possibly, at some point
							//can't be bothered to implement this right now.
							//TODO fix this, at some point
							
							return;
							
						}
						
						try (Scanner c = new Scanner(prevBlockMetaFile)) {
							
							while (c.hasNextLine()) {
								
								String l = c.nextLine();
								
								if (l.equals(" ") || l.equals("\n")) continue;
								
								WalletAdress wa = new WalletAdress(l.split(" ")[0]);
								List<PendingFunds> tcs = BlockMetadata.getTXCS(l);
								
								BigDecimal spendable = BigDecimal.ZERO;
								List<PendingFunds> actual = new ArrayList<PendingFunds>();
								
								for (PendingFunds p : tcs) {
									
									p.confRemain--;
									
									if (p.spendable()) {
										
										spendable = spendable.add(p.amount);
										
									} else {
										
										actual.add(p);
										
									}
									
								}
								
								for (int i = 0; i < txclone.size(); i++) {
									
									AbstractTransaction t = txclone.get(i);
									
									if (t.getDestination().equals(wa)) {
		
										boolean cb = (t instanceof CoinbaseTransaction);
										int ampe = cb ? Monieo.CONFIRMATIONS_BLOCK_SENSITIVE : Monieo.CONFIRMATIONS;
										actual.add(new PendingFunds(t.getAmount(), ampe));
										txclone.remove(i);
										i--;
										
									} else if (t instanceof Transaction && ((Transaction)t).getSource().equals(wa)) {
										
										spendable = spendable.subtract(t.getAmount());
										txclone.remove(i);
										i--;
										
									}
									
								}
								
								String lnwrite = wa.adress + " " + spendable;
								
								for (PendingFunds pf : actual) {
									
									lnwrite = lnwrite + " " + pf.serialize();
									
								}
								
								fw.write(lnwrite + "\n");
								
							}
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
					
					HashMap<WalletAdress, List<PendingFunds>> wpf = new HashMap<WalletAdress, List<PendingFunds>>();
					
					for (AbstractTransaction at : txclone) {
						
						if (!wpf.containsKey(at.getDestination())) {
							
							wpf.put(at.getDestination(), new ArrayList<PendingFunds>());
							
						}
						
					}
					
					for (AbstractTransaction at : txclone) {
						
						PendingFunds pf;
						
						if (at instanceof CoinbaseTransaction) {
							
							pf = new PendingFunds(at.getAmount(), Monieo.CONFIRMATIONS_BLOCK_SENSITIVE);
							
						} else {
							
							pf = new PendingFunds(at.getAmount(), Monieo.CONFIRMATIONS);
							
						}
						
						List<PendingFunds> lpf = wpf.get(at.getDestination());
						
						lpf.add(pf);
						
						wpf.put(at.getDestination(), lpf);
						
					}
					
					for (WalletAdress w : wpf.keySet()) {
						
						String lnwrite = w.adress;
						
						for (PendingFunds pf : wpf.get(w)) {
							
							lnwrite = lnwrite + " " + pf.serialize();
							
						}
						
						fw.write(lnwrite + "\n");
						
					}
					
				} catch (IOException e) {
					
					e.printStackTrace();
					return;
					
				}
				
			}
			
		}
		
	}

}
