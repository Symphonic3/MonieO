package org.monieo.monieoclient.mining;

import org.monieo.monieoclient.Monieo;

public class DefaultMinerImpl implements AbstractMiner{

	Monieo m;
	
	public DefaultMinerImpl() {};
	
	@Override
	public void begin(Monieo m) {
		
		this.m = m;
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Monieo getMonieoInstance() {
		return m;
	}

	@Override
	public MiningStatistics retrieveMiningStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMiningName() {
		return "DefaultCPUMiner";
	}

}
