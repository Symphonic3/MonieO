package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.AbstractTransaction;
import org.monieo.monieoclient.blockchain.Block;
import org.monieo.monieoclient.blockchain.BlockHeader;
import org.monieo.monieoclient.blockchain.CoinbaseTransaction;

public class DefaultMinerImpl implements AbstractMiner{

	Thread t = null;
	
	private boolean stop = false;
	
	Consumer<MiningStatistics> supervisor = null;
	
	MiningStatistics curr = null;
	
	public static final int HASHES_BEFORE_RECHECK = 1000;
	
	public DefaultMinerImpl() {};
	
	@Override
	public void begin(Consumer<MiningStatistics> sup) {
		this.supervisor = sup;
		
		curr = new MiningStatistics(BigInteger.valueOf(0), BigInteger.valueOf(0), 0, BigDecimal.valueOf(0), System.currentTimeMillis());
		
		t = new Thread(this);
		t.start();
		
	}

	@Override
	public void stop() {
		stop = true;
		supervisor.accept(new MiningStatistics(BigInteger.valueOf(0), BigInteger.valueOf(0), 0, BigDecimal.valueOf(0), 0));
		
	}

	@Override
	public String getMiningName() {
		return "DefaultCPUMiner";
	}

	@Override
	public void run() {
		
		BigInteger nonce = BigInteger.ZERO; //this should be reset in a more reliable way when a new block is started
		
		while (true) {
						if (stop) return;
			
			Block h = Monieo.INSTANCE.getHighestBlock();
			long hei = h.header.height + 1;
			
			BigInteger diff = h.calculateNextDifficulty();
			
			curr.blockTarget = diff;

			List<AbstractTransaction> tx = Monieo.INSTANCE.txp.get(1024*750, h); //750 is completely arbitrary. This should be optimized later.
			CoinbaseTransaction ct = new CoinbaseTransaction(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, Monieo.INSTANCE.getWalletByNick("MININGWALLET").getAsWalletAdress(), Block.getMaxCoinbase(hei));
			tx.add(ct);
			
			long nettime = Monieo.INSTANCE.getNetAdjustedTime();
			
			Block b = new Block(new BlockHeader(Monieo.MAGIC_NUMBERS,
					Monieo.PROTOCOL_VERSION,
					Monieo.INSTANCE.getHighestBlock().hash(),
					Block.merkle(tx.toArray(new AbstractTransaction[tx.size()])),
					nettime,
					BigInteger.ZERO,
					tx.size(),
					hei,
					diff)
			);
			
			in: for (int i = 0; i < HASHES_BEFORE_RECHECK; i++) {
				
				if (new BigInteger(b.hash().getBytes()).compareTo(b.header.diff) == -1) {
					
					System.out.println("Found valid block!");
					
					if (b.validate()) {
						
						Monieo.INSTANCE.handleBlock(b);
						System.out.println("Handled valid block!");
						nonce = BigInteger.ZERO;
						curr.blocks++;
						curr.total = curr.total.add(ct.getAmount());
						break in;
						
					}
					
				}

				nonce = nonce.add(BigInteger.ONE);
				
				b.header.nonce = nonce;
				
			}
			
			curr.hashes = curr.hashes.add(BigInteger.valueOf(HASHES_BEFORE_RECHECK));
			supervisor.accept(curr);
			
		}
		
	}

}
