package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.wallet.Wallet;

public class Transaction extends AbstractTransaction {
	
	public final TransactionData d;
	public final String pubkey;
	public final String signature;
	 
	public Transaction(TransactionData d, String pubkey, String signature) {
		 super(d.magicn, d.ver);
		 this.d = d;
		 this.pubkey = pubkey;
		 this.signature = signature;
		 
	}
	
	public static Transaction createNewTransaction(Wallet fundsOwner, WalletAdress to, BigDecimal amount, BigDecimal fee) {
		
		BigInteger nonce = new BigInteger(String.valueOf(System.currentTimeMillis()) + String.valueOf(ThreadLocalRandom.current().nextInt()));
		
		TransactionData td = new TransactionData(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, fundsOwner.getAsWalletAdress(), to, amount, fee, Monieo.INSTANCE.getNetAdjustedTime(), nonce);
		
		String sig = fundsOwner.sign(td.serialize());
		
		return new Transaction(td, Monieo.serializeKeyPairPublic(fundsOwner.getKeyPair()), sig);
		
	}

	public static Transaction deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Transaction(TransactionData.deserialize(s), data[8], data[9]);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}
	
	public String serialize() {
		
		return String.join(" ", d.serialize(), pubkey, signature);
		
	}
	
	@Override
	public WalletAdress getDestination() {
		return d.to;
	}
	
	public WalletAdress getSource() {
		return d.from;
	}

	@Override
	public BigDecimal getAmount() {
		return d.amount;
	}

	@Override
	boolean testValidity() {

		if (!d.validate()) return false;
		
		if (!Monieo.sha256d(pubkey).equals(d.from.adress)) return false;
		
		PublicKey pk = Monieo.deserializePublicKey(pubkey);
		
		if (pk == null) return false;
		try {
			
			if (Monieo.verifySignature(d.serialize(), signature, pk)) return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
		
	}
	
	public boolean testValidityWithEffectiveMeta(BlockMetadata m, long timetest) {
		
		if (!testValidityWithTime(timetest)) return false;
		
		return d.testValidityWithEffectiveMeta(m, timetest);
		
	}
	
	public boolean testValidityWithTime(long timetest) {
		
		if (!validate()) return false;
		
		return d.testValidityWithTime(timetest);
		
	}
	
}
