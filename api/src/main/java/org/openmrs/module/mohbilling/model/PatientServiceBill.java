/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

/**
 * @author EMR/RBC
 * 
 */
public class PatientServiceBill implements Comparable<PatientServiceBill> {
	
	private Integer patientServiceBillId;
	
	private Date serviceDate;
	private BillableService service;
	private HopService hopService;
	private BigDecimal unitPrice;
	private BigDecimal quantity;
	private BigDecimal paidQuantity;
	private Consommation consommation;
	private Boolean paid = Boolean.FALSE;	
	
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
	
	public PatientServiceBill(){
		
	}
	/**
	 * sets required parameters of PatientServiceBill
	 * @param bs
	 * @param serviceDate
	 * @param unitPrice
	 * @param quantity
	 * @param creator
	 * @param createdDate
	 */

	public PatientServiceBill(BillableService bs,HopService service,Date serviceDate,BigDecimal unitPrice,BigDecimal quantity,User creator,Date createdDate){
		this.service = bs;
		this.serviceDate=serviceDate;
		this.unitPrice=unitPrice;
		this.quantity=quantity;
		this.creator=creator;
		this.createdDate=createdDate;
		this.setHopService(service);

	}
	/**
	 * Creates a new instance of PatientServiceBill from existing
	 * Copies required parameters except newQuantity
	 * @param psbToCopy
	 * @param newQuantity
	 * @return
	 */
	public static PatientServiceBill newInstance(PatientServiceBill psbToCopy,BigDecimal newQuantity){

		PatientServiceBill newPsb=new PatientServiceBill(psbToCopy.getService(),psbToCopy.getHopService()   ,psbToCopy.getServiceDate(),psbToCopy.getUnitPrice(),newQuantity,psbToCopy.getCreator(),new Date());

		return newPsb;
	}

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
		return unitPrice.multiply(quantity, mc);
	}

	/**
	 * @return the quantity
	 */
	public BigDecimal getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the paidQuantity
	 */
	public BigDecimal getPaidQuantity() {
		return paidQuantity;
	}
	/**
	 * @param paidQuantity the paidQuantity to set
	 */
	public void setPaidQuantity(BigDecimal paidQuantity) {
		this.paidQuantity = paidQuantity;
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
	 * @return the consommation
	 */
	public Consommation getConsommation() {
		return consommation;
	}

	/**
	 * @param consommation the consommation to set
	 */
	public void setConsommation(Consommation consommation) {
		this.consommation = consommation;
	}


	/**
	 * @return the voided
	 */
	public Boolean getVoided() {
		return voided;
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
	
	
	public Boolean isPaid() {
		return paid;
	}
	/**
	 * @return the paid
	 */
	public Boolean getPaid() {
		return isPaid();
	}

	/**
	 * @param paid the paid to set
	 */
	public void setPaid(Boolean paid) {
		this.paid = paid;
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
	 * @return the hopService
	 */
	public HopService getHopService() {
		return hopService;
	}

	/**
	 * @param hopService the hopService to set
	 */
	public void setHopService(HopService hopService) {
		this.hopService = hopService;
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
