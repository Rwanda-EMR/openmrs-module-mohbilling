/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.module.mohbilling.model.ServiceRevenue;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author emr
 *
 */
public class AllServiceRevenue {
	private List<ServiceRevenue> revenues;
	private BigDecimal allDueAmounts; 
	private BigDecimal allpaidAmount;
	private String reportingPeriod;
	private User collector;
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
	 * @param collector the collector to set
	 */
	public void setCollector(User collector) {
		this.collector = collector;
	}
	

}
