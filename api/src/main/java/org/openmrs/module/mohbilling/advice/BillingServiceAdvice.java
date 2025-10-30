/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohbilling.advice;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.guice.RequestScoped;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.aop.AfterReturningAdvice;

@RequestScoped
public class BillingServiceAdvice implements AfterReturningAdvice {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void afterReturning(Object returnVal, Method method, Object[] args, Object target) throws Throwable {
        log.info("Inside BillingServiceAdvice afterReturning method");
        if (method.getName().equals("saveAdmission") && args.length > 0 && args[0] instanceof Admission) {
            Admission admission = (Admission) returnVal;

            Integer insurancePolicyId = admission.getInsurancePolicy().getInsurancePolicyId();

            InsurancePolicy insurancePolicy = Context.getService(BillingService.class).getInsurancePolicy(insurancePolicyId);

            Patient patient = insurancePolicy.getOwner();
            int admissionType = admission.getAdmissionType();
            VisitService visitService = Context.getVisitService();

            List<Visit> visits = visitService.getActiveVisitsByPatient(patient);
            if (CollectionUtils.isEmpty(visits)) {
                VisitType visitType = visitService.getVisitType(admissionType);
                if (visitType == null) {
                    visitType = visitService.getAllVisitTypes().get(0);
                }

                Visit visit = new Visit(patient, visitType, admission.getAdmissionDate());
                visit.setLocation(Context.getUserContext().getLocation());
                visitService.saveVisit(visit);
            }
        }
        log.info("Exiting BillingServiceAdvice afterReturning method");
    }
}
