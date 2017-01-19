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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.guice.RequestScoped;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * @author Kamonyo
 * 
 *         AOP class used to insert an appointment when the EncounterService
 *         methods saves an Encounter
 */
@RequestScoped
public class PatientServiceAdvice implements AfterReturningAdvice {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see org.springframework.aop.AfterReturningAdvice#afterReturning(Object,
	 *      Method, Object[], Object)
	 */
	@Override
	public void afterReturning(Object returnVal, Method method, Object[] args,
			Object target) throws Throwable {

//		if (method.getName().equals("savePatient")) {
//			String[] splits = returnVal.toString().split("#");
//
//			Integer patientId = Integer.parseInt(splits[1]);
//			Patient patient = Context.getPatientService().getPatient(patientId);
//
//			/** Getting the Patient Identifier from the system **/
//			PatientIdentifier pi = InsurancePolicyUtil
//					.getPrimaryPatientIdentifierForLocation(patient,
//							InsurancePolicyUtil.getLocationLoggedIn());
//
//			/**
//			 * This is only executed when the Patient Identifier type is PRIMARY
//			 * CARE TYPE and the insurance card does not exist
//			 */			
//			if (InsurancePolicyUtil.insuranceDoesNotExist(patient)
//					&& InsurancePolicyUtil
//							.getPrimaryPatientIdentifierForLocation(patient,
//									InsurancePolicyUtil.getLocationLoggedIn()) != null) {
//
//				InsurancePolicy policy = new InsurancePolicy();
//
//				policy.setCoverageStartDate(new Date());
//				policy.setExpirationDate(InsurancePolicyUtil.addYears(
//						new Date(), 20));
//				policy.setInsurance(InsuranceUtil.getInsuranceByConcept(Context
//						.getConceptService().getConcept(
//								BillingConstants.NONE_CONCEPT_ID)));// TODO: to be changed to GP...
////				policy.setHasThirdPart(false);// Just added the default value of
//												// THIRD PART
//				policy.setOwner(patient);
//
//				InsurancePolicyUtil.createInsurancePolicy(policy);
//			}
//		}
	}
}
