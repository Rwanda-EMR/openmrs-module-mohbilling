package org.openmrs.module.mohbilling.rest.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/globalBill",
        supportedClass = GlobalBill.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class GlobalBillResource extends DelegatingCrudResource<GlobalBill> {

    private static final Map<String, String> SORT_KEYS;
    static {
        SORT_KEYS = new LinkedHashMap<>();

        SORT_KEYS.put("admissionDate", "admission.createdDate");
        SORT_KEYS.put("createdDate", "createdDate");
        SORT_KEYS.put("closingDate", "closingDate");
        SORT_KEYS.put("globalBillId", "globalBillId");
    }

    private String normalizeOrderBy(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "admission.createdDate";
        String key = raw.trim();
        String mapped = SORT_KEYS.get(key);
        if (mapped != null) return mapped;

        for (String allowed : SORT_KEYS.values()) {
            if (allowed.equals(key)) return key;
        }
        return "admission.createdDate";
    }

    private String normalizeDirection(String raw) {
        if (raw == null) return "desc";
        String v = raw.trim().toLowerCase();
        return ("asc".equals(v) ? "asc" : "desc");
    }

    private int normalizeStart(RequestContext ctx) {
        Integer s = ctx.getStartIndex();
        return s == null || s < 0 ? 0 : s;
    }

    private int normalizeLimit(RequestContext ctx) {
        Integer l = ctx.getLimit();
        return l == null || l <= 0 ? 10 : l;
    }

    @Override
    protected String getUniqueId(GlobalBill delegate) {
        return String.valueOf(delegate.getGlobalBillId());
    }

    @Override
    public GlobalBill getByUniqueId(String uniqueId) {
        BillingService billingService = Context.getService(BillingService.class);
        return billingService.GetGlobalBill(Integer.valueOf(uniqueId));
    }

    @Override
    protected void delete(GlobalBill globalBill, String s, RequestContext requestContext) throws ResponseException {
        // not supported
    }

    @Override
    public GlobalBill newDelegate() {
        return new GlobalBill();
    }

    @Override
    public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
        if (!propertiesToUpdate.containsKey("closedBy")) {
            propertiesToUpdate.put("closedBy", Context.getAuthenticatedUser());
        }
        return super.update(uuid, propertiesToUpdate, context);
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
        // not supported
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("admission");
        return description;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("closed");
        description.addProperty("closedBy");
        description.addProperty("closingReason");
        description.addProperty("closingDate");
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
        } else if (ipCardNumber != null) {
            Beneficiary ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(ipCardNumber);
            InsurancePolicy ip = InsurancePolicyUtil.getInsurancePolicyByBeneficiary(ben);
            globalBills = GlobalBillUtil.getGlobalBillsByInsurancePolicy(ip);
        } else if (billIdentifier != null) {
            GlobalBill globalBill = GlobalBillUtil.getGlobalBillByBillIdentifier(billIdentifier);
            if (globalBill != null) globalBills.add(globalBill);
        }

        return new NeedsPaging<>(globalBills, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        BillingService svc = Context.getService(BillingService.class);

        int start = context.getStartIndex() == null ? 0 : Math.max(0, context.getStartIndex());
        int limit = context.getLimit() == null || context.getLimit() <= 0 ? 10 : context.getLimit();

        String orderByRaw     = context.getRequest().getParameter("orderBy");
        String orderDirRaw    = context.getRequest().getParameter("orderDirection");
        String fallbackByRaw  = context.getRequest().getParameter("fallbackOrderBy");
        String fallbackDirRaw = context.getRequest().getParameter("fallbackDirection");

        String orderBy     = normalizeOrderBy(orderByRaw);
        String orderDir    = normalizeDirection(orderDirRaw);
        String fallbackBy  = normalizeOrderBy(fallbackByRaw == null ? "createdDate" : fallbackByRaw);
        String fallbackDir = normalizeDirection(fallbackDirRaw == null ? "desc" : fallbackDirRaw);

        List<GlobalBill> page =
                svc.getGlobalBillsByPagination(start, limit, orderBy, orderDir, fallbackBy, fallbackDir);

        boolean hasMore;
        try {
            long total = svc.getGlobalBillCount();
            hasMore = (start + page.size()) < total;
        } catch (Throwable ignore) {
            hasMore = page.size() >= limit;
        }

        return new org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged<GlobalBill>(context, page, hasMore);
    }

}
