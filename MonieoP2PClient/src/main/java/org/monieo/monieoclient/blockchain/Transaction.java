package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PublicKey;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.wallet.Wallet;

public class Transaction extends AbstractTransaction {
	
	public final TransactionData d;
	public final String pubkey;
	public final String signature;
	 
	public Transaction(TransactionData d, String pubkey, String signature) {
		 super(d.mn, d.pv);
		 this.d = d;
		 this.pubkey = pubkey;
		 this.signature = signature;
		 
	}
	
	public static Transaction createNewTransaction(Wallet fundsOwner, String to, BigDecimal amount, BigDecimal fee) {
		
		if (!fundsOwner.hasSK) return null;
		
		BigInteger nonce = Monieo.INSTANCE.getHighestBlock().getMetadata().getWalletData(fundsOwner.getAsString()).nonce;
		
		//should we use net adjusted time here?
		TransactionData td = new TransactionData(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, fundsOwner.getAsString(), to, amount, fee, nonce);
		
		String sig = fundsOwner.sign(td.serialize());
		
		if (sig == null) return null;
		
		return new Transaction(td, Monieo.base64(fundsOwner.getKeyPair().getPublic().getEncoded()), sig);
		
	}

	public static Transaction deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transaction without throwing error does not mean the transaction is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Transaction(TransactionData.deserialize(s), data[7], data[8]);
			
		} catch (Exception e) {

			//invalid data (great code)
			return null;
			
		}
		
	}
	
	public String serialize() {
		
		return String.join(" ", d.serialize(), pubkey, signature);
		
	}
	
	@Override
	public String getDestination() {
		return d.to;
	}
	
	public String getSource() {
		return d.from;
	}

	@Override
	public BigDecimal getAmount() {
		return d.amount;
	}

	@Override
	boolean testValidity() {

		if (d == null) return false;
		
		if (!Monieo.assertSupportedProtocol(new String[]{d.mn,d.pv})) return false;
		
		//if (d.from.length() != 64 || d.to.length() != 64) return false;
		
		if (d.amount.signum() != 1 || d.amount.scale() > 8) return false;
		if (d.fee.signum() == -1 || d.fee.scale() > 8) return false;
		
		if (!Wallet.getAddress(pubkey).equals(d.from)) return false;
		
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
	
	//testHasAmount method removed because this needs to be checked dynamically
	
}
