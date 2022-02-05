package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

public class CoinbaseTransaction extends AbstractTransaction{
	
	public final WalletAdress destination;
	public final BigDecimal amount;
	
	public CoinbaseTransaction(String magicn, String ver, WalletAdress d, BigDecimal a) {
		super(magicn, ver);
		this.destination = d;
		this.amount = a;
		
	}
	@Override
	public WalletAdress getDestination() {
		return destination;
	}

	@Override
	public BigDecimal getAmount() {
		return amount;
	}

	@Override
	boolean testValidity() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static CoinbaseTransaction deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new CoinbaseTransaction(data[0], data[1], new WalletAdress(data[2]), new BigDecimal(data[3]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	@Override
	public String serialize() {
		
		return String.join(" ", magicn, ver, destination.adress, amount.toPlainString());
		
	}

}
