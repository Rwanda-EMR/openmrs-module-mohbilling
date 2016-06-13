package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingAdmissionFormController extends
		ParameterizableViewController {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		ModelAndView mav = new ModelAndView();
		InsurancePolicy ip =null;
	
	if (request.getParameter("save") != null && request.getParameter("save").equals("true")) {
		
		Admission admission = new Admission();
		admission.setAdmissionDate(new Date());
		admission.setDischargingDate(new Date());
		admission.setIsAdmitted(true);
		admission.setCreator(Context.getAuthenticatedUser());
		
		
	ip =Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));
		
		
	}
		
		
	 ip = Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));
		

		mav.addObject("insurancePolicy", ip);
		mav.setViewName(getViewName());
		return mav;
	}

}
