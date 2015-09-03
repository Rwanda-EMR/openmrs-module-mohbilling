package org.openmrs.module.mohbilling.mariam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class duplicatedMapKeys extends HashMap<String, List<Integer>>  {

	 public void put(String key, Integer number) {
	        List<Integer> current = get(key);
	        if (current == null) {
	            current = new ArrayList<Integer>();
	            super.put(key, current);
	        }
	        current.add(number);
	    }

	public static void main(String[] args) {
		duplicatedMapKeys m = new duplicatedMapKeys();
	        m.put("a", 2);
	        m.put("a", 2);
	        m.put("b", 3);
	        
	        List<Integer> values = new ArrayList<Integer>();
	        Map<String,Integer> newMap = new HashMap<String, Integer>();
	        
	        for(Map.Entry e : m.entrySet()) {
	            values=(List<Integer>) e.getValue();
	            Integer sum=0;
	            sum=m.getSum(values);
	            newMap.put((String) e.getKey(), sum);
	        }
	        for (String key : newMap.keySet()) {
				System.out.println("KEY :" +key+" Value: "+newMap.get(key));
			}
	}

	public Integer getSum(List<Integer> list){
		Integer sum=0;
		for (Integer t : list) {
			sum=sum+t;
		}
		return sum;
	}
	
}
