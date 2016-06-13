/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author emr
 *
 */
public class AdmissionUtil {
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	
	/**
	 * save the patient admission to DB
	 * @param admission patient admission to be saved
	 * @return the admission saved
	 */
	public Admission savePatientAdmission(Admission admission){
		
		return getService().saveAdmission(admission);
		
	}
	
}
