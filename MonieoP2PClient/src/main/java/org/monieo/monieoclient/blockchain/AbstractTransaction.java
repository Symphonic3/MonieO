package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

public abstract class AbstractTransaction extends MonieoDataObject{

	public AbstractTransaction(String m, String v) {
		super(m, v);
	}

	public abstract WalletAdress getDestination();
	public abstract BigDecimal getAmount();
	
}
