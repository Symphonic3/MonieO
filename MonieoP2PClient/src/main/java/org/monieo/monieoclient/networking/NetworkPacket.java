package org.monieo.monieoclient.networking;

import java.util.ArrayList;
import java.util.List;

import org.monieo.monieoclient.Monieo;
import org.monieo.monieoclient.blockchain.Block;

public class NetworkPacket {
	
	public enum NetworkPacketType {
		
		SEND_VER,
		ACK_VER,
		REQUEST_BLOCKS_AFTER,
		REQUEST_SINGLE_BLOCK, //potentially unnessecary
		SEND_TRANSACTION,
		SEND_BLOCK,
		SEND_ADDR,
		KEEPALIVE
			
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
	
	public static NetworkPacket generateSyncPacket() {
		
		int step = 10;
		
		List<String> hashes = new ArrayList<String>();
		
		Block b = Monieo.INSTANCE.getHighestBlock();
		
		outer: while (true) {
			
			for (int i = 0; i < (step-1); i++) {
				
				b = b.getPrevious();
				if (b == null) {
					
					hashes.add(Monieo.GENESIS_HASH);
					break outer;
					
				}
				
			}
			
			step *= 2;
			
			if (b.getPrevious() != null) hashes.add(b.header.preHash);
			
		}
		
		return new NetworkPacket(Monieo.MAGIC_NUMBERS, Monieo.PROTOCOL_VERSION, NetworkPacketType.REQUEST_BLOCKS_AFTER, String.join(" ", hashes));

	}

}
