package org.monieo.monieoclient.mining;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

import org.monieo.monieoclient.Monieo;

public class DefaultMinerImpl implements AbstractMiner{

	Thread t = null;
	
	private boolean stop = false;
	
	Monieo m;
	
	Consumer<MiningStatistics> supervisor = null;
	
	public DefaultMinerImpl() {};
	
	@Override
	public void begin(Monieo m, Consumer<MiningStatistics> sup) {
		
		this.m = m;
		this.supervisor = sup;
		
		t = new Thread(this);
		t.start();
		
	}

	@Override
	public void stop() {
		stop = true;
		supervisor.accept(new MiningStatistics(0, BigInteger.valueOf(0), 0, 0, BigDecimal.valueOf(0)));
		
	}

	@Override
	public Monieo getMonieoInstance() {
		return m;
	}

	@Override
	public String getMiningName() {
		return "DefaultCPUMiner";
	}

	@Override
	public void run() {
		
		while (true) {
			
			if (stop) return;
			
		}
		
	}

}
