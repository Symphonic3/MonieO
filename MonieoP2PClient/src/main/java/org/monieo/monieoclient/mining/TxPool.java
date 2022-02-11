package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.networking.NetworkCommand;
import org.monieo.monieoclient.networking.Node;
import org.monieo.monieoclient.networking.NetworkCommand.NetworkCommandType;

public class TxPool {

	private Vector<Transaction> transactions = new Vector<Transaction>();
	
	private static int MAX_TRANSACTION_SIZE = 1000;
	private static int MAX_TXPOOL_SIZE = 1000;
	
	public TxPool() {
		
	}
	
	public void sort() {

		transactions.removeIf(new Predicate<Transaction>() {

			@Override
			public boolean test(Transaction t) {
				return !t.testValidityWithTime(Monieo.INSTANCE.getNetAdjustedTime());
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
		
		if (transactions.size() > MAX_TXPOOL_SIZE) transactions.subList(MAX_TXPOOL_SIZE, transactions.size()).clear();
		
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
		
		int i = 0;
		
		outer: while (true) {
			
			if (lt.size() == i) break;
			
			if (((Transaction) lt.get(i)).testValidityWithBlock(bl)) {
				
				for (AbstractTransaction at : ret) {
					
					if (((Transaction)at).getSource().equals(((Transaction)lt.get(i)).getSource())) {
						
						i++;
						continue outer;
						
					}
					
				}
				
				ret.add(lt.get(i));
			}
			
			i++;
			
			int b = 0;
			
			for (AbstractTransaction t : ret) {
				
				b += t.serialize().getBytes().length;
				
			}
			
			if (b > maxsize) {
				
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
		Node.propagateAll(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_TRANSACTION, t.serialize()), null);
		
		transactions.add(t);
		
		sort();
		
	}
	
}
