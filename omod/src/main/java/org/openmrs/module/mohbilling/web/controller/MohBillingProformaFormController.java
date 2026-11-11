package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ProformaUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.Proforma;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates proforma invoices: pick patient and insurance, then add services priced with that
 * insurance's tariff.
 */
public class MohBillingProformaFormController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	public static final String PRIVILEGE_CREATE = "Billing Proforma - Create";

	private BillingService billingService;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!Context.isAuthenticated()) {
			return new ModelAndView(new RedirectView("/login.htm"));
		}
		if (!Context.hasPrivilege(PRIVILEGE_CREATE)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"You do not have permission to create proforma invoices.");
			return new ModelAndView(new RedirectView("proformas.list"));
		}

		Patient patient = getPatient(request.getParameter("patientId"));
		Insurance insurance = getInsurance(request.getParameter("insuranceId"));
		Department department = getDepartment(request.getParameter("departmentId"));

		if ("POST".equalsIgnoreCase(request.getMethod()) && request.getParameter("save") != null) {
			try {
				Proforma proforma = ProformaUtil.createProforma(patient, insurance, department,
						request.getParameter("addressee"), request.getParameter("notes"), readQuantities(request));
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						"Proforma " + proforma.getIdentifier() + " created.");
				return new ModelAndView(new RedirectView("proformas.list?proformaId=" + proforma.getProformaId()));
			}
			catch (APIException e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			}
			catch (Exception e) {
				log.error("Unable to create proforma", e);
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"Unable to create proforma: " + e.getMessage());
			}
		}

		ModelAndView mav = new ModelAndView(getViewName());
		mav.addObject("patient", patient);
		mav.addObject("insurance", insurance);
		mav.addObject("department", department);
		mav.addObject("addressee", request.getParameter("addressee"));
		mav.addObject("notes", request.getParameter("notes"));
		mav.addObject("insurances", InsuranceUtil.getInsurances(true));
		mav.addObject("departments", DepartementUtil.getAllHospitalDepartements());
		if (insurance != null) {
			InsuranceRate rate = insurance.getRateOnDate(new Date());
			mav.addObject("insuranceRate", rate != null ? rate.getRate() : null);
			mav.addObject("flatFee", rate != null ? rate.getFlatFee() : null);
			mav.addObject("services", ProformaUtil.getActiveBillableServices(insurance, new Date()));
		}
		return mav;
	}

	/** Reads parallel serviceId / quantity request arrays, summing duplicates. */
	private Map<Integer, BigDecimal> readQuantities(HttpServletRequest request) {
		Map<Integer, BigDecimal> quantities = new LinkedHashMap<Integer, BigDecimal>();
		String[] serviceIds = request.getParameterValues("serviceId");
		String[] qtys = request.getParameterValues("quantity");
		if (serviceIds == null || qtys == null) {
			return quantities;
		}
		for (int i = 0; i < serviceIds.length && i < qtys.length; i++) {
			try {
				Integer serviceId = Integer.valueOf(serviceIds[i].trim());
				BigDecimal qty = new BigDecimal(qtys[i].trim());
				BigDecimal existing = quantities.get(serviceId);
				quantities.put(serviceId, existing == null ? qty : existing.add(qty));
			}
			catch (NumberFormatException e) {
				throw new APIException("Invalid quantity: " + qtys[i]);
			}
		}
		return quantities;
	}

	private Patient getPatient(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		try {
			return Context.getPatientService().getPatient(Integer.valueOf(id.trim()));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private Insurance getInsurance(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		try {
			return billingService.getInsurance(Integer.valueOf(id.trim()));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	private Department getDepartment(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		try {
			return billingService.getDepartement(Integer.valueOf(id.trim()));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}
}
