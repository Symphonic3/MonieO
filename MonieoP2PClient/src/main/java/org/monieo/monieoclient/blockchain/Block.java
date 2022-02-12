package org.monieo.monieoclient.blockchain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.monieo.monieoclient.Monieo;

public class Block extends MonieoDataObject{
	
	public final BlockHeader header;
	public final AbstractTransaction[] transactions;
	
	public Block(BlockHeader header, AbstractTransaction... transactions) {
		super(header.mn, header.pv);
		this.header = header;
		this.transactions = transactions;
		
	}
	
	public static Block getByHash(String hash) {
		
		File f = new File(Monieo.INSTANCE.blocksFolder.getPath() + "/" + hash + ".blk");
		
		return Block.deserialize(Monieo.readFileData(f));
		
	}
	
	public Block getPrevious() {
		
		return getByHash(header.preHash);
		
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
			
			transactions[transactions.length-1] = CoinbaseTransaction.deserialize(data[1]);
			if (transactions[0] == null) return null;
			
			for (int i = 0; i < transactions.length-1; i++) {
				
				transactions[i] = Transaction.deserialize(data[i]);
				if (transactions[i] == null) return null;
				
			}
			
			//note that returning a block without throwing error does not mean the block is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Block(BlockHeader.deserialize(data[0]), transactions);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
	public BigInteger calculateNextDifficulty() {
		
		List<Block> blocksToAverage = new ArrayList<Block>();
		
		Block b = this;
		
		while (b != null) {
			
			blocksToAverage.add(b);
			
			if (blocksToAverage.size() >= 30) break;
			
			b = b.getPrevious();
			
		}
		
		if (blocksToAverage.size() < 2) return Monieo.MAXIMUM_HASH_VALUE;
		
		Collections.reverse(blocksToAverage);
		
		long sum = 0;
		
		BigDecimal sumdiff = BigDecimal.ZERO;
		
		for (int i = 1; i < blocksToAverage.size(); i++) {
			
			Block bz = blocksToAverage.get(i);
			
			sum += bz.header.timestamp - blocksToAverage.get(i-1).header.timestamp;
			
			sumdiff = sumdiff.add(new BigDecimal(Monieo.MAXIMUM_HASH_VALUE).divide(new BigDecimal(blocksToAverage.get(i-1).header.diff), 100, RoundingMode.HALF_UP));
			
		}
		
		sumdiff = sumdiff.divide(BigDecimal.valueOf(blocksToAverage.size()-1), 100, RoundingMode.HALF_UP);
		
		BigDecimal av = new BigDecimal(sum).divide(new BigDecimal(blocksToAverage.size()-1), 100, RoundingMode.HALF_UP);
		
		BigDecimal f = sumdiff.divide(av, 100, RoundingMode.HALF_UP);
		
		BigDecimal discrepancy = new BigDecimal(Monieo.MAXIMUM_HASH_VALUE).divide(f.multiply(new BigDecimal(120000)), 100, RoundingMode.HALF_UP); //2min
		
		return discrepancy.setScale(0, RoundingMode.HALF_UP).toBigIntegerExact();
		
	}

	@Override
	boolean testValidity() {
		
		if (this.equals(Monieo.genesis())) return true;
		
		if (transactions.length == 0) return false;
		
		if (!merkle(transactions).equals(header.merkleRoot)) return false;
		
		if (header == null) return false;
		
		Block prev = getPrevious();
		
		if (prev == null) return false;
		
		if (!Monieo.assertSupportedProtocol(new String[]{header.mn,header.pv})) return false;
			
		if (header.height != prev.header.height+1) return false;
		
		if (!prev.calculateNextDifficulty().equals(header.diff)) return false;
		
		if (new BigInteger(1, rawHash()).compareTo(header.diff) != -1) return false;
		
		if (prev.header.timestamp >= header.timestamp) return false;
		
		if (Monieo.INSTANCE.getNetAdjustedTime() + 7200000 < header.timestamp) return false; //2h
		
		int cb = 0;
		
		for (AbstractTransaction t : transactions) {
			
			if (t == null) return false;
			
			if (t instanceof CoinbaseTransaction) {
				
				if (!((CoinbaseTransaction) t).validate(this)) return false;
				
				cb++;
				
			} else if (t instanceof Transaction) {
				
				Transaction at = (Transaction) t;
				
				if (!at.testValidityWithBlock(prev) || !at.testValidityWithTime(header.timestamp)) return false;
				
			}
			
			int co = 0;
			
			for (AbstractTransaction t1 : transactions) {
				
				if (t1 instanceof Transaction && t instanceof Transaction) {
					
					if (((Transaction) t1).getSource().equals(((Transaction) t).getSource())) co++;
					
				}
				
			}
			
			if (co > 1) return false; //takes into account coinbasetransactions
			
			//if (t.expired()) return false;
			
		}
		
		if (cb != 1) return false;
		
		if (serialize().getBytes().length > 1024*1024) return false;
		
		return true;
		
	}
	
	public static String merkle(AbstractTransaction[] transactions) {
		
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
	
	public static BigDecimal getMaxCoinbase(long h) {
		
		BigDecimal defaultAmount = new BigDecimal("10");
		
		int halvings = (int)(h/525600);
		
		for (int i = 0; i < halvings; i++) {
			
			defaultAmount = defaultAmount.divide(new BigDecimal("2"));
			
		}
		
		return defaultAmount;
		
	}
	
	public String hash() {
		
		return header.hash();
		
	}
	
	public byte[] rawHash() {
		
		return header.rawHash();
		
	}
	
	public BlockMetadata getMetadata() {
		
		return new BlockMetadata(new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta"));
		
	}
	
	public boolean hasMetadata() {
		
		return new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta").exists();
		
	}
	
	//WARNING! WITH THIS CURRENT TERRIBLE CODE, THE MINER MUST PLACE THEIR COINBASE TRANSACTION AT THE END OF THE BLOCK IF THEY WISH TO RECIEVE ALL FEES.
	public void generateMetadata() {
		
		//This method is full of horrible code.
		//Deal with it, or refactor it yourself.
		
		if (!hasMetadata()) {
			
			File blockmetafile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta");
			
			if (!blockmetafile.exists()) {
				
				try {
					blockmetafile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				String ph = header.preHash;
				
				File prevBlockMetaFile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + ph + ".blkmeta");

				List<AbstractTransaction> txclone = new ArrayList<AbstractTransaction>(Arrays.asList(transactions.clone()));
				
				BigDecimal fees = BigDecimal.ZERO;
				
				try (FileWriter fw = new FileWriter(blockmetafile, false)) {
					
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
										actual.add(new PendingFunds(fees, Monieo.CONFIRMATIONS));
										txclone.remove(i);
										i--;
										
									} else if (t instanceof Transaction && ((Transaction)t).getSource().equals(wa)) {
										
										spendable = spendable.subtract(t.getAmount()).subtract(((Transaction)t).d.fee);
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
