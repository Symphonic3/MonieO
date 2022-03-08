package org.monieo.monieoclient.networking;

import org.monieo.monieoclient.Monieo;

public class BannedNetAddress {

	public String adress;
	public long banExpiry;
	
	public BannedNetAddress(String a, long banExpiry) {
		
		this.adress = a;
		this.banExpiry = banExpiry;
		
	}
	
	public boolean isBanned() {
		
		return System.currentTimeMillis() < banExpiry;
		
	}
	
	protected void ban() {
		
		banExpiry = System.currentTimeMillis() + 86400000; //24 hours
		Monieo.INSTANCE.nam.saveBans();
		
	}
	
	
	public String serializeForBans() {
		
		return String.join(" ", adress, String.valueOf(banExpiry));
		
	}
	
}
