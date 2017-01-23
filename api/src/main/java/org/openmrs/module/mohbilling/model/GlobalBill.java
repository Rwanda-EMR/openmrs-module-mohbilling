/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author emr
 *
 */
public class GlobalBill {
	
	private Integer globalBillId;
	
	private Admission admission;
	
	private String billIdentifier;
	
	private BigDecimal globalAmount = new BigDecimal(0);
	
	private Set<Consommation> consommations;
	
    private Date createdDate;
    
    private User creator;
    
    private Date closingDate;
    
    private User closedBy;
    
    private Boolean closed = Boolean.FALSE;;
	
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
	 * @return the billIdentifier
	 */
	public String getBillIdentifier() {
		return billIdentifier;
	}

	/**
	 * @return the globalAmount
	 */
	public BigDecimal getGlobalAmount() {
		return globalAmount;
	}

	/**
	 * @param globalAmount the globalAmount to set
	 */
	public void setGlobalAmount(BigDecimal globalAmount) {
		this.globalAmount = globalAmount;
	}

	/**
	 * @param billIdentifier the billIdentifier to set
	 */
	public void setBillIdentifier(String billIdentifier) {
		this.billIdentifier = billIdentifier;
	}

	/**
	 * @return the consommations
	 */
	public Set<Consommation> getConsommations() {
		return consommations;
	}

	/**
	 * @param consommations the consommations to set
	 */
	public void setConsommations(Set<Consommation> consommations) {
		this.consommations = consommations;
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
	 * @return the closingDate
	 */
	public Date getClosingDate() {
		return closingDate;
	}

	/**
	 * @param closingDate the closingDate to set
	 */
	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	/**
	 * @return the closedBy
	 */
	public User getClosedBy() {
		return closedBy;
	}

	/**
	 * @return the closed
	 */
	public Boolean getClosed() {
		return closed;
	}
	
	
	/**
	 * @return
	 */
	public Boolean isClosed(){
		return closed;
	}

	/**
	 * @param closed the closed to set
	 */
	public void setClosed(Boolean closed) {
		this.closed = closed;
	}

	/**
	 * @param closedBy the closedBy to set
	 */
	public void setClosedBy(User closedBy) {
		this.closedBy = closedBy;
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
	public void addConsommation(Consommation consommation){
		
		if (consommations == null)
			consommations = new TreeSet<Consommation>();
		if (consommation != null) {
			consommation.setGlobalBill(this);
			consommations.add(consommation);
		}
	}




}
