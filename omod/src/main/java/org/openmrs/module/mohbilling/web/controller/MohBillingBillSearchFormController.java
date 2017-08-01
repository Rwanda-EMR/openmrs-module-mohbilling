/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author emr
 *
 */
public class MohBillingBillSearchFormController extends ParameterizableViewController {

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView mav = new ModelAndView();
	
		String billIdentifier = request.getParameter("billIdentifier");
		Beneficiary ben = null;
		
		try {
			if(billIdentifier != null){
				GlobalBill globalBill = GlobalBillUtil.getGlobalBillByBillIdentifier(billIdentifier);
				String insuranceCardNo  =null;
				 insuranceCardNo  = globalBill.getAdmission().getInsurancePolicy().getInsuranceCardNo();
				 ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(insuranceCardNo);
				
				return new ModelAndView(new RedirectView(
						"consommation.list?globalBillId="
								+ globalBill.getGlobalBillId()
								+ "&ipCardNumber="+insuranceCardNo
													)
				);
			}
		} catch (Exception e) {
			request.getSession().setAttribute(
					WebConstants.OPENMRS_ERROR_ATTR,"No Global Bill found..");
		}
		
		
		//search consommation
		 Consommation consommation = null;
		 String consommationIdStr = request.getParameter("consommationId");
		 try {
		 if(consommationIdStr!=null){
			  consommation = ConsommationUtil.getConsommation(Integer.valueOf(consommationIdStr));
			 	if(consommation!=null){
				mav.addObject("consommation", consommation);
			 	request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
							"Fiche de Consommation No: "+consommation.getConsommationId());
			 	return new ModelAndView(new RedirectView(
						"patientBillPayment.form?consommationId="
								+ consommation.getConsommationId())
				);
			 	}
		 }
		} catch (Exception e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"Searching Fiche de Consommation does not exist..!");
	    }
		mav.addObject("consommation", consommation);
		mav.setViewName(getViewName());
		return mav;
	}
	

}
