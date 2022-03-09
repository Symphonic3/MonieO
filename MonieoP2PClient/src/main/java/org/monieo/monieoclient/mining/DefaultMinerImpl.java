package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.BlockHeader;
import org.monieo.monieoclient.blockchain.CoinbaseTransaction;
import org.monieo.monieoclient.randomx.RandomXManager;

public class DefaultMinerImpl implements AbstractMiner{

	public static final int HASHES_BEFORE_RECHECK = 500;
	
	Consumer<MiningStatistics> supervisor = null;
	MiningStatistics curr = null;	
	
	Timer t = null;
	public volatile boolean stop = false;
	
	ArrayList<MiningWorker> workers = new ArrayList<MiningWorker>();
	
	public DefaultMinerImpl() {};
	
	Block reference = null;
	
	@Override
	public void begin(Consumer<MiningStatistics> sup, MiningSettings set) {
		
		while (t != null) {};
		
		stop = false;
		
		RandomXManager.msettings = set;
		RandomXManager.applySettings();
		
		this.supervisor = sup;
		
		curr = new MiningStatistics(new ArrayList<Integer>(), BigInteger.valueOf(0), 0, BigDecimal.valueOf(0), System.currentTimeMillis());
		
		for (int i = 0; i < set.threads; i++) {
			
			workers.add(new MiningWorker(this));
			
		}
		
		t = new Timer();
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				if (!Monieo.INSTANCE.getHighestBlock().equals(reference)) {

					resetBlock();
					
				}
				
				ArrayList<Integer> hr = new ArrayList<Integer>();
				
				for (MiningWorker mw : workers) {

					hr.add(mw.hashrate);
					
				}
				
				curr.hashrates = hr;
				supervisor.accept(curr);
				
			}
			
		}, 0, 2500);
		
	}

	@Override
	public MiningStatistics getMiningStatistics() {
		return curr;
	}
	
	@Override
	public void stop() {
		t.cancel();
		t = null;
		stop = true;
		
		for (MiningWorker w : workers) {
			
			try {
				w.thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public String getMiningName() {
		return "DefaultCPUMiner";
	}
	
	Block genBlock() {
		
		Block h = null;
		
		while (h == null) {
			
			h = Monieo.INSTANCE.getHighestBlock();
			
		};

		long hei = h.header.height + 1;
		
		BigInteger diff = h.calculateNextDifficulty();
		
		curr.blockTarget = diff;
		
		long nettime = Monieo.INSTANCE.getNetAdjustedTime();
		
		long btoavtime = 0;
		int divisor = 0;
		Block d = h;
		
		while (d != null) {
			
			btoavtime += d.header.timestamp;
			divisor++;
			
			if (divisor == 6) break;
			
			d = d.getPrevious();
			
		}
		
		if ((btoavtime/divisor) >= nettime) nettime = (btoavtime/divisor)+1;

		List<AbstractTransaction> tx = Monieo.INSTANCE.txp.get(1024*128, h); //128 is completely arbitrary. This should be optimized later.
		
		CoinbaseTransaction ct = new CoinbaseTransaction(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, Monieo.INSTANCE.getWalletByNick("MININGWALLET").getAsString(), Block.getMaxCoinbase(hei));
		tx.add(ct);
		
		AbstractTransaction[] txr = tx.toArray(new AbstractTransaction[tx.size()]);
		
		Block b = new Block(new BlockHeader(Monieo.MAGIC_NUMBERS,
				Monieo.PROTOCOL_VERSION,
				Monieo.INSTANCE.getHighestBlockHash(),
				Block.merkle(txr),
				nettime,
				BigInteger.ZERO,
				txr.length,
				hei,
				diff), txr
		);
		
		return b;
		
	}
	
	public void resetBlock() {
		
		for (MiningWorker mw : workers) {
			
			mw.b = genBlock();
			mw.cont = true;
			
		}
		
	}

	@Override
	public void acceptWork(Block b) {

		if (b.validate()) {
			
			if (b.isReady()) {
				
				System.out.println("Found valid block!");
				
				Monieo.INSTANCE.handleBlock(b);

				curr.blocks++;
				curr.total = curr.total.add(Block.getMaxCoinbase(b.header.height));
				
				resetBlock();
				
				return;
				
			} else {
				
				throw new IllegalStateException("Found block but it was not ready!");
				
			}
			
		}
		
		throw new IllegalStateException("Found block but block could not be validated!");
		
	}

}
