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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.InsuranceBill;
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

import java.util.Date;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/insuranceBill",
        supportedClass = InsuranceBill.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class InsuranceBillResource extends DelegatingCrudResource<InsuranceBill> {
    @Override
    protected String getUniqueId(InsuranceBill delegate) {
        return String.valueOf(delegate.getInsuranceBillId());
    }

    @Override
    public InsuranceBill getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    protected void delete(InsuranceBill insuranceBill, String s, RequestContext requestContext) throws ResponseException {
        insuranceBill.setVoided(true);
        insuranceBill.setVoidedBy(Context.getAuthenticatedUser());
        insuranceBill.setVoidedDate(new Date());
        insuranceBill.setVoidReason(s);
        Context.getService(BillingService.class).saveInsuranceBill(insuranceBill);
    }

    @Override
    public InsuranceBill newDelegate() {
        return new InsuranceBill();
    }

    @Override
    public InsuranceBill save(InsuranceBill insuranceBill) {
        Context.getService(BillingService.class).saveInsuranceBill(insuranceBill);
        return insuranceBill;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("insuranceBillId", new IntegerProperty())
                    .property("insurance", new RefProperty("#/definitions/MohbillingInsuranceGet"))
                    .property("billIdentifier", new StringProperty())
                    .property("amount", new DecimalProperty())
                    .property("dueAmount", new DecimalProperty())
                    .property("paidAmount", new DecimalProperty())
                    .property("billingMonth", new DateProperty())
                    .property("status", new StringProperty());
        }
        if (rep instanceof FullRepresentation) {
            model
                    .property("globalBills", new ArrayProperty(new RefProperty("#/definitions/MohbillingGlobalBillGet")))
                    .property("creator", new RefProperty("#/definitions/UserGet"))
                    .property("createdDate", new DateTimeProperty());
        }
        return model;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("insurance", new ObjectProperty()
                        .property("insuranceId", new IntegerProperty()
                                .description("insuranceId of the insurance"))
                        .description("Insurance reference object"))
                .property("billIdentifier", new StringProperty())
                .property("amount", new DecimalProperty())
                .property("billingMonth", new DateProperty());

        model.required("insurance")
                .required("amount")
                .required("billingMonth");

        return model;
    }

    @Override
    public void purge(InsuranceBill insuranceBill, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(insuranceBill);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("amount");
            description.addProperty("creator");
            description.addProperty("createdDate");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("amount");
            description.addProperty("creator");
            description.addProperty("createdDate");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }
}
