package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.networking.NetworkCommand.NetworkCommandType;

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
		n.sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_VER, null),
				new PacketCommitment(new Predicate<NetworkCommand>() {

					@Override
					public boolean test(NetworkCommand t) {
						return (t.cmd == NetworkCommandType.ACK_VER);
					}
					
				}));
		
		Monieo.INSTANCE.nodes.add(n);
		
		for (AbstractTransaction t : Monieo.INSTANCE.txp.get(-1, Monieo.INSTANCE.getHighestBlock())) {
			
			n.sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_TRANSACTION, t.serialize()), null);
			
		}
		
		n.sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.REQUEST_BLOCKS_AFTER, Monieo.INSTANCE.getHighestBlockHash()), null);
		
		new Thread(n).start();
		
	}

}
