package org.monieo.monieoclient.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.Transaction;
import org.monieo.monieoclient.networking.NetworkPacket.NetworkPacketType;

public class Node implements Runnable{
	
	private Socket socket;
	
	public boolean remoteAcknowledgedLocal = false;
	public boolean localAcknowledgedRemote = false;
	public static long MIN_RESPONSE_TIME = 15000;
	
	public static String TERM = "EOM";
	
	public volatile boolean busy = false;
	public Vector<PacketCommitment> packetCommitments = new Vector<PacketCommitment>();
	
	private boolean kill = false;
	
	public Vector<Consumer<Node>> queue = new Vector<Consumer<Node>>();
	
	private boolean server;
	
	PrintWriter pw;
	BufferedReader br;
	
	public Node(Socket s, boolean server) {
		
		this.socket = s;
		this.server = server;
		
		Monieo.INSTANCE.attemptRememberNode(getAdress());
		
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
		
		System.out.println("DC BECAUSE OF INFRACTION!");
		Monieo.INSTANCE.fetchByAdress(getAdress()).ban();
		disconnect();
		
	}
	
	public void queueAction(Consumer<Node> a) {
		
		if (queue.size() > 0) queue.insertElementAt(a, 0); else queue.add(a);
		
	}
	
	public static void propagateAll(NetworkPacket nc, PacketCommitment pc) {
		
		Monieo.INSTANCE.nodes.forEach(new Consumer<Node>() {

			@Override
			public void accept(Node t) {
				t.sendNetworkPacket(nc, pc);
				
			}
			
		});
		
	}
	
	public static Node randomNode() {
		
		return Monieo.INSTANCE.nodes.get(ThreadLocalRandom.current().nextInt(Monieo.INSTANCE.nodes.size()));
		
	}
	
	public void sendNetworkPacket(NetworkPacket nc, PacketCommitment pc) {
		
		queueAction(new Consumer<Node>() {

			@Override
			public void accept(Node t) {
				
				pw.print(nc.serialize());
				pw.println();
				pw.print(TERM);
				pw.println();
				pw.flush();
				
			}
			
		});
		
		if (pc == null) return;
		
		if (packetCommitments.size() > 0) packetCommitments.insertElementAt(pc, 0); else packetCommitments.add(pc);
		
	}

