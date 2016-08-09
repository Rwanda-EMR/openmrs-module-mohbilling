
/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.model.ThirdPartyBill;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

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

		if (request.getParameter("save") != null) {
			Consommation consommation = handleSavePatientConsommation(request, mav);
			if (null == consommation)
				 new ModelAndView(new RedirectView(
						"billing.form?insurancePolicyId="
								+ request.getParameter("insurancePolicyId")
								+ "&ipCardNumber="+request.getParameter("ipCardNumber")
								+ "&globalBillId="+request.getParameter("globalBillId")				
						)
				);
			else
				return new ModelAndView(new RedirectView(
						"patientBillPayment.form?consommationId="
								+ consommation.getConsommationId() + "&ipCardNumber="
								+ consommation.getBeneficiary().getPolicyIdNumber()));
		}
		if (request.getParameter("searchDpt") != null) {
		  Department department = DepartementUtil.getDepartement(Integer.valueOf(request.getParameter("departmentId")));
	
		 
			if (department !=null)
			
				return new ModelAndView(new RedirectView(
						"billing.form?insurancePolicyId="
								+ request.getParameter("insurancePolicyId")
								+ "&ipCardNumber="+request.getParameter("ipCardNumber")	
								+ "&globalBillId="+request.getParameter("globalBillId")	
								+ "&departmentId="+department.getDepartmentId()	));
		
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
				 
				 log.info("categories zize>>>>>>>>>>>>>>>>>>>"+categories.size());
					
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

		} catch (Exception e) {
		//	log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			return new ModelAndView(new RedirectView("patientSearchBill.form"));
		}

		return mav;

	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private Consommation handleSavePatientConsommation(HttpServletRequest request,
			ModelAndView mav) {

		Consommation saveConsommation = null;
		Integer globalBillId =Integer.valueOf(request.getParameter("globalBillId"));
		
		GlobalBill globalBill = GlobalBillUtil.getGlobalBill(globalBillId);
		BigDecimal globalAmount = globalBill.getGlobalAmount();
		Consommation consom = null;

		try {
			int numberOfServicesClicked = Integer.valueOf(request
					.getParameter("numberOfServicesClicked"));
			
			consom = new Consommation();		
			
			BigDecimal totalAmount = new BigDecimal(0);

			Beneficiary beneficiary = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(request
							.getParameter("ipCardNumber"));
			Insurance insurance = beneficiary.getInsurancePolicy().getInsurance();
			//check whether the insurance does have a third party;
		    ThirdParty thirdParty = beneficiary.getInsurancePolicy().getThirdParty();
		   

			consom.setBeneficiary(beneficiary);
			consom.setCreatedDate(new Date());
			consom.setCreator(Context.getAuthenticatedUser());
			consom.setVoided(false);

			for (int i = 0; i < numberOfServicesClicked; i++) {
				BigDecimal  quantity= null;
				BigDecimal unitPrice = null;
				if (request.getParameter("billableServiceId_" + i) != null) {

					PatientServiceBill psb = new PatientServiceBill();

					BillableService bs = InsuranceUtil
							.getValidBillableService(Integer.valueOf(request
									.getParameter("billableServiceId_" + i)));
					psb.setService(bs);
				
					if(request.getParameter("quantity_" + i)!=null&&!request.getParameter("quantity_" + i).equals(""))
						 quantity = BigDecimal.valueOf(Double.valueOf(request.getParameter("quantity_" + i)));
					psb.setQuantity(BigDecimal.valueOf(Double.valueOf(request.getParameter("quantity_" + i))));
					

					psb.setServiceDate(new Date());
					unitPrice = BigDecimal.valueOf(Double.valueOf(request
							.getParameter("servicePrice_" + i)));
					psb.setUnitPrice(BigDecimal.valueOf(Double.valueOf(request
							.getParameter("servicePrice_" + i))));
					psb.setPaid(false);
					psb.setCreatedDate(new Date());
					psb.setCreator(Context.getAuthenticatedUser());					
					psb.setVoided(false);
					//totalAmount.add(quantity.multiply(unitPrice)), mc), mc);
					totalAmount = totalAmount.add(quantity.multiply(unitPrice));
					consom.addBillItem(psb);
				}
			}					
			
		PatientBill	 pb =PatientBillUtil.createPatientBill(totalAmount, beneficiary.getInsurancePolicy());					  
	   InsuranceBill ib =InsuranceBillUtil.createInsuranceBill(insurance, totalAmount);			
				
		ThirdPartyBill	thirdPartyBill =	ThirdPartyBillUtil.createThirdPartyBill(beneficiary.getInsurancePolicy(), totalAmount);
							
			consom.setGlobalBill(globalBill);
			consom.setPatientBill(pb);
			consom.setInsuranceBill(ib);
			consom.setThirdPartyBill(thirdPartyBill);
			
			//ConsommationUtil.createConsommation(consom);
			
			saveConsommation = ConsommationUtil.saveConsommation(consom);
			globalAmount =globalAmount.add(pb.getAmount());
			globalBill.setGlobalAmount(globalAmount);
			GlobalBillUtil.saveGlobalBill(globalBill);			

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"Consommation has been saved successfully !");

			return saveConsommation;

		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The  consommation  has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());

			e.printStackTrace();

			return null;
		}

		// return true;
	}
	
}