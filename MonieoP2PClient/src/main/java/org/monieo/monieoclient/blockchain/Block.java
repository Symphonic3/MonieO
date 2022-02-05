package org.monieo.monieoclient.blockchain;

public class Block {
	
	public final BlockHeader header;
	public final Transaction[] transactions;
	
	public Block(BlockHeader header, Transaction... transactions) {
		
		this.header = header;
		this.transactions = transactions;
		
	}

}
