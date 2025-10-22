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

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.service.BillingService;
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
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/facilityServicePrice",
        supportedClass = FacilityServicePrice.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
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
        throw new ResourceDoesNotSupportOperationException();
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
        throw new ResourceDoesNotSupportOperationException();
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
        if (conceptUuid != null) {
            Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
            FacilityServicePrice facilityServicePrice = Context.getService(BillingService.class).getFacilityServiceByConcept(concept);
            return new NeedsPaging<>(Collections.singletonList(facilityServicePrice), context);
        } else {
            return super.doSearch(context);
        }
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllFacilityServicePrices(), context);
    }

    @PropertySetter("fullPrice")
    public static void setFullPrice(FacilityServicePrice facilityServicePrice, Object value) {
        if (value instanceof String) {
            facilityServicePrice.setFullPrice(new BigDecimal((String) value));
        } else if (value instanceof Long) {
            facilityServicePrice.setFullPrice(BigDecimal.valueOf((Long) value));
        } else if (value instanceof Integer) {
            facilityServicePrice.setFullPrice(BigDecimal.valueOf((Integer) value));
        } else if (value instanceof BigInteger) {
            facilityServicePrice.setFullPrice(new BigDecimal((BigInteger) value));
        } else if (value instanceof Double) {
            facilityServicePrice.setFullPrice(new BigDecimal(String.valueOf((Double) value)));
        } else if (value instanceof Number) {
            facilityServicePrice.setFullPrice(new BigDecimal(String.valueOf(((Number) value).doubleValue())));
        } else {
            throw new IllegalArgumentException("Cannot convert object of type " + value.getClass().getName() + " to BigDecimal.");
        }
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
