/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

import java.math.BigDecimal;
import java.util.Date;

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
	
	private String refundReason;
	
	private Boolean approved = false;

	private User approvedBy;

	private Date approvalDate;

	private Boolean declined = false;

	private String decliningNote;
	
	private Date declineDate;

	private Boolean voided = false;

	private User voidedBy;

	private Date voidedDate;

	private String voidReason;
	
	public Boolean isApproved() {
		return approved;
	}

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
	 * @return the refundReason
	 */
	public String getRefundReason() {
		return refundReason;
	}

	/**
	 * @param refundReason the refundReason to set
	 */
	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	/**
	 * @return the approved
	 */
	public Boolean getApproved() {
		return approved;
	}

	/**
	 * @param approved the approved to set
	 */
	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	/**
	 * @return the approvedBy
	 */
	public User getApprovedBy() {
		return approvedBy;
	}

	/**
	 * @param approvedBy the approvedBy to set
	 */
	public void setApprovedBy(User approvedBy) {
		this.approvedBy = approvedBy;
	}

	/**
	 * @return the approvalDate
	 */
	public Date getApprovalDate() {
		return approvalDate;
	}

	/**
	 * @param approvalDate the approvalDate to set
	 */
	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	/**
	 * @return the declined
	 */
	public Boolean getDeclined() {
		return declined;
	}

	/**
	 * @param declined the declined to set
	 */
	public void setDeclined(Boolean declined) {
		this.declined = declined;
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

	/**
	 * @return the declineDate
	 */
	public Date getDeclineDate() {
		return declineDate;
	}

	/**
	 * @param declineDate the declineDate to set
	 */
	public void setDeclineDate(Date declineDate) {
		this.declineDate = declineDate;
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

	


