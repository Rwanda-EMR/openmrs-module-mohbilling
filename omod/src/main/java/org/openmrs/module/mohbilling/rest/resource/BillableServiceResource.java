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
import org.openmrs.module.mohbilling.model.BillableService;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/billableService",
        supportedClass = BillableService.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
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
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public BillableService newDelegate() {
        return new BillableService();
    }

    @Override
    public BillableService save(BillableService billableService) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void purge(BillableService billableService, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
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
}
