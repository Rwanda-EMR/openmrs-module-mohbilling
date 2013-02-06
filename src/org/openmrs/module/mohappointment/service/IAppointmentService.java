/**
 * 
 */
package org.openmrs.module.mohappointment.service;

import java.util.Collection;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Person;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.model.ServiceProviders;
import org.openmrs.module.mohappointment.model.Services;

/**
 * @author Kamonyo
 * 
 *         All services to be used by the Appointment Management module are
 *         defined (contracted) in this class. This the Business layer side.
 */
public interface IAppointmentService {

	/**
	 * Gets an appointment by its appointmentId
	 * 
	 * @param appointmentId
	 *            , the appointment to be matched
	 * @return appointment matching the id
	 */
	public Appointment getAppointmentById(int appointmentId);

	/**
	 * Gets all existing appointments.
	 * 
	 * @return list of all existing appointments
	 */
	public Collection<Appointment> getAllAppointments();

	/**
	 * Saves an appointment in the database.
	 * 
	 * @param appointment
	 *            , the appointment to be saved
	 */
	public void saveAppointment(Appointment appointment);

	/**
	 * Edits an existing appointment.
	 * 
	 * @param appointment
	 *            , the appointment to be updated
	 */
	public void updateAppointment(Appointment appointment);

	/**
	 * Edits an existing appointment.
	 * 
	 * @param appointment
	 *            , the appointment to be updated
	 * @param stateId
	 *            , the stateId to be updated
	 */
	public void updateState(Appointment appointment, Integer stateId);

	/**
	 * Removes a given appointment from the database.
	 * 
	 * @param appointment
	 *            , the appointment to be cancelled
	 */
	public void cancelAppointment(Appointment appointment);

	/**
	 * Gets appointments IDs by entering different conditions of filtering and
	 * also specifying the limit number to be returned.
	 * 
	 * @param conditions
	 *            , conditions are as follows: [patientId, providerId,
	 *            locationId, appointmentDate, attended, appointmentDate,
	 *            appointmentState, reason]
	 * @return the list of matching appointmentIds
	 */
	public List<Integer> getAppointmentIdsByMulti(Object[] conditions, int limit);

	/**
	 * Gets the last entered appointmentId
	 * 
	 * @return the matched appointmentId
	 */
	public Integer lastAppointmentId();

	/**
	 * Loads all existing appointments (not voided)
	 */
	public void loadAllAppointments();

	/**
	 * Gets the list of all existing appointmentStates
	 * 
	 * @return the list of appointmentState
	 */
	public Collection<AppointmentState> getAppointmentStates();

	/**
	 * Gets the appointmentState matching the entered name
	 * 
	 * @param name
	 *            the name of the state to be matched
	 * @return appointmentState the matched appointment state
	 */
	public AppointmentState getAppointmentStatesByName(String name);

	/**
	 * Saves the entered Services into the DB
	 * 
	 * @param service
	 *            the service to be saved
	 */
	public void saveService(Services service);

	/**
	 * Updates the entered Services in DB
	 * 
	 * @param service
	 *            the service to be updated
	 */
	public void updateService(Services service);

	/**
	 * Saves the entered ServiceProviders into the DB
	 * 
	 * @param service
	 *            the serviceProvider to be saved
	 */
	public void saveServiceProviders(ServiceProviders serviceProvider);

	/**
	 * Updates the entered ServiceProviders in DB
	 * 
	 * @param service
	 *            the serviceProvider to be updated
	 */
	public void updateServiceProviders(ServiceProviders serviceProvider);

	/**
	 * Gets the Services that matches the entered provider: i.e. the provider is
	 * working in that service.
	 * 
	 * @param provider
	 *            the provider to be matched in order to get his/her service
	 * @return the matching service
	 */
	public Services getServiceByProvider(Person provider);

	/**
	 * Gets the Providers working in the entered service
	 * 
	 * @param service
	 *            the service to be matched in order to get his/her service.
	 * @return all the matching providerIds <code>personId</code> in that
	 *         service
	 */
	public Collection<Integer> getPersonsByService(Services service);

	/**
	 * Gets the matching Services by entering the <code>serviceId</code>
	 * 
	 * @param serviceId
	 *            the serviceId to be matched in order to get the service
	 * 
	 * @return the service matched
	 */
	public Services getServiceById(Integer serviceId);

	/**
	 * Gets the matching Services by entering the <code>Concept</code>
	 * 
	 * @param concept
	 *            the Concept to be matched in order to get the service
	 * 
	 * @returnthe service matched
	 */
	public Services getServiceByConcept(Concept concept);

	/**
	 * Gets all the existing ServiceProviders from the DB: i.e. not voided
	 * 
	 * @return the list of all serviceProviders;
	 */
	public Collection<ServiceProviders> getServiceProviders();

	/**
	 * Gets all the existing services in the Hospital or HC
	 * 
	 * @return the list of all services
	 */
	public Collection<Services> getServices();

	/**
	 * Gets the Services that matches the entered provider: i.e. the provider is
	 * working in that service.
	 * 
	 * @param provider
	 *            the provider to be matched in order to get his/her service
	 * @returnall the matching providerIds <code>personId</code> in that service
	 */
	public Collection<Services> getServicesByProvider(Person provider);
	
	/**
	 * Gets the matching ServiceProviders by entering the <code>serviceProviderId</code>
	 * 
	 * @param serviceProviderId
	 *            the serviceProviderId to be matched in order to get the serviceProviders
	 * 
	 * @return the serviceProviders matched
	 */
	public ServiceProviders getServiceProviderById(int serviceProviderId);
}
