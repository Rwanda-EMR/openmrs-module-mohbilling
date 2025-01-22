package org.openmrs.module.mohbilling.rest.controller;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.automation.MTNMomoApiIntegrationRequestToPay;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.rest.dto.BillResponseDTO;
import org.openmrs.module.mohbilling.rest.model.PostSubmissionObs;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/mohbilling/afterPostSubmission")
public class AfterPostSubmissionController extends BaseRestController {

    @Autowired
    ObsService obsService;
    private static final Logger log = LoggerFactory.getLogger(AfterPostSubmissionController.class);

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> postSubmission(@RequestBody PostSubmissionObs postSubmissionObs) {
        try {
            if (postSubmissionObs == null || postSubmissionObs.getObs() == null || postSubmissionObs.getObs().isEmpty()) {
                return new ResponseEntity<>("No observations provided", HttpStatus.BAD_REQUEST);
            }

            MTNMomoApiIntegrationRequestToPay momo = new MTNMomoApiIntegrationRequestToPay();
            String currentPhoneNumber = null;
            String referenceId = (UUID.randomUUID()).toString();
            String epaymentPhoneNumberUUID = Context.getAdministrationService()
                    .getGlobalProperty("registration.ePaymentPhoneNumberConcept");

            List<Object> createdBills = new ArrayList<>();

            // Get patient from first Obs
            Obs firstObs = obsService.getObsByUuid(postSubmissionObs.getObs().get(0).getUuid());
            if (firstObs == null) {
                return new ResponseEntity<>("No observation found with provided UUID", HttpStatus.NOT_FOUND);
            }

            Patient patient = firstObs.getEncounter().getPatient();
            if (patient == null) {
                return new ResponseEntity<>("No patient found for the observation", HttpStatus.NOT_FOUND);
            }

            List<Obs> currentPhoneNumbers = Utils.getLastNObservations(1, patient,
                    Context.getConceptService().getConceptByUuid(epaymentPhoneNumberUUID), false);

            if (currentPhoneNumbers.size() >= 1) {
                currentPhoneNumber = currentPhoneNumbers.get(0).getValueText();
            }

            Integer insuranceNumberConceptID = Integer.parseInt(Context.getAdministrationService()
                    .getGlobalProperty("registration.insuranceNumberConcept"));

            String insuranceCardNumber = null;
            List<Obs> currentInsuranceId = Utils.getLastNObservations(1, patient,
                    Context.getConceptService().getConcept(insuranceNumberConceptID), false);

            if (currentInsuranceId.size() >= 1) {
                insuranceCardNumber = currentInsuranceId.get(0).getValueText();
            }

            InsurancePolicy ip = Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);
            if (ip == null) {
                return new ResponseEntity<>("No insurance policy found for card number: " + insuranceCardNumber,
                        HttpStatus.NOT_FOUND);
            }

            List<PatientServiceBill> psbList = new ArrayList<>();
            Department department = null;
            BigDecimal totalMaximaTopay = new BigDecimal(0);

            // Process each Obs
            for (PostSubmissionObs.ObsIdentifier obsId : postSubmissionObs.getObs()) {
                Obs o = obsService.getObsByUuid(obsId.getUuid());
                if (o == null) continue;

                PatientServiceBill psb = new PatientServiceBill();

                if (department == null && o.getValueCoded() != null) {
                    for (Department dept : Context.getService(BillingService.class).getAllDepartements()) {
                        if (o.getValueCoded().getName().getName().trim().equals(dept.getName().trim())) {
                            department = dept;
                            break;
                        }
                    }
                }

                // Process ValueCoded
                if (o.getValueCoded() != null) {
                    processValueCodedObs(o, ip, psb, totalMaximaTopay, psbList);
                }
                // Process ValueNumeric
                if (o.getValueNumeric() != null) {
                    processValueNumericObs(o, ip, psb, totalMaximaTopay, psbList);
                }
                // Process ValueBoolean
                if (o.getValueBoolean() != null) {
                    processValueBooleanObs(o, ip, psb, totalMaximaTopay, psbList);
                }
            }

            if (department == null) {
                return new ResponseEntity<>("No department found for the given observations",
                        HttpStatus.NOT_FOUND);
            }

            List<BillResponseDTO> responseList = new ArrayList<>();

            if (psbList.isEmpty()) {
                return new ResponseEntity<>("No billable services found", HttpStatus.NOT_FOUND);
            }

            // Get the global bill
            GlobalBill gb = Context.getService(BillingService.class)
                    .getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());

