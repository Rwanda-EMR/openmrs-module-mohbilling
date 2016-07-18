package org.openmrs.module.mohbilling.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingGlobalBillListController extends
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
		List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
		String ipCardNumber = request.getParameter("ipCardNumber");
		String billIdentifier = request.getParameter("billIdentifier");
		Beneficiary ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(ipCardNumber);
		InsurancePolicy ip = InsurancePolicyUtil.getInsurancePolicyByBeneficiary(ben);
		
		
		Beneficiary benef = Context.getService(BillingService.class).getBeneficiaryByPolicyNumber(ipCardNumber);
		
		if(ipCardNumber!=null ){
			log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@insurance Policy id"+ip.getInsurancePolicyId());
		 globalBills = GlobalBillUtil.getGlobalBillsByInsurancePolicy(ip);		
		}	
		if(billIdentifier != null){
			GlobalBill globalBill = GlobalBillUtil.getGlobalBillByBillIdentifier(billIdentifier);
			globalBills.add(globalBill);
		}
		mav.addObject("insurancePolicy", ip);
		mav.addObject("beneficiary",benef);	
		mav.addObject("globalBills", globalBills);
		
		
		mav.setViewName(getViewName());

		return mav;
	}
}
