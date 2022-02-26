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
import java.util.HashSet;
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
		
		Block r = Block.deserialize(Monieo.readFileData(f));
		
		return r;
		
	}
	
	private File getWorkFile() {
		
		return new File(Monieo.INSTANCE.blocksExtraDataFolder.getPath() + "/" + hash() + ".blkd");
		
	}
	
	public void generateWorkData() {
		
		BigDecimal work = null;
		
		if (this.equals(Monieo.genesis())) {
			
			work = getSingleWork();
			
		} else {
			
			Block p = getPrevious();
			
			if (!p.hasWorkData()) {
				
				List<Block> genm = new ArrayList<Block>();
				genm.add(p);
				
				genmloop: while (true) {
					
					if (p != null && !p.hasWorkData()) {
						
						genm.add(p);
						
					} else {
						
						for (int i = genm.size(); i-- > 0; ) {

							genm.get(i).generateWorkData();
							
						}
						
						break genmloop;
						
					}

					p = p.getPrevious();
					
				}
				
			}
			
			work = getPrevious().getChainWork().add(getSingleWork());
			
		}
		
		File f = getWorkFile();
		
		try (FileWriter fw = new FileWriter(f, false)) {
			
			fw.append(work.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public BigDecimal getChainWork() {
		
		File f = getWorkFile();
		
		if (!f.exists()) {
			
			generateWorkData();
			
		}
		
		return new BigDecimal(Monieo.readFileData(f));
		
	}
	
	public BigDecimal getSingleWork() {
		
		return new BigDecimal(Monieo.MAXIMUM_HASH_VALUE).divide(new BigDecimal(header.diff), 100, RoundingMode.HALF_UP);
		
	}
	
	public boolean hasWorkData() {
		
		return getWorkFile().exists();
		
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
				
				Transaction t = Transaction.deserialize(data[i+1]);

				if (t == null) return null;

				transactions[i] = t;
				
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
			
			sumdiff = sumdiff.add(blocksToAverage.get(i-1).getSingleWork());
			
		}
		
		sumdiff = sumdiff.divide(BigDecimal.valueOf(blocksToAverage.size()-1), 100, RoundingMode.HALF_UP);
		
		BigDecimal av = new BigDecimal(sum).divide(new BigDecimal(blocksToAverage.size()-1), 100, RoundingMode.HALF_UP);
		
		BigDecimal f = sumdiff.divide(av, 100, RoundingMode.HALF_UP);
		
		BigDecimal discrepancy = new BigDecimal(Monieo.MAXIMUM_HASH_VALUE).divide(f.multiply(new BigDecimal(60000)), 100, RoundingMode.HALF_UP); //2min
		
		return discrepancy.setScale(0, RoundingMode.HALF_UP).toBigIntegerExact();
		
	}

	@Override
	boolean testValidity() {

		try {
			
			if (this.equals(Monieo.genesis())) return true;

			if (!Monieo.assertSupportedProtocol(new String[]{header.mn,header.pv})) return false;
			
			if (transactions.length == 0) return false;

			if (!merkle(transactions).equals(header.merkleRoot)) return false;

			if (header == null) return false;
			
			Block prev = getPrevious();

			if (prev == null) return false;

			if (header.height != prev.header.height+1) return false;

			if (!prev.calculateNextDifficulty().equals(header.diff)) return false;

			if (new BigInteger(1, rawHash()).compareTo(header.diff) != -1) return false;

			long btoavtime = 0;
			int divisor = 0;
			Block d = prev;
			
			while (d != null) {
				
				btoavtime += d.header.timestamp;
				divisor++;
				
				if (divisor == 6) break;
				
				d = d.getPrevious();
				
			}
			
			if ((btoavtime/divisor) >= header.timestamp) return false;

			if (Monieo.INSTANCE.getNetAdjustedTime() + 7200000 < header.timestamp) return false; //2h

			if (serialize().getBytes().length > 1024*256) return false; //256kb
			
			//validate transactions
			//all of this is a very inefficient algorithm. if anyone has any bright ideas on how to fix this, please do!
			//we could easily reduce complexity if we thought about it a bit more but i can't be bothered to do that right now
			
			List<AbstractTransaction> txf = Arrays.asList(transactions);
			
			List<String> sources = new ArrayList<String>();
			boolean cb = false;
			
			for (AbstractTransaction t : txf) {
				
				if (t == null) return false;
				
				if (t instanceof CoinbaseTransaction) {
					
					if (cb == true) return false; //>1 coinbase transaction
					cb = true;

					if (!((CoinbaseTransaction) t).validate(this)) return false;
					
					continue;
					
				} else if (t instanceof Transaction) {
					
					if (Collections.frequency(txf, t) > 1) return false;
					
				} else return false; //shouldn't happen
				
				Transaction tr = (Transaction) t;
				
				if (!sources.contains(tr.getSource())) sources.add(tr.getSource());
				
			}
			
			BlockMetadata m = prev.getMetadata();
			
			for (String s : sources) {
				
				WalletData wd = m.getWalletData(s);
				
				BigDecimal bal = BlockMetadata.getSpendableBalance(wd.pf);
				BigInteger nonce = wd.nonce;
				
				int am = 0;
				
				for (AbstractTransaction t : transactions) {
					
					if (t instanceof Transaction) {
						
						Transaction tr = (Transaction) t;
						
						if (!tr.getSource().equals(s)) continue;
						
						am++;
						
						bal = bal.subtract(tr.d.amount.add(tr.d.fee));
						
						if (tr.d.nonce.compareTo(nonce) == 1) nonce = tr.d.nonce;
						
					}
					
				}
				
				if (bal.compareTo(BigDecimal.ZERO) == -1) return false;
				
				if (!nonce.equals(wd.nonce.add(BigInteger.valueOf(am-1)))) return false;
				
			}

			return true;
			
		} catch (Exception e) {
			
			return false;
			
		}
		
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
		
		BigDecimal defaultAmount = BigDecimal.valueOf(5);
		
		int halvings = (int)(h/1051200);
		
		for (int i = 0; i < halvings; i++) {
			
			defaultAmount = defaultAmount.divide(BigDecimal.valueOf(2));
			
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
		
		if (!hasMetadata()) generateMetadata();
		
		return new BlockMetadata(new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta"));
		
	}
	
	public boolean hasMetadata() {
		
		return new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta").exists();
		
	}

	//coinbase transaction is at end of block!!!
	public void generateMetadata() {
		
		//This method is full of horrible code.
		//Deal with it, or refactor it yourself.
		
		//removing this sanity check because we shouldn't need it anyways
		//if (!validate()) throw new IllegalStateException("Attempted to generate metadata for an invalid block!");
		
		if (!hasMetadata()) {
			
			File blockmetafile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + hash() + ".blkmeta");
			try {
				blockmetafile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			String ph = header.preHash;
			File prevBlockMetaFile = new File(Monieo.INSTANCE.blockMetadataFolder.getPath() + "/" + ph + ".blkmeta");
			
			//parse transactions
			BigDecimal fees = BigDecimal.ZERO;
			
			HashMap<String, WalletData> pfToAdd = new HashMap<String, WalletData>();
			
			for (AbstractTransaction t : transactions) {
				
				boolean cb = (t instanceof CoinbaseTransaction); //assume that if it is not a coinbasetransaction it is a transaction
				
				String w = t.getDestination();
				
				if (!pfToAdd.containsKey(w)) pfToAdd.put(w, new WalletData(w, BigInteger.ZERO, new ArrayList<PendingFunds>()));
				
				if (cb) {
					
					//negative symbol is important here!
					pfToAdd.get(w).pf.add(new PendingFunds(t.getAmount(), -Monieo.CONFIRMATIONS_BLOCK_SENSITIVE));
					pfToAdd.get(w).pf.add(new PendingFunds(fees, 0));
					
				} else {
					
					pfToAdd.get(w).pf.add(new PendingFunds(t.getAmount(), 0));
					
					Transaction tr = ((Transaction) t);
					fees = fees.add(tr.d.fee);
					
					String w2 = tr.getSource();
					
					if (!pfToAdd.containsKey(w2)) pfToAdd.put(w2, new WalletData(w2, BigInteger.ZERO, new ArrayList<PendingFunds>()));

					pfToAdd.get(w2).pf.add(new PendingFunds(tr.d.amount.negate().add(tr.d.fee.negate()), 0)); //lol
					//this increments the nonce assuming that transactions are already validated and all nonces line up. this is checked in Block#validate().
					pfToAdd.get(w2).nonce = tr.d.nonce.add(BigInteger.ONE);
					
				}
				
			}
			
			try (FileWriter fw = new FileWriter(blockmetafile, false)) {
				
				if (!this.equals(Monieo.genesis())) {
					
					//generate any needed previous metadata
					if (!prevBlockMetaFile.exists()) {
						
						Block p = getPrevious();
						
						List<Block> genm = new ArrayList<Block>();
						
						genmloop: while (true) {
							
							if (p == null) {
								
								//This shouldn't happen in practice because blocks will check if they have a valid parent before passing validation.
								//If a block like this is recieved, block synchronization will take care of getting the chain straight.
								
								return;
								
							} else {
								
								if (!p.hasMetadata()) {
									
									genm.add(p);
									
								} else {
									
									for (int i = genm.size(); i-- > 0; ) {

										genm.get(i).generateMetadata();
										
									}
									
									break genmloop;
									
								}
								
							}

							p = p.getPrevious();
							
						}
						
					}
					
					//execute writes
					try (Scanner c = new Scanner(prevBlockMetaFile)) {
						
						while (c.hasNextLine()) {
							
							String l = c.nextLine();
							
							if (l.equals(" ") || l.equals("\n")) continue;
							
							String wa = l.split(" ")[0];
							BigInteger nonce = new BigInteger(l.split(" ")[1]);
							
							List<PendingFunds> tcs = BlockMetadata.getTXCS(l);
							BigDecimal fullyconfirmed = BigDecimal.ZERO;
							
							for (int i = 0; i < tcs.size(); i++) {
								
								PendingFunds t = tcs.get(i);
								
								t.conf++;
								
								if (t.isOverConfirmed()) {
									
									fullyconfirmed = fullyconfirmed.add(t.amount);
									
									tcs.remove(i);
									i--;
									
								}
								
							}
							
							if (pfToAdd.containsKey(wa)) {
								
								for (PendingFunds pf : pfToAdd.get(wa).pf) {
									
									if (pf.isOverConfirmed()) {
										
										fullyconfirmed = fullyconfirmed.add(pf.amount);
										
									} else if (!pf.isJunk()) { //usually happens for empty fees
										
										tcs.add(pf);
										
									}
									
								}
								
							}
							
							//WalletData dat = pfToAdd.get(wa);
							//BigInteger f = new BigInteger(l.split(" ")[1]);
							
							String lnwrite = wa + " " + (pfToAdd.containsKey(wa) ? nonce.add(pfToAdd.get(wa).nonce) : nonce) + " " + fullyconfirmed.stripTrailingZeros().toPlainString();
							
							for (PendingFunds pf : tcs) {
								
								lnwrite = lnwrite + " " + pf.serialize();
								
							}

							fw.write(lnwrite + "\n");
							
							if (pfToAdd.containsKey(wa)) pfToAdd.remove(wa);
							
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}

				for (String w : pfToAdd.keySet()) {
					
					String lnwrite = w + " " + 0 + " " + 0;
					
					for (PendingFunds pf : pfToAdd.get(w).pf) {
						
						if (!pf.isJunk()) lnwrite = lnwrite + " " + pf.serialize();
						
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
