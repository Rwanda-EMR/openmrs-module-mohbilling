/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 *
 */
public class MohBillingBillingPatientSearchFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		
		/*String patientIdStr = request.getParameter("patientId");
		int  patientId = 0;
		if(patientIdStr != null){
		 patientId = Integer.parseInt(patientIdStr);
		}
		
		Patient owner = Context.getPatientService().getPatient(patientId);
				
		List<Beneficiary> bens = new ArrayList<Beneficiary>();
		
		bens =  InsurancePolicyUtil.getBeneficiaryByOwner(owner);
		if(bens.size() > 0 && bens.get(0).getInsurancePolicy().getInsuranceCardNo() != null){
		mav.addObject("insuranceCardNumber",bens.get(0).getInsurancePolicy().getInsuranceCardNo());
		}
		if (owner != null && bens.isEmpty()){
		mav.addObject("messageIfNoInsuranceCardNoFound","The patient you have selected does not have any Insurance card Number");
		}*/
		mav.setViewName(getViewName());
       
		return mav;

	}
}
