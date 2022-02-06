package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.wallet.Wallet;

public class Transaction extends AbstractTransaction {
	
	public final TransactionData d;
	public final String pubkey;
	public final String signature;
	 
	public Transaction(TransactionData d, String pubkey, String signature) {
		 super(d.magicn, d.ver);
		 this.d = d;
		 this.pubkey = pubkey;
		 this.signature = signature;
		 
	}
	
	public static Transaction createNewTransaction(Wallet fundsOwner, WalletAdress to, BigDecimal amount, BigDecimal fee) {
		
		return null;
		
	}

	public static Transaction deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Transaction(TransactionData.deserialize(s), data[8], data[9]);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
	public String serialize() {
		
		return String.join(" ", d.serialize(), pubkey, signature);
		
	}
	
	@Override
	public WalletAdress getDestination() {
		return d.to;
	}
	
	public WalletAdress getSource() {
		return d.from;
	}

	@Override
	public BigDecimal getAmount() {
		return d.amount;
	}

	@Override
	boolean testValidity() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
