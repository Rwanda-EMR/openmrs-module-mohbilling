/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Kamonyo
 */
public class MohBillingRefundBillFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		Integer billItemId = null;

		if (request.getParameter("billItemId") != null
				&& !request.getParameter("billItemId").equals("")) {

			billItemId = Integer.parseInt(request.getParameter("billItemId"));
		}

		if (request.getParameter("ipCardNumber") != null
				&& !request.getParameter("ipCardNumber").equals("")) {

			Beneficiary ben = InsurancePolicyUtil
					.getBeneficiaryByPolicyIdNo(request
							.getParameter("ipCardNumber"));
			mav.addObject("beneficiary", ben);

			InsurancePolicy ip = InsurancePolicyUtil
					.getInsurancePolicyByBeneficiary(ben);
			mav.addObject("insurancePolicy", ip);
		}

		if (request.getParameter("patientBillId") != null
				&& !request.getParameter("patientBillId").equals("")) {

			PatientBill bill = PatientBillUtil.getPatientBillById(Integer
					.parseInt(request.getParameter("patientBillId")));
			if (billItemId != null)
				if (request.getParameter("removeIt") != null)
					if (!request.getParameter("removeIt").equals("false")) {
						PatientServiceBill temp = null;
						for (PatientServiceBill psb : bill.getBillItems()) {
							if (psb.getPatientServiceBillId().intValue() == billItemId
									.intValue()) {
								temp = psb;
								break;
							}
						}

						// Removing the no-needed bill item...
						bill.removeBillItem(temp);

						// Saving the changes made...
						PatientBillUtil.savePatientBill(bill);
					}

			mav.addObject("patientBill", bill);
			mav.addObject("index", bill.getBillItems().size());
		}

		return mav;
	}
}
