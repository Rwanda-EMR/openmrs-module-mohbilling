/**
 *
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.automation.MTNMomoApiIntegrationRequestToPay;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

	public static Consommation handleSavePatientConsommation(
			HttpServletRequest request, ModelAndView mav) {

		Consommation saveConsommation = null;
		Consommation existingConsom = null;

		Integer globalBillId =Integer.valueOf(request.getParameter("globalBillId"));
		Integer departmentId =Integer.valueOf(request.getParameter("departmentId"));

		GlobalBill globalBill = GlobalBillUtil.getGlobalBill(globalBillId);
		Department department = DepartementUtil.getDepartement(departmentId);

		Beneficiary beneficiary = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(request
				.getParameter("ipCardNumber"));
		Insurance insurance = beneficiary.getInsurancePolicy().getInsurance();
		//check whether the insurance does have a third party;

		User creator = Context.getAuthenticatedUser();

		int numberOfServicesClicked=0;
		String[] billItems = request.getParameterValues("billItem");
		//update quantity on the existing consommation/add new item on the existing consommation
		if(request.getParameter("consommationId")!=null || request.getParameter("addNew")!=null) {
			existingConsom = ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId")));
			numberOfServicesClicked = billItems.length;
		}
		//add new consommation
		else if (request.getParameter("consommationId")==null){
			existingConsom = new Consommation(globalBill, beneficiary, new Date(), creator, false);
			numberOfServicesClicked = Integer.valueOf(request
					.getParameter("numberOfServicesClicked"));
		}
		String message="";
		for (int i = 0; i < numberOfServicesClicked; i++) {
			BigDecimal  quantity= null;
			BigDecimal unitPrice = null;
			BillableService bs = null;
			PatientServiceBill psb =null;
			String drugf="";
			Integer item_type=null;
			if(billItems!=null){
				if(request.getParameter("removeItem_"  + billItems[i])!=null){
					PatientServiceBill itemToRemove = ConsommationUtil.getPatientServiceBill(Integer.valueOf(request.getParameter("removeItem_" + billItems[i])));
					retireItem(itemToRemove);
					syncVoidedItemInConsommation(existingConsom, itemToRemove);

					message="Item removed succefully...";
				}
				else{
					PatientServiceBill existingPsb = ConsommationUtil.getPatientServiceBill(Integer.valueOf(billItems[i]));
					quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("newQuantity_"  + billItems[i])));
					unitPrice = existingPsb.getUnitPrice();

					if(existingPsb!=null && request.getParameter("newDrugFrequency_"  + billItems[i])!=null && !(request.getParameter("newDrugFrequency_"  + billItems[i]).trim()).equals("")){
						existingPsb.setDrugFrequency(request.getParameter("newDrugFrequency_"  + billItems[i]));
					}
					psb = PatientServiceBill.newInstance(existingPsb, quantity);
					existingPsb.setVoided(true);
					existingPsb.setVoidedBy(creator);
					existingPsb.setVoidReason("edit");
					existingPsb.setVoidedDate(new Date());
					syncVoidedItemInConsommation(existingConsom, existingPsb);


					existingConsom.addBillItem(psb);
					message="Items' quantities/Drug frequency have been changed succefully...";
				}

			}
			else{
				if(request.getParameter("consomationToAddOn")!=null && !request.getParameter("consomationToAddOn").equals("")){
					existingConsom= ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consomationToAddOn")));
					numberOfServicesClicked = Integer.valueOf(request.getParameter("numberOfServicesClicked"));

					if(request.getParameter("billableServiceId_"+i)!=null && request.getParameter("quantity_"+i)!=null && request.getParameter("servicePrice_"+i)!=null) {
						bs = InsuranceUtil.getValidBillableService(Integer.valueOf(request.getParameter("billableServiceId_" + i)));
						HopService hopService = HopServiceUtil.getServiceByName(bs.getServiceCategory().getName());
						quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("quantity_" + i)));
						unitPrice = BigDecimal.valueOf(Double.valueOf(request.getParameter("servicePrice_" + i)));
						drugf = request.getParameter("frequency_"+i);
						item_type = bs.getFacilityServicePrice().getItemType().intValue();
						psb = new PatientServiceBill(bs, hopService, new Date(), unitPrice, quantity, creator, new Date(),drugf,item_type); // <<<<<<<<<<<<<<<<<<< Here Suspect Number One
						existingConsom.addBillItem(psb);


						message = "New Items have been added to the existing consommation succefully..";
					}
				}
				else {
					if(request.getParameter("billableServiceId_"+i)!=null && request.getParameter("quantity_"+i)!=null && request.getParameter("servicePrice_"+i)!=null) {
						bs = InsuranceUtil.getValidBillableService(Integer.valueOf(request.getParameter("billableServiceId_" + i)));
						HopService hopService = HopServiceUtil.getServiceByName(bs.getServiceCategory().getName());
						quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("quantity_" + i)));
						item_type = bs.getFacilityServicePrice().getItemType().intValue();
						unitPrice = BigDecimal.valueOf(Double.valueOf(request.getParameter("servicePrice_" + i)));
						drugf = request.getParameter("frequency_"+i);
						psb = new PatientServiceBill(bs, hopService, new Date(), unitPrice, quantity, creator, new Date(),drugf,item_type);
						existingConsom.addBillItem(psb);
						message = "A new consommation has been added to the global bill...";
					}
				}
			}
		}
		// Recompute amounts from current active (non-voided) items
		BigDecimal totalAmount = calculateNonVoidedItemsTotal(existingConsom);
		existingConsom.setDepartment(department);

		if (existingConsom.getPatientBill() != null
				&& existingConsom.getPatientBill().getPatientBillId() != null) {
			// Editing existing consommation: keep the same PatientBill row and refresh amounts
			saveConsommation = ConsommationUtil.saveConsommation(existingConsom);
			refreshBillAmountsFromNonVoidedItems(existingConsom, beneficiary.getInsurancePolicy());
		} else {
			// New consommation must have PatientBill before insert (patient_bill_id NOT NULL)
			PatientBill pb = PatientBillUtil.createPatientBill(totalAmount, beneficiary.getInsurancePolicy());
			InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(insurance, totalAmount);
			ThirdPartyBill thirdPartyBill = ThirdPartyBillUtil.createThirdPartyBill(beneficiary.getInsurancePolicy(),
					totalAmount);
			existingConsom.setPatientBill(pb);
			existingConsom.setInsuranceBill(ib);
			existingConsom.setThirdPartyBill(thirdPartyBill);
			saveConsommation = ConsommationUtil.saveConsommation(existingConsom);
			globalBill.setGlobalAmount(calculateGlobalBillNonVoidedTotal(globalBill));
			GlobalBillUtil.saveGlobalBill(globalBill);
		}

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
                                                      User billCreator,Department department){
        return getService().getConsommations(startDate, endDate, insurance, tp, billCreator, department, Integer.MAX_VALUE, 0);
	}

	public static List<Consommation> getConsommations(Date startDate,
													  Date endDate, Insurance insurance, ThirdParty tp,
													  User billCreator, Department department, int limit, int page) {
		int currentPage = Math.max(page, 1);
		int offSet = limit <= 0 ? 0 : (currentPage - 1) * limit;
		return getService().getConsommations(startDate, endDate, insurance, tp, billCreator, department, limit, offSet);
	}

	public static List<Consommation> getConsommationsOld(Date startDate,
													  Date endDate, Insurance insurance, ThirdParty tp,
													  User billCreator,Department department){
		return getService().getConsommationsOld(startDate, endDate, insurance, tp, billCreator, department);
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
		Consommation consommation = psb.getConsommation();
		syncVoidedItemInConsommation(consommation, psb);
		ConsommationUtil.saveConsommation(consommation);
		if (consommation != null && consommation.getBeneficiary() != null
				&& consommation.getBeneficiary().getInsurancePolicy() != null) {
			refreshBillAmountsFromNonVoidedItems(consommation,
					consommation.getBeneficiary().getInsurancePolicy());
		}
	}

	/**
	 * Recalculates and persists PatientBill / InsuranceBill / ThirdPartyBill amounts
	 * (and GlobalBill.globalAmount) from non-voided PatientServiceBill rows on the
	 * given consommation. Keeps the existing PatientBill row so Irembo / payment
	 * linkage is preserved.
	 */
	public static void refreshBillAmountsFromNonVoidedItems(Consommation consommation,
			InsurancePolicy insurancePolicy) {
		if (consommation == null || insurancePolicy == null) {
			return;
		}
		Insurance insurance = insurancePolicy.getInsurance();
		if (insurance == null) {
			return;
		}

		BigDecimal totalAmount = calculateNonVoidedItemsTotal(consommation);
		BigDecimal patientAmount = calculatePatientShare(totalAmount, insurancePolicy);
		BigDecimal insuranceAmount = calculateInsuranceShare(totalAmount, insurance);
		BigDecimal thirdPartyAmount = calculateThirdPartyShare(totalAmount, insurancePolicy);

		PatientBill patientBill = consommation.getPatientBill();
		if (patientBill != null) {
			patientBill.setAmount(patientAmount);
			PatientBillUtil.savePatientBill(patientBill);
		}

		InsuranceBill insuranceBill = consommation.getInsuranceBill();
		if (insuranceBill != null) {
			insuranceBill.setAmount(insuranceAmount);
			InsuranceBillUtil.saveInsuranceBill(insuranceBill);
		}

		ThirdPartyBill thirdPartyBill = consommation.getThirdPartyBill();
		if (thirdPartyBill != null) {
			thirdPartyBill.setAmount(thirdPartyAmount != null ? thirdPartyAmount : BigDecimal.ZERO);
			ThirdPartyBillUtil.saveThirdPartyBill(thirdPartyBill);
		} else if (thirdPartyAmount != null && thirdPartyAmount.compareTo(BigDecimal.ZERO) > 0) {
			ThirdPartyBill created = ThirdPartyBillUtil.createThirdPartyBill(insurancePolicy, totalAmount);
			consommation.setThirdPartyBill(created);
			saveConsommation(consommation);
		}

		GlobalBill globalBill = consommation.getGlobalBill();
		if (globalBill != null) {
			globalBill.setGlobalAmount(calculateGlobalBillNonVoidedTotal(globalBill));
			GlobalBillUtil.saveGlobalBill(globalBill);
		}
	}

	private static BigDecimal calculatePatientShare(BigDecimal totalAmount, InsurancePolicy insurancePolicy) {
		if (totalAmount == null) {
			return BigDecimal.ZERO;
		}
		InsuranceRate validRate = insurancePolicy.getInsurance().getRateOnDate(new Date());
		if (insurancePolicy.getInsurance().getCurrentRate() != null
				&& insurancePolicy.getInsurance().getCurrentRate().getFlatFee() != null
				&& insurancePolicy.getInsurance().getCurrentRate().getFlatFee().compareTo(BigDecimal.ZERO) > 0) {
			return BigDecimal.ZERO;
		}
		float rateToPay;
		ThirdParty thirdParty = insurancePolicy.getThirdParty();
		if (thirdParty == null) {
			rateToPay = 100 - validRate.getRate();
		} else {
			rateToPay = (100 - validRate.getRate()) - thirdParty.getRate();
		}
		return totalAmount.multiply(BigDecimal.valueOf(rateToPay / 100d));
	}

	private static BigDecimal calculateInsuranceShare(BigDecimal totalAmount, Insurance insurance) {
		if (totalAmount == null || insurance == null) {
			return BigDecimal.ZERO;
		}
		InsuranceRate validRate = insurance.getRateOnDate(new Date());
		if (validRate == null) {
			return BigDecimal.ZERO;
		}
		return totalAmount.multiply(BigDecimal.valueOf(validRate.getRate() / 100d));
	}

	private static BigDecimal calculateThirdPartyShare(BigDecimal totalAmount, InsurancePolicy insurancePolicy) {
		if (totalAmount == null || insurancePolicy == null || insurancePolicy.getThirdParty() == null) {
			return null;
		}
		return totalAmount.multiply(BigDecimal.valueOf(insurancePolicy.getThirdParty().getRate() / 100d));
	}

	private static BigDecimal calculateNonVoidedItemsTotal(Consommation consommation) {
		BigDecimal total = BigDecimal.ZERO;
		if (consommation == null || consommation.getBillItems() == null) {
			return total;
		}

		for (PatientServiceBill item : consommation.getBillItems()) {
			if (item == null || Boolean.TRUE.equals(item.getVoided())) {
				continue;
			}
			BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
			BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
			total = total.add(quantity.multiply(unitPrice));
		}
		return total;
	}

	private static BigDecimal calculateGlobalBillNonVoidedTotal(GlobalBill globalBill) {
		BigDecimal total = BigDecimal.ZERO;
		if (globalBill == null) {
			return total;
		}

		List<Consommation> consommations = getConsommationsByGlobalBill(globalBill);
		if (consommations == null) {
			return total;
		}

		for (Consommation consommation : consommations) {
			if (consommation == null || Boolean.TRUE.equals(consommation.getVoided())) {
				continue;
			}
			total = total.add(calculateNonVoidedItemsTotal(consommation));
		}
		return total;
	}

	private static void syncVoidedItemInConsommation(Consommation consommation, PatientServiceBill voidedItem) {
		if (consommation == null || voidedItem == null || consommation.getBillItems() == null) {
			return;
		}
		for (PatientServiceBill item : consommation.getBillItems()) {
			if (item == null || item.getPatientServiceBillId() == null || voidedItem.getPatientServiceBillId() == null) {
				continue;
			}
			if (item.getPatientServiceBillId().equals(voidedItem.getPatientServiceBillId())) {
				item.setVoided(true);
				item.setVoidedBy(voidedItem.getVoidedBy());
				item.setVoidedDate(voidedItem.getVoidedDate());
				item.setVoidReason(voidedItem.getVoidReason());
				return;
			}
		}
	}

	public static int getTotalConsommations(Date startDate,
											Date endDate, Insurance insurance, ThirdParty tp,
											User billCreator, Department department) {
		return getService().getTotalConsommations(startDate,
				endDate, insurance, tp,
				billCreator, department);
				
	}
}
