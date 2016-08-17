/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * PaidServiceBills are put together in one refund
 * 
 * @author emr
 * 
 */
public class PaidServiceBillRefund  implements Comparable<PaidServiceBillRefund> {

	private Integer paidServiceBillRefundId;

	private PaymentRefund refund;

	private PaidServiceBill paidItem;

	private BigDecimal refQuantity;

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
	 * @return the refQuantity
	 */
	public BigDecimal getRefQuantity() {
		return refQuantity;
	}

	/**
	 * @param refQuantity the refQuantity to set
	 */
	public void setRefQuantity(BigDecimal refQuantity) {
		this.refQuantity = refQuantity;
	}

	@Override
	public int compareTo(PaidServiceBillRefund other) {
		int ret = OpenmrsUtil.compareWithNullAsGreatest(this.getVoided(), other.getVoided());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}
	

	
}

	


