/**
 * 
 */
package org.openmrs.module.mohappointment.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.model.AppointmentView;
import org.openmrs.module.mohappointment.model.ServiceProviders;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentUtil {

	private static Log log = LogFactory.getLog(AppointmentUtil.class);

	/**
	 * Gets Appointment Context service.
	 * 
	 * @return IAppointmentService instance
	 */
	public static IAppointmentService getAppointmentService() {
		return Context.getService(IAppointmentService.class);
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param codedConceptQuestionId
	 * @return
	 */
	public static HashMap<Integer, String> createConceptCodedOptions(
			Integer codedConceptQuestionId) {
		HashMap<Integer, String> answersMap = new HashMap<Integer, String>();
		Concept questionConcept = Context.getConceptService().getConcept(
				Integer.valueOf(codedConceptQuestionId));
		if (questionConcept != null) {
			for (ConceptAnswer ca : questionConcept.getAnswers()) {
				answersMap.put(ca.getAnswerConcept().getConceptId(), ca
						.getAnswerConcept().getDisplayString());
			}
		}
		return answersMap;

	}

	/**
	 * Converts into AppointmentView an Appointment
	 * 
	 * @param app
	 *            the appointment to be converted
	 * @return view the AppointmentView
	 */
	public static AppointmentView convertIntoAppointmentViewObject(
			Appointment app) {
		Services services = null;

		if (app.getReason() != null)
			services = AppointmentUtil.getServiceByConcept(app.getReason()
					.getValueCoded());
		else
			services = app.getService();

		AppointmentView view = new AppointmentView();

		view.setAppointmentId(app.getAppointmentId());
		view.setAppointmentDate(app.getAppointmentDate());
		view.setAppointmentState(app.getAppointmentState());
		view.setEncounter(app.getEncounter());
		view.setAttended(app.getAttended());
		view.setLocation(app.getLocation());
		view.setNextVisitDate(app.getNextVisitDate());
		view.setPatient(app.getPatient());
		view.setProvider(app.getProvider());
		view.setReason(app.getReason());
		view.setVoided(app.isVoided());
		view.setService(services);
		view.setPatientUrl(AppointmentUtil.getPatientURL(services, null));

		return view;
	}

	/**
	 * Converts into AppointmentView list a list of Appointments
	 * 
	 * @param appointments
	 *            the list of Appointments
	 * @return <code>views</code> the list of appointments views
	 */
	public static List<AppointmentView> convertIntoAppointmentViewList(
			List<Appointment> appointments) {

		List<AppointmentView> views = new ArrayList<AppointmentView>();

		for (Appointment app : appointments) {
			views.add(AppointmentUtil.convertIntoAppointmentViewObject(app));
		}

		return views;
	}

	/**
	 * Gets a Services by matching a given Concept
	 * 
	 * @param concept
	 *            the Concept to be matched
	 * @return Services object that matched the Concept
	 */
	public static Services getServiceByConcept(Concept concept) {

		IAppointmentService ias = getAppointmentService();
		
		if(ias != null)
			for (Services service : ias.getServices()) {
				if (service.getConcept().getConceptId().intValue() == concept
						.getConceptId().intValue())
					return service;
			}

		return null;
	}

	/**
	 * Cancels an appointmen by setting it to <code>voided==true</code>
	 * 
	 * @param request
	 *            the HTTP Servlet Request
	 * @param service
	 *            the Appointment Hibernate Service
	 * @return true when the cancellation is successful, false otherwise
	 */
	public static boolean cancelAppointment(HttpServletRequest request) {

		Integer appointmentId = 0;

		IAppointmentService service = getAppointmentService();

		if (request.getParameter("appointmentId") != null)
			if (!request.getParameter("appointmentId").equalsIgnoreCase("")) {
				appointmentId = Integer.valueOf(request
						.getParameter("appointmentId"));
				Appointment appointment = service
						.getAppointmentById(appointmentId);

				if (request.getParameter("cancel") != null) {

					appointment.setVoided(true);
					appointment.setAppointmentState(new AppointmentState(1,
							"NULL"));
					service.saveAppointment(appointment);

					return true;
				}
			}
		return false;
	}

	/**
	 * Sets an appointment as attended (i.e. <code>attended=true</code>)
	 * 
	 * @param request
	 *            the HTTP Servlet Request
	 * @param service
	 *            the Appointment Hibernate Service
	 * @return true when the update is successful, false otherwise
	 */
	public static boolean setAttendedAppointment(HttpServletRequest request) {

		Integer appointmentId = 0;
		IAppointmentService service = getAppointmentService();

		if (request.getParameter("appointmentId") != null)
			if (!request.getParameter("appointmentId").equalsIgnoreCase("")) {
				appointmentId = Integer.valueOf(request
						.getParameter("appointmentId"));

				Appointment appointment = service
						.getAppointmentById(appointmentId);

				if (request.getParameter("attended") != null) {

					appointment.setAttended(true);
					appointment.setAppointmentState(new AppointmentState(9,
							"ATTENDED"));
					service.saveAppointment(appointment);

					return true;
				}
			}
		return false;
	}

	/**
	 * Gets the very last encounter datetime of a given patient
	 * 
	 * @param patient
	 *            the patient to be matched
	 * @return the last encounter datetime that a given patient got
	 */
	public static Date getPatientLastVisitDate(Patient patient) {

		List<Encounter> encList = Context.getEncounterService()
				.getEncountersByPatientId(patient.getPatientId());
		Date maxDate = encList.get(0).getEncounterDatetime();

		for (Encounter enc : Context.getEncounterService()
				.getEncountersByPatientId(patient.getPatientId())) {
			if (enc.getEncounterDatetime().compareTo(maxDate) > 0) {
				maxDate = enc.getEncounterDatetime();
			}
		}
		return maxDate;
	}

	/**
	 * Gets all the Appointments of today associated with a given Provider
	 * 
	 * @param Provider
	 *            the provider (associated to many services) to be matched
	 * @param startDate
	 *            the start date of the period we are trying to match
	 * @param endDate
	 *            the end date of the period we are trying to match
	 * @return the list of Appointments that were matched
	 */
	public static List<Appointment> getTodayAppointmentsForProvider(
			User authUser, Date startDate, Date endDate,
			Services selectedService) {

		List<Appointment> appointments = new ArrayList<Appointment>();
		IAppointmentService ias = getAppointmentService();
		List<Services> services = null;
		Services servise = null;

		if ((authUser.getPerson().getPersonId().intValue() > 1)) {
			services = (List<Services>) ias.getServicesByProvider(authUser
					.getPerson());
		}

		if (services != null && selectedService == null) {
			if (services.size() > 1) {

				appointments = getNoSelectedService(authUser, startDate,
						endDate, appointments, ias, services);
			} else if (services.size() == 1) {

				appointments = getWhereProviderWorksInOneService(authUser,
						startDate, endDate, ias, servise);
			}

		}

		else if (selectedService != null) {

			appointments = getFilteredBySelectedService(authUser, startDate,
					endDate, selectedService, ias);
		}

		return appointments;
	}

	/**
	 * This means the selected service is filtered amongst different services a
	 * provider works for
	 */
	private static List<Appointment> getFilteredBySelectedService(
			User authUser, Date startDate, Date endDate,
			Services selectedService, IAppointmentService ias) {
		List<Appointment> appointments;
		List<Integer> waitingAppointmentIds = new ArrayList<Integer>();
		Object[] conditionsWaitingAppointment = { null, null, null, startDate,
				null, endDate, 4, selectedService.getServiceId() };
		log.info("__________ <<<<<< Inside the Method: >>>>> ____________ The service ID:"
				+ selectedService.getServiceId()
				+ ", called: "
				+ selectedService.getName());

		if (authUser.getPerson() != null) {
			waitingAppointmentIds = ias.getAppointmentIdsByMulti(
					conditionsWaitingAppointment, 100);
			List<Appointment> waitingAppointments = new ArrayList<Appointment>();
			for (Integer appointmentId : waitingAppointmentIds) {
				waitingAppointments.add(ias.getAppointmentById(appointmentId));
			}

			appointments = waitingAppointments;
		} else
			appointments = new ArrayList<Appointment>();
		return appointments;
	}

	/**
	 * This means the provider works in one service!
	 */
	private static List<Appointment> getWhereProviderWorksInOneService(
			User authUser, Date startDate, Date endDate,
			IAppointmentService ias, Services servise) {
		List<Appointment> appointments;

		if ((authUser.getPerson().getPersonId().intValue() > 1))
			servise = ias.getServiceByProvider(authUser.getPerson());

		appointments = getFilteredBySelectedService(authUser, startDate,
				endDate, servise, ias);
		return appointments;
	}

	/**
	 * Checks whether the services is not null and there is no selected service
	 * by the provider
	 */
	private static List<Appointment> getNoSelectedService(User authUser,
			Date startDate, Date endDate, List<Appointment> appointments,
			IAppointmentService ias, List<Services> services) {
		List<Integer> waitingAppointmentIds = new ArrayList<Integer>();
		for (Services serv : services) {
			// Here I am using the Hibernate method by passing WAITING
			// state + ServiceId
			Object[] conditionsWaitingAppointment = { null, null, null,
					startDate, null, endDate, 4, serv.getServiceId() };
			log.info("_________<< I got some services, : >>_________"
					+ serv.getName() + "\n");

			if (authUser.getPerson() != null) {
				waitingAppointmentIds = ias.getAppointmentIdsByMulti(
						conditionsWaitingAppointment, 100);
				List<Appointment> waitingAppointments = new ArrayList<Appointment>();
				for (Integer appointmentId : waitingAppointmentIds) {
					waitingAppointments.add(ias
							.getAppointmentById(appointmentId));
				}
				if (waitingAppointments != null) {
					log.info("___________________No of Waiting Appointments gotten from service : "
							+ waitingAppointments.size());
					appointments.addAll(waitingAppointments);
				}
			} else
				appointments = new ArrayList<Appointment>();
		}
		return appointments;
	}

	/**
	 * Gets the URL to redirect to the Patient Default page depending on the
	 * module/service
	 * 
	 * @param service
	 * @param provider
	 * @return the URL that will be the redirect to the default patient page
	 */
	public static String getPatientURL(Services service, User provider) {

		GlobalProperty labProperty = Context.getAdministrationService()
				.getGlobalPropertyObject("mohappointment.link.laboratory_link");
		GlobalProperty pharmacyProperty = Context.getAdministrationService()
				.getGlobalPropertyObject("mohappointment.link.pharmacy_link");
		GlobalProperty labConcept = Context.getAdministrationService()
				.getGlobalPropertyObject(
						"mohappointment.concept.laboratory_concept");
		GlobalProperty pharmacyConcept = Context.getAdministrationService()
				.getGlobalPropertyObject(
						"mohappointment.concept.pharmacy_concept");

		if (service != null
				&& service
						.equals(AppointmentUtil.getServiceByConcept(Context
								.getConceptService().getConcept(
										Integer.parseInt(labConcept
												.getPropertyValue())))))
			return labProperty.getPropertyValue();
		else if (service != null
				&& service.equals(AppointmentUtil.getServiceByConcept(Context
						.getConceptService().getConcept(
								Integer.parseInt(pharmacyConcept
										.getPropertyValue())))))
			return pharmacyProperty.getPropertyValue();
		else
			return "/patientDashboard.form";
	}

	/**
	 * Saves Appointment and set it on waiting list
	 * 
	 * @param appointment
	 *            the appointment to be saved: this should be including setting
	 *            all attributes.
	 */
	public static void saveWaitingAppointment(Appointment appointment) {

		IAppointmentService service = getAppointmentService();

		appointment.setAppointmentState(new AppointmentState(4, "WAITING"));
		service.saveAppointment(appointment);
	}

	/**
	 * Saves Appointment and removes it from the waiting list
	 * 
	 * @param appointment
	 *            the appointment to be saved: this should be including setting
	 *            all attributes.
	 */
	public static void saveAttendedAppointment(Appointment appointment) {

		IAppointmentService service = getAppointmentService();

		appointment.setAppointmentState(new AppointmentState(9, "ATTENDED"));
		appointment.setAttended(true);
		service.saveAppointment(appointment);
	}

	/**
	 * Saves Appointment and set it on waiting list
	 * 
	 * @param appointment
	 *            the appointment to be saved: this should be including setting
	 *            all attributes.
	 */
	public static void saveUpcomingAppointment(Appointment appointment) {

		IAppointmentService service = getAppointmentService();

		appointment.setAppointmentState(new AppointmentState(3, "UPCOMING"));
		service.saveAppointment(appointment);
	}

	/**
	 * Gets the Appointment corresponding to the provided ID
	 * 
	 * @param id
	 *            the ID to be matched.
	 * @return the appointment corresponding to the given ID
	 */
	public static Appointment getWaitingAppointmentById(int id) {

		IAppointmentService service = getAppointmentService();

		return service.getAppointmentById(id);
	}

	/**
	 * Gets the Appointment corresponding to the provided ID
	 * 
	 * @param id
	 *            the ID to be matched.
	 * @return the appointment corresponding to the given ID
	 */
	public static void editServiceProvider(ServiceProviders serviceProvider) {

		IAppointmentService service = getAppointmentService();

		service.saveServiceProviders(serviceProvider);
	}

	/**
	 * Gets all existing Services
	 * 
	 * @return the services
	 */
	public static List<Services> getAllServices() {

		IAppointmentService services = getAppointmentService();

		return (List<Services>) services.getServices();
	}

	/**
	 * Gets all existing ServiceProviders
	 * 
	 * @return the serviceProviders
	 */
	public static List<ServiceProviders> getAllServiceProviders() {

		IAppointmentService serviceProviders = getAppointmentService();

		return (List<ServiceProviders>) serviceProviders.getServiceProviders();
	}

	/**
	 * Gets the ServiceProviders that matches the provided ID
	 * 
	 * @param id
	 * @return ServiceProviders matching the given ID
	 */
	public static ServiceProviders getServiceProvidersById(int id) {

		IAppointmentService service = getAppointmentService();

		return service.getServiceProviderById(id);
	}

	/**
	 * Gets the Appointments corresponding to the provided Patient and Date
	 * Using [patientId, providerId, locationId, appointmentDate, attended,
	 * appointmentDate, appointmentState, reason] as Conditions
	 * 
	 * @param patient
	 *            the patient to be matched
	 * @param date
	 *            the date to be matched, if not provided, just pass null
	 * @return all appointments that match the conditions
	 */
	public static List<Appointment> getAppointmentsByPatientAndDate(
			Patient patient, Services clinicalService, Date date) {

		IAppointmentService service = getAppointmentService();
		Object[] conditions = { patient.getPatientId(), null, null, date, null,
				null, null, null };
		List<Appointment> appointments = new ArrayList<Appointment>();

		if (clinicalService != null) {
			for (Integer id : service.getAppointmentIdsByMulti(conditions, 100)) {
				Appointment app = service.getAppointmentById(id);
				if (app.getService().equals(clinicalService))
					appointments.add(app);
			}
		} else
			for (Integer id : service.getAppointmentIdsByMulti(conditions, 100))
				appointments.add(service.getAppointmentById(id));

		return appointments;
	}

}
