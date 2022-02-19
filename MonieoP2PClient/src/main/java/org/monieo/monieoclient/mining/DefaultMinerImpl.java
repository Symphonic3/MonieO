package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	
	public static final int HASHES_BEFORE_RECHECK = 100000;
	
	public DefaultMinerImpl() {};
	
	@Override
	public void begin(Consumer<MiningStatistics> sup) {
		
		while (t != null) {};
		
		stop = false;
		
		this.supervisor = sup;
		
		curr = new MiningStatistics(BigInteger.valueOf(0), BigInteger.valueOf(0), 0, BigDecimal.valueOf(0), System.currentTimeMillis());
		
		t = new Thread(this);
		t.start();
		
	}

	@Override
	public MiningStatistics getMiningStatistics() {
		return curr;
	}
	
	@Override
	public void stop() {
		stop = true;
		
	}

	@Override
	public String getMiningName() {
		return "DefaultCPUMiner";
	}

	@Override
	public void run() {
		
		MessageDigest digest = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		while (true) {
						if (stop) {
				
				t = null;
				return;
				
			}
			
			Block h = Monieo.INSTANCE.getHighestBlock();
			if (h == null) continue;
			long hei = h.header.height + 1;
			
			BigInteger diff = h.calculateNextDifficulty();
			
			curr.blockTarget = diff;
			
			long nettime = Monieo.INSTANCE.getNetAdjustedTime();

			List<AbstractTransaction> tx = Monieo.INSTANCE.txp.get(1024*70, h); //750 is completely arbitrary. This should be optimized later.
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
			
			BigInteger nonce = BigInteger.ZERO;
			
			//This algorithm is really bad! Should be reimplemented in C, which would also make it easier to convert to OpenCL in the future.
			in: for (int i = 0; i < HASHES_BEFORE_RECHECK; i++) {
				
				if (new BigInteger(1, b.rawHash()).compareTo(b.header.diff) == -1) {
					
					if (b.validate()) {
						
						System.out.println("Found valid block!");
						
						Monieo.INSTANCE.handleBlock(b);
						nonce = BigInteger.ZERO;
						curr.blocks++;
						curr.total = curr.total.add(ct.getAmount());
						break in;
						
					}
					
					throw new IllegalStateException("Found valid block but block could not be validated!");
					
				}

				nonce = nonce.add(BigInteger.ONE);
				
				b.header.nonce = nonce;
				
				//System.out.println(new BigInteger(1, b.rawHash()));
				
			}
			
			curr.hashes = curr.hashes.add(BigInteger.valueOf(HASHES_BEFORE_RECHECK));
			supervisor.accept(curr);
			
		}
		
	}

}
