package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class MohBillingConfigurationFormController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#
	 * handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName(getViewName());


		if(request.getParameter("updateInsurance")!=null){
			// check null
			List<GlobalBill> globalBillsWithNullInsurance= Context.getService(BillingService.class).getGlobalBills();
			int i;
			if(globalBillsWithNullInsurance.size()>0) {
				for (i = globalBillsWithNullInsurance.size(); i > 0; i--) {
					GlobalBill g = globalBillsWithNullInsurance.get(i - 1);
					if (g.getInsurance() == null) {
						g.setInsurance(g.getAdmission().getInsurancePolicy().getInsurance());
						Context.getService(BillingService.class).saveGlobalBill(g);
					}
				}
			}




		/*	for (GlobalBill g:globalBillsWithNullInsurance){
				if(g.getInsurance()==null) {
				g.setInsurance(g.getAdmission().getInsurancePolicy().getInsurance());
				Context.getService(BillingService.class).saveGlobalBill(g);
				}
			}*/
			// end check null
		}


		return mav;
	}

}
