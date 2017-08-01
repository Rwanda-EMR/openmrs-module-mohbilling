/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.Patient;
import org.openmrs.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author rbcemr
 *
 */
public class PatientAccount {
	private Integer patientAccountId;
	private Patient patient;
	private BigDecimal balance = new BigDecimal(0);
	private Set<Transaction> transactions;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	
	/**
	 * @return the patientAccountId
	 */
	public Integer getPatientAccountId() {
		return patientAccountId;
	}
	/**
	 * @param patientAccountId the patientAccountId to set
	 */
	public void setPatientAccountId(Integer patientAccountId) {
		this.patientAccountId = patientAccountId;
	}
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	/**
	 * @return the balance
	 */
	public BigDecimal getBalance() {
		return balance;
	}
	/**
	 * @param balance the balance to set
	 */
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
		// balance.add(depositAmount);
	}
	/**
	 * @return the transactions
	 */
	public Set<Transaction> getTransactions() {
		return transactions;
	}
	/**
	 * @param transactions the transactions to set
	 */
	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
	}
	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	/**
	 * @return the voided
	 */
	public Boolean getVoided() {
		return voided;
	}
	/**
	 * @param voided the voided to set
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	/**
	 * @return the voidedBy
	 */
	public User getVoidedBy() {
		return voidedBy;
	}
	/**
	 * @param voidedBy the voidedBy to set
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	/**
	 * @return the voidedDate
	 */
	public Date getVoidedDate() {
		return voidedDate;
	}
	/**
	 * @param voidedDate the voidedDate to set
	 */
	public void setVoidedDate(Date voidedDate) {
		this.voidedDate = voidedDate;
	}
	/**
	 * @return the voidReason
	 */
	public String getVoidReason() {
		return voidReason;
	}
	/**
	 * @param voidReason the voidReason to set
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	 public void addTransaction(Transaction transaction){
		 if (transactions == null)
			 transactions = new TreeSet<Transaction>();
		 if (transaction != null) {
			transaction.setPatientAccount(this);
			transactions.add(transaction);
		 }
	 }
	  public BigDecimal deposit (BigDecimal depositAmount) {
	        balance.add(depositAmount);
	        return balance;
	    }

	    public BigDecimal withdraw(BigDecimal withdrawAmmount) {
	        balance.subtract(withdrawAmmount);
	        return balance;
	    }

	
}
