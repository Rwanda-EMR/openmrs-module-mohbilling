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
package org.openmrs.module.mohappointment.advice;

import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.guice.RequestScoped;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.model.AppointmentState;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.module.mohappointment.utils.ConstantValues;
import org.springframework.aop.AfterReturningAdvice;

/**
 * @author Kamonyo
 * 
 *         AOP class used to insert an appointment when the EncounterService
 *         methods saves an Encounter
 */
@RequestScoped
public class EncounterServiceAdvice implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());
	private static int encounterId = 0;

	/**
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(Object,
	 *      Method, Object[], Object)
	 */
	@Override
	public void afterReturning(Object returnVal, Method method, Object[] args,
			Object target) throws Throwable {
		//handleSavePrimaryCareEncounter(returnVal, method, args);

		//handleSaveNextVisitEncounter(returnVal, method, args);
	}

	/**
	 * Gets an Encounter while Primary Care orientation is done and saves it to
	 * the waiting list for a specific service
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
	private Patient handleSavePrimaryCareEncounter(Object returnVal,
			Method method, Object[] args) {

		Encounter encounter;
		if (method.getName().equals("saveEncounter")) {

			// Setting the parameter from the EncounterService.saveEncounter
			// method
			encounter = (Encounter) returnVal;

			Appointment appointment = null;

			// 1. Getting the Obs associated to the encounter:

			if (encounter.getObs() != null
					&& encounter.getEncounterId().intValue() != encounterId)
				for (Obs obs : encounter.getObs()) {

					if (obs.getConcept().getConceptId() == ConstantValues.PRIMARY_CARE_SERVICE_REQUESTED) {

						// Avoiding to save the Appointment many times
						if (obs.getValueCoded() != null) {

							appointment = new Appointment();
							Services serv = AppointmentUtil
									.getServiceByConcept(obs.getValueCoded());

							// Setting the appointment attributes
							appointment.setPatient(encounter.getPatient());
							appointment.setLocation(encounter.getLocation());
							appointment.setProvider(Context.getPersonService()
									.getPerson(
											encounter.getProvider()
													.getPersonId()));
							appointment.setService(serv);
							appointment.setEncounter(encounter);
							appointment.setAppointmentDate(encounter
									.getEncounterDatetime());

							// We don't need this as this is a waiting
							appointment.setNextVisitDate(null);
							appointment.setReason(obs);

							// Setting the AppointmentState to WAITING
							appointment
									.setAppointmentState(new AppointmentState(
											4, "WAITING"));
							appointment.setCreatedDate(new Date());
							appointment.setCreator(Context
									.getAuthenticatedUser());
							appointment.setAttended(false);
							appointment.setVoided(false);

							// Saving the appointment
							IAppointmentService service = Context
									.getService(IAppointmentService.class);
							service.saveAppointment(appointment);

							encounterId = encounter.getEncounterId().intValue();

							return appointment.getPatient();
						}
					}
				}
		}
		return null;
	}

	/**
	 * Gets an Obs or Encounter while anywhere a SaveObs action or SaveEncounter
	 * is done and saves it to the upcoming list
	 * 
	 * @param returnVal the value to be returned by the methods
	 * 
	 * @param returnVal
	 *            the returned Encounter when saved
	 * @param method
	 *            the name of the Method to catch
	 * @param args
	 *            the arguments passed to the method to catch
	 */
	private void handleSaveNextVisitEncounter(Object returnVal, Method method,
			Object[] args) {
		boolean stateChanged = false;
		boolean saved = false;
		IAppointmentService service = Context
				.getService(IAppointmentService.class);
		Obs nextVisitDate = null;
		Obs reasonForVisit = null;

		/**
		 * Setting up an appointment using Save Encounter method advice
		 */

		if (method.getName().equals("saveEncounter")) {

			int count1 = 0;
			count1++;
			if (count1 <= 2) {
				// Setting the parameter from the EncounterService.saveEncounter
				// method
				Encounter encounter = (Encounter) args[0];
				Appointment appointment = null;
				boolean appointmentFound = false;
				// 1. Getting the Obs associated to the encounter:
				if (encounter.getObs() != null)
					for (Obs obs : encounter.getObs()) {

						if (obs.getConcept().getConceptId() == ConstantValues.NEXT_SCHEDULED_VISIT) {

							nextVisitDate = obs;
							appointmentFound = true;
							log
									.info("__________________>>>>>>>>>>>>>>>>>> NEXT_SCHEDULED_VISIT");
						}

						if (obs.getConcept().getConceptId() == ConstantValues.REASON_FOR_VISIT) {

							reasonForVisit = obs;
							log
									.info("__________________>>>>>>>>>>>>>>>>>> REASON_FOR_VISIT");
						}
					}

				if (appointmentFound) {

					appointment = new Appointment();

					// Setting the appointment attributes
					appointment.setPatient(encounter.getPatient());
					appointment.setLocation(encounter.getLocation());
					appointment.setProvider(encounter.getProvider());
					appointment.setEncounter(encounter);
					appointment.setAppointmentDate(nextVisitDate
							.getValueDatetime());
					appointment.setNextVisitDate(nextVisitDate);
					log
							.info("_________________>>>>>>>>>>>>>>>> Next Visit date obs id : "
									+ nextVisitDate.getEncounter()
											.getEncounterId());
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
					log
							.info("__________________>>>>>>>>>>>>>>>>>> service.saveAppointment(appointment);"
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
					}

					if (stateChanged == true) {
						saved = false;
						log
								.info("__________________>>>>>>>>>>>>>>>>>> saved = false;");
					}
				}
			}
		} else
			return;
	}

}
