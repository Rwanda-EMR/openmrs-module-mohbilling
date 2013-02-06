/**
 * 
 */
package org.openmrs.module.mohappointment.model;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.User;

/**
 * @author Kamonyo
 * 
 */
public class Services {

	private Integer serviceId;
	
	private String name;
	private String description;
	private Concept concept;
	
	private User creator;
	private Date createdDate;
	private Boolean retired;
	private User retiredBy;
	private String retireReason;
	private Date retireDate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IService#getServiceId()
	 */
	public Integer getServiceId() {
		return serviceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IService#setServiceId(java.lang
	 * .Integer)
	 */
	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IService#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IService#setName(java.lang.String
	 * )
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IService#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IService#setDescription(java.
	 * lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
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
	public Boolean getRetired() {
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
	 * @return the retireDate
	 */
	public Date getRetireDate() {
		return retireDate;
	}

	/**
	 * @param retireDate
	 *            the retireDate to set
	 */
	public void setRetireDate(Date retireDate) {
		this.retireDate = retireDate;
	}

	// ********** Java overriden methods **********

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		Services service = (Services) obj;
		if (service != null)
			if (service.getServiceId() == this.serviceId
					&& service.hashCode() == this.hashCode()) {
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
		return this.serviceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Services ID: " + this.serviceId + "\n - Services Name: "
				+ this.name + "\n - Description: " + this.description
				+ "\n - Concept: " + this.concept.getDisplayString()
				+ "\n - Creator: " + this.creator.getName();
	}

}