            if (gb == null) {
                return new ResponseEntity<>("No open global bill found for insurance card: "
                        + ip.getInsuranceCardNo(), HttpStatus.NOT_FOUND);
            }

            // Handle global amount calculations with null checks
            BigDecimal globalAmount = gb.getGlobalAmount();
            if (globalAmount == null) {
                globalAmount = BigDecimal.ZERO;
            }

            if (totalMaximaTopay == null) {
                totalMaximaTopay = BigDecimal.ZERO;
            }

            globalAmount = globalAmount.add(totalMaximaTopay);
            gb.setGlobalAmount(globalAmount);
            gb = GlobalBillUtil.saveGlobalBill(gb);

            // Create bills with null checks
            PatientBill pb = PatientBillUtil.createPatientBill(totalMaximaTopay, ip);
            if (pb == null) {
                return new ResponseEntity<>("Failed to create patient bill",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(ip.getInsurance(), totalMaximaTopay);
            if (ib == null) {
                return new ResponseEntity<>("Failed to create insurance bill",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Handle third party bill creation
            ThirdPartyBill tpb = null;
            if (ip.getThirdParty() != null) {
                tpb = ThirdPartyBillUtil.createThirdPartyBill(ip, totalMaximaTopay);
            }

            // Generate UUIDs for each bill
            String patientBillUuid = UUID.randomUUID().toString();
            String insuranceBillUuid = UUID.randomUUID().toString();
            String thirdPartyBillUuid = tpb != null ? UUID.randomUUID().toString() : "";
            String globalBillUuid = UUID.randomUUID().toString();

            // Set UUIDs for each bill
            pb.setUuid(patientBillUuid);
            ib.setUuid(insuranceBillUuid);
            if (tpb != null) {
                tpb.setUuid(thirdPartyBillUuid);
            }
            gb.setUuid(globalBillUuid);

            // Create response DTO with UUIDs
            BillResponseDTO billResponse = new BillResponseDTO(
                    patientBillUuid,
                    insuranceBillUuid,
                    thirdPartyBillUuid,
                    globalBillUuid,
                    firstObs.getUuid(),
                    totalMaximaTopay
            );

            // Handle MoMo payment if phone number exists
            if (currentPhoneNumber != null && !currentPhoneNumber.trim().isEmpty()) {
                processMoMoPayment(pb, ip, currentPhoneNumber, referenceId, totalMaximaTopay, momo);
            }

            // Create and save consumption
            Consommation cons = createConsommation(pb, ib, tpb, gb, department, insuranceCardNumber);
            ConsommationUtil.saveConsommation(cons);

            // Save patient service bills
            for (PatientServiceBill psb : psbList) {
                psb.setConsommation(cons);
                ConsommationUtil.createPatientServiceBill(psb);
                createdBills.add(psb);
            }

            responseList.add(billResponse);
            return new ResponseEntity<>(responseList, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error in postSubmission", e);
            return new ResponseEntity<>("An error occurred: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processValueCodedObs(Obs o, InsurancePolicy ip, PatientServiceBill psb,
                                      BigDecimal totalMaximaTopay, List<PatientServiceBill> psbList) {
        FacilityServicePrice fsp = Context.getService(BillingService.class)
                .getFacilityServiceByConcept(o.getValueCoded());
        if (fsp != null && !fsp.isHidden()) {
            log.debug("FacilityServicePrice Found: {}", fsp.getName());
            BillableService bs = Context.getService(BillingService.class)
                    .getBillableServiceByConcept(fsp, ip.getInsurance());

            setupPatientServiceBill(psb, bs, new BigDecimal(1), fsp, totalMaximaTopay);
            psbList.add(psb);
        }
    }

    private void processValueNumericObs(Obs o, InsurancePolicy ip, PatientServiceBill psb,
                                        BigDecimal totalMaximaTopay, List<PatientServiceBill> psbList) {
        FacilityServicePrice fsp = Context.getService(BillingService.class)
                .getFacilityServiceByConcept(o.getConcept());
        if (fsp != null && !fsp.isHidden()) {
            log.debug("FacilityServicePrice Found: {}", fsp.getName());
            BillableService bs = Context.getService(BillingService.class)
                    .getBillableServiceByConcept(fsp, ip.getInsurance());

            setupPatientServiceBill(psb, bs, BigDecimal.valueOf(o.getValueNumeric()), fsp, totalMaximaTopay);
            psbList.add(psb);
        }
    }

    private void processValueBooleanObs(Obs o, InsurancePolicy ip, PatientServiceBill psb,
                                        BigDecimal totalMaximaTopay, List<PatientServiceBill> psbList) {
        FacilityServicePrice fsp = Context.getService(BillingService.class)
                .getFacilityServiceByConcept(o.getConcept());
        if (fsp != null && !fsp.isHidden()) {
            log.debug("FacilityServicePrice Found: {}", fsp.getName());
            BillableService bs = Context.getService(BillingService.class)
                    .getBillableServiceByConcept(fsp, ip.getInsurance());

            setupPatientServiceBill(psb, bs, new BigDecimal(1), fsp, totalMaximaTopay);
            psbList.add(psb);
        }
    }

    private void setupPatientServiceBill(PatientServiceBill psb, BillableService bs,
                                         BigDecimal quantity, FacilityServicePrice fsp, BigDecimal totalMaximaTopay) {
        log.debug("BillableService maxima_to_pay: {}", bs.getMaximaToPay());
        psb.setService(bs);
        psb.setServiceDate(new Date());
        psb.setUnitPrice(bs.getMaximaToPay());
        psb.setQuantity(quantity);
        psb.setHopService(Context.getService(BillingService.class).getHopService(fsp.getCategory()));
        psb.setCreator(Context.getAuthenticatedUser());
        psb.setCreatedDate(new Date());
        psb.setItemType(1);

        totalMaximaTopay = totalMaximaTopay.add(bs.getMaximaToPay().multiply(quantity));
    }

    private void processMoMoPayment(PatientBill pb, InsurancePolicy ip, String currentPhoneNumber,
                                    String referenceId, BigDecimal totalMaximaTopay, MTNMomoApiIntegrationRequestToPay momo)
            throws IOException {
        pb = PatientBillUtil.createPatientBillWithMoMoRequestToPay(
                totalMaximaTopay,
                ip,
                currentPhoneNumber,
                referenceId
        );

        momo.requesttopay(
                referenceId,
                pb.getAmount().setScale(0, RoundingMode.UP).toString(),
                currentPhoneNumber
        );

        pb.setTransactionStatus(momo.getransactionStatus(referenceId));
        pb = PatientBillUtil.savePatientBill(pb);
    }

    private Consommation createConsommation(PatientBill pb, InsuranceBill ib, ThirdPartyBill tpb,
                                            GlobalBill gb, Department department, String insuranceCardNumber) {
        Consommation cons = new Consommation();
        cons.setBeneficiary(Context.getService(BillingService.class)
                .getBeneficiaryByPolicyNumber(insuranceCardNumber));
        cons.setPatientBill(pb);
        cons.setInsuranceBill(ib);
        cons.setThirdPartyBill(tpb);
        cons.setGlobalBill(gb);
        cons.setCreatedDate(new Date());
        cons.setCreator(Context.getAuthenticatedUser());
        cons.setDepartment(department);
        return cons;
    }
}
