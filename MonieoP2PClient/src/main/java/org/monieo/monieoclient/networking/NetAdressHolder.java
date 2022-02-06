package org.monieo.monieoclient.networking;

import org.monieo.monieoclient.Monieo;

public class NetAdressHolder {
	
	public String adress;
	public long expiry;
	public long banExpiry;
	
	public NetAdressHolder(String a, long e, long b) {
		
		this.adress = a;
		this.expiry = e;
		this.banExpiry = b;
		
	}
	
	public boolean isBanned() {
		
		return System.currentTimeMillis() < banExpiry;
		
	}
	
	public boolean isExpired() {
		
		return System.currentTimeMillis() > expiry;
		
	}
	
	public void ban() {
		
		banExpiry = System.currentTimeMillis() + 86400000; //24 hours
		Monieo.INSTANCE.saveKnownNodes();
		
	}
	
	public String serialize() {
		
		return String.join(" ", adress, String.valueOf(expiry), String.valueOf(banExpiry));
		
	}
	
	public static NetAdressHolder deserialize(String in) {
		
		try {
			
			String[] s = in.split(" ");
			
			if (s.length != 3) return null;
			
			return new NetAdressHolder(s[0], Long.valueOf(s[1]), Long.valueOf(s[2]));
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
			
		}
		
	}
	
}
