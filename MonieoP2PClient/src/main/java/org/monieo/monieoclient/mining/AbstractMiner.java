package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.randomx.RandomXManager;
import org.monieo.monieoclient.randomx.RandomXManager.RandomXFlags;

public interface AbstractMiner {
	
	public void begin(Consumer<MiningStatistics> supervisor, MiningSettings set);
	public void stop();
	public String getMiningName();
	public MiningStatistics getMiningStatistics();
	public void acceptWork(Block b);
	
	public class MiningStatistics {

		public List<Integer> hashrates;
		public BigInteger blockTarget;
		public int blocks;
		public BigDecimal total;
		public final long beginTime;
		
		public MiningStatistics(List<Integer> hashrates, BigInteger blockTarget, int blocks, BigDecimal total, long beginTime) {
			
			this.hashrates = hashrates;
			this.blockTarget = blockTarget;
			this.blocks = blocks;
			this.total = total;
			this.beginTime = beginTime;
			
		}
		
	}
	
	public class MiningSettings {
		
		public boolean mLargePages;
		public boolean oArg2;
		public boolean fullRam;
		public boolean hAES;
		public int threads;
		
		public MiningSettings(boolean mLargePages, boolean oArg2, boolean fullRam, boolean hAES, int threads) {
			
			this.mLargePages = mLargePages;
			this.oArg2 = oArg2;
			this.fullRam = fullRam;
			this.hAES = hAES;
			this.threads = threads;
			
		}
		
		public static MiningSettings of(int threads, RandomXFlags... flags) {
			
			int a = RandomXManager.flagsToInt(flags);
			
			boolean mLargePages = (a & RandomXFlags.RANDOMX_FLAG_LARGE_PAGES.v()) == RandomXFlags.RANDOMX_FLAG_LARGE_PAGES.v();
			boolean oArg2 = (a & RandomXFlags.RANDOMX_FLAG_ARGON2_AVX2.v()) == RandomXFlags.RANDOMX_FLAG_ARGON2_AVX2.v() || 
					(a & RandomXFlags.RANDOMX_FLAG_ARGON2_SSSE3.v()) == RandomXFlags.RANDOMX_FLAG_ARGON2_SSSE3.v();
			boolean fullRam = (a & RandomXFlags.RANDOMX_FLAG_FULL_MEM.v()) == RandomXFlags.RANDOMX_FLAG_FULL_MEM.v();
			boolean hAES = (a & RandomXFlags.RANDOMX_FLAG_HARD_AES.v()) == RandomXFlags.RANDOMX_FLAG_HARD_AES.v();
			
			return new MiningSettings(mLargePages, oArg2, fullRam, hAES, threads);

		}
		
		public int getThreads() {
			
			return threads;
			
		}
		
		public int toFlags() {
			
			int i = 0;
			
			if (mLargePages) i += RandomXFlags.RANDOMX_FLAG_LARGE_PAGES.v();
			if (oArg2) {
				
				int fla = RandomXManager.getFlags();
				
				if ((fla & RandomXFlags.RANDOMX_FLAG_ARGON2_AVX2.v()) == RandomXFlags.RANDOMX_FLAG_ARGON2_AVX2.v()) {
					
					i += RandomXFlags.RANDOMX_FLAG_ARGON2_AVX2.v();
					
				}
				
				if ((fla & RandomXFlags.RANDOMX_FLAG_ARGON2_SSSE3.v()) == RandomXFlags.RANDOMX_FLAG_ARGON2_SSSE3.v()) {
					
					i += RandomXFlags.RANDOMX_FLAG_ARGON2_SSSE3.v();
					
				}
				
			}
			if (fullRam) i += RandomXFlags.RANDOMX_FLAG_FULL_MEM.v();
			if (hAES) i += RandomXFlags.RANDOMX_FLAG_HARD_AES.v();
			
			return i;
			
		}

		public boolean sameSettings(MiningSettings o) {
			
			if (o == null) return false;
			
			return (o.fullRam == fullRam && o.hAES == hAES && o.mLargePages == mLargePages && o.oArg2 == oArg2);
			
		}
		
	}

}
