/**
 * 
 */
package org.openmrs.module.mohappointment.observerpattern;

import org.openmrs.module.mohappointment.model.Appointment;

/**
 * @author Kamonyo
 * 
 */
public class ConcreteObserver implements IObserver {

	private Appointment appointment;

	public ConcreteObserver(Appointment appointment) {
		this.appointment = appointment;
		this.appointment.addObserver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.observerpattern.IObserver#update(org
	 * .openmrs.module.mohappointment.observerpattern.ISubject)
	 */
	@Override
	public void update(ISubject subject) {
		System.out.println("********* Observer Pattern *********");
		System.out.println("The Subject State is : "
				+ appointment.getState().toString());
	}

}
