/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohappointment.model;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.module.mohappointment.observerpattern.IObserver;
import org.openmrs.module.mohappointment.statepattern.Attended;
import org.openmrs.module.mohappointment.statepattern.Confirmed;
import org.openmrs.module.mohappointment.statepattern.Expired;
import org.openmrs.module.mohappointment.statepattern.InAdvance;
import org.openmrs.module.mohappointment.statepattern.Null;
import org.openmrs.module.mohappointment.statepattern.Postponed;
import org.openmrs.module.mohappointment.statepattern.Retired;
import org.openmrs.module.mohappointment.statepattern.State;
import org.openmrs.module.mohappointment.statepattern.Upcoming;
import org.openmrs.module.mohappointment.statepattern.Waiting;

/**
 * Appointment class defining the structure of an appointment with all its
 * attributes.
 */
@SuppressWarnings("deprecation")
public class Appointment { // implements ISubject {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/** Attributes */

	private Integer appointmentId;
	private Date appointmentDate;
	private Obs reason;
	private Obs nextVisitDate;
	private String note;
	private Encounter encounter;
	private Boolean attended = false;
	private Location location;
	private Person provider;
	private Services service;
	private Patient patient;
	private boolean voided;
	private String voidReason;
	private Date voidedDate;
	private User creator;
	private User voidedBy;
	private Date createdDate;

	private AppointmentState appointmentState;

	// Observers to be added to this subject

	private List<IObserver> observers;

	public Appointment() {

		// Default values
		// this.state = Null.enter(this);
		this.appointmentState = new AppointmentState(1, "NULL");
		this.voided = false;
		this.attended = false;

		// Observers list is instantiated over here
		this.observers = new ArrayList<IObserver>();
	}

	public Integer getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Integer appointmentId) {
		this.appointmentId = appointmentId;
	}

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public Obs getReason() {
		return reason;
	}

	public void setReason(Obs reason) {
		this.reason = reason;
	}

	public Obs getNextVisitDate() {
		return nextVisitDate;
	}

	public void setNextVisitDate(Obs nextVisitDate) {
		this.nextVisitDate = nextVisitDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	public Encounter getEncounter() {
		return encounter;
	}

	public void setAttended(Boolean attended) {
		this.attended = attended;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.IAppointment#getAttended()
	 */

	public Boolean getAttended() {
		return attended;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.IAppointment#getLocation()
	 */

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Person getProvider() {
		return provider;
	}

	public void setProvider(Person provider) {
		this.provider = provider;
	}

	/**
	 * @return the service
	 */
	public Services getService() {
		return service;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public void setService(Services service) {
		this.service = service;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}

	public boolean isVoided() {
		return voided;
	}

	/**
	 * @return the voidReason
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason
	 *            the voidReason to set
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return the voidedDate
	 */
	public Date getVoidedDate() {
		return voidedDate;
	}

	/**
	 * @param voidedDate
	 *            the voidedDate to set
	 */
	public void setVoidedDate(Date voidedDate) {
		this.voidedDate = voidedDate;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return the voidedBy
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy
	 *            the voidedBy to set
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public AppointmentState getAppointmentState() {
		return appointmentState;
	}

	public void setAppointmentState(AppointmentState appointmentState) {
		this.appointmentState = appointmentState;
	}

	public void setState(State state) {
		this.state = state;

		// Notify all observers about this change
		notifyObservers();

		// This is for displaying in console...
		// out.flush();
	}

	public State getState() {

		if (this.appointmentState.getAppointmentStateId() == 1) {
			log.info(">>>>>>>>>> This is the NULL state <<<<<<<<<");
			return Null.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 2) {
			log
					.info(">>>>>>>>>> This is the CONFIRMED state set when the appointment is assigned <<<<<<<<<");
			return Confirmed.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 3) {
			log.info(">>>>>>>>>> This is the UPCOMING state <<<<<<<<<");
			return Upcoming.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 4) {
			log.info(">>>>>>>>>> This is the WAITING state <<<<<<<<<");
			return Waiting.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 5) {
			return InAdvance.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 6) {
			return Expired.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 7) {
			return Retired.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 8) {
			return Postponed.enter(this);
		} else if (this.appointmentState.getAppointmentStateId() == 9) {
			return Attended.enter(this);
		} else
			return null;
	}

	/* -------------- JAVA Overriden methods -------------- */

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */

	public boolean equals(Object obj) {

		Appointment appointment = (Appointment) obj;
		if (appointment != null)
			if (appointment.getAppointmentId() == this.appointmentId
					&& appointment.hashCode() == this.hashCode()) {
				return true;
			}

		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */

	public int hashCode() {
		return this.appointmentId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */

	public String toString() {

		return "\n - AppointmentId : " + this.appointmentId
				+ "\n - Appointment Date : " + this.appointmentDate.getDate()
				+ "/" + (this.appointmentDate.getMonth() + 1) + "/"
				+ (this.appointmentDate.getYear() + 1900) + "\n - Patient : "
				+ this.patient.getFamilyName() + "\n - Provider : "
				// + this.provider.getPersonName().getFullName()
				+ "\n - Appointment State : "
				+ this.appointmentState.getDescription();
	}

	// **************** STATE PATTERN *****************

	private State state;

	// For display in Console...
	/**
	 * private BufferedReader in; private PrintWriter out;
	 * 
	 * public Appointment(InputStream is, OutputStream os) throws IOException {
	 * in = new BufferedReader(new InputStreamReader(is)); out = new
	 * PrintWriter(os);
	 * 
	 * state = Null.enter(this); this.voided = false; this.attended = false;
	 * 
	 * this.observers = new ArrayList<IObserver>(); }
	 * 
	 * public void Run() throws IOException { String line = null;
	 * 
	 * out .print("Command: (null, confirmed, upcoming, inadvance, attended, postponed, expired, retired, quit): "
	 * ); out.flush();
	 * 
	 * line = in.readLine();
	 * 
	 * while (line != null) { if (line.equals("null")) state.isNull(); if
	 * (line.equals("confirmed")) state.confirmed(); if
	 * (line.equals("upcoming")) state.upcoming(); if (line.equals("inadvance"))
	 * state.inAdvance(); if (line.equals("postponed")) state.postponed(); if
	 * (line.equals("expired")) state.expired(); if (line.equals("retired"))
	 * state.retired(); if (line.equals("attended")) state.attended(); if
	 * (line.equals("quit")) { System.out.println("Exiting..."); break; }
	 * 
	 * out .print("Command: (null, confirmed, upcoming, inadvance, postponed, expired, retired, quit): "
	 * ); line = in.readLine(); out.flush(); } }
	 */

	// ************* OBSERVER PATTERN **************
	public void addObserver(IObserver observer) {
		this.observers.add(observer);
	}

	public void delObserver(IObserver observer) {
		this.observers.remove(observer);
	}

	public void notifyObservers() {
		// int count = 0;
		for (IObserver obs : observers) {
			// obs.update(this);
			// count++;
			// System.out
			// .println("--------------observer #" + count + " is added");
		}
	}

}
