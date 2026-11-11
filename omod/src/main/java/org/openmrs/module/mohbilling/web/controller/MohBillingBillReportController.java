package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillReportUtil;
import org.openmrs.module.mohbilling.businesslogic.BillReportUtil.InsuranceFilter;
import org.openmrs.module.mohbilling.businesslogic.BillReportUtil.PagedDisplayRows;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.model.BillReportFacilityHeader;
import org.openmrs.module.mohbilling.model.BillReportItem;
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

		if ("true".equalsIgnoreCase(request.getParameter("export"))) {
			exportBillReport(request, response);
			return null;
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("insuranceOptions", BillReportUtil.buildInsuranceFilterOptions());
		mav.addObject("admissions", Collections.emptyList());
		mav.addObject("facilityHeader", BillReportUtil.buildFacilityHeader());
		mav.addObject("page", 1);
		mav.addObject("pageSize", BillReportUtil.DEFAULT_WEB_PAGE_SIZE);
		mav.addObject("totalCount", 0);
		mav.addObject("totalPages", 1);

		if (request.getParameter("formStatus") == null || request.getParameter("formStatus").equals("")) {
			return mav;
		}

		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String insuranceFilterStr = request.getParameter("insuranceFilter");
		if (insuranceFilterStr == null || insuranceFilterStr.trim().isEmpty()) {
			// Backward compatibility with older form field name
			insuranceFilterStr = request.getParameter("insuranceId");
		}

		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);
		mav.addObject("selectedInsuranceFilter", insuranceFilterStr);

		Date startDate = parseDayStart(startDateStr);
		Date endDate = parseDayEnd(endDateStr);
		if (startDate == null || endDate == null) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Start Date and End Date are required.");
			return mav;
		}

		int page = parsePositiveInt(request.getParameter("page"), 1);
		int pageSize = parsePositiveInt(request.getParameter("pageSize"), BillReportUtil.DEFAULT_WEB_PAGE_SIZE);
		if (pageSize > BillReportUtil.DEFAULT_WEB_PAGE_SIZE) {
			pageSize = BillReportUtil.DEFAULT_WEB_PAGE_SIZE;
		}

		InsuranceFilter insuranceFilter = BillReportUtil.resolveInsuranceFilter(insuranceFilterStr);
		PagedDisplayRows pageResult = BillReportUtil.buildAdmissionDisplayRowsPage(startDate, endDate,
				insuranceFilter, page, pageSize);

		log.info("Bill Report admissions search: start=" + startDate + ", end=" + endDate
				+ ", insuranceFilter=" + insuranceFilterStr + ", page=" + pageResult.getPage()
				+ ", pageSize=" + pageResult.getPageSize() + ", pageRows=" + pageResult.getRows().size()
				+ ", totalCount=" + pageResult.getTotalCount());

		mav.addObject("admissions", pageResult.getRows());
		mav.addObject("page", pageResult.getPage());
		mav.addObject("pageSize", pageResult.getPageSize());
		mav.addObject("totalCount", pageResult.getTotalCount());
		mav.addObject("totalPages", pageResult.getTotalPages());
		mav.addObject("rowNumberOffset", pageResult.getRowNumberOffset());
		mav.addObject("resultMsg",
				"Found " + pageResult.getTotalCount() + " billed item(s) from " + startDateStr + " to " + endDateStr
						+ " (showing page " + pageResult.getPage() + " of " + pageResult.getTotalPages()
						+ ", " + pageResult.getPageSize() + " per page)");
		return mav;
	}

	private void exportBillReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String insuranceFilterStr = request.getParameter("insuranceFilter");
		if (insuranceFilterStr == null || insuranceFilterStr.trim().isEmpty()) {
			insuranceFilterStr = request.getParameter("insuranceId");
		}

		Date startDate = parseDayStart(startDateStr);
		Date endDate = parseDayEnd(endDateStr);
		if (startDate == null || endDate == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Start Date and End Date are required.");
			return;
		}

		InsuranceFilter insuranceFilter = BillReportUtil.resolveInsuranceFilter(insuranceFilterStr);
		List<BillReportItem> reportRows = BillReportUtil.buildReportItems(startDate, endDate, insuranceFilter);
		BillReportFacilityHeader facilityHeader = BillReportUtil.buildFacilityHeader();
		Insurance insurance = insuranceFilter != null ? insuranceFilter.getInsurance() : null;
		if (insurance == null && insuranceFilter != null && insuranceFilter.getCategory() != null) {
			insurance = new Insurance();
			insurance.setName(insuranceFilter.getCategory());
		}

		log.info("Bill Report Excel export: start=" + startDate + ", end=" + endDate
				+ ", insuranceFilter=" + insuranceFilterStr + ", rows=" + reportRows.size());
		FileExporter.exportBillReportData(response, insurance, facilityHeader, reportRows, startDate, endDate);
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

	private static int parsePositiveInt(String value, int defaultValue) {
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			int parsed = Integer.parseInt(value.trim());
			return parsed > 0 ? parsed : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
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
