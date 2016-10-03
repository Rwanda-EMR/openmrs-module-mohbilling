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
import org.openmrs.module.mohbilling.ParametersConversion;
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
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

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
		BillPayment payment = BillPaymentUtil.getBillPaymentById(Integer.parseInt(paymentIdStr));
		Consommation consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
		List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);

		PaymentRefund refund = null;	
		
		if(request.getParameter("edit")!=null){
			refund = PaymentRefundUtil.getRefundById(Integer.parseInt(request.getParameter("refundId")));
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The refund has been Verified by the Chief Cashier !");
			handleEditRefund(payment, refund, request);
			mav.addObject("refund", refund);
			
		}

		if (request.getParameter("save") != null) {
			refund = handleSavePaymentRefund(request);
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The refund has been submitted successfully ! Wait for the chief cashier approval");
			return new ModelAndView(new RedirectView(
					"patientBillPayment.form?"
					+" paymentId="+payment.getPatientBill().getPatientBillId()
					+" &ipCardNumber="+consommation.getBeneficiary().getInsurancePolicy().getInsuranceCardNo()
					+" &consommationId="+consommation.getConsommationId()
			));
		}


		if(request.getParameter("refundingDate")!=null){
			//String refundDateStr = request.getParameter("refundingDate");
			String refundAmountStr = request.getParameter("refundedAmount");
			refund = PaymentRefundUtil.getRefundById(Integer.parseInt(request.getParameter("refundId")));
			refund.setRefundedAmount(BigDecimal.valueOf(Double.parseDouble(refundAmountStr)));
			refund.setRefundedBy(Context.getAuthenticatedUser()); 
			
			Context.getService(BillingService.class).savePaymentRefund(refund);
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The refund has been confirmed !");
			return new ModelAndView(new RedirectView(
					"patientBillPayment.form?"
					+"consommationId="+consommation.getConsommationId()
					+"&ipCardNumber="+consommation.getBeneficiary().getInsurancePolicy().getInsuranceCardNo()
					+"&refundId="+refund.getRefundId()
			));
		}
		
		
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

		//if(request.getParameter("refundedAmount")!=null){
		if(payment!=null){
		 refund = new PaymentRefund();
		 refund.setBillPayment(payment);
		 refund.setCreatedDate(new Date()); //submissionDate
		 refund.setCreator(Context.getAuthenticatedUser()); //submitted by
		 
		 //create paidServiceBillRefund		 
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
				Integer  paidSviceBillid = Integer.parseInt(paidItemIdstr);
                BigDecimal refQuantity = BigDecimal.valueOf(Double.parseDouble(request.getParameter("quantity-"+paidSviceBillid)));
				PaidServiceBill paidItem  =BillPaymentUtil.getPaidServiceBill(paidSviceBillid);
				PaidServiceBillRefund paidSbRefund = new PaidServiceBillRefund();
				
				String refundReason = request.getParameter("refundReason-"+paidSviceBillid);
				
				// submit a refund
				paidSbRefund.setCreatedDate(new Date());
				paidSbRefund.setRefund(refund);
				paidSbRefund.setCreator(Context.getAuthenticatedUser());
				paidSbRefund.setVoided(false);
				paidSbRefund.setPaidItem(paidItem);
				paidSbRefund.setRefQuantity(refQuantity);
				paidSbRefund.setRefundReason(refundReason);
				
				PaymentRefundUtil.createPaidServiceRefund(paidSbRefund);
		 }
		}		
		
		return refund;
		
		
		
	}

	private void handleEditRefund(BillPayment payment,PaymentRefund refund,HttpServletRequest request){
	
		 Map<String, String[]> parameterMap = request.getParameterMap();

		 for (String  parameterName : parameterMap.keySet()) {
			 if (parameterName.startsWith("approveRadio_")) {
			   
				 // retrieve the value of refunded item
				String[] splittedParameterName = parameterName.split("_");
				String refundItemIdstr = splittedParameterName[1];
				Integer  refundItemId = Integer.parseInt(refundItemIdstr);

				PaidServiceBillRefund paidServiceRefund  = PaymentRefundUtil.getPaidServiceBillRefund(refundItemId);
				
				//retrieve an action
				String actionParam[] = request.getParameter("approveRadio_"+refundItemId).split("_");
				Integer action = Integer.valueOf(actionParam[0]);

				// add properties on existing refund
				if(action==1){
				paidServiceRefund.setApproved(true);
				paidServiceRefund.setApprovalDate(new Date());
				paidServiceRefund.setApprovedBy(Context.getAuthenticatedUser());
				paidServiceRefund.isApproved();
				}
				else if(action==0){
					paidServiceRefund.setDeclined(true);
					paidServiceRefund.setDeclineDate(new Date());
					paidServiceRefund.setDecliningNote(request.getParameter("declineNote_"+refundItemId));
				}
				
				PaymentRefundUtil.createPaidServiceRefund(paidServiceRefund);
		 }
		 }
	}

}
