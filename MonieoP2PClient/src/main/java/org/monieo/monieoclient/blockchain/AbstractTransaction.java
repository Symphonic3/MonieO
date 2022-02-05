package org.monieo.monieoclient.blockchain;

public interface AbstractTransaction {

	public boolean isValid();
	public int confirmations();
	public WalletAdress getDestination();
	
}
