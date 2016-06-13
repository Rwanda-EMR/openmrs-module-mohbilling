/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.User;

/**
 * @author EMR@RBC
 *
 */
public class Deposit {

	private Integer depositId;
	private Patient patient;
	private BigDecimal amount = new BigDecimal(0);
	private Date depositDate;
	private String depositReason;
	private Set<DipositWithdrawal> withdrawals;
	private User creator;
	private Date creationDate;
	private boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	/**
	 * @return the depositId
	 */
	public Integer getDepositId() {
		return depositId;
	}
	/**
	 * @param depositId the depositId to set
	 */
	public void setDepositId(Integer depositId) {
		this.depositId = depositId;
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
	 * @return the amount
	 */
	public BigDecimal getAmount() {
		return amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	/**
	 * @return the depositDate
	 */
	public Date getDepositDate() {
		return depositDate;
	}
	/**
	 * @param depositDate the depositDate to set
	 */
	public void setDepositDate(Date depositDate) {
		this.depositDate = depositDate;
	}
	/**
	 * @return the depositReason
	 */
	public String getDepositReason() {
		return depositReason;
	}
	/**
	 * @param depositReason the depositReason to set
	 */
	public void setDepositReason(String depositReason) {
		this.depositReason = depositReason;
	}
	/**
	 * @return the withdrawals
	 */
	public Set<DipositWithdrawal> getWithdrawals() {
		return withdrawals;
	}
	/**
	 * @param withdrawals the withdrawals to set
	 */
	public void setWithdrawals(Set<DipositWithdrawal> withdrawals) {
		this.withdrawals = withdrawals;
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
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}
	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	/**
	 * @return the voided
	 */
	public boolean isVoided() {
		return voided;
	}
	/**
	 * @param voided the voided to set
	 */
	public void setVoided(boolean voided) {
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
	
	
}

