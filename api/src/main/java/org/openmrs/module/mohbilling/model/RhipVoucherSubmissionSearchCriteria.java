package org.openmrs.module.mohbilling.model;

import java.util.Date;

public class RhipVoucherSubmissionSearchCriteria {

	/** Filter the period on the discharge (closing) date of the global bill: the default */
	public static final String DATE_TYPE_DISCHARGE = "DISCHARGE";

	/** Filter the period on the admission date, as the bill report does */
	public static final String DATE_TYPE_ADMISSION = "ADMISSION";

	private Date dischargeStartDate;
	private Date dischargeEndDate;
	private String status;
	private Integer insuranceId;
	private String dateType;
	private java.util.List<Integer> insuranceIds;
	private String query;
	private String sortBy;
	private String sortDirection;

	public Date getDischargeStartDate() {
		return dischargeStartDate;
	}

	public void setDischargeStartDate(Date dischargeStartDate) {
		this.dischargeStartDate = dischargeStartDate;
	}

	public Date getDischargeEndDate() {
		return dischargeEndDate;
	}

	public void setDischargeEndDate(Date dischargeEndDate) {
		this.dischargeEndDate = dischargeEndDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getInsuranceId() {
		return insuranceId;
	}

	public void setInsuranceId(Integer insuranceId) {
		this.insuranceId = insuranceId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

	/**
	 * @return which date the period is on: {@link #DATE_TYPE_ADMISSION}, or else the discharge date.  The period itself
	 * is still set with {@link #setDischargeStartDate(Date)} and {@link #setDischargeEndDate(Date)}.
	 */
	public String getDateType() {
		return dateType;
	}

	public void setDateType(String dateType) {
		this.dateType = dateType;
	}

	/**
	 * @return true if the period is on the admission date
	 */
	public boolean isAdmissionDateType() {
		return DATE_TYPE_ADMISSION.equalsIgnoreCase(dateType);
	}

	/**
	 * @return the insurances to show the bills of, for example every insurance of a category; used when no single
	 * insurance is set
	 */
	public java.util.List<Integer> getInsuranceIds() {
		return insuranceIds;
	}

	public void setInsuranceIds(java.util.List<Integer> insuranceIds) {
		this.insuranceIds = insuranceIds;
	}

	/**
	 * @return true if the bills are of several given insurances
	 */
	public boolean hasInsuranceIds() {
		return insuranceId == null && insuranceIds != null && !insuranceIds.isEmpty();
	}
}
