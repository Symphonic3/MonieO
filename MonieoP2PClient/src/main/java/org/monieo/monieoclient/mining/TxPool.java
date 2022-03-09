package org.monieo.monieoclient.mining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.BlockMetadata;
import org.monieo.monieoclient.blockchain.PendingFunds;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.blockchain.WalletData;
import org.monieo.monieoclient.networking.NetworkPacket;
import org.monieo.monieoclient.networking.Node;
import org.monieo.monieoclient.wallet.Wallet;
import org.monieo.monieoclient.networking.NetworkPacket.NetworkPacketType;

public class TxPool {

	private Vector<Transaction> transactions = new Vector<Transaction>();
	
	private static int MAX_TRANSACTION_SIZE = 1000;
	private static int MAX_TXPOOL_SIZE = 1000;
	
	File f;
	
	public TxPool(File f) {
		
		this.f = f;
		
		if (f.exists()) {
			
			for (File f2 : f.listFiles()) {
				
				transactions.add(Transaction.deserialize(Monieo.readFileData(f2)));
				
			}
			
			sort();
			
		}
		
	}
	
	public void trackTx(Transaction t) {
		
		List<String> track = new ArrayList<String>(Arrays.asList(Monieo.readFileData(Monieo.INSTANCE.trackTx).split("\n")));
		track.add(t.hash());
		
		try (FileWriter fw = new FileWriter(Monieo.INSTANCE.trackTx, false)) {

			fw.write(String.join("\n", track));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public List<Transaction> getTrackedTx() {

		List<Transaction> ret = new ArrayList<Transaction>();
		
		List<String> track = new ArrayList<String>(Arrays.asList(Monieo.readFileData(Monieo.INSTANCE.trackTx).split("\n")));
		
		for (Transaction t : transactions) {
			
			if (track.contains(t.hash())) {
				
				ret.add(t);
				
			}
			
		}
		
		try (FileWriter fw = new FileWriter(Monieo.INSTANCE.trackTx, false)) {
			
			for (Transaction t : ret) {
				
				fw.append(t.hash() + "\n");
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ret.retainAll(get(-1, Monieo.INSTANCE.getHighestBlock()));
		
		return ret;
		
	}
	
	private boolean shouldBeInPool(Transaction t) {
		
		Block old = Monieo.INSTANCE.getHighestBlock();
		
		for (int i = 0; i < Monieo.CONFIRMATIONS_IGNORE; i++) {
			
			Block to = old.getPrevious();
			
			if (Monieo.genesis().equals(to)) {
				
				old = to;
				break;
				
			} else if (to == null) {
				
				break;
				
			}
			
			old = to;
			
		}
		
		BlockMetadata m = old.getMetadata();
		
		if (m.getWalletData(t.getSource()).nonce.compareTo(t.d.nonce) == 1) {
			
			return false;
			
		} else return true;
		
	}
	
	@SuppressWarnings("unchecked")
	public void sort() {
		
		transactions.removeIf(new Predicate<Transaction>() {

			@Override
			public boolean test(Transaction t) {
				return !shouldBeInPool(t);
			}
			
		});
		
		transactions.sort(new Comparator<Transaction>() {

			@Override
			public int compare(Transaction o1, Transaction o2) {
				
				BigDecimal ratio1 = ratio(o1);
				BigDecimal ratio2 = ratio(o2);

				return ratio2.compareTo(ratio1);
				
			}
			
		});
		
		//this is not optimal sorting. say alice has tx a with ratio 1 and tx b with ratio 2. this algorithm
		//will prioritize txa over another transaction of ratio 2. this can be fixed by grouping transactions together
		//so that the first transaction's fee is prioritized. However, if we do this, we need to check the context
		//with which we request the transaction so that a transaction already in a block with a high fee does not
		//affect sorting of transactions from the same source with a lower fee.
		//i'm sure there is a simple solution to this dilemma, someone can fix it later
		
		transactions.sort(new Comparator<Transaction>() {

			@Override
			public int compare(Transaction o1, Transaction o2) {

				if (o1.getSource().equals(o2.getSource())) {
					
					return o1.d.nonce.compareTo(o2.d.nonce);
					
				} else return 0;
				
			}
			
		});
		
		if (transactions.size() > MAX_TXPOOL_SIZE) transactions.subList(MAX_TXPOOL_SIZE, transactions.size()).clear();
		
		//remove transactions with same nonce (bad algorithm)
		outer: for (int i = 0; i < transactions.size(); i++) {
			
			inner: for (int k = 0; k < transactions.size(); k++) {
				
				if (i != k) {
					
					Transaction t = transactions.get(i);
					Transaction t2 = transactions.get(k);
					
					if (t.getSource().equals(t2.getSource())) {
						
						if (t.d.nonce.equals(t2.d.nonce)) {
							
							if (ratio(t).compareTo(ratio(t2)) == 1) {
								
								transactions.remove(k);
								k--;
								continue inner;
								
							} else {
								
								transactions.remove(i);
								i--;
								continue outer;
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		if (f.exists()) {
			
			for (File f2 : f.listFiles()) {
				
				if (!Monieo.INSTANCE.isMonieoFile(f) || !f2.getName().endsWith(".mnot")) throw new RuntimeException("Sanity check failed!");
				
			}
			
			for (File f2 : f.listFiles()) {
				
				f2.delete();
				
			}
			
			for (Transaction t : (Vector<Transaction>)transactions.clone()) {
				
				File f2 = new File(f.getPath() + "/" + t.hash() + ".mnot");
				
				try {
					f2.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try (FileWriter fw = new FileWriter(f2, false)) {
					
					fw.write(t.serialize());
					
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				
			}
			
		}
		
	}
	
	public BigDecimal ratio(Transaction l) {
		
		return l.d.fee.divide(new BigDecimal(l.serialize().getBytes().length), 100, RoundingMode.HALF_UP);
		
	}
	
	public List<AbstractTransaction> get() {
		
		sort();
		
		return new ArrayList<AbstractTransaction>(transactions);
		
	}
	
	//this is another example of a terrible algorithm! but it works!
	//TODO this algorithm takes a horrendous amount of time to run. we need to minimize how much this runs by using a constant miner that listens to when it should restart
	public List<AbstractTransaction> get(int maxsize, Block bl) {
		
		List<AbstractTransaction> lt = get();
		
		List<AbstractTransaction> ret = new ArrayList<AbstractTransaction>();
		
		BlockMetadata m = bl.getMetadata();
		
		HashMap<String, WalletData> sources = new HashMap<String, WalletData>();
		
		for (int i = 0; i < lt.size(); i++) {
			
			Transaction tr = (Transaction) lt.get(i);
			
			if (!sources.containsKey(tr.getSource())) sources.put(tr.getSource(), m.getWalletData(tr.getSource()));

			WalletData w = sources.get(tr.getSource());
			//althought transactions are ordered based on nonce, sometimes the nonce may be too high. this check is nessecary.
			if (!tr.d.nonce.equals(w.nonce)) continue;
			BigDecimal bal = BlockMetadata.getSpendableBalance(w.pf);
			bal = bal.subtract(tr.d.amount.add(tr.d.fee));
			if (bal.compareTo(BigDecimal.ZERO) == -1) continue;
			
			w.nonce = w.nonce.add(BigInteger.ONE);
			w.pf.add(new PendingFunds(tr.d.amount.add(tr.d.fee).negate(), 0));

			if (!ret.contains(lt.get(i))) {
				
				ret.add(lt.get(i));
				
				int b = 0;
				
				for (AbstractTransaction t : ret) {
					
					b += t.serialize().getBytes().length;
					
				}
				
				if (maxsize > 0 && b > maxsize) {
					
					ret.remove(ret.size()-1);
					break;
					
				}
				
			}
			
		}
		
		return ret;
		
	}
	
	public void add(Transaction t) {
		
		if (t == null || !t.validate()) throw new RuntimeException("Attempted to add invalid/null transaction to txpool!");

		if (transactions.contains(t)) return;

		if (!shouldBeInPool(t)) return;

		int size = t.serialize().getBytes().length;

		if (size > MAX_TRANSACTION_SIZE) return;

		//completely optional. find a way to reward this with the protocol.
		Node.propagateAll(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_TRANSACTION, t.serialize()));

		transactions.add(t);
		
		for (Wallet w : Monieo.INSTANCE.myWallets) {
			
			if (w.getAsString().equals(t.getSource())) {
				
				trackTx(t);
				Monieo.INSTANCE.ui.refresh(false, false);
				break;
				
			}
			
		}
		
		sort();
		
	}
	
}
