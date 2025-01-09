package org.openmrs.module.mohbilling.impl;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingProcessingService;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.BillingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service("billingProcessingService")
@Transactional
public class BillingProcessingServiceImpl extends BaseOpenmrsService implements BillingProcessingService {

    private BillingDAO billingDAO;

    public void setBillingDAO(BillingDAO billingDAO) {
        this.billingDAO = billingDAO;
    }

    @Override
    @Transactional(timeout = 30)
    public PatientBill createBill(Patient patient, List<Obs> observations, String insuranceCardNumber) {
        BillingService billingService = Context.getService(BillingService.class);

        // Fetch insurance policy
        InsurancePolicy insurancePolicy = billingService.getInsurancePolicyByCardNo(insuranceCardNumber);
        if (insurancePolicy == null) {
            throw new IllegalArgumentException("Insurance policy not found for the provided card number.");
        }

        // Generate service bills using BillingUtils
        BigDecimal totalMaximaToPay = BigDecimal.ZERO;
        List<PatientServiceBill> serviceBills = BillingUtils.generateServiceBills(observations, insurancePolicy);
        for (PatientServiceBill psb : serviceBills) {
            totalMaximaToPay = totalMaximaToPay.add(psb.getUnitPrice().multiply(psb.getQuantity()));
        }

        // Update the global bill
        GlobalBill globalBill = billingService.getOpenGlobalBillByInsuranceCardNo(insurancePolicy.getInsuranceCardNo());
        if (globalBill == null) {
            throw new IllegalStateException("No open global bill found for the insurance card number.");
        }
        globalBill.setGlobalAmount(globalBill.getGlobalAmount().add(totalMaximaToPay));
        billingService.saveGlobalBill(globalBill);

        // Create and save patient bill
        PatientBill patientBill = new PatientBill();
        patientBill.setAmount(totalMaximaToPay);
        patientBill.setCreator(Context.getAuthenticatedUser());
        patientBill.setCreatedDate(new Date());
        billingService.savePatientBill(patientBill);

        // Save consommation
        Consommation consommation = new Consommation();
        consommation.setBeneficiary(billingService.getBeneficiaryByPolicyNumber(insuranceCardNumber));
        consommation.setPatientBill(patientBill);
        consommation.setGlobalBill(globalBill);
        consommation.setCreatedDate(new Date());
        consommation.setCreator(Context.getAuthenticatedUser());
        billingService.saveConsommation(consommation);

        // Attach bills to consommation
        for (PatientServiceBill psb : serviceBills) {
            psb.setConsommation(consommation);
            billingService.saveBilledItem(psb);
        }

        return patientBill;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientBill getPatientBillById(int id) {
        return Context.getService(BillingService.class).getPatientBill(id);
    }

    @Override
    @Transactional
    public void voidPatientBill(PatientBill patientBill, User voidedBy, Date voidedDate, String voidReason) {
        if (patientBill != null) {
            patientBill.setVoided(true);
            patientBill.setVoidedBy(voidedBy);
            patientBill.setVoidedDate(voidedDate);
            patientBill.setVoidReason(voidReason);
            Context.getService(BillingService.class).savePatientBill(patientBill);
        }
    }

    @Override
    @Transactional
    public PatientBill savePatientBill(PatientBill patientBill) {
        return Context.getService(BillingService.class).savePatientBill(patientBill);
    }

    @Override
    public List<PatientBill> getAllPatientBills() {
    return Context.getService(BillingService.class).getAllPatientBills();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> getPatientBillsByPagination(Integer startIndex, Integer pageSize,
            String orderBy, String orderDirection) {
        return Context.getService(BillingService.class)
                .getPatientBillsByPagination(startIndex, pageSize, orderBy, orderDirection);
    }
}
