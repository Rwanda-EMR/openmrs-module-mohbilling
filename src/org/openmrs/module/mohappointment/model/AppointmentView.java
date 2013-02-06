/**
 * 
 */
package org.openmrs.module.mohappointment.model;

import java.util.Date;

import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;

/**
 * @author Kamonyo
 * 
 *         This class is intended to let the formViews get the attributes
 *         associated with one appointment to be displayed.
 */
public class AppointmentView {

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

	private AppointmentState appointmentState;
	
	private String patientUrl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getAppointmentId
	 * ()
	 */
	public Integer getAppointmentId() {
		return appointmentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setAppointmentId
	 * (java.lang.Integer)
	 */
	public void setAppointmentId(Integer appointmentId) {
		this.appointmentId = appointmentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getAppointmentDate
	 * ()
	 */
	public Date getAppointmentDate() {
		return appointmentDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setAppointmentDate
	 * (java.util.Date)
	 */
	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IAppointmentView#getReason()
	 */
	public Obs getReason() {
		return reason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setReason(org
	 * .openmrs.Obs)
	 */
	public void setReason(Obs reason) {
		this.reason = reason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getNextVisitDate
	 * ()
	 */
	public Obs getNextVisitDate() {
		return nextVisitDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setNextVisitDate
	 * (org.openmrs.Obs)
	 */
	public void setNextVisitDate(Obs nextVisitDate) {
		this.nextVisitDate = nextVisitDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IAppointmentView#getNote()
	 */
	public String getNote() {
		return note;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setNote(java
	 * .lang.String)
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getEncounter()
	 */
	public Encounter getEncounter() {
		return encounter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setEncounter
	 * (org.openmrs.Encounter)
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getAttended()
	 */
	public Boolean getAttended() {
		return attended;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setAttended(
	 * java.lang.Boolean)
	 */
	public void setAttended(Boolean attended) {
		this.attended = attended;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getLocation()
	 */
	public Location getLocation() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setLocation(
	 * org.openmrs.Location)
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getProvider()
	 */
	public Person getProvider() {
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setProvider(
	 * org.openmrs.Person)
	 */
	public void setProvider(Person provider) {
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getService()
	 */
	public Services getService() {
		return service;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setService(org
	 * .openmrs.module.mohappointment.service.IService)
	 */
	public void setService(Services service) {
		this.service = service;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getPatient()
	 */
	public Patient getPatient() {
		return patient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setPatient(org
	 * .openmrs.Patient)
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IAppointmentView#isVoided()
	 */
	public boolean isVoided() {
		return voided;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setVoided(boolean
	 * )
	 */
	public void setVoided(boolean voided) {
		this.voided = voided;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#getAppointmentState
	 * ()
	 */
	public AppointmentState getAppointmentState() {
		return appointmentState;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IAppointmentView#setAppointmentState
	 * (org.openmrs.module.mohappointment.model.AppointmentState)
	 */
	public void setAppointmentState(AppointmentState appointmentState) {
		this.appointmentState = appointmentState;
	}

	/**
	 * @return the patientUrl
	 */
	public String getPatientUrl() {
		return patientUrl;
	}

	/**
	 * @param patientUrl the patientUrl to set
	 */
	public void setPatientUrl(String patientUrl) {
		this.patientUrl = patientUrl;
	}

}
