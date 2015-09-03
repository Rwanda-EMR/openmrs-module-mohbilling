package org.openmrs.module.mohbilling.mariam;

import java.text.DecimalFormat;

public class Round {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double kilobytes = 6022.999;
		 
		System.out.println("kilobytes : " + kilobytes);
	 
		double newKB = Math.round(kilobytes*100)/100;
		System.out.println("kilobytes (Math.round) : " + newKB);
	 
		DecimalFormat df = new DecimalFormat("###.##");
		System.out.println("kilobytes (DecimalFormat) : " + df.format(kilobytes));

	}

}
