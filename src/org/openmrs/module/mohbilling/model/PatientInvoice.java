package org.openmrs.module.mohbilling.model;

import java.util.Map;

public class PatientInvoice {
	PatientBill patientBill;
	Map<String, Double> categoryAmount;
	/**
	 * @return the patientBill
	 */
	public PatientBill getPatientBill() {
		return patientBill;
	}
	/**
	 * @param patientBill the patientBill to set
	 */
	public void setPatientBill(PatientBill patientBill) {
		this.patientBill = patientBill;
	}
	
	/**
	 * @return the categoryAmount
	 */
	public Map<String, Double> getCategoryAmount() {
		return categoryAmount;
	}
	/**
	 * @param categoryAmount the categoryAmount to set
	 */
	public void setCategoryAmount(Map<String, Double> categoryAmount) {
		this.categoryAmount = categoryAmount;
	}
	

}
