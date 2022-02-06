package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

import org.monieo.monieoclient.Monieo;

public abstract class AbstractTransaction extends MonieoDataObject{

	public AbstractTransaction(String m, String v) {
		super(m, v);
	}

	public abstract WalletAdress getDestination();
	public abstract BigDecimal getAmount();
	
	public abstract String serialize();
	public String hash() {
		
		return Monieo.sha256d(serialize());
		
	}
	
	public abstract boolean expired();
	
}
