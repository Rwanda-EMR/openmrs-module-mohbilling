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
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.BillingUtils;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
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

import java.util.stream.Collectors;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/insuranceRate",
        supportedClass = InsuranceRate.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class InsuranceRateResource extends DelegatingCrudResource<InsuranceRate> {
    @Override
    protected String getUniqueId(InsuranceRate delegate) {
        return String.valueOf(delegate.getInsuranceRateId());
    }

    @Override
    public InsuranceRate getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    protected void delete(InsuranceRate insuranceRate, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public InsuranceRate newDelegate() {
        return new InsuranceRate();
    }

    @Override
    public InsuranceRate save(InsuranceRate insuranceRate) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void purge(InsuranceRate insuranceRate, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("insurance");
        description.addProperty("rate");
        description.addProperty("flatFee");
        description.addProperty("startDate");
        description.addProperty("endDate");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("insuranceRateId", new IntegerProperty())
                    .property("insurance", new RefProperty("#/definitions/MohbillingInsuranceGet"))
                    .property("rate", new FloatProperty()
                            .description("Coverage rate as decimal (e.g., 0.85 for 85%)"))
                    .property("flatFee", new DecimalProperty()
                            .description("Flat fee amount"))
                    .property("startDate", new DateTimeProperty())
                    .property("endDate", new DateTimeProperty());
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
                .property("insurance", new ObjectProperty()
                        .property("insuranceId", new IntegerProperty()
                                .description("ID of the insurance"))
                        .description("Insurance reference object"))
                .property("rate", new FloatProperty()
                        .example(0.85f)
                        .description("Coverage rate as decimal (e.g., 0.85 for 85%)"))
                .property("flatFee", new DecimalProperty()
                        .example("500.00")
                        .description("Flat fee amount"))
                .property("startDate", new DateTimeProperty())
                .property("endDate", new DateTimeProperty());

        model.required("insurance")
                .required("rate")
                .required("startDate");

        return model;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceRateId");
            description.addProperty("insurance", Representation.REF);
            description.addProperty("rate");
            description.addProperty("flatFee");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceRateId");
            description.addProperty("insurance");
            description.addProperty("rate");
            description.addProperty("flatFee");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addSelfLink();
        }

        return description;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllInsurances().stream()
                .map(insurance -> insurance.getRates()).collect(Collectors.toList()), context);
    }

    @PropertySetter("flatFee")
    public static void setFlatFee(InsuranceRate insuranceRate, Object value) {
        insuranceRate.setFlatFee(BillingUtils.convertRawValueToBigDecimal(value));
    }
}
