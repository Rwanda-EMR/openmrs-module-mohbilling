/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.PaymentRefundUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author EMR@RBC
 */
public class MohBillingSearchBillPaymentController extends ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		BillPayment payment = null;
		Consommation consommation = null;
		
     try {
    	 System.out.println("11111111111111111111111111111111111111111111111");
		 if(request.getParameter("paymentId")!=null && !request.getParameter("paymentId").equals("") ){
       	   
       	   payment = BillPaymentUtil.getBillPaymentById(Integer.parseInt(request.getParameter("paymentId")));
       	   consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
       	   List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);
			 System.out.println("222222222222222222222222222222222222222222");
       	   if(consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
       		   paidItems= BillPaymentUtil.getOldPayments(payment);
       	   }
			 System.out.println("33333333333333333333333333333333333333333333333");
       	   mav.addObject("paidItems", paidItems); 
       	   mav.addObject("payment", payment);
       	   mav.addObject("consommation",consommation); 
       	   mav.addObject("insurancePolicy",consommation.getBeneficiary().getInsurancePolicy()); 
       	   mav.addObject("beneficiary",consommation.getBeneficiary());
			 System.out.println("4444444444444444444444444444444444444444444");
            List<PaymentRefund> submittedRefunds = PaymentRefundUtil.getAllSubmittedPaymentRefunds();
      		List<PaymentRefund> pendingRefunds = new ArrayList<PaymentRefund>();
      		List<PaymentRefund> checkedRefundsByChief = new ArrayList<PaymentRefund>();
			 System.out.println("555555555555555555555555555555555555555555");
      		if(submittedRefunds!=null){
      		// get all refund with all items checked by the chief cashier
      		for (PaymentRefund refund : submittedRefunds) {
      			if(!PaymentRefundUtil.areAllRefundItemsConfirmed(refund))
      				pendingRefunds.add(refund);
      		}
				System.out.println("6666666666666666666666666666666666666666666");
      		// get all refund with some item not yet checked by the chief cashier
      		for (PaymentRefund refund : submittedRefunds) {
      			if(PaymentRefundUtil.areAllRefundItemsConfirmed(refund))
      				if(refund.getRefundedAmount()==null)
      				checkedRefundsByChief.add(refund);
      		}
				System.out.println("7777777777777777777777777777777777777777777");
      		mav.addObject("pendingRefunds", pendingRefunds);
      		mav.addObject("checkedRefundsByChief", checkedRefundsByChief);
      		}
			 System.out.println("88888888888888888888888888888888888888888888");
            if(request.getParameter("print")!=null){
				System.out.println("9999999999999999999999999999999999999999999999");
				FileExporter exp = new FileExporter();
				System.out.println("10101010101010101010101010101010101010101010101010");
				exp.printPayment(request, response, payment, consommation, "receipt.pdf");
              }
			 System.out.println("11 11 11 11 11 11 11 11 11 11 11 11 11");
			 String editPayStr = "";
			 if(request.getParameter("editPay")!=null){
				 editPayStr = request.getParameter("editPay");
				 mav.addObject("editPayStr", editPayStr);
				 }
		 }
    	  

	} catch (Exception e) {
		log.error(e.getMessage());
	}
 		return mav;
	}
}
