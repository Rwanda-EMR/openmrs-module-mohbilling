/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.RecoveryUtil;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Sending Information to the Form that needs to be UPDATED/CHANGED
 */
public class MohBillingProcessRecoveryController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		mav.addObject("allInsurances", InsuranceUtil.getInsurances(true));
		mav.addObject("allThirdParties",
				InsurancePolicyUtil.getAllThirdParties());

		if (request.getParameter("editRecovery") != null
				&& request.getParameter("editRecoveryId") != null)
			if (!request.getParameter("editRecovery").equals("")
					&& !request.getParameter("editRecoveryId").equals("")) {

				Recovery recovery = RecoveryUtil.getRecovery(Integer
						.parseInt(request.getParameter("editRecoveryId")));

				mav.addObject("recovery", recovery);
			}
		
		if(request.getParameter("deleteRecovery") != null
				&& request.getParameter("deleteRecoveryId") != null)
			if (!request.getParameter("deleteRecovery").equals("")
					&& !request.getParameter("deleteRecoveryId").equals("")) {
				
				Recovery recovery = RecoveryUtil.getRecovery(Integer
						.parseInt(request.getParameter("deleteRecoveryId")));
				
				RecoveryUtil.retireRecovery(recovery);
				
				mav.addObject("recoveryList", RecoveryUtil.getAllRecoveries());
				
				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR,
						"The Recover has been removed !");
				
				return new ModelAndView(new RedirectView("recovery.list"));
			}

		return mav;
	}
}
