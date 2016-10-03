/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * @author EMR@RBC
 */
public class MohBillingSearchBillPaymentController extends	ParameterizableViewController {
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
    	  if(request.getParameter("paymentId")!=null && !request.getParameter("paymentId").equals("") ){
       	   
       	   payment = BillPaymentUtil.getBillPaymentById(Integer.parseInt(request.getParameter("paymentId")));
       	   consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());    	   
       	   List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);

       	   mav.addObject("paidItems", paidItems); 
       	   mav.addObject("payment", payment);
       	   mav.addObject("consommation",consommation); 
       	   mav.addObject("insurancePolicy",consommation.getBeneficiary().getInsurancePolicy()); 
       	   mav.addObject("beneficiary",consommation.getBeneficiary()); 
       	   
       	   
              if(request.getParameter("print")!=null){
           	   FileExporter exp = new FileExporter();
           	   exp.printPayment(request, response, payment, consommation, "receipt");
              }
          }

	} catch (Exception e) {
		log.info(e.getMessage());
	}
 

		List<PaymentRefund> submittedRefunds = PaymentRefundUtil.getAllSubmittedPaymentRefunds();
		List<PaymentRefund> pendingRefunds = new ArrayList<PaymentRefund>();
		List<PaymentRefund> checkedRefundsByChief = new ArrayList<PaymentRefund>();
		
		// get all refund with all items checked by the chief cashier
		for (PaymentRefund refund : submittedRefunds) {
			if(!PaymentRefundUtil.areAllRefundItemsConfirmed(refund))
				pendingRefunds.add(refund);
		}
		// get all refund with some item not yet checked by the chief cashier
		for (PaymentRefund refund : submittedRefunds) {
			if(PaymentRefundUtil.areAllRefundItemsConfirmed(refund))
				if(refund.getRefundedAmount()==null)
				checkedRefundsByChief.add(refund);
		}
		mav.addObject("pendingRefunds", pendingRefunds);
		mav.addObject("checkedRefundsByChief", checkedRefundsByChief);
		return mav;
	}
}
