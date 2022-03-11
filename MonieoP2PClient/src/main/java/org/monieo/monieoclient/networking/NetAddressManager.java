package org.monieo.monieoclient.networking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Predicate;

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
		
		init(nfolder, newBuckets, NEW_SIZE_REAL, false);
		init(tfolder, triedBuckets, TRIED_SIZE_REAL, true);
		
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
	
	private void init(File parent, List<Bucket> b, int amount, boolean tried) {
		
		parent.mkdirs();
		
		for (int i = 0; i < amount; i++) {
			
			File curr = new File(parent.getPath() + "/" + i + ".dat");
			if (!curr.exists())
				try {
					curr.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			b.add(new Bucket(curr, tried));
			
		}
		
	}
	
	public List<String> get1000Addresses() {
		
		List<String> ret = new ArrayList<String>();
		
		List<Bucket> possible = new ArrayList<Bucket>();
		
		for (int i = 0; i < triedBuckets.size(); i++) {
			
			Bucket b = triedBuckets.get(i);
			
			if (!b.isEmpty()) possible.add(b);
			
		}
		
		for (int i = 0; i < newBuckets.size(); i++) {
			
			Bucket b = newBuckets.get(i);
			
			if (!b.isEmpty()) possible.add(b);
			
		}
		
		Collections.shuffle(possible);
		
		ou: for (Bucket b : possible) {
			
			for (int i = 0; i < 64; i++) {
				
				BucketNetAddress c = b.addresses[i];
				
				if (c != null) {
					
					if (ret.size() < 1000) {
						
						ret.add(c.adress);
						
					} else break ou;
					
				}
				
			}
			
		}
		
		return ret;
		
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
					
					BucketNetAddress dummy = new BucketNetAddress(n.getAdress(), -1, true);
					
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
		
		bans.removeIf(new Predicate<BannedNetAddress>() {

			@Override
			public boolean test(BannedNetAddress t) {
				return t.adress.equals(add);
			}
			
		});
		
		BannedNetAddress an = new BannedNetAddress(add, -1);
		bans.add(an);
		an.ban();
		Monieo.INSTANCE.nam.saveBans();
		
	}
	
	public boolean isBanned(String add) {
		
		for (BannedNetAddress n : bans) {
			
			if (n.adress.equals(add)) {
				
				return n.isBanned();
				
			}
			
		}
		
		return false;
		
	}
	
	public void gotNew(String add) {
		
		BucketNetAddress dummy = new BucketNetAddress(add, System.currentTimeMillis(), true);
		
		Bucket b = dummy.getBucket(this);
		
		if (b.addresses[dummy.getBucketPosition()].adress.equals(add)) return;
		dummy.tried = false;
		if (b.addresses[dummy.getBucketPosition()].adress.equals(add)) return;
		
		b.addresses[dummy.getBucketPosition()] = dummy;
		
		saveBuckets();
		
	}
	
	public void switchAction(String add, boolean triedOriginally) {
		
		remove(add, triedOriginally);
		
		BucketNetAddress dummy = new BucketNetAddress(add, System.currentTimeMillis(), !triedOriginally);
		
		Bucket b = dummy.getBucket(this);
		
		BucketNetAddress t = b.addresses[dummy.getBucketPosition()];
		
		if (t != null && t.adress.equals(add)) return;
		
		b.addresses[dummy.getBucketPosition()] = dummy;
		
		saveBuckets();
		
	}
	
	public void successfullyConnectedOrDisconnected(String add) {
		
		switchAction(add, false);
		
	}
	
	public void couldNotConnectToNode(String add) {
		
		switchAction(add, true);
		
	}
	
	private void remove(String add, boolean triedTable) {
		
		BucketNetAddress dummy = new BucketNetAddress(add, -1, triedTable);
		
		Bucket b = dummy.getBucket(this);
		
		BucketNetAddress t = b.addresses[dummy.getBucketPosition()];
		
		if (t != null && t.adress.equals(add)) {
			
			b.addresses[dummy.getBucketPosition()] = null;
			
		}
		
	}
	
	public void saveBans() {
		
		try (FileWriter fw = new FileWriter(bansFile)) {
			
			for (BannedNetAddress n : bans) {
				
				if (n.isBanned()) {
					
					fw.append(n.adress + " " + n.banExpiry);
					
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void saveBuckets() {
		
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
