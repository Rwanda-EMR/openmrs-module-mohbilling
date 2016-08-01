/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.mapping.Value;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author emr
 *
 */
public class MohBillingRefundFormController  extends ParameterizableViewController{

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		
		String paymentIdStr = request.getParameter("paymentId");
		
		if(paymentIdStr !=null){
			BillPayment payment = BillPaymentUtil.getBillPaymentById(Integer.parseInt(paymentIdStr));
			
			
			
		}
	
		
		

		return mav;
	}
	

}
