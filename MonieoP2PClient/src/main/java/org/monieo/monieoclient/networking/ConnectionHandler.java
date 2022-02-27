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
				
				if (Monieo.INSTANCE.nodes.size() >= Monieo.MAX_CONNECTIONS) continue;
				
				Socket clientSocket = serverSocket.accept();
				
				String inet = clientSocket.getInetAddress().getHostAddress();
				
				if (Monieo.INSTANCE.knownNodes.contains(Monieo.INSTANCE.fetchByAdress(inet)) && !Monieo.INSTANCE.getValidNodesRightNow().contains(inet)) {
					
					System.out.println("bue");
					clientSocket.close();
					continue;
					
				}
				
				for (int k = 0; k < Monieo.INSTANCE.nodes.size(); k++) {
					
					Node n = Monieo.INSTANCE.nodes.get(k);
					
					if (n.getAdress().equalsIgnoreCase(inet)) {
						
						n.disconnect();
						k--;
						
					}
					
				}
				
				nodeDo(clientSocket, true);
			} catch (IOException e) {
				//TODO remove this
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
		} catch (IOException e) {}
		
	}
	
	private void nodeDo(Socket s, boolean server) {
		
		System.out.println("Connected to: " + s.getInetAddress().getHostAddress());
		
		Node n = new Node(s, server);
		n.queueNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_VER, String.valueOf(System.currentTimeMillis())));
		
		Monieo.INSTANCE.nodes.add(n);
		
		if (Monieo.INSTANCE.ui != null) Monieo.INSTANCE.ui.refresh(false, false);
		
		new Thread(n).start();
		
	}

}
