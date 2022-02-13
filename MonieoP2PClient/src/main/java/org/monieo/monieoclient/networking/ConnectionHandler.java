package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
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
				nodeDo(clientSocket, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void connect(String inet) {
		
		for (Node n : Monieo.INSTANCE.nodes) {
			
			if (n.getAdress().equalsIgnoreCase(inet)) return;
			
		}
		
		try {
			System.out.println("sadsda fat");
			System.out.println("BB" + inet);
			Socket s = new Socket(inet, Monieo.PORT);
			System.out.println("the big cheese");
			nodeDo(s, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void nodeDo(Socket s, boolean server) {
		
		System.out.println(s.getInetAddress().getHostAddress());
		
		Node n = new Node(s, server);
		n.sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_VER, null),
				new PacketCommitment(new Predicate<NetworkCommand>() {

					@Override
					public boolean test(NetworkCommand t) {
						return (t.cmd == NetworkCommandType.ACK_VER);
					}
					
				}));
		Monieo.INSTANCE.nodes.add(n);
		new Thread(n).start();
		
	}

}
