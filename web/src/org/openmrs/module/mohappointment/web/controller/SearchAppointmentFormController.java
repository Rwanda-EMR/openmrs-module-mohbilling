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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.AppointmentView;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.module.mohappointment.utils.ConstantValues;
import org.openmrs.module.mohappointment.utils.ContextProvider;
import org.openmrs.module.mohappointment.utils.FileExporterUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 * Comments
 */
public class SearchAppointmentFormController extends
		ParameterizableViewController {

	private Log log = LogFactory.getLog(this.getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		IAppointmentService ias = Context.getService(IAppointmentService.class);

		List<AppointmentView> appointments = (request.getParameter("patient") == null) ? null
				: getAppointments(request, mav);

		if (request.getParameter("export") != null) {
			FileExporterUtil util = new FileExporterUtil();
			util.exportAppointments(request, response, getAppointments(request,
					mav));
		}
		mav.addObject("appointments", appointments);

		mav.addObject("reasonForAppointmentOptions", AppointmentUtil
				.createConceptCodedOptions(ConstantValues.REASON_FOR_VISIT));
		mav.addObject("appointmentStates", ias.getAppointmentStates());
		mav.addObject("areasToSee", ias.getServices());
		mav.addObject("today", Context.getDateFormat().format(new Date()));
		mav.addObject("creator", Context.getAuthenticatedUser());
		mav.addObject("reportName", ContextProvider
				.getMessage("mohappointment.export.report.search.result"));
		// log
		// .info("______________________From SearchAppointmentFormController : "
		// + mav.getModelMap().get("appointments").toString());

		return mav;
	}

	private List<AppointmentView> getAppointments(HttpServletRequest request,
			ModelAndView mav) {
		IAppointmentService ias = Context.getService(IAppointmentService.class);

		try {

			String patientId = (request.getParameter("patient") != null && request
					.getParameter("patient").trim().compareTo("") != 0) ? request
					.getParameter("patient")
					: null;
			String providerId = (request.getParameter("provider") != null && request
					.getParameter("provider").trim().compareTo("") != 0) ? request
					.getParameter("provider")
					: null;
			String locationId = (request.getParameter("location") != null && request
					.getParameter("location").trim().compareTo("") != 0) ? request
					.getParameter("location")
					: null;
			Date dateFrom = (request.getParameter("dateFrom") != null
					&& (request.getParameter("dateFrom").trim().compareTo("") != 0) ? (Date) Context
					.getDateFormat().parse(request.getParameter("dateFrom"))
					: null);
			Date dateTo = (request.getParameter("dateTo") != null
					&& (request.getParameter("dateTo").trim().compareTo("") != 0) ? (Date) Context
					.getDateFormat().parse(request.getParameter("dateTo"))
					: null);
			String stateOfApp = (request.getParameter("stateofappointment") != null && request
					.getParameter("stateofappointment").trim().compareTo("") != 0) ? request
					.getParameter("stateofappointment")
					: null;
			Integer reasonOfApp = (request.getParameter("reasonofappointment") != null && request
					.getParameter("reasonofappointment").trim().compareTo("") != 0) ? Integer
					.valueOf(request.getParameter("reasonofappointment"))
					: null;

			mav.addObject("parameters", createAdditionalParameters(patientId,
					providerId, locationId, dateFrom, dateTo, stateOfApp,
					reasonOfApp));

			Object[] conditions = { patientId, providerId, locationId,
					dateFrom, null, dateTo, stateOfApp, reasonOfApp };

			List<Integer> appointmentIds = ias.getAppointmentIdsByMulti(
					conditions, 100);

			// log.info("-------------------------------->>>>>>>> "+appointmentIds.size());

			List<AppointmentView> appointments = new ArrayList<AppointmentView>();
			for (Integer appointmentId : appointmentIds) {
				// log.info("-------------------------------->>> appID: "+appointmentId);
				appointments.add(AppointmentUtil
						.convertIntoAppointmentViewObject(ias
								.getAppointmentById(appointmentId)));
			}

			return appointments;
		} catch (Exception e) {
			log.error("------------------------ " + e.getMessage()
					+ " -------------------------");
			e.printStackTrace();
			return new ArrayList<AppointmentView>();
		}
	}

	private String createAdditionalParameters(String patientId,
			String providerId, String locationId, Date dateFrom, Date dateTo,
			String stateOfApp, Integer reasonOfApp) {

		String parameters = "";

		parameters += (null != patientId) ? "&patient=" + patientId : "";
		parameters += (null != providerId) ? "&provider=" + providerId : "";
		parameters += (null != locationId) ? "&location=" + locationId : "";
		parameters += (null != dateFrom) ? "&dateFrom="
				+ Context.getDateFormat().format(dateFrom) : "";
		parameters += (null != dateTo) ? "&dateTo="
				+ Context.getDateFormat().format(dateTo) : "";
		parameters += (null != stateOfApp) ? "&stateofappointment="
				+ stateOfApp : "";
		parameters += (null != reasonOfApp) ? "&reasonofappointment="
				+ reasonOfApp.intValue() : "";

		return parameters;
	}

}
