package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ProformaUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.Proforma;
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
import java.util.Date;

/**
 * Searches, views, prints (PDF) and cancels proforma invoices.
 */
public class MohBillingProformaListController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	public static final String PRIVILEGE_VIEW = "Billing Proforma - View";
	public static final String PRIVILEGE_CANCEL = "Billing Proforma - Cancel";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private static final int MAX_RESULTS = 200;

	private BillingService billingService;
	private String detailViewName;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!Context.isAuthenticated()) {
			return new ModelAndView(new RedirectView("/login.htm"));
		}
		if (!Context.hasPrivilege(PRIVILEGE_VIEW)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"You do not have permission to view proforma invoices.");
			return new ModelAndView(new RedirectView("/openmrs"));
		}

		Proforma proforma = null;
		if (StringUtils.isNotBlank(request.getParameter("proformaId"))) {
			try {
				proforma = billingService.getProforma(Integer.valueOf(request.getParameter("proformaId").trim()));
			}
			catch (NumberFormatException ignored) {
				// handled below as not found
			}
			if (proforma == null) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Proforma not found.");
				return new ModelAndView(new RedirectView("proformas.list"));
			}
		}

		if (proforma != null && "POST".equalsIgnoreCase(request.getMethod()) && "cancel".equals(request.getParameter("action"))) {
			if (!Context.hasPrivilege(PRIVILEGE_CANCEL)) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"You do not have permission to cancel proforma invoices.");
			} else {
				try {
					ProformaUtil.cancelProforma(proforma, request.getParameter("cancelReason"));
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
							"Proforma " + proforma.getIdentifier() + " cancelled.");
				}
				catch (APIException e) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
				}
			}
			return new ModelAndView(new RedirectView("proformas.list?proformaId=" + proforma.getProformaId()));
		}

		if (proforma != null && request.getParameter("print") != null) {
			new FileExporter().printProforma(request, response, proforma, proforma.getIdentifier() + ".pdf");
			return null;
		}

		if (proforma != null) {
			ModelAndView mav = new ModelAndView(detailViewName);
			mav.addObject("proforma", proforma);
			return mav;
		}

		return buildListModel(request);
	}

	private ModelAndView buildListModel(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView(getViewName());
		Patient patient = null;
		if (StringUtils.isNotBlank(request.getParameter("patientId"))) {
			try {
				patient = Context.getPatientService().getPatient(Integer.valueOf(request.getParameter("patientId").trim()));
			}
			catch (NumberFormatException ignored) {
				// ignore invalid filter
			}
		}
		Insurance insurance = null;
		if (StringUtils.isNotBlank(request.getParameter("insuranceId"))) {
			try {
				insurance = billingService.getInsurance(Integer.valueOf(request.getParameter("insuranceId").trim()));
			}
			catch (NumberFormatException ignored) {
				// ignore invalid filter
			}
		}
		Date startDate = parseDate(request.getParameter("startDate"));
		Date endDate = parseDate(request.getParameter("endDate"));
		Date endExclusive = null;
		if (endDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			endExclusive = cal.getTime();
		}
		String status = StringUtils.trimToNull(request.getParameter("status"));
		String identifier = StringUtils.trimToNull(request.getParameter("identifier"));

		mav.addObject("proformas", billingService.getProformas(patient, insurance, startDate, endExclusive, status,
				identifier, MAX_RESULTS));
		mav.addObject("maxResults", MAX_RESULTS);
		mav.addObject("insurances", InsuranceUtil.getInsurances(true));
		mav.addObject("patient", patient);
		mav.addObject("insuranceId", insurance != null ? insurance.getInsuranceId() : null);
		mav.addObject("startDate", request.getParameter("startDate"));
		mav.addObject("endDate", request.getParameter("endDate"));
		mav.addObject("status", status);
		mav.addObject("identifier", identifier);
		return mav;
	}

	private Date parseDate(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			SimpleDateFormat df = new SimpleDateFormat(DATE_PATTERN);
			df.setLenient(false);
			return df.parse(value.trim());
		}
		catch (ParseException e) {
			return null;
		}
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public void setDetailViewName(String detailViewName) {
		this.detailViewName = detailViewName;
	}
}
