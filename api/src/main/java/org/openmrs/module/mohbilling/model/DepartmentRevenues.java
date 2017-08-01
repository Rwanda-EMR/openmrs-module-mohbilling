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
public class DepartmentRevenues {

	public Department department;
	public List<PaidServiceRevenue> paidServiceRevenues;
	public BigDecimal amount;
	/**
	 * @return the department
	 */
	public Department getDepartment() {
		return department;
	}
	/**
	 * @param department the department to set
	 */
	public void setDepartment(Department department) {
		this.department = department;
	}
	/**
	 * @return the paidServiceRevenues
	 */
	public List<PaidServiceRevenue> getPaidServiceRevenues() {
		return paidServiceRevenues;
	}
	/**
	 * @param paidServiceRevenues the paidServiceRevenues to set
	 */
	public void setPaidServiceRevenues(List<PaidServiceRevenue> paidServiceRevenues) {
		this.paidServiceRevenues = paidServiceRevenues;
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

	
	
	
}
