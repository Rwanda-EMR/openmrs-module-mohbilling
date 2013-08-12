/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingPatientBillPaymentFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			handleSavePatientBillPayment(request);
		}

		try {
			PatientBill pb = null;
			List<PatientBill> patientBills = null;

			if (request.getParameter("patientBillId") == null)
				return new ModelAndView(new RedirectView(
						"patientSearchBill.form"));

			pb = Context.getService(BillingService.class).getPatientBill(
					Integer.parseInt(request.getParameter("patientBillId")));
			patientBills = PatientBillUtil.getBillsByBeneficiary(pb
					.getBeneficiary());

			mav.addObject("patientBill", pb);
			mav.addObject("patientBills", patientBills);
			mav.addObject("beneficiary", pb.getBeneficiary());

			InsurancePolicy ip = pb.getBeneficiary().getInsurancePolicy();
			mav.addObject("insurancePolicy", ip);

			// check the validity of the insurancePolicy for today
			Date today = new Date();
			mav
					.addObject("valid",
							((ip.getCoverageStartDate().getTime() <= today
									.getTime()) && (today.getTime() <= ip
									.getExpirationDate().getTime())));
			mav.addObject("todayDate", today);
			mav.addObject("authUser", Context.getAuthenticatedUser());

		} catch (Exception e) {
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			return new ModelAndView(new RedirectView("patientSearchBill.form"));
		}

		return mav;
	}

	/**
	 * @param request
	 * @return
	 */
	private BillPayment handleSavePatientBillPayment(HttpServletRequest request) {
		BillPayment billPayment = null;

		try {
			PatientBill pb = Context.getService(BillingService.class)
					.getPatientBill(
							Integer.parseInt(request
									.getParameter("patientBillId")));

			if (null != request.getParameter("receivedCash")){
					//&& Integer.parseInt(request.getParameter("receivedCash")
							//.trim()) > 0) {
				BillPayment bp = new BillPayment();
				bp.setAmountPaid(BigDecimal.valueOf(Double.parseDouble(request
						.getParameter("receivedCash").trim())));
				bp.setCollector(Context.getUserService()
						.getUser(
								Integer.parseInt(request
										.getParameter("billCollector"))));
				bp.setDateReceived(Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));
				bp.setPatientBill(pb);

				bp.setCreatedDate(new Date());
				bp.setCreator(Context.getAuthenticatedUser());

				billPayment = PatientBillUtil.createBillPayment(bp);

				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR,
						"The Bill Payment has been saved successfully !");
				return billPayment;

			} else {
				request
						.getSession()
						.setAttribute(
								WebConstants.OPENMRS_MSG_ATTR,
								"The Bill Payment cannot be saved when the 'Received Amount' is not greater than 0 !");
				return null;
			}

		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The Bill Payment has not been saved !");
			log.error("" + e.getMessage());
			e.printStackTrace();

			return null;
		}

	}

}
