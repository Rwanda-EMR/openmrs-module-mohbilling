/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Transaction;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.Date;
import java.util.List;

/**
 * @author rbcemr
 *
 */
public class DepositUtil {
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	public static List<Transaction> getTransactions(Date startDate, Date endDate,
			User collector, String type){
		return getService().getTransactions(startDate, endDate, collector, type);
	}
}
