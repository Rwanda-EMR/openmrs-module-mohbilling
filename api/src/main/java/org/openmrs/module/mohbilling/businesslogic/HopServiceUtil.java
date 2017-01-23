/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author emr
 *
 */
public class HopServiceUtil {
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	public static HopService createHopService(HopService service){
		
		getService().saveHopService(service);
		return service;
		
	}
	public static HopService getHopServiceById(Integer serviceId){
	return	getService().getHopService(serviceId);		
	
	}
	/**
	 * Get all services  by departement
	 * departement is null it will load all departement
	 * @param departement
	 * @return a list<Hopservices>
	 */
	public static List<HopService> getAllHospitalServices(){
	     return 	getService().getAllHopService();		
	}
	public  static Set<ServiceCategory> getServiceCategoryByInsurancePolicyDepartment(InsurancePolicy ip,Department department){
		Set<ServiceCategory> categories = new HashSet<ServiceCategory>();
		Set<String> serviceNames = GlobalPropertyConfig.getListOfHopServicesByDepartment(department);
		
		for (ServiceCategory category : ip.getInsurance().getCategories()) {
			if (serviceNames.contains(category.getName())) {
				categories.add(category);
				
			}
			
		}
		
		return categories;
	
	}
	
	public static List<HopService> getHospitalServicesByDepartment(Department department){
		return getService().getHospitalServicesByDepartment(department);
	}
	
	
	/**
	 * Gets HopServiceByName
	 * @param name
	 * @return HopService
	 */
	public static HopService getServiceByName(String name) {
		
		return getService().getServiceByName(name);
	}	

}
