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
package org.openmrs.module.mohbilling.advice;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.guice.RequestScoped;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
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

		createInsurancePolicyCard(returnVal, method, args);
	}

	/**
	 * Gets an Encounter while Primary Care orientation is done and create
	 * Insurance Policy Card.
	 * 
	 * @param returnVal
	 *            the returned Encounter when saved
	 * @param method
	 *            the name of the Method to catch
	 * @param args
	 *            the arguments passed to the method to catch
	 */
	private void createInsurancePolicyCard(Object returnVal, Method method,
			Object[] args) {

		Encounter encounter;
		if (method.getName().equals("saveEncounter")) {

			// Setting the parameter from the EncounterService.saveEncounter
			// method
			encounter = (Encounter) returnVal;

			// 1. Getting the Obs associated to the encounter:

			if (encounter.getObs() != null
					&& encounter.getEncounterId().intValue() != encounterId)
				for (Obs obs : encounter.getObs()) {

					if (obs.getConcept().getConceptId() == BillingConstants.PRIMARY_CARE_INSURANCE_COVERAGE_START_DATE) {
						// TODO:
						// Avoiding to save the Appointment many times
						if (obs.getObsDatetime() != null) {

							;
						}
					}
					if (obs.getConcept().getConceptId() == BillingConstants.PRIMARY_CARE_INSURANCE_EXPIRATION_DATE) {
						// TODO:
						// Avoiding to save the Appointment many times
						if (obs.getObsDatetime() != null) {

							;
						}
					}
				}
		}
	}

}
