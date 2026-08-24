package org.openmrs.module.mohbilling.integration.insurance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.model.RhipIntegrationLog;
import org.openmrs.module.mohbilling.service.BillingService;

import javax.net.ssl.SSLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RhipVoucherProvider {

	protected Log log = LogFactory.getLog(getClass());

	private static final String MMI_INSURANCE_TYPE = "MMI";
	private static final String RAMA_INSURANCE_TYPE = "rama";
	private static final String SPECIAL_CASE_INSURANCE_TYPE = "special_case";
	private static final String DEFAULT_RAMA_PRESCRIPTION_DESTINATION = "FACILITY_DISPENSE";
	private static final String PRACTITIONER_TYPE_FOREIGN = "FOREIGN";

	// RHIP endpoints can be slow (especially immediately after practitioner creation); keep these generous.
	private static final int CONNECT_TIMEOUT = 15000;
	private static final int SOCKET_TIMEOUT = 30000;
	private static final int CONNECTION_REQUEST_TIMEOUT = 15000;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String REDACTED = "***";

	private RhipVoucherIntegrationConfig config;

	public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && config.isVoucherEnabled());
		if (!ret.isEnabled()) {
			return ret;
		}
		if (request == null) {
			ret.setErrorMessage("No voucher request provided");
			return ret;
		}
		String payload = buildVoucherJson(request);
		if (payload == null) {
			ret.setErrorMessage("Unable to serialize RHIP voucher request to JSON");
			return ret;
		}
		if (StringUtils.isBlank(payload)) {
			log.warn("RHIP voucher payload is empty");
		}
		log.info("RHIP voucher payload: " + redactSensitivePayload(payload));
		return executePost(config.getVoucherUrl(), payload, "VOUCHER_SUBMIT", request.getProcessedBy());
	}

	private String buildVoucherJson(RhipVoucherRequest request) {
		if (isRamaInsuranceType(request.getInsuranceType())) {
			return buildRamaVoucherJson(request);
		}
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", normalizeInsuranceTypeForRhip(request.getInsuranceType()));
		payload.put("facilityFosaId", request.getFacilityFosaId());
		payload.put("patientIdentifier", request.getPatientIdentifier());
		payload.put("procedures", request.getProcedures());
		payload.put("userAccountCode", request.getUserAccountCode());
		payload.put("processedBy", request.getProcessedBy());
		if (isMmiInsuranceType(request.getInsuranceType())) {
			payload.put("notes", request.getNotes());
		}
		payload.put("practitionerLicenseNumber", request.getPractitionerLicenseNumber());
		if (!isRamaInsuranceType(request.getInsuranceType())) {
			payload.put("patientType", request.getPatientType());
		}
		payload.put("healthCareStayType", request.getHealthCareStayType());
		payload.put("admissionDate", request.getAdmissionDate());
		payload.put("dischargeDate", request.getDischargeDate());
		payload.put("treatmentForNewBorn", request.getTreatmentForNewBorn());
		payload.put("diagnosisIds", request.getDiagnosisIds());
		payload.put("referralFacilityId", request.getReferralFacilityId());
		payload.put("patientPhoneNumber", request.getPatientPhoneNumber());
		payload.put("prescriptionDestination", resolvePrescriptionDestination(request));
		payload.put("visitReferenceNumber", resolveVisitReferenceNumber(request));
		return toJson(payload);
	}

	private String buildRamaVoucherJson(RhipVoucherRequest request) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", normalizeInsuranceTypeForRhip(request.getInsuranceType()));
		payload.put("facilityFosaId", request.getFacilityFosaId());
		payload.put("patientIdentifier", request.getPatientIdentifier());
		payload.put("procedures", buildRamaProcedures(request.getProcedures()));
		payload.put("practitionerLicenseNumber", request.getPractitionerLicenseNumber());
		payload.put("healthCareStayType", request.getHealthCareStayType());
		payload.put("admissionDate", request.getAdmissionDate());
		payload.put("diagnosisIds", request.getDiagnosisIds());
		payload.put("referralFacilityId", request.getReferralFacilityId());
		payload.put("patientPhoneNumber", request.getPatientPhoneNumber());
		payload.put("prescriptionDestination", resolvePrescriptionDestination(request));
		payload.put("visitReferenceNumber", request.getVisitReferenceNumber());
		return toJson(payload);
	}

	private List<Map<String, Object>> buildRamaProcedures(List<RhipVoucherProcedure> procedures) {
		List<Map<String, Object>> ramaProcedures = new ArrayList<>();
		if (procedures == null) {
			return ramaProcedures;
		}
		for (RhipVoucherProcedure procedure : procedures) {
			if (procedure == null) {
				continue;
			}
			Map<String, Object> ramaProcedure = new LinkedHashMap<>();
			ramaProcedure.put("code", procedure.getCode());
			ramaProcedure.put("quantity", procedure.getQuantity());
			ramaProcedure.put("prescribedAt", procedure.getPrescribedAt());
			if (StringUtils.isNotBlank(procedure.getPosology())) {
				ramaProcedure.put("posology", procedure.getPosology().trim());
			}
			if (StringUtils.isNotBlank(procedure.getFrequency())) {
				ramaProcedure.put("frequency", procedure.getFrequency().trim());
			}
			if (procedure.getDurationDays() != null) {
				ramaProcedure.put("durationDays", procedure.getDurationDays());
			}
			if (StringUtils.isNotBlank(procedure.getDispensingDate())) {
				ramaProcedure.put("dispensingDate", procedure.getDispensingDate().trim());
			}
			ramaProcedures.add(ramaProcedure);
		}
		return ramaProcedures;
	}

	private String resolvePrescriptionDestination(RhipVoucherRequest request) {
		if (request == null) {
			return null;
		}
		if (StringUtils.isNotBlank(request.getPrescriptionDestination())) {
			return request.getPrescriptionDestination().trim();
		}
		if (RAMA_INSURANCE_TYPE.equals(normalizeInsuranceTypeForRhip(request.getInsuranceType()))) {
			return DEFAULT_RAMA_PRESCRIPTION_DESTINATION;
		}
		return null;
	}

	private boolean isRamaInsuranceType(String insuranceType) {
		return RAMA_INSURANCE_TYPE.equals(normalizeInsuranceTypeForRhip(insuranceType));
	}

	private String resolveVisitReferenceNumber(RhipVoucherRequest request) {
		if (request == null) {
			return null;
		}
		if (StringUtils.isNotBlank(request.getVisitReferenceNumber())) {
			return request.getVisitReferenceNumber().trim();
		}
		if (isMmiInsuranceType(request.getInsuranceType())) {
			return StringUtils.trimToNull(request.getReceptionNumber());
		}
		return null;
	}

	public IntegrationResponse getPractitionerDetails(String insuranceType, String licenseNumber) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && config.isPractitionerIntegrationEnabled());
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Practitioner integration is not configured");
			return ret;
		}
		String payload = buildPractitionerDetailsJson(insuranceType, licenseNumber);
		if (payload == null) {
			ret.setErrorMessage("Unable to serialize RHIP practitioner details request to JSON");
			return ret;
		}
		log.info("RHIP practitioner details payload: " + redactSensitivePayload(payload));
		return executePost(config.getPractitionerDetailsUrl(), payload, "PRACTITIONER_DETAILS", null);
	}

	public IntegrationResponse createPractitioner(String insuranceType, String practitionerType, String documentNumber,
	                                             String documentType, String practitionerLicenseNumber, String facilityFosaId,
	                                             String phoneNumber, String practitionerSubCategoryTypeId, String contractType,
	                                             String firstName, String lastName, String gender, String dateOfBirth) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && config.isPractitionerIntegrationEnabled());
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Practitioner integration is not configured");
			return ret;
		}
		String payload = buildPractitionerCreateJson(insuranceType, practitionerType, documentNumber, documentType,
				practitionerLicenseNumber, facilityFosaId, phoneNumber, practitionerSubCategoryTypeId, contractType,
				firstName, lastName, gender, dateOfBirth);
		if (payload == null) {
			ret.setErrorMessage("Unable to serialize RHIP practitioner registration request to JSON");
			return ret;
		}
		log.info("RHIP practitioner create payload: " + redactSensitivePayload(payload));
		return executePost(config.getPractitionerCreateUrl(), payload, "PRACTITIONER_CREATE", null);
	}

	public IntegrationResponse getPractitionerTypes(String insuranceType, String categoryId) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && StringUtils.isNotBlank(config.getPractitionerTypesUrl()));
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Practitioner types endpoint is not configured");
			return ret;
		}
		Map<String, Object> payload = new LinkedHashMap<>();
		// RHIP types endpoint expects lower-cased insuranceType (e.g. "cbhi") in some environments.
		payload.put("insuranceType", normalizeInsuranceTypeForRhip(insuranceType));
		// categoryId is optional; when not filtering, RHIP accepts an empty string.
		payload.put("categoryId", categoryId == null ? "" : categoryId);
		String json = toJson(payload);
		if (json == null) {
			ret.setErrorMessage("Unable to serialize RHIP practitioner types request to JSON");
			return ret;
		}
		return executePost(config.getPractitionerTypesUrl(), json, "PRACTITIONER_TYPES", null);
	}

	public RhipVoucherIntegrationConfig getConfig() {
		return config;
	}

	public void setConfig(RhipVoucherIntegrationConfig config) {
		this.config = config;
	}

	private IntegrationResponse executePost(String url, String payload, String operationType, String senderUsername) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(StringUtils.isNotBlank(url));
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Endpoint URL is not configured");
			persistRhipIntegrationLog(url, operationType, senderUsername, payload, ret);
			return ret;
		}
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setConnectTimeout(CONNECT_TIMEOUT)
						.setSocketTimeout(SOCKET_TIMEOUT)
						.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
						.build())
				.build()) {
			HttpPost httpPost = new HttpPost(url);
			log.debug("POSTING to " + url);
			httpPost.setHeader("Content-Type", "application/json");
			String apiKey = config == null ? null : config.getVoucherApiKey();
			if (StringUtils.isNotBlank(apiKey)) {
				httpPost.setHeader("x-api-key", apiKey);
			}
			String apiOrigin = config == null ? null : config.getVoucherApiOrigin();
			if (StringUtils.isNotBlank(apiOrigin)) {
				httpPost.setHeader("Origin", apiOrigin);
			}
			httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
			ret.setEndpointAccessible(false);
			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				ret.setEndpointAccessible(true);
				ret.setResponseCode(response.getStatusLine().getStatusCode());
				HttpEntity entity = response.getEntity();
				String data = "";
				try {
					data = EntityUtils.toString(entity);
				} catch (Exception ignored) {
				}
				if (StringUtils.isNotBlank(data)) {
					ret.setResponseEntity(data);
				}
				if (ret.getResponseCode() != null && ret.getResponseCode() >= 400) {
					String bodySnippet = StringUtils.isBlank(data) ? ""
					        : (": " + truncate(redactSensitivePayload(data), 500));
					ret.setErrorMessage("RHIP returned HTTP " + ret.getResponseCode() + bodySnippet);
				}
			}
		} catch (Exception e) {
			log.warn("Error POSTING to " + url, e);
			ret.setErrorMessage(toUserFriendlyError(e, url));
		} finally {
			persistRhipIntegrationLog(url, operationType, senderUsername, payload, ret);
		}
		return ret;
	}

	private void persistRhipIntegrationLog(String url, String operationType, String senderUsername,
	                                       String requestPayload, IntegrationResponse response) {
		try {
			BillingService billingService = Context.getService(BillingService.class);
			if (billingService == null) {
				return;
			}
			User currentUser = null;
			try {
				currentUser = Context.getAuthenticatedUser();
			} catch (Exception ignored) {
			}
			RhipIntegrationLog logEntry = new RhipIntegrationLog();
			logEntry.setDateCreated(new Date());
			logEntry.setCreator(currentUser);
			logEntry.setSenderUsername(resolveSenderUsername(senderUsername, currentUser));
			logEntry.setOperationType(operationType);
			logEntry.setEndpointUrl(url);
			logEntry.setRequestPayload(requestPayload);
			logEntry.setResponseCode(response == null ? null : response.getResponseCode());
			logEntry.setResponseStatus(resolveResponseStatus(response));
			logEntry.setResponseBody(toEntityString(response == null ? null : response.getResponseEntity()));
			logEntry.setErrorMessage(response == null ? "No response" : response.getErrorMessage());
			logEntry.setUuid(UUID.randomUUID().toString());
			billingService.saveRhipIntegrationLog(logEntry);
		} catch (Exception e) {
			log.warn("Unable to persist RHIP integration log entry", e);
		}
	}

	private String resolveSenderUsername(String senderUsername, User currentUser) {
		if (StringUtils.isNotBlank(senderUsername)) {
			return senderUsername.trim();
		}
		return currentUser == null ? null : currentUser.getUsername();
	}

	private String resolveResponseStatus(IntegrationResponse response) {
		if (response == null) {
			return "NO_RESPONSE";
		}
		if (StringUtils.isNotBlank(response.getErrorMessage())) {
			return "ERROR";
		}
		Integer code = response.getResponseCode();
		if (code == null) {
			return "UNKNOWN";
		}
		return code >= 200 && code < 300 ? "SUCCESS" : "HTTP_" + code;
	}

	private String toEntityString(Object entity) {
		if (entity == null) {
			return null;
		}
		return entity.toString();
	}

	private String toUserFriendlyError(Exception e, String url) {
		String target = StringUtils.isBlank(url) ? "RHIP" : url.trim();
		if (e instanceof ConnectTimeoutException) {
			return "Timed out connecting to RHIP (" + target + ").";
		}
		if (e instanceof SocketTimeoutException) {
			return "Timed out waiting for RHIP response (" + target + ").";
		}
		if (e instanceof UnknownHostException) {
			return "Cannot resolve RHIP host (" + target + "). Check DNS/network.";
		}
		if (e instanceof HttpHostConnectException) {
			return "Cannot connect to RHIP (" + target + "). Connection refused or blocked.";
		}
		if (e instanceof SSLException) {
			return "SSL/TLS error when connecting to RHIP (" + target + ").";
		}
		String message = e.getMessage();
		if (StringUtils.isBlank(message)) {
			return "Unexpected error calling RHIP (" + target + "): " + e.getClass().getSimpleName();
		}
		return "Error calling RHIP (" + target + "): " + message;
	}

	private String truncate(String text, int maxChars) {
		if (text == null) {
			return null;
		}
		if (text.length() <= maxChars) {
			return text;
		}
		return text.substring(0, maxChars) + "...";
	}

	private String redactSensitivePayload(String payload) {
		if (StringUtils.isBlank(payload)) {
			return payload;
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(payload);
			redactNode(root);
			return OBJECT_MAPPER.writeValueAsString(root);
		}
		catch (Exception ignored) {
			// Fallback when payload is not JSON.
			return payload;
		}
	}

	private void redactNode(JsonNode node) {
		if (node == null) {
			return;
		}
		if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> entry = fields.next();
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				if (isSensitiveKey(key) && node instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
					((com.fasterxml.jackson.databind.node.ObjectNode) node).put(key, REDACTED);
				} else {
					redactNode(value);
				}
			}
			return;
		}
		if (node.isArray()) {
			for (JsonNode item : node) {
				redactNode(item);
			}
		}
	}

	private boolean isSensitiveKey(String key) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		String k = key.trim().toLowerCase();
		return "patientidentifier".equals(k)
		        || "patientphonenumber".equals(k)
		        || "phonenumber".equals(k)
		        || "practitionerlicensenumber".equals(k)
		        || "documentnumber".equals(k)
		        || "useraccountcode".equals(k)
		        || "firstname".equals(k)
		        || "lastname".equals(k)
		        || "dateofbirth".equals(k);
	}

	private String buildPractitionerDetailsJson(String insuranceType, String licenseNumber) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", normalizeInsuranceTypeForRhip(insuranceType));
		payload.put("licenseNumber", licenseNumber);
		return toJson(payload);
	}

	private String buildPractitionerCreateJson(String insuranceType, String practitionerType, String documentNumber,
	                                           String documentType, String practitionerLicenseNumber, String facilityFosaId,
	                                           String phoneNumber, String practitionerSubCategoryTypeId, String contractType,
	                                           String firstName, String lastName, String gender, String dateOfBirth) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", normalizeInsuranceTypeForRhip(insuranceType));
		payload.put("practitionerType", practitionerType);
		payload.put("documentNumber", documentNumber);
		payload.put("documentType", documentType);
		payload.put("practitionerLicenseNumber", practitionerLicenseNumber);
		payload.put("facilityFosaId", facilityFosaId);
		payload.put("phoneNumber", phoneNumber);
		payload.put("practitionerSubCategoryTypeId", practitionerSubCategoryTypeId);
		payload.put("contractType", contractType);
		if (isForeignPractitionerType(practitionerType)) {
			payload.put("firstName", firstName);
			payload.put("lastName", lastName);
			payload.put("gender", gender);
			payload.put("dateOfBirth", dateOfBirth);
		}
		return toJson(payload);
	}

	private boolean isMmiInsuranceType(String insuranceType) {
		return StringUtils.isNotBlank(insuranceType) && MMI_INSURANCE_TYPE.equalsIgnoreCase(insuranceType.trim());
	}

	private String normalizeInsuranceTypeForRhip(String insuranceType) {
		if (StringUtils.isBlank(insuranceType)) {
			return null;
		}
		String normalized = insuranceType.trim();
		if ("MUTUELLE".equalsIgnoreCase(normalized)) {
			return "cbhi";
		}
		if ("SPECIAL CASE".equalsIgnoreCase(normalized) || "SPECIAL_CASE".equalsIgnoreCase(normalized)) {
			return SPECIAL_CASE_INSURANCE_TYPE;
		}
		return normalized.toLowerCase();
	}

	private boolean isForeignPractitionerType(String practitionerType) {
		return StringUtils.isNotBlank(practitionerType) && PRACTITIONER_TYPE_FOREIGN.equalsIgnoreCase(practitionerType.trim());
	}

	private String toJson(Object value) {
		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.warn("Unable to serialize payload to JSON", e);
			return null;
		}
	}
}
