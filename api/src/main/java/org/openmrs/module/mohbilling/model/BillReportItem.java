package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * One product/service line for the CBHI Bill Report (facility columns excluded).
 */
public class BillReportItem {

	private Integer rowNumber;
	private Date admissionDate;
	private Date dischargeDate;
	private String wardType;
	private String voucherId;
	private String patientId;
	private String patientNames;
	private String socialRegistryLevel;
	private String patientNid;
	private String householdHeadNid;
	private String patientPhoneNumber;
	private Date patientDob;
	private String patientGender;
	private String providerName;
	private String providerQualification;
	private String providerPhoneNumber;
	private String providerRegistrationNumber;
	private String primaryWorkingDiagnosis;
	private String primaryConfirmedByLabImaging;
	private String icd11Code;
	private String productName;
	private String productCode;
	private String productType;
	private String productCategory;
	private String productUnit;
	private BigDecimal productCost;
	private BigDecimal coPayment;
	private BigDecimal rssbPayment;
	private BigDecimal totalInvoice;

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public Date getAdmissionDate() {
		return admissionDate;
	}

	public void setAdmissionDate(Date admissionDate) {
		this.admissionDate = admissionDate;
	}

	public Date getDischargeDate() {
		return dischargeDate;
	}

	public void setDischargeDate(Date dischargeDate) {
		this.dischargeDate = dischargeDate;
	}

	public String getWardType() {
		return wardType;
	}

	public void setWardType(String wardType) {
		this.wardType = wardType;
	}

	public String getVoucherId() {
		return voucherId;
	}

	public void setVoucherId(String voucherId) {
		this.voucherId = voucherId;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getPatientNames() {
		return patientNames;
	}

	public void setPatientNames(String patientNames) {
		this.patientNames = patientNames;
	}

	public String getSocialRegistryLevel() {
		return socialRegistryLevel;
	}

	public void setSocialRegistryLevel(String socialRegistryLevel) {
		this.socialRegistryLevel = socialRegistryLevel;
	}

	public String getPatientNid() {
		return patientNid;
	}

	public void setPatientNid(String patientNid) {
		this.patientNid = patientNid;
	}

	public String getHouseholdHeadNid() {
		return householdHeadNid;
	}

	public void setHouseholdHeadNid(String householdHeadNid) {
		this.householdHeadNid = householdHeadNid;
	}

	public String getPatientPhoneNumber() {
		return patientPhoneNumber;
	}

	public void setPatientPhoneNumber(String patientPhoneNumber) {
		this.patientPhoneNumber = patientPhoneNumber;
	}

	public Date getPatientDob() {
		return patientDob;
	}

	public void setPatientDob(Date patientDob) {
		this.patientDob = patientDob;
	}

	public String getPatientGender() {
		return patientGender;
	}

	public void setPatientGender(String patientGender) {
		this.patientGender = patientGender;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderQualification() {
		return providerQualification;
	}

	public void setProviderQualification(String providerQualification) {
		this.providerQualification = providerQualification;
	}

	public String getProviderPhoneNumber() {
		return providerPhoneNumber;
	}

	public void setProviderPhoneNumber(String providerPhoneNumber) {
		this.providerPhoneNumber = providerPhoneNumber;
	}

	public String getProviderRegistrationNumber() {
		return providerRegistrationNumber;
	}

	public void setProviderRegistrationNumber(String providerRegistrationNumber) {
		this.providerRegistrationNumber = providerRegistrationNumber;
	}

	public String getPrimaryWorkingDiagnosis() {
		return primaryWorkingDiagnosis;
	}

	public void setPrimaryWorkingDiagnosis(String primaryWorkingDiagnosis) {
		this.primaryWorkingDiagnosis = primaryWorkingDiagnosis;
	}

	public String getPrimaryConfirmedByLabImaging() {
		return primaryConfirmedByLabImaging;
	}

	public void setPrimaryConfirmedByLabImaging(String primaryConfirmedByLabImaging) {
		this.primaryConfirmedByLabImaging = primaryConfirmedByLabImaging;
	}

	public String getIcd11Code() {
		return icd11Code;
	}

	public void setIcd11Code(String icd11Code) {
		this.icd11Code = icd11Code;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public String getProductUnit() {
		return productUnit;
	}

	public void setProductUnit(String productUnit) {
		this.productUnit = productUnit;
	}

	public BigDecimal getProductCost() {
		return productCost;
	}

	public void setProductCost(BigDecimal productCost) {
		this.productCost = productCost;
	}

	public BigDecimal getCoPayment() {
		return coPayment;
	}

	public void setCoPayment(BigDecimal coPayment) {
		this.coPayment = coPayment;
	}

	public BigDecimal getRssbPayment() {
		return rssbPayment;
	}

	public void setRssbPayment(BigDecimal rssbPayment) {
		this.rssbPayment = rssbPayment;
	}

	public BigDecimal getTotalInvoice() {
		return totalInvoice;
	}

	public void setTotalInvoice(BigDecimal totalInvoice) {
		this.totalInvoice = totalInvoice;
	}
}
