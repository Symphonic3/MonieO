package org.monieo.monieoclient.blockchain;

import java.math.BigInteger;

import org.monieo.monieoclient.Monieo;

public class BlockHeader extends MonieoDataObject{
	
	public final String preHash;
	public final String merkleRoot;
	public final long timestamp;
	public final BigInteger nonce;
	public final int amntTransactions;
	
	public BlockHeader(String mn, String pv, String p, String m, long t, BigInteger n, int a) {
		super(mn, pv);
		this.preHash = p;
		this.merkleRoot = m;
		this.timestamp = t;
		this.nonce = n;
		this.amntTransactions = a;
		
	}

	public String serialize() {

		return String.join(" ", Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, preHash, merkleRoot, String.valueOf(timestamp), nonce.toString(), String.valueOf(amntTransactions));
		
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
					Integer.valueOf(data[6]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

	@Override
	boolean testValidity() {
		// TODO Auto-generated method stub na dverify that wallets are  v vkllpaliud
		return true;
	}
	
	public String hash() {
		
		return Monieo.sha256d(serialize());
		
	}
	
}
