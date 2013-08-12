/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class PatientBill {
	private Integer patientBillId;
	private String description;
	private Beneficiary beneficiary;
	private BigDecimal amount;
	private Boolean printed;
	private Boolean isPaid;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	private Set<PatientServiceBill> billItems;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the beneficiary
	 */
	public Beneficiary getBeneficiary() {
		return beneficiary;
	}

	/**
	 * @param beneficiary
	 *            the beneficiary to set
	 */
	public void setBeneficiary(Beneficiary beneficiary) {
		this.beneficiary = beneficiary;
	}

	/**
	 * @return the amount
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * @return the printed
	 */
	public Boolean isPrinted() {
		return printed;
	}

	/**
	 * @param printed
	 *            the printed to set
	 */
	public void setPrinted(Boolean printed) {
		this.printed = printed;
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
	 * @return
	 */
	public Set<PatientServiceBill> getBillItems() {
		return billItems;
	}

	/**
	 * @param billItems
	 */
	public void setBillItems(Set<PatientServiceBill> billItems) {
		this.billItems = billItems;
	}

	/**
	 * @param serviceBill
	 * @return
	 */
	public boolean addBillItem(PatientServiceBill serviceBill) {
		if (serviceBill != null) {
			serviceBill.setPatientBill(this);
			if (billItems == null)
				billItems = new TreeSet<PatientServiceBill>();
			if (!OpenmrsUtil.collectionContains(billItems, serviceBill)){
				this.amount = this.amount.add(serviceBill.getAmount());		
				System.out.println("***************** New Amoount after ADDING ******** : "+this.amount);		
				return billItems.add(serviceBill);
			}
		}
		return false;
	}

	/**
	 * @param psb
	 * @return
	 */
	public boolean removeBillItem(PatientServiceBill psb) {
		if (billItems != null){
			this.amount = this.amount.subtract(psb.getAmount());
			System.out.println("***************** New Amoount after SUBTRACTING ******** : "+this.amount);
			return billItems.remove(psb);
		}
		return false;
	}

	/**
	 * @return the list of payments
	 */
	public Set<BillPayment> getPayments() {
		return payments;
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
	 * @return true when the bill is printed, false otherwise
	 */
	public Boolean getPrinted() {
		return printed;
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
				+ "\n - Bill Description : " + this.description
				+ "\n - Benefeciary : "
				+ this.beneficiary.getPatient().getFamilyName() + " "
				+ this.beneficiary.getPatient().getGivenName()
				+ "\n - Amount : " + this.amount + "\n - is Paid : "
				+ this.isPaid + "\n - Is Printed : " + this.printed
				+ "\n - Creator : " + this.creator.getName();

	}

}
