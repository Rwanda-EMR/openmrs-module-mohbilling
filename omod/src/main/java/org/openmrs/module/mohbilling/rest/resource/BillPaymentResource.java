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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
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
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/billPayment",
        supportedClass = BillPayment.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class BillPaymentResource extends DelegatingCrudResource<BillPayment> {
    @Override
    protected String getUniqueId(BillPayment delegate) {
        return String.valueOf(delegate.getBillPaymentId());
    }

    @Override
    public BillPayment getByUniqueId(String s) {
        return Context.getService(BillingService.class).getBillPayment(Integer.valueOf(s));
    }

    @Override
    protected void delete(BillPayment billPayment, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public BillPayment newDelegate() {
        return new BillPayment();
    }

    @Override
    public BillPayment save(BillPayment billPayment) {
        if (billPayment.getCreator() == null) {
            billPayment.setCreator(Context.getAuthenticatedUser());
        }
        Context.getService(BillingService.class).saveBillPayment(billPayment);

        PatientBill patientBill = billPayment.getPatientBill();
        BigDecimal patientBillAmount = patientBill.getAmount();

        if (patientBillAmount.compareTo(patientBill.getAmountPaid()) == 0 ||
                patientBillAmount.compareTo(billPayment.getAmountPaid()) == 0) {
            patientBill.setIsPaid(true);
            Context.getService(BillingService.class).savePatientBill(patientBill);
        }

        billPayment.getPaidItems().stream().forEach(paidServiceBill -> {
            paidServiceBill.getBillItem().setPaidQuantity(paidServiceBill.getPaidQty());
            ConsommationUtil.saveBilledItem(paidServiceBill.getBillItem());
        });

        return billPayment;
    }

    @Override
    public void purge(BillPayment billPayment, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("amountPaid");
        description.addRequiredProperty("patientBill");
        description.addProperty("dateReceived");
        description.addProperty("collector");
        description.addProperty("paidItems");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("amountPaid");
            description.addProperty("dateReceived");
            description.addProperty("collector", Representation.REF);
            description.addProperty("paidItems", Representation.REF);
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("amountPaid");
            description.addProperty("dateReceived");
            description.addProperty("collector", Representation.REF);
            description.addProperty("paidItems", Representation.REF);
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @PropertySetter("amountPaid")
    public static void setAmountPaid(BillPayment billPayment, Object value) {
        billPayment.setAmountPaid(BigDecimal.valueOf((Double) value));
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String patientBill = context.getRequest().getParameter("patientBill");
        List<BillPayment> billPayments = new ArrayList<>();
        if (patientBill != null) {
            PatientBill pBill = Context.getService(BillingService.class).getPatientBill(Integer.parseInt(patientBill));
            billPayments = Context.getService(BillingService.class).getBillPaymentsByPatientBill(pBill);
        }
        return new NeedsPaging<>(billPayments, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getAllBillPayments(), context);
    }
}
