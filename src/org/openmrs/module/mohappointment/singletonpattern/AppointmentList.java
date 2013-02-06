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
package org.openmrs.module.mohappointment.singletonpattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.service.IAppointmentService;

/**
 *	
 */
public class AppointmentList extends ArrayList<Appointment> {

	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = -5773055027470828015L;

	private static List<Appointment> appointmentList = new ArrayList<Appointment>();

	private static AppointmentList list = null;

	private AppointmentList() {
	}

	/**
	 * Initializes the list of appointment (singleton)
	 * 
	 * @return the singleton
	 */
	public static AppointmentList getInstance() {
		if (list == null) {
			list = new AppointmentList();
		}
		return list;
	}

	/**
	 * Gets the maximum appointmentId to be able to increment it for a new
	 * appointment added
	 * 
	 * @return the last appointmentId
	 */
	private Integer getLastAppointmentId() {
		IAppointmentService service = Context
				.getService(IAppointmentService.class);
		return service.lastAppointmentId();
	}

	/**
	 * Gets all appointments without attendance (non attended)
	 * 
	 * @return a collection of non attended appointments
	 */
	private Collection<Appointment> getAppointmentWithoutAttendance() {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getAttended() == true || app.getAttended() == false)
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets all appointments by specifying the attendance (yes/no)
	 * 
	 * @param attended
	 * @return the list of all attended or non attended appointments
	 */
	private Collection<Appointment> getAppointmentByAttendance(boolean attended) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getAttended() == attended)
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets the appointments by specifying a period between two dates (startDate
	 * and endDate)
	 * 
	 * @param startDate
	 *            the starting date
	 * @param endDate
	 *            the end date
	 * @return the collection of appointment matching the period
	 */
	private Collection<Appointment> getAppointmentByPeriod(Date startDate,
			Date endDate) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getAppointmentDate().compareTo(startDate) >= 0
					&& app.getAppointmentDate().compareTo(endDate) <= 0)
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets the appointments by specifying the reason of appointment.
	 * 
	 * @param string
	 *            the reason of appointment
	 * @return the collection of appointment matching the reason
	 */
	private Collection<Appointment> getAppointmentByReason(String reason) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getReason().equals(reason))
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets the appointments by specifying the date of appointment.
	 * 
	 * @param Date
	 *            the appointment date
	 * @return the collection of appointment matching the date
	 */
	private Collection<Appointment> getAppointmentByDate(Date appointmentDate) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getAppointmentDate() == appointmentDate)
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets the appointments by specifying the location of appointment.
	 * 
	 * @param location
	 *            the location of appointment
	 * @return the collection of appointment matching the location
	 */
	private Collection<Appointment> getAppointmentByLocation(Location location) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getLocation().getLocationId() == location.getLocationId())
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets the appointments by specifying the service of appointment.
	 * 
	 * @param service
	 *            the service
	 * @return the collection of appointments matching the Services.
	 */
	private Collection<Appointment> getAppointmentByProvider(Person provider) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getProvider().getPersonId() == provider.getPersonId())
				appointments.add(app);
		}
		return appointments;
	}

	/**
	 * Gets the appointments by specifying the patient of appointment.
	 * 
	 * @param patient
	 *            the patient
	 * @return the collection of appointments matching the Patient.
	 */
	private Collection<? extends Appointment> getAppointmentByPatient(
			Patient patient) {
		Collection<Appointment> appointments = new ArrayList<Appointment>();
		for (Appointment app : appointmentList) {
			if (app.getPatient().getPatientId() == patient.getPatientId())
				appointments.add(app);
		}
		return appointments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#iterator
	 * ()
	 */

	public Iterator<Appointment> iterator() {
		return appointmentList.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#
	 * addAppointment(org.openmrs.module.mohappointment.service.Appointment)
	 */

	public boolean addAppointment(Appointment appointment) {
		// Uncomment when you are done with testing on SingletonDriver.java

		IAppointmentService service = Context
				.getService(IAppointmentService.class);
		service.saveAppointment(appointment);
		appointment.setAppointmentId(getLastAppointmentId() + 1);
		return appointmentList.add(appointment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#
	 * deleteAppointment(org.openmrs.module.mohappointment.service.Appointment)
	 */

	public boolean cancelAppointment(Appointment appointment) {
		IAppointmentService service = Context
				.getService(IAppointmentService.class);
		service.cancelAppointment(appointment);
		return appointmentList.remove(appointment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#
	 * updateAppointment(org.openmrs.module.mohappointment.service.Appointment)
	 */

	public void updateAppointment(Appointment appointment) {

		IAppointmentService appointService = Context
				.getService(IAppointmentService.class);

		appointService.updateAppointment(appointment);

		for (Appointment app : appointmentList)
			if (app.getAppointmentId() == appointment.getAppointmentId()) {

				// Updating the matching appointment
				app.setPatient(appointment.getPatient());
				app.setLocation(appointment.getLocation());
				app.setProvider(appointment.getProvider());
				app.setAppointmentDate(appointment.getAppointmentDate());
				app.setReason(appointment.getReason());
				app.setNote(appointment.getNote());
				app.setAttended(appointment.getAttended());
				return;// avoiding to continue to loop while the appointment has
				// been found and updated
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#
	 * addAllAppointments(java.util.Collection)
	 */

	public boolean addAllAppointments(Collection<Appointment> appoints) {
		return appointmentList.addAll(appoints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#
	 * getAppointmentsByMulti(java.lang.Object[])
	 */

	public List<Appointment> getAppointmentsByMulti(Object[] conditions) {

		List<Appointment> result = null;
		StringBuilder combinedSearch = new StringBuilder("");

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// The following lines of code have to be changed in order to match the
		// right one in HibernateAppointmentDAO
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

		if (!conditions[0].equals(""))
			combinedSearch.append(" service_id = " + conditions[0] + " AND ");
		if (!conditions[1].equals(""))
			combinedSearch.append(" location_id = " + conditions[1] + " AND ");
		if (!conditions[2].equals(""))
			combinedSearch.append(" appointment_date = '" + conditions[2]
					+ "' AND ");
		if (!conditions[3].equals(""))
			combinedSearch.append(" reason LIKE '" + conditions[3] + "%' AND ");
		if (!conditions[4].equals("") && !conditions[5].equals(""))
			combinedSearch.append(" appointment_date BETWEEN '" + conditions[4]
					+ "' AND '" + conditions[5] + "' AND ");
		if (!conditions[6].equals("")) {
			if ((Boolean) conditions[6] == false)
				combinedSearch.append(" attended = " + (Boolean) conditions[6]
						+ " AND ");
			else
				// Here we need both Attended and Non-Attended when Include
				// Attended is selected
				combinedSearch
						.append(" (attended = TRUE OR attended = FALSE) AND ");
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.singletonpattern.IAppointmentList#
	 * getPatientAppointmentsByMulti(java.lang.Object[])
	 */

	public List<Appointment> getPatientAppointmentsByMulti(Object[] conditions) {

		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// !!!!!!Maybe we don't need this anymore!!!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		List<Appointment> result = new ArrayList<Appointment>();

		if (!conditions[0].equals(""))// Patient Id
			result.addAll(this.getAppointmentByPatient(Context
					.getPatientService().getPatient(
							Integer.parseInt(conditions[0].toString()))));
		if (!conditions[0].equals(""))// Provider Id
			result.addAll(this.getAppointmentByProvider(Context
					.getPersonService().getPerson(
							Integer.parseInt(conditions[0].toString()))));
		if (!conditions[1].equals(""))// Location Id
			result.addAll(this.getAppointmentByLocation(Context
					.getLocationService().getLocation(
							Integer.parseInt(conditions[1].toString()))));
		if (!conditions[2].equals(""))// Appointment Date
			result.addAll(this.getAppointmentByDate((Date) conditions[2]));
		if (!conditions[3].equals(""))// Reason
			result
					.addAll(this.getAppointmentByReason(conditions[3]
							.toString()));
		if (!conditions[4].equals("") && !conditions[5].equals(""))// Start and
			// End Dates
			result.addAll(this.getAppointmentByPeriod((Date) conditions[4],
					(Date) conditions[5]));
		if (!conditions[6].equals("")) {// Attended
			if ((Boolean) conditions[6] == false)
				result.addAll(this.getAppointmentByAttendance(false));
			else
				result.addAll(this.getAppointmentWithoutAttendance());
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.service.IAppointmentList#size()
	 */

	public int size() {

		return appointmentList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.service.IAppointmentList#isEmpty()
	 */

	public boolean isEmpty() {
		return appointmentList.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.service.IAppointmentList#get(int)
	 */

	public Appointment get(int index) {
		return appointmentList.get(index);
	}

}
