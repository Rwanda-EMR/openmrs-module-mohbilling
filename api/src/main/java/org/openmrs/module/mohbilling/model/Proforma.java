package org.openmrs.module.mohbilling.model;

import org.openmrs.Patient;
import org.openmrs.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Proforma invoice: a priced estimate of services a patient may receive, issued to a
 * supporting entity before treatment. It is deliberately independent from GlobalBill /
 * Consommation so it never affects revenue, insurance, cashier or RHIP flows.
 */
public class Proforma {

	public static final String STATUS_ISSUED = "ISSUED";
	public static final String STATUS_CANCELLED = "CANCELLED";

	private Integer proformaId;
	private String identifier;
	private Patient patient;
	private Insurance insurance;
	private Department department;
	private Float insuranceRate;
	private BigDecimal flatFee;
	private BigDecimal totalAmount = BigDecimal.ZERO;
	private BigDecimal insuranceAmount = BigDecimal.ZERO;
	private BigDecimal patientAmount = BigDecimal.ZERO;
	private Date validUntil;
	private String addressee;
	private String notes;
	private String status = STATUS_ISSUED;
	private User creator;
	private Date createdDate;
	private User cancelledBy;
	private Date cancelledDate;
	private String cancelReason;
	private String uuid;
	private Set<ProformaItem> items = new LinkedHashSet<ProformaItem>();

	public void addItem(ProformaItem item) {
		item.setProforma(this);
		items.add(item);
	}

	public boolean isCancelled() {
		return STATUS_CANCELLED.equals(status);
	}

	public boolean isExpired() {
		return validUntil != null && validUntil.before(new Date());
	}

	public Integer getProformaId() {
		return proformaId;
	}

	public void setProformaId(Integer proformaId) {
		this.proformaId = proformaId;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Insurance getInsurance() {
		return insurance;
	}

	public void setInsurance(Insurance insurance) {
		this.insurance = insurance;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Float getInsuranceRate() {
		return insuranceRate;
	}

	public void setInsuranceRate(Float insuranceRate) {
		this.insuranceRate = insuranceRate;
	}

	public BigDecimal getFlatFee() {
		return flatFee;
	}

	public void setFlatFee(BigDecimal flatFee) {
		this.flatFee = flatFee;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getInsuranceAmount() {
		return insuranceAmount;
	}

	public void setInsuranceAmount(BigDecimal insuranceAmount) {
		this.insuranceAmount = insuranceAmount;
	}

	public BigDecimal getPatientAmount() {
		return patientAmount;
	}

	public void setPatientAmount(BigDecimal patientAmount) {
		this.patientAmount = patientAmount;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getAddressee() {
		return addressee;
	}

	public void setAddressee(String addressee) {
		this.addressee = addressee;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public User getCancelledBy() {
		return cancelledBy;
	}

	public void setCancelledBy(User cancelledBy) {
		this.cancelledBy = cancelledBy;
	}

	public Date getCancelledDate() {
		return cancelledDate;
	}

	public void setCancelledDate(Date cancelledDate) {
		this.cancelledDate = cancelledDate;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Set<ProformaItem> getItems() {
		return items;
	}

	public void setItems(Set<ProformaItem> items) {
		this.items = items;
	}
}
