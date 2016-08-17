/**
 * 
 */
package org.openmrs.module.mohbilling;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.model.Department;

/**
 * @author emr
 *
 */
public class GlobalPropertyConfig {
	
	
	public static String getListOfHopServicesByDepartment1(Department department){
		  return Context.getAdministrationService().getGlobalProperty("mohbilling."+department.getName()+"");
		 }
	
	/**
	 * gets a list of Hop Services by a matching  department  
	 * @param department
	 * @return Set<HopService> services 
	 */
	public static Set<String> getListOfHopServicesByDepartment(Department department){
		
		  Set<String> services = new HashSet<String>();
		  StringTokenizer tokenizer = new StringTokenizer(getListOfHopServicesByDepartment1(department),",");
		  while (tokenizer.hasMoreTokens()) {
		   Integer ServiceId = Integer.parseInt(tokenizer.nextToken());
		   String  hopServiceName= HopServiceUtil.getHopServiceById(ServiceId).getName();
		   services.add(hopServiceName);		   
		    
		        }
		  return services;
		 }	
}
