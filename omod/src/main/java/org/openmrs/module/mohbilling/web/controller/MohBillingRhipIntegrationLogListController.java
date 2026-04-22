package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.RhipIntegrationLog;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

public class MohBillingRhipIntegrationLogListController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	private BillingService billingService;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!Context.isAuthenticated()) {
			return new ModelAndView(new RedirectView("/login.htm"));
		}

		User user = Context.getAuthenticatedUser();
		if (user == null || !user.isSuperUser()) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Only system administrator users can access RHIP integration logs.");
			return new ModelAndView(new RedirectView("billingAdmin.form"));
		}

		int limit = 300;
		String limitParam = request.getParameter("limit");
		if (limitParam != null) {
			try {
				limit = Integer.parseInt(limitParam);
			} catch (NumberFormatException ignored) {
			}
		}
		if (limit <= 0) {
			limit = 300;
		}
		if (limit > 1000) {
			limit = 1000;
		}

		List<RhipIntegrationLog> logs = billingService == null
				? Collections.<RhipIntegrationLog>emptyList()
				: billingService.getRecentRhipIntegrationLogs(limit);

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("logs", logs);
		mav.addObject("limit", limit);
		return mav;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}
}
