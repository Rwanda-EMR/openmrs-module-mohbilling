/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author emr
 *
 */
public class ThirdPartyBillUtil {
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	public static ThirdPartyBill saveThirdPartyBill(ThirdPartyBill thirdBill) {
		if (thirdBill != null) {
			getService().saveThirdPartyBill(thirdBill);
			
			return thirdBill;
		}

		return null;
	}
	public static ThirdPartyBill createThirdPartyBill(InsurancePolicy ip,BigDecimal totalAmount){
		
		ThirdPartyBill thirdPartyBill = null;
		
		if(ip.getThirdParty() != null){
			 BigDecimal thirdAmount  = totalAmount.multiply(BigDecimal.valueOf((ip.getThirdParty().getRate())/100));
			
			 thirdPartyBill = new ThirdPartyBill();
			 thirdPartyBill.setAmount(thirdAmount);
			 thirdPartyBill.setCreatedDate(new Date());
			 thirdPartyBill.setCreator(Context.getAuthenticatedUser());
			 thirdPartyBill.setVoided(false);
			
			 thirdPartyBill = saveThirdPartyBill(thirdPartyBill);
			
		}
		return thirdPartyBill;
		
	}
	
	
}
