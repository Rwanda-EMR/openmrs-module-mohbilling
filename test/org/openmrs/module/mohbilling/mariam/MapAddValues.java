package org.openmrs.module.mohbilling.mariam;

import java.util.Collection;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

public class MapAddValues {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 MultiMap myMultimap = new MultiHashMap();
		 
	        // Adding some key/value
	        myMultimap.put("Fruits", "Banana");
	        myMultimap.put("Fruits", "Apple");
	        myMultimap.put("Fruits", "Pear");
	        myMultimap.put("Vegetables", "Carrot");
	 
//	        // Getting values
//	        Collection<String> fruits = (Collection<String>) myMultimap.get("Fruits");
//	        System.out.println(fruits); // [Banana, Apple, Pear]
//	        Collection<String> vegetables = (Collection<String>) myMultimap.get("Vegetables");
//	        System.out.println(vegetables); // [Carrot]
	 

	        Collection coll = (Collection) myMultimap.get("Fruits");
	        
	        System.out.println(coll);
	}

}
