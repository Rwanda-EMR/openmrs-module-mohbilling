package org.openmrs.module.mohbilling.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
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

public class MohBillingViewGlobalBillController extends
			ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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
					processVoucherResponse(request, gb, voucherResponse.getResponseEntity(), voucherResponse.getResponseCode());
				} else if (voucherResponse.getResponseEntity() instanceof String) {
					processVoucherResponse(request, gb, voucherResponse.getResponseEntity(), voucherResponse.getResponseCode());
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

	private void processVoucherResponse(HttpServletRequest request, GlobalBill globalBill, Object responseEntity, Integer responseCode) {
		String responseBody = toJsonString(responseEntity);
		JsonNode root = toJsonNode(responseEntity);

		String success = jsonValueString(root == null ? null : root.get("success"));
		String message = jsonValueString(root == null ? null : root.get("message"));

		JsonNode data = root == null ? null : root.get("data");
		String voucherCode = jsonValueString(data == null ? null : data.get("voucherCode"));
		String voucherReferenceNumber = jsonValueString(data == null ? null : data.get("voucherReferenceNumber"));
		String status = jsonValueString(data == null ? null : data.get("status"));

		log.info("RHIP voucher response received (status=" + responseCode
				+ ", success=" + success
				+ ", message=" + message
				+ ", voucherCode=" + voucherCode
				+ ", voucherReferenceNumber=" + voucherReferenceNumber
				+ ", voucherStatus=" + status
				+ ")");
		if ("true".equalsIgnoreCase(success)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					StringUtils.isBlank(message) ? "RHIP voucher submitted successfully" : message);
			log.info("RHIP voucher submitted successfully: " + responseBody);
			if (voucherCode != null) {
				request.getSession().setAttribute("rhipVoucherCode", voucherCode);
			}
			if (voucherReferenceNumber != null) {
				request.getSession().setAttribute("rhipVoucherReferenceNumber", voucherReferenceNumber);
			}
			if (status != null) {
				request.getSession().setAttribute("rhipVoucherStatus", status);
			}
			if (globalBill != null && (voucherCode != null || voucherReferenceNumber != null)) {
				globalBill.setRhipVoucherCode(voucherCode);
				globalBill.setRhipVoucherReferenceNumber(voucherReferenceNumber);
				try {
					GlobalBillUtil.saveGlobalBill(globalBill);
				} catch (Exception e) {
					log.warn("Unable to persist RHIP voucher details for global bill " + globalBill.getGlobalBillId(), e);
				}
			}
		} else {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					StringUtils.isBlank(message) ? "RHIP voucher submission failed" : message);
			log.warn("RHIP voucher submission failed: " + responseBody);
		}
	}

	private JsonNode toJsonNode(Object responseEntity) {
		if (responseEntity == null) {
			return null;
		}
		if (responseEntity instanceof JsonNode) {
			return (JsonNode) responseEntity;
		}
		if (responseEntity instanceof String) {
			String body = ((String) responseEntity).trim();
			try {
				JsonNode node = OBJECT_MAPPER.readTree(body);
				if (node != null && node.isTextual()) {
					String inner = node.asText();
					try {
						return OBJECT_MAPPER.readTree(inner);
					} catch (Exception ignored) {
						return node;
					}
				}
				return node;
			} catch (Exception ignored) {
			}

			String normalized = body;
			if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
				normalized = normalized.substring(1, normalized.length() - 1);
			}
			normalized = normalized.replace("\\\"", "\"").replace("\\\\", "\\");
			try {
				return OBJECT_MAPPER.readTree(normalized);
			} catch (Exception ignored) {
				return null;
			}
		}
		try {
			return OBJECT_MAPPER.valueToTree(responseEntity);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private String jsonValueString(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isTextual()) {
			return node.asText();
		}
		if (node.isBoolean()) {
			return String.valueOf(node.booleanValue());
		}
		if (node.isNumber()) {
			return node.numberValue().toString();
		}
		return node.toString();
	}

	private String toJsonString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof JsonNode) {
			return value.toString();
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			return String.valueOf(value);
		}
	}
}
