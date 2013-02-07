/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class FacilityServicePrice implements Comparable<FacilityServicePrice> {

	private Integer facilityServicePriceId;
	private String name;
	private String shortName;
	private String description;
	private BigDecimal fullPrice;
	private Date startDate;
	private Date endDate;
	private Date createdDate;
	private Boolean retired = false;
	private Date retiredDate;
	private String retireReason;
	private Location location;
	private Concept concept;
	private User creator;
	private User retiredBy;
	private Set<BillableService> billableServices;

	/**
	 * @return list of BillableServices
	 */
	public Set<BillableService> getBillableServices() {
		return billableServices;
	}

	/**
	 * @param services
	 */
	public void setBillableServices(Set<BillableService> services) {
		this.billableServices = services;
	}

	/**
	 * @return the facilityServicePriceId
	 */
	public Integer getFacilityServicePriceId() {
		return facilityServicePriceId;
	}

	/**
	 * @param facilityServicePriceId
	 *            the facilityServicePriceId to set
	 */
	public void setFacilityServicePriceId(Integer facilityServicePriceId) {
		this.facilityServicePriceId = facilityServicePriceId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName
	 *            the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the fullPrice
	 */
	public BigDecimal getFullPrice() {
		return fullPrice;
	}

	/**
	 * @param fullPrice
	 *            the fullPrice to set
	 */
	public void setFullPrice(BigDecimal fullPrice) {
		this.fullPrice = fullPrice;
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
	 * @param retired
	 *            the retired to set
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
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
	 * @return the retireReason
	 */
	public String getRetireReason() {
		return retireReason;
	}

	/**
	 * @param retireReason2
	 *            the retireReason to set
	 */
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept
	 *            the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
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
	 * Adds a Billable Service to the List of Billable Services for this
	 * FacilityServicePrice object
	 * 
	 * @param bs
	 *            ,the BillableService to be added
	 * @return true when the Billable Service is added successfully, false
	 *         otherwise
	 */
	public boolean addBillableService(BillableService bs) {
		if (bs != null) {
			bs.setFacilityServicePrice(this);
			if (billableServices == null)
				billableServices = new TreeSet<BillableService>();
			if (!OpenmrsUtil.collectionContains(billableServices, bs))
				return billableServices.add(bs);
		}
		return false;
	}

	/**
	 * Removes a Billable Service from the List of Billable Services for this
	 * FacilityServicePrice object
	 * 
	 * @param bs
	 *            ,the BillableService to be removed
	 * @return true when the Billable Service is removed successfully, false
	 *         otherwise
	 */
	public boolean removeBillableService(BillableService bs) {
		if (billableServices != null)
			return this.billableServices.remove(bs);
		return false;
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
		if (obj instanceof FacilityServicePrice == false)
			return false;
		FacilityServicePrice other = (FacilityServicePrice) obj;
		if (other.getFacilityServicePriceId() != null
				&& other.getFacilityServicePriceId().equals(
						this.getFacilityServicePriceId())
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

		if (this.facilityServicePriceId == null)
			return super.hashCode();
		return this.facilityServicePriceId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "\n - Facility Service Id : " + this.facilityServicePriceId
				+ "\n - Name : " + this.getName() + "\n - Short name : "
				+ this.shortName + "\n - Description : " + this.description
				+ "\n - Full Price : " + this.fullPrice + "\n - Location : "
				+ this.location.getName() + "\n - Concept :"
				+ this.concept.getDisplayString() + "\n - Creator : "
				+ this.creator.getName();
	}

	/**
	 * @param other
	 *            ,the Facility to be compared to
	 * @return -1 when this is less than the passed value, +1 otherwise
	 */
	@Override
	public int compareTo(FacilityServicePrice other) {

		int ret = other.isRetired().compareTo(this.isRetired());
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;
	}

}
