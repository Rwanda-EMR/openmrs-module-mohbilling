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
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherIntegrationConfig;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MohBillingViewGlobalBillController extends
			ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private RhipVoucherService voucherService;
	private RhipVoucherIntegrationConfig voucherIntegrationConfig;
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
			mav.addObject("showRhipVoucherButton", shouldShowRhipVoucherButton(globalBill));
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

			Date diagnosisStartDate = getStartOfDay(gb.getAdmission().getAdmissionDate());
			Date diagnosisEndDate = getEndOfDay(gb.getClosingDate() == null ? new Date() : gb.getClosingDate());
			Integer patientId = gb.getAdmission().getInsurancePolicy().getOwner().getPatientId();

			String finalDiagnosis=GlobalBillUtil.getDiagnosisFromAdmissionToDischarge(finalDiagnosisConceptQuestion,diagnosisStartDate,diagnosisEndDate,patientId);
			String differentialDiagnosis=GlobalBillUtil.getDiagnosisFromAdmissionToDischarge(differentialDiagnosisConceptQuestion,diagnosisStartDate,diagnosisEndDate,patientId);

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
			} else if (StringUtils.isNotBlank(gb.getRhipVoucherCode()) || StringUtils.isNotBlank(gb.getRhipVoucherReferenceNumber())) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "RHIP voucher already exists for this global bill");
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
					} else if (voucherResponse.getErrorMessage() != null) {
						String friendlyError = toFriendlyVoucherError(voucherResponse);
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, friendlyError);
						log.warn("Error submitting RHIP voucher: " + friendlyError);
					} else if (voucherResponse.getResponseCode() == null && voucherResponse.getResponseEntity() == null) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"RHIP voucher submission failed: empty response from RHIP");
						log.warn("RHIP voucher submission failed: empty response from RHIP");
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

	public void setVoucherService(RhipVoucherService voucherService) {
		this.voucherService = voucherService;
	}

	public void setVoucherIntegrationConfig(RhipVoucherIntegrationConfig voucherIntegrationConfig) {
		this.voucherIntegrationConfig = voucherIntegrationConfig;
	}

	private boolean shouldShowRhipVoucherButton(GlobalBill globalBill) {
		if (globalBill == null || !Boolean.TRUE.equals(globalBill.getClosed())) {
			return false;
		}
		if (StringUtils.isNotBlank(globalBill.getRhipVoucherCode())
				|| StringUtils.isNotBlank(globalBill.getRhipVoucherReferenceNumber())) {
			return false;
		}
		Admission admission = globalBill.getAdmission();
		InsurancePolicy insurancePolicy = admission == null ? null : admission.getInsurancePolicy();
		Insurance insurance = insurancePolicy == null ? null : insurancePolicy.getInsurance();
		if (insurance == null) {
			return false;
		}
		RhipVoucherIntegrationConfig config = voucherIntegrationConfig == null
				? new RhipVoucherIntegrationConfig()
				: voucherIntegrationConfig;
		return config.isVoucherButtonEnabledForInsurance(insurance.getCategory(), insurance.getName());
	}

	private void processVoucherResponse(HttpServletRequest request, GlobalBill globalBill, Object responseEntity, Integer responseCode) {
		String responseBody = toJsonString(responseEntity);
		JsonNode root = toJsonNode(responseEntity);

		String success = jsonValueString(root == null ? null : root.get("success"));
		String message = jsonValueString(root == null ? null : root.get("message"));
		String failureMessage = extractRhipFailureMessage(root);

		JsonNode data = root == null ? null : root.get("data");
		String voucherCode = jsonValueString(data == null ? null : data.get("voucherCode"));
		String voucherReferenceNumber = jsonValueString(data == null ? null : data.get("voucherReferenceNumber"));
		String status = jsonValueString(data == null ? null : data.get("status"));

		log.info("RHIP voucher response received (status=" + responseCode
				+ ", success=" + success
				+ ", message=" + message
				+ ", failureMessage=" + failureMessage
				+ ", voucherCode=" + voucherCode
				+ ", voucherReferenceNumber=" + voucherReferenceNumber
				+ ", voucherStatus=" + status
				+ ")");
		if ("true".equalsIgnoreCase(success)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					StringUtils.isBlank(message) ? "RHIP voucher submitted successfully" : message);
			log.info("RHIP voucher submitted successfully");
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
					StringUtils.isBlank(failureMessage) ? "RHIP voucher submission failed" : failureMessage);
			log.warn("RHIP voucher submission failed"
					+ (StringUtils.isBlank(failureMessage) ? "" : ": " + failureMessage));
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

	private String toFriendlyVoucherError(IntegrationResponse response) {
		String message = extractRhipMessage(response);
		if (StringUtils.isBlank(message)) {
			String raw = response == null ? null : response.getErrorMessage();
			return StringUtils.isBlank(raw) ? "Unable to submit RHIP voucher. Please try again." : "Error submitting RHIP voucher: " + raw;
		}
		String normalized = message.toLowerCase();
		if (normalized.contains("at least one diagnosis id")
				|| normalized.contains("icd-11")
				|| normalized.contains("diagnosis")) {
			return "Unable to submit RHIP voucher: at least one ICD-11 diagnosis code is required. "
					+ "Please enter/select a diagnosis with code format like XM1QR3 and try again.";
		}
		if (normalized.contains("treatmentfornewborn")) {
			return "Unable to submit RHIP voucher: treatment for newborn must be set to Yes or No.";
		}
		if (normalized.contains("receptionnumber")) {
			return "Unable to submit RHIP voucher: MMI reception number is required. "
					+ "Please create MMI patient reception first, then try again.";
		}
		return "Unable to submit RHIP voucher: " + message;
	}

	private String extractRhipMessage(IntegrationResponse response) {
		if (response == null) {
			return null;
		}
		String fromEntity = extractMessageFromJsonString(toJsonString(response.getResponseEntity()));
		if (StringUtils.isNotBlank(fromEntity)) {
			return fromEntity;
		}
		String errorMessage = response.getErrorMessage();
		if (StringUtils.isBlank(errorMessage)) {
			return null;
		}
		int firstBrace = errorMessage.indexOf('{');
		if (firstBrace >= 0) {
			String jsonPart = errorMessage.substring(firstBrace);
			String fromErrorJson = extractMessageFromJsonString(jsonPart);
			if (StringUtils.isNotBlank(fromErrorJson)) {
				return fromErrorJson;
			}
		}
		return errorMessage;
	}

	private String extractMessageFromJsonString(String json) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		try {
			JsonNode node = OBJECT_MAPPER.readTree(json);
			return extractRhipFailureMessage(node);
		}
		catch (Exception ignored) {
			return null;
		}
	}

	private String extractRhipFailureMessage(JsonNode root) {
		String detailedErrors = extractRhipErrorMessages(root == null ? null : root.get("errors"));
		if (StringUtils.isNotBlank(detailedErrors)) {
			return detailedErrors;
		}
		return jsonValueString(root == null ? null : root.get("message"));
	}

	private String extractRhipErrorMessages(JsonNode errorsNode) {
		if (errorsNode == null || errorsNode.isNull()) {
			return null;
		}
		List<String> messages = new ArrayList<String>();
		if (errorsNode.isArray()) {
			for (JsonNode errorNode : errorsNode) {
				String message = null;
				if (errorNode != null && errorNode.isObject()) {
					message = jsonValueString(errorNode.get("message"));
				} else {
					message = jsonValueString(errorNode);
				}
				message = normalizeRhipErrorMessage(message);
				if (StringUtils.isNotBlank(message) && !messages.contains(message)) {
					messages.add(message);
				}
			}
		} else {
			String message = normalizeRhipErrorMessage(jsonValueString(errorsNode));
			if (StringUtils.isNotBlank(message)) {
				messages.add(message);
			}
		}
		return messages.isEmpty() ? null : StringUtils.join(messages, "; ");
	}

	private String normalizeRhipErrorMessage(String message) {
		if (StringUtils.isBlank(message)) {
			return null;
		}
		String normalized = message.trim();
		if (normalized.startsWith("[") || normalized.startsWith("\"") || normalized.startsWith("'")) {
			try {
				JsonNode parsed = OBJECT_MAPPER.readTree(normalized);
				if (parsed != null && parsed.isArray()) {
					List<String> values = new ArrayList<String>();
					for (JsonNode value : parsed) {
						String parsedValue = normalizeRhipErrorMessage(jsonValueString(value));
						if (StringUtils.isNotBlank(parsedValue)) {
							values.add(parsedValue);
						}
					}
					if (!values.isEmpty()) {
						return StringUtils.join(values, "; ");
					}
				}
				if (parsed != null && parsed.isTextual()) {
					normalized = parsed.asText();
				}
			} catch (Exception ignored) {
			}
		}
		normalized = normalized.trim();
		while (normalized.startsWith("[") || normalized.startsWith("\"") || normalized.startsWith("'")) {
			normalized = normalized.substring(1).trim();
		}
		while (normalized.endsWith("]") || normalized.endsWith("\"") || normalized.endsWith("'")) {
			normalized = normalized.substring(0, normalized.length() - 1).trim();
		}
		return normalized;
	}
}
