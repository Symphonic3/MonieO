package org.monieo.monieoclient.wallet;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Signature;
import org.monieo.monieoclient.Monieo;

public class Wallet {

	public String nickname;
	private final String address;
	private final KeyPair keyPair;
	
	public final boolean hasSK;
	
	public static final String BASE_58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";  // 0-9 and a-z except 0OIl
	private static final BigInteger BASE_58_LEN = BigInteger.valueOf(BASE_58.length());
	
	public Wallet(String nick, KeyPair keyPair) {
		this.nickname = nick;
		this.keyPair = keyPair;
		hasSK = true;
		address = getAddress(Monieo.base64(keyPair.getPublic().getEncoded()));
	}
	
	public Wallet(String nick, PublicKey key) {
		this.nickname = nick;
		this.keyPair = new KeyPair(key, null);
		hasSK = false;
		address = getAddress(Monieo.base64(keyPair.getPublic().getEncoded()));
	}
	
	public static Wallet newWallet(String nick) {
		
		return new Wallet(nick, Monieo.generateKeyPair());
		
	}
	
	public static String getAddress(String pubkey) {
		
		byte[] b = Monieo.sha256dRaw(pubkey);
		
		return getBase58(b);
		
	}
	
	private static String getBase58(byte[] data) {
		
		String s = "";
		BigInteger n = new BigInteger(1, data);
		
		while (n.signum() != 0) {
			
			BigInteger[] r = n.divideAndRemainder(BASE_58_LEN);
			
			s = s + BASE_58.charAt(r[1].intValue());
			
			n = r[0];
			
		}
		
		return s;
	}
	
	public String sign(String msg) {
		
		if (!hasSK) return null;
		
		try {
			
			Signature privateSignature = Signature.getInstance("SHA256withRSA");
			privateSignature.initSign(keyPair.getPrivate());
			privateSignature.update(msg.getBytes("UTF8"));

			byte[] signature = privateSignature.sign();

			return Monieo.base64(signature);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
			
		}

	}
	
	public KeyPair getKeyPair() {
		
		return keyPair;
		
	}
	
	public String getAsString() {
		
		return address;
		
	}
	
}
