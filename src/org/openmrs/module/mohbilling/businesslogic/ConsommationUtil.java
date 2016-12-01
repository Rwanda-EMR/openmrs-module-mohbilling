/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
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

		Consommation consommation = new  Consommation();

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
	
	public static Consommation handleSavePatientConsommation(
			HttpServletRequest request, ModelAndView mav) {

		Consommation saveConsommation = null;
		Consommation existingConsom = null;

		Integer globalBillId =Integer.valueOf(request.getParameter("globalBillId"));
		Integer departmentId =Integer.valueOf(request.getParameter("departmentId"));
		
		GlobalBill globalBill = GlobalBillUtil.getGlobalBill(globalBillId);
		Department department = DepartementUtil.getDepartement(departmentId);
		
		BigDecimal globalAmount = globalBill.getGlobalAmount();
		BigDecimal totalAmount = new BigDecimal(0);
		Beneficiary beneficiary = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(request
				.getParameter("ipCardNumber"));
		Insurance insurance = beneficiary.getInsurancePolicy().getInsurance();
		//check whether the insurance does have a third party;
		ThirdParty thirdParty = beneficiary.getInsurancePolicy().getThirdParty();
		
		User creator = Context.getAuthenticatedUser();
		
		int numberOfServicesClicked=0;
		String[] billItems = request.getParameterValues("billItem");
		 BigDecimal insuranceRate = (new BigDecimal(beneficiary.getInsurancePolicy().getInsurance().getCurrentRate().getRate()));
		 BigDecimal patientRate = (new BigDecimal(100).subtract(insuranceRate)).divide(new BigDecimal(100));
		 
		//update quantity on the existing consommation/add new item on the existing consommation 
		if(request.getParameter("consommationId")!=null || request.getParameter("addNew")!=null){
			existingConsom = ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId")));
			numberOfServicesClicked = billItems.length;
			// Get 100% of the Ticket Moderateur
			totalAmount=existingConsom.getPatientBill().getAmount().divide(patientRate);
		}

		//add new consommation
		else if (request.getParameter("consommationId")==null){
			existingConsom = new Consommation(globalBill, beneficiary, new Date(), creator, false);
			numberOfServicesClicked = Integer.valueOf(request
					.getParameter("numberOfServicesClicked"));	
		}
		String message="";
		BigDecimal voidedItemTotalAmount = new BigDecimal(0);
		BigDecimal addedItemTotalAmount = new BigDecimal(0);
			for (int i = 0; i < numberOfServicesClicked; i++) {
				BigDecimal  quantity= null;
				BigDecimal unitPrice = null;
				BillableService bs = null;
				PatientServiceBill psb =null;
				
				if(billItems!=null){
					PatientServiceBill existingPsb = ConsommationUtil.getPatientServiceBill(Integer.valueOf(billItems[i]));
					quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("newQuantity_"  + billItems[i])));
					unitPrice = existingPsb.getUnitPrice();
					psb = PatientServiceBill.newInstance(existingPsb,quantity);
					existingPsb.setVoided(true);
					existingPsb.setVoidedBy(creator);
					existingPsb.setVoidReason("edit");
					existingPsb.setVoidedDate(new Date());
					BigDecimal voidedItemAmount = existingPsb.getQuantity().multiply(existingPsb.getUnitPrice());
					BigDecimal newItemAmount = quantity.multiply(unitPrice);
					voidedItemTotalAmount = voidedItemTotalAmount.add(voidedItemAmount);
					addedItemTotalAmount=addedItemTotalAmount.add(newItemAmount);
					message="Items' quantities have been changed succefully...";
				}
				else if(billItems==null){
					if(request.getParameter("billableServiceId_" + i)!=null&&request.getParameter("quantity_" + i)!=null&&request.getParameter("servicePrice_" + i)!=null){
					 bs = InsuranceUtil.getValidBillableService(Integer.valueOf(request.getParameter("billableServiceId_" + i)));
					 HopService hopService =HopServiceUtil.getServiceByName(bs.getServiceCategory().getName());
					 quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("quantity_" + i)));
					 unitPrice = BigDecimal.valueOf(Double.valueOf(request.getParameter("servicePrice_" + i)));
					 psb = new PatientServiceBill(bs,hopService, new Date(), unitPrice, quantity, creator, new Date());
					// totalAmount = totalAmount.add(quantity.multiply(unitPrice));
					 addedItemTotalAmount=addedItemTotalAmount.add(quantity.multiply(unitPrice));
					 message="A new consommation has been added to the global bill...";
					}
				}
				
				if(request.getParameter("consomationToAddOn")!=null && !request.getParameter("consomationToAddOn").equals("")){
					existingConsom=ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consomationToAddOn")));
					numberOfServicesClicked = Integer.valueOf(request
							.getParameter("numberOfServicesClicked"));	
					bs = InsuranceUtil.getValidBillableService(Integer.valueOf(request.getParameter("billableServiceId_" + i)));
					 HopService hopService =HopServiceUtil.getServiceByName(bs.getServiceCategory().getName());
					 quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("quantity_" + i)));
					 unitPrice = BigDecimal.valueOf(Double.valueOf(request.getParameter("servicePrice_" + i)));
					 psb = new PatientServiceBill(bs,hopService, new Date(), unitPrice, quantity, creator, new Date());
					 message="New Items have been added to the existing consommation succefully..";
				}
				existingConsom.addBillItem(psb);
			}
			totalAmount = totalAmount.add(addedItemTotalAmount);
			totalAmount = totalAmount.subtract(voidedItemTotalAmount);
			
		PatientBill	 pb = PatientBillUtil.createPatientBill(totalAmount, beneficiary.getInsurancePolicy());					  
	    InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(insurance, totalAmount);				
		ThirdPartyBill	thirdPartyBill = ThirdPartyBillUtil.createThirdPartyBill(beneficiary.getInsurancePolicy(), totalAmount);
							
		existingConsom.setDepartment(department);					
		existingConsom.setPatientBill(pb);
		existingConsom.setDepartment(department);
		existingConsom.setInsuranceBill(ib);
		existingConsom.setThirdPartyBill(thirdPartyBill);
		
		saveConsommation = ConsommationUtil.saveConsommation(existingConsom);	
		globalAmount = globalAmount.subtract(voidedItemTotalAmount).add(addedItemTotalAmount);
		//globalAmount =globalAmount.add(totalAmount);
		globalBill.setGlobalAmount(globalAmount);
		GlobalBillUtil.saveGlobalBill(globalBill);	

		request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);

		return saveConsommation;
		

	}

	/**
	 * Gets Consommation by a given patientBill
	 * @param patientBill
	 * @return Consommation
	 */
	public static Consommation getConsommationByPatientBill(
			PatientBill patientBill) {
		// TODO Auto-generated method stub
		return getService().getConsommationByPatientBill(patientBill);
	}
	
	public static String getConsommationStatus(Integer id){
		Consommation c = ConsommationUtil.getConsommation(id);
		String status="";
		if(c.getPatientBill().getAmountPaid().compareTo(c.getPatientBill().getAmount())==1)
			status="Fully Paid";
		if(c.getPatientBill().getAmountPaid().compareTo(c.getPatientBill().getAmount())==-1)
			status="Partially Paid";
		if(c.getPatientBill().getAmountPaid().compareTo(BigDecimal.ZERO)==0)
			status="Unpaid";
		return status;
		
	}

	// all items are paid
	public static Boolean areAllItemsPaid(Consommation c){
		Boolean isTrue = false;
		for (PatientServiceBill psb : c.getBillItems()) {
			if(psb.getPaidQuantity()!=null){
			if(psb.getPaidQuantity().compareTo(psb.getQuantity())==1)
				isTrue=true;
			else{
				isTrue=false;
			}
			}
			else if(c.getPatientBill().getAmountPaid().compareTo(c.getPatientBill().getAmount())==1){
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
			User billCreator,Department department){
		 return getService().getConsommations(startDate, endDate, insurance, tp, billCreator, department);
	}

}
