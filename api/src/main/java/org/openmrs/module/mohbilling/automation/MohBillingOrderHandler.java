/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohbilling.automation;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.openmrs.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.transaction.Status;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Handler(supports = Order.class)
@Component
public class MohBillingOrderHandler implements MohBillingHandler<Order> {

    private static final Logger log = LoggerFactory.getLogger(MohBillingOrderHandler.class);

    ConceptService conceptService;

    BillingService billingService;

    @Autowired
    public MohBillingOrderHandler(ConceptService conceptService, @Qualifier("mohBillingService") BillingService billingService) {
        this.conceptService = conceptService;
        this.billingService = billingService;
    }

    private final ThreadLocal<DataHolder> threadLocalData = new ThreadLocal<>();

    /**
     * We set up a ThreadLocal here to collect all Orders that are saved during the course of a transaction,
     * including any nested transactions, which always happen when saving orders due to the new transaction that
     * is created when generating an order number.
     */
    @Override
    public void afterTransactionBegin() {
        if (threadLocalData.get() == null) {
            threadLocalData.set(new DataHolder());
        }
        threadLocalData.get().startTransaction();
        log.trace("afterTransactionBegin: data = {}", threadLocalData.get());
    }

    /**
     * Whenever a new order is saved, add it to the list of orders on the thread that need to be processed
     * Do not add Discontinue orders.  No billing data is currently updated in response to a discontinue order.
     */
    @Override
    public void handleCreatedEntity(Order order) {
        DataHolder data = threadLocalData.get();
        // Do not bill discontinued orders
        if (order.getAction() == Order.Action.DISCONTINUE) {
            log.debug("Order {} is a discontinue order.  Not adding to data", order);
        }
        else {
            data.addNewOrder(order);
            log.debug("Order {} added to data {}", order, data);
        }
    }

