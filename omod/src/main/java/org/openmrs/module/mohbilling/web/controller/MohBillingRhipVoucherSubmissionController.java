package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherService;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.RhipVoucherSubmission;
import org.openmrs.module.mohbilling.model.RhipVoucherSubmissionSearchCriteria;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MohBillingRhipVoucherSubmissionController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	public static final String PRIVILEGE_VIEW = "Billing RHIP Voucher - View Submission Page";
	public static final String PRIVILEGE_SEND = "Billing RHIP Voucher - Send";
	public static final String PRIVILEGE_RETRY = "Billing RHIP Voucher - Retry";
	public static final String PRIVILEGE_VIEW_HISTORY = "Billing RHIP Voucher - View History";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private static final int DEFAULT_PAGE_SIZE = 25;
	private static final int MAX_PAGE_SIZE = 100;

	private BillingService billingService;
	private RhipVoucherService voucherService;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!Context.isAuthenticated()) {
			return new ModelAndView(new RedirectView("/login.htm"));
		}
		if (!Context.hasPrivilege(PRIVILEGE_VIEW) && !Context.hasPrivilege("Billing Configuration - View Billing Admin")) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"You do not have permission to view RHIP voucher submissions.");
			return new ModelAndView(new RedirectView("billingAdmin.form"));
		}
		if ("POST".equalsIgnoreCase(request.getMethod())) {
			return handleSubmissionAction(request);
		}
		return buildListModel(request);
	}

	private ModelAndView handleSubmissionAction(HttpServletRequest request) {
		String action = trimToNull(request.getParameter("action"));
		Integer globalBillId = parseInteger(request.getParameter("globalBillId"));
		if (globalBillId == null) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Global bill is required.");
			return redirectToList(request);
		}
		if ("retry".equalsIgnoreCase(action) && !Context.hasPrivilege(PRIVILEGE_RETRY)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "You do not have permission to retry RHIP vouchers.");
			return redirectToList(request);
		}
		if (!"retry".equalsIgnoreCase(action) && !Context.hasPrivilege(PRIVILEGE_SEND)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "You do not have permission to send RHIP vouchers.");
			return redirectToList(request);
		}
		GlobalBill globalBill = GlobalBillUtil.getGlobalBill(globalBillId);
		if (voucherService == null) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "RHIP voucher service is not configured.");
			return redirectToList(request);
		}
		RhipVoucherSubmission submission = voucherService.submitVoucherForGlobalBillWithAudit(globalBill);
		if (submission != null && RhipVoucherSubmission.STATUS_SENT.equals(submission.getStatus())) {
			String reference = StringUtils.defaultIfBlank(submission.getVoucherReferenceNumber(), submission.getVoucherCode());
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"RHIP voucher submitted successfully" + (StringUtils.isBlank(reference) ? "." : ". Reference: " + reference));
		} else {
			String error = submission == null ? "RHIP voucher submission failed." : submission.getErrorMessage();
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					StringUtils.defaultIfBlank(error, "RHIP voucher submission failed."));
		}
		return redirectToList(request);
	}

	private ModelAndView buildListModel(HttpServletRequest request) {
		String dischargeDateText = defaultIfBlank(request.getParameter("dischargeDate"), new SimpleDateFormat(DATE_PATTERN).format(new Date()));
		Date dischargeDate = parseDate(dischargeDateText);
		if (dischargeDate == null) {
			dischargeDate = new Date();
			dischargeDateText = new SimpleDateFormat(DATE_PATTERN).format(dischargeDate);
		}
		String status = defaultIfBlank(request.getParameter("status"), RhipVoucherSubmission.STATUS_NOT_SENT).toUpperCase();
		String query = trimToNull(request.getParameter("query"));
		String sortBy = defaultIfBlank(request.getParameter("sortBy"), "dischargeDate");
		String sortDirection = defaultIfBlank(request.getParameter("sortDirection"), "desc");
		int page = parsePositiveInteger(request.getParameter("page"), 1);
		int pageSize = parsePositiveInteger(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE);
		if (pageSize > MAX_PAGE_SIZE) {
			pageSize = MAX_PAGE_SIZE;
		}

		RhipVoucherSubmissionSearchCriteria criteria = new RhipVoucherSubmissionSearchCriteria();
		criteria.setDischargeStartDate(getStartOfDay(dischargeDate));
		criteria.setDischargeEndDate(getEndOfDay(dischargeDate));
		criteria.setStatus(status);
		criteria.setQuery(query);
		criteria.setSortBy(sortBy);
		criteria.setSortDirection(sortDirection);

		int totalCount = billingService == null ? 0 : billingService.countRhipVoucherSubmissionGlobalBills(criteria);
		int totalPages = totalCount == 0 ? 1 : (int) Math.ceil((double) totalCount / pageSize);
		if (page > totalPages) {
			page = totalPages;
		}
		int firstResult = (page - 1) * pageSize;
		List<GlobalBill> globalBills = billingService == null
				? Collections.<GlobalBill>emptyList()
				: billingService.getRhipVoucherSubmissionGlobalBills(criteria, firstResult, pageSize);
		List<RhipVoucherSubmissionRow> rows = buildRows(globalBills);

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("rows", rows);
		mav.addObject("historyByGlobalBillId", buildHistoryByGlobalBillId(globalBills));
		mav.addObject("consommationsByGlobalBillId", buildConsommationsByGlobalBillId(globalBills));
		mav.addObject("dischargeDate", dischargeDateText);
		mav.addObject("status", status);
		mav.addObject("query", query == null ? "" : query);
		mav.addObject("sortBy", sortBy);
		mav.addObject("sortDirection", sortDirection);
		mav.addObject("page", page);
		mav.addObject("pageSize", pageSize);
		mav.addObject("totalCount", totalCount);
		mav.addObject("totalPages", totalPages);
		mav.addObject("hasPreviousPage", page > 1);
		mav.addObject("hasNextPage", page < totalPages);
		mav.addObject("previousPage", page - 1);
		mav.addObject("nextPage", page + 1);
		mav.addObject("filterQueryString", buildFilterQueryString(request, dischargeDateText, status, query, sortBy, sortDirection, pageSize));
		mav.addObject("canSend", Context.hasPrivilege(PRIVILEGE_SEND));
		mav.addObject("canRetry", Context.hasPrivilege(PRIVILEGE_RETRY));
		mav.addObject("canViewHistory", Context.hasPrivilege(PRIVILEGE_VIEW_HISTORY));
		return mav;
	}

	private List<RhipVoucherSubmissionRow> buildRows(List<GlobalBill> globalBills) {
		List<RhipVoucherSubmissionRow> rows = new ArrayList<RhipVoucherSubmissionRow>();
		if (globalBills == null) {
			return rows;
		}
		for (GlobalBill globalBill : globalBills) {
			RhipVoucherSubmission latest = billingService == null ? null : billingService.getLatestRhipVoucherSubmission(globalBill);
			RhipVoucherSubmission successful = billingService == null ? null : billingService.getSuccessfulRhipVoucherSubmission(globalBill);
			rows.add(new RhipVoucherSubmissionRow(globalBill, latest, successful));
		}
		return rows;
	}

	private Map<Integer, List<RhipVoucherSubmission>> buildHistoryByGlobalBillId(List<GlobalBill> globalBills) {
		Map<Integer, List<RhipVoucherSubmission>> ret = new LinkedHashMap<Integer, List<RhipVoucherSubmission>>();
		if (globalBills == null || billingService == null) {
			return ret;
		}
		for (GlobalBill globalBill : globalBills) {
			ret.put(globalBill.getGlobalBillId(), billingService.getRhipVoucherSubmissionsByGlobalBill(globalBill));
		}
		return ret;
	}

	private Map<Integer, List<Consommation>> buildConsommationsByGlobalBillId(List<GlobalBill> globalBills) {
		Map<Integer, List<Consommation>> ret = new HashMap<Integer, List<Consommation>>();
		if (globalBills == null) {
			return ret;
		}
		for (GlobalBill globalBill : globalBills) {
			try {
				ret.put(globalBill.getGlobalBillId(), ConsommationUtil.getConsommationsByGlobalBill(globalBill));
			} catch (Exception e) {
				log.warn("Unable to load consommations for global bill " + globalBill.getGlobalBillId(), e);
				ret.put(globalBill.getGlobalBillId(), Collections.<Consommation>emptyList());
			}
		}
		return ret;
	}

	private ModelAndView redirectToList(HttpServletRequest request) {
		return new ModelAndView(new RedirectView("rhipVoucherSubmissions.form?" + buildRedirectQueryString(request)));
	}

	private String buildRedirectQueryString(HttpServletRequest request) {
		StringBuilder ret = new StringBuilder();
		appendQueryParam(ret, "dischargeDate", request.getParameter("dischargeDate"));
		appendQueryParam(ret, "status", request.getParameter("status"));
		appendQueryParam(ret, "query", request.getParameter("query"));
		appendQueryParam(ret, "sortBy", request.getParameter("sortBy"));
		appendQueryParam(ret, "sortDirection", request.getParameter("sortDirection"));
		appendQueryParam(ret, "page", request.getParameter("page"));
		appendQueryParam(ret, "pageSize", request.getParameter("pageSize"));
		return ret.toString();
	}

	private String buildFilterQueryString(HttpServletRequest request, String dischargeDate, String status, String query,
			String sortBy, String sortDirection, int pageSize) {
		StringBuilder ret = new StringBuilder();
		appendQueryParam(ret, "dischargeDate", dischargeDate);
		appendQueryParam(ret, "status", status);
		appendQueryParam(ret, "query", query);
		appendQueryParam(ret, "sortBy", sortBy);
		appendQueryParam(ret, "sortDirection", sortDirection);
		appendQueryParam(ret, "pageSize", String.valueOf(pageSize));
		return ret.toString();
	}

	private void appendQueryParam(StringBuilder builder, String name, String value) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return;
		}
		if (builder.length() > 0) {
			builder.append("&");
		}
		try {
			builder.append(name).append("=").append(URLEncoder.encode(trimmed, "UTF-8"));
		} catch (Exception e) {
			builder.append(name).append("=").append(trimmed);
		}
	}

	private Date parseDate(String value) {
		try {
			return new SimpleDateFormat(DATE_PATTERN).parse(value);
		}
		catch (ParseException e) {
			return null;
		}
	}

	private Date getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	private int parsePositiveInteger(String value, int defaultValue) {
		Integer parsed = parseInteger(value);
		return parsed == null || parsed <= 0 ? defaultValue : parsed;
	}

	private Integer parseInteger(String value) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return null;
		}
		try {
			return Integer.valueOf(trimmed);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.length() == 0 ? null : trimmed;
	}

	private String defaultIfBlank(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value.trim();
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public void setVoucherService(RhipVoucherService voucherService) {
		this.voucherService = voucherService;
	}

	public static class RhipVoucherSubmissionRow {
		private final GlobalBill globalBill;
		private final RhipVoucherSubmission latestSubmission;
		private final RhipVoucherSubmission successfulSubmission;

		public RhipVoucherSubmissionRow(GlobalBill globalBill, RhipVoucherSubmission latestSubmission,
				RhipVoucherSubmission successfulSubmission) {
			this.globalBill = globalBill;
			this.latestSubmission = latestSubmission;
			this.successfulSubmission = successfulSubmission;
		}

		public GlobalBill getGlobalBill() {
			return globalBill;
		}

		public RhipVoucherSubmission getLatestSubmission() {
			return latestSubmission;
		}

		public String getEffectiveStatus() {
			if (successfulSubmission != null || StringUtils.isNotBlank(globalBill.getRhipVoucherCode())
					|| StringUtils.isNotBlank(globalBill.getRhipVoucherReferenceNumber())) {
				return RhipVoucherSubmission.STATUS_SENT;
			}
			return latestSubmission == null ? RhipVoucherSubmission.STATUS_NOT_SENT : latestSubmission.getStatus();
		}

		public String getDisplayStatus() {
			String status = getEffectiveStatus();
			if (RhipVoucherSubmission.STATUS_SENT.equals(status)) {
				return "Sent";
			}
			if (RhipVoucherSubmission.STATUS_FAILED.equals(status)) {
				return "Failed";
			}
			if (RhipVoucherSubmission.STATUS_PROCESSING.equals(status)) {
				return "Processing";
			}
			return "Not Sent";
		}

		public Date getLastSubmissionDate() {
			return latestSubmission == null ? null : latestSubmission.getDateSubmitted();
		}

		public String getSubmissionMessage() {
			return latestSubmission == null ? "" : StringUtils.defaultIfBlank(latestSubmission.getErrorMessage(), "");
		}

		public String getVoucherReference() {
			if (successfulSubmission != null) {
				return StringUtils.defaultIfBlank(successfulSubmission.getVoucherReferenceNumber(), successfulSubmission.getVoucherCode());
			}
			return StringUtils.defaultIfBlank(globalBill.getRhipVoucherReferenceNumber(), globalBill.getRhipVoucherCode());
		}

		public String getPatientName() {
			Patient patient = getPatient();
			return patient == null || patient.getPersonName() == null ? "" : patient.getPersonName().toString();
		}

		public String getPatientIdentifier() {
			Patient patient = getPatient();
			if (patient == null || patient.getIdentifiers() == null || patient.getIdentifiers().isEmpty()) {
				return "";
			}
			PatientIdentifier identifier = patient.getPatientIdentifier();
			return identifier == null ? "" : identifier.getIdentifier();
		}

		public String getInsuranceName() {
			Insurance insurance = globalBill.getInsurance();
			return insurance == null ? "" : insurance.getName();
		}

		public String getInsuranceCardNumber() {
			InsurancePolicy policy = getInsurancePolicy();
			return policy == null ? "" : policy.getInsuranceCardNo();
		}

		public BigDecimal getPatientContribution() {
			BigDecimal amount = globalBill.getGlobalAmount() == null ? BigDecimal.ZERO : globalBill.getGlobalAmount();
			InsurancePolicy policy = getInsurancePolicy();
			if (policy == null || policy.getInsurance() == null || policy.getInsurance().getCurrentRate() == null
					|| policy.getInsurance().getCurrentRate().getRate() == null) {
				return BigDecimal.ZERO;
			}
			BigDecimal patientRate = BigDecimal.valueOf(100).subtract(
					BigDecimal.valueOf(policy.getInsurance().getCurrentRate().getRate()));
			return amount.multiply(patientRate).divide(BigDecimal.valueOf(100));
		}

		public BigDecimal getInsuranceContribution() {
			BigDecimal amount = globalBill.getGlobalAmount() == null ? BigDecimal.ZERO : globalBill.getGlobalAmount();
			BigDecimal patientContribution = getPatientContribution();
			return amount.subtract(patientContribution);
		}

		private Patient getPatient() {
			InsurancePolicy policy = getInsurancePolicy();
			return policy == null ? null : policy.getOwner();
		}

		private InsurancePolicy getInsurancePolicy() {
			Admission admission = globalBill == null ? null : globalBill.getAdmission();
			return admission == null ? null : admission.getInsurancePolicy();
		}
	}
}