	@Override
	public void run() {

		try {

			pw = new PrintWriter(socket.getOutputStream(), false, Charset.forName("UTF8"));
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF8")));
			
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
				
				NetworkPacket nc = null;
				
				if (!br.ready()) continue;
				
				String s = "";
				String t;
				
				inner: while ((t = br.readLine()) != null) {
					
					if (t.equals(TERM)) break inner;
					s = s + t + "\n";
					
				}
				
				s = s.trim();

				if (s == null || s.equals("")) continue;
				
				System.out.println("recieved data!");
				
				nc = NetworkPacket.deserialize(s);

				if (nc == null || !Monieo.assertSupportedProtocol(new String[] {nc.magicn, nc.ver}) || !handle(nc)) {
					
					infraction();
					continue;
					
				} else {
					
					//packet success!
					//TODO implement timer keepalive thing
					
				}
				
			}
			
		} catch (Exception e) {
			
			//possibly infraction here?
			System.out.println("DC BECAUSE OF EXCEPTION!");
			e.printStackTrace();
			disconnect();			

			return;
			
		}
		
	}
	
	public boolean handle(NetworkPacket nc) {
		
		System.out.println("rec'd network command!" + nc.cmd);
		System.out.println("==BEGIN NETWORK COMMAND==");
		System.out.println(nc.data);
		System.out.println("==END NETWORK COMMAND==");
		
		for (int i = 0; i < packetCommitments.size(); i++) {
			
			if (packetCommitments.get(i).attemptFillShouldBanNode(nc)) { //disconnects node instead of banning, as this is only indicitive that the node is disconnected, not nessecarily that it is uncooperative

				System.out.println("DC BECAUSE OF COMMITMENT!");
				disconnect();
				return true;
				
			} else {
				
				packetCommitments.remove(i);
				i--;
				
			}
			
		}
		
		if (nc.cmd == NetworkPacketType.SEND_VER) {
			
			if (!localAcknowledgedRemote) {
				
				sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.ACK_VER, null), null);
				
				localAcknowledgedRemote = true;
				
			} else return false;
			
		} else if (nc.cmd == NetworkPacketType.ACK_VER) {
			
			if (!remoteAcknowledgedLocal) {
				
				remoteAcknowledgedLocal = true;
				
			} else return false;
			
		} 
		
		if (nc.cmd == NetworkPacketType.ACK_VER || nc.cmd == NetworkPacketType.SEND_VER) {
			
			if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
				
				for (AbstractTransaction t : Monieo.INSTANCE.txp.get(-1, Monieo.INSTANCE.getHighestBlock())) {
					
					sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_TRANSACTION, t.serialize()), null);
					
				}
				
				Block b = Monieo.INSTANCE.getHighestBlock();
				Block g = Monieo.genesis();
				
				for (int i = 0; i < Monieo.CONFIRMATIONS_BLOCK_SENSITIVE*2; i++) {
					
					if (b.equals(g)) {
						
						break;
						
					}
					
					b = b.getPrevious();
					
				}
				
				sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.REQUEST_BLOCKS_AFTER, b.hash()), null);
				
			}
			
		} else if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
			
			if (nc.cmd == NetworkPacketType.REQUEST_BLOCKS_AFTER) {
				
				String wantedHash = nc.data;
				
				List<String> hashes = new ArrayList<String>();
				
				Block b = Monieo.INSTANCE.getHighestBlock();
				Block g = Monieo.genesis();
				
				while(true) {
					
					if (!b.hash().equals(wantedHash)) {
						
						hashes.add(b.hash());
						
					} else break;
					
					if (b.equals(g)) {
						
						//don't have block, sorry
						return true;
						
					}
					
					b = b.getPrevious();
					
				}
				
				for (String s : hashes) {
					
					sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, 
							Block.getByHash(s).serialize()), null);
					
				}
				
			} else if (nc.cmd == NetworkPacketType.REQUEST_SINGLE_BLOCK) {
				
				//the previous method of doing this was unsafe.
				//to avoid issues, we are using the algorithm for next blocks to ensure
				//file path injection is impossible
				//obviously there are better ways to do this, but this netcommand
				//might be removed anyways.
				
				String wantedHash = nc.data;
				
				Block b = Monieo.INSTANCE.getHighestBlock();
				Block g = Monieo.genesis();
				
				while(true) {
					
					if (b.hash().equals(wantedHash)) {
						
						if (b != null && b.validate()) {
							
							sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, b.serialize()), null);
							break;
							
						}
						
					}
					
					if (b.equals(g)) {
						
						//don't have block, sorry
						return true;
						
					}
					
					b = b.getPrevious();
					
				}

			} else if (nc.cmd == NetworkPacketType.REQUEST_NODES) {
				
				List<String> s = Monieo.INSTANCE.getValidNodesRightNow();
				
				String k = "";
				
				for (String a : s) {
					
					k = a + " ";
					
				}
				
				k = k.substring(0, k.length() - 1);
				
				sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_NODES, k), null);
				
			} else if (nc.cmd == NetworkPacketType.REQUEST_TIME) {
				
				sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_TIME, String.valueOf(System.currentTimeMillis())), null);
				
			} else if (nc.cmd == NetworkPacketType.SEND_TRANSACTION) {

				Transaction t = Transaction.deserialize(nc.data);
				
				if (t == null || !t.validate()) return false;
				
				Monieo.INSTANCE.txp.add(t);
				
			} else if (nc.cmd == NetworkPacketType.SEND_BLOCK) {
				
				Block b = Block.deserialize(nc.data);
				
				if (b == null || !b.validate()) return false;
				
				Monieo.INSTANCE.handleBlock(b);
				
			} else if (nc.cmd == NetworkPacketType.SEND_NODES) {
				
				
				
			} else if (nc.cmd == NetworkPacketType.SEND_TIME) {
				
				
				
			}
			
			//TODO do this here this one
			
		}
		
		return true;
		
	}
	
}
