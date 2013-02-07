/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingCheckPatientBillPaymentFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("patientId") != null) {
			List<PatientBill> pbList = PatientBillUtil
					.getBillsByPatient(Context.getPatientService().getPatient(
							Integer.valueOf(request.getParameter("patientId"))));

			mav.addObject("patientBills", pbList);
		}

		return mav;

	}

}
