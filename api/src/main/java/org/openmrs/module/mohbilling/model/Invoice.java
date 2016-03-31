package org.openmrs.module.mohbilling.model;

import java.util.Date;
import java.util.List;


public class Invoice {	
	Date createdDate;
	List<Consommation> consommationList;	
	Double subTotal;
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
	 * @return the consommationList
	 */
	public List<Consommation> getConsommationList() {
		return consommationList;
	}
	/**
	 * @param consommationList the consommationList to set
	 */
	public void setConsommationList(List<Consommation> consommationList) {
		this.consommationList = consommationList;
	}
	/**
	 * @return the subTotal
	 */
	public Double getSubTotal() {
		return subTotal;
	}
	/**
	 * @param subTotal the subTotal to set
	 */
	public void setSubTotal(Double subTotal) {
		this.subTotal = subTotal;
	}
	
}