    /**
     * Before the transaction completes, we check to see if we are in a nested transaction or at the root transaction
     * If we are at the root transaction, and it is about to complete, then we know at this point we have all orders
     * that have been saved within the transaction.  This is the point where we want to create billable items.
     * As exceptions will cause the entire transaction to fail, and commits will be part of the same transaction.
     * We do all orders together here, rather than as they are saved, so that we can group them by department
     * as well as ensure that any duplicate checking that is needed can be performed.
     * We group the orders based on department, so that all billable items for the same department will be included
     * on the same bill, rather than have one bill per order.  This allows all lab orders to be billed together,
     * all pharmacy orders to be billed together, etc.
     */
    @Override
    public void beforeTransactionCompletion() {
        DataHolder data = threadLocalData.get();
        log.trace("beforeTransactionCompletion: data = {}", data);
        if (data.getNewOrders().isEmpty()) {
            return;
        }
        if (data.getNumberOfStartedTransactions() > 1) {
            return;
        }
        if (data.getNumberOfStartedTransactions() == 0) {
            throw new RuntimeException("No existing transactions detected within beforeTransactionCompletion");
        }

        // If we are here, then there is only one started transaction remaining, and we have orders to process

        // Retrieve the patient and validate that all orders are for the same patient
        Patient patient = null;
        for (Order order : data.getNewOrders()) {
            if (patient == null) {
                patient = order.getPatient();
            }
            else {
                if (!patient.equals(order.getPatient())) {
                    throw new RuntimeException("Multiple orders are being saved for different patients in the same tx");
                }
            }
        }

        // Get the patient's insurance.  If none is found, throw an Exception
        InsurancePolicy insurancePolicy = getInsurancePolicyForPatient(patient);
        if (insurancePolicy == null) {
            throw new RuntimeException("No insurance policy found for patient: " + patient);
        }
        log.debug("insurancePolicy: {}", insurancePolicy);

        Beneficiary beneficiary = billingService.getBeneficiaryByPolicyNumber(insurancePolicy.getInsuranceCardNo());
        log.debug("policy beneficiary: {}", beneficiary);

        // Get patient's phone number
        String patientPhoneNumber = getPhoneNumberForPatient(patient);
        log.debug("patientPhoneNumber: {}", patientPhoneNumber);

        // Get the patient's global bill, if none found, throw an Exception
        GlobalBill globalBill = billingService.getOpenGlobalBillByInsuranceCardNo(insurancePolicy.getInsuranceCardNo());
        if (globalBill == null) {
            throw new RuntimeException("No global bill found for patient: " + patient);
        }
        log.debug("Starting global bill amount: {}", globalBill.getGlobalAmount());

        User currentUser = Context.getAuthenticatedUser();
        Date now = new Date();

        Set<Concept> billedConceptSets = new HashSet<>();
        Map<Concept, Concept> conceptsToBillAtSetLevel = getConceptsToBillAtSetLevel();

        // Iterate over each order and create PatientServiceBill for each as appropriate, grouped by department
        Map<Department, List<PatientServiceBill>> patientServiceBillsByDepartment = new HashMap<>();
        for (Order order : data.getNewOrders()) {

            Concept orderable = order.getConcept();

            // Support laboratorymanagement behavior where some tests are billed at the set level
            if (conceptsToBillAtSetLevel.containsKey(orderable)) {
                orderable = conceptsToBillAtSetLevel.get(orderable);
                if (billedConceptSets.contains(orderable)) {
                    continue; // Do not bill the same concept set twice
                }
                billedConceptSets.add(orderable);
            }

            // Get the facility service price associated with this particular order.
            FacilityServicePrice facilityServicePrice = null;
            if (order instanceof DrugOrder) {
                DrugOrder drugOrder = (DrugOrder) order;
                if (drugOrder.getDrug() != null) {
                    log.debug("Getting facility service price for drug by name {}", drugOrder.getDrug().getName());
                    facilityServicePrice = billingService.getFacilityServiceByName(drugOrder.getDrug().getName());
                }
                else {
                    log.debug("No drug configured on drug order");
                }
            }
            if (facilityServicePrice == null) {
                log.debug("Getting facility service price by orderable concept {}", orderable);
                facilityServicePrice = billingService.getFacilityServiceByConcept(orderable);
            }
            if (facilityServicePrice == null) {
                log.debug("No facility service price found, not billing order {}", order);
                continue;
            }
            log.debug("Facility service price {}", facilityServicePrice);
            if (facilityServicePrice.isHidden()) {
                log.debug("Hidden facility service price for {}, not billing order {}", orderable, order);
                continue;
            }

            BillableService billableService = billingService.getBillableServiceByConcept(facilityServicePrice, insurancePolicy.getInsurance());
            HopService hopService = billingService.getHopService(facilityServicePrice.getCategory());
            BigDecimal unitPrice = billableService.getMaximaToPay();
            log.debug("unitPrice: {}", unitPrice);
            BigDecimal quantity = new BigDecimal(1);
            if (order instanceof DrugOrder) {
                DrugOrder drugOrder = (DrugOrder) order;
                if (drugOrder.getQuantity() != null) {
                    quantity = BigDecimal.valueOf(drugOrder.getQuantity());
                }
                else {
                    log.warn("drugOrder without quantity, defaulting to 1");
                }
            }
            log.debug("quantity: {}", quantity);

            // Get the department for the billable service
            Department department = getDepartmentForOrder(order);
            if (department == null) {
                throw new RuntimeException("Unable to determine the department for order to bill");
            }
            log.debug("Department: {}", department.getName());

            PatientServiceBill patientServiceBill = new PatientServiceBill();
            patientServiceBill.setService(billableService);
            patientServiceBill.setServiceDate(now);
            patientServiceBill.setUnitPrice(unitPrice);
            patientServiceBill.setQuantity(quantity);
            patientServiceBill.setHopService(hopService);
            patientServiceBill.setCreator(currentUser);
            patientServiceBill.setCreatedDate(now);
            patientServiceBill.setItemType(1);
            patientServiceBillsByDepartment.computeIfAbsent(department, k -> new ArrayList<>()).add(patientServiceBill);
        }

        // For each department, create appropriate bills
        BigDecimal globalBillAmount = globalBill.getGlobalAmount();
        for (Department department : patientServiceBillsByDepartment.keySet()) {
            BigDecimal amountForDepartment = BigDecimal.ZERO;
            List<PatientServiceBill> patientServiceBills = patientServiceBillsByDepartment.get(department);
            log.debug("Processing {} patientServiceBills for department {}", patientServiceBills.size(), department);
            for (PatientServiceBill patientServiceBill : patientServiceBills) {
                BigDecimal billAmount = patientServiceBill.getQuantity().multiply(patientServiceBill.getUnitPrice());
                globalBillAmount = globalBillAmount.add(billAmount);
                amountForDepartment = amountForDepartment.add(billAmount);
            }
            PatientBill patientBill;
            if (StringUtils.isNotBlank(patientPhoneNumber)) {
                patientBill = PatientBillUtil.createPatientBillWithMoMoRequestToPay(amountForDepartment, insurancePolicy, patientPhoneNumber, UUID.randomUUID().toString());
            }
            else {
                patientBill = PatientBillUtil.createPatientBill(amountForDepartment, insurancePolicy);
            }
            log.debug("patientBill: {}", patientBill);
            data.addPatientBillId(patientBill.getPatientBillId());
            ThirdPartyBill thirdPartyBill = ThirdPartyBillUtil.createThirdPartyBill(insurancePolicy, amountForDepartment);
            log.info("thirdPartyBill: {}", thirdPartyBill);
            InsuranceBill insuranceBill = InsuranceBillUtil.createInsuranceBill(insurancePolicy.getInsurance(), amountForDepartment);
            log.info("insuranceBill: {}", insuranceBill);

            Consommation consommation = new Consommation();
            consommation.setDepartment(department);
            consommation.setBeneficiary(beneficiary);
            consommation.setGlobalBill(globalBill);
            consommation.setCreatedDate(now);
            consommation.setCreator(currentUser);
            consommation.setPatientBill(patientBill);
            consommation.setThirdPartyBill(thirdPartyBill);
            consommation.setInsuranceBill(insuranceBill);
            for (PatientServiceBill patientServiceBill : patientServiceBills) {
                patientServiceBill.setConsommation(consommation);
                consommation.addBillItem(patientServiceBill);
            }
            billingService.saveConsommation(consommation);
            log.debug("consommation: {}", consommation);
        }

        // Update global bill amount
        globalBill.setGlobalAmount(globalBillAmount);
        billingService.saveGlobalBill(globalBill);
        log.debug("Ending global bill amount: {}", globalBill.getGlobalAmount());
    }

