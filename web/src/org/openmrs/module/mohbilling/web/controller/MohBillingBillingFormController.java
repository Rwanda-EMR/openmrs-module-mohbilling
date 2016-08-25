
/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.ServiceCategory;
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
		Consommation consommation = null;
		
		if(request.getParameter("edit")!=null){
			consommation = ConsommationUtil.getConsommation(Integer.valueOf(request.getParameter("consommationId")));
			mav.addObject("consommation", consommation);
		}

		if (request.getParameter("save") != null) {
			consommation = ConsommationUtil.handleSavePatientConsommation(request, mav);
			
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
		
		return mav;

	}
}