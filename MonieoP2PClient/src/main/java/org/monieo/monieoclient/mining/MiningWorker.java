package org.monieo.monieoclient.mining;

import java.math.BigInteger;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.randomx.RandomXInstance;
import org.monieo.monieoclient.randomx.RandomXManager;

public class MiningWorker {

	public Thread thread;
	
	public volatile int hashrate;

	Block b;
	final String id;
	
	public MiningWorker(DefaultMinerImpl m, String id) {
			
		this.id = id;
		
		System.out.println(id);
		
		Runnable r = new Runnable() {
			
			public void run() {
				
				long start = System.currentTimeMillis();
				
				RandomXInstance rx = RandomXManager.getManager().getRandomX();
				
				long nonce = 0;
				
				wh: while (true) {
					
					if (m.stop) return;
					
					while (b == null) { if (m.stop) return; };
					
					start = System.currentTimeMillis();
					
					for (int i = 0; i < DefaultMinerImpl.HASHES_BEFORE_RECHECK; i++) {
						
						if (new BigInteger(1, rx.hash(b.header.serialize())).compareTo(b.header.diff) == -1) {
							
							System.out.println(b.hash());
							m.acceptWork(b);
							b = null;
							nonce = 0;
							continue wh;
							
						}
						
						b.header.nonce = new BigInteger(++nonce + id);
						
					}
					
					hashrate = (int) ( ( ((double)DefaultMinerImpl.HASHES_BEFORE_RECHECK) / ((double)(System.currentTimeMillis()-start)) ) * 1000);
					
				}
				
			}
			
		};
		
		thread = new Thread(r);
		thread.start();
		
	}
	
	public void setBlock(Block x) {
		
		b = x;
		
	}

}
