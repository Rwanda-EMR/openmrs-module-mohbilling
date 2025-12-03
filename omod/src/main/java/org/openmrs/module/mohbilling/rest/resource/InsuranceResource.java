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
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.BillingUtils;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
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

import java.util.Date;
import java.util.Set;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/insurance",
        supportedClass = Insurance.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class InsuranceResource extends DelegatingCrudResource<Insurance> {

    @Override
    protected String getUniqueId(Insurance delegate) {
        return String.valueOf(delegate.getInsuranceId());
    }

    @Override
    public Insurance getByUniqueId(String insuranceId) {
        return Context.getService(BillingService.class).getInsurance(Integer.parseInt(insuranceId));
    }

    @Override
    protected void delete(Insurance insurance, String s, RequestContext requestContext) throws ResponseException {
        insurance.setVoided(true);
        insurance.setVoidedBy(Context.getAuthenticatedUser());
        insurance.setVoidedDate(new Date());
        insurance.setVoidReason(s);
        Context.getService(BillingService.class).saveInsurance(insurance);
    }

    @Override
    public Insurance newDelegate() {
        return new Insurance();
    }

    @Override
    public Insurance save(Insurance insurance) {
        if (insurance.getCreator() == null) {
            insurance.setCreator(Context.getAuthenticatedUser());
        }

        if (insurance.getCreatedDate() == null) {
            insurance.setCreatedDate(new Date());
        }

        if (insurance.isVoided() && insurance.getVoidedBy() == null) {
            insurance.setVoidedBy(Context.getAuthenticatedUser());
            insurance.setVoidedDate(new Date());
        }

        Context.getService(BillingService.class).saveInsurance(insurance);
        return insurance;
    }

    @Override
    public void purge(Insurance insurance, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(insurance);
    }

    @PropertyGetter("rate")
    public Float getRate(Insurance insurance) {
        InsuranceRate currentRate = insurance.getCurrentRate();
        return (currentRate != null) ? currentRate.getRate() : null;
    }

    @PropertyGetter("flatFee")
    public String getFlatFee(Insurance insurance) {
        InsuranceRate currentRate = insurance.getCurrentRate();
        return (currentRate != null && currentRate.getFlatFee() != null) ?
                currentRate.getFlatFee().toString() : null;
    }


    @PropertyGetter("voided")
    public Boolean getVoided(Insurance insurance) {
        return insurance.isVoided();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("name");
        description.addProperty("address");
        description.addProperty("phone");
        description.addProperty("concept");
        description.addProperty("category");
        description.addProperty("rates");
        description.addProperty("categories");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("insuranceId", new IntegerProperty())
                    .property("name", new StringProperty())
                    .property("address", new StringProperty())
                    .property("phone", new StringProperty())
                    .property("category", new StringProperty())
                    .property("rate", new FloatProperty()
                            .description("Current insurance coverage rate"))
                    .property("flatFee", new DecimalProperty()
                            .description("Current flat fee amount"));
        }
        if (rep instanceof FullRepresentation) {
            model
                    .property("concept", new RefProperty("#/definitions/ConceptGet"))
                    .property("creator", new RefProperty("#/definitions/UserGet"))
                    .property("createdDate", new DateTimeProperty())
                    .property("voided", new BooleanProperty())
                    .property("voidedBy", new RefProperty("#/definitions/UserGet"))
                    .property("voidedDate", new DateTimeProperty())
                    .property("voidReason", new StringProperty());
        }
        return model;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("name", new StringProperty())
                .property("address", new StringProperty())
                .property("phone", new StringProperty())
                .property("concept", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the concept"))
                        .description("Concept reference object"))
                .property("rates", new ArrayProperty(
                        new ObjectProperty()
                                .property("rate", new FloatProperty())
                                .property("flatFee", new DecimalProperty())
                                .property("startDate", new DateTimeProperty())))
                .property("categories", new ArrayProperty(new StringProperty()));

        model.required("name");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("name", new StringProperty())
                .property("address", new StringProperty())
                .property("phone", new StringProperty())
                .property("concept", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the concept"))
                        .description("Concept reference object"))
                .property("rates", new ArrayProperty(
                        new ObjectProperty()
                                .property("rate", new FloatProperty())
                                .property("flatFee", new DecimalProperty())
                                .property("startDate", new DateTimeProperty())))
                .property("categories", new ArrayProperty(new StringProperty()))
                .property("voided", new BooleanProperty())
                .property("voidReason", new StringProperty());

        return model;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("name");
        description.addProperty("address");
        description.addProperty("phone");
        description.addProperty("concept");
        description.addProperty("rates");
        description.addProperty("categories");
        description.addProperty("voided");
        description.addProperty("voidReason");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceId");
            description.addProperty("name");
            description.addProperty("address");
            description.addProperty("phone");
            description.addProperty("category");
            description.addProperty("rate");
            description.addProperty("flatFee");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceId");
            description.addProperty("name");
            description.addProperty("address");
            description.addProperty("phone");
            description.addProperty("category");
            description.addProperty("rate");
            description.addProperty("flatFee");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        } else if (representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("insuranceId");
            description.addProperty("name");
            description.addProperty("address");
            description.addProperty("phone");
            description.addProperty("category");
            description.addProperty("rate");
            description.addProperty("flatFee");

            description.addProperty("concept", Representation.REF);
            description.addProperty("creator", Representation.REF);
            description.addProperty("createdDate");
            description.addProperty("voided");
            description.addSelfLink();
        }

        return description;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllInsurances(context.getIncludeAll()), context);
    }

    @PropertySetter("voided")
    public static void setVoided(Insurance insurance, Object value) {
        insurance.setVoided(Boolean.valueOf(String.valueOf(value)));
    }

    @PropertySetter("rates")
    public static void setRates(Insurance insurance, Set<InsuranceRate> rates) {
        rates.stream().forEach(rate -> {
            if (rate.getCreator() == null) {
                rate.setCreator(Context.getAuthenticatedUser());
            }
            if (rate.getCreatedDate() == null) {
                rate.setCreatedDate(new Date());
            }
            rate.setFlatFee(BillingUtils.convertRawValueToBigDecimal(rate.getFlatFee()));
            insurance.addInsuranceRate(rate);
        });
    }

    @PropertySetter("categories")
    public static void setCategories(Insurance insurance, Set<ServiceCategory> categories) {
        categories.stream().forEach(category -> {
            if (category.getCreator() == null) {
                category.setCreator(Context.getAuthenticatedUser());
            }
            if (category.getCreatedDate() == null) {
                category.setCreatedDate(new Date());
            }
            insurance.addServiceCategory(category);
        });
    }
}
