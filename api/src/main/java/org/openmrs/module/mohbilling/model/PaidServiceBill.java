/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author rbcemr
 *
 */
public class PaidServiceBill {
	
	private Integer paidServiceBillId;
	private PatientServiceBill billItem;
	private BillPayment billPayment;
	//The paid quantity
	private BigDecimal paidQty;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	/**
	 * @return the paidServiceBillId
	 */
	public Integer getPaidServiceBillId() {
		return paidServiceBillId;
	}
	/**
	 * @param paidServiceBillId the paidServiceBillId to set
	 */
	public void setPaidServiceBillId(Integer paidServiceBillId) {
		this.paidServiceBillId = paidServiceBillId;
	}
	/**
	 * @return the paidItem
	 */
	public PatientServiceBill getBillItem() {
		return billItem;
	}
	/**
	 * @param paidItem the paidItem to set
	 */
	public void setBillItem(PatientServiceBill billItem) {
		this.billItem = billItem;
	}
	/**
	 * @return the billPayment
	 */
	public BillPayment getBillPayment() {
		return billPayment;
	}
	/**
	 * @param billPayment the billPayment to set
	 */
	public void setBillPayment(BillPayment billPayment) {
		this.billPayment = billPayment;
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
	 * gets the paid quantity
	 * @return the paidQty
	 */
	public BigDecimal getPaidQty() {
		return paidQty;
	}
	/**
	 * Sets the paid Quantity
	 * @param paidQty the paidQty to set
	 */
	public void setPaidQty(BigDecimal paidQty) {
		this.paidQty = paidQty;
	}

}
