package org.monieo.monieoclient.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class TransactionData extends MonieoDataObject{
	
	public final WalletAdress from;
	public final WalletAdress to;
	public final BigDecimal amount;
	public final BigDecimal fee;
	public final long timestamp;
	public final BigInteger nonce;
	
	public TransactionData(String magicn, String ver, WalletAdress from, WalletAdress to, BigDecimal amount, BigDecimal fee, long timestamp, BigInteger nonce) {
		super(magicn, ver);
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.fee = fee;
		this.timestamp = timestamp;
		this.nonce = nonce;
		
	}
	
	public String serialize() {

		return String.join(" ", magicn, ver, from.adress, to.adress, amount.toPlainString(), fee.toPlainString(), String.valueOf(timestamp), String.valueOf(nonce));
		
	}
	
	public static TransactionData deserialize(String s) {
		
		try {

			String[] data = s.split(" ");
			
			//note that returning a transactiondata without throwing error does not mean the transactiondata is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new TransactionData(data[0], data[1], 
					new WalletAdress(data[2]),
					new WalletAdress(data[3]),
					new BigDecimal(data[4]),
					new BigDecimal(data[5]),
					Long.valueOf(data[6]),
					new BigInteger(data[7]));
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

	@Override
	boolean testValidity() {
		
		if (!from.isValid() || !to.isValid()) return false;
		
		if (amount.signum() != 1 || amount.scale() > 8) return false;
		if (fee.signum() != 1 || fee.scale() > 8) return false;
		
		return true;
		
	}
	
	public boolean testValidityWithBlock(Block b) {
		
		if (!validate()) return false;
		
		BlockMetadata m = b.getMetadata();
		
		BigDecimal bal = BlockMetadata.getSpendableBalance(m.getFullTransactions(from));
		
		if (amount.add(fee).compareTo(bal) == 1) return false;
		
		Block ba = b;
		
		while (ba != null) {
			
			for (AbstractTransaction at : Arrays.asList(b.transactions)) {
				
				if (at instanceof Transaction) {
					
					if (((Transaction) at).d.equals(this)) return false;
					
				}
				
			}
			
			ba = b.getPrevious();
			
			if (ba.header.timestamp < timestamp-7200000) break;
			
		}
		
		return true;
		
	}
	
	public boolean testValidityWithTime(long timetest) {
		
		if (!validate()) return false;
		
		if (timetest > timestamp + 86400000); //1d
		if (timetest + 7200000 < timestamp) return false; //2h
		
		return true;
		
	}
	
}
