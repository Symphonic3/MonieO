package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionData {
	
	public final WalletAdress from;
	public final WalletAdress to;
	public final BigDecimal amount;
	public final BigDecimal fee;
	public final long timestamp;
	public final BigInteger nonce;
	
	public TransactionData(WalletAdress from, WalletAdress to, BigDecimal amount, BigDecimal fee, long timestamp, BigInteger nonce) {
		
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
		this.timestamp = timestamp;
		this.nonce = nonce;
		
	}
	
	public String serialize() {

		return String.join(" ", from.adress, to.adress, amount.toPlainString(), fee.toPlainString(), String.valueOf(timestamp), String.valueOf(nonce));
		
	}
	
	public static TransactionData deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			if (data.length != 8) return null;
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new TransactionData(new WalletAdress(data[0]),
					new WalletAdress(data[1]),
					new BigDecimal(data[2]),
					new BigDecimal(data[3]),
					Long.valueOf(data[4]),
					new BigInteger(data[5]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
}
