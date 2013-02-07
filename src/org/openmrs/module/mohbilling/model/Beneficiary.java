/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.util.Date;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class Beneficiary implements Comparable<Beneficiary> {
	private Integer beneficiaryId;
	private Patient patient;
	private InsurancePolicy insurancePolicy;
	private String policyIdNumber;
	private User creator;
	private Date createdDate;
	private Boolean retired = false;
	private User retiredBy;
	private Date retiredDate;
	private String retireReason;

	/**
	 * @return the beneficiaryId
	 */
	public Integer getBeneficiaryId() {
		return beneficiaryId;
	}

	/**
	 * @param beneficiaryId
	 *            the beneficiaryId to set
	 */
	public void setBeneficiaryId(Integer beneficiaryId) {
		this.beneficiaryId = beneficiaryId;
	}

	/**
	 * @param patient
	 *            the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param insurancePolicy
	 *            the insurancePolicy to set
	 */
	public void setInsurancePolicy(InsurancePolicy insurancePolicy) {
		this.insurancePolicy = insurancePolicy;
	}

	/**
	 * @return the insurancePolicy
	 */
	public InsurancePolicy getInsurancePolicy() {
		return insurancePolicy;
	}

	/**
	 * @return the policyIdNumber
	 */
	public String getPolicyIdNumber() {
		return policyIdNumber;
	}

	/**
	 * @param policyIdNumber
	 *            the policyIdNumber to set
	 */
	public void setPolicyIdNumber(String policyIdNumber) {
		this.policyIdNumber = policyIdNumber;
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
	public void setRetired(boolean retired) {
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
	 * @param retireReason
	 *            the retireReason to set
	 */
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
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
		if (obj instanceof Beneficiary == false)
			return false;
		Beneficiary other = (Beneficiary) obj;
		if (other.getBeneficiaryId() != null
				&& other.getBeneficiaryId().equals(this.getBeneficiaryId())
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
		if (this.beneficiaryId == null)
			return super.hashCode();
		return this.beneficiaryId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Beneficiary Id : " + this.beneficiaryId
				+ "\n - Beneficiary names : " + this.patient.getFamilyName()
				+ " " + this.patient.getGivenName()
				+ "\n - Beneficiary Card No : " + this.policyIdNumber
				+ "\n - Insurance policy No : "
				+ this.insurancePolicy.getInsuranceCardNo() + "\n - Creator : "
				+ this.creator.getName();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Beneficiary other) {
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
