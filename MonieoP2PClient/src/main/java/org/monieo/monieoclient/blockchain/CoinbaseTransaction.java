package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

public class CoinbaseTransaction extends AbstractTransaction{
	
	public final String destination;
	public final BigDecimal amount;
	
	public CoinbaseTransaction(String magicn, String ver, String d, BigDecimal a) {
		super(magicn, ver);
		this.destination = d;
		this.amount = a;
		
	}
	@Override
	public String getDestination() {
		return destination;
	}

	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	boolean testValidity() {
		return false;
	}
	
	public boolean validate(Block b) {
		
		if (amount.signum() != 1 || amount.scale() > 8) return false;
		
		if (amount.compareTo(Block.getMaxCoinbase(b.header.height)) != 1) return true;
		
		return false;
		
	}
	
	public static CoinbaseTransaction deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new CoinbaseTransaction(data[0], data[1], new String(data[2]), new BigDecimal(data[3]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	@Override
	public String serialize() {
		
		return String.join(" ", magicn, ver, destination, amount.toPlainString());
		
	}

}
