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
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/consommation",
        supportedClass = Consommation.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class ConsommationResource extends DelegatingCrudResource<Consommation> {

    @Override
    protected String getUniqueId(Consommation delegate) {
        return String.valueOf(delegate.getConsommationId());
    }

    @Override
    public Consommation getByUniqueId(String s) {
        return Context.getService(BillingService.class).getConsommation(Integer.parseInt(s));
    }

    @Override
    protected void delete(Consommation consommation, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Consommation newDelegate() {
        return new Consommation();
    }

    @Override
    public Consommation save(Consommation consommation) {
        Context.getService(BillingService.class).saveConsommation(consommation);
        return consommation;
    }

    @Override
    public void purge(Consommation consommation, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("consommationId");
            description.addProperty("department", Representation.REF);
            description.addProperty("billItems", Representation.REF);
            description.addProperty("patientBill", Representation.REF);
            description.addProperty("insuranceBill", Representation.REF);
            description.addProperty("thirdPartyBill", Representation.REF);
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("consommationId");
            description.addProperty("department", Representation.REF);
            description.addProperty("billItems");
            description.addProperty("patientBill", Representation.REF);
            description.addProperty("insuranceBill", Representation.REF);
            description.addProperty("thirdPartyBill", Representation.REF);
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String globalBillId = context.getRequest().getParameter("globalBillId");
        List<Consommation> consommations = new ArrayList<>();
        if (globalBillId != null) {
            GlobalBill globalBill = GlobalBillUtil.getGlobalBill(Integer.valueOf(globalBillId));
            consommations = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
        }
        return new NeedsPaging<>(consommations, context);
    }
}
