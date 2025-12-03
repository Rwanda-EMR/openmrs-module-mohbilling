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
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.math.BigDecimal;
import java.util.Date;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/paidServiceBill",
        supportedClass = PaidServiceBill.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class PaidServiceBillResource extends DelegatingCrudResource<PaidServiceBill> {
    @Override
    protected String getUniqueId(PaidServiceBill delegate) {
        return String.valueOf(delegate.getPaidServiceBillId());
    }

    @Override
    public PaidServiceBill getByUniqueId(String s) {
        return Context.getService(BillingService.class).getPaidServiceBill(Integer.valueOf(s));
    }

    @Override
    protected void delete(PaidServiceBill paidServiceBill, String s, RequestContext requestContext) throws ResponseException {
        paidServiceBill.setVoided(true);
        paidServiceBill.setVoidedBy(Context.getAuthenticatedUser());
        paidServiceBill.setVoidedDate(new Date());
        paidServiceBill.setVoidReason(s);
        Context.getService(BillingService.class).savePaidServiceBill(paidServiceBill);
    }

    @Override
    public PaidServiceBill newDelegate() {
        return new PaidServiceBill();
    }

    @Override
    public PaidServiceBill save(PaidServiceBill paidServiceBill) {
        if (paidServiceBill.getCreator() == null) {
            paidServiceBill.setCreator(Context.getAuthenticatedUser());
        }
        Context.getService(BillingService.class).savePaidServiceBill(paidServiceBill);
        return paidServiceBill;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("paidServiceBillId", new IntegerProperty())
                    .property("patientServiceBill", new RefProperty("#/definitions/MohbillingPatientServiceBillGet"))
                    .property("insuranceBill", new RefProperty("#/definitions/MohbillingInsuranceBillGet"))
                    .property("amountPaid", new DecimalProperty());
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
                .property("patientServiceBill", new ObjectProperty()
                        .property("patientServiceBillId", new IntegerProperty()
                                .description("ID of the patient service bill"))
                        .description("Patient service bill reference object"))
                .property("insuranceBill", new ObjectProperty()
                        .property("insuranceBillId", new IntegerProperty()
                                .description("ID of the insurance bill"))
                        .description("Insurance bill reference object"))
                .property("amountPaid", new DecimalProperty()
                        .example("1000.00"));

        model.required("patientServiceBill")
                .required("insuranceBill")
                .required("amountPaid");

        return model;
    }

    @Override
    public void purge(PaidServiceBill paidServiceBill, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(paidServiceBill);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("paidQty");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("paidQty");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @PropertySetter("paidQty")
    public static void setPaidQty(PaidServiceBill paidServiceBill, Object value) {
        paidServiceBill.setPaidQty(new BigDecimal((Integer) value));
    }
}
