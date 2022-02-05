package org.monieo.monieoclient.networking;

import java.net.Socket;
import java.util.List;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;

public interface AbstractNode {

	public Socket getSocket();	
	public void requestAdresses(Callback<List<String>> back);
	public void sendTransaction(Transaction t);
	public void sendBlock(Block b);
	
}
