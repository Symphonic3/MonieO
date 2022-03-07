package org.monieo.monieoclient.networking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.monieo.monieoclient.Monieo;

public class NetAddressManager {
	
	public static final int NEW_SIZE = 9;
	public static final int TRIED_SIZE = 7;
	public static final int BUCKET_SIZE = 6;
	
	private static final int NEW_SIZE_REAL = (int) Math.pow(2, NEW_SIZE);
	private static final int TRIED_SIZE_REAL = (int) Math.pow(2, TRIED_SIZE);

	List<Bucket> newBuckets = new ArrayList<Bucket>(NEW_SIZE_REAL);
	List<Bucket> triedBuckets = new ArrayList<Bucket>(TRIED_SIZE_REAL);
	
	List<BannedNetAddress> bans = new ArrayList<BannedNetAddress>();
	
	File bansFile;
	
	public static final String[] HARDCODED_ADDRESSES =  {
		
		"104.205.241.243",
		"75.159.78.133"
		
	};
	
	public NetAddressManager() {
		
		File nfolder = new File(Monieo.INSTANCE.workingFolder.getPath() + "/nodes/new");
		File tfolder = new File(Monieo.INSTANCE.workingFolder.getPath() + "/nodes/tried");
		
		init(nfolder, newBuckets, NEW_SIZE_REAL);
		init(tfolder, triedBuckets, TRIED_SIZE_REAL);
		
		bansFile = new File(Monieo.INSTANCE.workingFolder.getPath() + "bannednodes.dat");
		try {
			bansFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (Scanner c = new Scanner(bansFile)) {
			
			while (c.hasNextLine()) {
				
				String s = c.nextLine();
				
				String[] spl = s.split(" ");
				
				if (spl.length != 2) throw new IllegalStateException("Tried to handle banned entry with more than 2 fields!");
				
				bans.add(new BannedNetAddress(spl[0], Long.valueOf(spl[1])));
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void init(File parent, List<Bucket> b, int amount) {
		
		parent.mkdirs();
		
		for (int i = 0; i < amount; i++) {
			
			File curr = new File(parent.getPath() + "/" + i + ".dat");
			if (!curr.exists())
				try {
					curr.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			b.add(new Bucket(curr));
			
		}
		
	}
	
	public String getPossibleAddressOutbound() {
		
		Random r = new Random();
		
		List<Bucket> possible = new ArrayList<Bucket>();
		
		boolean tried = r.nextBoolean();
		if (tried) {
			
			List<Integer> disallowed = new ArrayList<Integer>(Monieo.MAX_OUTGOING_CONNECTIONS);
			
			for (Node n : Monieo.INSTANCE.nodes) {
				
				//decide if this check should even be here
				if (!n.isServer()) {
					
					BucketNetAddress dummy = new BucketNetAddress(n.getAdress(), -1);
					
					disallowed.add(dummy.getTriedBucket());
					
				}
				
			}
			
			for (int i = 0; i < triedBuckets.size(); i++) {
				
				Bucket b = triedBuckets.get(i);
				
				if (!b.isEmpty() && (!disallowed.contains(i))) possible.add(b);
				
			}
			
			
		} else {
			
			for (Bucket b : newBuckets) {
				
				if (!b.isEmpty()) possible.add(b);
				
			}
			
		}
		
		if (possible.size() == 0) {
			
			return HARDCODED_ADDRESSES[r.nextInt(HARDCODED_ADDRESSES.length)];
			
		}
		
		Bucket b = possible.get(r.nextInt(possible.size()));
		
		return b.getRandom().adress;
		
	}
	
	public void ban(String add) {
		
		
		
	}
	
	public boolean isBanned(String add) {
		
		return false;
		
	}
	
	public void moveToNewTable(String add) {
		
		
		
	}
	
	public void moveToTriedTable(String add) {
		
		
	}
	
	public void addToNewTable(String add) {
		
		
		
	}
	
	public void successfullyConnectedOrDisconnected(String add) {
		
		//TODO all
		
	}
	
	public void couldNotConnectToNode(String add) {
		
		
		
	}
	
	public void forceSaveAll() {
		
		for (Bucket b : newBuckets) {
			
			b.save();
			
		}
		
		for (Bucket b : triedBuckets) {
			
			b.save();
			
		}
		
	}
	
	public String whoToEvict(String inboundAddress) {
		
		if (Monieo.INSTANCE.ch.full()) {
			
			//BucketNetAddress dummy = new BucketNetAddress(inboundAddress, -1);
			
			//TODO evict clients that are in the same bucket in favour of new clients
			
			return null;
			
		} else return null;
		
	}
	
}
