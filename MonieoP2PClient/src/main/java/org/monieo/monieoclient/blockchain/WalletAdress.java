package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;

public class WalletAdress {
	
	public final String adress;
	
	public WalletAdress(String adress) {
		
		this.adress = adress;
		
	}
	
	public boolean isValid() {
		
		return adress.length() == 64;
		
	}

}
