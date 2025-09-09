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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.module.mohbilling.service.BillingService;
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
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/consommation",
        supportedClass = Consommation.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class ConsommationResource extends DelegatingCrudResource<Consommation> {

    @Override
    protected String getUniqueId(Consommation delegate) {
        return String.valueOf(delegate.getConsommationId());
    }

    @Override
    public Consommation getByUniqueId(String s) {
        return Context.getService(BillingService.class).getConsommation(Integer.parseInt(s));
    }

    @Override
    protected void delete(Consommation consommation, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Consommation newDelegate() {
        return new Consommation();
    }

    @Override
    public Consommation save(Consommation consommation) {
        if (consommation.getCreator() == null) {
            consommation.setCreator(Context.getAuthenticatedUser());
        }
        if (consommation.getCreatedDate() == null) {
            consommation.setCreatedDate(new Date());
        }

        GlobalBill globalBill = GlobalBillUtil.getGlobalBill(consommation.getGlobalBill().getGlobalBillId());

        BigDecimal totalAmount = consommation.getBillItems().stream()
                .map(billItem -> billItem.getUnitPrice().multiply(billItem.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        InsuranceBill insuranceBill = InsuranceBillUtil.createInsuranceBill(globalBill.getAdmission().getInsurancePolicy().getInsurance(),
                totalAmount);
        consommation.setInsuranceBill(insuranceBill);

        ThirdPartyBill thirdPartyBill = ThirdPartyBillUtil.createThirdPartyBill(globalBill.getAdmission().getInsurancePolicy(), totalAmount);
        consommation.setThirdPartyBill(thirdPartyBill);

        PatientBill patientBill = PatientBillUtil.createPatientBill(totalAmount, globalBill.getAdmission().getInsurancePolicy());
        consommation.setPatientBill(patientBill);

        Context.getService(BillingService.class).saveConsommation(consommation);
        return consommation;
    }

    @Override
    public void purge(Consommation consommation, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("department");
        description.addRequiredProperty("beneficiary");
        description.addRequiredProperty("globalBill");
        description.addProperty("billItems");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        DelegatingResourceDescription description = null;

        if (representation instanceof RefRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("consommationId");
            description.addProperty("paymentStatus");
            description.addProperty("department", Representation.REF);
            description.addProperty("billItems", Representation.REF);
            description.addProperty("patientBill", Representation.REF);
            description.addProperty("insuranceBill", Representation.REF);
            description.addProperty("thirdPartyBill", Representation.REF);
            description.addSelfLink();
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            description = new DelegatingResourceDescription();
            description.addProperty("consommationId");
            description.addProperty("paymentStatus");
            description.addProperty("department", Representation.REF);
            description.addProperty("billItems");
            description.addProperty("patientBill", Representation.REF);
            description.addProperty("insuranceBill", Representation.REF);
            description.addProperty("thirdPartyBill", Representation.REF);
            description.addSelfLink();
            if (representation instanceof DefaultRepresentation) {
                description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
        }

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        String globalBillId = context.getRequest().getParameter("globalBillId");
        String patientName = context.getRequest().getParameter("patientName");
        String policyIdNumber = context.getRequest().getParameter("policyIdNumber"); // aka insurance card no
        String orderBy = context.getRequest().getParameter("orderBy"); // default: createdDate
        String orderDirection = context.getRequest().getParameter("orderDirection"); // default: desc

        Integer startIndex = context.getStartIndex();
        Integer pageSize = context.getLimit() != null ? context.getLimit() : 10;

        List<Consommation> consommations = new ArrayList<>();

        if (globalBillId != null) {
            GlobalBill globalBill = GlobalBillUtil.getGlobalBill(Integer.valueOf(globalBillId));
            consommations = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
            return new NeedsPaging<>(consommations, context);
        }

        if ((patientName != null && !patientName.trim().isEmpty()) || (policyIdNumber != null && !policyIdNumber.trim().isEmpty())) {
            consommations = Context.getService(BillingService.class)
                    .findConsommationsByPatientOrPolicy(
                            patientName,
                            policyIdNumber,
                            startIndex,
                            pageSize,
                            (orderBy != null && !orderBy.trim().isEmpty()) ? orderBy : "createdDate",
                            (orderDirection != null && !orderDirection.trim().isEmpty()) ? orderDirection : "desc");
            return new NeedsPaging<>(consommations, context);
        }

        consommations = Context.getService(BillingService.class)
                .getNewestConsommations(
                        startIndex,
                        pageSize,
                        (orderBy != null && !orderBy.trim().isEmpty()) ? orderBy : "createdDate",
                        (orderDirection != null && !orderDirection.trim().isEmpty()) ? orderDirection : "desc");
        return new NeedsPaging<>(consommations, context);
    }

    @PropertySetter("billItems")
    public void setBillItems(Consommation consommation, Set<PatientServiceBill> billItems) {
        billItems.stream().forEach(billItem -> consommation.addBillItem(billItem));
    }

    @PropertyGetter("paymentStatus")
    public String getStatus(Consommation consommation) {
        BigDecimal totalAmount = consommation.getBillItems().stream()
                .map(billItem -> billItem.getUnitPrice().multiply(billItem.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidAmount = consommation.getPatientBill().getAmountPaid();

        BigDecimal insuranceAndThirdPartyAmount = getInsuranceAndThirdPartyAmount(consommation);

        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "Unpaid";
        } else {
            if (paidAmount.add(insuranceAndThirdPartyAmount).compareTo(totalAmount) >= 0) {
                return "Fully Paid";
            } else {
                return "Partially Paid";
            }
        }
        //return ConsommationUtil.getConsommationStatus(consommation.getConsommationId());
    }

    public BigDecimal getInsuranceAndThirdPartyAmount(Consommation consommation) {
        BigDecimal insuranceAndThirdPartyAmount = BigDecimal.ZERO;

        if (consommation.getInsuranceBill() != null) {
            // Insurance will pay part of the bill
            insuranceAndThirdPartyAmount = insuranceAndThirdPartyAmount.add(consommation.getInsuranceBill().getAmount());
        }

        if (consommation.getThirdPartyBill() != null) {
            insuranceAndThirdPartyAmount = insuranceAndThirdPartyAmount.add(consommation.getThirdPartyBill().getAmount());
        }

        return insuranceAndThirdPartyAmount;
    }
}
