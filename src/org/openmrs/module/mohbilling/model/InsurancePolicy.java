/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * @author Kamonyo
 * 
 */
public class InsurancePolicy {

	private Integer insurancePolicyId;
	private Insurance insurance;
	private Patient owner;
	private String insuranceCardNo;
	private Date coverageStartDate;
	private Date expirationDate;
	private User creator;
	private Date createdDate;
	private Boolean retired = false;
	private User retiredBy;
	private Date retiredDate;
	private String retireReason;
	private Set<Beneficiary> beneficiaries;

	/**
	 * @return the insurancePolicyId
	 */
	public Integer getInsurancePolicyId() {
		return insurancePolicyId;
	}

	/**
	 * @param insurancePolicyId
	 *            the insurancePolicyId to set
	 */
	public void setInsurancePolicyId(Integer insurancePolicyId) {
		this.insurancePolicyId = insurancePolicyId;
	}

	/**
	 * @return the insurance
	 */
	public Insurance getInsurance() {
		return insurance;
	}

	/**
	 * @param insurance
	 *            the insurance to set
	 */
	public void setInsurance(Insurance insurance) {
		this.insurance = insurance;
	}

	/**
	 * @return the insuranceCardNo
	 */
	public String getInsuranceCardNo() {
		return insuranceCardNo;
	}

	/**
	 * @param insuranceCardNo
	 *            the insuranceCardNo to set
	 */
	public void setInsuranceCardNo(String insuranceCardNo) {
		this.insuranceCardNo = insuranceCardNo;
	}

	/**
	 * @return the owner
	 */
	public Patient getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(Patient owner) {
		this.owner = owner;
	}

	/**
	 * @return the coverageStartDate
	 */
	public Date getCoverageStartDate() {
		return coverageStartDate;
	}

	/**
	 * @param coverageStartDate
	 *            the coverageStartDate to set
	 */
	public void setCoverageStartDate(Date coverageStartDate) {
		this.coverageStartDate = coverageStartDate;
	}

	/**
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate
	 *            the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
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
	public boolean isRetired() {
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
	 * @return beneficiaries list
	 */
	public Set<Beneficiary> getBeneficiaries() {
		return beneficiaries;
	}

	/**
	 * @param beneficiaries
	 */
	public void setBeneficiaries(Set<Beneficiary> beneficiaries) {
		this.beneficiaries = beneficiaries;
	}

	/**
	 * Adds the beneficiary to the list of beneficiary of this InsurancePolicy
	 * 
	 * @param beneficiary
	 *            , the beneficiary to be added
	 * @return true when the beneficiary is added successfully, false otherwise
	 */
	public boolean addBeneficiary(Beneficiary b) {
		if (b != null) {
			b.setInsurancePolicy(this);
			if (beneficiaries == null)
				beneficiaries = new TreeSet<Beneficiary>();
			if (!OpenmrsUtil.collectionContains(beneficiaries, b))
				return beneficiaries.add(b);
		}
		return false;
	}

	/**
	 * Removes the beneficiary from the list of beneficiary of this
	 * InsurancePolicy
	 * 
	 * @param beneficiary
	 *            , the beneficiary to be removed
	 * @return true when the beneficiary is removed successfully, false
	 *         otherwise
	 */
	public boolean removeBeneficiary(Beneficiary beneficiary) {
		if (beneficiaries != null)
			return this.beneficiaries.remove(beneficiary);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		InsurancePolicy card = (InsurancePolicy) obj;
		if (card != null)
			if (card.getInsurancePolicyId().equals(this.insurancePolicyId)
					&& this.hashCode() == card.hashCode()) {
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

		// This returns the id in order to let testing be easier
		return this.insurancePolicyId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - Insurance Policy Id : " + this.insurancePolicyId
				+ "\n - Insurance Name : " + this.insurance.getName()
				+ "\n - Card No : " + this.insuranceCardNo
				+ "\n - Coverage Start Date : "
				+ this.coverageStartDate.toString() + "\n - Creator : "
				+ this.creator.getName();

	}

}
