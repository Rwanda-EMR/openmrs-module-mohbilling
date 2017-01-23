/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rbcemr
 * 
 */
public class MohBillingFacilityServiceListController extends
		ParameterizableViewController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("facilityServiceId") != null)
			if (!request.getParameter("facilityServiceId").equals(""))
				FacilityServicePriceUtil
						.addCategoryToFacilityService(FacilityServicePriceUtil.getFacilityServicePrice(Integer
								.parseInt(request
										.getParameter("facilityServiceId"))));

		if (request.getParameter("addCategoryToFacility") != null)
			if (request.getParameter("addCategoryToFacility").equals("UPDATE"))
				if (FacilityServicePriceUtil
						.addCategoryToAllFacilityServices(InsuranceCategory.BASE
								.toString()))

					request.getSession()
							.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
									"The Facility Service Categories have been added successfully !");
				else
					request.getSession().setAttribute(
							WebConstants.OPENMRS_ERROR_ATTR,
							"DID NOT Update AT ALL !");

		mav.addObject("facilityServices",
				FacilityServicePriceUtil.getFacilityServices(true));

		return mav;

	}
}
