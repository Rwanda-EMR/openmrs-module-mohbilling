/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.web.controller.PortletController;

/**
 *
 */
public class BillingDashboardPortletController extends PortletController {
	@Override
	protected void populateModel(HttpServletRequest request,
			Map<String, Object> model) {
		
		request.setAttribute("policies", InsurancePolicyUtil
				.getPolicyIdByPatient(Integer.valueOf(request
						.getParameter("patientId"))));
		
		super.populateModel(request, model);
	}
}
