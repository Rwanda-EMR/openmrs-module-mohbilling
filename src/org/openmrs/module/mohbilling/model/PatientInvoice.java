package org.openmrs.module.mohbilling.model;

import java.util.Map;

public class PatientInvoice {

	private PatientBill patientBill;
	private Map<String, Invoice> invoiceMap;
	private Double totalAmount;
	private Double insuranceCost;
	private Double patientCost;

	public PatientBill getPatientBill() {
		return patientBill;
	}

	public void setPatientBill(PatientBill patientBill) {
		this.patientBill = patientBill;
	}

	public Map<String, Invoice> getInvoiceMap() {
		return invoiceMap;
	}

	public void setInvoiceMap(Map<String, Invoice> invoiceMap) {
		this.invoiceMap = invoiceMap;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Double getInsuranceCost() {
		return insuranceCost;
	}

	public void setInsuranceCost(Double insuranceCost) {
		this.insuranceCost = insuranceCost;
	}

	public Double getPatientCost() {
		return patientCost;
	}

	public void setPatientCost(Double patientCost) {
		this.patientCost = patientCost;
	}

}
