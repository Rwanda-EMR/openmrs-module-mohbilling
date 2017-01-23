/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.PaidServiceBillRefund;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
	
	public static List<PaymentRefund> getAllSubmittedPaymentRefunds(){
		return getService().getAllSubmittedPaymentRefunds();
	}
	
	public static PaymentRefund getRefundById(Integer id){
		return getService().getRefundById(id);
	}

	public static PaidServiceBillRefund getPaidServiceBillRefund(Integer paidSviceBillRefundid) {
		return  getService().getPaidServiceBillRefund(paidSviceBillRefundid);
	}

	public static Boolean areAllRefundItemsConfirmed(PaymentRefund refund){
		Boolean allChecked=false;
		Set<PaidServiceBillRefund> items = refund.getRefundedItems();
		for (PaidServiceBillRefund refundItem : items) {
			if(refundItem.getApproved()==true || refundItem.getDeclined()==true)
				allChecked=true;
			else
				allChecked=false;
		}
		return allChecked;
		
	}

	public static List<PaymentRefund> getRefundsBetweenDatesAndByCollector(Date startDate, Date endDate, User collector){
		return getService().getRefundsBetweenDatesAndByCollector(startDate, endDate, collector);
	}
	
}
