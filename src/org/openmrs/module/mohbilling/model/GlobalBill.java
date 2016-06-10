/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.util.Date;
import java.util.List;

import org.openmrs.User;

/**
 * @author emr
 *
 */
public class GlobalBill {
	
	private Integer globalBillId;
	
	private Admission admission;
	
	private List<PatientBill> patientBills;
	
    private Date createdDate;
	
	private Boolean voided = false;
	
	private User voidedBy;
	
	private Date voidedDate;
	
	private String voidReason;

	/**
	 * @return the globalBillId
	 */
	public Integer getGlobalBillId() {
		return globalBillId;
	}

	/**
	 * @param globalBillId the globalBillId to set
	 */
	public void setGlobalBillId(Integer globalBillId) {
		this.globalBillId = globalBillId;
	}

	/**
	 * @return the admission
	 */
	public Admission getAdmission() {
		return admission;
	}

	/**
	 * @param admission the admission to set
	 */
	public void setAdmission(Admission admission) {
		this.admission = admission;
	}

	/**
	 * @return the patientBills
	 */
	public List<PatientBill> getPatientBills() {
		return patientBills;
	}

	/**
	 * @param patientBills the patientBills to set
	 */
	public void setPatientBills(List<PatientBill> patientBills) {
		this.patientBills = patientBills;
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
