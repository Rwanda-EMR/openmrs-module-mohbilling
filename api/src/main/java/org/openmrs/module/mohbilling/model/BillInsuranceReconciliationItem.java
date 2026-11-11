package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * One global bill that appears in Bill Report or Insurance Report but not both,
 * for a selected admission-date range.
 */
public class BillInsuranceReconciliationItem {

	public static final String APPEARS_IN_BILL_REPORT = "Bill Report";
	public static final String APPEARS_IN_INSURANCE_REPORT = "Insurance Report";

	private Integer rowNumber;
	/** Where the bill is found: {@link #APPEARS_IN_BILL_REPORT} or {@link #APPEARS_IN_INSURANCE_REPORT}. */
	private String appearsIn;
	private String reason;
	private Integer globalBillId;
	private String voucherId;
	private Date admissionDate;
	private Date closingDate;
	private String patientNames;
	private String patientId;
	private String cardNumber;
	private String insuranceName;
	private BigDecimal totalAmount;

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getAppearsIn() {
		return appearsIn;
	}

	public void setAppearsIn(String appearsIn) {
		this.appearsIn = appearsIn;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getGlobalBillId() {
		return globalBillId;
	}

	public void setGlobalBillId(Integer globalBillId) {
		this.globalBillId = globalBillId;
	}

	public String getVoucherId() {
		return voucherId;
	}

	public void setVoucherId(String voucherId) {
		this.voucherId = voucherId;
	}

	public Date getAdmissionDate() {
		return admissionDate;
	}

	public void setAdmissionDate(Date admissionDate) {
		this.admissionDate = admissionDate;
	}

	public Date getClosingDate() {
		return closingDate;
	}

	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	public String getPatientNames() {
		return patientNames;
	}

	public void setPatientNames(String patientNames) {
		this.patientNames = patientNames;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getInsuranceName() {
		return insuranceName;
	}

	public void setInsuranceName(String insuranceName) {
		this.insuranceName = insuranceName;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
}
