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
public class Postponed extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static Postponed instance;

	// Constructor
	private Postponed(Appointment appointment) {
		System.out.println("Postponed State is instantiated...");
		this.appointment = appointment;
	}

	public Appointment upcoming() {
		System.out.println("Moving to Upcoming state...");
		appointment.setState(Upcoming.enter(appointment));

		if (appointment != null) {
			appointment.setState(Upcoming.enter(appointment));
			System.out.println("Moving to Upcoming state...");
		} else
			log.info(">>>>>> The Upcoming state was not instantiated");

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(3, "UPCOMING"));
		service.updateState(appointment, 3);
		// service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
		return appointment;
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new Postponed(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Postponed";
	}
}
