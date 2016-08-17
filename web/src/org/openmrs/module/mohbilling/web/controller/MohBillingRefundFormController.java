/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.mapping.Value;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PaymentRefundUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PaidServiceBillRefund;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author emr
 *This class controls the mohBillingPaymentRefundForm.jsp page
 */
public class MohBillingRefundFormController  extends ParameterizableViewController{
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		
		String paymentIdStr = request.getParameter("paymentId");	
		
		if(request.getParameter("save")!=null){
			handleSavePaymentRefund(request);		
	     
		}
		
		BillPayment payment = BillPaymentUtil.getBillPaymentById(Integer.parseInt(paymentIdStr));
		Consommation consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
		List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);
		mav.addObject("paidItems",paidItems);
		mav.addObject("payment", payment);
		mav.addObject("consommation",consommation);
		mav.addObject("insurancePolicy",consommation.getBeneficiary().getInsurancePolicy());
		mav.addObject("authUser", Context.getAuthenticatedUser());	
	return mav;
	}
	/**
	 * @param request
	 * @return paymentRefund
	 */
	private PaymentRefund handleSavePaymentRefund(HttpServletRequest request) {
		
		BillPayment payment = BillPaymentUtil.getBillPaymentById(Integer.parseInt(request.getParameter("paymentId")));
		PaymentRefund refund = null;
		if(request.getParameter("refundedAmount")!=null){
		 refund = new PaymentRefund();
		 refund.setBillPayment(payment);
		 refund.setRefundedBy(Context.getAuthenticatedUser());
		 refund.setCreatedDate(new Date());
		 refund.setCreator(Context.getAuthenticatedUser());
		 refund.setRefundedAmount(new BigDecimal(800));
		
		 //create paidServiceBillRefund		 
		log.info("refunding>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+refund);
		 Map<String, String[]> parameterMap = request.getParameterMap();	
		 
		 for (String  parameterName : parameterMap.keySet()) {
			 
			 //to do
			     //Get only  parameterName checked but starting with paid- knowing at the index 1 there is paidServiceBillid
			     //Big Decimal RefQty =request.getParameter("quantity"-paidServiceBillId);
			 //create  new PaidServiceBillRefund object and populate it
			 //refund.add(new paidServiceBillRefund)
			 //saveRefund();
			 
			 if (!parameterName.startsWith("item-")) {
					continue;
				}
							
				String[] splittedParameterName = parameterName.split("-");
				String paidItemIdstr = splittedParameterName[2];
				log.info("<<<<<<<<<paidItemIdstr>>>>>>>>>>>"+paidItemIdstr);
				Integer  paidSviceBillid = Integer.parseInt(paidItemIdstr);
             BigDecimal refQuantity = BigDecimal.valueOf(Double.parseDouble(request.getParameter("quantity-"+paidSviceBillid)));
				
				PaidServiceBill paidItem  =BillPaymentUtil.getPaidServiceBill(paidSviceBillid);
				log.info("<<<<<<<<<<<<<paid servicebill>>>>>>>>>>>>>>"+paidItem);
				PaidServiceBillRefund paidSbRefund = new PaidServiceBillRefund();
				
				paidSbRefund.setCreatedDate(new Date());
				paidSbRefund.setRefund(refund);
				paidSbRefund.setCreator(Context.getAuthenticatedUser());
				paidSbRefund.setVoided(false);
				paidSbRefund.setPaidItem(paidItem);
				paidSbRefund.setRefQuantity(refQuantity);
				
				
				PaymentRefundUtil.createPaidServiceRefund(paidSbRefund);
				
		 }
		}
		return refund;
		
		
		
	}

	

}
