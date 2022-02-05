package org.monieo.monieoclient.networking;

import java.net.Socket;
import java.util.List;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;

public interface AbstractNode {

	public Socket getSocket();
	public void requestBlocksAfter(String prevhash, Callback<List<Block>> back);
	public void requestBlock(String hash, Callback<Block> back);
	public void requestAdresses(Callback<List<String>> back);
	public void sendTransactions(List<Transaction> t);
	public void sendBlocks(List<Block> b);
	public void sendAdresses(List<String> addr);
	
}
