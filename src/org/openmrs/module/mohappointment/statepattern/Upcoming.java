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
public class Upcoming extends State {
	private Appointment appointment;
	private Log log = LogFactory.getLog(this.getClass());
	private static Upcoming instance;

	// Constructor
	private Upcoming(Appointment appointment) {
		System.out.println("Upcoming State is instantiated...");
		this.appointment = appointment;
	}

	public void postponed() {
		// System.out.println("Moving to Postponed state...");
		// appointment.setState(Postponed.enter(appointment));

		if (appointment != null) {
			appointment.setState(Postponed.enter(appointment));
			System.out.println("Moving to Postponed state...");
		} else
			log.info(">>>>>> The Postponed state was not instantiated");

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(8,
		// "POSTPONED"));
		service.updateState(appointment, 8);
		// service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
	}

	public void expired() {
		// System.out.println("Moving to Expired state...");
		// appointment.setState(Expired.enter(appointment));

		if (appointment != null) {
			appointment.setState(Expired.enter(appointment));
			System.out.println("Moving to Expired state...");
		} else
			log.info(">>>>>> The Expired state was not instantiated");

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(6, "EXPIRED"));
		service.updateState(appointment, 6);
		// service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
	}

	public void inAdvance() {
		System.out.println("Moving to InAdvance state...");
		appointment.setState(InAdvance.enter(appointment));

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		appointment.setAppointmentState(new AppointmentState(5, "INADVANCE"));
		service.updateAppointment(appointment);
	}

	public Appointment waiting() {
		// System.out.println("Moving to Waiting state...");
		// appointment.setState(Waiting.enter(appointment));

		// Save to DB here
		// IAppointmentService service = Context
		// .getService(IAppointmentService.class);
		//
		// appointment.setAppointmentState(new AppointmentState(4, "WAITING"));
		// service.updateAppointment(appointment);
		//		
		// if (appointment != null) {
		// appointment.setState(Expired.enter(appointment));
		// System.out.println("Moving to Expired state...");
		// } else
		// log.info(">>>>>> The Expired state was not instantiated");

		// Save to DB here
		IAppointmentService service = Context
				.getService(IAppointmentService.class);

		// appointment.setAppointmentState(new AppointmentState(4, "WAITING"));
		// service.updateState(appointment, 4);
		// // service.updateAppointment(appointment);
		// log.info("--------INSIDE Upcoming STATE Object-------->> "
		// + appointment.toString());
		appointment.setAppointmentState(service
				.getAppointmentStatesByName("WAITING"));
		log.info("__________________>>>>>>>>>>>>>>>>>> "
				+ appointment.getAppointmentState().toString());
		// service.updateState(appointment, 2);
		service.updateAppointment(appointment);
		log.info("---------------->> " + appointment.toString());
		return appointment;
	}

	public static State enter(Appointment appointment) {
		if (instance == null)
			instance = new Upcoming(appointment);

		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Upcoming";
	}
}
