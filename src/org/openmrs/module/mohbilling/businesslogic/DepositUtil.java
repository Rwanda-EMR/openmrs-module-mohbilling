/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.sql.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.Deposit;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author EMR@RBC
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
	/**
	 * creates patient deposit
	 * @param deposit
	 * @return
	 */
	public static Deposit createDeposit(Deposit deposit){
		
		getService().saveDeposit(deposit);
		return deposit;
		
	}
	/**
	 * gets a list of deposits according to provided parameters (patient and/or startdate and/or enddate and/or collector)
	 * @param patient
	 * @param startDate
	 * @param endDate
	 * @param collector
	 * @return
	 */
	public static List<Deposit> getDepositList(Patient patient,Date startDate,Date endDate,User collector){
		return getService().getDepositList(patient, startDate, endDate, collector);
	}
	/**
	 * gives diposit with a given id
	 * @param depositId
	 * @return Deposit that has id equals to depositId
	 */
	public static Deposit getDeposit(Integer depositId){
		return getService().getDeposit(depositId);
	}
}
