package org.monieo.monieoclient.wallet;

import java.security.KeyPair;
import java.security.Signature;
import java.util.Base64;

import org.monieo.monieoclient.Monieo;

public class Wallet {

    public String nickname;
    private final String address;
    private final KeyPair keyPair;

    public Wallet(String nick, KeyPair keyPair) {
        this.nickname = nick;
        this.keyPair = keyPair;
        address = Monieo.sha256d(Monieo.serializeKeyPairPublic(keyPair));
    }
    
    public static Wallet newWallet(String nick) {
    	
    	return new Wallet(nick, Monieo.generateKeyPair());
    	
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
