package org.monieo.monieoclient.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.monieo.monieoclient.Monieo;

public class NodeManager implements Runnable{
	
	@Override
	public void run() {
		
		Monieo instance = Monieo.INSTANCE;
		
		while(true) {
			
			for (Node n : instance.nodes) {
				
				if (!n.queue.isEmpty()) {
					
					for (Consumer<Node> a : n.queue) {
						
						a.accept(n);
						
					}
					
				}
				
				n.queue.clear();
				
			}
			//handle incoming and outgoing packets
			
		}
		
	}

}
