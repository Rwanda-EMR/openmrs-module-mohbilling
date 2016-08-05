/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBillRefund;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author emr
 *
 */
public class PaymentRefundUtil {	

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	public static PaidServiceBillRefund createPaidServiceRefund(PaidServiceBillRefund psbRefund){
		
		PaymentRefund refund = new  PaymentRefund();

		if (psbRefund != null) {
			refund = psbRefund.getRefund();
			refund.addRefundedItem(psbRefund);
			getService().savePaymentRefund(refund);
			return psbRefund;
		}

		return null;
		
	}
	

}
