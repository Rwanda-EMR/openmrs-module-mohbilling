/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class BillPayment implements Comparable<BillPayment> {

	private Integer billPaymentId;
	private BigDecimal amountPaid;
	private Date dateReceived;
	private User collector;
	private PatientBill patientBill;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;

	/**
	 * @return the billPaymentId
	 */
	public Integer getBillPaymentId() {
		return billPaymentId;
	}

	/**
	 * @param billPaymentId
	 *            the billPaymentId to set
	 */
	public void setBillPaymentId(Integer billPaymentId) {
		this.billPaymentId = billPaymentId;
	}

	/**
	 * @return the amountPaid
	 */
	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	/**
	 * @param amountPaid
	 *            the amountPaid to set
	 */
	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	/**
	 * @return the dateReceived
	 */
	public Date getDateReceived() {
		return dateReceived;
	}

	/**
	 * @param dateReceived
	 *            the dateReceived to set
	 */
	public void setDateReceived(Date dateReceived) {
		this.dateReceived = dateReceived;
	}

	/**
	 * @return the collector
	 */
	public User getCollector() {
		return collector;
	}

	/**
	 * @param collector
	 *            the collector to set
	 */
	public void setCollector(User collector) {
		this.collector = collector;
	}

	/**
	 * @return the patientBill
	 */
	public PatientBill getPatientBill() {
		return patientBill;
	}

	/**
	 * @param patientBill
	 *            the patientBill to set
	 */
	public void setPatientBill(PatientBill patientBill) {
		this.patientBill = patientBill;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
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
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the voided
	 */
	public boolean isVoided() {
		return voided;
	}

	/**
	 * @param voided
	 *            the voided to set
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
	 * @param voidedBy
	 *            the voidedBy to set
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
	 * @param voidedDate
	 *            the voidedDate to set
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
	 * @param voidReason
	 *            the voidReason to set
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (obj instanceof BillPayment == false)
			return false;
		BillPayment bp = (BillPayment) obj;
		if (bp.getBillPaymentId() != null
				&& bp.getBillPaymentId().equals(this.billPaymentId)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.getBillPaymentId() == null)
			return super.hashCode();
		return this.getBillPaymentId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Payment Id : " + this.billPaymentId
				+ "\n - Amount Paid : " + this.amountPaid + "\n - Collector : "
				+ this.getCollector().getName() + "\n - Date received : "
				+ this.getDateReceived().toString() + "\n - Creator : "
				+ this.creator.getName();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BillPayment other) {
		int ret = OpenmrsUtil.compareWithNullAsGreatest(this.isVoided(), other
				.isVoided());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getDateReceived(),
					other.getDateReceived());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}

}
