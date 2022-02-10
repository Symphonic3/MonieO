package org.monieo.monieoclient.blockchain;

import java.math.BigInteger;

import org.monieo.monieoclient.Monieo;

public class BlockHeader{
	
	String mn;
	String pv;
	
	public final String preHash;
	public final String merkleRoot;
	public final long timestamp;
	public final BigInteger nonce;
	public final int amntTransactions;
	public final long height;
	public final BigInteger diff;
	
	public BlockHeader(String mn, String pv, String p, String m, long t, BigInteger n, int a, long h, BigInteger d) {
		this.mn = mn;
		this.pv = pv;
		this.preHash = p;
		this.merkleRoot = m;
		this.timestamp = t;
		this.nonce = n;
		this.amntTransactions = a;
		this.height = h;
		this.diff = d;
		
	}

	public String serialize() {

		return String.join(" ", Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION,
				preHash, merkleRoot, String.valueOf(timestamp), nonce.toString(), String.valueOf(amntTransactions), String.valueOf(height), diff.toString());
		
	}

	public static BlockHeader deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a blockheader without throwing error does not mean the blockheader is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new BlockHeader(data[0], data[1], data[2],
					data[3],
					Long.valueOf(data[4]),
					new BigInteger(data[5]),
					Integer.valueOf(data[6]),
					Long.valueOf(data[7]), new BigInteger(data[8]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

	public String hash() {
		
		return Monieo.sha256d(serialize());
		
	}
	
}
