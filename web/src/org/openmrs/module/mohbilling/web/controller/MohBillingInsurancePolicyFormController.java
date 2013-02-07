/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Yves GAKUBA
 * 
 *         This controller backs the
 *         /web/module/mohBillingInsurancePolicyForm.jsp page. This controller
 *         is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */
public class MohBillingInsurancePolicyFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			boolean saved = handleSaveInsurancePolicy(request, mav);
			if (saved)
				return new ModelAndView(new RedirectView(
						"insurancePolicySearch.form"));
		} else {
			try {
				if (null != request.getParameter("patientId")) {
					// if a patientId is specified, then creation of an
					// insurancePolicy is the only option possible
					// mav
					// .addObject(
					// "insurancePolicy",
					// (new InsurancePolicyUtil())
					// .getInsurancePolicyByOwner(Context
					// .getPatientService()
					// .getPatient(
					// Integer
					// .valueOf(request
					// .getParameter("patientId")))));

					InsurancePolicy ip = new InsurancePolicy();
					ip.setOwner(Context.getPatientService().getPatient(
							Integer.valueOf(request.getParameter("patientId"))));
					mav.addObject("insurancePolicy", ip);

					mav.addObject("beneficiaryId",
							request.getParameter("patientId"));
				} else if (null != request.getParameter("insurancePolicyId")) {
					// if an insurancePolicyId is specified, then edition of an
					// insurancePolicy is the only option possible
					InsurancePolicy ip = Context.getService(
							BillingService.class).getInsurancePolicy(
							Integer.valueOf(request
									.getParameter("insurancePolicyId")));
					mav.addObject("insurancePolicy", ip);
					mav.addObject("beneficiaryId", ip.getOwner().getPatientId());
				} else
					return new ModelAndView(new RedirectView(
							"insurancePolicySearch.form"));
			} catch (Exception e) {
				log.error(">>>MOH>>BILLING>> " + e.getMessage());
				e.printStackTrace();

				return new ModelAndView(new RedirectView(
						"insurancePolicySearch.form"));
			}
		}

		new InsuranceUtil();
		mav.addObject("insurances", InsuranceUtil.getInsurances(true));

		return mav;

	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private boolean handleSaveInsurancePolicy(HttpServletRequest request,
			ModelAndView mav) {

		try {
			// insurancePolicy
			InsurancePolicy ip = new InsurancePolicy();

			if (request.getParameter("insurancePolicyCoverageStartDate") != null
					&& !request
							.getParameter("insurancePolicyCoverageStartDate")
							.equals(""))
				ip.setCoverageStartDate(Context
						.getDateFormat()
						.parse(request
								.getParameter("insurancePolicyCoverageStartDate")));
			
			if (request.getParameter("insurancePolicyExpirationDate") != null
					&& !request
							.getParameter("insurancePolicyExpirationDate")
							.equals(""))
			ip.setExpirationDate(Context.getDateFormat().parse(
					request.getParameter("insurancePolicyExpirationDate")));
			
			ip.setInsurance(Context.getService(BillingService.class)
					.getInsurance(
							Integer.valueOf(request
									.getParameter("insurancePolicyInsurance"))));
			
			if (request.getParameter("insurancePolicyOwnerCardNumber") != null
					&& !request
							.getParameter("insurancePolicyOwnerCardNumber")
							.equals(""))
			ip.setInsuranceCardNo(request
					.getParameter("insurancePolicyOwnerCardNumber"));
			
			ip.setOwner(Context.getPatientService().getPatient(
					Integer.valueOf(request
							.getParameter("insurancePolicyOwner"))));

			// beneficiaries
			for (int i = 1; i < 11; i++) {
				if (request.getParameter("insurancePolicyBeneficiary_" + i) != null
						&& request
								.getParameter("insurancePolicyBeneficiary_" + i)
								.trim().compareTo("") != 0) {
					Beneficiary b = new Beneficiary();

					b.setPatient(Context.getPatientService().getPatient(
							Integer.valueOf(request
									.getParameter("insurancePolicyBeneficiary_"
											+ i))));
					b.setPolicyIdNumber(request
							.getParameter("insurancePolicyBeneficiaryCardNumber_"
									+ i));

					b.setCreatedDate(new Date());
					b.setCreator(Context.getAuthenticatedUser());
					b.setRetired(false);

					ip.addBeneficiary(b);
				}
			}

			ip.setCreatedDate(new Date());
			ip.setCreator(Context.getAuthenticatedUser());
			ip.setRetired(false);

			InsurancePolicyUtil.createInsurancePolicy(ip);

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"The insurance policy has been saved successfully !");
		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance policy has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}

		/**
		 * Updating an existing InsurancePolicy (i.e. beneficiaries)
		 */
		if (request.getParameter("insurancePolicyId") != null) {
			try {
				InsurancePolicy ip = Context.getService(BillingService.class)
						.getInsurancePolicy(
								Integer.valueOf(request
										.getParameter("insurancePolicyId")));
				if (null == ip) {
					request.getSession().setAttribute(
							WebConstants.OPENMRS_ERROR_ATTR,
							"The insurance policy with ID ['"
									+ request.getParameter("insurancePolicyId")
									+ "'] cannot be found !");
				} else
					mav.addObject("insurancePolicy", ip);
			} catch (Exception e) {
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,
						"The insurance policy with ID ['"
								+ request.getParameter("insurancePolicyId")
								+ "'] cannot be found !");
				log.error(">>>MOH>>BILLING>> " + e.getMessage());
				e.printStackTrace();
			}
		}

		return true;
	}
}
