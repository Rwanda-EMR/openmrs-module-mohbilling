package org.openmrs.module.mohbilling.rest.resource;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingProcessingService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
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
import org.openmrs.module.webservices.rest.web.response.ResponseException;


@Resource(name = RestConstants.VERSION_1 + "/mohbilling/patientBill",
        supportedClass = PatientBill.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class PatientBillResource extends DelegatingCrudResource<PatientBill> {

    @Override
    public PatientBill getByUniqueId(String uniqueId) {
        return Context.getService(BillingProcessingService.class).getPatientBillById(Integer.parseInt(uniqueId));
    }

    @Override
    public PatientBill newDelegate() {
        return new PatientBill();
    }

    @Override
    public PatientBill save(PatientBill delegate) {
        return Context.getService(BillingProcessingService.class).savePatientBill(delegate);
    }

    @Override
    protected void delete(PatientBill delegate, String reason, RequestContext context) throws ResponseException {
        Context.getService(BillingProcessingService.class).voidPatientBill(delegate, Context.getAuthenticatedUser(), null, reason);
    }

    @Override
    public void purge(PatientBill delegate, RequestContext context) throws ResponseException {
        // Implementation not required unless we'd need to permanently delete bills
        // BA guidance
    }

    @Override
    protected String getUniqueId(PatientBill delegate) {
        return String.valueOf(delegate.getPatientBillId());
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("patientBillId");
            description.addProperty("amount");
            description.addProperty("createdDate");
            description.addProperty("status");
            description.addProperty("voided");
            description.addProperty("payments");
            description.addProperty("phoneNumber");
            description.addProperty("transactionStatus");
            description.addProperty("paymentConfirmedBy");
            description.addProperty("paymentConfirmedDate");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("patientBillId");
            description.addProperty("amount");
            description.addProperty("createdDate");
            description.addProperty("status");
            description.addProperty("voided");
            description.addProperty("voidedBy");
            description.addProperty("voidedDate");
            description.addProperty("voidReason");
            description.addProperty("payments");
            description.addProperty("phoneNumber");
            description.addProperty("transactionStatus");
            description.addProperty("paymentConfirmedBy");
            description.addProperty("paymentConfirmedDate");
            description.addSelfLink();
            return description;
        } else if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("patientBillId");
            description.addProperty("amount");
            description.addProperty("createdDate");
            description.addProperty("status");
            description.addProperty("voided");
            description.addProperty("payments");
            description.addProperty("phoneNumber");
            description.addProperty("transactionStatus");
            description.addProperty("paymentConfirmedBy");
            description.addProperty("paymentConfirmedDate");
            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("amount");
        description.addRequiredProperty("status");
        return description;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("amount");
        description.addRequiredProperty("status");
        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        BillingProcessingService service = Context.getService(BillingProcessingService.class);

        Integer startIndex = context.getStartIndex();
        Integer limit = context.getLimit();

        // Get order parameters from request
        String orderBy = context.getRequest().getParameter("orderBy");
        String orderDirection = context.getRequest().getParameter("order");

        List<PatientBill> bills = service.getPatientBillsByPagination(
                startIndex,
                limit,
                orderBy,
                orderDirection
        );

        return new AlreadyPaged<>(context, bills, false);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        BillingProcessingService service = Context.getService(BillingProcessingService.class);

        Integer startIndex = context.getStartIndex();
        Integer limit = context.getLimit();

        // Get order parameters from request
        String orderBy = context.getRequest().getParameter("orderBy");
        String orderDirection = context.getRequest().getParameter("order");

        List<PatientBill> bills = service.getPatientBillsByPagination(
                startIndex,
                limit,
                orderBy,
                orderDirection
        );

        return new NeedsPaging<PatientBill>(bills, context);
    }
}
