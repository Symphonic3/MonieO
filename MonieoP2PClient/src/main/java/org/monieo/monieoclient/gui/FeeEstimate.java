package org.monieo.monieoclient.gui;

import java.math.BigDecimal;

public class FeeEstimate {

	BigDecimal fee;
	FeeEstimateType t;
	
	public FeeEstimate(BigDecimal fee, FeeEstimateType t) {
		
		this.fee = fee;
		this.t = t;
		
	}
	
	public enum FeeEstimateType {
		
		SMALLEST,
		AVERAGE,
		LARGEST
		
	}
	
	@Override
	public String toString() {
		
		String s = "";
		
		if (t == FeeEstimateType.SMALLEST) {
			
			s = "1 hour";
			
		} else if (t == FeeEstimateType.AVERAGE) {
			
			s = "30 minutes";
			
		} else if (t == FeeEstimateType.LARGEST) {
			
			s = "1 minute";
			
		}
		
		return "Processing estimate: " + s + " (" + fee.toPlainString() + " MNO)";
		
	}
	
}
