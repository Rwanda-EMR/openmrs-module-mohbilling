package org.openmrs.module.mohbilling.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingGlobalBillListController extends
		ParameterizableViewController {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		String ipCardNumber = request.getParameter("ipCardNumber");
		InsurancePolicy ip = InsurancePolicyUtil.getInsurancePolicyByCardNo(ipCardNumber);
		
		List<GlobalBill> globalBills = GlobalBillUtil.getGlobalBillsByInsurancePolicy(ip);
		
		mav.addObject("globalBills", globalBills);	
		mav.setViewName(getViewName());
		
		// TODO Auto-generated method stub
		return mav;
	}
}
