package org.openmrs.module.mohbilling.model;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

import java.math.BigDecimal;
import java.util.Date;

public class Recovery implements Comparable<Recovery> {

	private Integer recoveryId;
	private Insurance insuranceId;
	private ThirdParty thirdParty;
	private Date startPeriod;
	private Date endPeriod;
	private String status;
	private BigDecimal dueAmount;
	private Date submissionDate;
	private Date verificationDate;
	private BigDecimal paidAmount;
	private Date paymentDate;
	private String partlyPayReason;
	private String noPaymentReason;
	private String observation;

	private User creator;
	private Date createdDate;
	private User changedBy;
	private Boolean retired = false;
	private User retiredBy;
	private Date retiredDate;
	private String retireReason;

	/**
	 * @return the recoveryId
	 */
	public Integer getRecoveryId() {
		return recoveryId;
	}

	/**
	 * @return the insuranceId
	 */
	public Insurance getInsuranceId() {
		return insuranceId;
	}

	/**
	 * @return the startPeriod
	 */
	public Date getStartPeriod() {
		return startPeriod;
	}

	/**
	 * @return the endPeriod
	 */
	public Date getEndPeriod() {
		return endPeriod;
	}

	/**
	 * @return the paidAmount
	 */
	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	/**
	 * @return the paymentDate
	 */
	public Date getPaymentDate() {
		return paymentDate;
	}

	/**
	 * @return the thirdParty
	 */
	public ThirdParty getThirdParty() {
		return thirdParty;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the dueAmount
	 */
	public BigDecimal getDueAmount() {
		return dueAmount;
	}

	/**
	 * @return the submissionDate
	 */
	public Date getSubmissionDate() {
		return submissionDate;
	}

	/**
	 * @return the verificationDate
	 */
	public Date getVerificationDate() {
		return verificationDate;
	}

	/**
	 * @return the partlyPayReason
	 */
	public String getPartlyPayReason() {
		return partlyPayReason;
	}

	/**
	 * @return the noPaymentReason
	 */
	public String getNoPaymentReason() {
		return noPaymentReason;
	}

	/**
	 * @return the observation
	 */
	public String getObservation() {
		return observation;
	}

	/**
	 * @return the changedBy
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @return the retired
	 */
	public Boolean isRetired() {
		return retired;
	}

	/**
	 * @return the retiredBy
	 */
	public User getRetiredBy() {
		return retiredBy;
	}

	/**
	 * @return the retiredDate
	 */
	public Date getRetiredDate() {
		return retiredDate;
	}

	/**
	 * @return the retireReason
	 */
	public String getRetireReason() {
		return retireReason;
	}

	/**
	 * @param recoveryId
	 *            the recoveryId to set
	 */
	public void setRecoveryId(Integer recoveryId) {
		this.recoveryId = recoveryId;
	}

	/**
	 * @param insuranceId
	 *            the insuranceId to set
	 */
	public void setInsuranceId(Insurance insuranceId) {
		this.insuranceId = insuranceId;
	}

	/**
	 * @param startPeriod
	 *            the startPeriod to set
	 */
	public void setStartPeriod(Date startPeriod) {
		this.startPeriod = startPeriod;
	}

	/**
	 * @param endPeriod
	 *            the endPeriod to set
	 */
	public void setEndPeriod(Date endPeriod) {
		this.endPeriod = endPeriod;
	}

	/**
	 * @param paidAmount
	 *            the paidAmount to set
	 */
	public void setPaidAmount(BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
	}

	/**
	 * @param paymentDate
	 *            the paymentDate to set
	 */
	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	/**
	 * @param thirdParty the thirdParty to set
	 */
	public void setThirdParty(ThirdParty thirdParty) {
		this.thirdParty = thirdParty;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param dueAmount the dueAmount to set
	 */
	public void setDueAmount(BigDecimal dueAmount) {
		this.dueAmount = dueAmount;
	}

	/**
	 * @param submissionDate the submissionDate to set
	 */
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}

	/**
	 * @param verificationDate the verificationDate to set
	 */
	public void setVerificationDate(Date verificationDate) {
		this.verificationDate = verificationDate;
	}

	/**
	 * @param partlyPayReason the partlyPayReason to set
	 */
	public void setPartlyPayReason(String partlyPayReason) {
		this.partlyPayReason = partlyPayReason;
	}

	/**
	 * @param noPaymentReason the noPaymentReason to set
	 */
	public void setNoPaymentReason(String noPaymentReason) {
		this.noPaymentReason = noPaymentReason;
	}

	/**
	 * @param observation the observation to set
	 */
	public void setObservation(String observation) {
		this.observation = observation;
	}

	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @param retired
	 *            the retired to set
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * @param retiredBy
	 *            the retiredBy to set
	 */
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}

	/**
	 * @param retiredDate
	 *            the retiredDate to set
	 */
	public void setRetiredDate(Date retiredDate) {
		this.retiredDate = retiredDate;
	}

	/**
	 * @param retireReason
	 *            the retireReason to set
	 */
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof Recovery == false)
			return false;
		Recovery other = (Recovery) obj;
		if (other.getRecoveryId() != null
				&& other.getRecoveryId().equals(this.getRecoveryId())
				&& this.hashCode() == other.hashCode()) {
			return true;
		}

		return false;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.recoveryId == null)
			return super.hashCode();
		return this.recoveryId.hashCode();
	}

	/**
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Recovery other) {
		// TODO Auto-generated method stub

		int ret = other.isRetired().compareTo(this.isRetired());

		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(),
					other.hashCode());
		return ret;
	}

}
