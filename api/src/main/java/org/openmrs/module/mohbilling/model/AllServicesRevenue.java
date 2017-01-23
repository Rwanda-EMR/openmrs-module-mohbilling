/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author emr
 *
 */
public class AllServicesRevenue {
	
	private Consommation consommation;
	
	private List<ServiceRevenue> revenues;
	
	private BigDecimal allDueAmounts; 
	
	private BigDecimal allpaidAmount;
	
	private String reportingPeriod;
	
	
	
	private User collector;
	
	
	public AllServicesRevenue(BigDecimal allDueAmounts, BigDecimal allPaidAmount,String reportingPeriod) {		
		this.allDueAmounts =allDueAmounts;
		this.allpaidAmount =allPaidAmount;
		this.reportingPeriod = reportingPeriod;		
	}
	/**
	 * @return the revenues
	 */
	public List<ServiceRevenue> getRevenues() {
		return revenues;
	}
	/**
	 * @param revenues the revenues to set
	 */
	public void setRevenues(List<ServiceRevenue> revenues) {
		this.revenues = revenues;
	}
	/**
	 * @return the allDueAmounts
	 */
	public BigDecimal getAllDueAmounts() {
		return allDueAmounts;
	}
	/**
	 * @param allDueAmounts the allDueAmounts to set
	 */
	public void setAllDueAmounts(BigDecimal allDueAmounts) {
		this.allDueAmounts = allDueAmounts;
	}
	/**
	 * @return the allpaidAmount
	 */
	public BigDecimal getAllpaidAmount() {
		return allpaidAmount;
	}
	/**
	 * @param allpaidAmount the allpaidAmount to set
	 */
	public void setAllpaidAmount(BigDecimal allpaidAmount) {
		this.allpaidAmount = allpaidAmount;
	}
	/**
	 * @return the reportingPeriod
	 */
	public String getReportingPeriod() {
		return reportingPeriod;
	}
	/**
	 * @param reportingPeriod the reportingPeriod to set
	 */
	public void setReportingPeriod(String reportingPeriod) {
		this.reportingPeriod = reportingPeriod;
	}
	/**
	 * @return the collector
	 */
	public User getCollector() {
		return collector;
	}
	/**
	 * @return the consommation
	 */
	public Consommation getConsommation() {
		return consommation;
	}
	/**
	 * @param consommation the consommation to set
	 */
	public void setConsommation(Consommation consommation) {
		this.consommation = consommation;
	}
	/**
	 * @param collector the collector to set
	 */
	public void setCollector(User collector) {
		this.collector = collector;
	}
	

}
