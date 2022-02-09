package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

import org.monieo.monieoclient.Monieo;

public class PendingFunds {
	
	//If a transaction is > 100 confirmations, it is combined at 100 confirmations with any others

	long confRemain;
	BigDecimal amount;
	
	public PendingFunds(BigDecimal transactionAmount, long confRemain) {
		
		//the fields we get here are already validated, presumably

		this.amount = transactionAmount;
		this.confRemain = confRemain;
		
	}
	
	public String serialize() {
		
		if (confRemain <= 0) {
			
			return amount.toPlainString();
			
		} else {
			
			return amount.toPlainString() + ":" + String.valueOf(confRemain);			
			
		}
		
	}
	
	public static PendingFunds deserialize(String s) {
		
		String[] data = s.split(":");
		
		if (data.length == 1) {
			
			return new PendingFunds(new BigDecimal(data[0]), 0);
			
		} else if (data.length == 2) {
			
			return new PendingFunds(new BigDecimal(data[0]), Long.valueOf(data[1]));
			
		}
		
		return null;
		
	}
	
	public boolean spendable() {
		
		return confRemain <= 0;
		
	}

}