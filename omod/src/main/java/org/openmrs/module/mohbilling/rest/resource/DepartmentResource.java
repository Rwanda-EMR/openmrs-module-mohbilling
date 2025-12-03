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
import org.openmrs.module.mohbilling.model.Department;
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

import java.util.Date;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/department",
        supportedClass = Department.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class DepartmentResource extends DelegatingCrudResource<Department> {

    @Override
    protected String getUniqueId(Department delegate) {
        return String.valueOf(delegate.getDepartmentId());
    }

    @Override
    public Department getByUniqueId(String s) {
        return Context.getService(BillingService.class).getDepartement(Integer.valueOf(s));
    }

    @Override
    protected void delete(Department department, String s, RequestContext requestContext) throws ResponseException {
        department.setVoided(true);
        department.setVoidedBy(Context.getAuthenticatedUser());
        department.setVoidedDate(new Date());
        department.setVoidReason(s);
        Context.getService(BillingService.class).saveDepartement(department);
    }

    @Override
    public Department newDelegate() {
        return new Department();
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
                    .property("departmentId", new IntegerProperty())
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

        model.required("name")
                .required("description");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return getCREATEModel(rep);
    }

    @Override
    public Department save(Department department) {
        if (department.getCreator() == null) {
            department.setCreator(Context.getAuthenticatedUser());
        }
        if (department.getCreatedDate() == null) {
            department.setCreatedDate(new Date());
        }
        return Context.getService(BillingService.class).saveDepartement(department);
    }

    @Override
    public void purge(Department department, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(department);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("departmentId");
            description.addProperty("name");
            description.addProperty("description");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("departmentId");
            description.addProperty("name");
            description.addProperty("description");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllDepartements(), context);
    }
}
