package org.monieo.monieoclient.networking;

import java.util.ArrayList;
import java.util.List;

import org.monieo.monieoclient.Monieo;

public class NodeManager implements Runnable{

	public List<AbstractNode> connectedNodes = new ArrayList<AbstractNode>();
	
	@Override
	public void run() {
		
		Monieo instance = Monieo.INSTANCE;
		
		while(true) {
			
			//handle incoming and outgoing packets
			
		}
		
	}

}
