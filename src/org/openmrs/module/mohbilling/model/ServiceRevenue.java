package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;

public class ServiceRevenue {
	
	HopService service ;
	BigDecimal dueAmount;
	
	
	public ServiceRevenue(HopService svce,BigDecimal dueAmount){
		this.service =svce;
		this.dueAmount=dueAmount;
	}
	
	/**
	 * @return the service
	 */
	public HopService getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(HopService service) {
		this.service = service;
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
