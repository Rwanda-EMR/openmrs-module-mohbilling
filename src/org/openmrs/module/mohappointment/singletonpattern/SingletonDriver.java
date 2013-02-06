package org.openmrs.module.mohappointment.singletonpattern;

import org.openmrs.module.mohappointment.model.Appointment;

/**
 * @author Kamonyo
 * 
 *         This is to test the AppointmentList Singleton
 * 
 */
public class SingletonDriver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Kamonyo");
		AppointmentList instance = AppointmentList.getInstance();
		System.out.println("<<<<>>>>" + (instance instanceof AppointmentList));
		instance.addAppointment(new Appointment());
		System.out.println("1a. The size of the list is : " + instance.size());

		AppointmentList instance1 = AppointmentList.getInstance();
		instance1.addAppointment(new Appointment());
		System.out.println("1b. The size of the list is : " + instance1.size());

	}
}
