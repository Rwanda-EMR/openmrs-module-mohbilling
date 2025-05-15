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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/insurancePolicy",
        supportedClass = InsurancePolicy.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class InsurancePolicyResource extends DelegatingCrudResource<InsurancePolicy> {

    @Override
    protected String getUniqueId(InsurancePolicy delegate) {
        return String.valueOf(delegate.getInsurancePolicyId());
    }

    @Override
    public InsurancePolicy getByUniqueId(String s) {
        return Context.getService(BillingService.class).getInsurancePolicy(Integer.parseInt(s));
    }

    @Override
    protected void delete(InsurancePolicy insurancePolicy, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public InsurancePolicy newDelegate() {
        return new InsurancePolicy();
    }

    @Override
    public InsurancePolicy save(InsurancePolicy insurancePolicy) {
        if (insurancePolicy.getCreator() == null) {
            insurancePolicy.setCreator(Context.getAuthenticatedUser());
        }

        if (insurancePolicy.getCreatedDate() == null) {
            insurancePolicy.setCreatedDate(new Date());
        }

        Context.getService(BillingService.class).saveInsurancePolicy(insurancePolicy);
        return insurancePolicy;
    }

    @Override
    public void purge(InsurancePolicy insurancePolicy, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("insurance");
        description.addProperty("owner");
        description.addProperty("insuranceCardNo");
        description.addProperty("coverageStartDate");
        description.addProperty("expirationDate");
        description.addProperty("thirdParty");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceCardNo");
            description.addProperty("coverageStartDate");
            description.addProperty("expirationDate");
            description.addProperty("owner", Representation.REF);
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceCardNo");
            description.addProperty("coverageStartDate");
            description.addProperty("expirationDate");
            description.addProperty("owner");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String patientId = context.getRequest().getParameter("patientId");
        String insuranceCardNo = context.getRequest().getParameter("insuranceCardNo");
        List<InsurancePolicy> insurancePolicies = Collections.emptyList();
        if (patientId != null) {
            insurancePolicies = Context.getService(BillingService.class)
                    .getAllInsurancePoliciesByPatient(Context.getPatientService().getPatient(Integer.valueOf(patientId)));
        }

        if (insuranceCardNo != null) {
            InsurancePolicy insurancePolicy = Context.getService(
                    BillingService.class).getInsurancePolicyByCardNo(insuranceCardNo);
            insurancePolicies = new ArrayList<>();
            insurancePolicies.add(insurancePolicy);
        }

        return new NeedsPaging<>(insurancePolicies, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllInsurancePolicies(), context);
    }
}
