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
import org.openmrs.module.mohbilling.model.DepositPayment;
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
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Date;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/depositPayment",
        supportedClass = DepositPayment.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class DepositPaymentResource extends DelegatingCrudResource<DepositPayment> {

    @Override
    protected String getUniqueId(DepositPayment delegate) {
        return String.valueOf(delegate.getDepositPaymentId());
    }

    @Override
    public DepositPayment getByUniqueId(String depositPaymentId) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    protected void delete(DepositPayment depositPayment, String reason, RequestContext requestContext) throws ResponseException {
        depositPayment.setVoided(true);
        depositPayment.setVoidedBy(Context.getAuthenticatedUser());
        depositPayment.setVoidedDate(new Date());
        depositPayment.setVoidReason(reason);
        Context.getService(BillingService.class).saveDepositPayment(depositPayment);
    }

    @Override
    public DepositPayment newDelegate() {
        return new DepositPayment();
    }

    @Override
    public DepositPayment save(DepositPayment depositPayment) {
        if (depositPayment.getCreator() == null) {
            depositPayment.setCreator(Context.getAuthenticatedUser());
        }

        if (depositPayment.getCreatedDate() == null) {
            depositPayment.setCreatedDate(new Date());
        }

        Context.getService(BillingService.class).saveDepositPayment(depositPayment);
        return depositPayment;
    }

    @Override
    public void purge(DepositPayment depositPayment, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(depositPayment);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("transaction");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("depositPaymentId", new IntegerProperty())
                    .property("transaction", new RefProperty("#/definitions/MohbillingTransactionGet"))
                    .property("amountPaid", new DecimalProperty())
                    .property("collectedBy", new RefProperty("#/definitions/UserGet"));
        }
        if (rep instanceof FullRepresentation) {
            model
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
                .property("transaction", new ObjectProperty()
                        .property("transactionId", new IntegerProperty()
                                .description("ID of the transaction"))
                        .description("Transaction reference object"));

        model.required("transaction")
                .required("amountPaid");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("voided", new BooleanProperty())
                .property("voidReason", new StringProperty());

        return model;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("voided");
        description.addProperty("voidReason");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("depositPaymentId");
            description.addProperty("transaction", Representation.REF);
            description.addProperty("amountPaid");
            description.addProperty("collectedBy", Representation.REF);
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("depositPaymentId");
            description.addProperty("transaction");
            description.addProperty("amountPaid");
            description.addProperty("collectedBy");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        } else if (representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("depositPaymentId");
            description.addProperty("transaction");
            description.addProperty("amountPaid");
            description.addProperty("collectedBy");
            description.addProperty("creator", Representation.REF);
            description.addProperty("createdDate");
            description.addProperty("voided");
            description.addProperty("voidedBy", Representation.REF);
            description.addProperty("voidedDate");
            description.addProperty("voidReason");
            description.addSelfLink();
        }

        return description;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        throw new ResourceDoesNotSupportOperationException();
    }
}