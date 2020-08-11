package org.openmrs.module.mohbilling.automation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

public class CreateBillOnSaveLabAndPharmacyOrders{

public static void createBillOnSaveLabOrders(List<Concept> labOrdersConceptsList, Patient patient){
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
            if ("LABORATOIRE".equals(dept.getName().trim())) {
                department = dept;
                break;
            }
        }
    }
    //List<Obs> obs=session.getSubmissionActions().getObsToCreate();
    BigDecimal totalMaximaTopay=new BigDecimal(0);
    for (Concept concept:labOrdersConceptsList) {
        System.out.println("Billing: Concept Lab Test: " + concept);

        PatientServiceBill psb=new PatientServiceBill();
            FacilityServicePrice fsp = Context.getService(BillingService.class).getFacilityServiceByConcept(concept);
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
                psbList.add(psb);
            }
    }


if(psbList.size()>0) {
    GlobalBill gb = Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(ip.getInsuranceCardNo());
    BigDecimal globalAmount = gb.getGlobalAmount().add(totalMaximaTopay);
    gb.setGlobalAmount(globalAmount);
    gb = GlobalBillUtil.saveGlobalBill(gb);

    PatientBill pb = PatientBillUtil.createPatientBill(totalMaximaTopay, ip);
    InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(ip.getInsurance(), totalMaximaTopay);

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
}