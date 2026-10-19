package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MohBillingCohortBuilderFormController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		String hiddenInsuranceName = getHiddenInsuranceName();


		if (request.getParameter("formStatus") != null
				&& !request.getParameter("formStatus").equals("")) {
			String startDateStr = request.getParameter("startDate");
			String startHourStr = request.getParameter("startHour");
			String startMinStr = request.getParameter("startMinute");

			String endDateStr = request.getParameter("endDate");
			String endHourStr = request.getParameter("endHour");
			String endMinuteStr = request.getParameter("endMinute");

			String collectorStr = null;
			String insuranceStr = null;
			String thirdPartyStr = null;
			String billStatus = null;
			String departmentStr = null;


			User creator = null;
			Department department = null;

			String billCreatorStr = null;
			if(request.getParameter("billCreator")!=null &&!request.getParameter("billCreator").equals("")){
				billCreatorStr=request.getParameter("billCreator");
				creator = Context.getUserService().getUser(Integer.valueOf(billCreatorStr));
			}
			if(request.getParameter("billStatus")!=null&&!request.getParameter("billStatus").equals(""))
				billStatus = request.getParameter("billStatus");

			if(request.getParameter("departmentId")!=null&&!request.getParameter("departmentId").equals("")){
				departmentStr = request.getParameter("departmentId");
				department = DepartementUtil.getDepartement(Integer.valueOf(departmentStr));
			}

			// parameters
			Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);

			Date startDate = (Date) params[0];
			Date endDate = (Date) params[1];

			Insurance insurance = null;
			ThirdParty thirdParty =null;
			if(request.getParameter("insuranceId")!=null &&!request.getParameter("insuranceId").equals("") ){
				insurance = InsuranceUtil.getInsurance(Integer.valueOf(request.getParameter("insuranceId")));
			}
			if(request.getParameter("thirdPartyId")!=null && !request.getParameter("thirdPartyId").equals(""))
				thirdParty = Context.getService(BillingService.class).getThirdParty(Integer.valueOf(request.getParameter("thirdPartyId")));

			List<Consommation> cons = ConsommationUtil.getConsommationsOld(startDate, endDate, insurance, thirdParty, creator, department);
			if (hiddenInsuranceName != null) {
				List<Consommation> consWithoutPrivate = new ArrayList<Consommation>();
				for (Consommation con : cons) {
					if (!isInsurance(con.getBeneficiary().getInsurancePolicy().getInsurance(), hiddenInsuranceName))
						consWithoutPrivate.add(con);
				}
				cons = consWithoutPrivate;
			}
			String globalBillStatus = null;
			if(request.getParameter("globalBillStatus")!=null&&!request.getParameter("globalBillStatus").equals(""))
				globalBillStatus = request.getParameter("globalBillStatus");

			// one status per bill, used by both the filters and the table
			Map<Integer, String> billStatuses = new HashMap<Integer, String>();
			Map<Integer, String> globalBillStatuses = new HashMap<Integer, String>();
			List<Consommation> filteredCons = new ArrayList<Consommation>();
			for (Consommation con : cons) {
				String conBillStatus = getBillStatus(con.getPatientBill());
				String conGlobalBillStatus = getGlobalBillStatus(con.getGlobalBill());
				if ((billStatus == null || billStatus.equals(conBillStatus))
						&& (globalBillStatus == null || globalBillStatus.equals(conGlobalBillStatus))) {
					filteredCons.add(con);
					billStatuses.put(con.getConsommationId(), conBillStatus);
					globalBillStatuses.put(con.getConsommationId(), conGlobalBillStatus);
				}
			}
			mav.addObject("consommations", filteredCons);
			mav.addObject("billStatuses", billStatuses);
			mav.addObject("globalBillStatuses", globalBillStatuses);
			mav.addObject("billStatus", billStatus);
			mav.addObject("globalBillStatus", globalBillStatus);
		}
		mav.addObject("showGlobalBillStatusFilter", true);
		List<Insurance> insurances = InsuranceUtil.getAllInsurances();
		if (hiddenInsuranceName != null) {
			List<Insurance> insurancesWithoutPrivate = new ArrayList<Insurance>();
			for (Insurance ins : insurances) {
				if (!isInsurance(ins, hiddenInsuranceName))
					insurancesWithoutPrivate.add(ins);
			}
			insurances = insurancesWithoutPrivate;
		}
		mav.addObject("insurances", insurances);
		mav.addObject("thirdParties", InsurancePolicyUtil.getAllThirdParties());
		mav.addObject("departments", DepartementUtil.getAllHospitalDepartements());
		return mav;
	}

	/**
	 * UNPAID when nothing was paid, FULLY PAID when payments cover the patient bill, PARTLY PAID otherwise
	 */
	private String getBillStatus(PatientBill patientBill) {
		if (patientBill == null || patientBill.getPayments() == null || patientBill.getPayments().isEmpty())
			return "UNPAID";
		BigDecimal paid = BigDecimal.ZERO;
		for (BillPayment bp : patientBill.getPayments()) {
			if (bp.getAmountPaid() != null)
				paid = paid.add(bp.getAmountPaid());
		}
		BigDecimal due = patientBill.getAmount() == null ? BigDecimal.ZERO : patientBill.getAmount();
		return paid.compareTo(due) >= 0 ? "FULLY PAID" : "PARTLY PAID";
	}

	private String getGlobalBillStatus(GlobalBill globalBill) {
		return globalBill != null && globalBill.getClosingDate() != null ? "DISCHARGED" : "NOT DISCHARGED";
	}

	/**
	 * Name of the insurance to hide: the private insurance named in rwandaemr.autoCreateInsuranceType,
	 * or null when mohbilling.findBills.hidePrivateInsurance is not true or no private insurance is set
	 */
	private String getHiddenInsuranceName() {
		boolean hide = Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_FIND_BILLS_HIDE_PRIVATE_INSURANCE, "false").trim());
		if (!hide)
			return null;
		String privateInsuranceName = Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_PRIVATE_INSURANCE_NAME);
		if (privateInsuranceName == null || privateInsuranceName.trim().isEmpty()) {
			log.warn(BillingConstants.GLOBAL_PROPERTY_PRIVATE_INSURANCE_NAME + " is not set, so no insurance is hidden on Find Bills");
			return null;
		}
		return privateInsuranceName.trim();
	}

	private boolean isInsurance(Insurance insurance, String insuranceName) {
		return insurance != null && insurance.getName() != null && insuranceName.equalsIgnoreCase(insurance.getName().trim());
	}
}
