package org.monieo.monieoclient.blockchain;

import java.math.BigInteger;
import java.util.List;

public class WalletData {
	
	public String wa;
	public BigInteger nonce;
	public List<PendingFunds> pf;
	
	public WalletData(String wa, BigInteger nonce, List<PendingFunds> pf) {
		
		this.wa = wa;
		this.nonce = nonce;
		this.pf = pf;
		
	}
	
}
