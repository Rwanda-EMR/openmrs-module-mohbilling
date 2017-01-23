/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author mariam
 *
 */
public class PaidServiceRevenue {
 public String service;
 public List<PaidServiceBill> paidItems;
 public BigDecimal paidAmount;
/**
 * @return the service
 */
public String getService() {
	return service;
}
/**
 * @param service the service to set
 */
public void setService(String service) {
	this.service = service;
}
/**
 * @return the paidItems
 */
public List<PaidServiceBill> getPaidItems() {
	return paidItems;
}
/**
 * @param paidItems the paidItems to set
 */
public void setPaidItems(List<PaidServiceBill> paidItems) {
	this.paidItems = paidItems;
}
/**
 * @return the paidAmount
 */
public BigDecimal getPaidAmount() {
	return paidAmount;
}
/**
 * @param paidAmount the paidAmount to set
 */
public void setPaidAmount(BigDecimal paidAmount) {
	this.paidAmount = paidAmount;
}
 
 
}
