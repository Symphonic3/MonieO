package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

import org.monieo.monieoclient.Monieo;

public class PendingFunds {
	
	//If a transaction is > CONFIRMATIONS_BLOCK_SENSITIVE, it is combined with no confirmations field with any others

	long conf;
	BigDecimal amount;
	
	public PendingFunds(BigDecimal transactionAmount, long conf) {
		
		//the fields we get here are already validated, presumably

		this.amount = transactionAmount.stripTrailingZeros();
		this.conf = conf;
		
	}
	
	public String serialize() {
		
		if (isOverConfirmed()) {
			
			return amount.toPlainString();
			
		} else {
			
			return amount.toPlainString() + ":" + String.valueOf(conf);			
			
		}
		
	}
	
	public static PendingFunds deserialize(String s) {
		
		String[] data = s.split(":");
		
		if (data.length == 1) {
			
			return new PendingFunds(new BigDecimal(data[0]), Monieo.CONFIRMATIONS_IGNORE);
			
		} else if (data.length == 2) {
			
			return new PendingFunds(new BigDecimal(data[0]), Long.valueOf(data[1]));
			
		}
		
		return null;
		
	}
	
	public boolean isSpendable() {
		
		return conf >= 0;
		
	}
	
	public boolean isOverConfirmed() {
		
		return conf >= Monieo.CONFIRMATIONS_IGNORE;
		
	}
	
	public boolean isJunk() {
		
		return amount.compareTo(BigDecimal.ZERO) == 0;
		
	}

}
