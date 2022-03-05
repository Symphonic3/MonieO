package org.monieo.monieoclient.randomx;

import org.monieo.monieoclient.Monieo;

public class RandomX {

	enum RandomXFlags {
		
		  RANDOMX_FLAG_DEFAULT(0),
		  RANDOMX_FLAG_LARGE_PAGES(1),
		  RANDOMX_FLAG_HARD_AES(2),
		  RANDOMX_FLAG_FULL_MEM(4),
		  RANDOMX_FLAG_JIT(8),
		  RANDOMX_FLAG_SECURE(16),
		  RANDOMX_FLAG_ARGON2_SSSE3(32),
		  RANDOMX_FLAG_ARGON2_AVX2(64),
		  RANDOMX_FLAG_ARGON2(96);

		private final int value;
		  
		RandomXFlags(int i) {
			
			value = i;
		
		}
		
	    public int v() {
	        return value;
	    }
		
	};
	
	public static final String KEY = "MonieO.";
	
	private static RandomX INSTANCE = null;
	private static final RandomXJNI RXJNI = new RandomXJNI();
	
	int flags;
	
	long datasetPtr = RandomXJNI.NULLPTR;
	long cachePtr = RandomXJNI.NULLPTR;
	long vmPtr = RandomXJNI.NULLPTR;
	
	private RandomX(int f, String key) {
		
		flags = f;
    	
    	System.out.print("Initializing RandomX...");
		
    	cachePtr = RXJNI.randomxAllocCache(flags);
    	if (cachePtr == RandomXJNI.NULLPTR) {
    		
    		throw new RuntimeException("Could not allocate RandomX cache!");
    		
    	}
    	
    	RXJNI.randomxInitCache(cachePtr, key, key.length());
    	
    	datasetPtr = RXJNI.randomxAllocDataSet(flags);
    	if (datasetPtr == RandomXJNI.NULLPTR) {
    		
    		throw new RuntimeException("Could not allocate RandomX dataset!");
    		
    	}
    	
    	long dIC = RXJNI.randomxDatasetItemCount();
    	
    	RXJNI.randomxInitDataset(datasetPtr, cachePtr, 0, dIC);
    	
    	RXJNI.randomxReleaseCache(cachePtr);
    	cachePtr = RandomXJNI.NULLPTR;
    	
    	vmPtr = RXJNI.randomxCreateVmWithDataset(flags, datasetPtr);
    	if (vmPtr == RandomXJNI.NULLPTR) {
    		
    		System.out.println("Could not create RandomX virtual machine!");
    		
    	}
    	
    	System.out.println("RandomX Initialized!");
		
	}
	
	public byte[] hash(String data) {
		
		String dn = Monieo.sha256d(data);
		
		return RXJNI.randomxCalculateHash(vmPtr, dn, dn.length());
		
	}
	
	public void close() {
		
		if (cachePtr != RandomXJNI.NULLPTR) RXJNI.randomxReleaseCache(cachePtr);
		if (vmPtr != RandomXJNI.NULLPTR) RXJNI.randomxDestroyVm(vmPtr);
		if (datasetPtr != RandomXJNI.NULLPTR) RXJNI.randomxReleaseDataset(datasetPtr);
		
		INSTANCE = null;
		
	}
	
	public static RandomX getRandomX() {
		
    	//while (true) {}
		
		if (INSTANCE == null) {
			
			INSTANCE = new RandomX(getFlags(), KEY);
			
		}
		
		return INSTANCE;
		
	}
	
	public static void setRandomX(RandomXFlags... add) {
		
		int f = computeFlags(add);
		
		if (INSTANCE.flags != f) {
			
			if (INSTANCE != null) INSTANCE.close();
			INSTANCE = new RandomX(f, KEY);
			
		}
		
	}
	
	public static int computeFlags(RandomXFlags... add) {
		
		int i = getFlags();
		
		for (RandomXFlags f : add) i |= f.v();

		return i;
		
	}
	
	public static int getFlags() {
		
		int i = 0;
		
		i |= RandomXFlags.RANDOMX_FLAG_FULL_MEM.v();
		//i |= RandomXFlags.RANDOMX_FLAG_JIT.v();
		
		return i;
		
	}
	
}