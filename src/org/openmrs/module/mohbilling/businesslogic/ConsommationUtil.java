/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.util.Date;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author emr
 *
 */
public class ConsommationUtil {
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	

	public static Consommation saveConsommation(Consommation consom) {
		getService().saveConsommation(consom);
		return consom;
	}
	
	/**
	 * Creates a PatientServiceBill object and saves it in the DB through
	 * PatientBill which is its parent
	 * 
	 * @param psb
	 *            the PatientServiceBill to be saved
	 * @return psb the PatientServiceBill that has been saved
	 */
	public static PatientServiceBill createPatientServiceBill(
			PatientServiceBill psb) {

		Consommation consommation = new  Consommation();

		if (psb != null) {
			consommation = psb.getConsommation();
			consommation.addBillItem(psb);
			getService().saveConsommation(consommation);
			return psb;
		}

		return null;
	}
	public static Consommation createConsommation(Consommation consommation){
		
		GlobalBill globalBill = new GlobalBill(); 

		if (consommation != null) {
			consommation.setVoided(false);
			consommation.setCreatedDate(new Date());
			consommation.setCreator(Context.getAuthenticatedUser());
			globalBill = consommation.getGlobalBill();
			globalBill.addConsommation(consommation);
			getService().saveGlobalBill(globalBill);
			return consommation;
		}

		return null;
		
		
	}
	
	
	

}
