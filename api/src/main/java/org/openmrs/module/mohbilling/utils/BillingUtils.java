package org.openmrs.module.mohbilling.utils;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BillingUtils {

    /**
     * Generates a list of PatientServiceBill objects from observations and an insurance policy.
     *
     * @param observations    The list of observations to process
     * @param insurancePolicy The insurance policy for the patient
     * @return A list of PatientServiceBill objects
     */
    public static List<PatientServiceBill> generateServiceBills(List<Obs> observations, InsurancePolicy insurancePolicy) {
        List<PatientServiceBill> serviceBills = new ArrayList<>();
        BillingService billingService = Context.getService(BillingService.class);

        for (Obs obs : observations) {
            FacilityServicePrice facilityServicePrice = billingService.getFacilityServiceByConcept(obs.getConcept());
            if (facilityServicePrice != null && !facilityServicePrice.isHidden()) {
                BillableService billableService = billingService.getBillableServiceByConcept(facilityServicePrice, insurancePolicy.getInsurance());
                if (billableService != null) {
                    PatientServiceBill serviceBill = new PatientServiceBill();
                    serviceBill.setService(billableService);
                    serviceBill.setServiceDate(obs.getObsDatetime());
                    serviceBill.setUnitPrice(billableService.getMaximaToPay());
                    serviceBill.setQuantity(BigDecimal.ONE); // Default to 1 unless otherwise specified
                    serviceBill.setCreatedDate(obs.getDateCreated());
                    serviceBill.setCreator(obs.getCreator());
                    serviceBill.setItemType(1); // Define appropriate item type
                    serviceBills.add(serviceBill);
                }
            }
        }

        return serviceBills;
    }

    public static BigDecimal convertRawValueToBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof String) {
            return new BigDecimal((String) value);
        } else if (value instanceof Long) {
            return BigDecimal.valueOf((Long) value);
        } else if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        } else if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        } else if (value instanceof Double) {
            return new BigDecimal(String.valueOf(value));
        } else if (value instanceof Number) {
            return new BigDecimal(String.valueOf(((Number) value).doubleValue()));
        } else {
            throw new IllegalArgumentException("Cannot convert object of type " + value.getClass().getName() + " to BigDecimal.");
        }
    }
}
