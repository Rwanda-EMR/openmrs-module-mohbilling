/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.sql.Date;

import org.openmrs.User;

/**
 * @author EMR@RBC
 *
 */
public class DepositWithdrawal {
 private Integer withdrawalID;
 private Date withdrawalDate;
 private Deposit deposit;
 private BigDecimal withdrawalAmount = new BigDecimal(0);
 private String withdrawalReason;
 private User cashier;
 private User creator;
 private Date createdDate;
 private boolean voided = false;
 private User voidedBy;
 private Date voidedDate;
 private String voidReason;
/**
 * @return the withdrawalID
 */
public Integer getWithdrawalID() {
	return withdrawalID;
}
/**
 * @param withdrawalID the withdrawalID to set
 */
public void setWithdrawalID(Integer withdrawalID) {
	this.withdrawalID = withdrawalID;
}
/**
 * @return the withdrawalDate
 */
public Date getWithdrawalDate() {
	return withdrawalDate;
}
/**
 * @param withdrawalDate the withdrawalDate to set
 */
public void setWithdrawalDate(Date withdrawalDate) {
	this.withdrawalDate = withdrawalDate;
}
/**
 * @return the withdrawalAmount
 */
public BigDecimal getWithdrawalAmount() {
	return withdrawalAmount;
}
/**
 * @param withdrawalAmount the withdrawalAmount to set
 */
public void setWithdrawalAmount(BigDecimal withdrawalAmount) {
	this.withdrawalAmount = withdrawalAmount;
}
/**
 * @return the cashier
 */
public User getCashier() {
	return cashier;
}
/**
 * @param cashier the cashier to set
 */
public void setCashier(User cashier) {
	this.cashier = cashier;
}
/**
 * @return the withdrawalReason
 */
public String getWithdrawalReason() {
	return withdrawalReason;
}
/**
 * @param withdrawalReason the withdrawalReason to set
 */
public void setWithdrawalReason(String withdrawalReason) {
	this.withdrawalReason = withdrawalReason;
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
/**
 * @return the deposit
 */
public Deposit getDeposit() {
	return deposit;
}
/**
 * @param deposit the deposit to set
 */
public void setDeposit(Deposit deposit) {
	this.deposit = deposit;
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

 
 
 
}
