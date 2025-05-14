package org.openmrs.module.mohbilling.rest.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingProcessingService;
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
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/mohbilling/patientBill",
        supportedClass = PatientBill.class,
        supportedOpenmrsVersions = {"2.0 - 2.*"})
public class PatientBillResource extends DelegatingCrudResource<PatientBill> {

    private static final Log log = LogFactory.getLog(PatientBillResource.class);

    @Override
    public PatientBill getByUniqueId(String uniqueId) {
        return Context.getService(BillingProcessingService.class).getPatientBillById(Integer.parseInt(uniqueId));
    }

    @Override
    public PatientBill newDelegate() {
        return new PatientBill();
    }

    @Override
    public PatientBill save(PatientBill patientBill) {
        if (patientBill.getCreator() == null) {
            patientBill.setCreator(Context.getAuthenticatedUser());
        }
        if (patientBill.getCreatedDate() == null) {
            patientBill.setCreatedDate(new Date());
        }
        return Context.getService(BillingProcessingService.class).savePatientBill(patientBill);
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

    /**
     * Property getter for creator name
     */
    @PropertyGetter("creator")
    public String getCreator(PatientBill bill) {
        if (bill.getCreator() != null && bill.getCreator().getPerson() != null) {
            return bill.getCreator().getPerson().getPersonName().getFullName();
        }
        return "--";
    }

    /**
     * Property getter for policyIdNumber
     */
    @PropertyGetter("policyIdNumber")
    public String getPolicyIdNumber(PatientBill bill) {
        try {
            Consommation cons = Context.getService(BillingService.class).getConsommationByPatientBill(bill);
            if (cons != null && cons.getBeneficiary() != null) {
                return cons.getBeneficiary().getPolicyIdNumber();
            }
        } catch (Exception e) {
            log.error("Error getting policyIdNumber", e);
        }
        return "--";
    }

    /**
     * Property getter for beneficiaryName
     */
    @PropertyGetter("beneficiaryName")
    public String getBeneficiaryName(PatientBill bill) {
        try {
            Consommation cons = Context.getService(BillingService.class).getConsommationByPatientBill(bill);
            if (cons != null && cons.getBeneficiary() != null) {
                if (cons.getBeneficiary().getPatient() != null) {
                    return cons.getBeneficiary().getPatient().getPersonName().getFullName();
                } else if (cons.getBeneficiary().getOwnerName() != null) {
                    return cons.getBeneficiary().getOwnerName();
                }
            }
        } catch (Exception e) {
            log.error("Error getting beneficiaryName", e);
        }
        return "--";
    }

    /**
     * Property getter for insuranceName
     */
    @PropertyGetter("insuranceName")
    public String getInsuranceName(PatientBill bill) {
        try {
            Consommation cons = Context.getService(BillingService.class).getConsommationByPatientBill(bill);
            if (cons != null && cons.getBeneficiary() != null &&
                    cons.getBeneficiary().getInsurancePolicy() != null &&
                    cons.getBeneficiary().getInsurancePolicy().getInsurance() != null) {
                return cons.getBeneficiary().getInsurancePolicy().getInsurance().getName();
            }
        } catch (Exception e) {
            log.error("Error getting insuranceName", e);
        }
        return "--";
    }

    /**
     * Property getter for departmentName
     */
    @PropertyGetter("departmentName")
    public String getDepartmentName(PatientBill bill) {
        try {
            Consommation cons = Context.getService(BillingService.class).getConsommationByPatientBill(bill);
            if (cons != null && cons.getDepartment() != null) {
                return cons.getDepartment().getName();
            }
        } catch (Exception e) {
            log.error("Error getting departmentName", e);
        }
        return "--";
    }

    /**
     * Property getter for serviceName - returns the name of the first service in the bill
     */
    @PropertyGetter("serviceName")
    public String getServiceName(PatientBill bill) {
        try {
            Consommation cons = Context.getService(BillingService.class).getConsommationByPatientBill(bill);
            if (cons != null && cons.getBillItems() != null && !cons.getBillItems().isEmpty()) {
                PatientServiceBill item = cons.getBillItems().iterator().next(); // Get first item

                if (item.getService() != null &&
                        item.getService().getFacilityServicePrice() != null) {
                    return item.getService().getFacilityServicePrice().getName();
                } else if (item.getServiceOtherDescription() != null) {
                    return item.getServiceOtherDescription();
                } else if (item.getServiceOther() != null) {
                    return item.getServiceOther();
                }
            }

            // Fall back to department name if no items
            return getDepartmentName(bill);
        } catch (Exception e) {
            log.error("Error getting service name", e);
            return getDepartmentName(bill);
        }
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
            description.addProperty("paymentConfirmedDate");

            description.addProperty("creator");
            description.addProperty("departmentName");
            description.addProperty("policyIdNumber");
            description.addProperty("beneficiaryName");
            description.addProperty("insuranceName");

            description.addProperty("serviceName");

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

            description.addProperty("creator");
            description.addProperty("departmentName");
            description.addProperty("policyIdNumber");
            description.addProperty("beneficiaryName");
            description.addProperty("insuranceName");

            description.addProperty("serviceName");

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

            description.addProperty("policyIdNumber");
            description.addProperty("beneficiaryName");
            description.addProperty("insuranceName");

            description.addProperty("serviceName");

            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        log.debug("Starting doGetAll with parameters: startIndex=" + context.getStartIndex() +
                ", limit=" + context.getLimit());

        BillingProcessingService service = Context.getService(BillingProcessingService.class);

        Integer startIndex = context.getStartIndex();
        Integer limit = context.getLimit();
        String orderBy = context.getRequest().getParameter("orderBy");
        String orderDirection = context.getRequest().getParameter("order");

        List<PatientBill> bills = service.getPatientBillsByPagination(
                startIndex,
                limit,
                orderBy,
                orderDirection
        );

        log.debug("Retrieved " + (bills != null ? bills.size() : "null") + " bills");

        return new AlreadyPaged<>(context, bills, false);
    }

    @PropertySetter("amount")
    public void setAmount(PatientBill instance, Object value) {
        try {
            if (value == null) {
                instance.setAmount(null);
            } else if (value instanceof BigDecimal) {
                instance.setAmount((BigDecimal) value);
            } else if (value instanceof Number) {
                instance.setAmount(new BigDecimal(value.toString()));
            } else if (value instanceof String) {
                instance.setAmount(new BigDecimal((String) value));
            } else {
                throw new ConversionException("Unable to convert " + value.getClass() + " to BigDecimal");
            }
        } catch (Exception e) {
            throw new ConversionException("Unable to convert " + value + " to BigDecimal: " + e.getMessage());
        }
    }
}
