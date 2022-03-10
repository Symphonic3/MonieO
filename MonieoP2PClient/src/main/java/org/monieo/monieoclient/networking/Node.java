package org.monieo.monieoclient.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
	
	private volatile boolean kill = false;
	public volatile boolean doNotDisconnectPeer = false;
	
	public LinkedBlockingQueue<Consumer<Node>> queue = new LinkedBlockingQueue<Consumer<Node>>();
	
	private boolean server;
	
	public final long timeConnected;
	private volatile long timeRecieved = Long.MIN_VALUE;
	
	volatile boolean ibd = false;
	
	PrintWriter pw;
	BufferedReader br;
	
	NodeWatcher nw;
	
	public long lastValidPacketTime;
	
	public class NodeWatcher implements Runnable {
		
		Node n;
		
		public NodeWatcher(Node n) {
			
			this.n = n;
			
		}

		@Override
		public void run() {
			
			while (true) {
				
				if (n.kill) return;
				
				try {
					
					Consumer<Node> cn = queue.take();
					
					cn.accept(n);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		}

	}
	
	public Node(Socket s, boolean server) {
		
		this.socket = s;
		this.server = server;
		
		this.timeConnected = System.currentTimeMillis();
		lastValidPacketTime = timeConnected;
		
		try {
			
			this.pw = new PrintWriter(socket.getOutputStream(), false);
			this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
		} catch (IOException e) {

			System.out.println("D/C BECAUSE OF SOCKET RETRIEVE EXCEPTION!");
			
			//TODO remove this
			e.printStackTrace();
			disconnect(false);			

			return;
			
			
		}
		
		nw = new NodeWatcher(this);
		new Thread(nw).start();
		
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
	
	public void disconnect(boolean peacefully) {
		
		kill = true;
		if (!remoteAcknowledgedLocal || !localAcknowledgedRemote) {
			
			Monieo.INSTANCE.nam.couldNotConnectToNode(getAdress());
			
		} else if (peacefully) Monieo.INSTANCE.nam.successfullyConnectedOrDisconnected(getAdress());
		try {
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Monieo.INSTANCE.nodes.remove(this);
		if (Monieo.INSTANCE.ui != null && Monieo.INSTANCE.ui.fullInit) Monieo.INSTANCE.ui.refresh(false, false);
		
	}
	
	public void infraction() {
		
		System.out.println("D/C BECAUSE OF INFRACTION!");
		Monieo.INSTANCE.nam.ban(getAdress());
		disconnect(false);
		
	}
	
	public void queueAction(Consumer<Node> a) {
		
		queue.add(a);
		
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
				
				System.out.println("sending queued packet: " + nc.cmd);
				
				sendNetworkPacket(nc);
				
			}
			
		});
		
	}
	
	private void sendNetworkPacket(NetworkPacket nc) {

		pw.print(nc.serialize());
		pw.print("\n");
		pw.print(TERM);
		pw.print("\n");
		pw.flush();
		
	}

	@Override
	public void run() {

		try {
			
			while (true) {
				
				if (kill) return;
				
				NetworkPacket nc = null;
				
				if (!br.ready()) continue;
				
				String s = "";
				String t;
				
				inner: while ((t = br.readLine()) != null) {

					if (kill) return;
					
					if (t.equals(TERM)) break inner;
					s = s + t + "\n";
					
				}
				
				s = s.trim();

				if (s == null || s.equals("")) continue;
				
				//System.out.println("recieved data!");
				
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
			System.out.println("D/C BECAUSE OF EXCEPTION!");
			
			//TODO remove this
			e.printStackTrace();
			disconnect(false);

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
					
					queueNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.ACK_VER, null));
					
					localAcknowledgedRemote = true;
					
				} else return false;
				
			} else if (nc.cmd == NetworkPacketType.ACK_VER) {
				
				if (!remoteAcknowledgedLocal) {
					
					remoteAcknowledgedLocal = true;
					
				} else return false;
				
			} 
			
			if (nc.cmd == NetworkPacketType.ACK_VER || nc.cmd == NetworkPacketType.SEND_VER) {
				
				if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
					
					Monieo.INSTANCE.nam.successfullyConnectedOrDisconnected(getAdress());
					
					String addr = String.join("\n", Monieo.INSTANCE.nam.get1000Addresses());
					
					System.out.println("qaddr");
					queueNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_ADDR, addr));
					
					List<AbstractTransaction> g = Monieo.INSTANCE.txp.get(-1, Monieo.INSTANCE.getHighestBlock());

					for (AbstractTransaction t : g) {
						
						if (kill) return true;
						
						queueNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_TRANSACTION, t.serialize()));
						
					}

					System.out.println("qsync");
					queueNetworkPacket(NetworkPacket.generateSyncPacket());
					
				}
				
			} else if (remoteAcknowledgedLocal && localAcknowledgedRemote) {
				
				if (nc.cmd == NetworkPacketType.REQUEST_BLOCKS_AFTER || nc.cmd == NetworkPacketType.KEEPALIVE) {
					
					if (doNotDisconnectPeer && nc.cmd == NetworkPacketType.KEEPALIVE) return true;
					
					queueAction(new Consumer<Node>() {

						@Override
						public void accept(Node t) {
							
							String[] wantedHashP = nc.data.split(" ");
							
							if (wantedHashP.length > 1 && !ibd) {
																
								//IBD
								
								System.out.println("IBD");
								
								doNotDisconnectPeer = true;
								ibd = true;
								
							}
							
							int i = 0;
							
							List<String> hashes = new ArrayList<String>();
							
							Block b = Monieo.INSTANCE.getHighestBlock();
							hashes.add(Monieo.INSTANCE.getHighestBlockHash());
							Block g = Monieo.genesis();
							
							while(true) {
								
								if (kill) return;
								
								if (b.equals(g)) {
																
									if (wantedHashP.length > i+1) {
										
										hashes.clear();
										i++;
										b = Monieo.INSTANCE.getHighestBlock();
										hashes.add(Monieo.INSTANCE.getHighestBlockHash());
										continue;
										
									} else break;
									
								}
								
								if (!b.header.preHash.equals(wantedHashP[i])) {
									hashes.add(b.header.preHash);
									
								} else break;
								
								b = b.getPrevious();
								
							}
							
							hashes.add(b.hash());
							
							Collections.reverse(hashes);
							
							for (String s : hashes) {
								
								if (kill) return;
								
								t.sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, 
										Block.getByHash(s).serialize()));
								
							}
							
							if (ibd) doNotDisconnectPeer = false;
							
						}
						
					});
					
				} else if (nc.cmd == NetworkPacketType.REQUEST_SINGLE_BLOCK) {
					
					//the previous method of doing this was unsafe.
					//to avoid issues, we are using the algorithm for next blocks to ensure
					//file path injection is impossible
					//obviously there are better ways to do this, but this netcommand
					//might be removed anyways.
					
					/*queueAction(new Consumer<Node>() {

						@Override
						public void accept(Node t) {
							
							String wantedHash = nc.data;
							
							Block b = Monieo.INSTANCE.getHighestBlock();
							Block g = Monieo.genesis();
							
							while(true) {

								if (kill) return;
								
								if (b.equals(g)) {
									
									//don't have block, sorry
									return;
									
								}
								
								if (b.header.preHash.equals(wantedHash)) {
									
									t.sendNetworkPacket(new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.SEND_BLOCK, b.getPrevious().serialize()));
									break;
									
								}
								
								b = b.getPrevious();
								
							}

							
						}
						
					});*/
					
				} else if (nc.cmd == NetworkPacketType.SEND_TRANSACTION) {

					Transaction t = Transaction.deserialize(nc.data);
					
					if (t == null || !t.validate()) return false;
					
					Monieo.INSTANCE.txp.add(t);
					
				} else if (nc.cmd == NetworkPacketType.SEND_BLOCK) {
					
					Block b = Block.deserialize(nc.data);
					
					if (b == null || !b.validate()) return false;
					
					Monieo.INSTANCE.handleBlock(b);
					
				} else if (nc.cmd == NetworkPacketType.SEND_ADDR) {
					
					String[] addr = nc.data.split("\n");
					
					if (addr.length > 25000) return false; //TODO this
					
					for (String s : addr) {
						
						if (s == null || s.equals("")) continue;
						
						String[] bs = s.split("\\.");
						
						if (bs.length != 4) return false;
						
						byte[] b = new byte[4];
						
						for (int i = 0; i < bs.length; i++) {
							
							b[i] = Integer.valueOf(bs[i]).byteValue();
					
						}
						
						try {
							
							InetAddress.getByAddress(b);
							
						} catch (Exception e) {
							return false;
						}
						
						Monieo.INSTANCE.nam.gotNew(s);
						
					}
					
				}
				
			}
			
		} catch (Exception e) {
			return false;
		}

		return true;
		
	}
	
}
