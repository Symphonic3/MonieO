package org.monieo.monieoclient.networking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Bucket {
	
	BucketNetAddress[] addresses = new BucketNetAddress[64];
	
	boolean empty = true;
	
	File f;
	
	public Bucket(File f, boolean tried) {
		
		this.f = f;
		
		int i = 0;
		
		try (Scanner c = new Scanner(f)) {
			
			while (c.hasNextLine()) {
				
				String s = c.nextLine();
				
				if (s == null || s.equals("")) break;

				String[] spl = s.split(" ");
				
				addresses[i] = new BucketNetAddress(spl[0], Long.valueOf(spl[1]), tried);
				empty = false;
				
				i++;

			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public void save() {
		
		try (FileWriter fw = new FileWriter(f, false)) {
			
			for (BucketNetAddress address : addresses) {
				
				if (address == null) fw.append("\n"); else fw.append(address.serializeForBucket() + "\n");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public boolean isEmpty() {
		
		return empty;
		
	}
	
	public BucketNetAddress getRandom() {
		
		Random r = new Random();
		
		if (isEmpty()) return null;
		
		List<BucketNetAddress> possible = new ArrayList<BucketNetAddress>();
		
		for (int i = 0; i < 64; i++) {
			
			BucketNetAddress c = addresses[i];
			
			if (c == null) continue;
			
			possible.add(c);
			
		}
		
		return possible.get(r.nextInt(possible.size()));
		
	}

}
