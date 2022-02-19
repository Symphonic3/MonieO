package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

import org.monieo.monieoclient.Monieo;

public interface AbstractMiner extends Runnable {
	
	public void begin(Consumer<MiningStatistics> supervisor);
	public void stop();
	public String getMiningName();
	public MiningStatistics getMiningStatistics();
	
	public class MiningStatistics {

		public BigInteger hashes;
		public BigInteger blockTarget;
		public int blocks;
		public BigDecimal total;
		public final long beginTime;
		
		public MiningStatistics(BigInteger h, BigInteger blockTarget, int blocks, BigDecimal total, long beginTime) {
			
			this.hashes = h;
			this.blockTarget = blockTarget;
			this.blocks = blocks;
			this.total = total;
			this.beginTime = beginTime;
			
		}
		
	}

}
