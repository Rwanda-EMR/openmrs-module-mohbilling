package org.openmrs.module.mohbilling.model;

import java.util.Date;

public class RhipVoucherSubmissionSearchCriteria {

	private Date dischargeStartDate;
	private Date dischargeEndDate;
	private String status;
	private Integer insuranceId;
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
}
