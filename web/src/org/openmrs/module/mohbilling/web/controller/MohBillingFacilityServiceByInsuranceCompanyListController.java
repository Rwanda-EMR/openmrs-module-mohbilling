/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingFacilityServiceByInsuranceCompanyListController extends
		ParameterizableViewController {

	private Log log = LogFactory.getLog(this.getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		try {
			FacilityServicePrice facilityService = Context.getService(
					BillingService.class).getFacilityServicePrice(
					Integer.valueOf(request.getParameter("facilityServiceId")));

			mav.addObject("billableServices", FacilityServicePriceUtil
					.getBillableServices(facilityService, new Date(), false));

			mav.addObject("facilityService", facilityService);
		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,
					"The facilityService with ID ['"
							+ request.getParameter("facilityServiceId")
							+ "'] cannot be found !");
			log.error(">>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return new ModelAndView(new RedirectView("facilityService.list"));
		}

		return mav;

	}

}
