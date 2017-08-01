package org.openmrs.module.mohbilling.businesslogic;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openmrs.api.context.Context;

public class BillingGlobalProperties {
	
	public static String getGpServiceCategory(){
		
		 return Context.getAdministrationService().getGlobalProperty("billing.serviceCategories");
	}
	public static String getGpServiceCategoryColumns(){
		
		return Context.getAdministrationService().getGlobalProperty("billing.reportColumns");
		
	}
	public static List<String> getListofServiceCategory(){		
		 ArrayList<String> list = new ArrayList<String>();
		  StringTokenizer tokenizer = new StringTokenizer(getGpServiceCategory(),",");
		  while (tokenizer.hasMoreTokens()) {
		   String catgoryStr = tokenizer.nextToken();
		         list.add(catgoryStr);
		        }
		  return list;
		
	}
	public static ArrayList<String> getreportColumns(){
		
		ArrayList<String> list = new ArrayList<String>();
		  StringTokenizer tokenizer = new StringTokenizer(getGpServiceCategoryColumns(),",");
		  while (tokenizer.hasMoreTokens()) {
		   String catgoryStr = tokenizer.nextToken();
		         list.add(catgoryStr);
		        }
		  return list;
        }
	}
