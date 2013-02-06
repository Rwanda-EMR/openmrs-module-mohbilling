/**
 * 
 */
package org.openmrs.module.mohappointment.statepattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.service.IAppointmentService;

/**
 * @author Kamonyo
 * 
 */
public class Confirmed extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static Confirmed instance;

	// Constructor
	private Confirmed(Appointment appointment) {
		System.out.println("Confirmed State is instantiated...");
		this.appointment = appointment;

	}

	public Appointment upcoming() {
		if (appointment != null) {
			appointment.setState(Upcoming.enter(appointment));
			System.out.println("Moving to Upcoming state...");
		} else
			log.info(">>>>>> The Upcoming state was not instantiated");

		// Save to DB here

		// Declaring a service from the OpenMRS.Context

		IAppointmentService service = Context
				.getService(IAppointmentService.class);
		// 1. Find a way of keeping the State of any appointment up to date
		// (from DB)
		// 2. Trying to update the AppointmentState in the DB.

		// appointment.setAppointmentState(new AppointmentState(3, "UPCOMING"));
		// service.updateState(appointment, 3);
		// log.info("--------INSIDE Confirmed STATE Object-------->> "
		// + appointment.toString());
		appointment.setAppointmentState(service
				.getAppointmentStatesByName("UPCOMING"));
		log.info("__________________>>>>>>>>>>>>>>>>>> "
				+ appointment.getAppointmentState().toString());
		// service.updateState(appointment, 2);
		service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
		return appointment;
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new Confirmed(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Confirmed";
	}
}
