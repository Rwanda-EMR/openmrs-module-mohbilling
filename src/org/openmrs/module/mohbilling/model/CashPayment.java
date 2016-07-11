/**
 * 
 */
package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;

/**
 * @author emr
 *
 */
public class CashPayment  extends BillPayment {
	
	private Integer cashPaymentId;
	
	private BigDecimal amountPaid;

	/**
	 * @return the cashPaymentId
	 */
	public Integer getCashPaymentId() {
		return cashPaymentId;
	}

	/**
	 * @param cashPaymentId the cashPaymentId to set
	 */
	public void setCashPaymentId(Integer cashPaymentId) {
		this.cashPaymentId = cashPaymentId;
	}

	/**
	 * @return the amountPaid
	 */
	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	/**
	 * @param amountPaid the amountPaid to set
	 */
	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}
	

}