    /**
     * After the transaction completes successfully, we send out any text messages and other external integrations.
     * We perform this here as this is the point where we know that the data has been successfully committed to the
     * database, based on the status variable that is passed in.
     * This prevents sending messages about bills that failed to save, and this also allows sending one message
     * for each individual bill, rather than one message per orderable.
     */
    @Override
    public void afterTransactionCompletion(int status) {
        DataHolder data = threadLocalData.get();
        data.endTransaction();
        log.trace("afterTransactionCompletion: status = {}; data = {}", status, data);
        // After all transaction data has been processed, send request to pay, if transaction successfully committed
        if (data.getNumberOfStartedTransactions() == 0) {
            try {
                if (status == Status.STATUS_COMMITTED) {
                    for (Integer patientBillId : data.getPatientBillsCreated()) {
                        PatientBill patientBill = billingService.getPatientBill(patientBillId);
                        if (patientBill != null) {
                            try {
                                BigDecimal amountToPay = patientBill.getAmount().setScale(0, RoundingMode.UP);
                                String phoneNumber = patientBill.getPhoneNumber();
                                String referenceId = patientBill.getReferenceId();
                                log.debug("patientBillId: {}, referenceId: {}, phoneNumber: {}, amountToPay: {}", patientBillId, referenceId, phoneNumber, amountToPay);
                                if (StringUtils.isNotBlank(phoneNumber) && StringUtils.isNotBlank(referenceId)) {
                                    if (amountToPay.compareTo(BigDecimal.ZERO) > 0) {
                                        log.info("Sending MTN Request to pay {} to {}, referenceId {}", amountToPay, phoneNumber, referenceId);
                                        MTNMomoApiIntegrationRequestToPay momo = new MTNMomoApiIntegrationRequestToPay();
                                        momo.requesttopay(referenceId, amountToPay.toString(), phoneNumber);
                                        patientBill.setTransactionStatus(momo.getransactionStatus(referenceId));
                                        PatientBillUtil.savePatientBill(patientBill);
                                    } else {
                                        log.debug("Amount to pay is not > 0, not sending");
                                    }
                                } else {
                                    log.debug("Patient phone number is null, not sending");
                                }
                            } catch (Exception e) {
                                log.error("Unable to send MTN Request to pay or updating patient bill", e);
                            }
                        }
                        else {
                            log.warn("afterTransactionCompletion: patientBill not found: {}", patientBillId);
                        }
                    }
                }
                else {
                    log.trace("afterTransactionCompletion: tx was not committed.");
                }
            } finally {
                log.trace("Finally block afterTransactionCompletion: data = {}", data);
                threadLocalData.remove();
                log.trace("Removed threadLocalData");
            }
        }
    }

