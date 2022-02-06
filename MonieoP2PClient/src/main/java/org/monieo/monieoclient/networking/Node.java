package org.monieo.monieoclient.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.function.Consumer;

public class Node implements Runnable{
	
	private Socket socket;
	
	private boolean remoteAcknowledgedLocal = false;
	private boolean localAcknowledgedRemote = false;
	public long minResponseTime = Long.MAX_VALUE;
	
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
	
	public void queueAction(Consumer<Node> a) {
		
		queue.add(a);
		
	}
	
	public void sendNetworkCommand(NetworkCommand nc) {
		
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
		
	}

	@Override
	public void run() {
		
		InputStream in;
		OutputStream out;
		
		try {

			in = socket.getInputStream();
			out = socket.getOutputStream();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
			
		}
		
		while (true) {
			
			if (!queue.isEmpty()) {
				
				for (Consumer<Node> a : queue) {
					
					a.accept(this);
					
				}
				
			}
			
			queue.clear();
			
			NetworkCommand nc = null;
			
			try {
				
				if (in.available() != 0) {
					
					String s = new String(in.readAllBytes(), "UTF8");
					
					nc = NetworkCommand.deserialize(s);
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (nc == null) continue;

			//TODO handle io
			
		}
		
	}
	
}
