package org.openmrs.module.mohbilling.web.controller;

import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.PaymentRefundUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MohBillingViewRefundedItemsController extends
		ParameterizableViewController {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
	 	  if(request.getParameter("refundId")!=null && !request.getParameter("refundId").equals("")){
			  PaymentRefund refund = PaymentRefundUtil.getRefundById(Integer.valueOf(request.getParameter("refundId")));
			  mav.addObject("refund", refund);
			  Consommation consommation= ConsommationUtil.getConsommationByPatientBill(refund.getBillPayment().getPatientBill());
			  mav.addObject("consommation",consommation);
	}
	 	 return mav;
	}
}
