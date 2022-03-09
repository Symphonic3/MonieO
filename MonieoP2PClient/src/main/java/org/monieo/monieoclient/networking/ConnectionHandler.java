package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.networking.NetworkPacket.NetworkPacketType;

public class ConnectionHandler implements Runnable{

	private ServerSocket serverSocket;
	
	@Override
	public void run() {
		
		try {
			serverSocket = new ServerSocket(Monieo.PORT);
		} catch (IOException e1) {
			if (e1 instanceof BindException) {
				
				System.out.println("You cannot have two instances of MonieO running simultaneously!");
				System.exit(0);
				
			}
			e1.printStackTrace();
		}
		
		while (true) {
			
			try {
				
				Socket clientSocket = serverSocket.accept();
				String add = clientSocket.getInetAddress().getHostAddress();
				
				if (Monieo.INSTANCE.nam.isBanned(add)) {
					
					System.out.println("bue");
					clientSocket.close();
					continue;
					
				}
				
				if (full()) {
					
					String e = Monieo.INSTANCE.nam.whoToEvict(add);
					
					if (e == null) {
						
						System.out.println("bue2");
						clientSocket.close();
						continue;
						
					} else {
						
						//TODO ad
						/*for (int k = 0; k < Monieo.INSTANCE.nodes.size(); k++) {
							
							Node n = Monieo.INSTANCE.nodes.get(k);
							
							if (n.getAdress().equalsIgnoreCase(inet)) {
								
								n.disconnect(false);
								k--;
								
							}
							
						}*/
						
					}
					
				}
				
				nodeDo(clientSocket, true);
			} catch (IOException e) {
				//TODO remove this
				e.printStackTrace();
			}
			
		}
		
	}
	
	public boolean full() {
		
		return Monieo.INSTANCE.nodes.size() >= Monieo.MAX_CONNECTIONS;
		
	}
	
	public boolean connect(String inet) {
		
		/*for (Node n : Monieo.INSTANCE.nodes) {
			
			if (n.getAdress().equalsIgnoreCase(inet)) return;
			
		}*/
		
		try {
			Socket s = new Socket();
			s.connect(new InetSocketAddress(inet, Monieo.PORT), 2500);
			nodeDo(s, false);
		} catch (IOException e) {
			
			return false;
			
		}
		
		return true;
		
	}
	
	private void nodeDo(Socket s, boolean server) {
		
		System.out.println("Connected to: " + s.getInetAddress().getHostAddress());
		
		Node n = new Node(s, server);
		System.out.println("sending ver");
		n.queueNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_VER, String.valueOf(System.currentTimeMillis())));
		
		Monieo.INSTANCE.nodes.add(n);
		
		if (Monieo.INSTANCE.ui != null && Monieo.INSTANCE.ui.fullInit) Monieo.INSTANCE.ui.refresh(false, false);
		
		new Thread(n).start();
		
	}

}
