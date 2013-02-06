/**
 * 
 */
package org.openmrs.module.mohappointment.statepattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.service.IAppointmentService;

/**
 * @author Kamonyo
 * 
 */
public class Expired extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static Expired instance;

	// Constructor
	private Expired(Appointment appointment) {
		System.out.println("Expired State is instantiated...");
		this.appointment = appointment;
	}

	public void retired() {
		// System.out.println("Moving to Retired state...");

		/*
		 * This must be automatic when the "Time (10 days)" set as the
		 * expiration date of an appointment, it moves its own state to the
		 * retired state which the non reversible state after expiration
		 */
		// appointment.setState(Retired.enter(appointment));
		if (appointment != null) {
			appointment.setState(Retired.enter(appointment));
			System.out.println("Moving to Retired state...");
		} else
			log.info(">>>>>> The Retired state was not instantiated");

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(7, "RETIRED"));
		service.updateState(appointment, 7);
		// service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
	}

	public void postponed() {
		System.out.println("Moving to Postponed state...");
		/*
		 * This is set when the patient calls of tells the facility the reason
		 * that the appointment has not been respected. Then the new date is set
		 * and the appointment changes the state to postponed state.
		 */
		appointment.setState(Postponed.enter(appointment));

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		appointment.setAppointmentState(new AppointmentState(8, "POSTPONED"));
		service.updateAppointment(appointment);
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new Expired(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Expired";
	}
}
