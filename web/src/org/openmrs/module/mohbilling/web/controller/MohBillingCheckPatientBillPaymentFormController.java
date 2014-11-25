/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		
		if (request.getParameter("patientId") != null && !request.getParameter("patientId").equals("")
				&& request.getParameter("dateFrom") != null && !request.getParameter("dateFrom").equals("")
				&& request.getParameter("dateTo") != null && !request.getParameter("dateTo").equals("")) {
				
			String startDateStr = request.getParameter("dateFrom");
			String endDateStr = request.getParameter("dateTo");

			Date startDate = sdf.parse(startDateStr.split("/")[2] + "-"
					+ startDateStr.split("/")[1] + "-"
					+ startDateStr.split("/")[0]);
			
			Date endDate = sdf.parse(endDateStr.split("/")[2] + "-"
					+ endDateStr.split("/")[1] + "-"
					+ endDateStr.split("/")[0]);
				
				// List<PatientBill> pbList = PatientBillUtil
				// .getBillsByPatient(Context.getPatientService()
				// .getPatient(
				// Integer.valueOf(request
				// .getParameter("patientId"))));
			
			System.out.println("Start Date: " + startDate + " End Date: " + endDate);
			

			List<PatientBill> pbList = Context.getService(
					BillingService.class).billCohortBuilder(null, startDate,
					endDate, Integer.valueOf(request.getParameter("patientId")), null, null,
					null);

			/** Updating status for unmarked ones */
			// for(PatientBill bill: pbList)
			// if(bill.getStatus() == null)
			// PatientBillUtil.markBillAsPaid(bill);

			mav.addObject("policies", InsurancePolicyUtil
					.getPolicyIdByPatient(Integer.valueOf(request
							.getParameter("patientId"))));

			mav.addObject("patientBills", pbList);

		} else {
			request.getSession()
					.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"The Patient is not selected or date are not filled. Please select and retry!");
		}

		return mav;

	}

}
