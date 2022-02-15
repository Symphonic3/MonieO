package org.monieo.monieoclient.networking;

public class NetworkCommand {
	
	public enum NetworkCommandType {
		
		SEND_VER,
		ACK_VER,
		REQUEST_BLOCKS_AFTER,
		REQUEST_SINGLE_BLOCK,
		REQUEST_NODES,
		REQUEST_TIME,
		SEND_TRANSACTION,
		SEND_BLOCK,
		SEND_NODES,
		SEND_TIME
			
	}
	
	String magicn;
	String ver;
	NetworkCommandType cmd;
	String data;
	
	public NetworkCommand(String m, String v, NetworkCommandType c, String d) {
		
		this.magicn = m;
		this.ver = v;
		this.cmd = c;
		this.data = d;
		
	}
	
	public String serialize() {
		
		return String.join("\n", String.join(" ", magicn, ver, cmd.toString()), data == null ? "" : data);
		
	}
	
	public static NetworkCommand deserialize(String s) {
		
		try {
			
			int ind = s.indexOf("\n");
			if (ind == -1) ind = s.length();
			
			String[] netinfo = s.substring(0, ind).split(" ");
			
			String data;
			
			try {
				
				data = s.substring(ind+1);
				
			} catch (IndexOutOfBoundsException e) {
				
				data = "";
				
			}
			
			NetworkCommandType nct = NetworkCommandType.valueOf(netinfo[2]);
			
			return new NetworkCommand(netinfo[0], netinfo[1], nct, data);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

}
