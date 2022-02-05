package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

public class Transaction implements AbstractTransaction {
	
	public final TransactionData d;
	public final String pubkey;
	public final String signature;
	 
	public Transaction(TransactionData d, String pubkey, String signature) {
		 
		 this.d = d;
		 this.pubkey = pubkey;
		 this.signature = signature;
		 
	}

	public static Transaction deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			if (data.length != 8) return null;
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Transaction(TransactionData.deserialize(s), data[6], data[7]);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
	public String serialize() {
		
		return String.join(" ", d.serialize(), pubkey, signature);
		
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int confirmations() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public WalletAdress getDestination() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public WalletAdress getSource() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
