package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherService;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MohBillingViewGlobalBillController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	private RhipVoucherService voucherService;
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		String globalBillStr = request.getParameter("globalBillId");
		Integer globalBillId = Integer.valueOf(request.getParameter("globalBillId"));
		List<ServiceRevenue> serviceRevenueList = new ArrayList<ServiceRevenue>();
		Consommation consommation =null;

		if(globalBillStr!=null && !globalBillStr.equals("")){
			GlobalBill globalBill= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr));
			PatientIdentifier patientIdentifier= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr)).getAdmission().getInsurancePolicy().getOwner().getPatientIdentifier(3);

			List<Consommation> consommations = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
			List<PatientServiceBill> allItems = new ArrayList<PatientServiceBill>();
			for (Consommation c : consommations) {
				for (PatientServiceBill item : c.getBillItems()) {
					if (!item.getVoided()) {
						allItems.add(item);
					}
				}
			}
			List<HopService> revenuesCategories = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.REVENUE");
			for (HopService hopService : revenuesCategories) {
				if(ReportsUtil.getServiceRevenues(allItems, hopService)!=null)
					serviceRevenueList.add(ReportsUtil.getServiceRevenues(allItems, hopService));

			}
			ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.IMAGING");
			if(imagingRevenue!=null)
				serviceRevenueList.add(imagingRevenue);

			ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.ACTS");
			if(actsRevenue!=null)
				serviceRevenueList.add(actsRevenue);
			ServiceRevenue actsDCPRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.DCPACTS");
			if(actsDCPRevenue!=null)
				serviceRevenueList.add(actsDCPRevenue);

			ServiceRevenue autreRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.AUTRES");
			if(autreRevenue!=null)
				serviceRevenueList.add(autreRevenue);

			mav.addObject("globalBill", globalBill);
			mav.addObject("patientIdentifier",patientIdentifier);
			request.getSession().setAttribute("globalBill" , globalBill);

		}
		mav.addObject("serviceRevenueList", serviceRevenueList);
		request.getSession().setAttribute("serviceRevenueList" , serviceRevenueList);

		if(request.getParameter("print")!=null){
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.valueOf(request.getParameter("globalBillId")));
			FileExporter exp = new FileExporter();
			List<ServiceRevenue> sr = (List<ServiceRevenue>) request.getSession().getAttribute("serviceRevenueList" );

			String finalDiagnosisConceptQuestion=Context.getAdministrationService().getGlobalProperty("billing.finalDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");
			String differentialDiagnosisConceptQuestion=Context.getAdministrationService().getGlobalProperty("billing.differentialDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");

			String finalDiagnosis=GlobalBillUtil.getDiagnosisFromAdmissionToDischarge(finalDiagnosisConceptQuestion,gb.getAdmission().getAdmissionDate()+"",gb.getClosingDate()+"",gb.getAdmission().getInsurancePolicy().getOwner().getPatientId());
			String differentialDiagnosis=GlobalBillUtil.getDiagnosisFromAdmissionToDischarge(differentialDiagnosisConceptQuestion,gb.getAdmission().getAdmissionDate()+"",gb.getClosingDate()+"",gb.getAdmission().getInsurancePolicy().getOwner().getPatientId());

			exp.printGlobalBill(request, response, gb,differentialDiagnosis+"",finalDiagnosis+"",sr, gb.getBillIdentifier()+".pdf");
		}
		if(request.getParameter("revert_global_bill")!=null && Context.isAuthenticated()){
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.parseInt(request.getParameter("globalBillId")));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String closingDate = df.format(gb.getClosingDate());
			String currDate = df.format(new Date());
			if(closingDate.equals(currDate)==true) {
				gb.setClosed(false);
				gb.setClosedBy(null);
				gb.setClosingDate(null);
				gb.setClosingReason(null);
				gb.setEditedBy(Context.getAuthenticatedUser());
				gb.setEditingReason(request.getParameter("editGBill"));
				GlobalBillUtil.saveGlobalBill(gb);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,"Global Bill Reverted, You are allowed to change it");
				return new ModelAndView(new RedirectView("globalBill.list?insurancePolicyId=" + gb.getAdmission().getInsurancePolicy() +
						"&ipCardNumber=" + gb.getAdmission().getInsurancePolicy().getInsuranceCardNo()));
			}
			else {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"Only Globall Bill closed TODAY can be reverted ");
			}
		}
		if (request.getParameter("send_voucher") != null && Context.isAuthenticated()) {
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.parseInt(request.getParameter("globalBillId")));
			if (!Boolean.TRUE.equals(gb.getClosed())) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Global Bill must be discharged before sending a voucher");
			} else if (voucherService == null) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "RHIP voucher service is not configured");
			} else {
				IntegrationResponse voucherResponse = voucherService.submitVoucherForGlobalBill(gb);
				logVoucherResponse(voucherResponse);
				if (voucherResponse == null) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"RHIP voucher submission failed: no response received");
					log.warn("RHIP voucher submission failed: no response received");
				} else if (!voucherResponse.isEnabled()) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"RHIP voucher submission failed: integration is disabled");
					log.warn("RHIP voucher submission failed: integration is disabled");
				} else if (voucherResponse.getResponseCode() == null && voucherResponse.getResponseEntity() == null) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"RHIP voucher submission failed: empty response from RHIP");
					log.warn("RHIP voucher submission failed: empty response from RHIP");
				} else if (voucherResponse.getErrorMessage() != null) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Error submitting RHIP voucher: " + voucherResponse.getErrorMessage());
					log.warn("Error submitting RHIP voucher: " + voucherResponse.getErrorMessage());
				} else if (voucherResponse.getResponseEntity() instanceof java.util.Map) {
					java.util.Map responseBody = (java.util.Map) voucherResponse.getResponseEntity();
					Object success = responseBody.get("success");
					Object message = responseBody.get("message");
					if (Boolean.TRUE.equals(success)) {
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
								message == null ? "RHIP voucher submitted successfully" : message.toString());
						setVoucherReceiptAttributes(request, responseBody);
					} else {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								message == null ? "RHIP voucher submission failed" : message.toString());
						log.warn("RHIP voucher submission failed: " + responseBody);
					}
				} else if (voucherResponse.getResponseEntity() instanceof String) {
					String responseBody = (String) voucherResponse.getResponseEntity();
					VoucherResponseDetails details = parseVoucherResponseDetails(responseBody);
					if (Boolean.TRUE.equals(details.success)) {
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
								details.message == null ? "RHIP voucher submitted successfully" : details.message);
						setVoucherReceiptAttributes(request, details);
					} else {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								details.message == null ? "RHIP voucher submission failed" : details.message);
						log.warn("RHIP voucher submission failed: " + responseBody);
					}
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "RHIP voucher submitted successfully");
				}
			}
			return new ModelAndView(new RedirectView("viewGlobalBill.form?globalBillId=" + gb.getGlobalBillId()));
		}
		return mav;
	}

	public void setVoucherService(RhipVoucherService voucherService) {
		this.voucherService = voucherService;
	}

	private void logVoucherResponse(IntegrationResponse voucherResponse) {
		if (voucherResponse == null) {
			log.warn("RHIP voucher response is null");
			return;
		}
		Object responseEntity = voucherResponse.getResponseEntity();
		if (responseEntity == null) {
			log.info("RHIP voucher response received (status="
					+ voucherResponse.getResponseCode() + ", no body)");
			return;
		}
		if (responseEntity instanceof java.util.Map) {
			java.util.Map responseBody = (java.util.Map) responseEntity;
			Object success = responseBody.get("success");
			Object message = responseBody.get("message");
			Object voucherCode = null;
			Object voucherReferenceNumber = null;
			Object status = null;
			Object data = responseBody.get("data");
			if (data instanceof java.util.Map) {
				java.util.Map dataMap = (java.util.Map) data;
				voucherCode = dataMap.get("voucherCode");
				voucherReferenceNumber = dataMap.get("voucherReferenceNumber");
				status = dataMap.get("status");
			}
			log.info("RHIP voucher response received (status=" + voucherResponse.getResponseCode()
					+ ", success=" + success
					+ ", message=" + message
					+ ", voucherCode=" + voucherCode
					+ ", voucherReferenceNumber=" + voucherReferenceNumber
					+ ", voucherStatus=" + status
					+ ")");
			return;
		}
		if (responseEntity instanceof String) {
			String responseBody = (String) responseEntity;
			VoucherResponseDetails details = parseVoucherResponseDetails(responseBody);
			log.info("RHIP voucher response received (status=" + voucherResponse.getResponseCode()
					+ ", success=" + details.success
					+ ", message=" + details.message
					+ ", voucherCode=" + details.voucherCode
					+ ", voucherReferenceNumber=" + details.voucherReferenceNumber
					+ ", voucherStatus=" + details.status
					+ ")");
			return;
		}
		log.info("RHIP voucher response received (status=" + voucherResponse.getResponseCode()
				+ "): " + responseEntity);
	}

	private void setVoucherReceiptAttributes(HttpServletRequest request, java.util.Map responseBody) {
		if (request == null || responseBody == null) {
			return;
		}
		Object data = responseBody.get("data");
		if (!(data instanceof java.util.Map)) {
			return;
		}
		java.util.Map dataMap = (java.util.Map) data;
		Object voucherCode = dataMap.get("voucherCode");
		Object voucherReferenceNumber = dataMap.get("voucherReferenceNumber");
		Object status = dataMap.get("status");
		if (voucherCode != null) {
			request.getSession().setAttribute("rhipVoucherCode", voucherCode.toString());
		}
		if (voucherReferenceNumber != null) {
			request.getSession().setAttribute("rhipVoucherReferenceNumber", voucherReferenceNumber.toString());
		}
		if (status != null) {
			request.getSession().setAttribute("rhipVoucherStatus", status.toString());
		}
	}

	private void setVoucherReceiptAttributes(HttpServletRequest request, VoucherResponseDetails details) {
		if (request == null || details == null) {
			return;
		}
		if (details.voucherCode != null) {
			request.getSession().setAttribute("rhipVoucherCode", details.voucherCode);
		}
		if (details.voucherReferenceNumber != null) {
			request.getSession().setAttribute("rhipVoucherReferenceNumber", details.voucherReferenceNumber);
		}
		if (details.status != null) {
			request.getSession().setAttribute("rhipVoucherStatus", details.status);
		}
	}

	private VoucherResponseDetails parseVoucherResponseDetails(String responseBody) {
		VoucherResponseDetails details = new VoucherResponseDetails();
		if (responseBody == null) {
			return details;
		}
		String normalized = normalizeJsonString(responseBody);
		details.success = extractJsonBoolean(normalized, "success");
		details.message = extractJsonString(normalized, "message");
		details.voucherCode = extractJsonString(normalized, "voucherCode");
		details.voucherReferenceNumber = extractJsonString(normalized, "voucherReferenceNumber");
		details.status = extractJsonString(normalized, "status");
		return details;
	}

	private Boolean extractJsonBoolean(String responseBody, String key) {
		Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(responseBody);
		if (matcher.find()) {
			return Boolean.valueOf(matcher.group(1));
		}
		return null;
	}

	private String extractJsonString(String responseBody, String key) {
		Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
		Matcher matcher = pattern.matcher(responseBody);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private String normalizeJsonString(String responseBody) {
		String normalized = responseBody.trim();
		if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
			normalized = normalized.substring(1, normalized.length() - 1);
		}
		normalized = normalized.replace("\\\"", "\"");
		normalized = normalized.replace("\\\\", "\\");
		return normalized;
	}

	private static class VoucherResponseDetails {
		private Boolean success;
		private String message;
		private String voucherCode;
		private String voucherReferenceNumber;
		private String status;
	}
}
