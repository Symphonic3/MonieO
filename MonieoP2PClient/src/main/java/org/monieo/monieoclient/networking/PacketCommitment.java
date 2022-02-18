package org.monieo.monieoclient.networking;

import java.util.function.Predicate;

public class PacketCommitment {
	
	public boolean isFilled = false;
	public long timeMax;
	
	Predicate<NetworkPacket> test;
	
	public PacketCommitment(Predicate<NetworkPacket> testResponse) {
		
		timeMax = System.currentTimeMillis() + Node.MIN_RESPONSE_TIME;
		this.test = testResponse;
		
	}
	
	public boolean attemptFillShouldBanNode(NetworkPacket nc) {
		
		if (test.test(nc)) {
			
			isFilled = true;
			return false;
			
		} else {
			
			if (System.currentTimeMillis() > timeMax) {
				
				return true;
				
			}
			
			return false;
			
		}
		
	}
	
}
