package org.openmrs.module.mohbilling.model;

import java.util.Date;

import org.openmrs.User;

public class Recovery implements Comparable<Recovery>{

	private Integer recoveryId;
	private Insurance insuranceId;
	private Date startPeriod;
	private Date endPeriod;
	private float paidAmount;
	private Date payementDate;
	
	private User creator;
	private Date createdDate;
	private Boolean retired = false;
	private User retiredBy;
	private Date retiredDate;
	private String retireReason;
	
	public Integer getRecoveryId() {
		return recoveryId;
	}



	public void setRecoveryId(Integer recoveryId) {
		this.recoveryId = recoveryId;
	}

	

	public Insurance getInsuranceId() {
		return insuranceId;
	}



	public void setInsuranceId(Insurance insuranceId) {
		this.insuranceId = insuranceId;
	}



	public Date getStartPeriod() {
		return startPeriod;
	}



	public void setStartPeriod(Date startPeriod) {
		this.startPeriod = startPeriod;
	}



	public Date getEndPeriod() {
		return endPeriod;
	}



	public void setEndPeriod(Date endPeriod) {
		this.endPeriod = endPeriod;
	}



	public float getPaidAmount() {
		return paidAmount;
	}



	public void setPaidAmount(float paidAmount) {
		this.paidAmount = paidAmount;
	}



	public Date getPayementDate() {
		return payementDate;
	}



	public void setPayementDate(Date payementDate) {
		this.payementDate = payementDate;
	}



	@Override
	public int compareTo(Recovery arg0) {
		// TODO Auto-generated method stub
		return 0;
	}



	public User getCreator() {
		return creator;
	}



	public void setCreator(User creator) {
		this.creator = creator;
	}



	public Date getCreatedDate() {
		return createdDate;
	}



	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}



	public Boolean getRetired() {
		return retired;
	}



	public void setRetired(Boolean retired) {
		this.retired = retired;
	}



	public User getRetiredBy() {
		return retiredBy;
	}



	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}



	public Date getRetiredDate() {
		return retiredDate;
	}



	public void setRetiredDate(Date retiredDate) {
		this.retiredDate = retiredDate;
	}



	public String getRetireReason() {
		return retireReason;
	}



	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
	

}
