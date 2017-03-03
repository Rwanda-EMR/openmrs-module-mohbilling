/**
 * 
 */
package org.openmrs.module.mohbilling;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.HopService;

import java.util.*;

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
		  StringTokenizer tokenizer = null;
		  if(getListOfHopServicesByDepartment1(department)!=null){
		  tokenizer = new StringTokenizer(getListOfHopServicesByDepartment1(department),",");
		  while (tokenizer.hasMoreTokens()) {
		   Integer ServiceId = Integer.parseInt(tokenizer.nextToken());
		   String  hopServiceName= HopServiceUtil.getHopServiceById(ServiceId).getName();
		   services.add(hopServiceName);	
		   }
		  }
		  return services;
		 }	
	
	 /**
		 * Gets a List of services from global properties matching with a given parameter
		 * @param revenueCateg parameters name configured in Global property
		 * @return List<HopService>
		 */
		public static List<HopService> getHospitalServiceByCategory(String categ){
			List<HopService> services = new ArrayList<HopService>();
			try {
				  StringTokenizer tokenizer = new StringTokenizer(getHospitalServiceByCateg(categ),",");
				  while (tokenizer.hasMoreTokens()) {
				   Integer serviceId = Integer.parseInt(tokenizer.nextToken());
				   HopService service = HopServiceUtil.getHopServiceById(serviceId);
				   services.add(service);
					System.out.println("rrrrrrrrrrrrrrrrrrrrrrrrrrrrruuuuuuuuuuuuuuuuuuuuuuuuuouyt "+service.getName().toString());
				        }
			} catch (Exception e) {
				// TODO: handle exception
			}
			  return services;
		}
		 /**
		 * gets a  revenues matching revenueCateg
		 * @param category
		 * @return String object
		 */
		public static String getHospitalServiceByCateg(String categ){
			return Context.getAdministrationService().getGlobalProperty(categ);
		}
}
