package org.monieo.monieoclient.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.monieo.monieoclient.Monieo;

public class BucketNetAddress {
	
	public String adress;
	public long timePlaced;
	public boolean tried;
	
	public BucketNetAddress(String a, long timePlaced, boolean tried) {
		
		this.adress = a;
		this.timePlaced = timePlaced;
		this.tried = tried;
		
	}
	
	public String serializeForBucket() {
		
		return String.join(" ", adress, String.valueOf(timePlaced));
		
	}
	
	byte[] addressBytes() {
		
		try {
			return InetAddress.getByName(adress).getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not parse IP: " + adress);
		}
		
	}
	
	public static byte[] half(byte[] in, boolean first) {
		
		int ild2 = (in.length/2);
		
		byte[] ret = new byte[ild2];
		
		for (int i = 0; i < ild2; i++) {
			
			if (first) {
				
				ret[i] = in[i];
				
			} else {
				
				ret[i] = in[i + ild2];
				
			}
			
		}
		
		return ret;
		
	}
	
	public int getNewBucket() {
		
		return entropicValue(half(addressBytes(), true), NetAddressManager.NEW_SIZE);
		
	}
	
	public int getTriedBucket() {
		
		return entropicValue(half(addressBytes(), true), NetAddressManager.TRIED_SIZE);
		
	}
	
	public int getBucketPosition() {
		
		return entropicValue(half(addressBytes(), false), NetAddressManager.BUCKET_SIZE);
		
	}
	
	public static int entropicValue(byte[] in, int bits) {
		
		byte[] hash = Monieo.sha256dRawInRawOut(in);
		
		int ret = 0;
		
		for (int i = 0; i < bits; i++) {
			
			int byteN = i/8;
			
			byte curr = hash[byteN];
			
			ret += curr & (int)(Math.pow(2, i - (byteN*8)));
			
		}
		
		return ret;
		
	}
	
	
	public Bucket getBucket(NetAddressManager nam) {
		
		if (tried) {
			
			return nam.triedBuckets.get(getTriedBucket());
			
		} else {
			
			return nam.newBuckets.get(getNewBucket());
			
		}
		
	}
	
}
