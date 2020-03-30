package org.openmrs.module.mohbilling.automation;

import org.openmrs.*;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.mohbilling.businesslogic.AdmissionUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
// Below tag will be pasted in any OPD admission html form
// <postSubmissionAction class="org.openmrs.module.mohbilling.automation.CreateOPDAdmissionOnHtmlFormSubmissionAction"/>

public class CreateOPDAdmissionOnHtmlFormSubmissionAction implements CustomFormSubmissionAction {


    @Override
    public void applyAction(FormEntrySession session) {
//Integer insuranceTypeconceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceTypeConcept"));
Integer insuranceNumberConceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept"));

String insuranceCardNumber=null;
String diseaseType=null;
GlobalBill gb =null;



//List<Obs> currentInsurance=Context.getObsService().getLastNObservations(1,session.getPatient(),Context.getConceptService().getConcept(insuranceTypeconceptID),false);
List<Obs> currentInsuranceId=Context.getObsService().getLastNObservations(1,session.getPatient(),Context.getConceptService().getConcept(insuranceNumberConceptID),false);

/*
if(currentInsurance.size()>=1)
    System.out.println("Insuranceeeeeeeeee Company : "+  currentInsurance.get(0).getValueCoded().getName().getName());
*/

if(currentInsuranceId.size()>=1)
    insuranceCardNumber=currentInsuranceId.get(0).getValueText();


        InsurancePolicy ip =Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);
        GlobalBill openGb=Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());

        if(openGb==null) {
            Admission admission = new Admission();
            admission.setAdmissionDate(new Date());
            admission.setInsurancePolicy(ip);
            admission.setIsAdmitted(false);
            admission.setCreator(Context.getAuthenticatedUser());
            admission.setCreatedDate(new Date());


            List<Obs> obs = session.getSubmissionActions().getObsToCreate();

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

    }


}