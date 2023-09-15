package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.List;

public class ServiceReportRevenue {

	private String service ;
	private List<PatientServiceBillReport> billItems;
	private BigDecimal dueAmount;


	public ServiceReportRevenue(String service, BigDecimal dueAmount) {
		this.service = service;
		this.dueAmount = dueAmount;
	}


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
	 * @return the billItems
	 */
	public List<PatientServiceBillReport> getBillItems() {
		return billItems;
	}


	/**
	 * @param billItems the billItems to set
	 */
	public void setBillItems(List<PatientServiceBillReport> billItems) {
		this.billItems = billItems;
	}


	/**
	 * @return the dueAmount
	 */
	public BigDecimal getDueAmount() {
		return dueAmount;
	}


	/**
	 * @param dueAmount the dueAmount to set
	 */
	public void setDueAmount(BigDecimal dueAmount) {
		this.dueAmount = dueAmount;
	}
	

}
