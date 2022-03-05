package org.monieo.monieoclient.randomx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.monieo.monieoclient.Monieo;

public class RandomXJNI {

	public static final long NULLPTR = 0;
	
    public static boolean DLLS_LOADED = false;
    
    public RandomXJNI() {
    	
    	if (!DLLS_LOADED) {
    		
            File f = new File(Monieo.INSTANCE.workingFolder.getPath() + "/randomx-dll.dll");
            File f2 = new File(Monieo.INSTANCE.workingFolder.getPath() + "/Dll1.dll");
            
            if (!f.exists()) {
            	
        		try {
        			Files.copy(Monieo.class.getClassLoader().getResourceAsStream("randomx-dll.dll"), f.toPath());
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
            	
            }
            
            if (!f2.exists()) {
            	
        		try {
        			Files.copy(Monieo.class.getClassLoader().getResourceAsStream("Dll1.dll"), f2.toPath());
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        		
            }

            try {
            	
            	System.load(f.getPath());
                System.load(f2.getPath());
                
                DLLS_LOADED = true;
                System.out.println("Loaded RandomX-JNI dlls successfully");
            	
            } catch (Exception e) {
            	
            	e.printStackTrace();
            	
            }
    		
    	}
    	
    }
    
    private static void copyStreamToFile(InputStream in, File f) {

    	try {
    		
        	OutputStream outStream = new FileOutputStream(f);

        	byte[] buffer = new byte[8 * 1024];
        	int bytesRead;
        	while ((bytesRead = in.read(buffer)) != -1) {
        		
        		outStream.write(buffer, 0, bytesRead);
        		
        	}
        	
        	in.close();
        	outStream.flush();
        	outStream.close();
    		
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    public native int randomxGetFlags();
    public native long randomxAllocCache(int flags);
    public native void randomxInitCache(long cachePointer, String key, int keySize);
    public native void randomxReleaseCache(long cachePointer);
    public native long randomxAllocDataSet(int flags);
    public native long randomxDatasetItemCount();
    public native void randomxInitDataset(long datasetPointer, long cachePointer, long startItem, long itemCount);
    public native void randomxReleaseDataset(long datasetPointer);
    public native long randomxCreateVmWithDataset(int flags, long datasetPointer);
    public native long ranomxCreateVmWithCache(int flags, long cachePointer);
    //public native void randomxVmSetCache(long vmPointer, long cachePointer);
    //public native void randomxVmSetDataset(long vmPointer, long datasetPointer);
    public native void randomxDestroyVm(long vmPointer);
    public native byte[] randomxCalculateHash(long vmPointer, String input, long inputSize);
    
}