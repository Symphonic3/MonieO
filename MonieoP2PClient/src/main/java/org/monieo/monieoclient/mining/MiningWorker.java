package org.monieo.monieoclient.mining;

import java.math.BigInteger;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.randomx.RandomXInstance;
import org.monieo.monieoclient.randomx.RandomXManager;

public class MiningWorker {

	public Thread thread;
	
	public volatile int hashrate;

	Block b;
	
	public MiningWorker(DefaultMinerImpl m) {
			
		Runnable r = new Runnable() {
			
			public void run() {
				
				long start = System.currentTimeMillis();
				
				RandomXInstance rx = RandomXManager.getManager().getRandomX();
				
				wh: while (true) {
					
					if (m.stop) return;
					
					while (b == null) { if (m.stop) return; };
					
					start = System.currentTimeMillis();
					
					for (int i = 0; i < DefaultMinerImpl.HASHES_BEFORE_RECHECK; i++) {
						
						if (new BigInteger(1, rx.hash(b.header.serialize())).compareTo(b.header.diff) == -1) {
							
							System.out.println(b.hash());
							m.acceptWork(b);
							b = null;
							continue wh;
							
						}
						
						b.header.nonce = b.header.nonce.add(BigInteger.ONE);
						
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
