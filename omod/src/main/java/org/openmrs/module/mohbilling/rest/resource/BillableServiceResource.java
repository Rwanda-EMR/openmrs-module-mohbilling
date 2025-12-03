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
import org.openmrs.module.mohbilling.model.BillableService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/billableService",
        supportedClass = BillableService.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class BillableServiceResource extends DelegatingCrudResource<BillableService> {
    @Override
    protected String getUniqueId(BillableService delegate) {
        return String.valueOf(delegate.getServiceId());
    }

    @Override
    public BillableService getByUniqueId(String s) {
        return Context.getService(BillingService.class).getBillableService(Integer.parseInt(s));
    }

    @Override
    protected void delete(BillableService billableService, String s, RequestContext requestContext) throws ResponseException {
        billableService.setRetired(true);
        billableService.setRetiredBy(Context.getAuthenticatedUser());
        billableService.setRetiredDate(new Date());
        billableService.setRetireReason(s);
        Context.getService(BillingService.class).saveBillableService(billableService);
    }

    @Override
    public void purge(BillableService billableService, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(billableService);
    }

    @Override
    public BillableService newDelegate() {
        return new BillableService();
    }

    @Override
    public BillableService save(BillableService billableService) {
        if (billableService.getCreator() == null) {
            billableService.setCreator(Context.getAuthenticatedUser());
        }
        if (billableService.getCreatedDate() == null) {
            billableService.setCreatedDate(new Date());
        }
        if (billableService.getStartDate() == null) {
            billableService.setStartDate(new Date());
        }
        return Context.getService(BillingService.class).saveBillableService(billableService);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("startDate");
        description.addRequiredProperty("facilityServicePrice");
        description.addProperty("insurance");
        description.addProperty("maximaToPay");
        description.addProperty("endDate");
        description.addProperty("serviceCategory");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("serviceId", new IntegerProperty())
                    .property("insurance", new RefProperty("#/definitions/MohbillingInsuranceGet"))
                    .property("maximaToPay", new DecimalProperty()
                            .description("Maximum amount insurance will pay"))
                    .property("facilityServicePrice", new RefProperty("#/definitions/MohbillingFacilityServicePriceGet"))
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
                .property("facilityServicePrice", new ObjectProperty()
                        .property("facilityServicePriceId", new IntegerProperty()
                                .description("facilityServicePriceId of the facility service price"))
                        .description("FacilityServicePrice reference object"))
                .property("insurance", new ObjectProperty()
                        .property("insuranceId", new IntegerProperty()
                                .description("insuranceId of the insurance"))
                        .description("Insurance reference object"))
                .property("serviceCategory", new ObjectProperty()
                        .property("serviceCategoryId", new IntegerProperty()
                                .description("serviceCategoryId of the service category"))
                        .description("ServiceCategory reference object"))
                .property("maximaToPay", new DecimalProperty()
                        .description("Maximum amount insurance will pay"))
                .property("startDate", new DateTimeProperty())
                .property("endDate", new DateTimeProperty());

        model.required("facilityServicePrice")
                .required("startDate");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("insurance", new ObjectProperty()
                        .property("insuranceId", new IntegerProperty()
                                .description("insuranceId of the insurance"))
                        .description("Insurance reference object"))
                .property("maximaToPay", new DecimalProperty()
                        .description("Maximum amount insurance will pay"))
                .property("endDate", new DateTimeProperty());

        return model;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;
        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceId");
            description.addProperty("insurance", Representation.REF);
            description.addProperty("maximaToPay");
            description.addProperty("facilityServicePrice", Representation.REF);
            description.addProperty("startDate");
            description.addProperty("endDate");
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceId");
            description.addProperty("insurance");
            description.addProperty("maximaToPay");
            description.addProperty("facilityServicePrice");
            description.addProperty("startDate");
            description.addProperty("endDate");
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String serviceCategoryId = context.getRequest().getParameter("serviceCategoryId");
        String facilityServicePriceId = context.getRequest().getParameter("facilityServicePriceId");
        List<BillableService> billableServices = new ArrayList<>();

        if (serviceCategoryId != null && facilityServicePriceId != null) {
            billableServices = Context.getService(BillingService.class).getBillableServicesByCategoryAndFacilityServicePrice(
                    Integer.parseInt(serviceCategoryId), Integer.parseInt(facilityServicePriceId));
        } else {
            if (serviceCategoryId != null) {
                billableServices = Context.getService(BillingService.class).getBillableServiceByCategory(
                        Context.getService(BillingService.class).getServiceCategory(Integer.parseInt(serviceCategoryId)));
            }

            if (facilityServicePriceId != null) {
                billableServices = Context.getService(BillingService.class).getBillableServicesByFacilityService(
                        Context.getService(BillingService.class).getFacilityServicePrice(Integer.parseInt(facilityServicePriceId)));
            }
        }
        return new NeedsPaging<>(billableServices, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllBillableServices(), context);
    }

    @PropertySetter("maximaToPay")
    public static void setMaximaToPay(BillableService billableService, Object value) {
        billableService.setMaximaToPay(BillingUtils.convertRawValueToBigDecimal(value));
    }
}
