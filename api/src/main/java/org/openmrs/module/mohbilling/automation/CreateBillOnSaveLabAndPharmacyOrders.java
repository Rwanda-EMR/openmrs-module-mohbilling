package org.openmrs.module.mohbilling.automation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CreateBillOnSaveLabAndPharmacyOrders{

    protected static final Log log = LogFactory.getLog(CreateBillOnSaveLabAndPharmacyOrders.class);

    public static List<Concept> notAvailableOnFacilityPriceList(List<Concept> labOrdersConceptsList){
        //log.error("\n\n\n\notAvailableOnFacilityPriceList----------------------------------\n::::::::::::::;:::" + labOrdersConceptsList.size());
        List<Concept> concepts = new ArrayList<>();
        for(int count=0; count < labOrdersConceptsList.size(); count++){
            //for (Concept concept:labOrdersConceptsList) {
            Concept concept = labOrdersConceptsList.get(count);
            log.error(concept.getDisplayString() + " is being checked for FSP existence!");
            try{
                FacilityServicePrice fsp = Context.getService(BillingService.class).getFacilityServiceByConcept(concept);
                if (fsp!=null) {
                    log.info(concept.getId() + " is found in the Facility Price List.");
                } else {
                    //log.error("The current concept of " + concept.getId() + "::" + concept.getUuid() + " is not in the facility service price: =======> \n");

                    concepts.add(concept);
                }
            } catch(Exception e){
                //log.error("The current concept of " + concept.getId() + "::" + concept.getUuid() + " is not in the facility service price: =======> \n" + e.getMessage());

                concepts.add(concept);
            }
        }
        //log.error("\n\n\n\nGetting out the function now ----------------------------------\n\n\n");
        return concepts;

    }

    public static void createBillOnSaveLabOrders(Set<Concept> labOrdersConceptsList, Patient patient){




        MTNMomoApiIntegrationRequestToPay momo=new MTNMomoApiIntegrationRequestToPay();
        String currentPhoneNumber=null;
        String referenceId = (UUID.randomUUID()).toString();
        String epaymentPhoneNumberUUID=Context.getAdministrationService().getGlobalProperty("registration.ePaymentPhoneNumberConcept");
        List<Obs> currentPhoneNumbers=Utils.getLastNObservations(1,patient,Context.getConceptService().getConceptByUuid(epaymentPhoneNumberUUID),false);

        if(currentPhoneNumbers.size()>=1) {
            currentPhoneNumber = currentPhoneNumbers.get(0).getValueText();
            //currentPhoneNumber = "250788312518"; // For testing
        }




        Integer insuranceNumberConceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept"));
        String insuranceCardNumber=null;
        List<Obs> currentInsuranceId=Utils.getLastNObservations(1,patient,Context.getConceptService().getConcept(insuranceNumberConceptID),false);
        if(currentInsuranceId.size()>=1)
            insuranceCardNumber=currentInsuranceId.get(0).getValueText();
        InsurancePolicy ip =Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);


        List<PatientServiceBill> psbList=new ArrayList<PatientServiceBill>();
        Department department=null;
        if (department==null) {
            for (Department dept : Context.getService(BillingService.class).getAllDepartements()) {
                if ("LABORATOIRE".equals(dept.getName().trim()) || "LABORATORY".equals(dept.getName().trim()) ) {
                    department = dept;
                    break;
                }
            }
        }

        //List<Obs> obs=session.getSubmissionActions().getObsToCreate();
        BigDecimal totalMaximaTopay=new BigDecimal(0);
        for (Concept concept:labOrdersConceptsList) {
            // System.out.println("Billing: Concept Lab Test: " + concept);
            //log.error("The Found concept is this: " + concept);
            PatientServiceBill psb=new PatientServiceBill();
            try{
                FacilityServicePrice fsp = Context.getService(BillingService.class).getFacilityServiceByConcept(concept);
                //log.error("Found Facility Service Price: " + fsp.getName());
                // if(true){
                //     continue;
                // }
                if (fsp!=null) {
                    BillableService bs = Context.getService(BillingService.class).getBillableServiceByConcept(fsp,ip.getInsurance());
                    totalMaximaTopay=totalMaximaTopay.add(bs.getMaximaToPay());
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
            } catch(Exception e){
                //log.error("The current concept of " + concept.getId() + "::" + concept.getUuid() + " is not in the facility service price: =======> \n" + e.getMessage());
            }
        }
        // if(true){
        //     return;
        // }
        if(psbList.size()>0) {
            GlobalBill gb = Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());
            BigDecimal total=gb.getGlobalAmount().add(totalMaximaTopay);
            BigDecimal globalAmount = gb.getGlobalAmount().add(totalMaximaTopay);
            gb.setGlobalAmount(globalAmount);
            gb = GlobalBillUtil.saveGlobalBill(gb);

            PatientBill pb = PatientBillUtil.createPatientBill(totalMaximaTopay, ip);
            InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(ip.getInsurance(), totalMaximaTopay);



            if (currentPhoneNumber!=null){
                pb = PatientBillUtil.createPatientBillWithMoMoRequestToPay(totalMaximaTopay, ip,currentPhoneNumber,referenceId);
                try {
                    momo.requesttopay(referenceId,pb.getAmount().setScale(0, RoundingMode.UP).toString(),currentPhoneNumber);
                    //pb.setTransactionStatus("PENDING");
                    pb.setTransactionStatus(momo.getransactionStatus(referenceId));
                    pb =PatientBillUtil.savePatientBill(pb);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


            Consommation cons = new Consommation();
            cons.setBeneficiary(Context.getService(BillingService.class).getBeneficiaryByPolicyNumber(insuranceCardNumber));
            cons.setPatientBill(pb);
            cons.setInsuranceBill(ib);
            cons.setGlobalBill(gb);
            cons.setCreatedDate(new Date());
            cons.setCreator(Context.getAuthenticatedUser());
            cons.setDepartment(department);
            ConsommationUtil.saveConsommation(cons);

            for (PatientServiceBill psb : psbList) {
                psb.setConsommation(cons);
                ConsommationUtil.createPatientServiceBill(psb);
            }

        }
    }
    public static void checkBilling(){
        System.out.println("Billing is checked");
    }



    public static void createBillOnSavePharmacyOrders(List<DrugOrderedAndQuantinty> phamacyDrugOrderList, Patient patient){

        MTNMomoApiIntegrationRequestToPay momo=new MTNMomoApiIntegrationRequestToPay();
        String currentPhoneNumber=null;
        String referenceId = (UUID.randomUUID()).toString();
        String epaymentPhoneNumberUUID=Context.getAdministrationService().getGlobalProperty("registration.ePaymentPhoneNumberConcept");
        List<Obs> currentPhoneNumbers=Utils.getLastNObservations(1,patient,Context.getConceptService().getConceptByUuid(epaymentPhoneNumberUUID),false);

        if(currentPhoneNumbers.size()>=1) {
            currentPhoneNumber = currentPhoneNumbers.get(0).getValueText();
            //currentPhoneNumber = "250788312518"; // For testing
        }




        Integer insuranceNumberConceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept"));
        String insuranceCardNumber=null;
        List<Obs> currentInsuranceId=Utils.getLastNObservations(1,patient,Context.getConceptService().getConcept(insuranceNumberConceptID),false);
        if(currentInsuranceId.size()>=1)
            insuranceCardNumber=currentInsuranceId.get(0).getValueText();
        InsurancePolicy ip =Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);


        List<PatientServiceBill> psbList=new ArrayList<PatientServiceBill>();
        Department department=null;
        if (department==null) {
            for (Department dept : Context.getService(BillingService.class).getAllDepartements()) {
                if ("PHARMACY".equals(dept.getName().trim()) || "PHARMACIE".equals(dept.getName().trim())) {
                    department = dept;
                    break;
                }
            }
        }
        //List<Obs> obs=session.getSubmissionActions().getObsToCreate();
        BigDecimal totalMaximaTopay=new BigDecimal(0);
        for (DrugOrderedAndQuantinty drug:phamacyDrugOrderList) {
            System.out.println("Billing: Pharmacy order: " + drug.getDrug().getName());

            PatientServiceBill psb=new PatientServiceBill();
            FacilityServicePrice fsp = Context.getService(BillingService.class).getFacilityServiceByName(drug.getDrug().getName());
            if (fsp!=null) {
                BillableService bs = Context.getService(BillingService.class).getBillableServiceByConcept(fsp,ip.getInsurance());
                if (bs!=null) {
                    totalMaximaTopay = totalMaximaTopay.add(bs.getMaximaToPay().multiply(drug.getQuantity()));
                    psb.setService(bs);
                    psb.setServiceDate(new Date());
                    psb.setUnitPrice(bs.getMaximaToPay());
                    psb.setQuantity(drug.getQuantity());
                    psb.setHopService(Context.getService(BillingService.class).getHopService(fsp.getCategory()));
                    psb.setCreator(Context.getAuthenticatedUser());
                    psb.setCreatedDate(new Date());
                    psb.setDrugFrequency(drug.getDrugFrequency());
                    psb.setItemType(1);
                    psbList.add(psb);
                }
            }
        }


        if(psbList.size()>0) {
            GlobalBill gb = Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());
            BigDecimal globalAmount = gb.getGlobalAmount().add(totalMaximaTopay);
            gb.setGlobalAmount(globalAmount);
            gb = GlobalBillUtil.saveGlobalBill(gb);

            PatientBill pb = PatientBillUtil.createPatientBill(totalMaximaTopay, ip);
            InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(ip.getInsurance(), totalMaximaTopay);


            if (currentPhoneNumber!=null){
                pb = PatientBillUtil.createPatientBillWithMoMoRequestToPay(totalMaximaTopay, ip,currentPhoneNumber,referenceId);
                try {
                    momo.requesttopay(referenceId,pb.getAmount().setScale(0, RoundingMode.UP).toString(),currentPhoneNumber);
                    //pb.setTransactionStatus("PENDING");
                    pb.setTransactionStatus(momo.getransactionStatus(referenceId));
                    pb =PatientBillUtil.savePatientBill(pb);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            Consommation cons = new Consommation();
            cons.setBeneficiary(Context.getService(BillingService.class).getBeneficiaryByPolicyNumber(insuranceCardNumber));
            cons.setPatientBill(pb);
            cons.setInsuranceBill(ib);
            cons.setGlobalBill(gb);
            cons.setCreatedDate(new Date());
            cons.setCreator(Context.getAuthenticatedUser());
            cons.setDepartment(department);
            ConsommationUtil.saveConsommation(cons);

            for (PatientServiceBill psb : psbList) {
                psb.setConsommation(cons);
                ConsommationUtil.createPatientServiceBill(psb);
            }

        }
    }




}