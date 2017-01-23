/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class MohBillingThirdPatryFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("thirdParties", InsurancePolicyUtil.getAllThirdParties());

		if (handleSaveThirdParty(request)) {

			mav.addObject("thirdParty", null);
			mav.addObject("thirdPartyId", "");
			mav.addObject("thirdParties",
					InsurancePolicyUtil.getAllThirdParties());
		}

		/** UPDATE the specified ThirdParty... */
		if (request.getParameter("editThirdParty") != null
				&& request.getParameter("editThirdParty").equals("true")) {

			ThirdParty thirdParty = InsurancePolicyUtil.getThirdParty(Integer
					.parseInt(request.getParameter("editThirdPartyId")));

			mav.addObject("thirdParty", thirdParty);
			mav.addObject("thirdPartyId",
					request.getParameter("editThirdPartyId"));
			
			return mav;
		}

		/** DELETE the specified ThirdParty: this means voiding it... */
		if (request.getParameter("deleteThirdParty") != null
				&& request.getParameter("deleteThirdParty").equals("true")) {

			ThirdParty thirdParty = InsurancePolicyUtil.getThirdParty(Integer
					.parseInt(request.getParameter("deleteThirdPartyId")));

			InsurancePolicyUtil.voidThirdParty(thirdParty);

			mav.addObject("thirdParties",
					InsurancePolicyUtil.getAllThirdParties());
			
			return mav;
		}

		return mav;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param request
	 * @throws NumberFormatException
	 */
	private Boolean handleSaveThirdParty(HttpServletRequest request)
			throws NumberFormatException {

		if (request.getParameter("save") != null
				&& request.getParameter("save").equals("true")) {

			ThirdParty thirdParty = null;

			if (request.getParameter("thirdPartyId") != null) {
				if (!request.getParameter("thirdPartyId").equals("")) {

					thirdParty = InsurancePolicyUtil.getThirdParty(Integer
							.parseInt(request.getParameter("thirdPartyId")));
				}

				if (thirdParty != null) {
					if (request.getParameter("thirdPartyName") != null
							&& !request.getParameter("thirdPartyName").equals(
									"")) {

						thirdParty.setName(request
								.getParameter("thirdPartyName"));
					}

					if (request.getParameter("thirdPartyRate") != null
							&& !request.getParameter("thirdPartyRate").equals(
									"")) {

						thirdParty.setRate(Float.parseFloat(request
								.getParameter("thirdPartyRate")));
					}

					if (thirdParty.getName() != null
							&& thirdParty.getRate() != null) {
						InsurancePolicyUtil.saveThirdParty(thirdParty);

						request.getSession()
								.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
										"The Third Party has been updated successfully !");
					} else
						request.getSession().setAttribute(
								WebConstants.OPENMRS_ERROR_ATTR,
								"The NAME or RATE can't be Blank !");

					return true;

				} else {

					thirdParty = new ThirdParty();

					if (request.getParameter("thirdPartyName") != null
							&& !request.getParameter("thirdPartyName").equals(
									"")) {

						thirdParty.setName(request
								.getParameter("thirdPartyName"));
					}

					if (request.getParameter("thirdPartyRate") != null
							&& !request.getParameter("thirdPartyRate").equals(
									"")) {

						thirdParty.setRate(Float.parseFloat(request
								.getParameter("thirdPartyRate")));
					}

					if (thirdParty.getName() != null
							&& thirdParty.getRate() != null) {
						InsurancePolicyUtil.saveThirdParty(thirdParty);

						request.getSession()
								.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
										"The Third Party has been saved successfully !");
					} else
						request.getSession().setAttribute(
								WebConstants.OPENMRS_ERROR_ATTR,
								"The NAME or RATE can't be Blank !");

					return true;
				}

			} else
				return false;
		}

		return false;
	}
}
