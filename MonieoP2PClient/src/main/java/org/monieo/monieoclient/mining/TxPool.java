package org.monieo.monieoclient.mining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.networking.NetworkPacket;
import org.monieo.monieoclient.networking.Node;
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
	
	public void sort() {

		//TODO remove transactions past a week or so

		transactions.sort(new Comparator<Transaction>() {

			@Override
			public int compare(Transaction o1, Transaction o2) {
				
				BigDecimal ratio1 = ratio(o1);
				BigDecimal ratio2 = ratio(o2);

				return ratio2.compareTo(ratio1);
				
			}
			
		});
		
		if (transactions.size() > MAX_TXPOOL_SIZE) transactions.subList(MAX_TXPOOL_SIZE, transactions.size()).clear();
		
		if (f.exists()) {
			
			for (File f2 : f.listFiles()) {
				
				if (!Monieo.INSTANCE.isMonieoFile(f) || !f2.getName().endsWith(".mnot")) throw new RuntimeException("Sanity check failed!");
				
			}
			
			for (File f2 : f.listFiles()) {
				
				f2.delete();
				
			}
			
			for (Transaction t : transactions) {
				
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
		
		return l.d.fee.divide(new BigDecimal(l.serialize().getBytes().length));
		
	}
	
	public List<AbstractTransaction> get() {
		
		sort();
		
		return new ArrayList<AbstractTransaction>(transactions);
		
	}
	
	public List<AbstractTransaction> get(int maxsize, Block bl) {
		
		List<AbstractTransaction> lt = get();
		
		List<AbstractTransaction> ret = new ArrayList<AbstractTransaction>();
		
		outer: for (int i = 0; i < lt.size(); i++) {
			
			Transaction tr = (Transaction) lt.get(i);
			
			if (tr.testHasAmount(bl)) {
				
				for (AbstractTransaction at : ret) {
					
					if (((Transaction)at).equals(tr)) {

						continue outer;
						
					}
					
				}
				
				ret.add(lt.get(i));
				
			}
			
			int b = 0;
			
			for (AbstractTransaction t : ret) {
				
				b += t.serialize().getBytes().length;
				
			}
			
			if (maxsize > 0 && b > maxsize) {
				
				ret.remove(ret.size()-1);
				break;
				
			}
			
		}
		
		return ret;
		
	}
	
	public void add(Transaction t) {
		
		if (t == null || !t.validate()) throw new RuntimeException("Attempted to add invalid/null transaction to txpool!");
		
		if (transactions.contains(t)) return;
		
		int size = t.serialize().getBytes().length;
		
		if (size > MAX_TRANSACTION_SIZE) return;

		//completely optional. find a way to reward this with the protocol.
		Node.propagateAll(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_TRANSACTION, t.serialize()), null);
		
		transactions.add(t);
		
		sort();
		
	}
	
}
