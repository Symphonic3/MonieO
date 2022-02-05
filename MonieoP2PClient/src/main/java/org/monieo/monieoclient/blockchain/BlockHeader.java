package org.monieo.monieoclient.blockchain;

import java.math.BigInteger;

import org.monieo.monieoclient.Monieo;

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
			if (data.length != 7) return null;
			if (!Monieo.assertSupportedProtocol(data)) return null;
			
			//note that returning a blockheader without throwing error does not mean the blockheader is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new BlockHeader(data[2],
					data[3],
					Long.valueOf(data[4]),
					new BigInteger(data[5]),
					Integer.valueOf(data[6]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
}
