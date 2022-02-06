package org.monieo.monieoclient.blockchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.Scanner;

public class BlockMetadata {

	File blockmetafile;
	
	public BlockMetadata(File f) {
		
		this.blockmetafile = f;
		
	}
	
	public BigDecimal balance(WalletAdress wa) {
		
		try (Scanner c = new Scanner(blockmetafile)) {
			
			while (c.hasNextLine()) {
				
				String[] s = c.next().split(" ");
				if (s.length != 2) throw new IllegalStateException("Detected invalid balance field in block metadata! " + blockmetafile.getPath());
				
				if (s[0].equalsIgnoreCase(wa.adress)) {
					
					return new BigDecimal(s[1]);
					
				}
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return BigDecimal.ZERO;
		
	}
	
}
