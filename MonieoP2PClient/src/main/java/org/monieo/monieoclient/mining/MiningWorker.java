package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.monieo.monieoclient.blockchain.Block;

public class MiningWorker {

	public volatile boolean cont = false;
	public Thread thread;
	
	public volatile int hashrate;

	Block b;
	
	public MiningWorker(DefaultMinerImpl m) {
		
		Runnable r = new Runnable() {
			
			public void run() {
				
				long start = System.currentTimeMillis();
				
				while (true) {
					
					if (m.stop) return;
					
					while (b == null) { if (m.stop) return; };
					
					start = System.currentTimeMillis();
					
					for (int i = 0; i < DefaultMinerImpl.HASHES_BEFORE_RECHECK; i++) {
						
						if (new BigInteger(1, b.rawHash()).compareTo(b.header.diff) == -1) {
							
							m.acceptWork(b);
							cont = false;
							while (!cont) {};
							
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
		cont = true;
		
	}

}
