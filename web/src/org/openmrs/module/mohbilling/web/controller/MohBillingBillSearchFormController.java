/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;
/**
 * @author emr
 *
 */
public class MohBillingBillSearchFormController extends ParameterizableViewController {

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView  mav = new ModelAndView();
	
		String billIdentifier = request.getParameter("billIdentifier");
		Beneficiary ben = null;
		
		if(billIdentifier != null){
			GlobalBill globalBill = GlobalBillUtil.getGlobalBillByBillIdentifier(billIdentifier);		
			String insuranceCardNo  = globalBill.getAdmission().getInsurancePolicy().getInsuranceCardNo();
			 ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(insuranceCardNo);			
			
			return new ModelAndView(new RedirectView(
					"consommation.list?globalBillId="
							+ globalBill.getGlobalBillId()
							+ "&ipCardNumber="+insuranceCardNo
												)
			);
		}
			
		mav.setViewName(getViewName());
		return mav;
	}
	

}
