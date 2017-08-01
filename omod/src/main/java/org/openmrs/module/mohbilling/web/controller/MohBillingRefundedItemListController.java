/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.PaymentRefundUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mariam
 *
 */
public class MohBillingRefundedItemListController extends
		ParameterizableViewController {
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
  	  if(request.getParameter("refundId")!=null && !request.getParameter("refundId").equals("")){
		  PaymentRefund refund = PaymentRefundUtil.getRefundById(Integer.valueOf(request.getParameter("refundId")));
		  mav.addObject("refund", refund);
		  mav.addObject("consommation", ConsommationUtil.getConsommationByPatientBill(refund.getBillPayment().getPatientBill()));
		  /*Consommation consommation = ConsommationUtil.getConsommationByPatientBill(refund.getBillPayment().getPatientBill());
		  mav.addObject("consommation",consommation);*/
	  }
  	  
		return mav;
	}

}
