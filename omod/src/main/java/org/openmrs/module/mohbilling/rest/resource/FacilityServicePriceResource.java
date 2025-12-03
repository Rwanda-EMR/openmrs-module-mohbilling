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
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
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
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/facilityServicePrice",
        supportedClass = FacilityServicePrice.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class FacilityServicePriceResource extends DelegatingCrudResource<FacilityServicePrice> {
    @Override
    protected String getUniqueId(FacilityServicePrice delegate) {
        return String.valueOf(delegate.getFacilityServicePriceId());
    }

    @Override
    public FacilityServicePrice getByUniqueId(String s) {
        return Context.getService(BillingService.class).getFacilityServicePrice(Integer.valueOf(s));
    }

    @Override
    protected void delete(FacilityServicePrice facilityServicePrice, String s, RequestContext requestContext) throws ResponseException {
        facilityServicePrice.setRetired(true);
        facilityServicePrice.setRetiredBy(Context.getAuthenticatedUser());
        facilityServicePrice.setRetiredDate(new Date());
        facilityServicePrice.setRetireReason(s);
        Context.getService(BillingService.class).saveFacilityServicePrice(facilityServicePrice);
    }

    @Override
    public FacilityServicePrice newDelegate() {
        return new FacilityServicePrice();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("name");
        description.addRequiredProperty("fullPrice");
        description.addRequiredProperty("startDate");
        description.addRequiredProperty("location");
        description.addProperty("shortName");
        description.addProperty("description");
        description.addProperty("category");
        description.addProperty("endDate");
        description.addProperty("concept");
        description.addProperty("itemType");
        description.addProperty("hideItem");
        description.addProperty("hidden");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("facilityServicePriceId", new IntegerProperty())
                    .property("name", new StringProperty())
                    .property("shortName", new StringProperty())
                    .property("description", new StringProperty())
                    .property("category", new StringProperty())
                    .property("fullPrice", new DecimalProperty())
                    .property("startDate", new DateTimeProperty())
                    .property("endDate", new DateTimeProperty())
                    .property("location", new RefProperty("#/definitions/LocationGet"))
                    .property("concept", new RefProperty("#/definitions/ConceptGet"))
                    .property("hidden", new BooleanProperty())
                    .property("itemType", new IntegerProperty())
                    .property("hideItem", new IntegerProperty());
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
                .property("name", new StringProperty())
                .property("shortName", new StringProperty())
                .property("description", new StringProperty())
                .property("category", new StringProperty())
                .property("fullPrice", new DecimalProperty()
                        .example("1000.00"))
                .property("startDate", new DateTimeProperty())
                .property("endDate", new DateTimeProperty())
                .property("location", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the location"))
                        .description("Location reference object"))
                .property("concept", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the concept"))
                        .description("Concept reference object"))
                .property("hidden", new BooleanProperty()._default("false"))
                .property("itemType", new IntegerProperty())
                .property("hideItem", new IntegerProperty());

        model.required("name")
                .required("fullPrice")
                .required("startDate")
                .required("location");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return getCREATEModel(rep);
    }

    @Override
    public FacilityServicePrice save(FacilityServicePrice facilityServicePrice) {
        if (facilityServicePrice.getCreator() == null) {
            facilityServicePrice.setCreator(Context.getAuthenticatedUser());
        }
        if (facilityServicePrice.getCreatedDate() == null) {
            facilityServicePrice.setCreatedDate(new Date());
        }

        Context.getService(BillingService.class).saveFacilityServicePrice(facilityServicePrice);
        return facilityServicePrice;
    }

    @Override
    public void purge(FacilityServicePrice facilityServicePrice, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(facilityServicePrice);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("facilityServicePriceId");
            description.addProperty("name");
            description.addProperty("shortName");
            description.addProperty("description");
            description.addProperty("category");
            description.addProperty("fullPrice");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addProperty("location", Representation.REF);
            description.addProperty("concept", Representation.REF);
            description.addProperty("hidden");
            description.addProperty("itemType");
            description.addProperty("hideItem");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("facilityServicePriceId");
            description.addProperty("name");
            description.addProperty("shortName");
            description.addProperty("description");
            description.addProperty("category");
            description.addProperty("fullPrice");
            description.addProperty("startDate");
            description.addProperty("endDate");
            description.addProperty("location");
            description.addProperty("concept");
            description.addProperty("hidden");
            description.addProperty("itemType");
            description.addProperty("hideItem");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String conceptUuid = context.getRequest().getParameter("concept");
        String category = context.getRequest().getParameter("category");
        String hiddenParam = context.getRequest().getParameter("hidden");
        String qParam = context.getRequest().getParameter("q");

        if (conceptUuid != null) {
            Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
            FacilityServicePrice facilityServicePrice = Context.getService(BillingService.class).getFacilityServiceByConcept(concept);
            return new NeedsPaging<>(Collections.singletonList(facilityServicePrice), context);
        } else if (category != null || hiddenParam != null || qParam != null) {
            // Parse hidden parameter
            Boolean hidden = null;
            if (hiddenParam != null && !hiddenParam.trim().isEmpty()) {
                hidden = Boolean.parseBoolean(hiddenParam);
            }

            // Use database-level search for better performance
            BillingService billingService = Context.getService(BillingService.class);
            int startIndex = context.getStartIndex();
            int limit = context.getLimit();

            List<FacilityServicePrice> results = billingService.searchFacilityServicePrices(
                    category, hidden, qParam, startIndex, limit
            );

            long totalCount = billingService.countFacilityServicePrices(category, hidden, qParam);
            boolean hasMore = (startIndex + limit) < totalCount;

            return new AlreadyPaged<>(context, results, hasMore, totalCount);
        } else {
            return super.doSearch(context);
        }
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        int startIndex = context.getStartIndex();
        int limit = context.getLimit();

        List<FacilityServicePrice> facilityServicePrices = Context.getService(BillingService.class)
                .getAllFacilityServicePrices(startIndex, limit);

        long totalCount = Context.getService(BillingService.class).getFacilityServicePricesCount();
        boolean hasMore = (startIndex + limit) < totalCount;

        return new AlreadyPaged<>(context, facilityServicePrices, hasMore, totalCount);
    }

    @PropertySetter("fullPrice")
    public static void setFullPrice(FacilityServicePrice facilityServicePrice, Object value) {
        facilityServicePrice.setFullPrice(BillingUtils.convertRawValueToBigDecimal(value));
    }

    @PropertySetter("itemType")
    public static void setItemType(FacilityServicePrice facilityServicePrice, Object value) {
        if (value instanceof String) {
            facilityServicePrice.setItemType(Integer.parseInt((String) value));
        } else if (value instanceof Integer) {
            facilityServicePrice.setItemType((Integer) value);
        } else {
            throw new ConversionException("Unable to convert " + value.getClass() + " to Integer");
        }
    }

    @PropertySetter("hideItem")
    public static void setHideItem(FacilityServicePrice facilityServicePrice, Object value) {
        if (value instanceof String) {
            facilityServicePrice.setHideItem(Integer.parseInt((String) value));
        } else if (value instanceof Integer) {
            facilityServicePrice.setHideItem((Integer) value);
        } else {
            throw new ConversionException("Unable to convert " + value.getClass() + " to Integer");
        }
    }

    @PropertySetter("hidden")
    public static void setHidden(FacilityServicePrice facilityServicePrice, Object value) {
        facilityServicePrice.setHidden(Boolean.valueOf(String.valueOf(value)));
    }
}
