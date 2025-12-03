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
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.Transaction;
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
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/patientAccount",
        supportedClass = PatientAccount.class,
        supportedOpenmrsVersions = {"2.0 - 9.*"})
public class PatientAccountResource extends DelegatingCrudResource<PatientAccount> {

    @Override
    protected String getUniqueId(PatientAccount delegate) {
        return String.valueOf(delegate.getPatientAccountId());
    }

    @Override
    public PatientAccount getByUniqueId(String patientAccountId) {
        return Context.getService(BillingService.class).getPatientAccount(Integer.parseInt(patientAccountId));
    }

    @Override
    protected void delete(PatientAccount patientAccount, String reason, RequestContext requestContext) throws ResponseException {
        patientAccount.setVoided(true);
        patientAccount.setVoidedBy(Context.getAuthenticatedUser());
        patientAccount.setVoidedDate(new Date());
        patientAccount.setVoidReason(reason);
        Context.getService(BillingService.class).savePatientAccount(patientAccount);
    }

    @Override
    public PatientAccount newDelegate() {
        return new PatientAccount();
    }

    @Override
    public PatientAccount save(PatientAccount patientAccount) {
        if (patientAccount.getCreator() == null) {
            patientAccount.setCreator(Context.getAuthenticatedUser());
        }

        if (patientAccount.getCreatedDate() == null) {
            patientAccount.setCreatedDate(new Date());
        }

        Context.getService(BillingService.class).savePatientAccount(patientAccount);
        return patientAccount;
    }

    @Override
    public void purge(PatientAccount patientAccount, RequestContext requestContext) throws ResponseException {
        Context.getService(BillingService.class).purge(patientAccount);
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("patient");
        description.addProperty("balance");
        description.addProperty("transactions");
        return description;
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model
                    .property("patientAccountId", new IntegerProperty())
                    .property("patient", new RefProperty("#/definitions/PatientGet"))
                    .property("balance", new DecimalProperty()
                            .description("Current account balance"));
        }
        if (rep instanceof FullRepresentation) {
            model
                    .property("transactions", new ArrayProperty(new RefProperty("#/definitions/MohbillingTransactionGet")))
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
                .property("patient", new ObjectProperty()
                        .property("uuid", new StringProperty()
                                .description("UUID of the patient"))
                        .description("Patient reference object"))
                .property("balance", new DecimalProperty()
                        .example("0.00")
                        .description("Initial account balance"))
                .property("transactions", new ArrayProperty(new ObjectProperty()));

        model.required("patient");

        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        ModelImpl model = new ModelImpl()
                .property("balance", new DecimalProperty()
                        .example("1000.00"))
                .property("transactions", new ArrayProperty(new ObjectProperty()))
                .property("voided", new BooleanProperty())
                .property("voidReason", new StringProperty());

        return model;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("balance");
        description.addProperty("transactions");
        description.addProperty("voided");
        description.addProperty("voidReason");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("patientAccountId");
            description.addProperty("patient", Representation.REF);
            description.addProperty("balance");
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("patientAccountId");
            description.addProperty("patient");
            description.addProperty("balance");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
        } else if (representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("patientAccountId");
            description.addProperty("patient");
            description.addProperty("balance");
            description.addProperty("transactions");
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
        String patientUuid = context.getRequest().getParameter("patient");
        List<PatientAccount> patientAccounts = new ArrayList<>();

        if (patientUuid != null) {
            Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
            if (patient != null) {
                PatientAccount account = Context.getService(BillingService.class).getPatientAccount(patient);
                if (account != null) {
                    patientAccounts.add(account);
                }
            }
        }

        return new NeedsPaging<>(patientAccounts, context);
    }

    @PropertySetter("balance")
    public static void setBalance(PatientAccount patientAccount, Object value) {
        patientAccount.setBalance(BillingUtils.convertRawValueToBigDecimal(value));
    }

    @PropertySetter("transactions")
    public void setTransactions(PatientAccount patientAccount, Set<Transaction> transactions) {
        transactions.stream().forEach(transaction -> {
            if (transaction.getCreator() == null) {
                transaction.setCreator(Context.getAuthenticatedUser());
            }

            if (transaction.getCreatedDate() == null) {
                transaction.setCreatedDate(new Date());
            }

            if (transaction.getTransactionDate() == null) {
                transaction.setTransactionDate(new Date());
            }

            patientAccount.addTransaction(transaction);
        });
    }

    @PropertyGetter("transactions")
    public static Object getTransactions(PatientAccount patientAccount) throws ConversionException {
        if (patientAccount.getTransactions() != null && !patientAccount.getTransactions().isEmpty()) {
            return patientAccount.getTransactions();
        }
        return null;
    }
}