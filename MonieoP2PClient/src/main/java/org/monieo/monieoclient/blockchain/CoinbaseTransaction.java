package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

public class CoinbaseTransaction implements AbstractTransaction{
	
	public final WalletAdress destination;
	public final BigDecimal amount;
	
	public CoinbaseTransaction(WalletAdress d, BigDecimal a) {
		
		this.destination = d;
		this.amount = a;
		
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
		return destination;
	}

}
