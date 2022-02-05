package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.monieo.monieoclient.Monieo;

public class TransactionData extends MonieoDataObject{
	
	public final WalletAdress from;
	public final WalletAdress to;
	public final BigDecimal amount;
	public final BigDecimal fee;
	public final long timestamp;
	public final BigInteger nonce;
	
	public TransactionData(String magicn, String ver, WalletAdress from, WalletAdress to, BigDecimal amount, BigDecimal fee, long timestamp, BigInteger nonce) {
		super(magicn, ver);
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
		this.timestamp = timestamp;
		this.nonce = nonce;
		
	}
	
	public String serialize() {

		return String.join(" ", magicn, ver, from.adress, to.adress, amount.toPlainString(), fee.toPlainString(), String.valueOf(timestamp), String.valueOf(nonce));
		
	}
	
	public static TransactionData deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transactiondata without throwing error does not mean the transactiondata is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new TransactionData(data[0], data[1], 
					new WalletAdress(data[2]),
					new WalletAdress(data[3]),
					new BigDecimal(data[4]),
					new BigDecimal(data[5]),
					Long.valueOf(data[6]),
					new BigInteger(data[7]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

	@Override
	boolean testValidity() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
