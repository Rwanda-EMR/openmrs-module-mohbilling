package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.util.Date;

public class RhipPractitionerType {

	private Integer rhipPractitionerTypeId;
	private String rhipId;
	private String name;
	private String type;
	private String categoryRhipId;
	private String categoryName;
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean voided = Boolean.FALSE;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	private String uuid;

	public Integer getRhipPractitionerTypeId() {
		return rhipPractitionerTypeId;
	}

	public void setRhipPractitionerTypeId(Integer rhipPractitionerTypeId) {
		this.rhipPractitionerTypeId = rhipPractitionerTypeId;
	}

	public String getRhipId() {
		return rhipId;
	}

	public void setRhipId(String rhipId) {
		this.rhipId = rhipId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCategoryRhipId() {
		return categoryRhipId;
	}

	public void setCategoryRhipId(String categoryRhipId) {
		this.categoryRhipId = categoryRhipId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public Date getDateChanged() {
		return dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public Boolean getVoided() {
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

	public Date getDateVoided() {
		return dateVoided;
	}

	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}

