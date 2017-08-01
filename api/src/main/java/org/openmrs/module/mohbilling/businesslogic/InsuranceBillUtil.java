/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author emr
 *
 */
public class InsuranceBillUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	/**
	 * save the insurance bill
	 * @param ib the insuranceBill
	 * @return the insuranceBill that has been saved
	 */
	public static InsuranceBill saveInsuranceBill(InsuranceBill ib) {
		if (ib != null) {
			getService().saveInsuranceBill(ib);
			return ib;
		}

		return null;
	}
	public static InsuranceBill createInsuranceBill(Insurance insurance,BigDecimal totalAmount){
		 
		InsuranceRate validRate = insurance.getRateOnDate(new Date());
		 
	    InsuranceBill  ib = new InsuranceBill();
		  BigDecimal ibAmount =totalAmount.multiply(BigDecimal.valueOf((validRate.getRate())/100));
		  ib.setAmount(ibAmount);
		  ib.setCreatedDate(new Date());
		  ib.setCreator(Context.getAuthenticatedUser());
		  ib.setVoided(false);
		  
		  ib =saveInsuranceBill(ib);
		  
		  return ib;
		  
		
	}


}
