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
public class InAdvance extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static InAdvance instance;

	// Constructor
	private InAdvance(Appointment appointment) {
		System.out.println("InAdvance State is instantiated...");
		this.appointment = appointment;
	}

	public void attended() {
		// System.out.println("Moving to Attended state...");
		// appointment.setState(Attended.enter(appointment));

		if (appointment != null) {
			appointment.setState(Attended.enter(appointment));
			System.out.println("Moving to Attended state...");
		} else
			log.info(">>>>>> The Attended state was not instantiated");

		// Save to DB here

		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(9, "ATTENDED"));
		service.updateState(appointment, 9);
		// service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new InAdvance(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InAdvance";
	}
}