    /**
     * At this point, no changes to billing are done if orders are updated.
     * Generally, orders are immutable, though they can be discontinued, expired, or be marked with a fulfiller status
     * that indicates they will not be performed.  If we need to handle that and update billable items in repsonse,
     * then this would be the place to do it.
     */
    @Override
    public void handleUpdatedEntity(Order order) {
        log.trace("handleUpdatedEntity is not configured to modify billing: order = {}", order);
    }

    /**
     * At this point, no changes to billing are done if orders are deleted (either purged or voided)
     * If we need to handle updates to billables due to deleted orders, this would be the place to do it.
     */
    @Override
    public void handleDeletedEntity(Order order) {
        log.trace("handleDeletedEntity is not configured to modify billing: order = {}", order);
    }

    /**
     * @param conceptRef the uuid, id, or name to look up the concept by
     * @return a concept by uuid, id, or name
     */
    Concept getConcept(String conceptRef) {
        conceptRef = conceptRef.trim();
        Concept concept = conceptService.getConceptByUuid(conceptRef);
        if (concept == null) {
            concept = conceptService.getConcept(conceptRef);
        }
        return concept;
    }

    /**
     * @return the InsurancePolicy for a given patient
     */
    InsurancePolicy getInsurancePolicyForPatient(Patient patient) {
        String insuranceNumberConceptRef = ConfigUtil.getGlobalProperty("registration.insuranceNumberConcept");
        Concept insuranceNumberConcept = getConcept(insuranceNumberConceptRef);
        log.debug("insuranceNumberConcept: {}", insuranceNumberConcept);
        List<Obs> latestInsuranceObs = Utils.getLastNObservations(1, patient, insuranceNumberConcept, false);
        String insuranceCardNumber = latestInsuranceObs.isEmpty() ? null : latestInsuranceObs.get(0).getValueText();
        log.debug("insuranceCardNumber: {}", insuranceCardNumber);
        return billingService.getInsurancePolicyByCardNo(insuranceCardNumber);
    }

    /**
     * @return the phone number for a given patient
     */
    String getPhoneNumberForPatient(Patient patient) {
        String epaymentPhoneNumberConceptRef = ConfigUtil.getGlobalProperty("registration.ePaymentPhoneNumberConcept");
        if (StringUtils.isBlank(epaymentPhoneNumberConceptRef)) {
            log.warn("registration.ePaymentPhoneNumberConcept global property is not configured");
            return null;
        }
        Concept epaymentPhoneNumberConcept = getConcept(epaymentPhoneNumberConceptRef);
        if (epaymentPhoneNumberConcept == null) {
            log.warn("registration.ePaymentPhoneNumberConcept not found: {}", epaymentPhoneNumberConceptRef);
        }
        List<Obs> currentPhoneNumbers = Utils.getLastNObservations(1, patient, epaymentPhoneNumberConcept, false);
        String currentPhoneNumber = null;
        if (!currentPhoneNumbers.isEmpty()) {
            currentPhoneNumber = currentPhoneNumbers.get(0).getValueText();
        }
        return StringUtils.isBlank(currentPhoneNumber) ? null : currentPhoneNumber;
    }

