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
	public static long MIN_RESPONSE_TIME = 10000; //10 seconds
	
	public static String TERM = "EOM";
	
	private boolean kill = false;
	
	public Vector<Consumer<Node>> queue = new Vector<Consumer<Node>>();
	
	private boolean server;
	
	private final long timeConnected;
	private long timeRecieved = Long.MIN_VALUE;
	
	PrintWriter pw;
	BufferedReader br;
	
	public long lastValidPacketTime;
	
	public Node(Socket s, boolean server) {
		
		this.socket = s;
		this.server = server;
		
		this.timeConnected = System.currentTimeMillis();
		lastValidPacketTime = timeConnected;
		
	}
	
	public long getTimeOffset() {
		
		long of = timeRecieved-timeConnected;
		
		if (Math.abs(of) > 3600000) { //1 hour
			
			return 0;
			
		} else return of;
		
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
		if (Monieo.INSTANCE.ui != null) Monieo.INSTANCE.ui.refresh(false);
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
	
	public static void propagateAll(NetworkPacket nc) {
		
		Monieo.INSTANCE.nodes.forEach(new Consumer<Node>() {

			@Override
			public void accept(Node t) {
				t.queueNetworkPacket(nc);
				
			}
			
		});
		
	}
	
	public static Node randomNode() {
		
		return Monieo.INSTANCE.nodes.get(ThreadLocalRandom.current().nextInt(Monieo.INSTANCE.nodes.size()));
		
	}
	
	public void queueNetworkPacket(NetworkPacket nc) {
		
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
		
	}
	
	private void sendNetworkPacketNow(NetworkPacket nc) {
		
		//this is done because sendnetworkpacketnow is only used in resource-intensive applications, where we will be sending lots and therefore
		//do not want to time out the remote client
		lastValidPacketTime = System.currentTimeMillis(); 
		
		pw.print(nc.serialize());
		pw.println();
		pw.print(TERM);
		pw.println();
		pw.flush();
		
	}

	@Override
	public void run() {

		try {

			pw = new PrintWriter(socket.getOutputStream(), false, Charset.forName("UTF8"));
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF8")));
			
			while (true) {
				
				if (kill) return;
				
				if (!queue.isEmpty()) {
					
					for (Consumer<Node> a : queue) {
						
						a.accept(this);
						
					}
					
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
					
					lastValidPacketTime = System.currentTimeMillis();
					
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
		
		try {
			
			if (nc.cmd == NetworkPacketType.SEND_VER) {
				
				if (!localAcknowledgedRemote) {
					
					timeRecieved = Long.valueOf(nc.data);
					
					sendNetworkPacketNow(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.ACK_VER, null));
					
					localAcknowledgedRemote = true;
					
				} else return false;
				
			} else if (nc.cmd == NetworkPacketType.ACK_VER) {
				
				if (!remoteAcknowledgedLocal) {
					
					remoteAcknowledgedLocal = true;
					
				} else return false;
				
			} 
			
			if (nc.cmd == NetworkPacketType.ACK_VER || nc.cmd == NetworkPacketType.SEND_VER) {
				
				if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
					
					Monieo.INSTANCE.attemptRememberNode(getAdress());
					
					for (AbstractTransaction t : Monieo.INSTANCE.txp.get(-1, Monieo.INSTANCE.getHighestBlock())) {
						
						sendNetworkPacketNow(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_TRANSACTION, t.serialize()));
						
					}
					
					sendNetworkPacketNow(NetworkPacket.generateSyncPacket());
					
				}
				
			} else if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
				
				if (nc.cmd == NetworkPacketType.REQUEST_BLOCKS_AFTER) {
					
					String[] wantedHashP = nc.data.split(" ");
					
					int i = 0;
					
					List<String> hashes = new ArrayList<String>();
					
					Block b = Monieo.INSTANCE.getHighestBlock();
					Block g = Monieo.genesis();
					
					while(true) {
						
						if (!b.hash().equals(wantedHashP[i])) {
							
							hashes.add(b.hash());
							
						} else break;
						
						if (b.equals(g)) {
														
							if (wantedHashP.length != i) {
								
								hashes.clear();
								i++;
								b = Monieo.INSTANCE.getHighestBlock();
								continue;
								
							} else break;
							
						}
						
						b = b.getPrevious();
						
					}
					
					for (String s : hashes) {
						
						sendNetworkPacketNow(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, 
								Block.getByHash(s).serialize()));
						
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
								
								sendNetworkPacketNow(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, b.serialize()));
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
					
					sendNetworkPacketNow(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_ADDR, k));
					
				} else if (nc.cmd == NetworkPacketType.SEND_TRANSACTION) {

					Transaction t = Transaction.deserialize(nc.data);
					
					if (t == null || !t.validate()) return false;
					
					Monieo.INSTANCE.txp.add(t);
					
				} else if (nc.cmd == NetworkPacketType.SEND_BLOCK) {
					
					Block b = Block.deserialize(nc.data);
					
					if (b == null || !b.validate()) return false;
					
					Monieo.INSTANCE.handleBlock(b);
					
				} else if (nc.cmd == NetworkPacketType.SEND_ADDR) {
					
					//TODO send_addr I/O
					
				}
				
			}
			
		} catch (Exception e) {
			return false;
		}

		return true;
		
	}
	
}
