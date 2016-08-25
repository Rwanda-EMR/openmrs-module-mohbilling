/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.util.List;

import org.openmrs.User;
import org.openmrs.module.mohbilling.model.ServiceRevenue;

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
	

}
