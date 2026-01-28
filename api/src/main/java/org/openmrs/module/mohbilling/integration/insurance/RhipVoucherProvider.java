package org.openmrs.module.mohbilling.integration.insurance;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;

import java.math.BigDecimal;
import java.util.List;

public class RhipVoucherProvider {

	protected Log log = LogFactory.getLog(getClass());

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int SOCKET_TIMEOUT = 5000;
	private static final int CONNECTION_REQUEST_TIMEOUT = 5000;

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
		log.info("RHIP voucher payload: " + payload);
		return executePost(config.getVoucherUrl(), payload);
	}

	public IntegrationResponse getPractitionerDetails(String insuranceType, String licenseNumber) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && config.isPractitionerIntegrationEnabled());
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Practitioner integration is not configured");
			return ret;
		}
		return executePost(config.getPractitionerDetailsUrl(), buildPractitionerDetailsJson(insuranceType, licenseNumber));
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
			httpPost.setEntity(new StringEntity(payload));
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

	private String buildVoucherJson(RhipVoucherRequest request) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		boolean first = true;
		first = appendJsonField(builder, "insuranceType", jsonString(request.getInsuranceType()), first);
		first = appendJsonField(builder, "facilityFosaId", jsonString(request.getFacilityFosaId()), first);
		first = appendJsonField(builder, "patientIdentifier", jsonString(request.getPatientIdentifier()), first);
		first = appendJsonField(builder, "procedures", proceduresJson(request.getProcedures()), first);
		first = appendJsonField(builder, "userAccountCode", jsonString(request.getUserAccountCode()), first);
		first = appendJsonField(builder, "processedBy", jsonString(request.getProcessedBy()), first);
		first = appendJsonField(builder, "notes", jsonString(request.getNotes()), first);
		first = appendJsonField(builder, "practitionerLicenseNumber",
				jsonString(request.getPractitionerLicenseNumber()), first);
		first = appendJsonField(builder, "patientType", jsonString(request.getPatientType()), first);
		first = appendJsonField(builder, "healthCareStayType", jsonString(request.getHealthCareStayType()), first);
		first = appendJsonField(builder, "admissionDate", jsonString(request.getAdmissionDate()), first);
		first = appendJsonField(builder, "dischargeDate", jsonString(request.getDischargeDate()), first);
		first = appendJsonField(builder, "treatmentForNewBorn", booleanJson(request.getTreatmentForNewBorn()), first);
		appendJsonField(builder, "patientPhoneNumber", jsonString(request.getPatientPhoneNumber()), first);
		builder.append("}");
		return builder.toString();
	}

	private String buildPractitionerDetailsJson(String insuranceType, String licenseNumber) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		boolean first = true;
		first = appendJsonField(builder, "insuranceType", jsonString(insuranceType), first);
		appendJsonField(builder, "licenseNumber", jsonString(licenseNumber), first);
		builder.append("}");
		return builder.toString();
	}

	private String buildPractitionerCreateJson(String insuranceType, String practitionerType, String documentNumber,
	                                           String documentType, String practitionerLicenseNumber, String facilityFosaId,
	                                           String phoneNumber, String practitionerSubCategoryTypeId, String contractType,
	                                           String firstName, String lastName, String gender, String dateOfBirth) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		boolean first = true;
		first = appendJsonField(builder, "insuranceType", jsonString(insuranceType), first);
		first = appendJsonField(builder, "practitionerType", jsonString(practitionerType), first);
		first = appendJsonField(builder, "documentNumber", jsonString(documentNumber), first);
		first = appendJsonField(builder, "documentType", jsonString(documentType), first);
		first = appendJsonField(builder, "practitionerLicenseNumber", jsonString(practitionerLicenseNumber), first);
		first = appendJsonField(builder, "facilityFosaId", jsonString(facilityFosaId), first);
		first = appendJsonField(builder, "phoneNumber", jsonString(phoneNumber), first);
		first = appendJsonField(builder, "practitionerSubCategoryTypeId", jsonString(practitionerSubCategoryTypeId), first);
		first = appendJsonField(builder, "contractType", jsonString(contractType), first);
		first = appendJsonField(builder, "firstName", jsonString(firstName), first);
		first = appendJsonField(builder, "lastName", jsonString(lastName), first);
		first = appendJsonField(builder, "gender", jsonString(gender), first);
		appendJsonField(builder, "dateOfBirth", jsonString(dateOfBirth), first);
		builder.append("}");
		return builder.toString();
	}

	private boolean appendJsonField(StringBuilder builder, String name, String value, boolean first) {
		if (!first) {
			builder.append(",");
		}
		builder.append("\"").append(escapeJson(name)).append("\":").append(value);
		return false;
	}

	private String proceduresJson(List<RhipVoucherProcedure> procedures) {
		if (procedures == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		boolean first = true;
		for (RhipVoucherProcedure procedure : procedures) {
			if (!first) {
				builder.append(",");
			}
			first = false;
			builder.append("{");
			boolean innerFirst = true;
			innerFirst = appendJsonField(builder, "code", jsonString(procedure == null ? null : procedure.getCode()), innerFirst);
			innerFirst = appendJsonField(builder, "quantity", numberJson(procedure == null ? null : procedure.getQuantity()), innerFirst);
			innerFirst = appendJsonField(builder, "prescribedAt", jsonString(procedure == null ? null : procedure.getPrescribedAt()), innerFirst);
			appendJsonField(builder, "price", numberJson(procedure == null ? null : procedure.getPrice()), innerFirst);
			builder.append("}");
		}
		builder.append("]");
		return builder.toString();
	}

	private String numberJson(BigDecimal value) {
		return value == null ? "null" : value.toString();
	}

	private String booleanJson(Boolean value) {
		return value == null ? "null" : value.toString();
	}

	private String jsonString(String value) {
		if (value == null) {
			return "null";
		}
		return "\"" + escapeJson(value) + "\"";
	}

	private String escapeJson(String value) {
		StringBuilder escaped = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
				case '"':
					escaped.append("\\\"");
					break;
				case '\\':
					escaped.append("\\\\");
					break;
				case '\b':
					escaped.append("\\b");
					break;
				case '\f':
					escaped.append("\\f");
					break;
				case '\n':
					escaped.append("\\n");
					break;
				case '\r':
					escaped.append("\\r");
					break;
				case '\t':
					escaped.append("\\t");
					break;
				default:
					if (ch < 0x20) {
						String hex = Integer.toHexString(ch);
						escaped.append("\\u");
						for (int pad = hex.length(); pad < 4; pad++) {
							escaped.append("0");
						}
						escaped.append(hex);
					} else {
						escaped.append(ch);
					}
			}
		}
		return escaped.toString();
	}
}
