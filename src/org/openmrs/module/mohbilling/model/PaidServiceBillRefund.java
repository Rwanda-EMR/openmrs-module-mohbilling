/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.util.Date;

import org.openmrs.User;

/**
 * PaidServiceBills are put together in one refund
 * 
 * @author emr
 * 
 */
public class PaidServiceBillRefund {

	private Integer paidServiceBillRefundId;

	private PaymentRefund refund;

	private PaidServiceBill paidItem;
	
	private String decliningNote;

	private Integer refQuantity;

	private User creator;

	private Date createdDate;

	private Boolean voided = false;

	private User voidedBy;

	private Date voidedDate;

	private String voidReason;

	/**
	 * @return the paidServiceBillRefundId
	 */
	public Integer getPaidServiceBillRefundId() {
		return paidServiceBillRefundId;
	}

	/**
	 * @param paidServiceBillRefundId the paidServiceBillRefundId to set
	 */
	public void setPaidServiceBillRefundId(Integer paidServiceBillRefundId) {
		this.paidServiceBillRefundId = paidServiceBillRefundId;
	}

	/**
	 * @return the refund
	 */
	public PaymentRefund getRefund() {
		return refund;
	}

	/**
	 * @param refund the refund to set
	 */
	public void setRefund(PaymentRefund refund) {
		this.refund = refund;
	}

	/**
	 * @return the paidItem
	 */
	public PaidServiceBill getPaidItem() {
		return paidItem;
	}

	/**
	 * @param paidItem the paidItem to set
	 */
	public void setPaidItem(PaidServiceBill paidItem) {
		this.paidItem = paidItem;
	}

	/**
	 * @return the refQuantity
	 */
	public Integer getRefQuantity() {
		return refQuantity;
	}

	/**
	 * @param refQuantity the refQuantity to set
	 */
	public void setRefQuantity(Integer refQuantity) {
		this.refQuantity = refQuantity;
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

	/**
	 * @return the decliningNote
	 */
	public String getDecliningNote() {
		return decliningNote;
	}

	/**
	 * @param decliningNote the decliningNote to set
	 */
	public void setDecliningNote(String decliningNote) {
		this.decliningNote = decliningNote;
	}

	

}
