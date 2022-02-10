package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.monieo.monieoclient.blockchain.Transaction;

public class TxPool {

	private Vector<Transaction> transactions = new Vector<Transaction>();
	
	private static int MAX_TRANSACTION_SIZE = 1000;
	private static int MAX_TXPOOL_SIZE = 1000;
	
	public TxPool() {
		
	}
	
	public void sort() {

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
	
	public List<Transaction> get() {
		
		sort();
		
		return transactions;
		
	}
	
	public void add(Transaction t) {
		
		if (t == null || !t.validate()) throw new RuntimeException("Attempted to add invalid/null block to txpool!");
		
		int size = t.serialize().getBytes().length;
		
		if (size > MAX_TRANSACTION_SIZE) return;
		
		transactions.add(t);
		
		sort();
		
	}
	
}
