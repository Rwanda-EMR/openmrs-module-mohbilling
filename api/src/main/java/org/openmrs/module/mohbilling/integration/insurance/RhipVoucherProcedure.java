package org.openmrs.module.mohbilling.integration.insurance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class RhipVoucherProcedure {
	private String code;
	private BigDecimal quantity;
	private String prescribedAt;
	private BigDecimal price;
	private String posology;
	private String frequency;
	private Integer durationDays;
	private String dispensingDate;
	private String instructions;
	private Integer patientServiceBillId;
	private Boolean medicineLine;
	private String deliveryReferenceNumber;
	private Boolean dispensedInPharmacy;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public String getPrescribedAt() {
		return prescribedAt;
	}

	public void setPrescribedAt(String prescribedAt) {
		this.prescribedAt = prescribedAt;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getPosology() {
		return posology;
	}

	public void setPosology(String posology) {
		this.posology = posology;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public Integer getDurationDays() {
		return durationDays;
	}

	public void setDurationDays(Integer durationDays) {
		this.durationDays = durationDays;
	}

	public String getDispensingDate() {
		return dispensingDate;
	}

	public void setDispensingDate(String dispensingDate) {
		this.dispensingDate = dispensingDate;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	@JsonIgnore
	public Integer getPatientServiceBillId() {
		return patientServiceBillId;
	}

	public void setPatientServiceBillId(Integer patientServiceBillId) {
		this.patientServiceBillId = patientServiceBillId;
	}

	@JsonIgnore
	public Boolean getMedicineLine() {
		return medicineLine;
	}

	public void setMedicineLine(Boolean medicineLine) {
		this.medicineLine = medicineLine;
	}

	/**
	 * MMI only: the delivery (RHIP /practitioner/medicines/delivery) this medicine was taken from. When set, MMI bills
	 * the line in house at that delivery's price and ignores price.
	 */
	public String getDeliveryReferenceNumber() {
		return deliveryReferenceNumber;
	}

	public void setDeliveryReferenceNumber(String deliveryReferenceNumber) {
		this.deliveryReferenceNumber = deliveryReferenceNumber;
	}

	/**
	 * MMI drug and consumable lines only (null otherwise): true when the line is billed at the price of the delivery in
	 * deliveryReferenceNumber, false when it is billed at the declared price
	 */
	public Boolean getHasPurchaseOrder() {
		if (!Boolean.TRUE.equals(medicineLine)) {
			return null;
		}
		return deliveryReferenceNumber != null && !deliveryReferenceNumber.trim().isEmpty();
	}

	/**
	 * True when the line comes from an RPM dispense. For MMI inpatients, such medicines were also sent to
	 * /visit-dispense in real time.
	 */
	@JsonIgnore
	public Boolean getDispensedInPharmacy() {
		return dispensedInPharmacy;
	}

	public void setDispensedInPharmacy(Boolean dispensedInPharmacy) {
		this.dispensedInPharmacy = dispensedInPharmacy;
	}
}
