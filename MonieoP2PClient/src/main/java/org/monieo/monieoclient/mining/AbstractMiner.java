package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.monieo.monieoclient.Monieo;

public interface AbstractMiner {
	
	public void begin(Monieo m);
	public void stop();
	public Monieo getMonieoInstance();
	public MiningStatistics retrieveMiningStatistics();
	public String getMiningName();
	
	public class MiningStatistics {

		public final long hashes;
		public final BigInteger blockTarget;
		public final int prob;
		public final int blocks;
		public final BigDecimal total;
		
		public MiningStatistics(long h, BigInteger blockTarget, int prob, int blocks, BigDecimal total) {
			
			this.hashes = h;
			this.blockTarget = blockTarget;
			this.prob = prob;
			this.blocks = blocks;
			this.total = total;
			
		}
		
	}

}
