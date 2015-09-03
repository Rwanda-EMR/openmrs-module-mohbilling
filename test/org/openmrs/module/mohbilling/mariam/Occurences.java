package org.openmrs.module.mohbilling.mariam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

public class Occurences {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		Object[][] objs = new Object[3][4];
		
//		objs[0][0]="2015-03-15";
//		objs[0][1]=100;
//		objs[0][2]=200;
//		objs[0][3]=300;
//		
//		objs[1][0]="2015-03-15";
//		objs[1][1]=3;
//		objs[1][2]=5;
//		objs[1][3]=9;
		
		Object objs[][]={
				{"2015-03-15",1,2,3},
				{"2015-03-15",3,5,9},
				{"2015-03-16",4,5,7},
				{"2015-03-16",3,5,9}	
		};
		

		Object newObj[][] = new Object[4][4];
		Map<String,Integer> total =  new HashMap<String, Integer>();;
			
		for (int i = 0; i < 3; i++) {
			String date="";

			for (int j = 0; j < 3; j++) {
				Integer sum=0;
//				System.out.print(objs[i][j]+" ");
//				System.out.println();
				date=(String) objs[i][0];
				if(date.equals(objs[i+1][0])){
//					System.out.println("equal "+date+" and"+objs[i+1][j]);
					Integer d = (Integer) (objs[i+1][j+1]);
					sum=sum+d;
					System.out.println("d "+d);
					System.out.println("sum "+sum);
				}
				else
					sum = sum+(Integer) objs[0][1];
				total.put(date, sum);
			}
			
			System.out.println();

		}
	for (String m : total.keySet()) {
		System.out.println(m+" value: "+total.get(m));
	}
}
}