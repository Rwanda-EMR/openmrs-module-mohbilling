package org.openmrs.module.mohappointment.statepattern;

import org.openmrs.module.mohappointment.model.Appointment;

public abstract class State {

	public void isNull() {
	}

	public Appointment confirmed() {
		return null;
	}

	public Appointment upcoming() {
		return null;
	}

	public void attended() {
	}

	public void expired() {
	}

	public void retired() {
	}

	public void postponed() {
	}

	public void inAdvance() {
	}

	public Appointment waiting() {
		return null;
	}

	public static State enter(Appointment appointment) {
		return null;
	}

	public String toString() {
		return null;
	}
}
