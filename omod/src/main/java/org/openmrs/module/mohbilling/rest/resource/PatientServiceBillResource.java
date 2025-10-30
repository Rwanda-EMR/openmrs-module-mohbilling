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
import org.openmrs.module.mohbilling.model.PatientServiceBill;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/patientServiceBill",
        supportedClass = PatientServiceBill.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class PatientServiceBillResource extends DelegatingCrudResource<PatientServiceBill> {
    @Override
    protected String getUniqueId(PatientServiceBill delegate) {
        return String.valueOf(delegate.getPatientServiceBillId());
    }

    @Override
    public PatientServiceBill getByUniqueId(String s) {
        return Context.getService(BillingService.class).getPatientServiceBill(Integer.valueOf(s));
    }

    @Override
    protected void delete(PatientServiceBill patientServiceBill, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public PatientServiceBill newDelegate() {
        return new PatientServiceBill();
    }

    @Override
    public PatientServiceBill save(PatientServiceBill patientServiceBill) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void purge(PatientServiceBill patientServiceBill, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("serviceDate");
        description.addProperty("service");
        description.addProperty("hopService");
        description.addProperty("unitPrice");
        description.addProperty("quantity");
        description.addProperty("paidQuantity");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("patientServiceBillId", new IntegerProperty())
                    .property("service", new RefProperty("#/definitions/MohbillingBillableServiceGet"))
                    .property("serviceDate", new DateTimeProperty())
                    .property("unitPrice", new DecimalProperty())
                    .property("quantity", new DecimalProperty())
                    .property("consommation", new RefProperty("#/definitions/MohbillingConsommationGet"))
                    .property("serviceOther", new StringProperty())
                    .property("serviceOtherDescription", new StringProperty())
                    .property("itemType", new IntegerProperty());
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
                .property("service", new ObjectProperty()
                        .property("serviceId", new IntegerProperty()
                                .description("ID of the service"))
                        .description("Service reference object"))
                .property("serviceDate", new DateTimeProperty())
                .property("unitPrice", new DecimalProperty()
                        .example("1000.00"))
                .property("quantity", new DecimalProperty())
                .property("consommation", new ObjectProperty()
                        .property("consommationId", new IntegerProperty()
                                .description("ID of the consommation"))
                        .description("Consommation reference object"))
                .property("serviceOther", new StringProperty())
                .property("serviceOtherDescription", new StringProperty())
                .property("itemType", new IntegerProperty());

        model.required("serviceDate")
                .required("unitPrice")
                .required("consommation");

        return model;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceDate");
            description.addProperty("service", Representation.REF);
            description.addProperty("hopService", Representation.REF);
            description.addProperty("unitPrice");
            description.addProperty("quantity");
            description.addProperty("paidQuantity");
            description.addProperty("paid");
            description.addProperty("serviceOther");
            description.addProperty("serviceOtherDescription");
            description.addProperty("drugFrequency");
            description.addProperty("itemType");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceDate");
            description.addProperty("service");
            description.addProperty("hopService");
            description.addProperty("unitPrice");
            description.addProperty("quantity");
            description.addProperty("paidQuantity");
            description.addProperty("paid");
            description.addProperty("serviceOther");
            description.addProperty("serviceOtherDescription");
            description.addProperty("drugFrequency");
            description.addProperty("itemType");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @PropertySetter("unitPrice")
    public static void setUnitPrice(PatientServiceBill patientServiceBill, Object value) {
        patientServiceBill.setUnitPrice(BigDecimal.valueOf((Double) value));
    }

    @PropertySetter("quantity")
    public static void setQuantity(PatientServiceBill patientServiceBill, Object value) {
        patientServiceBill.setQuantity(BigDecimal.valueOf((Integer) value));
    }

    @PropertySetter("paidQuantity")
    public static void setPaidQuantity(PatientServiceBill patientServiceBill, Object value) {
        patientServiceBill.setPaidQuantity(BigDecimal.valueOf((Integer) value));
    }
}
