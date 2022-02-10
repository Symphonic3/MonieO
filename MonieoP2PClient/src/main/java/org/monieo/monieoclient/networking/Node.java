package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.networking.NetworkCommand.NetworkCommandType;

public class Node implements Runnable{
	
	private Socket socket;
	
	private boolean remoteAcknowledgedLocal = false;
	private boolean localAcknowledgedRemote = false;
	public static long MIN_RESPONSE_TIME = 15000;
	
	public volatile boolean busy = false;
	public Vector<PacketCommitment> packetCommitments = new Vector<PacketCommitment>();
	
	private boolean kill = false;
	
	public Vector<Consumer<Node>> queue = new Vector<Consumer<Node>>();
	
	private boolean server;
	
	public Node(Socket s, boolean server) {
		
		this.socket = s;
		this.server = server;
		
	}

	public boolean isServer() {
		
		return server;
		
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
	
	public static void propagateAll(NetworkCommand nc, PacketCommitment pc) {
		
		Monieo.INSTANCE.nodes.forEach(new Consumer<Node>() {

			@Override
			public void accept(Node t) {
				t.sendNetworkCommand(nc, pc);
				
			}
			
		});
		
	}
	
	public static Node randomNode() {
		
		return Monieo.INSTANCE.nodes.get(ThreadLocalRandom.current().nextInt(Monieo.INSTANCE.nodes.size()));
		
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
				
				if (s == null || s.equals("")) continue;
				
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
				
			} else return false;
			
		} else if (nc.cmd == NetworkCommandType.ACK_VER) {
			
			if (!remoteAcknowledgedLocal) {
				
				remoteAcknowledgedLocal = true;
				
			} else return false;
			
		} else if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
			
			if (nc.cmd == NetworkCommandType.REQUEST_BLOCKS_AFTER) {
				
				String wantedHash = nc.data;
				
				List<String> hashes = new ArrayList<String>();
				
				Block b = Monieo.INSTANCE.getHighestBlock();
				Block g = Monieo.genesis();
				
				while(true) {
					
					if (b.equals(g)) {
						
						//don't have block, sorry
						return true;
						
					}
					
					if (!b.hash().equals(wantedHash)) {
						
						hashes.add(b.hash());
						
					} else break;
					
				}
				
				for (String s : hashes) {
					
					sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_BLOCK, 
							Block.getByHash(wantedHash).serialize()), null);
					
				}
				
			} else if (nc.cmd == NetworkCommandType.REQUEST_SINGLE_BLOCK) {
				
				Block b = Block.getByHash(nc.data);
				
				if (b != null && b.validate()) {
					
					sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_BLOCK, b.serialize()), null);
					
				}
				
			} else if (nc.cmd == NetworkCommandType.REQUEST_NODES) {
				
				List<String> s = Monieo.INSTANCE.getValidNodesRightNow();
				
				String k = "";
				
				for (String a : s) {
					
					k = a + " ";
					
				}
				
				k = k.substring(0, k.length() - 1);
				
				sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_NODES, k), null);
				
			} else if (nc.cmd == NetworkCommandType.REQUEST_TIME) {
				
				sendNetworkCommand(new NetworkCommand(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkCommandType.SEND_TIME, String.valueOf(System.currentTimeMillis())), null);
				
			} else if (nc.cmd == NetworkCommandType.SEND_TRANSACTION) {
				
				Block hb = Monieo.INSTANCE.getHighestBlock();
				
				Transaction t = Transaction.deserialize(nc.data);

				if (t == null || !t.testValidityWithBlock(hb) || !t.testValidityWithTime(hb.header.timestamp)) return false;
				
				Monieo.INSTANCE.txp.add(t);
				
			} else if (nc.cmd == NetworkCommandType.SEND_BLOCK) {
				
				Block b = Block.deserialize(nc.data);
				
				if (b == null || !b.validate()) return false;
				
				Monieo.INSTANCE.handleBlock(b);
				
			} else if (nc.cmd == NetworkCommandType.SEND_NODES) {
				
				
				
			} else if (nc.cmd == NetworkCommandType.SEND_TIME) {
				
				
				
			}
			
			//TODO do this here this one
			
		}
		
		return true;
		
	}
	
}
