package org.monieo.monieoclient.randomx;

import org.monieo.monieoclient.Monieo;

public class RandomXInstance {
	
	protected long vmPtr = RandomXJNI.NULLPTR;
	public volatile boolean alive = true;
	
	public RandomXInstance(long vmPtr) {
		
		this.vmPtr = vmPtr;
		
	}
	
	public void release() {
		
		if (alive) {

			alive = false;
			
			RandomXManager.RXJNI.randomxDestroyVm(vmPtr);
			
		}
		
	}
	
	public byte[] hash(String data) {
		
		String dn = Monieo.sha256d(data);
		return RandomXManager.RXJNI.randomxCalculateHash(vmPtr, dn, dn.length());
		
	}

}
