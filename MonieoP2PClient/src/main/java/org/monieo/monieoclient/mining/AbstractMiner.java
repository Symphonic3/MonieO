package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

import org.monieo.monieoclient.Monieo;

public interface AbstractMiner extends Runnable {
	
	public void begin(Monieo m, Consumer<MiningStatistics> supervisor);
	public void stop();
	public Monieo getMonieoInstance();
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
