/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingBillingFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			PatientBill pb = handleSavePatientBill(request, mav);
			if (null == pb)
				return new ModelAndView(new RedirectView(
						"billing.form?insurancePolicyId="
								+ request.getParameter("insurancePolicyId")
								+ "&ipCardNumber="
								+ request.getParameter("ipCardNumber")));
			else
				return new ModelAndView(new RedirectView(
						"patientBillPayment.form?patientBillId="
								+ pb.getPatientBillId() + "&ipCardNumber="
								+ pb.getBeneficiary().getPolicyIdNumber()));
		}

		try {
			if (request.getParameter("ipCardNumber") == null)
				return new ModelAndView(new RedirectView(
						"patientSearchBill.form"));

			Beneficiary ben = InsurancePolicyUtil
					.getBeneficiaryByPolicyIdNo(request
							.getParameter("ipCardNumber"));
			mav.addObject("beneficiary", ben);

			InsurancePolicy ip = InsurancePolicyUtil
					.getInsurancePolicyByBeneficiary(ben);
			mav.addObject("insurancePolicy", ip);

			// check the validity of the insurancePolicy for today
			Date today = new Date();
			mav
					.addObject("valid",
							((ip.getCoverageStartDate().getTime() <= today
									.getTime()) && (today.getTime() <= ip
									.getExpirationDate().getTime())));

		} catch (Exception e) {
		//	log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			return new ModelAndView(new RedirectView("patientSearchBill.form"));
		}

		return mav;

	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private PatientBill handleSavePatientBill(HttpServletRequest request,
			ModelAndView mav) {

		PatientBill savePatientBill = null;

		try {
			int numberOfServicesClicked = Integer.valueOf(request
					.getParameter("numberOfServicesClicked"));

			PatientBill pb = new PatientBill();

//			pb.setAmount(BigDecimal.valueOf(Double.valueOf(request
//					.getParameter("totalAmount"))));

			// Patient owner = Context.getPatientService().getPatient(
			// Integer.valueOf(request.getParameter("patientId")));

			Beneficiary beneficiary = InsurancePolicyUtil
					.getBeneficiaryByPolicyIdNo(request
							.getParameter("ipCardNumber"));

			// log
			// .info(">>>>MOH>>BILLING>> Trying to find corresponding Insurance Policy for "
			// + beneficiary.getPatient().getPersonName());
			// InsurancePolicy ip = ipUtil
			// .getInsurancePolicyByBeneficiary(beneficiary);

			// log.info(">>>>MOH>>BILLING>> Corresponding Insurance Policy : "
			// + ip);
			//
			// log
			// .info(">>>>MOH>>BILLING>> The Owner of Insurance Policy as Beneficiary : "
			// + ipUtil.getInsurancePolicyOwner(ip));

			pb.setBeneficiary(beneficiary);
			pb.setIsPaid(false);
			pb.setPrinted(false);

			pb.setCreatedDate(new Date());
			pb.setCreator(Context.getAuthenticatedUser());
			pb.setVoided(false);

			for (int i = 0; i < numberOfServicesClicked; i++) {
				if (request.getParameter("billableServiceId_" + i) != null) {

					PatientServiceBill psb = new PatientServiceBill();

					BillableService bs = InsuranceUtil
							.getValidBillableService(Integer.valueOf(request
									.getParameter("billableServiceId_" + i)));
					psb.setService(bs);
					psb.setQuantity(Integer.valueOf(request
							.getParameter("quantity_" + i)));
					psb.setServiceDate(new Date());
					psb.setUnitPrice(BigDecimal.valueOf(Double.valueOf(request
							.getParameter("servicePrice_" + i))));

					psb.setCreatedDate(new Date());
					psb.setCreator(Context.getAuthenticatedUser());
					psb.setVoided(false);

					pb.addBillItem(psb);
				}
			}

			// save the patientBill
			savePatientBill = PatientBillUtil.savePatientBill(pb);

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The Patient Bill has been saved successfully !");

			return savePatientBill;

		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The Patient Bill has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());

			e.printStackTrace();

			return null;
		}

		// return true;
	}
}
