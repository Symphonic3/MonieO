package org.monieo.monieoclient.blockchain;

import java.math.BigInteger;

public class BlockHeader {
	
	public final String preHash;
	public final String merkleRoot;
	public final long timestamp;
	public final BigInteger nonce;
	public final int amntTransactions;
	
	public BlockHeader(String p, String m, long t, BigInteger n, int a) {
		
		this.preHash = p;
		this.merkleRoot = m;
		this.timestamp = t;
		this.nonce = n;
		this.amntTransactions = a;
		
	}

}
