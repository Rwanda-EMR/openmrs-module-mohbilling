/**
 * 
 */
package org.openmrs.module.mohappointment.advice;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.module.mohappointment.utils.ConstantValues;
import org.springframework.aop.AfterReturningAdvice;

/**
 * @author Kamonyo Mugabo
 * 
 */
public class ObsServiceAdvice implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());
	private static int obsId = 0;

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public void afterReturning(Object returnVal, Method method, Object[] args,
			Object target) throws Throwable {

		//handleSaveNextVisitObs(returnVal, method, args);
	}

	/**
	 * Gets an Obs or Encounter while anywhere a SaveObs action or SaveEncounter
	 * is done and saves it to the upcoming list
	 * 
	 * @param returnVal
	 * 
	 * @param returnVal
	 *            the returned Encounter when saved
	 * @param method
	 *            the name of the Method to catch
	 * @param args
	 *            the arguments passed to the method to catch
	 */
	private void handleSaveNextVisitObs(Object returnVal, Method method,
			Object[] args) {
		boolean stateChanged = false;
		boolean saved = false;
		IAppointmentService service = Context
				.getService(IAppointmentService.class);
		Obs nextVisitDate = null;
		Obs reasonForVisit = null;

		/**
		 * Setting up an appointment using Save Obs method advice
		 */
		if (method.getName().equals("saveObs")) {
			Obs obs = (Obs) returnVal;
			Appointment appointment = null;
			boolean appointmentFound = false;
			if (obs != null && obs.getObsId().intValue() != obsId) {

				log.info("__________________>>>>>>>>>>>>>>>>>> GETTING IT AS AN OBS"
						+ obs.toString());

				if (obs.getConcept().getConceptId() == ConstantValues.NEXT_SCHEDULED_VISIT) {

					nextVisitDate = obs;
					appointmentFound = true;
					log.info("__________________>>>>>>>>>>>>>>>>>> NEXT_SCHEDULED_VISIT");
				}

				if (obs.getConcept().getConceptId() == ConstantValues.REASON_FOR_VISIT) {

					reasonForVisit = obs;
					log.info("__________________>>>>>>>>>>>>>>>>>> REASON_FOR_VISIT");
				}

				if (appointmentFound) {

					appointment = new Appointment();

					// Setting the appointment attributes
					appointment.setPatient(Context.getPatientService()
							.getPatient(obs.getPersonId()));
					appointment.setLocation(obs.getLocation());
					appointment.setProvider(obs.getEncounter().getProvider());
					appointment.setEncounter(obs.getEncounter());
					appointment.setAppointmentDate(nextVisitDate
							.getValueDatetime());
					appointment.setNextVisitDate(nextVisitDate);
					log.info("_________________>>>>>>>>>>>>>>>> Next Visit date obs id : "
							+ nextVisitDate.getEncounter().getEncounterId());
					appointment.setAttended(false);
					appointment.setVoided(false);
					appointment.setReason(reasonForVisit);
					// appointment.setState(Null.enter(appointment));
					appointment.setAppointmentState(new AppointmentState(3,
							"UPCOMING"));
					appointment.setCreatedDate(new Date());
					appointment.setCreator(Context.getAuthenticatedUser());

					// Saving the appointment
					service.saveAppointment(appointment);
					log.info("__________________>>>>>>>>>>>>>>>>>> service.saveAppointment(appointment);"
							+ service.getAllAppointments().size());
					saved = true;

					if (saved == true && stateChanged == false) {

						// Updating the Appointment State in the DB (moving from
						// Null to Confirmed)
						// appointment.getState().confirmed();
						// appointment.getState().upcoming();
						log.info("__________________>>>>>>>>>>>>>>>>>>"
								+ " appointment.getState().confirmed();"
								+ appointment.getAppointmentState().toString());

						// service.updateAppointment(appointment);

						log.info("__________________>>>>>>>>> "
								+ "AFTER SAVING THE APPOINTMENT >>>>>>>>>"
								+ service.getAppointmentById(
										appointment.getAppointmentId())
										.toString());

						stateChanged = true;
						obsId = obs.getObsId().intValue();
					}

					if (stateChanged == true) {
						saved = false;
						log.info("__________________>>>>>>>>>>>>>>>>>> saved = false;");
					}
				}
			}
		} else
			return;

	}

	@SuppressWarnings("unused")
	private void handleSavePrimaryCareObs(Object returnVal, Method method,
			Object[] args) {

		Encounter encounter;
		if (method.getName().equals("saveObs")) {

			// Setting the parameter from the ObsService.saveObs method

			Obs obs = (Obs) returnVal;
			Appointment appointment = null;

			// 1. Getting the Obs associated to the Primary Care Service:
			if (obs != null) {
				encounter = obs.getEncounter();
				if (obs.getConcept().getConceptId() == ConstantValues.PRIMARY_CARE_SERVICE_REQUESTED) {

					if (obs.getValueCoded() != null) {
						// && !encounterExist
						// && EncounterServiceAdvice.nextId <= 1) {
						log.info("***************APPOINTMENT"
								+ " SERVICE ConceptID***************"
								+ AppointmentUtil.getServiceByConcept(
										obs.getValueCoded()).toString());
						appointment = new Appointment();
						Services serv = AppointmentUtil.getServiceByConcept(obs
								.getValueCoded());

						// Setting the appointment attributes
						appointment.setPatient(encounter.getPatient());
						appointment.setLocation(encounter.getLocation());
						appointment.setProvider(Context.getPersonService()
								.getPerson(
										encounter.getProvider().getPersonId()));
						appointment.setService(serv);
						appointment.setEncounter(encounter);
						appointment.setAppointmentDate(encounter
								.getEncounterDatetime());

						// We don't need this as this is a waiting
						appointment.setNextVisitDate(null);
						appointment.setReason(obs);

						// Setting the AppointmentState to WAITING
						appointment.setAppointmentState(new AppointmentState(4,
								"WAITING"));
						appointment.setCreatedDate(new Date());
						appointment.setCreator(Context.getAuthenticatedUser());
						appointment.setAttended(false);
						appointment.setVoided(false);

						// Saving the appointment
						IAppointmentService service = Context
								.getService(IAppointmentService.class);
						service.saveAppointment(appointment);

						return;
					}
				}
			}

		}
	}

}
