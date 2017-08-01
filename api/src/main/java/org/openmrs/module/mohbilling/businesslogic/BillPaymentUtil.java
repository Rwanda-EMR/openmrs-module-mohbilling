package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BillPaymentUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 *
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	public static List<BillPayment> getAllReveivedAmount() {

		List<BillPayment> payments = new ArrayList<BillPayment>();

		for (BillPayment billpayment : getService().getAllBillPayments()) {

			payments.add(billpayment);
		}
		return payments;
	}

	public static List<BillPayment> getBillPaymentsByDateAndCollector(Date startDate, Date endDate, User collector) {
		List<BillPayment> payments = new ArrayList<BillPayment>();
		payments = getService().getBillPaymentsByDateAndCollector(startDate, endDate, collector);

		return payments;

	}


	public static void createPaidServiceBill(PaidServiceBill paidSb) {
		getService().savePaidServiceBill(paidSb);

	}

	/**
	 * Gets BillPayment by a given paymentid
	 *
	 * @param paymentId
	 * @return BillPayment
	 */
	public static BillPayment getBillPaymentById(Integer paymentId) {

		return getService().getBillPayment(paymentId);

	}

	/**
	 * Gets all paid billable services matching with a given payment
	 *
	 * @param payment
	 * @return List<PaidService> paidItems
	 */
	public static List<PaidServiceBill> getPaidItemsByBillPayment(
			BillPayment payment) {
		return getService().getPaidServices(payment);
	}

	/**
	 * gets paidServiceBill matching with a give paidServiceBillId
	 *
	 * @param paidSviceBillid
	 * @return paidItem
	 */
	public static PaidServiceBill getPaidServiceBill(Integer paidSviceBillid) {

		return getService().getPaidServiceBill(paidSviceBillid);
	}

	public static List<BillPayment> getAllPaymentByDatesAndCollector(
			Date startDate, Date endDate, User collector) {
		return getService().getBillPaymentsByDateAndCollector(startDate, endDate, collector);
	}

	public static List<PaidServiceBill> getPaidItemsByBillPayments(
			List<BillPayment> payments) {
		List<PaidServiceBill> paidItems = getService().getPaidItemsByBillPayments(payments);
		return paidItems;
	}

	public static List<PaymentRefund> getRefundsByBillPayment(BillPayment payment) {
		return getService().getRefundsByBillPayment(payment);
	}

	public static List<PaidServiceBill> getOldPayments(BillPayment payment) {
		List<PaidServiceBill> paidItems = new ArrayList<PaidServiceBill>();
		;
		Consommation consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
		for (PatientServiceBill psb : consommation.getBillItems()) {
			PaidServiceBill paidSb = new PaidServiceBill();
			paidSb.setPaidQty(psb.getQuantity());
			paidSb.setBillPayment(payment);
			paidSb.setCreator(payment.getCreator());
			paidSb.setCreatedDate(payment.getCreatedDate());
			paidSb.setVoided(payment.getVoided());
			paidSb.setBillItem(psb);
			// BillPaymentUtil.createPaidServiceBill(paidSb);
			if (!paidItems.contains(paidSb))
				paidItems.add(paidSb);
		}
		return paidItems;
	}

	public static List<PaidServiceBill> getOldPaidItems(List<BillPayment> payments) {
		List<PaidServiceBill> paidItems = new ArrayList<PaidServiceBill>();
		for (BillPayment bp : payments) {
			//paidItems = new ArrayList<PaidServiceBill>();
			//Consommation consommation = ConsommationUtil.getConsommationByPatientBill(bp.getPatientBill());
			Consommation consommation = null;
			if(!bp.getVoided())
				consommation = ConsommationUtil.getConsommationByPatientBill(bp.getPatientBill());
			for (PatientServiceBill psb : consommation.getBillItems()) {
				PaidServiceBill paidSb = new PaidServiceBill();
				paidSb.setPaidQty(psb.getQuantity());
				paidSb.setBillPayment(bp);
				paidSb.setCreator(bp.getCreator());
				paidSb.setCreatedDate(bp.getCreatedDate());
				paidSb.setVoided(bp.getVoided());
				paidSb.setBillItem(psb);
				// BillPaymentUtil.createPaidServiceBill(paidSb);
				paidItems.add(paidSb);
			}
		}
		return paidItems;
	}

	public static BigDecimal getTotalPaid(List<BillPayment> payments) {
		BigDecimal totalPaid = new BigDecimal(0);
		for (BillPayment payment : payments) {
			if(payment.getVoidReason()==null) {
				totalPaid = totalPaid.add(payment.getAmountPaid());
			}
		}
		return totalPaid;
	}

	public static BigDecimal getAmountRefunded(BillPayment bp) {
		BigDecimal refundedAmount = new BigDecimal(0);
		for (PaymentRefund r : getRefundsByBillPayment(bp)) {
			refundedAmount = refundedAmount.add(r.getRefundedAmount());
		}
		return refundedAmount;

	}
}
