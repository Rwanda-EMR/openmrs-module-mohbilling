
/**
 *
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Set;

/**
 * @author EMR-RBC
 *
 */
public class MohBillingBillingFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		Consommation consommation = null;

		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>edit a consommation>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyy"+request.getParameter("consommationId"));
		try {
			if((ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId"))).getPatientBill().getPayments().size()<=0)
					|| (!ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId"))).getGlobalBill().isClosed())){


				if(request.getParameter("edit")!=null){
					consommation = ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId")));
					mav.addObject("consommation", consommation);

				}

			}
			else{
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,"Impossible to edit this 'FICHE DE CONSOMMATION'. It has a payment or the global bill has been closed..");
			}
		} catch (Exception e) {
			log.error("" + e.getMessage());
			e.printStackTrace();
		}

		Consommation addNew = null;
		if(request.getParameter("addNew")!=null && !request.getParameter("addNew").equals("")){
			addNew = ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId")));
			mav.addObject("addNew", addNew);
		}
		String editStr = "";
		if(request.getParameter("edit")!=null){
			editStr = request.getParameter("edit");
			mav.addObject("editStr", editStr);
		}
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>create new consommation>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		if (request.getParameter("save") != null) {
			consommation = handleSavePatientConsommation(request, mav);
			if (null == consommation)
				new ModelAndView(new RedirectView(
						"billing.form?insurancePolicyId="
								+ request.getParameter("insurancePolicyId")
								+ "&ipCardNumber="+request.getParameter("ipCardNumber")
								+ "&globalBillId="+request.getParameter("globalBillId")		));

			else
				return new ModelAndView(new RedirectView(
						"patientBillPayment.form?consommationId="
								+ consommation.getConsommationId() + "&ipCardNumber="
								+ consommation.getBeneficiary().getPolicyIdNumber()));
		}

		if (request.getParameter("searchDpt") != null) {
			Department depart = null;

			if(request.getParameter("departmentId").equals("")){
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,"Please select the Department/Service!");
				return new ModelAndView(new RedirectView(
						"billing.form?insurancePolicyId="
								+ request.getParameter("insurancePolicyId")
								+ "&ipCardNumber="+request.getParameter("ipCardNumber")
								+ "&globalBillId="+request.getParameter("globalBillId")));
			}
			else{
				depart = DepartementUtil.getDepartement(Integer.valueOf(request.getParameter("departmentId")));
				Set<String> servicesByDepartment =null;
				if(GlobalPropertyConfig.getListOfHopServicesByDepartment(depart)!=null){
					servicesByDepartment = GlobalPropertyConfig.getListOfHopServicesByDepartment(depart);
					if(servicesByDepartment.size()!=0)
						request.getSession().setAttribute(
								WebConstants.OPENMRS_MSG_ATTR,"Available service categories in "+depart.getName()+" department ");
					else
						request.getSession().setAttribute(
								WebConstants.OPENMRS_ERROR_ATTR,"No service categories in "+ depart.getName()+" department! Contact the System Admin...");


					return new ModelAndView(new RedirectView(
							"billing.form?insurancePolicyId="
									+ request.getParameter("insurancePolicyId")
									+ "&ipCardNumber="+request.getParameter("ipCardNumber")
									+ "&globalBillId="+request.getParameter("globalBillId")
									+ "&departmentId="+depart.getDepartmentId()	));
				}

			}
		}
		try {
			if (request.getParameter("ipCardNumber") == null)
				return new ModelAndView(new RedirectView(
						"patientSearchBill.form"));

			Beneficiary ben = InsurancePolicyUtil
					.getBeneficiaryByPolicyIdNo(request
							.getParameter("ipCardNumber"));
			Set<ServiceCategory> categories = null;
			if(request.getParameter("departmentId")!=null){
				Department department = DepartementUtil.getDepartement(Integer.valueOf(request.getParameter("departmentId")));

				ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(request.getParameter("ipCardNumber"));
				categories = HopServiceUtil.getServiceCategoryByInsurancePolicyDepartment(ben.getInsurancePolicy(), department);
				mav.addObject("categories",categories);


			}


			mav.addObject("beneficiary", ben);

			InsurancePolicy ip = InsurancePolicyUtil
					.getInsurancePolicyByBeneficiary(ben);
			mav.addObject("insurancePolicy", ip);
			mav.addObject("globalBillId",request.getParameter("globalBillId"));

			// check the validity of the insurancePolicy for today
			Date today = new Date();
			mav.addObject("valid",
					((ip.getCoverageStartDate().getTime() <= today
							.getTime()) && (today.getTime() <= ip
							.getExpirationDate().getTime())));

			mav.addObject("departments", DepartementUtil.getAllHospitalDepartements());
			mav.addObject("patientAccount", PatientAccountUtil.getPatientAccountByPatient(ben.getPatient()));
		} catch (Exception e) {
			//	log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			return new ModelAndView(new RedirectView("patientSearchBill.form"));
		}
		//mav.addObject("edit", request.getParameter("edit"));
		return mav;

	}

	public Consommation handleSavePatientConsommation(HttpServletRequest request, ModelAndView mav) {

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

		User creator = Context.getAuthenticatedUser();

		int numberOfServicesClicked=0;
		String[] billItems = request.getParameterValues("billItem");
		BigDecimal insuranceRate = (new BigDecimal(beneficiary.getInsurancePolicy().getInsurance().getCurrentRate().getRate()));
		BigDecimal patientRate = (new BigDecimal(100).subtract(insuranceRate)).divide(new BigDecimal(100));

		//update quantity on the existing consommation/add new item on the existing consommation
		if(request.getParameter("consommationId")!=null || request.getParameter("addNew")!=null) {
			existingConsom = ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId")));
			numberOfServicesClicked = billItems.length;
			// Get 100% of the Ticket Moderateur
			//totalAmount=existingConsom.getPatientBill().getAmount().divide(patientRate);
			if (patientRate.compareTo(BigDecimal.ZERO) > 0) {
				totalAmount = existingConsom.getPatientBill().getAmount().divide(patientRate, 2, RoundingMode.HALF_UP);
			} else {
				totalAmount = existingConsom.getPatientBill().getAmount();
			}
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
		BigDecimal removedItemTotalAmount = new BigDecimal(0);
		BigDecimal totalExistingConsomm= new BigDecimal(0);
		int existingItemsLoopControl=0;

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
					ConsommationUtil.retireItem(itemToRemove);

					BigDecimal removedItemAmount = itemToRemove .getQuantity().multiply(itemToRemove .getUnitPrice());
					removedItemTotalAmount = removedItemTotalAmount.add(removedItemAmount);

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
					BigDecimal voidedItemAmount = existingPsb.getQuantity().multiply(existingPsb.getUnitPrice());
					BigDecimal newItemAmount = quantity.multiply(unitPrice);
					voidedItemTotalAmount = voidedItemTotalAmount.add(voidedItemAmount);
					addedItemTotalAmount=addedItemTotalAmount.add(newItemAmount);


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
						psb = new PatientServiceBill(bs, hopService, new Date(), unitPrice, quantity, creator, new Date(),drugf);
						addedItemTotalAmount=addedItemTotalAmount.add(quantity.multiply(unitPrice));
						if(existingItemsLoopControl==0) {
							for (PatientServiceBill pp : existingConsom.getBillItems()) {
								totalExistingConsomm = totalExistingConsomm.add(pp.getQuantity().multiply(pp.getUnitPrice()));
							}
							existingItemsLoopControl++;
						}

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
						// totalAmount = totalAmount.add(quantity.multiply(unitPrice));
						addedItemTotalAmount = addedItemTotalAmount.add(quantity.multiply(unitPrice));
						existingConsom.addBillItem(psb);
						message = "A new consommation has been added to the global bill...";
					}
				}
			}
		}
		BigDecimal addedItemTotalAmountt=addedItemTotalAmount.add(totalExistingConsomm);
		totalAmount = totalAmount.add(addedItemTotalAmountt);
		totalAmount = totalAmount.subtract(voidedItemTotalAmount);
		totalAmount = totalAmount.subtract(removedItemTotalAmount);

		PatientBill pb = PatientBillUtil.createPatientBill(totalAmount, beneficiary.getInsurancePolicy());
		InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(insurance, totalAmount);
		ThirdPartyBill	thirdPartyBill = ThirdPartyBillUtil.createThirdPartyBill(beneficiary.getInsurancePolicy(), totalAmount);

		existingConsom.setDepartment(department);
		existingConsom.setPatientBill(pb);
		existingConsom.setDepartment(department);
		existingConsom.setInsuranceBill(ib);
		existingConsom.setThirdPartyBill(thirdPartyBill);

		saveConsommation = ConsommationUtil.saveConsommation(existingConsom);
		globalAmount = globalAmount.subtract(voidedItemTotalAmount).subtract(removedItemTotalAmount).add(addedItemTotalAmount);
		//globalAmount =globalAmount.add(totalAmount);
		globalBill.setGlobalAmount(globalAmount);
		GlobalBillUtil.saveGlobalBill(globalBill);

		request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);

		return saveConsommation;


	}
}