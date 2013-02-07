/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingBillableServiceFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			BillableService bs = handleSaveInsuranceBillableService(request,
					mav);
			if (bs != null) {
				return new ModelAndView(new RedirectView(
						"billableService.list?insuranceId="
								+ bs.getInsurance().getInsuranceId()));
			}
		}

		try {

			mav.addObject("facilityServices",
					FacilityServicePriceUtil.getFacilityServices(true));

			if (null == request.getParameter("billableServiceId")) {
				mav.addObject(
						"insurance",
						Context.getService(BillingService.class).getInsurance(
								Integer.valueOf(request
										.getParameter("insuranceId"))));
			} else {
				new InsuranceUtil();
				BillableService billableService = InsuranceUtil
						.getValidBillableService(Integer.valueOf(request
								.getParameter("billableServiceId")));

				mav.addObject("billableService", billableService);
				mav.addObject("insurance", billableService.getInsurance());
			}

		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance with ID ['"
							+ request.getParameter("insuranceId")
							+ "'] cannot be found !");
			log.error(">>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return new ModelAndView(new RedirectView("insurance.list"));
		}

		return mav;

	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private BillableService handleSaveInsuranceBillableService(
			HttpServletRequest request, ModelAndView mav) {

		BillableService billableService = null;

		if (request.getParameter("billableServiceId") == null)
			billableService = new BillableService();
		else {
			try {
				billableService = InsuranceUtil.getValidBillableService(Integer
						.valueOf(request.getParameter("billableServiceId")));
			} catch (Exception e) {
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,
						"The Billable Service with id ['"
								+ request.getParameter("billableServiceId")
								+ "'] has not been found!");
				log.error("" + e.getMessage());
				e.printStackTrace();

				return null;
			}
		}

		try {
			// billableService
			billableService.setFacilityServicePrice(Context.getService(
					BillingService.class).getFacilityServicePrice(
					Integer.valueOf(request
							.getParameter("billableServiceFacilityService"))));
			billableService.setInsurance(Context.getService(
					BillingService.class).getInsurance(
					Integer.valueOf(request
							.getParameter("insuranceBillableService"))));
			billableService.setServiceCategory(InsuranceUtil
					.getValidServiceCategory(Integer.valueOf(request
							.getParameter("billableServiceServiceCategory"))));
			billableService.setStartDate(Context.getDateFormat().parse(
					request.getParameter("billableServiceStartDate")));

			// This only works/displays for Drug and Consumable categories
			if (request.getParameter("billableServiceMaximaToPay") != null
					&& !request.getParameter("billableServiceMaximaToPay")
							.equals(""))
				billableService.setMaximaToPay(BigDecimal.valueOf(Double
						.valueOf(request
								.getParameter("billableServiceMaximaToPay"))));

			if (billableService.getServiceId() == null) {
				billableService.setCreatedDate(new Date());
				billableService.setCreator(Context.getAuthenticatedUser());
				billableService.setRetired(false);
			}

			BillableService bs = InsuranceUtil
					.saveBillableService(billableService);

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The Billable Service has been saved successfully !");
			return bs;
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The billable service has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return null;
		}

		// return true;
	}

}
