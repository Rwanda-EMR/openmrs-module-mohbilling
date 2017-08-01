/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author EMR@RBC
 * 
 */
public class PatientBill {
	private Integer patientBillId;	
	private BigDecimal amount = new BigDecimal(0);
	private boolean isPaid;
	private String status;
	private User creator;
	private Date createdDate;
	private boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;	
	private Set<BillPayment> payments;

	/**
	 * @return the patientBillId
	 */
	public Integer getPatientBillId() {
		return patientBillId;
	}

	/**
	 * @param patientBillId
	 *            the patientBillId to set
	 */
	public void setPatientBillId(Integer patientBillId) {
		this.patientBillId = patientBillId;
	}


	/**
	 * @return the amount
	 */
	public BigDecimal getAmount() {
		return this.amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	

	/**
	 * @return the isPaid
	 */
	public Boolean getIsPaid() {
		return isPaid;
	}

	/**
	 * @param isPaid
	 *            the isPaid to set
	 */
	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
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
	public Boolean isVoided() {
		return voided;
	}

	/**
	 * @param voided
	 *            the voided to set
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

	/**
	 * @return the list of payments
	 */
	public Set<BillPayment> getPayments() {
		return payments;
	}

	/**
	 * Gets all payments made by the beneficiary
	 * 
	 * @return paidAmount
	 */
	public BigDecimal getAmountPaid() {

		if (payments != null) {
			BigDecimal paidAmount = new BigDecimal(0);
			
			for (BillPayment pay : payments)
				paidAmount = paidAmount.add(pay.getAmountPaid());

			return paidAmount;
		}
		
		return new BigDecimal(0);
	}

	/**
	 * @param payments
	 */
	public void setPayments(Set<BillPayment> payments) {
		this.payments = payments;
	}

	/**
	 * Adds the payment from payments list
	 * 
	 * @param payment
	 *            , the payment to be added
	 * @return true when payment is added successfully, false otherwise
	 */
	public boolean addBillPayment(BillPayment payment) {
		if (payment != null) {
			payment.setPatientBill(this);
			if (payments == null)
				payments = new TreeSet<BillPayment>();
			if (!OpenmrsUtil.collectionContains(payments, payment))
				return payments.add(payment);
		}
		return false;
	}

	/**
	 * Removes the payment from payments list
	 * 
	 * @param payment
	 *            , the payment to be removed
	 * @return true when payment is removed successfully, false otherwise
	 */
	public boolean removePayment(BillPayment payment) {
		if (payments != null)
			return payments.remove(payment);
		return false;
	}

	

	/**
	 * @return
	 */
	public Boolean getVoided() {
		return voided;
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
		if (obj instanceof PatientBill == false)
			return false;
		PatientBill pb = (PatientBill) obj;
		if (pb.getPatientBillId() != null
				&& pb.getPatientBillId().equals(this.getPatientBillId())) {
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
		if (this.getPatientBillId() == null)
			return super.hashCode();
		return this.getPatientBillId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Patient Bill Id : " + this.patientBillId		
						
				+ "\n - Amount : " + this.amount + "\n - is Paid : "
			
				+ "\n - Creator : " + this.creator.getPerson().getFamilyName()
				+ "\n - Creation date : " + this.createdDate;

	}

}
