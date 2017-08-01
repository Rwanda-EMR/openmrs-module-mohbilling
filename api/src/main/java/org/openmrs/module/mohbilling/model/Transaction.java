/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author rbcemr
 *
 */
public class Transaction implements Comparable<Transaction>{
 private Integer transactionId;
 private BigDecimal amount = new BigDecimal(0);
 private User collector;
 private Date transactionDate;
 private User creator;
 private Date createdDate;
 private String reason;
 private PatientAccount patientAccount;
 private Boolean voided = false;
 private User voidedBy;
 private Date voidedDate;
 private String voidReason;
/**
 * @return the transactionId
 */
public Integer getTransactionId() {
	return transactionId;
}
/**
 * @param transactionId the transactionId to set
 */
public void setTransactionId(Integer transactionId) {
	this.transactionId = transactionId;
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
 * @return the collector
 */
public User getCollector() {
	return collector;
}
/**
 * @param collector the collector to set
 */
public void setCollector(User collector) {
	this.collector = collector;
}
/**
 * @return the transactionDate
 */
public Date getTransactionDate() {
	return transactionDate;
}
/**
 * @param transactionDate the transactionDate to set
 */
public void setTransactionDate(Date transactionDate) {
	this.transactionDate = transactionDate;
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
 * @return the reason
 */
public String getReason() {
	return reason;
}
/**
 * @param reason the reason to set
 */
public void setReason(String reason) {
	this.reason = reason;
}
/**
 * @return the account
 */
public PatientAccount getPatientAccount() {
	return patientAccount;
}
/**
 * @param account the account to set
 */
public void setPatientAccount(PatientAccount patientAccount) {
	this.patientAccount = patientAccount;
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
@Override
public int compareTo(Transaction other) {
	int ret = OpenmrsUtil.compareWithNullAsGreatest(this.getVoided(), other.getVoided());
	if (ret == 0)
		ret = OpenmrsUtil.compareWithNullAsGreatest(this.getTransactionDate(),
				other.getTransactionDate());
	if (ret == 0 && this.getCreatedDate() != null)
		ret = OpenmrsUtil.compareWithNullAsGreatest(this.getCreatedDate(),
				other.getCreatedDate());
	if (ret == 0 && this.getCreatedDate() != null)
		ret = OpenmrsUtil.compareWithNullAsGreatest(this.hashCode(), other
				.hashCode());
	return ret;
}

 
}
