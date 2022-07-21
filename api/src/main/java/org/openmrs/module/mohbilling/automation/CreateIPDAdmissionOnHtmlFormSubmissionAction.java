package org.openmrs.module.mohbilling.automation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
// Below tag will be pasted in any IPD admission html form
// <postSubmissionAction class="org.openmrs.module.mohbilling.automation.CreateIPDAdmissionOnHtmlFormSubmissionAction"/>

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;

public class CreateIPDAdmissionOnHtmlFormSubmissionAction implements CustomFormSubmissionAction {


    @Override
    public void applyAction(FormEntrySession session) {
//Integer insuranceTypeconceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceTypeConcept"));
        Integer insuranceNumberConceptID = Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept"));

        String insuranceCardNumber = null;
        String diseaseType = null;
        GlobalBill gb = null;
        Department dp = null;



//List<Obs> currentInsurance=Context.getObsService().getLastNObservations(1,session.getPatient(),Context.getConceptService().getConcept(insuranceTypeconceptID),false);
List<Obs> currentInsuranceId=Utils.getLastNObservations(1,session.getPatient(),Context.getConceptService().getConcept(insuranceNumberConceptID),false);
        List<Obs> obs = session.getSubmissionActions().getObsToCreate();
/*
if(currentInsurance.size()>=1)
    System.out.println("Insuranceeeeeeeeee Company : "+  currentInsurance.get(0).getValueCoded().getName().getName());
*/

if(currentInsuranceId.size()>=1)
    insuranceCardNumber=currentInsuranceId.get(0).getValueText();


        InsurancePolicy ip =Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);
        GlobalBill openGb=Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());

            if (openGb == null) {
                Admission admission = new Admission();
                admission.setAdmissionDate(new Date());
                admission.setInsurancePolicy(ip);
                admission.setIsAdmitted(true);
                admission.setCreator(Context.getAuthenticatedUser());
                admission.setCreatedDate(new Date());


              /*  List<Obs> obs = session.getSubmissionActions().getObsToCreate();*/

                for (Obs o : obs) {
                    if (o.getConcept().getName().getName().equals("Disease Type")) {
                        diseaseType = o.getValueCoded().getName().getName();
                    }

                }
                admission.setDiseaseType(diseaseType);
                Admission savedAdmission = AdmissionUtil.savePatientAdmission(admission);

                //create new Global bill
                gb = new GlobalBill();
                gb.setAdmission(savedAdmission);
                gb.setBillIdentifier(ip.getInsuranceCardNo() + savedAdmission.getAdmissionId());
                gb.setCreatedDate(new Date());
                gb.setCreator(Context.getAuthenticatedUser());
                gb.setInsurance(ip.getInsurance());
                gb = GlobalBillUtil.saveGlobalBill(gb);
            }
        if (gb != null && gb.getInsurance().getCategory() != "NONE") {
            //if (gb != null && gb.getInsurance().getCategory() != "NONE") {
            List<InsurancePolicy> insurancePolicies = Context.getService(BillingService.class).getAllInsurancePoliciesByPatient(session.getPatient());
            for (InsurancePolicy insp : insurancePolicies) {
                if (insp.getInsurance().getCategory().equals("NONE") && (insp.getExpirationDate().after(new Date())) && Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(insp.getInsuranceCardNo()) == null) {
                    Admission admissionNone = new Admission();
                    admissionNone.setAdmissionDate(new Date());
                    admissionNone.setInsurancePolicy(insp);
                    admissionNone.setIsAdmitted(false);
                    admissionNone.setCreator(Context.getAuthenticatedUser());
                    admissionNone.setCreatedDate(new Date());
                    for (Obs o : obs) {
                        if (o.getConcept().getName().getName().equals("Disease Type")) {
                            diseaseType = o.getValueCoded().getName().getName();
                        }

                    }
                    admissionNone.setDiseaseType(diseaseType);
                    Admission savedAdmissionNone = AdmissionUtil.savePatientAdmission(admissionNone);

                    //create new Global bill
                    GlobalBill gbNone = null;
                    gbNone = new GlobalBill();
                    gbNone.setAdmission(savedAdmissionNone);
                    gbNone.setBillIdentifier(insp.getInsuranceCardNo() + savedAdmissionNone.getAdmissionId());
                    gbNone.setCreatedDate(new Date());
                    gbNone.setCreator(Context.getAuthenticatedUser());
                    gbNone.setInsurance(insp.getInsurance());
                    gbNone = GlobalBillUtil.saveGlobalBill(gbNone);

                    //Create Private admission consommation
                    BigDecimal totalMaximaToPayNone = new BigDecimal(0);
                    PatientServiceBill psbNone = new PatientServiceBill();
                    FacilityServicePrice fsp = Context.getService(BillingService.class).getFacilityServiceByName("Private Admission");
                    if (fsp != null) {
                        BillableService bs = Context.getService(BillingService.class).getBillableServiceByConcept(fsp, insp.getInsurance());
                        if (bs != null) {
                            totalMaximaToPayNone = totalMaximaToPayNone.add(bs.getMaximaToPay().multiply(new BigDecimal(1)));
                            psbNone.setService(bs);
                            psbNone.setServiceDate(new Date());
                            psbNone.setUnitPrice(bs.getMaximaToPay());
                            psbNone.setQuantity(new BigDecimal(1));
                            psbNone.setHopService(Context.getService(BillingService.class).getHopService(fsp.getCategory()));
                            psbNone.setCreator(Context.getAuthenticatedUser());
                            psbNone.setCreatedDate(new Date());
                        }
                    }

                    //patient bill none
                    PatientBill pbn = PatientBillUtil.createPatientBill(totalMaximaToPayNone, insp);
                    InsuranceBill ibn = InsuranceBillUtil.createInsuranceBill(insp.getInsurance(), totalMaximaToPayNone);

                    for (Obs o : obs) {

                        if (dp == null && o.getValueCoded() != null) {
                            for (Department dept : Context.getService(BillingService.class).getAllDepartements()) {
                                if (o.getValueCoded().getName().getName().toString().trim().equals(dept.getName().toString().trim())) {
                                    dp = dept;
                                    break;
                                }
                            }
                        }
                    }
                    Consommation cns = new Consommation();
                    cns.setBeneficiary(Context.getService(BillingService.class).getBeneficiaryByPolicyNumber(insp.getInsuranceCardNo()));
                    cns.setPatientBill(pbn);
                    cns.setInsuranceBill(ibn);
                    cns.setGlobalBill(gbNone);
                    cns.setCreatedDate(new Date());
                    cns.setCreator(Context.getAuthenticatedUser());
                    cns.setDepartment(dp);
                    ConsommationUtil.saveConsommation(cns);
                    psbNone.setConsommation(cns);
                    ConsommationUtil.createPatientServiceBill(psbNone);
                    //cons.addBillItem(psb);
                    break;

                }


            }
            //}
        }

        }
    }

}