/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.util.Date;

/**
 * @author emr
 *
 */

public class Admission {
	
	private Integer admissionId;
	
	private InsurancePolicy insurancePolicy;
	
	private Boolean isAdmitted = false;
	
	private Date admissionDate;
	
	private Date dischargingDate;
	
	private User creator;
	
	private Date createdDate;
	
	private Boolean voided = false;
	
	private User voidedBy;
	
	private Date voidedDate;
	
	private String voidReason;

	/**
	 * @return the admissionId
	 */
	public Integer getAdmissionId() {
		return admissionId;
	}

	/**
	 * @param admissionId the admissionId to set
	 */
	public void setAdmissionId(Integer admissionId) {
		this.admissionId = admissionId;
	}

	/**
	 * @return the insurancePolicy
	 */
	public InsurancePolicy getInsurancePolicy() {
		return insurancePolicy;
	}

	/**
	 * @param insurancePolicy the insurancePolicy to set
	 */
	public void setInsurancePolicy(InsurancePolicy insurancePolicy) {
		this.insurancePolicy = insurancePolicy;
	}

	/**
	 * @return the isAdmitted
	 */
	public Boolean getIsAdmitted() {
		return isAdmitted;
	}

	/**
	 * @param isAdmitted the isAdmitted to set
	 */
	public void setIsAdmitted(Boolean isAdmitted) {
		this.isAdmitted = isAdmitted;
	}

	/**
	 * @return the admissionDate
	 */
	public Date getAdmissionDate() {
		return admissionDate;
	}

	/**
	 * @param admissionDate the admissionDate to set
	 */
	public void setAdmissionDate(Date admissionDate) {
		this.admissionDate = admissionDate;
	}

	/**
	 * @return the dischargingDate
	 */
	public Date getDischargingDate() {
		return dischargingDate;
	}

	/**
	 * @param dischargingDate the dischargingDate to set
	 */
	public void setDischargingDate(Date dischargingDate) {
		this.dischargingDate = dischargingDate;
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
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * @param voided the voided to set
	 */
	public void setVoided(Boolean voided) {
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
