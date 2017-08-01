/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class InsuranceRate implements Comparable<InsuranceRate> {
	private Integer insuranceRateId;
	private Insurance insurance;
	private Float rate;
	private BigDecimal flatFee;
	private Date startDate;
	private Date endDate;
	private User creator;
	private Date createdDate;
	private Boolean retired = false;
	private User retiredBy;
	private Date retiredDate;
	private String retireReason;

	public Integer getInsuranceRateId() {
		return insuranceRateId;
	}

	public void setInsuranceRateId(Integer insuranceRateId) {
		this.insuranceRateId = insuranceRateId;
	}

	public Insurance getInsurance() {
		return insurance;
	}

	public void setInsurance(Insurance insurance) {
		this.insurance = insurance;
	}

	public Float getRate() {
		return rate;
	}

	public void setRate(Float rate) {
		this.rate = rate;
	}

	public BigDecimal getFlatFee() {
		return flatFee;
	}

	public void setFlatFee(BigDecimal flatFee) {
		this.flatFee = flatFee;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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

	public Boolean isRetired() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof InsuranceRate == false)
			return false;
		InsuranceRate other = (InsuranceRate) obj;
		if (other.getInsuranceRateId() != null
				&& other.getInsuranceRateId().equals(this.insuranceRateId)
				&& this.hashCode() == other.hashCode()) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.getInsuranceRateId() == null)
			return super.hashCode();
		return this.getInsuranceRateId().hashCode();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Insurance Rate Id : " + this.insuranceRateId
				+ "\n - Insurance Name : " + this.insurance.getName()
				+ "\n - Insurance Rate : " + this.rate + "\n - Flat Fee : "
				+ this.flatFee + "\n - Start Date : "
				+ this.startDate.toString() + "\n - Creator : "
				+ this.creator.getName();
	}

	@Override
	public int compareTo(InsuranceRate other) {
		int ret = other.isRetired().compareTo(this.isRetired());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getStartDate(),
					other.getStartDate());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getEndDate(),
					other.getEndDate());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getRate(), other
					.getRate());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getFlatFee(),
					other.getFlatFee());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}

}
