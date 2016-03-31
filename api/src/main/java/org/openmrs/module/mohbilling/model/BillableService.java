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
public class BillableService implements Comparable<BillableService> {
	private Integer serviceId;
	private Insurance insurance;
	private BigDecimal maximaToPay;
	private ServiceCategory serviceCategory;
	private FacilityServicePrice facilityServicePrice;
	private Date startDate;
	private Date endDate;
	private User creator;
	private Date createdDate;
	private Boolean retired = false;
	private Date retiredDate;
	private User retiredBy;
	private String retireReason;

	/**
	 * @return the serviceId
	 */
	public Integer getServiceId() {
		return serviceId;
	}

	/**
	 * @param serviceId
	 *            the serviceId to set
	 */
	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * @return the maximaToPay
	 */
	public BigDecimal getMaximaToPay() {
		return maximaToPay;
	}

	/**
	 * @param maximaToPay
	 *            the maximaToPay to set
	 */
	public void setMaximaToPay(BigDecimal maximaToPay) {
		this.maximaToPay = maximaToPay;
	}

	/**
	 * @return the serviceCategory
	 */
	public ServiceCategory getServiceCategory() {
		return serviceCategory;
	}

	/**
	 * @param serviceCategory
	 *            the serviceCategory to set
	 */
	public void setServiceCategory(ServiceCategory serviceCategory) {
		this.serviceCategory = serviceCategory;
	}

	/**
	 * @param facilityServicePrice
	 *            the facilityServicePrice to set
	 */
	public void setFacilityServicePrice(
			FacilityServicePrice facilityServicePrice) {
		this.facilityServicePrice = facilityServicePrice;
	}

	/**
	 * @return the facilityServicePrice
	 */
	public FacilityServicePrice getFacilityServicePrice() {
		return facilityServicePrice;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the retired
	 */
	public Boolean isRetired() {
		return retired;
	}

	/**
	 * @return the retiredDate
	 */
	public Date getRetiredDate() {
		return retiredDate;
	}

	/**
	 * @param retiredDate
	 *            the retiredDate to set
	 */
	public void setRetiredDate(Date retiredDate) {
		this.retiredDate = retiredDate;
	}

	/**
	 * @return the retiredBy
	 */
	public User getRetiredBy() {
		return retiredBy;
	}

	/**
	 * @param retiredBy
	 *            the retiredBy to set
	 */
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}

	/**
	 * @return the retireReason
	 */
	public String getRetireReason() {
		return retireReason;
	}

	/**
	 * @param retireReason
	 *            the retireReason to set
	 */
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}

	/**
	 * @return the insurance
	 */
	public Insurance getInsurance() {
		return insurance;
	}

	/**
	 * @param insurance
	 */
	public void setInsurance(Insurance insurance) {
		this.insurance = insurance;
	}

	/**
	 * @return true when the Billable Service is retired, otherwise false
	 */
	public Boolean getRetired() {
		return retired;
	}

	/**
	 * @param retired
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
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
		BillableService other = (BillableService) obj;
		if (other.getServiceId() != null
				&& other.getServiceId().equals(this.serviceId)
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
		if (this.getServiceId() == null)
			return super.hashCode();
		return this.getServiceId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BillableServiceId : " + this.serviceId;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BillableService other) {
		int ret = other.isRetired().compareTo(this.isRetired());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getStartDate(),
					other.getStartDate());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getEndDate(),
					other.getEndDate());
		if (ret == 0)
			OpenmrsUtil.compareWithNullAsGreatest(this.getMaximaToPay(), other
					.getMaximaToPay());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0)
			OpenmrsUtil.compareWithNullAsGreatest(this.getServiceId(), other
					.getServiceId());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}

}
