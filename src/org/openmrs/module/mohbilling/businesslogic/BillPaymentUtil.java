package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PaidServiceBillRefund;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.openmrs.module.mohbilling.service.BillingService;

public class BillPaymentUtil {
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	public static  List<BillPayment>  getAllReveivedAmount(){
		
		List<BillPayment> payments = new ArrayList<BillPayment>();
		
		for (BillPayment billpayment: getService().getAllBillPayments()){
		
			payments.add(billpayment);			
		}		
		return payments;
	}
	
	public static List<BillPayment> getBillPaymentsByDateAndCollector(Date startDate,Date endDate,User collector){
		List<BillPayment> payments = new ArrayList<BillPayment>();
		payments = getService().getBillPaymentsByDateAndCollector(startDate,endDate, collector);
		
		return payments;
		
	}


	public static void createPaidServiceBill(PaidServiceBill paidSb) {
			getService().savePaidServiceBill(paidSb);
			
		}
	/**
	 * Gets BillPayment by a given paymentid
	 * @param paymentId
	 * @return BillPayment
	 */
	public static BillPayment getBillPaymentById(Integer paymentId){
		
		return getService().getBillPayment(paymentId);
			
	}
	/**
	 * Gets all paid billable services matching with a given payment
	 * @param payment
	 * @return List<PaidService> paidItems
	 */
	public static List<PaidServiceBill> getPaidItemsByBillPayment(
			BillPayment payment) {
		return getService().getPaidServices(payment) ;
	}
	/**
	 * gets paidServiceBill matching with a give paidServiceBillId
	 * @param paidSviceBillid
	 * @return paidItem
	 */
	public static PaidServiceBill getPaidServiceBill(Integer paidSviceBillid) {
	
		return  getService().getPaidServiceBill(paidSviceBillid);
	}

	public static List<BillPayment> getAllPaymentByDatesAndCollector(
			Date startDate, Date endDate, User collector) {
		return getService().getBillPaymentsByDateAndCollector(startDate, endDate, collector);
	}

	public static List<PaidServiceBill> getPaidItemsByBillPayments(
			List<BillPayment> payments) {
		return  getService().getPaidItemsByBillPayments(payments);
	}

	public static List<PaymentRefund> getRefundsByBillPayment(BillPayment payment){
		return getService().getRefundsByBillPayment(payment);
	}
	
	public static List<PaidServiceBill> getOldPayments(BillPayment payment){
		List<PaidServiceBill> paidItems=null;
		Consommation consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
       		   paidItems=new ArrayList<PaidServiceBill>();
       		   for (PatientServiceBill psb : consommation.getBillItems()) {
       			   PaidServiceBill paidSb = new PaidServiceBill();
       			   paidSb.setPaidQty(psb.getQuantity());
       			   paidSb.setBillPayment(payment);
       			   paidSb.setCreator(payment.getCreator());
       			   paidSb.setCreatedDate(payment.getCreatedDate());			
       			   paidSb.setVoided(payment.getVoided());
       			   paidSb.setBillItem(psb);
       			   BillPaymentUtil.createPaidServiceBill(paidSb);
       			   paidItems.add(paidSb);
       	   }
    	return paidItems;
	}

	

}
