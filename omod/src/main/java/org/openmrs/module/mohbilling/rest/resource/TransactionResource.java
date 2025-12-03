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
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.Transaction;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.BillingUtils;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/transaction",
        supportedClass = Transaction.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class TransactionResource extends DelegatingCrudResource<Transaction> {

    @Override
    protected String getUniqueId(Transaction delegate) {
        return String.valueOf(delegate.getTransactionId());
    }

    @Override
    public Transaction getByUniqueId(String transactionId) {
        return Context.getService(BillingService.class).getTransactionById(Integer.parseInt(transactionId));
    }

    @Override
    protected void delete(Transaction transaction, String reason, RequestContext requestContext) throws ResponseException {
        transaction.setVoided(true);
        transaction.setVoidedBy(Context.getAuthenticatedUser());
        transaction.setVoidedDate(new Date());
        transaction.setVoidReason(reason);

        PatientAccount patientAccount = transaction.getPatientAccount();
        if (patientAccount != null) {
            Context.getService(BillingService.class).savePatientAccount(patientAccount);
        }
    }

    @Override
    public Transaction newDelegate() {
        return new Transaction();
    }

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getCreator() == null) {
            transaction.setCreator(Context.getAuthenticatedUser());
        }

        if (transaction.getCreatedDate() == null) {
            transaction.setCreatedDate(new Date());
        }

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(new Date());
        }

        PatientAccount patientAccount = transaction.getPatientAccount();
        if (patientAccount != null) {
            patientAccount.addTransaction(transaction);
            Context.getService(BillingService.class).savePatientAccount(patientAccount);
        }

        return transaction;
    }

    @Override
    public void purge(Transaction transaction, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(transaction);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("patientAccount");
        description.addRequiredProperty("amount");
        description.addProperty("collector");
        description.addProperty("transactionDate");
        description.addProperty("reason");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("transactionId", new IntegerProperty())
                    .property("patientAccount", new RefProperty("#/definitions/MohbillingPatientAccountGet"))
                    .property("amount", new DecimalProperty())
                    .property("collector", new RefProperty("#/definitions/UserGet"))
                    .property("transactionDate", new DateTimeProperty())
                    .property("reason", new StringProperty());
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
                .property("patientAccount", new ObjectProperty()
                        .property("patientAccountId", new IntegerProperty()
                                .description("ID of the patient account"))
                        .description("PatientAccount reference object"))
                .property("amount", new DecimalProperty()
                        .example("5000.00")
                        .description("Transaction amount (positive for deposit, negative for withdrawal)"))
                .property("collector", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the user who collected the transaction"))
                        .description("User reference object"))
                .property("transactionDate", new DateTimeProperty())
                .property("reason", new StringProperty()
                        .description("Reason for the transaction"));

        model.required("patientAccount")
                .required("amount");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("amount", new DecimalProperty()
                        .example("5000.00"))
                .property("collector", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the user who collected the transaction"))
                        .description("User reference object"))
                .property("transactionDate", new DateTimeProperty())
                .property("reason", new StringProperty())
                .property("voided", new BooleanProperty())
                .property("voidReason", new StringProperty());

        return model;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("amount");
        description.addProperty("collector");
        description.addProperty("transactionDate");
        description.addProperty("reason");
        description.addProperty("voided");
        description.addProperty("voidReason");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("transactionId");
            description.addProperty("patientAccount", Representation.REF);
            description.addProperty("amount");
            description.addProperty("transactionDate");
            description.addProperty("reason");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("transactionId");
            description.addProperty("patientAccount");
            description.addProperty("amount");
            description.addProperty("collector");
            description.addProperty("transactionDate");
            description.addProperty("reason");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        } else if (representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("transactionId");
            description.addProperty("patientAccount");
            description.addProperty("amount");
            description.addProperty("collector");
            description.addProperty("transactionDate");
            description.addProperty("reason");
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
        return new NeedsPaging<>(new ArrayList<>(Context.getService(BillingService.class)
                .getTransactions((PatientAccount) null, null, null, null)), context);
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String patientAccountId = context.getRequest().getParameter("patientAccount");
        String startDateParam = context.getRequest().getParameter("startDate");
        String endDateParam = context.getRequest().getParameter("endDate");
        String collectorUuid = context.getRequest().getParameter("collector");
        String reason = context.getRequest().getParameter("reason");
        User collector = collectorUuid != null ? Context.getUserService().getUserByUuid(collectorUuid) : null;
        Date startDate = startDateParam != null ? (Date) ConversionUtil.convert(startDateParam, Date.class) : null;
        Date endDate = endDateParam != null ? (Date) ConversionUtil.convert(endDateParam, Date.class) : new Date();

        if (startDate != null && collector != null) {
            return new NeedsPaging<>(Context.getService(BillingService.class)
                    .getTransactions(startDate, endDate, collector, reason), context);
        }

        PatientAccount patientAccount = patientAccountId != null ? Context.getService(BillingService.class)
                .getPatientAccount(Integer.parseInt(patientAccountId)) : null;
        return new NeedsPaging<>(new ArrayList<>(Context.getService(BillingService.class)
                .getTransactions(patientAccount, startDate, endDate, reason)), context);
    }

    @PropertySetter("amount")
    public static void setAmount(Transaction transaction, Object value) {
        transaction.setAmount(BillingUtils.convertRawValueToBigDecimal(value));
    }
}