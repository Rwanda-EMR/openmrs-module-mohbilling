package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillReportUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CBHI Bill Report — admissions listing (expanded to bill items) + editable facility settings.
 */
public class MohBillingBillReportController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		if ("true".equals(request.getParameter("ajaxSaveFacilitySetting"))) {
			handleAjaxSaveFacilitySetting(request, response);
			return null;
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		mav.addObject("admissions", Collections.emptyList());
		mav.addObject("facilityHeader", BillReportUtil.buildFacilityHeader());

		if (request.getParameter("formStatus") == null || request.getParameter("formStatus").equals("")) {
			return mav;
		}

		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String insuranceStr = request.getParameter("insuranceId");

		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);

		Integer insuranceId = null;
		if (insuranceStr != null && !insuranceStr.trim().isEmpty()) {
			insuranceId = Integer.valueOf(insuranceStr);
		}
		mav.addObject("selectedInsuranceId", insuranceId);

		Date startDate = parseDayStart(startDateStr);
		Date endDate = parseDayEnd(endDateStr);
		if (startDate == null || endDate == null) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Start Date and End Date are required.");
			return mav;
		}

		Insurance insurance = insuranceId != null ? InsuranceUtil.getInsurance(insuranceId) : null;
		List<Map<String, Object>> admissionRows = BillReportUtil.buildAdmissionDisplayRows(startDate, endDate,
				insurance);

		log.info("Bill Report admissions search: start=" + startDate + ", end=" + endDate
				+ ", insuranceId=" + insuranceId + ", rowsFound=" + admissionRows.size());

		mav.addObject("admissions", admissionRows);
		mav.addObject("resultMsg",
				"Found " + admissionRows.size() + " billed item(s) from " + startDateStr + " to " + endDateStr
						+ " (limited to first 10 patients for testing)");
		return mav;
	}

	private void handleAjaxSaveFacilitySetting(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		try {
			if (!Context.isAuthenticated()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.write("{\"success\":false,\"message\":\"Not authenticated\"}");
				return;
			}
			String fieldKey = request.getParameter("fieldKey");
			String value = request.getParameter("value");
			String saved = BillReportUtil.saveFacilitySetting(fieldKey, value);
			out.write("{\"success\":true,\"fieldKey\":\"" + escapeJson(fieldKey) + "\",\"value\":\""
					+ escapeJson(saved) + "\"}");
		} catch (Exception e) {
			log.error("Failed to save Bill Report facility setting", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write("{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
		} finally {
			out.flush();
		}
	}

	private static String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	private static Date parseDayStart(String dateStr) {
		return parseDate(dateStr, " 00:00:00");
	}

	private static Date parseDayEnd(String dateStr) {
		return parseDate(dateStr, " 23:59:59");
	}

	private static Date parseDate(String dateStr, String timeSuffix) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		try {
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			return formatter.parse(dateStr.trim() + timeSuffix);
		} catch (ParseException e) {
			return null;
		}
	}
}
