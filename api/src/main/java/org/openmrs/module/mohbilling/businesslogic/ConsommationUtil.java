/**
 *
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.automation.MTNMomoApiIntegrationRequestToPay;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.service.BillingService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author emr
 *
 */
public class ConsommationUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 *
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}


	public static Consommation saveConsommation(Consommation consom) {
		getService().saveConsommation(consom);
		return consom;
	}

	/**
	 * Creates a PatientServiceBill object and saves it in the DB through
	 * Consommation which is its parent
	 *
	 * @param psb
	 *            the PatientServiceBill to be saved
	 * @return psb the PatientServiceBill that has been saved
	 */
	public static PatientServiceBill createPatientServiceBill(
			PatientServiceBill psb) {

		Consommation consommation = new Consommation();

		if (psb != null) {
			consommation = psb.getConsommation();
			consommation.addBillItem(psb);
			getService().saveConsommation(consommation);
			return psb;
		}

		return null;
	}
	public static Consommation createConsommation(Consommation consommation){

		GlobalBill globalBill = new GlobalBill();

		if (consommation != null) {
			consommation.setVoided(false);
			consommation.setCreatedDate(new Date());
			consommation.setCreator(Context.getAuthenticatedUser());
			globalBill = consommation.getGlobalBill();
			globalBill.addConsommation(consommation);
			getService().saveGlobalBill(globalBill);
			return consommation;
		}

		return null;


	}

	public static Consommation getConsommation(Integer consommationId){
		return getService().getConsommation(consommationId);

	}
	public static PatientServiceBill saveBilledItem(PatientServiceBill psb){


		return  getService().saveBilledItem(psb);

	}


	public static PatientServiceBill getPatientServiceBill(	Integer patientServiceBillId) {
		return  getService().getPatientServiceBill(patientServiceBillId);

	}

	/**
	 * Gets list of Conosmmation by global bill
	 * @param globalBill
	 * @return List<Consommation>
	 */
	public static List<Consommation> getConsommationsByGlobalBill(
			GlobalBill globalBill) {

		return getService().getAllConsommationByGlobalBill(globalBill);
	}
	/**
	 * Gets List of consommations  matching with a given  beneficiary
	 * @param beneficiary
	 * @return List<Consommation>
	 */
	public static List<Consommation> getConsommationsByBeneficiary(
			Beneficiary beneficiary) {

		return getService().getConsommationsByBeneficiary(beneficiary);
	}

	/**
	 * Gets Consommation by a given patientBill
	 * @param patientBill
	 * @return Consommation
	 */
	public static Consommation getConsommationByPatientBill(
			PatientBill patientBill) {
		return getService().getConsommationByPatientBill(patientBill);
	}

	public static String getConsommationStatus(Integer id){
		Consommation c = ConsommationUtil.getConsommation(id);
		String status="";
		int res = c.getPatientBill().getAmount().compareTo(c.getPatientBill().getAmountPaid());
		BigDecimal diff=c.getPatientBill().getAmount().subtract(c.getPatientBill().getAmountPaid());

		//if due and paid are equal and diff is less than 1
		//if (diff.compareTo(BigDecimal.ZERO) >= 0 && diff.floatValue()<1)
		if (diff.floatValue() <= 0 || diff==c.getPatientBill().getAmount())
			status="FULLY PAID";
		//if due is greater than paid and paid greater than 1
		//if (res==1 && diff.compareTo(BigDecimal.ONE)==1)
		if (diff.floatValue() > 0 && diff!=c.getPatientBill().getAmount())
			status="PARTIALLY PAID";
		//if(c.getPatientBill().getAmountPaid().compareTo(BigDecimal.ZERO)==0)
		if(c.getPatientBill().getPayments().size()==0)
			status="UNPAID";
		return status;

	}

	// all items are paid
	public static Boolean areAllItemsPaid(Consommation c){
		Boolean isTrue = false;
		for (PatientServiceBill psb : c.getBillItems()) {
			if(psb.getPaidQuantity()!=null){
				if(psb.getPaidQuantity().compareTo(psb.getQuantity())==0)
					isTrue=true;
				else{
					isTrue=false;
				}
			}
			else if(c.getPatientBill().getAmountPaid().compareTo(c.getPatientBill().getAmount())==0){
				isTrue=true;
			}
		}
		return isTrue;
	}

	//none of items is paid
	public static Boolean isConsommationUnpaid(Consommation c){
		Boolean found = false;
		for (PatientServiceBill psb : c.getBillItems()) {
			if(psb.getPaidQuantity()!=null){
				if(psb.getPaidQuantity().compareTo(BigDecimal.ZERO)==0)
					found=true;
			}
			else if(c.getPatientBill().getAmountPaid().compareTo(BigDecimal.ZERO)==0){
				found=true;
			}
		}
		return found;
	}
	//consommation is partially paid
	public static Boolean isConsommationPartiallyPaid(Consommation c){
		Boolean found = false;
		for (PatientServiceBill psb : c.getBillItems()) {
			if(psb.getPaidQuantity()!=null){
				if(psb.getPaidQuantity().compareTo(psb.getQuantity())<0)
					found=true;
				else{
					found=false;
				}
			}
			else if(c.getPatientBill().getAmountPaid().compareTo(c.getPatientBill().getAmount())==-1){
				found=true;
			}
		}
		return found;
	}
	public static List<Consommation> getConsommations(Date startDate,
													  Date endDate, Insurance insurance, ThirdParty tp,
													  User billCreator,Department department, int recordsPerPage, int page){
		return getService().getConsommations(startDate, endDate, insurance, tp, billCreator, department, recordsPerPage, page);
	}
	public static List<Consommation> getConsommationsWithPatientNotConfirmed(Date startDate,
													  Date endDate) throws IOException {
		List<Consommation> cons=getService().getConsommationsWithPatientNotConfirmed(startDate, endDate);
		MTNMomoApiIntegrationRequestToPay momo=new MTNMomoApiIntegrationRequestToPay();
		for (Consommation c:cons) {
			PatientBill pb=c.getPatientBill();
			if (pb.getTransactionStatus()!=null && pb.getTransactionStatus().equals("PENDING")){
				pb.setTransactionStatus(momo.getransactionStatus(pb.getReferenceId()));
				pb=PatientBillUtil.savePatientBill(pb);
			}
		}

		return cons;
	}
	public static List<Consommation> getDCPConsommations(Date startDate, Date endDate,User billCreator){
		return getService().getDCPConsommations(startDate, endDate,billCreator);
	}

	public static void retireItem(PatientServiceBill psb){
		psb.setVoided(true);
		psb.setVoidedBy(Context.getAuthenticatedUser());
		psb.setVoidReason("removed");
		psb.setVoidedDate(new Date());
		ConsommationUtil.saveConsommation(psb.getConsommation());
	}

	public static int getTotalConsommations(Date startDate,
											Date endDate, Insurance insurance, ThirdParty tp,
											User billCreator, Department department) {
		return getService().getTotalConsommations(startDate,
				endDate, insurance, tp,
				billCreator, department);
	}
}