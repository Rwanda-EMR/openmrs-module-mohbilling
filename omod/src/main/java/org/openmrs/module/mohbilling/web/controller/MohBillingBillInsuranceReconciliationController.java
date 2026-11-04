package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.model.BillInsuranceReconciliationItem;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Bill Report vs Insurance Report reconciliation (global bills missing from either side).
 */
public class MohBillingBillInsuranceReconciliationController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		if ("true".equalsIgnoreCase(request.getParameter("export"))) {
			exportReconciliation(request, response);
			return null;
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("rows", Collections.emptyList());

		if (request.getParameter("formStatus") == null || request.getParameter("formStatus").equals("")) {
			return mav;
		}

		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);

		Date startDate = parseDayStart(startDateStr);
		Date endDate = parseDayEnd(endDateStr);
		if (startDate == null || endDate == null) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Start Date and End Date are required.");
			return mav;
		}

		List<BillInsuranceReconciliationItem> rows = Context.getService(BillingService.class)
				.getBillInsuranceReconciliation(startDate, endDate);
		log.info("Bill/Insurance reconciliation: start=" + startDate + ", end=" + endDate
				+ ", discrepancies=" + rows.size());
		mav.addObject("rows", rows);
		mav.addObject("resultMsg",
				"Found " + rows.size() + " bill(s) not present in both reports from "
						+ startDateStr + " to " + endDateStr);
		return mav;
	}

	private void exportReconciliation(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		Date startDate = parseDayStart(startDateStr);
		Date endDate = parseDayEnd(endDateStr);
		if (startDate == null || endDate == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Start Date and End Date are required.");
			return;
		}
		List<BillInsuranceReconciliationItem> rows = Context.getService(BillingService.class)
				.getBillInsuranceReconciliation(startDate, endDate);
		FileExporter.exportBillInsuranceReconciliationData(response, rows, startDate, endDate);
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
