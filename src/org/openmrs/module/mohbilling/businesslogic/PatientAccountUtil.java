/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.Transaction;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author rbcemr
 *
 */
public class PatientAccountUtil {
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	/**
	 * creates patient account
	 * @param account
	 * @return
	 */
	public static PatientAccount createPatientAccount(PatientAccount account){
		
		getService().savePatientAccount(account);
		return account;
		
	}
	/**
	 * Checks whether there is a patient account associated to the given account
	 * @param accountId
	 * @return false if it doesn't exist
	 */
	public  static boolean isPatientAccountExists(Patient patient){
		
		PatientAccount account = getService().getPatientAccount(patient);
		if (account !=null) {
			return true;			
		}
		else return false;		
	}
	public static Transaction createTransaction(Transaction transaction){
		PatientAccount pAccount = new  PatientAccount();

		if (transaction != null) {
			pAccount = transaction.getPatientAccount();
			pAccount.addTransaction(transaction);
			//pAccount.setBalance(transaction.getAmount());
			getService().savePatientAccount(pAccount);
			return transaction;
		}

		return null;
	}
	 public static PatientAccount getPatientAccountByPatient(Patient patient){
		 return getService().getPatientAccount(patient);
	 }
	 public static PatientAccount getPatientAccountById(Integer id){
		 return getService().getPatientAccount(id);
	 }
	 public static Set<Transaction> getTransactions(PatientAccount acc,
			Date startDate, Date endDate, String reason){
				return getService().getTransactions(acc, startDate, endDate, reason);
		 
	 }
	 public static Transaction createTransaction(BigDecimal amount,Date date,Date createdDate,User creator,PatientAccount acc,String reason,User collector){
		 Transaction transaction = new Transaction();
			transaction.setAmount(amount);
			
			transaction.setTransactionDate(date);
			transaction.setCreatedDate(createdDate);
			transaction.setCreator(creator);
			transaction.setPatientAccount(acc);
			transaction.setReason(reason);
			transaction.setCollector(collector);	
			return PatientAccountUtil.createTransaction(transaction);
	 }

}
