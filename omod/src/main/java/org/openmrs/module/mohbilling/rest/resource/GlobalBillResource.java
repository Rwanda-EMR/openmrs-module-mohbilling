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
import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
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

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/globalBill",
        supportedClass = GlobalBill.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class GlobalBillResource extends DelegatingCrudResource<GlobalBill> {

    @Override
    protected String getUniqueId(GlobalBill delegate) {
        return String.valueOf(delegate.getBillIdentifier());
    }

    @Override
    public GlobalBill getByUniqueId(String uniqueId) {
        BillingService billingService = Context.getService(BillingService.class);
        return billingService.getGlobalBillByBillIdentifier(uniqueId);
    }

    @Override
    protected void delete(GlobalBill globalBill, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public GlobalBill newDelegate() {
        return new GlobalBill();
    }

    @Override
    public GlobalBill save(GlobalBill globalBill) {
        BillingService billingService = Context.getService(BillingService.class);
        Integer insurancePolicyId = globalBill.getAdmission().getInsurancePolicy().getInsurancePolicyId();
        InsurancePolicy ip = billingService.getInsurancePolicy(insurancePolicyId);
        GlobalBill openGlobalBill = billingService.getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());

        if (openGlobalBill != null) {
            return openGlobalBill;
        }

        if (globalBill.getCreator() == null) {
            globalBill.setCreator(Context.getAuthenticatedUser());
        }

        if (globalBill.getCreatedDate() == null) {
            globalBill.setCreatedDate(new Date());
        }

        if (globalBill.getAdmission().getCreatedDate() == null) {
            globalBill.getAdmission().setCreatedDate(new Date());
        }

        if (globalBill.getAdmission().getCreator() == null) {
            globalBill.getAdmission().setCreator(Context.getAuthenticatedUser());
        }

        Integer admissionId;
        if (globalBill.getAdmission() == null || globalBill.getAdmission().getAdmissionId() == null) {
            Admission admission = billingService.saveAdmission(globalBill.getAdmission());
            admissionId = admission.getAdmissionId();
            globalBill.setAdmission(admission);
        } else {
            admissionId = globalBill.getAdmission().getAdmissionId();
        }

        globalBill.setInsurance(ip.getInsurance());
        globalBill.setBillIdentifier(ip.getInsuranceCardNo() + admissionId);

        return billingService.saveGlobalBill(globalBill);
    }

    @Override
    public void purge(GlobalBill globalBill, RequestContext requestContext) throws ResponseException {
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("admission");
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        if (representation instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("globalBillId");
            description.addProperty("admission", Representation.REF);
            description.addProperty("billIdentifier");
            description.addProperty("globalAmount");
            description.addProperty("consommations", Representation.REF);
            description.addProperty("createdDate");
            description.addProperty("creator", Representation.REF);
            description.addProperty("closingDate");
            description.addProperty("closedBy", Representation.REF);
            description.addProperty("closed");
            description.addProperty("insurance", Representation.REF);
            description.addProperty("closingReason");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("globalBillId");
            description.addProperty("admission", Representation.REF);
            description.addProperty("billIdentifier");
            description.addProperty("globalAmount");
            description.addProperty("consommations", Representation.REF);
            description.addProperty("createdDate");
            description.addProperty("creator", Representation.REF);
            description.addProperty("closingDate");
            description.addProperty("closedBy", Representation.REF);
            description.addProperty("closed");
            description.addProperty("insurance", Representation.REF);
            description.addProperty("closingReason");
            description.addSelfLink();
            return description;
        }
        return null;
    }


    @Override
    protected PageableResult doSearch(RequestContext context) {
        String ipCardNumber = context.getRequest().getParameter("ipCardNumber");
        String billIdentifier = context.getRequest().getParameter("billIdentifier");
        String patientUuid = context.getRequest().getParameter("patient");
        List<GlobalBill> globalBills = new ArrayList<>();

        if (patientUuid != null) {
            Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
            List<InsurancePolicy> insurancePolicies = InsurancePolicyUtil.getInsurancePoliciesByPatient(patient);
            for (InsurancePolicy insurancePolicy : insurancePolicies) {
                globalBills.addAll(GlobalBillUtil.getGlobalBillsByInsurancePolicy(insurancePolicy));
            }
            return new NeedsPaging<>(globalBills, context);
        }

        if (ipCardNumber != null) {
            Beneficiary ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(ipCardNumber);
            InsurancePolicy ip = InsurancePolicyUtil.getInsurancePolicyByBeneficiary(ben);
            globalBills = GlobalBillUtil.getGlobalBillsByInsurancePolicy(ip);
        }

        if (billIdentifier != null) {
            GlobalBill globalBill = GlobalBillUtil.getGlobalBillByBillIdentifier(billIdentifier);
            globalBills.add(globalBill);
        }
        return new NeedsPaging<>(globalBills, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BillingService.class).getGlobalBills(), context);
    }
}
