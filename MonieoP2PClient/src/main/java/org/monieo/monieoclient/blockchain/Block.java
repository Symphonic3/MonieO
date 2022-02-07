package org.monieo.monieoclient.blockchain;

import java.util.ArrayList;
import java.util.List;

import org.monieo.monieoclient.Monieo;

public class Block extends MonieoDataObject{
	
	public final BlockHeader header;
	public final AbstractTransaction[] transactions;
	
	public Block(BlockHeader header, AbstractTransaction... transactions) {
		super(header.magicn, header.ver);
		this.header = header;
		this.transactions = transactions;
		
	}

	public String serialize() {

		String[] ts = new String[transactions.length+1];
		
		ts[0] = header.serialize();
		
		for (int i = 0; i < transactions.length; i++) {
			
			ts[i+1] = transactions[i].serialize();
			
		}
		
		return String.join("\n", ts);
		
	}

	public static Block deserialize(String s) {
		
		try {

			String[] data = s.split("\n");
			
			AbstractTransaction[] transactions = new AbstractTransaction[data.length-1];
			
			transactions[0] = CoinbaseTransaction.deserialize(data[1]);
			
			for (int i = 1; i < transactions.length; i++) {
				
				transactions[i] = Transaction.deserialize(data[i+1]);
				
			}
			
			//note that returning a block without throwing error does not mean the block is valid and does not mean it does not have formatting issues.
			//This should be checked afterwards!
			
			return new Block(BlockHeader.deserialize(data[0]), transactions);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

	@Override
	boolean testValidity() {

		if (transactions.length == 0) return false;
		if (!merkle().equals(header.merkleRoot)) return false;
		if (!header.validate()) return false;
		for (AbstractTransaction t : transactions) {
			
			if (!t.validate()) return false;
			//if (t.expired()) return false;
			
		}
		
		return true;
		
	}
	
	public String merkle() {
		
		List<String> hashes = new ArrayList<String>();
		
		for (int i = 0; i < transactions.length; i++) {
			
			hashes.add(transactions[i].hash());
			
			if (i == transactions.length-1 && (transactions.length % 2 == 1)) {
				
				hashes.add(transactions[i].hash());
				
			}
			
		}
		
		while (hashes.size() != 1) {
			
			List<String> hashesn = new ArrayList<String>();
			
			for (int i = 0; i < hashes.size(); i+=2) {
				
				hashesn.add(Monieo.sha256d(hashes.get(i) + hashes.get(i+1)));
				
			}
			
			hashes = hashesn;
			
		}
	
		return hashes.get(0);
		
	}
	
	public String hash() {
		
		return header.hash();
		
	}

}
