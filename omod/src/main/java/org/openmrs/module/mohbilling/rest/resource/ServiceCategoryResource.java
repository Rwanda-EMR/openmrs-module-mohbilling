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
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.ServiceCategory;
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

import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/serviceCategory",
        supportedClass = ServiceCategory.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class ServiceCategoryResource extends DelegatingCrudResource<ServiceCategory> {
    @Override
    protected String getUniqueId(ServiceCategory delegate) {
        return String.valueOf(delegate.getServiceCategoryId());
    }

    @Override
    public ServiceCategory getByUniqueId(String s) {
        return Context.getService(BillingService.class).getServiceCategory(Integer.valueOf(s));
    }

    @Override
    protected void delete(ServiceCategory serviceCategory, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public ServiceCategory newDelegate() {
        return new ServiceCategory();
    }

    @Override
    public ServiceCategory save(ServiceCategory serviceCategory) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("serviceCategoryId", new IntegerProperty())
                    .property("name", new StringProperty())
                    .property("description", new StringProperty())
                    .property("insurance", new RefProperty("#/definitions/MohbillingInsuranceGet"));
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
                .property("description", new StringProperty())
                .property("insurance", new ObjectProperty()
                        .property("insuranceId", new IntegerProperty()
                                .description("ID of the insurance"))
                        .description("Insurance reference object"));

        model.required("name")
                .required("insurance");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return getCREATEModel(rep);
    }

    @Override
    public void purge(ServiceCategory serviceCategory, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;
        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceCategoryId");
            description.addProperty("name");
            description.addProperty("department", Representation.REF);
            description.addProperty("hopService", Representation.REF);
            description.addProperty("description");
            description.addProperty("price");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("serviceCategoryId");
            description.addProperty("name");
            description.addProperty("department");
            description.addProperty("hopService");
            description.addProperty("description");
            description.addProperty("price");
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }
        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String departmentId = context.getRequest().getParameter("departmentId");
        String ipCardNumber = context.getRequest().getParameter("ipCardNumber");

        Department department = null;
        if (departmentId != null) {
            department = DepartementUtil.getDepartement(Integer.valueOf(departmentId));
        }

        Beneficiary ben = null;
        if (ipCardNumber != null) {
            ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(ipCardNumber);
        }

        List<ServiceCategory> categories = new ArrayList<>();
        if (department != null && ben != null) {
            categories.addAll(HopServiceUtil.getServiceCategoryByInsurancePolicyDepartment(ben.getInsurancePolicy(), department));
        }

        return new NeedsPaging<>(categories, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllServiceCategories(), context);
    }
}
