package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.math.BigDecimal;
import java.util.Date;

public class RhipVoucherItemRecord {

	public static final String STATUS_SENT = "SENT";
	public static final String STATUS_REJECTED = "REJECTED";

	private Integer rhipVoucherItemRecordId;
	private GlobalBill globalBill;
	private PatientServiceBill patientServiceBill;
	private String productCode;
	private BigDecimal quantity;
	private BigDecimal price;
	private String status;
	private String rejectionReason;
	private String voucherCode;
	private String voucherReferenceNumber;
	private Date dateCreated;
	private User creator;
	private String uuid;

	public Integer getRhipVoucherItemRecordId() {
		return rhipVoucherItemRecordId;
	}

	public void setRhipVoucherItemRecordId(Integer rhipVoucherItemRecordId) {
		this.rhipVoucherItemRecordId = rhipVoucherItemRecordId;
	}

	public GlobalBill getGlobalBill() {
		return globalBill;
	}

	public void setGlobalBill(GlobalBill globalBill) {
		this.globalBill = globalBill;
	}

	public PatientServiceBill getPatientServiceBill() {
		return patientServiceBill;
	}

	public void setPatientServiceBill(PatientServiceBill patientServiceBill) {
		this.patientServiceBill = patientServiceBill;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getVoucherCode() {
		return voucherCode;
	}

	public void setVoucherCode(String voucherCode) {
		this.voucherCode = voucherCode;
	}

	public String getVoucherReferenceNumber() {
		return voucherReferenceNumber;
	}

	public void setVoucherReferenceNumber(String voucherReferenceNumber) {
		this.voucherReferenceNumber = voucherReferenceNumber;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
