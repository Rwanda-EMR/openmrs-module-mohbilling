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

/*import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;*/
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentDashboardByProviderFormController extends
		ParameterizableViewController {

	// private Log log = LogFactory.getLog(this.getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		User authUser = Context.getAuthenticatedUser();
		String defaultLoc = authUser.getUserProperties().get(
				OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION);

		mav.setViewName(getViewName());

		mav.addObject("authenticatedUser", authUser);
		mav.addObject("authenticatedUserLoc", (null != defaultLoc) ? Context
				.getLocationService().getDefaultLocation() : "-");
		mav.addObject("todayDate", new Date());

		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// filtering the Waiting Appointments
		filterWaitingAppointments(request, mav, authUser, service);

		// getting the Upcoming Appointments
		getUpcomingAppointments(mav, authUser, service);

		return mav;
	}

	/**
	 * Gets the Upcoming Appointments for a logged in Provider
	 * 
	 * @param mav
	 *            the Model and View
	 * @param authUser
	 *            the Provider
	 * @param service
	 *            the IAppointmentService
	 */
	private void getUpcomingAppointments(ModelAndView mav, User authUser,
			IAppointmentService service) {
		Object[] conditionsUpcomingAppointment = { null,
				authUser.getPerson().getPersonId(), null, null, null, null, 3,
				null };

		List<Integer> upcomingAppointmentIds = service
				.getAppointmentIdsByMulti(conditionsUpcomingAppointment, 100);
		List<Appointment> upcomingAppointments = new ArrayList<Appointment>();
		for (Integer appointmentId : upcomingAppointmentIds) {
			upcomingAppointments.add(service.getAppointmentById(appointmentId));
		}

		mav.addObject("upcomingAppointments", AppointmentUtil
				.convertIntoAppointmentViewList(upcomingAppointments));
	}

	/**
	 * Gets and filters the Waiting Appointments for a logged in Provider
	 * 
	 * @param request
	 *            the HttpServletRequest
	 * @param mav
	 *            the Model and View
	 * @param authUser
	 *            the Provider
	 * @param service
	 *            the IAppointmentService
	 */
	private void filterWaitingAppointments(HttpServletRequest request,
			ModelAndView mav, User authUser, IAppointmentService service) {
		String display = "none";
		// By default at the first display, we don't expect to filter waiting
		// list!
		List<Appointment> waitingAppointments = AppointmentUtil
				.getTodayAppointmentsForProvider(authUser, new Date(),
						new Date(), null);

		// Check if the Provider is associated to more than 1 service and
		// display filter div
		if (request.getParameter("select_service") != null) {
			if (request.getParameter("services_by_provider") != null
					&& !request.getParameter("services_by_provider").equals("")) {

				waitingAppointments = AppointmentUtil
						.getTodayAppointmentsForProvider(authUser, new Date(),
								new Date(),
								service.getServiceById(Integer.parseInt(request
										.getParameter("services_by_provider"))));

			}
		}

		List<Services> services = (List<Services>) service
				.getServicesByProvider(authUser.getPerson());

		if (services.size() > 1 && waitingAppointments.size() > 0) {
			mav.addObject("services", services);
			display = "block";
		}

		mav.addObject("display_filter", display);

		mav.addObject("waitingAppointments", AppointmentUtil
				.convertIntoAppointmentViewList(waitingAppointments));
	}

}
