package org.monieo.monieoclient.randomx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.monieo.monieoclient.Monieo;

public class RandomXManager {

	public enum RandomXFlags {
		
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
	
	private static RandomXManager INSTANCE = null;
	public static final RandomXJNI RXJNI = new RandomXJNI();
	
	int flags;
	
	private long datasetPtr = RandomXJNI.NULLPTR;
	private long cachePtr = RandomXJNI.NULLPTR;
	
	public HashMap<Long, RandomXInstance> rx = new HashMap<Long, RandomXInstance>();
	
	private RandomXManager(int f, String key) {
		
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
    	long ea = dIC/Monieo.SYSTEM_LOGICAL_THREADS;
    	long acc = 0;
    	
    	List<Thread> threads = new ArrayList<Thread>();
    	
    	for (int i = 0; i < Monieo.SYSTEM_LOGICAL_THREADS; i++) {
    		
    		long k = acc;
    		acc += ea;
    		
    		Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
		        	
		        	RXJNI.randomxInitDataset(datasetPtr, cachePtr, k, ea);
					
				}
				
			});
			
			t.start();
			threads.add(t);
    		
    	}
    	
    	RXJNI.randomxInitDataset(datasetPtr, cachePtr, acc, dIC-acc);
    	
    	for (Thread t : threads)
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	
    	RXJNI.randomxReleaseCache(cachePtr);
    	cachePtr = RandomXJNI.NULLPTR;
    	
    	System.out.println("RandomX Initialized!");
		
	}
	
	public static RandomXManager getManager() {
		
		return INSTANCE;
		
	}
	
	public RandomXInstance getRandomX() {
		
		long tid = Thread.currentThread().getId();
		
		if (!rx.containsKey(tid)) {
		
			clean();
			long vmPtr = RXJNI.randomxCreateVmWithDataset(flags, datasetPtr);
	    	if (vmPtr == RandomXJNI.NULLPTR) {
	    		
	    		throw new RuntimeException("Could not create RandomX virtual machine!");
	    		
	    	}
			rx.put(tid, new RandomXInstance(vmPtr));
			
		}

		return rx.get(tid);
		
	}
	
	public void clean() {
		
		rx.keySet().removeIf(new Predicate<Long>() {

			@Override
			public boolean test(Long l) {
				
				ThreadGroup t = Thread.currentThread().getThreadGroup();
				
				while (true) {
					
					ThreadGroup n = t.getParent();
					
					if (n != null) {
						
						t = n;
						
					} else break;
					
				}
				
				Thread[] ret = new Thread[256];
				int am = t.enumerate(ret);
				
				for (int i = 0; i < am; i++) {
					
					Thread tr = ret[i];
					
					long id = tr.getId();
					
					if (id == l) return false;
					
				}
				
				return true;

			}
			
		});
		
	}
	
	public void close() {
		
		if (cachePtr != RandomXJNI.NULLPTR) RXJNI.randomxReleaseCache(cachePtr);
		
		for (Long i : rx.keySet()) {
			
			rx.get(i).release();
			
		}
		
		if (datasetPtr != RandomXJNI.NULLPTR) RXJNI.randomxReleaseDataset(datasetPtr);
		
		INSTANCE = null;
		
	}
	
	public static void setRandomX(RandomXFlags... add) {
		
		int f = computeFlags(add);
		
		if (INSTANCE == null || INSTANCE.flags != f) {
			
			if (INSTANCE != null) INSTANCE.close();
			INSTANCE = new RandomXManager(f, KEY);
			
		}
		
	}
	
	public static int computeFlags(RandomXFlags... add) {
		
		int i = getFlags();
		
		for (RandomXFlags f : add) i |= f.v();

		return i;
		
	}
	
	public static int getFlags() {
		
		int i = RXJNI.randomxGetFlags();
		
		i |= RandomXFlags.RANDOMX_FLAG_FULL_MEM.v();
		
		return i;
		
	}
	
}