package org.monieo.monieoclient.networking;

public class NetworkPacket {
	
	public enum NetworkPacketType {
		
		SEND_VER,
		ACK_VER,
		REQUEST_BLOCKS_AFTER,
		REQUEST_SINGLE_BLOCK, //potentially unnessecary
		REQUEST_NODES,
		SEND_TRANSACTION,
		SEND_BLOCK,
		SEND_ADDR
			
	}
	
	String magicn;
	String ver;
	NetworkPacketType cmd;
	String data;
	
	public NetworkPacket(String m, String v, NetworkPacketType c, String d) {
		
		this.magicn = m;
		this.ver = v;
		this.cmd = c;
		this.data = d;
		
	}
	
	public String serialize() {
		
		return String.join("\n", String.join(" ", magicn, ver, cmd.toString()), data == null ? "" : data);
		
	}
	
	public static NetworkPacket deserialize(String s) {
		
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
			
			NetworkPacketType nct = NetworkPacketType.valueOf(netinfo[2]);
			
			return new NetworkPacket(netinfo[0], netinfo[1], nct, data);
			
		} catch (Exception e) {
			
			//invalid data (great code)
			return null;
			
		}
		
	}

}
