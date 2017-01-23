/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.Transaction;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rbcemr
 *
 */
public class MohBillingSearchPatientAccountFormController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		Patient patient = null;
		
		if(request.getParameter("patientId")!=null )	{	
			patient = Context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId")));
			mav.addObject("patient", patient);
			if (PatientAccountUtil.isPatientAccountExists(patient)==true) {
				mav.addObject("patientAccount", PatientAccountUtil.getPatientAccountByPatient(patient));
				PatientAccount account = PatientAccountUtil.getPatientAccountByPatient(patient);
				mav.addObject("transactions", account.getTransactions());
			}
			else {
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						" To make deposit you must create the account for "+patient.getPersonName()+" !");
				return new ModelAndView(new RedirectView("transaction.form?patientId="+patient.getPatientId()));
			}		
		}
		if (request.getParameter("printed")!=null) {
			Transaction transaction = PatientAccountUtil.getTransactionById(Integer.valueOf(request.getParameter("printed")));
			FileExporter fexp = new FileExporter();
			String fileName = "DepositReceipt_"+transaction.getPatientAccount().getPatient().getPatientId()+"_"+transaction.getTransactionId()+".pdf";
			fexp.printTransaction(request, response,transaction,fileName);
		}
		   mav.setViewName(getViewName());
		return mav;
	}

}
