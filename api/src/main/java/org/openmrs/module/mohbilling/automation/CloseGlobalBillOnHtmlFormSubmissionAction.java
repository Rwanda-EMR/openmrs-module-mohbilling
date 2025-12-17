package org.openmrs.module.mohbilling.automation;

import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;

import java.util.Date;
import java.util.List;

public class CloseGlobalBillOnHtmlFormSubmissionAction implements CustomFormSubmissionAction {


    @Override
    public void applyAction(FormEntrySession session) {
//Integer insuranceTypeconceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceTypeConcept"));
        Integer insuranceNumberConceptID=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept"));

        String insuranceCardNumber=null;
        Date closingDate=new Date();
//GlobalBill gb =null;



//List<Obs> currentInsurance=Context.getObsService().getLastNObservations(1,session.getPatient(),Context.getConceptService().getConcept(insuranceTypeconceptID),false);
        List<Obs> currentInsuranceId=Utils.getLastNObservations(1,session.getPatient(),Context.getConceptService().getConcept(insuranceNumberConceptID),false);

/*
if(currentInsurance.size()>=1)
    System.out.println("Insuranceeeeeeeeee Company : "+  currentInsurance.get(0).getValueCoded().getName().getName());
*/

        if(currentInsuranceId.size()>=1)
            insuranceCardNumber=currentInsuranceId.get(0).getValueText();


        InsurancePolicy ip =Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNumber);
        GlobalBill openGb=Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());

        List<Obs> obs = session.getSubmissionActions().getObsToCreate();

        for (Obs o : obs) {
            if (o.getConcept().getUuid().equals("61f48c84-714d-42b3-805c-15645370deb8")) {
                closingDate=o.getValueDate();
            }

        }
        openGb.setClosingDate(closingDate);
        openGb.setClosed(true);
        openGb.setClosedBy(Context.getAuthenticatedUser());
        openGb = GlobalBillUtil.saveGlobalBill(openGb);

        List<InsurancePolicy> insurancePolicies = Context.getService(BillingService.class).getAllInsurancePoliciesByPatient(session.getPatient());
        for (InsurancePolicy insp : insurancePolicies) {
            if (insp.getInsurance().getCategory().equals("NONE")) {
                GlobalBill noneOpenedGB= Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(insp.getInsuranceCardNo());
                if (noneOpenedGB!=null){
                    noneOpenedGB.setClosingDate(closingDate);
                    noneOpenedGB.setClosed(true);
                    noneOpenedGB.setClosedBy(Context.getAuthenticatedUser());
                    noneOpenedGB = GlobalBillUtil.saveGlobalBill(noneOpenedGB);
                    break;
                }
            }
        }
        List<Visit> activeVisits= Context.getVisitService().getActiveVisitsByPatient(session.getPatient());
        for (Visit v:activeVisits){
            Context.getVisitService().endVisit(v,new Date());
        }
    }



}