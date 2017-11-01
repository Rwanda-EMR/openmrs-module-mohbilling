package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.List;

public class PaymentRevenue {
	
	public BillPayment payment;
	public Beneficiary beneficiary;
	public List<PaidServiceRevenue> paidServiceRevenues;
	public BigDecimal amount;
	/**
	 * @return the payment
	 */
	public BillPayment getPayment() {
		return payment;
	}
	/**
	 * @param payment the payment to set
	 */
	public void setPayment(BillPayment payment) {
		this.payment = payment;
	}
	
	/**
	 * @return the beneficiary
	 */
	public Beneficiary getBeneficiary() {
		return beneficiary;
	}
	/**
	 * @param beneficiary the beneficiary to set
	 */
	public void setBeneficiary(Beneficiary beneficiary) {
		this.beneficiary = beneficiary;
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
