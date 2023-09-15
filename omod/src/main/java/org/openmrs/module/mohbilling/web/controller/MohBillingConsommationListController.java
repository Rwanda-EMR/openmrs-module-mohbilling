/**
 *
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author rbcemr
 *
 */
public class MohBillingConsommationListController extends ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		String discharge = request.getParameter("discharge");
		Integer globalBillId = Integer.valueOf(request.getParameter("globalBillId"));
		String  insuranceCardNo =request.getParameter("ipCardNumber");
		InsurancePolicy ip=null;
		GlobalBill globalBill =null;
		List<Consommation> consommations =null;
		if(discharge==null) {
			if (insuranceCardNo != null) {
				ip = Context.getService(BillingService.class).getInsurancePolicyByCardNo(insuranceCardNo);
				mav.addObject("insurancePolicy", ip);
			}
			if (globalBillId != null) {
				globalBill = GlobalBillUtil.getGlobalBill(globalBillId);
				consommations = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
				mav.addObject("globalBill", globalBill);
				mav.addObject("consommations", consommations);
			}
			mav.addObject("patientAccount", PatientAccountUtil.getPatientAccountByPatient(ip.getOwner()));
		}
		if(request.getParameter("discharge")!=null && Context.isAuthenticated()){
			discharge = request.getParameter("discharge");
			globalBill = GlobalBillUtil.getGlobalBill(Integer.valueOf(request.getParameter("globalBillId")));
			if(request.getParameter("edit")!=null){
				if(globalBill.getAdmission().getInsurancePolicy().getInsurance()==null) {
					globalBill.setInsurance(globalBill.getAdmission().getInsurancePolicy().getInsurance());
				}
				globalBill.setAdmission(globalBill.getAdmission());
				globalBill.setBillIdentifier(globalBill.getBillIdentifier());
				globalBill.setGlobalAmount(globalBill.getGlobalAmount());
				globalBill.setCreator(globalBill.getCreator());
				globalBill.setCreatedDate(globalBill.getCreatedDate());
				globalBill.setClosingDate(new Date());
				globalBill.setClosedBy(Context.getAuthenticatedUser());
				globalBill.setClosed(true);
				globalBill.setClosingReason(request.getParameter("closeGBReason"));
				GlobalBillUtil.saveGlobalBill(globalBill);
				return new ModelAndView(new RedirectView("viewGlobalBill.form?globalBillId="+globalBillId));
			}
			mav.addObject("globalBill", globalBill);
		}
		mav.addObject("discharge",discharge);
		mav.setViewName(getViewName());
		return mav;
	}
}