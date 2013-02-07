/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
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
public class MohBillingFacilityServiceFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			boolean saved = handleSaveFacilityService(request, mav);
			if (saved)
				return new ModelAndView(
						new RedirectView("facilityService.list"));
		}

		if (request.getParameter("retire") != null) {
			boolean retired = handleRetireFacilityService(request, mav);
			if (retired)
				return new ModelAndView(
						new RedirectView("facilityService.list"));
		}

		if (request.getParameter("facilityServiceId") != null) {
			try {
				mav.addObject("facilityService", (Context
						.getService(BillingService.class))
						.getFacilityServicePrice(Integer.valueOf(request
								.getParameter("facilityServiceId"))));
			} catch (Exception e) {
				log.error(">>>MOH>>BILLING>> The Facility Service with '"
						+ request.getParameter("facilityServiceId")
						+ "' cannot be found !");
				e.printStackTrace();

				return new ModelAndView(
						new RedirectView("facilityService.list"));
			}
		}
		if (Context.getLocationService().getDefaultLocation() == null)
			mav.addObject("locations", Context.getLocationService()
					.getAllLocations());
		else {
			List<Location> locations = new ArrayList<Location>();
			locations.add(Context.getLocationService().getDefaultLocation());
			mav.addObject("locations", locations);
		}

		return mav;

	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private boolean handleSaveFacilityService(HttpServletRequest request,
			ModelAndView mav) {

		FacilityServicePrice fs = null;

		try {
			// check if the facilityService is NEW or if you are trying to
			// UPDATE an
			// existing facilityService
			if (request.getParameter("facilityServiceId") != null) {
				fs = Context.getService(BillingService.class)
						.getFacilityServicePrice(
								Integer.valueOf(request
										.getParameter("facilityServiceId")));
			} else
				fs = new FacilityServicePrice();
		} catch (Exception e) {
			log.error(">>>MOH>>BILLING>> The Facility Service with '"
					+ request.getParameter("facilityServiceId")
					+ "' cannot be found !");
			e.printStackTrace();

			return false;
		}

		try {
			// facilityService
			fs.setName(request.getParameter("facilityServiceName"));
			fs.setShortName(request.getParameter("facilityServiceShortName"));
			fs.setDescription(request
					.getParameter("facilityServiceDescription"));
			fs.setStartDate(Context.getDateFormat().parse(
					request.getParameter("facilityServiceStartDate")));
			
			// Letting the Full Price to be 0 when not entered.
			if (request.getParameter("facilityServiceFullPrice") != null
					&& !request.getParameter("facilityServiceFullPrice")
							.equals(""))
				fs.setFullPrice(BigDecimal.valueOf(Double.valueOf(request
						.getParameter("facilityServiceFullPrice"))));
			else
				fs.setFullPrice(new BigDecimal(0));

			fs.setLocation(Context.getLocationService().getLocation(
					Integer.valueOf(request
							.getParameter("facilityServiceLocation"))));
			fs.setConcept(Context.getConceptService().getConcept(
					Integer.valueOf(request
							.getParameter("facilityServiceRelatedConcept"))));

			// check if the facilityService is NEW or if you are trying to
			// UPDATE an
			// existing facilityService
			if (null == fs.getFacilityServicePriceId()) {
				fs.setCreatedDate(new Date());
				fs.setCreator(Context.getAuthenticatedUser());
				fs.setRetired(false);

				FacilityServicePriceUtil.createFacilityService(fs);
			} else
				FacilityServicePriceUtil.editFacilityService(fs);

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The Facility Service has been saved successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The Facility Service has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private boolean handleRetireFacilityService(HttpServletRequest request,
			ModelAndView mav) {

		FacilityServicePrice fs = null;

		try {
			fs = Context.getService(BillingService.class)
					.getFacilityServicePrice(
							Integer.valueOf(request
									.getParameter("facilityServiceId")));
		} catch (Exception e) {
			log.error(">>>MOH>>BILLING>> The Facility Service with '"
					+ request.getParameter("facilityServiceId")
					+ "' cannot be found !");
			e.printStackTrace();

			return false;
		}

		try {
			// facilityService

			fs.setRetiredDate(new Date());
			fs.setRetiredBy(Context.getAuthenticatedUser());
			fs.setRetired(true);

			FacilityServicePriceUtil.retireFacilityServicePrice(fs, new Date(),
					request.getParameter("facilityServiceRetireReason"));

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The Facility Service has been retired successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The Facility Service has not been retired !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}

		return true;
	}

}
