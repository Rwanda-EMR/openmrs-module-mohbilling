package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.RhipIntegrationLog;
import org.openmrs.module.mohbilling.model.RhipIntegrationLogSearchCriteria;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MohBillingRhipIntegrationLogListController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	private static final int DEFAULT_PAGE_SIZE = 50;
	private static final int MAX_PAGE_SIZE = 200;
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private BillingService billingService;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!Context.isAuthenticated()) {
			return new ModelAndView(new RedirectView("/login.htm"));
		}

		User user = Context.getAuthenticatedUser();
		if (user == null || !user.isSuperUser()) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Only system administrator users can access RHIP integration logs.");
			return new ModelAndView(new RedirectView("billingAdmin.form"));
		}

		int page = parsePositiveInteger(request.getParameter("page"), 1);
		int pageSize = parsePositiveInteger(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE);
		if (pageSize > MAX_PAGE_SIZE) {
			pageSize = MAX_PAGE_SIZE;
		}

		RhipIntegrationLogSearchCriteria criteria = buildCriteria(request);
		int totalCount = billingService == null ? 0 : billingService.countRhipIntegrationLogs(criteria);
		int totalPages = totalCount == 0 ? 1 : (int) Math.ceil((double) totalCount / pageSize);
		if (page > totalPages) {
			page = totalPages;
		}
		int firstResult = (page - 1) * pageSize;

		List<RhipIntegrationLog> logs = billingService == null
				? Collections.<RhipIntegrationLog>emptyList()
				: billingService.getRhipIntegrationLogs(criteria, firstResult, pageSize);

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		mav.addObject("logs", logs);
		mav.addObject("page", page);
		mav.addObject("pageSize", pageSize);
		mav.addObject("totalCount", totalCount);
		mav.addObject("totalPages", totalPages);
		mav.addObject("hasPreviousPage", page > 1);
		mav.addObject("hasNextPage", page < totalPages);
		mav.addObject("previousPage", page - 1);
		mav.addObject("nextPage", page + 1);
		mav.addObject("startDate", safeParameter(request, "startDate"));
		mav.addObject("endDate", safeParameter(request, "endDate"));
		mav.addObject("senderUsername", safeParameter(request, "senderUsername"));
		mav.addObject("operationType", safeParameter(request, "operationType"));
		mav.addObject("responseStatus", safeParameter(request, "responseStatus"));
		mav.addObject("responseCode", safeParameter(request, "responseCode"));
		mav.addObject("query", safeParameter(request, "query"));
		mav.addObject("filterQueryString", buildFilterQueryString(request, pageSize));
		return mav;
	}

	private RhipIntegrationLogSearchCriteria buildCriteria(HttpServletRequest request) {
		RhipIntegrationLogSearchCriteria criteria = new RhipIntegrationLogSearchCriteria();
		criteria.setStartDate(parseDate(request.getParameter("startDate"), false));
		criteria.setEndDate(parseDate(request.getParameter("endDate"), true));
		criteria.setSenderUsername(trimToNull(request.getParameter("senderUsername")));
		criteria.setOperationType(trimToNull(request.getParameter("operationType")));
		criteria.setResponseStatus(trimToNull(request.getParameter("responseStatus")));
		criteria.setResponseCode(parseInteger(request.getParameter("responseCode")));
		criteria.setQuery(trimToNull(request.getParameter("query")));
		return criteria;
	}

	private int parsePositiveInteger(String value, int defaultValue) {
		Integer parsed = parseInteger(value);
		return parsed == null || parsed <= 0 ? defaultValue : parsed;
	}

	private Integer parseInteger(String value) {
		if (value == null || value.trim().length() == 0) {
			return null;
		}
		try {
			return Integer.valueOf(value.trim());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private Date parseDate(String value, boolean endOfDay) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return null;
		}
		try {
			Date date = new SimpleDateFormat(DATE_PATTERN).parse(trimmed);
			if (!endOfDay) {
				return date;
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 999);
			return calendar.getTime();
		}
		catch (ParseException e) {
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

	private String safeParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		return value == null ? "" : value.trim();
	}

	private String buildFilterQueryString(HttpServletRequest request, int pageSize) {
		StringBuilder ret = new StringBuilder();
		appendQueryParam(ret, "pageSize", String.valueOf(pageSize));
		appendQueryParam(ret, "startDate", request.getParameter("startDate"));
		appendQueryParam(ret, "endDate", request.getParameter("endDate"));
		appendQueryParam(ret, "senderUsername", request.getParameter("senderUsername"));
		appendQueryParam(ret, "operationType", request.getParameter("operationType"));
		appendQueryParam(ret, "responseStatus", request.getParameter("responseStatus"));
		appendQueryParam(ret, "responseCode", request.getParameter("responseCode"));
		appendQueryParam(ret, "query", request.getParameter("query"));
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
		builder.append(name).append("=").append(urlEncode(trimmed));
	}

	private String urlEncode(String value) {
		try {
			return java.net.URLEncoder.encode(value, "UTF-8");
		}
		catch (Exception e) {
			return value;
		}
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}
}
