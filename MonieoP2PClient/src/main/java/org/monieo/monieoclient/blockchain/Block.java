package org.monieo.monieoclient.blockchain;

public class Block {
	
	public final BlockHeader header;
	public final Transaction[] transactions;
	
	public Block(BlockHeader header, Transaction... transactions) {
		
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
			if (data.length < 2) return null;
			
			Transaction[] transactions = new Transaction[data.length-1];
			
			for (int i = 0; i < transactions.length; i++) {
				
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

}
