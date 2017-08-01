/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author emr
 *
 */
public class InsuranceBill {
	
	private Integer insuranceBillId;
	
	private BigDecimal amount = new BigDecimal(0);
	
	private User creator;
	
	private Date createdDate;
	
	private boolean voided = false;
	
	private User voidedBy;
	
	private Date voidedDate;
	
	private String voidReason;

	/**
	 * @return the insuranceBillId
	 */
	public Integer getInsuranceBillId() {
		return insuranceBillId;
	}

	/**
	 * @param insuranceBillId the insuranceBillId to set
	 */
	public void setInsuranceBillId(Integer insuranceBillId) {
		this.insuranceBillId = insuranceBillId;
	}

	/**
	 * @return the amount
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
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
