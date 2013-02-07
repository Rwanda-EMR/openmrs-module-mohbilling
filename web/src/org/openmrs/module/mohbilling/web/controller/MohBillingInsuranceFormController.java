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
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 *         This controller backs the /web/module/mohBillingInsuranceForm.jsp
 *         page. This controller is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */
public class MohBillingInsuranceFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("insuranceCategories", InsuranceCategory.values());

		if (request.getParameter("save") != null) {
			boolean saved = handleSaveInsurance(request, mav);
			if (saved)
				return new ModelAndView(new RedirectView("insurance.list"));
		} else if (request.getParameter("void") != null) {
			boolean voided = handleVoidInsurance(request, mav);
			if (voided)
				return new ModelAndView(new RedirectView("insurance.list"));
		}

		if (request.getParameter("insuranceId") != null) {
			try {
				Insurance insurance = Context.getService(BillingService.class)
						.getInsurance(
								Integer.valueOf(request
										.getParameter("insuranceId")));
				if (null == insurance) {
					request.getSession().setAttribute(
							WebConstants.OPENMRS_ERROR_ATTR,
							"The insurance with ID ['"
									+ request.getParameter("insuranceId")
									+ "'] cannot be found !");
					return new ModelAndView(new RedirectView("insurance.list"));
				} else
					mav.addObject("insurance", insurance);
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
		}

		return mav;

	}

	/**
	 * @param request
	 * @param mav
	 * @return true if the insurance has been saved successfully <br/>
	 *         false if the insurance failed to be saved
	 */
	private boolean handleSaveInsurance(HttpServletRequest request,
			ModelAndView mav) {

		Insurance insurance = null;
		InsuranceRate rate = null;

		if (request.getParameter("insuranceId") == null)
			insurance = new Insurance();
		else {
			try {
				insurance = Context.getService(BillingService.class)
						.getInsurance(
								Integer.valueOf(request
										.getParameter("insuranceId")));
			} catch (Exception e) {
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,
						"The insurance with id ['"
								+ request.getParameter("insuranceId")
								+ "'] has not been found!");
				log.error("" + e.getMessage());
				e.printStackTrace();

				return false;
			}
		}

		try {
			// insurance
			insurance.setAddress(request.getParameter("insuranceAddress"));
			insurance.setPhone(request.getParameter("insurancePhone"));
			insurance.setName(request.getParameter("insuranceName"));
			insurance.setConcept(Context.getConceptService().getConcept(
					Integer.valueOf(request
							.getParameter("insuranceRelatedConceptId"))));
			insurance.setCategory(request.getParameter("insuranceCategory"));

			if (null == insurance.getInsuranceId()) {
				insurance.setCreatedDate(new Date());
				insurance.setCreator(Context.getAuthenticatedUser());
				insurance.setVoided(false);
			}

			// insurance rate
			if (request.getParameter("insuranceRate") != null
					&& request.getParameter("insuranceRate").trim().compareTo(
							"") != 0) {
				try {
					rate = new InsuranceRate();

					rate
							.setFlatFee((request
									.getParameter("insuranceFlatFee") != null && request
									.getParameter("insuranceFlatFee").trim()
									.compareTo("") != 0) ? BigDecimal
									.valueOf(Long.valueOf(request
											.getParameter("insuranceFlatFee")))
									: null);
					rate
							.setRate((request.getParameter("insuranceRate") != null && request
									.getParameter("insuranceRate").trim()
									.compareTo("") != 0) ? Float
									.valueOf(request
											.getParameter("insuranceRate"))
									: null);
					rate
							.setStartDate((request
									.getParameter("insuranceRateStartDate") != null && request
									.getParameter("insuranceRateStartDate")
									.trim().compareTo("") != 0) ? Context
									.getDateFormat()
									.parse(
											request
													.getParameter("insuranceRateStartDate"))
									: null);

					rate.setRetired(false);
					rate.setCreatedDate(new Date());
					rate.setCreator(Context.getAuthenticatedUser());

				} catch (Exception e) {
					log.error(">>MOH>>BILLING>> " + e.getMessage());
					e.printStackTrace();
					request.getSession().setAttribute(
							WebConstants.OPENMRS_ERROR_ATTR,
							"The insurance rate has not been saved !");

					return false;
				}
			}

			InsuranceUtil.createInsurance(insurance, rate);

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The insurance has been saved successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * @param request
	 * @param mav
	 * @return true if the insurance has been voided successfully <br/>
	 *         false if the insurance failed to be voided
	 */
	private boolean handleVoidInsurance(HttpServletRequest request,
			ModelAndView mav) {

		Insurance insurance = null;

		try {
			insurance = Context.getService(BillingService.class).getInsurance(
					Integer.valueOf(request.getParameter("insuranceId")));
		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance with id ['"
							+ request.getParameter("insuranceId")
							+ "'] has not been found!");
			log.error("" + e.getMessage());
			e.printStackTrace();
			return false;
		}

		try {

			InsuranceUtil.voidInsurance(insurance, request
					.getParameter("voidReason"));

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The insurance has been voided successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance has not been voided !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
