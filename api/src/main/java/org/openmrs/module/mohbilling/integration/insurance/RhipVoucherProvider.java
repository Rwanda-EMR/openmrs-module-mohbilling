package org.openmrs.module.mohbilling.integration.insurance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class RhipVoucherProvider {

	protected Log log = LogFactory.getLog(getClass());

	private static final String MMI_INSURANCE_TYPE = "MMI";
	private static final String PRACTITIONER_TYPE_FOREIGN = "FOREIGN";

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int SOCKET_TIMEOUT = 5000;
	private static final int CONNECTION_REQUEST_TIMEOUT = 5000;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
		log.info("RHIP voucher payload: " + payload);
		return executePost(config.getVoucherUrl(), payload);
	}

	private String buildVoucherJson(RhipVoucherRequest request) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", request.getInsuranceType());
		payload.put("facilityFosaId", request.getFacilityFosaId());
		payload.put("patientIdentifier", request.getPatientIdentifier());
		payload.put("procedures", request.getProcedures());
		payload.put("userAccountCode", request.getUserAccountCode());
		payload.put("processedBy", request.getProcessedBy());
		if (isMmiInsuranceType(request.getInsuranceType())) {
			payload.put("notes", request.getNotes());
		}
		payload.put("practitionerLicenseNumber", request.getPractitionerLicenseNumber());
		payload.put("patientType", request.getPatientType());
		payload.put("healthCareStayType", request.getHealthCareStayType());
		payload.put("admissionDate", request.getAdmissionDate());
		payload.put("dischargeDate", request.getDischargeDate());
		payload.put("treatmentForNewBorn", request.getTreatmentForNewBorn());
		payload.put("patientPhoneNumber", request.getPatientPhoneNumber());
		return toJson(payload);
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
		return executePost(config.getPractitionerDetailsUrl(), payload);
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
		return executePost(config.getPractitionerCreateUrl(), payload);
	}

	public RhipVoucherIntegrationConfig getConfig() {
		return config;
	}

	public void setConfig(RhipVoucherIntegrationConfig config) {
		this.config = config;
	}

	private IntegrationResponse executePost(String url, String payload) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(StringUtils.isNotBlank(url));
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Endpoint URL is not configured");
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
			}
		} catch (Exception e) {
			ret.setErrorMessage(e.getMessage());
		}
		return ret;
	}

	private String buildPractitionerDetailsJson(String insuranceType, String licenseNumber) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", insuranceType);
		payload.put("licenseNumber", licenseNumber);
		return toJson(payload);
	}

	private String buildPractitionerCreateJson(String insuranceType, String practitionerType, String documentNumber,
	                                           String documentType, String practitionerLicenseNumber, String facilityFosaId,
	                                           String phoneNumber, String practitionerSubCategoryTypeId, String contractType,
	                                           String firstName, String lastName, String gender, String dateOfBirth) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("insuranceType", insuranceType);
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
