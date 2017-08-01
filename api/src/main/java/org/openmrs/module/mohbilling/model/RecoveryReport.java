package org.openmrs.module.mohbilling.model;

public class RecoveryReport {

	protected String insuranceName;
	protected Object insuranceDueAmount;
	protected float paidAmount;
	protected float remainingAmount;
	protected String startDateStr;
	protected String endDateStr;
	protected String paidDate;
	protected int month;

	/*
	 * protected float totalAmountTobePaid; protected float totalPaidAmount;
	 * protected float totalRemainingAmount;
	 */

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getPaidDate() {
		return paidDate;
	}

	public void setPaidDate(String paidDate) {
		this.paidDate = paidDate;
	}

	/*
	 * public float getTotalAmountTobePaid() { return totalAmountTobePaid; }
	 * 
	 * public void setTotalAmountTobePaid(float totalAmountTobePaid) {
	 * this.totalAmountTobePaid = totalAmountTobePaid; }
	 * 
	 * public float getTotalPaidAmount() { return totalPaidAmount; }
	 * 
	 * public void setTotalPaidAmount(float totalPaidAmount) {
	 * this.totalPaidAmount = totalPaidAmount; }
	 * 
	 * public float getTotalRemainingAmount() { return totalRemainingAmount; }
	 * 
	 * public void setTotalRemainingAmount(float totalRemainingAmount) {
	 * this.totalRemainingAmount = totalRemainingAmount; }
	 */

	public String getStartDateStr() {
		return startDateStr;
	}

	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}

	public String getEndDateStr() {
		return endDateStr;
	}

	public void setEndDateStr(String endDateStr) {
		this.endDateStr = endDateStr;
	}

	public String getInsuranceName() {
		return insuranceName;
	}

	public void setInsuranceName(String insuranceName) {
		this.insuranceName = insuranceName;
	}

	public Object getInsuranceDueAmount() {
		return insuranceDueAmount;
	}

	public void setInsuranceDueAmount(Object insuranceDueAmount) {
		this.insuranceDueAmount = insuranceDueAmount;
	}

	public float getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(float paidAmount) {
		this.paidAmount = paidAmount;
	}

	public float getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(float remainingAmount) {
		this.remainingAmount = remainingAmount;
	}
}
