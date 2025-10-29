package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class MohBillingGlobalBillListController extends ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();

		String ipCardNumber = request.getParameter("ipCardNumber");
		String billIdentifier = request.getParameter("billIdentifier");
		int page = 1;
		int size = 20;

		try {
			if (request.getParameter("page") != null) {
				page = Integer.parseInt(request.getParameter("page"));
			}
			if (request.getParameter("size") != null) {
				size = Integer.parseInt(request.getParameter("size"));
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid pagination parameters, using defaults");
		}

		Beneficiary ben = null;
		List<GlobalBill> globalBills = null;
		int totalBills = 0;

		if (ipCardNumber != null) {
			ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(ipCardNumber);
			InsurancePolicy ip = InsurancePolicyUtil.getInsurancePolicyByBeneficiary(ben);

			totalBills = GlobalBillUtil.countGlobalBillsByInsurancePolicy(ip);
			globalBills = GlobalBillUtil.getGlobalBillsByInsurancePolicy(ip, page, size);

			mav.addObject("beneficiary", ben);
			mav.addObject("insurancePolicy", ip);
		}

		if (billIdentifier != null) {
			GlobalBill globalBill = GlobalBillUtil.getGlobalBillByBillIdentifier(billIdentifier);
			globalBills = java.util.Collections.singletonList(globalBill);

			String insuranceCardNo = globalBill.getAdmission().getInsurancePolicy().getInsuranceCardNo();
			ben = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(insuranceCardNo);

			mav.addObject("beneficiary", ben);
			mav.addObject("insurancePolicy", globalBill.getAdmission().getInsurancePolicy());

			totalBills = 1; // only one bill in this case
			page = 1;
			size = 1;
		}

		if (ben != null) {
			mav.addObject("patientAccount", PatientAccountUtil.getPatientAccountByPatient(ben.getPatient()));
		}

		int totalPages = (int) Math.ceil((double) totalBills / size);

		mav.addObject("globalBills", globalBills);
		mav.addObject("totalBills", totalBills);
		mav.addObject("totalPages", totalPages);
		mav.addObject("currentPage", page);
		mav.addObject("pageSize", size);

		mav.setViewName(getViewName());
		return mav;
	}
}
