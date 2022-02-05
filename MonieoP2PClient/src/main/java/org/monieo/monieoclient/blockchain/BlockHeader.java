package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
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

	public String serialize() {

		return String.join(" ", preHash, merkleRoot, String.valueOf(timestamp), nonce.toString(), String.valueOf(amntTransactions));
		
	}

	public static BlockHeader deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			if (data.length != 5) return null;
			
			//note that returning a blockheader without throwing error does not mean the blockheader is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new BlockHeader(data[0],
					data[1],
					Long.valueOf(data[2]),
					new BigInteger(data[3]),
					Integer.valueOf(data[4]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
}
