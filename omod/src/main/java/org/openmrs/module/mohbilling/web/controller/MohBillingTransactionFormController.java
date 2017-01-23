/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.Transaction;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author rbcemr
 *
 */
public class MohBillingTransactionFormController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		
		Patient patient = Context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId")));
		String type = request.getParameter("type");
		if(request.getParameter("save")!=null )	{	

			Transaction transaction = null;
			PatientAccount account =null;
			BigDecimal balance=new BigDecimal(0);
			BigDecimal transAmount = BigDecimal.valueOf(Double.valueOf(request.getParameter("amount")));
			if(request.getParameter("patientAccountId").equals("")){
				account = new PatientAccount();
				
			}
			else{
				Integer accountId = Integer.valueOf(request.getParameter("patientAccountId"));
				account = PatientAccountUtil.getPatientAccountById(accountId);
			}
			if(type.equals("Withdrawal")){
				transAmount=transAmount.negate();
			}
			balance=account.getBalance().add(transAmount);
			
			account.setBalance(balance);
			account.setPatient(patient);
			account.setCreatedDate(new Date());
			account.setCreator(Context.getAuthenticatedUser());
			
			transaction = new Transaction();
			transaction.setAmount(transAmount);
			transaction.setTransactionDate(Context.getDateFormat().parse(request.getParameter("receivedDate")));
			transaction.setCreatedDate(new Date());
			transaction.setCreator(Context.getAuthenticatedUser());
			transaction.setPatientAccount(account);
			transaction.setReason(request.getParameter("reason"));
			/*transaction.setCollector(Context.getUserService().getUser(Integer.valueOf(request.getParameter("collector"))));	*/
			transaction.setCollector(Context.getAuthenticatedUser());
			PatientAccountUtil.createTransaction(transaction);
			
			
			return new ModelAndView(new RedirectView("searchPatientAccount.form?patientId="+account.getPatient().getPatientId()));
		}		
		mav.addObject("patientAccount", PatientAccountUtil.getPatientAccountByPatient(patient));
		mav.addObject("patient", patient);
		mav.addObject("type", type);
		
		/*if(request.getParameter("transactionId")!=null){
		log.info("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJSSSSSSSSSSSSS "+request.getParameter("transactionId"));
		mav.addObject("transaction", PatientAccountUtil.getTransactionById(Integer.valueOf(request.getParameter("transactionId"))));
		}*/
		
		return mav;
	}

}
