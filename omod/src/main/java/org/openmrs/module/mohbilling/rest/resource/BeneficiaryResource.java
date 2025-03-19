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

import java.util.Collections;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/beneficiary",
        supportedClass = Beneficiary.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BeneficiaryResource extends DelegatingCrudResource<Beneficiary> {
    @Override
    protected String getUniqueId(Beneficiary delegate) {
        return String.valueOf(delegate.getBeneficiaryId());
    }

    @Override
    public Beneficiary getByUniqueId(String s) {
        return Context.getService(BillingService.class).getBeneficiary(Integer.parseInt(s));
    }

    @Override
    protected void delete(Beneficiary beneficiary, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Beneficiary newDelegate() {
        return new Beneficiary();
    }

    @Override
    public Beneficiary save(Beneficiary beneficiary) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void purge(Beneficiary beneficiary, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("beneficiaryId");
            description.addProperty("insurancePolicy", Representation.REF);
            description.addProperty("policyIdNumber");
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("beneficiaryId");
            description.addProperty("insurancePolicy");
            description.addProperty("policyIdNumber");
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String insurancePolicyNumber = context.getRequest().getParameter("insurancePolicyNumber");
        Beneficiary beneficiary = Context.getService(BillingService.class).getBeneficiaryByPolicyNumber(insurancePolicyNumber);

        return new NeedsPaging<>(Collections.singletonList(beneficiary), context);
    }
}
