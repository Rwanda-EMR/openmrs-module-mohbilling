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
package org.openmrs.module.mohbilling.advice;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.guice.RequestScoped;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.automation.MTNMomoApiIntegrationRequestToPay;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.springframework.aop.AfterReturningAdvice;

@RequestScoped
public class OrderServiceAdvice implements AfterReturningAdvice {

    private final Log log = LogFactory.getLog(this.getClass());

    @Override
    public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {
        if (method.getName().equals("saveOrder") && objects.length > 0 && objects[0] instanceof Order) {
            Order order = (Order) objects[0];
            Patient patient = order.getPatient();
            String referenceId = (UUID.randomUUID()).toString();
            String epaymentPhoneNumberUUID = Context.getAdministrationService().getGlobalProperty("registration.ePaymentPhoneNumberConcept");
            List<Obs> currentPhoneNumbers = Utils.getLastNObservations(1, patient, Context.getConceptService().getConceptByUuid(epaymentPhoneNumberUUID), false);

            String currentPhoneNumber = null;
            if (!currentPhoneNumbers.isEmpty()) {
                currentPhoneNumber = currentPhoneNumbers.get(0).getValueText();
            }

            Integer insuranceNumberConceptID = Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept"));

            String insuranceCardNumber = null;

            List<Obs> currentInsuranceId = Utils.getLastNObservations(1, patient, Context.getConceptService().getConcept(insuranceNumberConceptID), false);

            if (currentInsuranceId.size() >= 1) {
                insuranceCardNumber = currentInsuranceId.get(0).getValueText();
                log.info("insuranceCardNumber: " + insuranceCardNumber);
            }

            InsurancePolicy ip = Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);
            log.info("ip: ----------- : " + ip);

            if (ip == null) {
                ip = Context.getService(BillingService.class).getAllInsurancePoliciesByPatient(patient).stream()
                        .filter(insurancePolicy -> insurancePolicy.getExpirationDate().after(new Date()))
                        .findFirst().orElse(null);
                if (insuranceCardNumber == null) {
                    insuranceCardNumber = ip.getInsuranceCardNo();
                }
            }

            List<PatientServiceBill> psbList = new ArrayList<>();
            Department department = null;
            BigDecimal totalMaximaTopay = new BigDecimal(0);
            PatientServiceBill psb = new PatientServiceBill();


            for (Department dept : Context.getService(BillingService.class).getAllDepartements()) {
                if (order.getConcept().getName().getName().trim().equals(dept.getName().trim())) {
                    department = dept;
                    break;
                }
            }

            FacilityServicePrice fsp = Context.getService(BillingService.class).getFacilityServiceByConcept(order.getConcept());
            if (ip != null && fsp != null && !fsp.isHidden()) {
                log.info("FacilityServicePrice Found: " + fsp.getName());
                BillableService bs = Context.getService(BillingService.class).getBillableServiceByConcept(fsp, ip.getInsurance());
                totalMaximaTopay = totalMaximaTopay.add(bs.getMaximaToPay());
                log.info("BillableService maxima_to_pay: " + bs.getMaximaToPay());
                psb.setService(bs);
                psb.setServiceDate(new Date());
                psb.setUnitPrice(bs.getMaximaToPay());
                psb.setQuantity(new BigDecimal(1));
                psb.setHopService(Context.getService(BillingService.class).getHopService(fsp.getCategory()));
                psb.setCreator(Context.getAuthenticatedUser());
                psb.setCreatedDate(new Date());
                psb.setItemType(1);

                psbList.add(psb);
            }

            if (psbList.size() > 0) {
                log.info("psbList.size(): " + psbList.size());
                GlobalBill gb = Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());
                BigDecimal globalAmount = gb.getGlobalAmount().add(totalMaximaTopay);
                gb.setGlobalAmount(globalAmount);
                gb = GlobalBillUtil.saveGlobalBill(gb);
                log.info("gb: " + gb);

                PatientBill pb = PatientBillUtil.createPatientBill(totalMaximaTopay, ip);
                log.info("pb: " + pb);
                ThirdPartyBill tpb = ThirdPartyBillUtil.createThirdPartyBill(ip, totalMaximaTopay);
                log.info("tpb: " + tpb);
                InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(ip.getInsurance(), totalMaximaTopay);
                log.info("ib: " + ib);

                if (currentPhoneNumber != null) {
                    log.info("currentPhoneNumber: " + currentPhoneNumber);
                    pb = PatientBillUtil.createPatientBillWithMoMoRequestToPay(totalMaximaTopay, ip, currentPhoneNumber, referenceId);
                    log.info("pb: " + pb);
                    try {
                        MTNMomoApiIntegrationRequestToPay momo = new MTNMomoApiIntegrationRequestToPay();
                        momo.requesttopay(referenceId, pb.getAmount().setScale(0, RoundingMode.UP).toString(), currentPhoneNumber);
                        pb.setTransactionStatus(momo.getransactionStatus(referenceId));
                        pb = PatientBillUtil.savePatientBill(pb);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                Consommation cons = new Consommation();
                cons.setBeneficiary(Context.getService(BillingService.class).getBeneficiaryByPolicyNumber(insuranceCardNumber));
                cons.setPatientBill(pb);
                cons.setInsuranceBill(ib);
                cons.setThirdPartyBill(tpb);
                cons.setGlobalBill(gb);
                cons.setCreatedDate(new Date());
                cons.setCreator(Context.getAuthenticatedUser());
                cons.setDepartment(department);
                ConsommationUtil.saveConsommation(cons);
                log.info("cons: " + cons);

                for (PatientServiceBill patientServiceBill : psbList) {
                    patientServiceBill.setConsommation(cons);
                    ConsommationUtil.createPatientServiceBill(patientServiceBill);
                    log.info("patientServiceBill: " + patientServiceBill);
                }
            }
        }
    }
}
