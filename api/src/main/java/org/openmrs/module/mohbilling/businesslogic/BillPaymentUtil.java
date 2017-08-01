package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PatientBill;
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
	
//	public static List<BillPayment> getPaymentsByPatientBill(PatientBill pb){
//		return pb.get
//	}
	

}
