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
public class Retired extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static Retired instance;

	// Constructor
	private Retired(Appointment appointment) {
		System.out.println("Retired State is instantiated...");
		this.appointment = appointment;
	}

	public void isNull() {
		// System.out.println("Moving to Null state...");
		// appointment.setState(Null.enter(appointment));

		if (appointment != null) {
			appointment.setState(Null.enter(appointment));
			System.out.println("Moving to Null state...");
		} else
			log.info(">>>>>> The Null state was not instantiated");

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(1, "NULL"));
		service.updateState(appointment, 1);
		// service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new Retired(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Retired";
	}
}
