package org.monieo.monieoclient.wallet;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;

import org.monieo.monieoclient.Monieo;

public class Wallet {

    public String nickname;
    private final String address;
    private final KeyPair keyPair;
    
	public static final String BASE_58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";  // 0-9 and a-z except 0OIl
	private static final BigInteger BASE_58_LEN = BigInteger.valueOf(BASE_58.length());

    public Wallet(String nick, KeyPair keyPair) {
        this.nickname = nick;
        this.keyPair = keyPair;
        address = getAddress(Monieo.serializeKeyPairPublic(keyPair));
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
    	
    	try {
    		
        	Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(keyPair.getPrivate());
            privateSignature.update(msg.getBytes("UTF8"));

            byte[] signature = privateSignature.sign();

            return Base64.getEncoder().encodeToString(signature);
    		
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
