/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author rbcemr
 *
 */
public class MohBillingConsommationFormController extends
		ParameterizableViewController {	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		Consommation existingConsom = Context.getService(BillingService.class).getConsommation(
				Integer.parseInt(request.getParameter("edit")));
		
		//Set<BillPayment> payments = existingConsom.getPatientBill().getPayments();

		if(request.getParameter("edit")!=null&&!request.getParameter("edit").equals("")){
		Consommation cpyConsom = new Consommation();
		cpyConsom.setBeneficiary(existingConsom.getBeneficiary());
		cpyConsom.setCreator(Context.getAuthenticatedUser());
		cpyConsom.setCreatedDate(new Date());
		cpyConsom.setVoided(false);
		
		BigDecimal totalAmount = new BigDecimal(0);
		Map<String, String[]> parameterMap = request.getParameterMap();	
		
		for (String  parameterName : parameterMap.keySet()) {
			
			if (!parameterName.startsWith("item-")) {
				continue;
			}
		
			String[] splittedParameterName = parameterName.split("-");
			String psbIdStr = splittedParameterName[2];			
			Integer  patientServiceBillId = Integer.parseInt(psbIdStr);				
			PatientServiceBill psb  = ConsommationUtil.getPatientServiceBill(patientServiceBillId);
			
			BigDecimal qty = new BigDecimal(0);
			PatientServiceBill cpyPsb = new PatientServiceBill();
			cpyPsb.setUnitPrice(psb.getUnitPrice());
			qty = psb.getQuantity();
			cpyPsb.setQuantity(qty);
			cpyPsb.setCreator(Context.getAuthenticatedUser());
			cpyPsb.setCreatedDate(new Date());
			cpyPsb.setConsommation(cpyConsom);
			cpyPsb.setServiceDate(psb.getServiceDate());
			
			totalAmount = totalAmount.add(qty.multiply(psb.getUnitPrice()));
	
			cpyConsom.addBillItem(cpyPsb);
		}
		PatientBill pb = PatientBillUtil.createPatientBill(totalAmount, existingConsom.getBeneficiary().getInsurancePolicy());
		InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(existingConsom.getBeneficiary().getInsurancePolicy().getInsurance(), totalAmount);
					
		ThirdPartyBill thirdPartyBill =	ThirdPartyBillUtil.createThirdPartyBill(existingConsom.getBeneficiary().getInsurancePolicy(), totalAmount);
		cpyConsom.setGlobalBill(existingConsom.getGlobalBill());
		cpyConsom.setPatientBill(pb);
		cpyConsom.setInsuranceBill(ib);
		cpyConsom.setThirdPartyBill(thirdPartyBill);
		cpyConsom.setGlobalBill(existingConsom.getGlobalBill());
		
		Consommation saveConsommation = ConsommationUtil.saveConsommation(cpyConsom);
		
		return new ModelAndView(new RedirectView(
				"patientBillPayment.form?consommationId="
						+ saveConsommation.getConsommationId() ));
		}
		

		mav.addObject("consommation", existingConsom);
		mav.addObject("ipCardNumber", existingConsom.getBeneficiary().getPolicyIdNumber());
		mav.addObject("globalBill", existingConsom.getGlobalBill());
		return mav;
	}

	

}
