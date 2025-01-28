/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.rest.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/admission",
        supportedClass = Admission.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class AdmissionResource extends DelegatingCrudResource<Admission> {

    @Override
    protected String getUniqueId(Admission delegate) {
        return String.valueOf(delegate.getAdmissionId());
    }

    @Override
    public Admission getByUniqueId(String s) {
        return Context.getService(BillingService.class).getPatientAdmission(Integer.parseInt(s));
    }

    @Override
    protected void delete(Admission admission, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Admission newDelegate() {
        return new Admission();
    }

    @Override
    public Admission save(Admission admission) {
        return Context.getService(BillingService.class).saveAdmission(admission);
    }

    @Override
    public void purge(Admission admission, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insurancePolicy", Representation.REF);
            description.addProperty("isAdmitted");
            description.addProperty("admissionDate");
            description.addProperty("dischargingDate");
            description.addProperty("diseaseType");
            description.addProperty("admissionType");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insurancePolicy", Representation.REF);
            description.addProperty("isAdmitted");
            description.addProperty("admissionDate");
            description.addProperty("dischargingDate");
            description.addProperty("diseaseType");
            description.addProperty("admissionType");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }
}
