package org.monieo.monieoclient.blockchain;

import org.monieo.monieoclient.Monieo;

public abstract class MonieoDataObject {

	public final String magicn;
	public final String ver;
	
	public MonieoDataObject(String m, String v) {
		
		this.magicn = m;
		this.ver = v;
		
	}
	
	private boolean hasBeenValidated = false;
	private boolean isValid = false;
	
	public boolean validate() {
		
		if (!hasBeenValidated) {
			
			isValid = Monieo.assertSupportedProtocol(new String[]{magicn,ver}) && testValidity();
			
			hasBeenValidated = true;
			
		}
		
		return isValid;
		
	}
	
	abstract boolean testValidity();
	public abstract String serialize();
	
	@Override
	public boolean equals(Object other) {
		
		if (other instanceof MonieoDataObject) {
			
			return serialize().equals(((MonieoDataObject) other).serialize());
			
		} else return false;
		
	}
	
}
