/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.service.BillingService;

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
		getService().getHopService(serviceId);
		
		return null;
		
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
	

}
