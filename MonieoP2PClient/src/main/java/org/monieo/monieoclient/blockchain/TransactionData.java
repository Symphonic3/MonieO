package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionData {
	
	public final String from;
	public final String to;
	public final BigDecimal amount;
	public final BigDecimal fee;
	public final BigInteger nonce;
	
	public String mn;
	public String pv;
	
	public TransactionData(String magicn, String ver, String from, String to, BigDecimal amount, BigDecimal fee, BigInteger nonce) {
		this.mn = magicn;
		this.pv = ver;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
		this.nonce = nonce;
		
	}
	
	public String serialize() {

		return String.join(" ", mn, pv, from, to, amount.toPlainString(), fee.toPlainString(), String.valueOf(nonce));
		
	}
	
	public static TransactionData deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transactiondata without throwing error does not mean the transactiondata is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new TransactionData(data[0], data[1], 
					new String(data[2]),
					new String(data[3]),
					new BigDecimal(data[4]),
					new BigDecimal(data[5]),
					new BigInteger(data[6]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
}
