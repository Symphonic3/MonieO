package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

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
	
	public static Transaction createNewTransaction(Wallet fundsOwner, WalletAdress to, BigDecimal amount, BigDecimal fee) {
		
		BigInteger nonce = new BigInteger(String.valueOf(System.currentTimeMillis()) + String.valueOf(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE)));
		
		//should we use net adjusted time here?
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

		if (d == null) return false;
		
		if (!Monieo.assertSupportedProtocol(new String[]{d.mn,d.pv})) return false;
		
		if (!d.from.isValid() || !d.to.isValid()) return false;
		
		if (d.amount.signum() != 1 || d.amount.scale() > 8) return false;
		if (d.fee.signum() == -1 || d.fee.scale() > 8) return false;
		
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
	
	public boolean testHasAmount(Block b) {
		
		BlockMetadata m = b.getMetadata();
		
		BigDecimal bal = BlockMetadata.getSpendableBalance(m.getFullTransactions(d.from));
		
		if (d.amount.add(d.fee).compareTo(bal) == 1) return false;
		
		Block ba = b;
		
		while (ba != null) {
			
			for (AbstractTransaction at : Arrays.asList(b.transactions)) {
				
				if (at instanceof Transaction) {
					
					if (((Transaction) at).d.equals(this.d)) return false;
					
				}
				
			}
			
			ba = ba.getPrevious();
			
			if (ba.header.timestamp < d.timestamp-7200000) break;
			
		}
		
		return true;
		
	}
	
	public boolean expired(long timetest) {
		
		if (timetest > d.timestamp + 86400000) return true; //1d
		
		return false;
		
	}
	
	public boolean tooFarInFuture(long timetest) {
		
		if (timetest + 7200000 < d.timestamp) return true; //2h
		
		return false;
		
	}
	
}
