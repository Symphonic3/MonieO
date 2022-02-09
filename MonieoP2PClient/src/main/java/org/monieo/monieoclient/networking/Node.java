package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.networking.NetworkCommand.NetworkCommandType;

public class Node implements Runnable{
	
	public class PacketCommitment {
		
		public boolean isFilled = false;
		public long timeMax;
		
		Predicate<NetworkCommand> test;
		
		public PacketCommitment(Predicate<NetworkCommand> testResponse) {
			
			timeMax = System.currentTimeMillis() + MIN_RESPONSE_TIME;
			this.test = testResponse;
			
		}
		
		public boolean attemptFillShouldBanNode(NetworkCommand nc) {
			
			if (test.test(nc)) {
				
				isFilled = true;
				return false;
				
			} else {
				
				if (System.currentTimeMillis() > timeMax) {
					
					return true;
					
				}
				
				return false;
				
			}
			
		}
		
	}
	
	private Socket socket;
	
	private boolean remoteAcknowledgedLocal = false;
	private boolean localAcknowledgedRemote = false;
	public static long MIN_RESPONSE_TIME = 15000;
	
	public volatile boolean busy = false;
	public Vector<PacketCommitment> packetCommitments = new Vector<PacketCommitment>();
	
	private boolean kill = false;
	
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
	
	public void disconnect() {
		
		try {
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Monieo.INSTANCE.nodes.remove(this);
		kill = true;
		
	}
	
	public void infraction() {
		
		Monieo.INSTANCE.fetchByAdress(getAdress()).ban();
		disconnect();
		
	}
	
	public void queueAction(Consumer<Node> a) {
		
		if (queue.size() > 0) queue.insertElementAt(a, 0); else queue.add(a);
		
	}
	
	public void sendNetworkCommand(NetworkCommand nc, PacketCommitment pc) {
		
		queueAction(new Consumer<Node>() {

			@Override
			public void accept(Node t) {
				
				try {
					t.getSocket().getOutputStream().write(nc.serialize().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
		});
		
		if (pc == null) return;
		
		if (packetCommitments.size() > 0) packetCommitments.insertElementAt(pc, 0); else packetCommitments.add(pc);
		
	}

	@Override
	public void run() {

		try {

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			
			while (true) {
				
				if (kill) return;
				
				if (!queue.isEmpty()) {
					
					busy = true;
					
					for (Consumer<Node> a : queue) {
						
						a.accept(this);
						
					}
					
					busy = false;
					
				}
				
				queue.clear();
				
				NetworkCommand nc = null;
				
				String s = new String(in.readAllBytes(), "UTF8");
				
				nc = NetworkCommand.deserialize(s);

				if (nc == null || !Monieo.assertSupportedProtocol(new String[] {nc.magicn, nc.ver}) || !handle(nc)) {
					
					infraction();
					continue;
					
				}
				
			}
			
		} catch (Exception e) {
			
			//possibly infraction here?
			disconnect();
			
			e.printStackTrace();

			return;
			
		}
		
	}
	
	public boolean handle(NetworkCommand nc) {
		
		for (PacketCommitment pc : packetCommitments) {
			
			if (pc.attemptFillShouldBanNode(nc)) { //disconnects node instead of banning, as this is only indicitive that the node is disconnected, not nessecarily that it is uncooperative

				disconnect();
				return true;
				
			}
			
		}
		
		if (nc.cmd == NetworkCommandType.SEND_VER) {
			
			if (!localAcknowledgedRemote) {
				
				sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.ACK_VER, null), null);
				
				localAcknowledgedRemote = true;
				
				if (!remoteAcknowledgedLocal) {
					
					sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_VER, null),
							new PacketCommitment(new Predicate<NetworkCommand>() {

								@Override
								public boolean test(NetworkCommand t) {
									return (t.cmd == NetworkCommandType.ACK_VER);
								}
								
							}));
					
				}
				
			} else return false;
			
		} else if (nc.cmd == NetworkCommandType.ACK_VER) {
			
			if (!remoteAcknowledgedLocal) {
				
				remoteAcknowledgedLocal = true;
				
			} else return false;
			
		} else if (nc.cmd == NetworkCommandType.REQUEST_BLOCKS_AFTER) {
			
			//TODO do this here this one
			
		}
		
		return true;
		
	}
	
}
