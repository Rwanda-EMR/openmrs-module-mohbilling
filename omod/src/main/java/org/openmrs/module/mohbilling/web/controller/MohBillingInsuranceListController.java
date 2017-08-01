/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rbcemr
 * 
 *         This controller backs the /web/module/mohBillingInsuranceList.jsp
 *         page. This controller is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */
public class MohBillingInsuranceListController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		mav.addObject("insurances", InsuranceUtil.getInsurances(true));
		
		
		//update other insurances taking rssb as a reference
		try {
			if(request.getParameter("update")!=null){
				 ServiceCategory sc = Context.getService(BillingService.class).getServiceCategory(Integer.valueOf(request.getParameter("serviceCategoryId")));
				 Context.getService(BillingService.class).updateOtherInsurances(sc);
			}
		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,
					"The service category already exist!");
		}

		return mav;

	}

}
