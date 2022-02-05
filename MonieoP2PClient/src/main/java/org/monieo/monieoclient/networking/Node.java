package org.monieo.monieoclient.networking;

import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;

public class Node {
	
	private Socket socket;
	
	private boolean remoteAcknowledgedLocal = false;
	private boolean localAcknowledgedRemote = false;
	public long minResponseTime = Long.MAX_VALUE;
	
	public Vector<Consumer<Node>> queue = new Vector<Consumer<Node>>();
	
	public Node(Socket s) {
		
		this.socket = s;

	}

	public String getAdress() {
		
		return socket.getInetAddress().getHostAddress();
		
	}
	
	public Socket getSocket() {
		
		return socket;
		
	}
	
	public void queueAction(Consumer<Node> a) {
		
		queue.add(a);
		
	}
	
}
