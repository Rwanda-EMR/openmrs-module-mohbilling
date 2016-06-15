package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepositUtil;
import org.openmrs.module.mohbilling.model.Deposit;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingDepositFormController extends
		ParameterizableViewController {
	
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		 
		/** Logger for this class and subclasses */
	

			ModelAndView mav = new ModelAndView();
			mav.setViewName(getViewName());
			
			Deposit deposit = new Deposit();
			
			
			if (null != request.getParameter("insurancePolicyId")) {
				InsurancePolicy ip = Context.getService(
						BillingService.class).getInsurancePolicy(
						Integer.valueOf(request
								.getParameter("insurancePolicyId")));

				mav.addObject("insurancePolicy", ip);
				mav.addObject("beneficiaryId", ip.getOwner().getPatientId());
			}
			
			if(request.getParameter("save")!=null && request.getParameter("save").equals("true")){
				InsurancePolicy ip = Context.getService(
						BillingService.class).getInsurancePolicy(
						Integer.valueOf(request
								.getParameter("insurancePolicyId")));
				deposit.setPatient(ip.getOwner());
				
				String depositDateStr = request.getParameter("depositDate");
				Date depositDate = null;
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				depositDate = df.parse(depositDateStr.split("/")[2] + "-"
						+ depositDateStr.split("/")[1] + "-"
						+ depositDateStr.split("/")[0]);

				String depositReason = request.getParameter("depositReason"); 
				
				deposit.setAmount(BigDecimal.valueOf(Double.valueOf(request
								.getParameter("depositAmount"))));
				deposit.setCashier(Context.getUserService().getUser(Integer.valueOf(request.getParameter("depositCollector"))));
				deposit.setDepositDate(depositDate);
				deposit.setDepositReason(depositReason);
				deposit.setWithdrawals(null);
				deposit.setCreator(Context.getAuthenticatedUser());
				deposit.setCreatedDate(new Date());
				deposit.setVoided(false);
				
				DepositUtil.createDeposit(deposit);
				
			}
//			Patient patient = Context.getPatientService().getPatient(314);
//			mav.addObject("depositList", DepositUtil.getDepositList(patient));
			return mav;
	}

}
