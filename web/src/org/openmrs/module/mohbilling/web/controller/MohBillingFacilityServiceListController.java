/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingFacilityServiceListController extends
		ParameterizableViewController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		mav.addObject("facilityServices", FacilityServicePriceUtil
				.getFacilityServices(true));

		return mav;

	}

}
