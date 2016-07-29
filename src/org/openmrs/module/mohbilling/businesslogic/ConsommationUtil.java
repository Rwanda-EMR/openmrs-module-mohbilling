/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
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
	
	public static Consommation getConsommation(Integer consommationId){
		return getService().getConsommation(consommationId);
		
	}
	public static PatientServiceBill saveBilledItem(PatientServiceBill psb){
		
		
		return  getService().saveBilledItem(psb);
		
	}


	public static PatientServiceBill getPatientServiceBill(	Integer patientServiceBillId) {
		return  getService().getPatientServiceBill(patientServiceBillId);
		
	}


	/**
	 * Gets list of Conosmmation by global bill
	 * @param globalBill
	 * @return List<Consommation>
	 */
	public static List<Consommation> getConsommationsByGlobalBill(
			GlobalBill globalBill) {
		
		return getService().getAllConsommationByGlobalBill(globalBill);
	}
	/**
	 * Gets List of consommations  matching with a given  beneficiary
	 * @param beneficiary
	 * @return List<Consommation>
	 */
	public static List<Consommation> getConsommationsByBeneficiary(
			Beneficiary beneficiary) {
		
		return getService().getConsommationsByBeneficiary(beneficiary);
	}
	
	
	
	
	
	

}
