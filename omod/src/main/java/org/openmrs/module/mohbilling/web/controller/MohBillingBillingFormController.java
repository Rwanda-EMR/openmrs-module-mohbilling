
/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
}