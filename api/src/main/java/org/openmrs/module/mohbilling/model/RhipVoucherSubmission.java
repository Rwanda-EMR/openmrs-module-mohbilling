package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.util.Date;

public class RhipVoucherSubmission {

	public static final String STATUS_NOT_SENT = "NOT_SENT";
	public static final String STATUS_PROCESSING = "PROCESSING";
	public static final String STATUS_SENT = "SENT";
	public static final String STATUS_FAILED = "FAILED";

	private Integer rhipVoucherSubmissionId;
	private GlobalBill globalBill;
	private String status;
	private String requestPayload;
	private String responsePayload;
	private Integer responseCode;
	private String voucherCode;
	private String voucherReferenceNumber;
	private String errorMessage;
	private User submittedBy;
	private Date dateSubmitted;
	private Integer attemptNumber;
	private String uuid;

	public Integer getRhipVoucherSubmissionId() {
		return rhipVoucherSubmissionId;
	}

	public void setRhipVoucherSubmissionId(Integer rhipVoucherSubmissionId) {
		this.rhipVoucherSubmissionId = rhipVoucherSubmissionId;
	}

	public GlobalBill getGlobalBill() {
		return globalBill;
	}

	public void setGlobalBill(GlobalBill globalBill) {
		this.globalBill = globalBill;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRequestPayload() {
		return requestPayload;
	}

	public void setRequestPayload(String requestPayload) {
		this.requestPayload = requestPayload;
	}

	public String getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public User getSubmittedBy() {
		return submittedBy;
	}

	public void setSubmittedBy(User submittedBy) {
		this.submittedBy = submittedBy;
	}

	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public Integer getAttemptNumber() {
		return attemptNumber;
	}

	public void setAttemptNumber(Integer attemptNumber) {
		this.attemptNumber = attemptNumber;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
