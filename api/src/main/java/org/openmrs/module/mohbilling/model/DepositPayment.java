package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.util.Date;

public class DepositPayment  extends BillPayment {
	
	private Integer depositPaymentId;
	private Transaction transaction;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	
	public DepositPayment(){
		
	}
	public DepositPayment(BillPayment bPayment){
		super(bPayment);
		if(bPayment != null){
			this.depositPaymentId =bPayment.getBillPaymentId();
			
		}		
	}
	/**
	 * @return the depositPaymentId
	 */
	public Integer getDepositPaymentId() {
		return depositPaymentId;
	}
	/**
	 * @param depositPaymentId the depositPaymentId to set
	 */
	public void setDepositPaymentId(Integer depositPaymentId) {
		this.depositPaymentId = depositPaymentId;
	}
	
	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}
	/**
	 * @param transaction the transaction to set
	 */
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
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
	
	

}
