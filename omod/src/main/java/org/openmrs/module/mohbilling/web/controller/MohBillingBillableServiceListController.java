package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 
 */

/**
 * @author EMR-RBC
 * 
 */
public class MohBillingBillableServiceListController extends
		ParameterizableViewController {

	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		try {
			if(request.getParameter("insuranceId") != null && !request.getParameter("insuranceId").equals("")){
				System.out.println("insurance id "+request.getParameter("insuranceId"));
				Insurance insurance = Context.getService(BillingService.class).getInsurance(Integer.valueOf(request	.getParameter("insuranceId")));
				mav.addObject("billableServices", (FacilityServicePriceUtil
						.getBillableServicesByInsurance(insurance, new Date())));
				mav.addObject("insurance", insurance);

				List<BillableService> currentBS = Context.getService(BillingService.class).getBillableServicesByInsurance(insurance);

				//to avoid loading billable services more than once
				if(currentBS.size()==0){
				Context.getService(BillingService.class).loadBillables(insurance);
				mav.addObject("msg", "All acts,drugs and consummables loaded successfully !");
				}
			}
			

		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance with ID ['"
							+ request.getParameter("insuranceId")
							+ "'] cannot be found !");
			//log.error(">>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return new ModelAndView(new RedirectView("insurance.list"));
		}

		return mav;

	}

}
