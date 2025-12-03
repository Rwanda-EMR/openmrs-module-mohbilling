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
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/thirdPartyBill",
        supportedClass = ThirdPartyBill.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class ThirdPartyBillResource extends DelegatingCrudResource<ThirdPartyBill> {
    @Override
    protected String getUniqueId(ThirdPartyBill delegate) {
        return String.valueOf(delegate.getThirdPartyBillId());
    }

    @Override
    public ThirdPartyBill getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    protected void delete(ThirdPartyBill thirdPartyBill, String s, RequestContext requestContext) throws ResponseException {
        thirdPartyBill.setVoided(true);
        thirdPartyBill.setVoidedBy(Context.getAuthenticatedUser());
        thirdPartyBill.setVoidedDate(new Date());
        thirdPartyBill.setVoidReason(s);
        Context.getService(BillingService.class).saveThirdPartyBill(thirdPartyBill);
    }

    @Override
    public ThirdPartyBill newDelegate() {
        return new ThirdPartyBill();
    }

    @Override
    public ThirdPartyBill save(ThirdPartyBill thirdPartyBill) {
        Context.getService(BillingService.class).saveThirdPartyBill(thirdPartyBill);
        return thirdPartyBill;
    }

    @Override
    public void purge(ThirdPartyBill thirdPartyBill, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(thirdPartyBill);
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("thirdPartyBillId", new IntegerProperty())
                    .property("thirdParty", new RefProperty("#/definitions/MohbillingThirdPartyGet"))
                    .property("insuranceBill", new RefProperty("#/definitions/MohbillingInsuranceBillGet"))
                    .property("amount", new DecimalProperty())
                    .property("dueAmount", new DecimalProperty())
                    .property("paidAmount", new DecimalProperty());
        }
        if (rep instanceof FullRepresentation) {
            model
                    .property("creator", new RefProperty("#/definitions/UserGet"))
                    .property("createdDate", new DateTimeProperty());
        }
        return model;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("thirdParty", new ObjectProperty()
                        .property("thirdPartyId", new IntegerProperty()
                                .description("ID of the third party"))
                        .description("Third party reference object"))
                .property("insuranceBill", new ObjectProperty()
                        .property("insuranceBillId", new IntegerProperty()
                                .description("ID of the insurance bill"))
                        .description("Insurance bill reference object"))
                .property("amount", new DecimalProperty()
                        .example("2000.00"));

        model.required("thirdParty")
                .required("insuranceBill")
                .required("amount");

        return model;
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
