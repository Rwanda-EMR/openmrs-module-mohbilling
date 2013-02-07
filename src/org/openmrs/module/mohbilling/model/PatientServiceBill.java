/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class PatientServiceBill implements Comparable<PatientServiceBill> {
	private Integer patientServiceBillId;
	private Date serviceDate;
	private BillableService service;
	private PatientBill patientBill;
	private BigDecimal unitPrice;
	private Integer quantity;
	// These following 2 attr. are alternative to have
	// another billableService Object and using
	// those fields should have special privileges
	private String serviceOther;
	private String serviceOtherDescription;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;

	/**
	 * @return the patientServiceBillId
	 */
	public Integer getPatientServiceBillId() {
		return patientServiceBillId;
	}

	/**
	 * @param patientServiceBillId
	 *            the patientServiceBillId to set
	 */
	public void setPatientServiceBillId(Integer patientServiceBillId) {
		this.patientServiceBillId = patientServiceBillId;
	}

	/**
	 * @return the serviceDate
	 */
	public Date getServiceDate() {
		return serviceDate;
	}

	/**
	 * @param serviceDate
	 *            the serviceDate to set
	 */
	public void setServiceDate(Date serviceDate) {
		this.serviceDate = serviceDate;
	}

	/**
	 * @return the service
	 */
	public BillableService getService() {
		return service;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public void setService(BillableService service) {
		this.service = service;
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
	 * @return the unitPrice
	 */
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	/**
	 * @param unitPrice
	 *            the unitPrice to set
	 */
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	/**
	 * @return the amount
	 */
	public BigDecimal getAmount() {
		// TODO: I have a feeling this is wrong, we need to get this right, to 2
		// decimal places? Maybe its right for RWF though.
		if (unitPrice == null)
			throw new RuntimeException(
					"Unable to calculate Bill Amount, because unitPrice is null");
		if (quantity == null)
			throw new RuntimeException(
					"Unable to calculate Bill Amount, because quantity is null");
		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);
		return unitPrice.multiply(BigDecimal.valueOf(quantity), mc);
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the serviceOther
	 */
	public String getServiceOther() {
		return serviceOther;
	}

	/**
	 * @param serviceOther
	 *            the serviceOther to set
	 */
	public void setServiceOther(String serviceOther) {
		this.serviceOther = serviceOther;
	}

	/**
	 * @return the serviceOtherDescription
	 */
	public String getServiceOtherDescription() {
		return serviceOtherDescription;
	}

	/**
	 * @param serviceOtherDescription
	 *            the serviceOtherDescription to set
	 */
	public void setServiceOtherDescription(String serviceOtherDescription) {
		this.serviceOtherDescription = serviceOtherDescription;
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
	 * Gets the Service Category for the Billable Service that is associated to
	 * this PatientServiceBill
	 * 
	 * @return service.serviceCategory
	 */
	public ServiceCategory getBillableServiceCategory() {
		return this.service.getServiceCategory();
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
		if (obj instanceof PatientServiceBill == false)
			return false;
		PatientServiceBill psb = (PatientServiceBill) obj;
		if (psb.getPatientServiceBillId() != null
				&& psb.getPatientServiceBillId().equals(
						this.patientServiceBillId)) {
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
		if (this.getPatientServiceBillId() == null)
			return super.hashCode();
		return this.getPatientServiceBillId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Patient Service Bill Id : " + this.patientServiceBillId
				+ "\n - Service Date : " + this.serviceDate.toString()
				+ "\n - Patient Bill : [#"
				+ this.patientBill.getPatientBillId() + "] "
				+ this.patientBill.getDescription() + "\n - Unit Price : "
				+ this.unitPrice + "\n - Quantity : " + this.quantity
				+ "\n - Amount : " + this.getAmount() + "\n - Service Other : "
				+ this.serviceOther + " - " + this.serviceOtherDescription
				+ "\n - Creator : " + this.creator.getName();

	}

	/**
	 * @param insurance
	 * @param date
	 * @param amount
	 * @return
	 */
	public BigDecimal applyInsuranceRate(Insurance insurance, Date date,
			BigDecimal amount) {

		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);
		BigDecimal rate = BigDecimal.valueOf(insurance.getRateOnDate(date)
				.getRate());

		return (insurance == null) ? amount : amount.multiply(rate, mc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PatientServiceBill other) {
		int ret = OpenmrsUtil.compareWithNullAsGreatest(this.isVoided(), other
				.isVoided());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getServiceDate(),
					other.getServiceDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}
}
