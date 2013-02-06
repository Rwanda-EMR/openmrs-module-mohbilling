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
package org.openmrs.module.mohappointment.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.SimplifiedObs;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * This controller backs the /web/mohappointment/patientSummary.jsp page. This
 * controller is tied to that jsp page in the
 * /metadata/moduleApplicationContext.xml file
 */
public class AppointmentPatientSummaryFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		Patient pt = Context.getPatientService().getPatient(
				Integer.valueOf(request.getParameter("patientId")));
		mav.addObject("patient", pt);
		mav.addObject("dOrders", Context.getOrderService()
				.getDrugOrdersByPatient(pt));

		List<Concept> concList = new ArrayList<Concept>();
		concList.add(Context.getConceptService().getConcept(2169));
		concList.add(Context.getConceptService().getConcept(5089));
		concList.add(Context.getConceptService().getConcept(5497));
		concList.add(Context.getConceptService().getConcept(856));

		Map<String, Object> observations = new HashMap<String, Object>();

		// Person p=Context.getPersonService().getPerson(pt.getPersonId());

		for (Concept c : concList) {
			observations.put(c.getDisplayString(),
					transformObsValueList(Context.getObsService()
							.getObservationsByPersonAndConcept(pt, c)));
		}
		mav.addObject("obsSummaryList", observations);

		mav.addObject("lastVisit", AppointmentUtil.getPatientLastVisitDate(pt));

		return mav;
	}

	private List<SimplifiedObs> transformObsValueList(List<Obs> obsList) {
		List<SimplifiedObs> simplifiedObsList = new ArrayList<SimplifiedObs>();
		int count = 0;

		for (Obs o : obsList) {
			simplifiedObsList.add(new SimplifiedObs(o));
			if (count == 4)
				break;
			else
				count += 1;
		}

		return simplifiedObsList;
	}

}