/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.util.Date;
import java.util.Set;

import org.openmrs.User;

/**
 * @author emr
 *
 */
public class Department {
	
	private Integer departementId;
	private String name;
	private String description;
	private Set<HopService> hopServices;
	private User creator;
	private Date createdDate;
	private boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	/**
	 * @return the departementId
	 */
	public Integer getDepartementId() {
		return departementId;
	}
	/**
	 * @param departementId the departementId to set
	 */
	public void setDepartementId(Integer departementId) {
		this.departementId = departementId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the hopServices
	 */
	public Set<HopService> getHopServices() {
		return hopServices;
	}
	/**
	 * @param hopServices the hopServices to set
	 */
	public void setHopServices(Set<HopService> hopServices) {
		this.hopServices = hopServices;
	}
	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}
	/**
	 * @param creator the creator to set
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
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	/**
	 * @return the voided
	 */
	public boolean isVoided() {
		return voided;
	}
	/**
	 * @param voided the voided to set
	 */
	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	/**
	 * @return the voidedBy
	 */
	public User getVoidedBy() {
		return voidedBy;
	}
	/**
	 * @param voidedBy the voidedBy to set
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	/**
	 * @return the voidedDate
	 */
	public Date getVoidedDate() {
		return voidedDate;
	}
	/**
	 * @param voidedDate the voidedDate to set
	 */
	public void setVoidedDate(Date voidedDate) {
		this.voidedDate = voidedDate;
	}
	/**
	 * @return the voidReason
	 */
	public String getVoidReason() {
		return voidReason;
	}
	/**
	 * @param voidReason the voidReason to set
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
}
