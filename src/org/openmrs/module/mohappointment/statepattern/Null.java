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
public class Null extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static Null instance;

	// Constructor
	private Null(Appointment appointment) {
		System.out.println("Null State is instantiated...");
		this.appointment = appointment;
	}

	public Appointment confirmed() {
		// System.out.println("Moving to Confirmed state...");
		// appointment.setState(Confirmed.enter(appointment));

		if (appointment != null) {
			appointment.setState(Confirmed.enter(appointment));
			System.out.println("Moving to Confirmed state...");

			// Save to DB here

			IAppointmentService service = Context
					.getService(IAppointmentService.class);

			appointment.setAppointmentState(service
					.getAppointmentStatesByName("CONFIRMED"));
			log.info("__________________>>>>>>>>>>>>>>>>>> "
					+ appointment.getAppointmentState().toString());
			// service.updateState(appointment, 2);
			service.updateAppointment(appointment);
			log.info("---------------->> " + appointment.toString());
			return appointment;
		} else {
			log.info(">>>>>> The Confirmed state was not instantiated");
			return null;
		}
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new Null(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Null";
	}

}
