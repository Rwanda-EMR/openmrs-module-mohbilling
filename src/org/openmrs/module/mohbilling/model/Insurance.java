/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class Insurance implements Comparable<Insurance> {
	private Integer insuranceId;
	private String name;
	private String address;
	private String phone;
	private Concept concept;
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	private Set<InsuranceRate> rates;
	private Set<ServiceCategory> categories;
	private String category;

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return
	 */
	public Set<InsuranceRate> getRates() {
		return rates;
	}

	/**
	 * @param rates
	 */
	public void setRates(Set<InsuranceRate> rates) {
		this.rates = rates;
	}

	/**
	 * @return the insuranceId
	 */
	public Integer getInsuranceId() {
		return insuranceId;
	}

	/**
	 * @param insuranceId
	 */
	public void setInsuranceId(Integer insuranceId) {
		this.insuranceId = insuranceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
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

	public Boolean isVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public User getVoidedBy() {
		return voidedBy;
	}

	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public Date getVoidedDate() {
		return voidedDate;
	}

	public void setVoidedDate(Date voidedDate) {
		this.voidedDate = voidedDate;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	public Set<ServiceCategory> getCategories() {
		return categories;
	}

	/**
	 * @param categories
	 */
	public void setCategories(Set<ServiceCategory> categories) {
		this.categories = categories;
	}

	public boolean addServiceCategory(ServiceCategory category) {
		if (category != null) {
			category.setInsurance(this);
			if (categories == null)
				categories = new TreeSet<ServiceCategory>();
			if (!OpenmrsUtil.collectionContains(categories, category))
				return categories.add(category);
		}
		return false;
	}

	public boolean removeServiceCategory(ServiceCategory category) {
		if (categories != null)
			return this.categories.remove(category);
		return false;
	}

	/**
	 * @param rate
	 * @return
	 */
	public boolean removeInsuranceRate(InsuranceRate rate) {
		if (this.rates != null)
			return this.rates.remove(rate);
		return false;
	}

	/**
	 * @param rate
	 * @return
	 */
	public boolean addInsuranceRate(InsuranceRate rate) {
		if (rate != null) {
			rate.setInsurance(this);
			if (rates == null)
				rates = new TreeSet<InsuranceRate>();
			if (!OpenmrsUtil.collectionContains(rates, rate))
				return rates.add(rate);
		}
		return false;
	}

	/**
	 * @param date
	 *            , the date to be matched
	 * @return insuranceRate when it matches the date
	 */
	public InsuranceRate getRateOnDate(Date date) {

		for (InsuranceRate insuranceRate : this.getRates()) {
			if (insuranceRate.getRetiredDate() != null) {
				if (insuranceRate.getRetiredDate().compareTo(date) > 0) {

					return insuranceRate;
				}
			} else {
				return insuranceRate;
			}
		}
		return null;
	}

	/**
	 * @param date
	 *            , the date to be matched
	 * @return today or current insuranceRate
	 */
	public InsuranceRate getCurrentRate() {
		return getRateOnDate(new Date());
	}

	/**
	 * @return Number of service category in this insurance
	 */
	public int getNumberOfServiceCategory(Boolean isRetired) {
		int count = 0;
		if (getCategories() != null) {
			for (ServiceCategory sc : getCategories())
				if (sc.isRetired() == isRetired)
					count += 1;
			return count;
		} else
			return 0;
	}

	/**
	 * @return Number of service category in this insurance
	 */
	public int getNumberOfCategories() {
		return getNumberOfServiceCategory(false);
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
		if (obj instanceof Insurance == false)
			return false;
		Insurance insurance = (Insurance) obj;
		if (insurance.getInsuranceId() != null
				&& insurance.getInsuranceId().equals(this.insuranceId)) {
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
		if (this.getInsuranceId() == null)
			return super.hashCode();
		return this.getInsuranceId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Insurance Id : " + this.insuranceId
				+ "\n - Insurance Name : " + this.name + "\n - Address : "
				+ this.address + "\n - Insurance Concept : "
				+ this.getConcept().getDisplayString() + "\n - Creator : "
				+ this.creator.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Insurance other) {
		int ret = OpenmrsUtil.compareWithNullAsGreatest(this.getName(), other
				.getName());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}
}
