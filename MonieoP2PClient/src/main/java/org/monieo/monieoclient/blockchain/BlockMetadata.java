package org.monieo.monieoclient.blockchain;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BlockMetadata {

	File blockmetafile;
	
	public BlockMetadata(File f) {
		
		this.blockmetafile = f;
		
	}
	
	public static BigDecimal getSpendableBalance(List<PendingFunds> pf) {
		
		BigDecimal bal = new BigDecimal(0);
		
		for (PendingFunds t : pf) {
			
			if (t.spendable()) bal = bal.add(t.amount);
			
		}
		
		return bal;
		
	}
	
	public static BigDecimal getUnspendableBalance(List<PendingFunds> pf) {
		
		BigDecimal bal = new BigDecimal(0);
		
		for (PendingFunds t : pf) {
			
			if (!t.spendable()) bal = bal.add(t.amount);
			
		}
		
		return bal;
		
	}
	
	public static BigDecimal getTotalBalanceInBlocks(List<PendingFunds> pf) {
		
		BigDecimal bal = new BigDecimal(0);
		
		for (PendingFunds t : pf) {
			
			bal = bal.add(t.amount);
			
		}
		
		return bal;
		
	}
	
	public List<PendingFunds> getFullTransactions(WalletAdress wa) {
		
		try (Scanner c = new Scanner(blockmetafile)) {
			
			while (c.hasNextLine()) {
				
				String da = c.nextLine();
				String[] s = da.split(" ");
				if (s.length < 2) throw new IllegalStateException("Detected invalid balance field in block metadata! " + blockmetafile.getPath());
				
				if (s[0].equals(wa.adress)) {
					
					return getTXCS(da);
					
				}
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<PendingFunds>();
		
	}
	
	public static List<PendingFunds> getTXCS(String line) {
		
		List<PendingFunds> tcs = new ArrayList<PendingFunds>();
		
		String[] s = line.split(" ");
		if (s.length < 2) throw new IllegalStateException("Detected invalid balance field in block metadata! (no file)");
		
		for (int i = 1; i < s.length; i++) {
			
			PendingFunds pf = PendingFunds.deserialize(s[i]);
			
			if (pf.amount.signum() != 1) {
				
				continue;
				
			}
			
			tcs.add(pf);
			
		}
		
		return tcs;
		
	}
	
	public Block getBlock() {
		
		return Block.getByHash(blockmetafile.getName().split(".")[0]);
		
	}
	
}
