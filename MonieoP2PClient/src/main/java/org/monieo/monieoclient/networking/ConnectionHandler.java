package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.networking.NetworkPacket.NetworkPacketType;

public class ConnectionHandler implements Runnable{

	private ServerSocket serverSocket;
	
	@Override
	public void run() {
		
        try {
			serverSocket = new ServerSocket(Monieo.PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			
			try {
				Socket clientSocket = serverSocket.accept();
				
				String inet = clientSocket.getInetAddress().getHostAddress();
				
				for (int k = 0; k < Monieo.INSTANCE.nodes.size(); k++) {
					
					Node n = Monieo.INSTANCE.nodes.get(k);
					
					if (n.getAdress().equalsIgnoreCase(inet)) {
						
						n.disconnect();
						k--;
						
					}
					
				}
				
				nodeDo(clientSocket, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void connect(String inet) {
		
		/*for (Node n : Monieo.INSTANCE.nodes) {
			
			if (n.getAdress().equalsIgnoreCase(inet)) return;
			
		}*/
		
		try {
			Socket s = new Socket(inet, Monieo.PORT);
			nodeDo(s, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void nodeDo(Socket s, boolean server) {
		
		System.out.println("Connected to: " + s.getInetAddress().getHostAddress());
		
		Node n = new Node(s, server);
		n.sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_VER, null));
		
		Monieo.INSTANCE.nodes.add(n);
		
		Monieo.INSTANCE.ui.totConnectedNodesDisplay.setText(String.valueOf(Monieo.INSTANCE.nodes.size()));
		
		new Thread(n).start();
		
	}

}
