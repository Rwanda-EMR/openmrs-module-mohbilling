package org.openmrs.module.mohbilling.integration.insurance;

import java.util.List;

public class RhipVoucherRequest {
	private String insuranceType;
	private String facilityFosaId;
	private String patientIdentifier;
	private List<RhipVoucherProcedure> procedures;
	private String userAccountCode;
	private String processedBy;
	private String notes;
	private String practitionerLicenseNumber;
	private String patientType;
	private String healthCareStayType;
	private String admissionDate;
	private String dischargeDate;
	private Boolean treatmentForNewBorn;
	private String patientPhoneNumber;

	public String getInsuranceType() {
		return insuranceType;
	}

	public void setInsuranceType(String insuranceType) {
		this.insuranceType = insuranceType;
	}

	public String getFacilityFosaId() {
		return facilityFosaId;
	}

	public void setFacilityFosaId(String facilityFosaId) {
		this.facilityFosaId = facilityFosaId;
	}

	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public List<RhipVoucherProcedure> getProcedures() {
		return procedures;
	}

	public void setProcedures(List<RhipVoucherProcedure> procedures) {
		this.procedures = procedures;
	}

	public String getUserAccountCode() {
		return userAccountCode;
	}

	public void setUserAccountCode(String userAccountCode) {
		this.userAccountCode = userAccountCode;
	}

	public String getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(String processedBy) {
		this.processedBy = processedBy;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getPractitionerLicenseNumber() {
		return practitionerLicenseNumber;
	}

	public void setPractitionerLicenseNumber(String practitionerLicenseNumber) {
		this.practitionerLicenseNumber = practitionerLicenseNumber;
	}

	public String getPatientType() {
		return patientType;
	}

	public void setPatientType(String patientType) {
		this.patientType = patientType;
	}

	public String getHealthCareStayType() {
		return healthCareStayType;
	}

	public void setHealthCareStayType(String healthCareStayType) {
		this.healthCareStayType = healthCareStayType;
	}

	public String getAdmissionDate() {
		return admissionDate;
	}

	public void setAdmissionDate(String admissionDate) {
		this.admissionDate = admissionDate;
	}

	public String getDischargeDate() {
		return dischargeDate;
	}

	public void setDischargeDate(String dischargeDate) {
		this.dischargeDate = dischargeDate;
	}

	public Boolean getTreatmentForNewBorn() {
		return treatmentForNewBorn;
	}

	public void setTreatmentForNewBorn(Boolean treatmentForNewBorn) {
		this.treatmentForNewBorn = treatmentForNewBorn;
	}

	public String getPatientPhoneNumber() {
		return patientPhoneNumber;
	}

	public void setPatientPhoneNumber(String patientPhoneNumber) {
		this.patientPhoneNumber = patientPhoneNumber;
	}
}
