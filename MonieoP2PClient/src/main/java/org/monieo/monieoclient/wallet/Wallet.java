package org.monieo.monieoclient.wallet;

import java.security.KeyPair;

import org.monieo.monieoclient.Monieo;

public class Wallet {

    public String nickname;
    public final String address;
    private final KeyPair keyPair;

    public Wallet(String nick, KeyPair keyPair) {
        this.nickname = nick;
        this.keyPair = keyPair;
        address = Monieo.sha256d(Monieo.serializeKeyPairPublic(keyPair));
    }
    
    public static Wallet newWallet(String nick) {
    	
    	return new Wallet(nick, Monieo.generateKeyPair());
    	
    }
    
    protected String sign(String msg) {
    	
    	//TODO this
    	return null;
    	
    }
    
    public KeyPair getKeyPair() {
    	
    	return keyPair;
    	
    }
    
}
