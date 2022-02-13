package org.monieo.monieoclient.blockchain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

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
		
		Block r = Block.deserialize(Monieo.readFileData(f));
		
		return r;
		
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
			
			CoinbaseTransaction ct = CoinbaseTransaction.deserialize(data[data.length-1]);
			if (ct == null) return null;
			
			transactions[transactions.length-1] = ct;
			
			for (int i = 0; i < transactions.length-1; i++) {
				
				Transaction t = Transaction.deserialize(data[i+1]);;
				if (t == null) return null;

				transactions[i] = t;
				
			}
			
			//note that returning a block without throwing error does not mean the block is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Block(BlockHeader.deserialize(data[0]), transactions);
			
		} catch (Exception e) {
			
			e.printStackTrace();
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
		
		System.out.println("a");
		
		if (transactions.length == 0) return false;
		System.out.println("b");
		if (!merkle(transactions).equals(header.merkleRoot)) return false;
		System.out.println("c");
		if (header == null) return false;
		System.out.println("d");
		Block prev = getPrevious();
		System.out.println("e");
		if (prev == null) return false;
		System.out.println("f");
		if (!Monieo.assertSupportedProtocol(new String[]{header.mn,header.pv})) return false;
		System.out.println("g");
		if (header.height != prev.header.height+1) return false;
		System.out.println("h");
		if (!prev.calculateNextDifficulty().equals(header.diff)) return false;
		System.out.println("i");
		if (new BigInteger(1, rawHash()).compareTo(header.diff) != -1) return false;
		System.out.println("j");
		if (prev.header.timestamp >= header.timestamp) return false;
		System.out.println("k");
		if (Monieo.INSTANCE.getNetAdjustedTime() + 7200000 < header.timestamp) return false; //2h
		System.out.println("l");
		int cb = 0;
		
		for (AbstractTransaction t : transactions) {
			
			if (t == null) return false;
			
			System.out.println("rho");
			
			if (t instanceof CoinbaseTransaction) {
				
				System.out.println("pi");
				
				if (!((CoinbaseTransaction) t).validate(this)) return false;
				
				cb++;
				
			} else if (t instanceof Transaction) {
				
				Transaction at = (Transaction) t;
				
				System.out.println("lambda");
				
				if (!at.testHasAmount(prev) || at.tooFarInFuture(header.timestamp) || at.expired(header.timestamp)) return false;
				
			}
			
			int count = 0;
			
			for (AbstractTransaction t1 : transactions) {
				
				if (t1 instanceof Transaction && t instanceof Transaction) {
					
					System.out.println("epsilon");
					
					if (((Transaction) t1).equals(((Transaction) t))) count++;
					
				}
				
			}
			
			if (count > 1) return false;
			
			//if (t.expired()) return false;
			
		}
		
		System.out.println("m");
		
		if (cb != 1) return false;
		
		System.out.println("n");
		
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

	//coinbase transaction is at end of block!!!
	public void generateMetadata() {
		
		//This method is full of horrible code.
		//Deal with it, or refactor it yourself.
		
		if (!hasMetadata()) {
			
			File blockmetafile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta");
			try {
				blockmetafile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			String ph = header.preHash;
			File prevBlockMetaFile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + ph + ".blkmeta");

			List<AbstractTransaction> txclone = new ArrayList<AbstractTransaction>(Arrays.asList(transactions.clone()));
			
			BigDecimal fees = BigDecimal.ZERO;
			
			for (AbstractTransaction t : txclone) {
				
				if (t instanceof Transaction) {
					
					Transaction tr = ((Transaction) t);
					
					fees = fees.add(tr.d.fee);
					
				}
				
			}
			
			HashMap<WalletAdress, List<PendingFunds>> pfToAdd = new HashMap<WalletAdress, List<PendingFunds>>();
			
			for (AbstractTransaction t : txclone) {
				
				WalletAdress w = t.getDestination();
				
				if (!pfToAdd.containsKey(w)) pfToAdd.put(w, new ArrayList<PendingFunds>());
				
				boolean cb = (t instanceof CoinbaseTransaction);
				
				if (t.getDestination().equals(w)) {
					
					if (cb) {
						
						pfToAdd.get(w).add(new PendingFunds(t.getAmount(), Monieo.CONFIRMATIONS_BLOCK_SENSITIVE));
						pfToAdd.get(w).add(new PendingFunds(fees, Monieo.CONFIRMATIONS));
						
					} else {
						
						pfToAdd.get(w).add(new PendingFunds(t.getAmount(), Monieo.CONFIRMATIONS));
						
					}
					
				} else if (t instanceof Transaction) { //is this unnecessary? probably.
					
					Transaction tr = (Transaction) t;
					
					if (tr.getSource().equals(w)) {
						
						pfToAdd.get(w).add(new PendingFunds(tr.d.amount.negate().add(tr.d.fee.negate()), 0)); //lol
						
					}
					
				}
				
			}
			
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
							
							for (int i = 0; i < tcs.size(); i++) {
								
								PendingFunds t = tcs.get(i);
								
								t.confRemain--;
								
								if (t.spendable()) {
									
									spendable = spendable.add(t.amount);
									
									tcs.remove(i);
									i--;
									
								}
								
							}
							
							if (pfToAdd.containsKey(wa)) {
								
								for (PendingFunds pf : pfToAdd.get(wa)) {
									
									if (pf.spendable()) {
										
										spendable = spendable.add(pf.amount);
										
									} else {
										
										tcs.add(pf);
										
									}
									
								}
								
								pfToAdd.remove(wa);
								
							}
							
							String lnwrite = wa.adress + " " + spendable;
							
							for (PendingFunds pf : tcs) {
								
								lnwrite = lnwrite + " " + pf.serialize();
								
							}
							
							fw.write(lnwrite + "\n");
							
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}

				for (WalletAdress w : pfToAdd.keySet()) {
					
					String lnwrite = w.adress;
					
					for (PendingFunds pf : pfToAdd.get(w)) {
						
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
