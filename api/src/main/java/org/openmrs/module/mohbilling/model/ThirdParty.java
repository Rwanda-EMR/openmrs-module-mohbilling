/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

import java.util.Date;

/**
 * @author Kamonyo
 */
public class ThirdParty implements Comparable<ThirdParty> {

	private Integer thirdPartyId;
	private String name;
	private Float rate;
	
	private User creator;
	private Date createdDate;
	private Boolean voided = false;
	private User voidedBy;
	private Date voidedDate;
	private String voidReason;
	
	/**
	 * @return the thirdPartyId
	 */
	public Integer getThirdPartyId() {
		return thirdPartyId;
	}



	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @return the rate
	 */
	public Float getRate() {
		return rate;
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
	 * @return the voided
	 */
	public Boolean isVoided() {
		return voided;
	}



	/**
	 * @return the voidedBy
	 */
	public User getVoidedBy() {
		return voidedBy;
	}



	/**
	 * @return the voidedDate
	 */
	public Date getVoidedDate() {
		return voidedDate;
	}



	/**
	 * @return the voidReason
	 */
	public String getVoidReason() {
		return voidReason;
	}



	/**
	 * @param thirdPartyId the thirdPartyId to set
	 */
	public void setThirdPartyId(Integer thirdPartyId) {
		this.thirdPartyId = thirdPartyId;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}



	/**
	 * @param rate the rate to set
	 */
	public void setRate(Float rate) {
		this.rate = rate;
	}



	/**
	 * @param creator the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}



	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}



	/**
	 * @param voided the voided to set
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}



	/**
	 * @param voidedBy the voidedBy to set
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}



	/**
	 * @param voidedDate the voidedDate to set
	 */
	public void setVoidedDate(Date voidedDate) {
		this.voidedDate = voidedDate;
	}



	/**
	 * @param voidReason the voidReason to set
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}


	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof ThirdParty == false)
			return false;
		ThirdParty other = (ThirdParty) obj;
		if (other.getThirdPartyId() != null
				&& other.getThirdPartyId().equals(this.getThirdPartyId())
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
		if (this.thirdPartyId == null)
			return super.hashCode();
		return this.thirdPartyId.hashCode();
	}

	/**
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(ThirdParty other) {
		
		int ret = other.isVoided().compareTo(this.isVoided());
		
		if (ret == 0)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
					other.getCreatedDate());
		if (ret == 0 && this.getCreatedDate() != null)
			ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
					.hashCode());
		return ret;

	}
}
