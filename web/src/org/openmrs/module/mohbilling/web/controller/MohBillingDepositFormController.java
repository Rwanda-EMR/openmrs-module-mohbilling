package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
				return null;
		 
		/** Logger for this class and subclasses */
	

/*			ModelAndView mav = new ModelAndView();
			mav.setViewName(getViewName());
			mav.addObject("authUser", Context.getAuthenticatedUser());
			
			Deposit deposit = new Deposit();
			Patient patient = null;
			if(request.getParameter("patientId")!=null){
				patient = Context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId")));
				mav.addObject("patient", patient);
				deposit.setPatient(patient);
			}

			if(request.getParameter("save")!=null && request.getParameter("save").equals("true")){				
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
				//return new ModelAndView(new RedirectView("deposit.form"));	
			}
			List<Deposit> deposits =DepositUtil.getDepositList(patient, null, null, null);
			
			BigDecimal totalDepositAmount = new BigDecimal(0);

			for (Deposit d : deposits) 
				totalDepositAmount = totalDepositAmount.add(d.getAmount());
			
			if (request.getParameter("depositId") != null) {
					mav.addObject("deposit", (DepositUtil.getDeposit(Integer.valueOf(request.getParameter("depositId")))));
			}	
			if (request.getParameter("retire") != null) {
				boolean retired = handleRetireDeposit(request, mav);
				if (retired)
					return new ModelAndView(
							new RedirectView("deposit.form"));
			}
			mav.addObject("totalDepositAmount", totalDepositAmount);
			mav.addObject("depositsList", deposits);
			mav.addObject("depositReasons", BillingGlobalProperties.getGpDepositReasons());

			return mav;*/
	}
	private boolean handleRetireDeposit(HttpServletRequest request,
			ModelAndView mav) {

	/*	Deposit deposit = null;

		try {
			deposit = DepositUtil.getDeposit(Integer.valueOf(request.getParameter("depositId")));
		} catch (Exception e) {
			log.error(">>>MOH>>BILLING>> The Deposit with '"
					+ request.getParameter("depositId")
					+ "' cannot be found !");
			e.printStackTrace();

			return false;
		}

		try {			
			deposit.setVoided(true);
			deposit.setVoidedDate(new Date());
			deposit.setVoidedBy(Context.getAuthenticatedUser());
			deposit.setVoidReason(request.getParameter("depositRetireReason"));

			DepositUtil.createDeposit(deposit);

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The patient deposit has been retired successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The patient deposit has not been retired !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}*/

		return true;
	}


}