    /**
     * @return the Department applicable for the given order
     */
    Department getDepartmentForOrder(Order order) {
        Department department = null;
        for (Department dept : billingService.getAllDepartements()) {
            String deptName = dept.getName().trim().toUpperCase();
            if (order instanceof TestOrder && ("LABORATOIRE".equals(deptName) || "LABORATORY".equals(deptName))) {
                department = dept;
                break;
            }
            else if (order instanceof DrugOrder && ("PHARMACIE".equals(deptName) || "PHARMACY".equals(deptName))) {
                department = dept;
                break;
            }
        }
        // If department is still null, look for an observation within the encounter the indicates the service
        if (department == null) {
            Concept service = null;
            Concept hospitalServices = getConcept("HOSPITAL SERVICES");
            if (hospitalServices == null) {
                hospitalServices = getConcept("a29f03ad-be6b-4958-b47e-eb9235d364a4");
            }
            if (hospitalServices != null) {
                for (Obs o : order.getEncounter().getAllObs()) {
                    if (o.getConcept().equals(hospitalServices)) {
                        service = o.getValueCoded();
                        log.debug("Found hospital service in encounter: {}", service);
                        break;
                    }
                }
                if (service == null) {
                    log.warn("No hospital service found in encounter");
                }
                else {
                    String serviceName = service.getName().getName().trim().toUpperCase();
                    for (Department dept : billingService.getAllDepartements()) {
                        String deptName = dept.getName().trim().toUpperCase();
                        if (serviceName.equals(deptName)) {
                            department = dept;
                        }
                    }
                }
            }
            else {
                log.warn("No hospital services concept found");
            }
        }
        return department;
    }

    /**
     * This supports the laboratorymanagement configuration where some tests are billed based on the set they are in
     * @return concept to set it should be billed in
     */
    public Map<Concept, Concept> getConceptsToBillAtSetLevel() {
        Map<Concept, Concept> ret = new HashMap<>();
        String gpVal = ConfigUtil.getGlobalProperty("laboratorymanagement.conceptSetsToBill");
        if (StringUtils.isNotBlank(gpVal)) {
            for (String conceptSetRef : gpVal.split(",")) {
                Concept conceptSet = getConcept(conceptSetRef);
                List<Concept> conceptSetMembers = conceptService.getConceptsByConceptSet(conceptSet);
                for (Concept setMember : conceptSetMembers) {
                    ret.put(setMember, conceptSet);
                }
            }
        }
        return ret;
    }

    /**
     * Data to store in the ThreadLocal
     */
    public static class DataHolder {

        private int numberOfStartedTransactions = 0;
        private final Set<Order> newOrders = new HashSet<>();
        private final Set<Integer> patientBillsCreated = new HashSet<>();

        public DataHolder() {}

        public String toString() {
            return "transactions: " + numberOfStartedTransactions +
                    "; newOrders: " + newOrders.size() +
                    "; patientBillsCreated: " + patientBillsCreated.size();
        }

        public void startTransaction() {
            numberOfStartedTransactions++;
        }

        public void endTransaction() {
            numberOfStartedTransactions--;
        }

        public int getNumberOfStartedTransactions() {
            return numberOfStartedTransactions;
        }

        public void addNewOrder(Order order) {
            newOrders.add(order);
        }

        public Set<Order> getNewOrders() {
            return newOrders;
        }

        public void addPatientBillId(Integer patientBillId) {
            patientBillsCreated.add(patientBillId);
        }

        public Set<Integer> getPatientBillsCreated() {
            return patientBillsCreated;
        }
    }
}
