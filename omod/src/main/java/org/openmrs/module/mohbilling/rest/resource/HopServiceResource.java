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
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.HopService;
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

import java.util.*;
import java.util.stream.Collectors;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/hopService",
        supportedClass = HopService.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class HopServiceResource extends DelegatingCrudResource<HopService> {
    @Override
    protected String getUniqueId(HopService delegate) {
        return String.valueOf(delegate.getServiceId());
    }

    @Override
    public HopService getByUniqueId(String s) {
        return Context.getService(BillingService.class).getHopService(Integer.parseInt(s));
    }

    @Override
    protected void delete(HopService hopService, String s, RequestContext requestContext) throws ResponseException {
        hopService.setVoided(true);
        hopService.setVoidedBy(Context.getAuthenticatedUser());
        hopService.setVoidedDate(new Date());
        hopService.setVoidReason(s);
        Context.getService(BillingService.class).saveHopService(hopService);
    }

    @Override
    public HopService newDelegate() {
        return new HopService();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("name");
        description.addRequiredProperty("description");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("hopServiceId", new IntegerProperty())
                    .property("name", new StringProperty())
                    .property("description", new StringProperty());
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
                .property("description", new StringProperty());

        model.required("name");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return getCREATEModel(rep);
    }

    @Override
    public HopService save(HopService hopService) {
        if (hopService.getCreator() == null) {
            hopService.setCreator(Context.getAuthenticatedUser());
        }
        if (hopService.getCreatedDate() == null) {
            hopService.setCreatedDate(new Date());
        }
        return Context.getService(BillingService.class).saveHopService(hopService);
    }

    @Override
    public void purge(HopService hopService, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(hopService);
    }


    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceId");
            description.addProperty("name");
            description.addProperty("department", Representation.REF);
            description.addProperty("description");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceId");
            description.addProperty("name");
            description.addProperty("department", Representation.REF);
            description.addProperty("description");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String department = context.getRequest().getParameter("department");
        Set<HopService> hopServices = new HashSet<>();
        if (department != null) {
            Department d = Context.getService(BillingService.class).getDepartement(Integer.parseInt(department));
            hopServices = Context.getService(BillingService.class).getAllHopService().stream().filter(hopService ->
                    hopService.getDepartment() != null && hopService.getDepartment().equals(d)).collect(Collectors.toSet());
            if (GlobalPropertyConfig.getListOfHopServicesByDepartment1(d) != null) {
                hopServices.addAll(
                        Arrays.stream(GlobalPropertyConfig.getListOfHopServicesByDepartment1(d).split(","))
                                .map(s -> HopServiceUtil.getHopServiceById(Integer.valueOf(s)))
                                .collect(Collectors.toList())
                );
            }
        }
        return new NeedsPaging<>(new ArrayList<>(hopServices), context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllHopService(), context);
    }
}
