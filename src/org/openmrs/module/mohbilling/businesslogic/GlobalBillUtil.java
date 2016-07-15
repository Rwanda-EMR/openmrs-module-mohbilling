/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author emr
 *
 */
public class GlobalBillUtil {	
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	/**
	 * Save global to DB
	 * @param globalBill
	 * @return 
	 */
	public static GlobalBill saveGlobalBill(GlobalBill globalBill){
		return getService().saveGlobalBill(globalBill);	
		
	}
	public static GlobalBill getGlobalBillByAdmission(Admission admission){
		
		return getService().getGlobalBillByAdmission(admission);
		
	}
	public static List<GlobalBill> getGlobalBillsByInsurancePolicy(InsurancePolicy ip){
	
		List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
	    List<Admission> admissions = AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);
	   
	   for (Admission admission : admissions) {
		   GlobalBill globalBill = GlobalBillUtil.getGlobalBillByAdmission(admission);
		   if(globalBill !=null){
			   globalBills.add(globalBill);
		   }
		  
	}
	return globalBills;
		
	}

	public static GlobalBill getGlobalBill(Integer globalBillId) {
		
	return 	getService().GetGlobalBill(globalBillId);
		
	}

	/**
	 * Get GlobalBill by bill identifier
	 * @param billIdentifier matching with globalBill
	 * @return GlobalBill
	 */
	public static GlobalBill getGlobalBillByBillIdentifier(String billIdentifier) {
		
		return getService().getGlobalBillByBillIdentifier(billIdentifier);
	}

}
