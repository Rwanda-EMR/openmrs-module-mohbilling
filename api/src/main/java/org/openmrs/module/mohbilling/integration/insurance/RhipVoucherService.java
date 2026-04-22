package org.openmrs.module.mohbilling.integration.insurance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.User;
import org.openmrs.PersonName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.metadata.RhipPractitionerConceptMetadata;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.service.RhipPractitionerTypeService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RhipVoucherService {

	private static final Log log = LogFactory.getLog(RhipVoucherService.class);
	private static final String CBHI_INSURANCE_TYPE = "CBHI";
	private static final String MMI_INSURANCE_TYPE = "MMI";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String PRACTITIONER_TYPE_LOCAL = "LOCAL";
	private static final String PRACTITIONER_TYPE_FOREIGN = "FOREIGN";
	private static final Pattern SUCCESS_PATTERN =
			Pattern.compile("\"success\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
	private static final Pattern UUID_PATTERN = Pattern.compile(
			"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	private static final Pattern ICD_CODE_PATTERN = Pattern.compile("^(?=.*\\d)[A-Za-z0-9.]+$");
	private static final Pattern ICD_CODE_WITH_NAME_PATTERN =
			Pattern.compile("([A-Za-z0-9.]+)\\s*-");
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private RhipVoucherProvider voucherProvider;
	private RhipVoucherIntegrationConfig config;
	private BillingService billingService;
	private ProviderService providerService;
	private PersonService personService;
	private ConceptService conceptService;
	private final Map<String, String> practitionerSubCategoryNameToIdCache = new ConcurrentHashMap<>();
	private RhipPractitionerTypeService rhipPractitionerTypeService;

	public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
		if (request != null && !isSupportedVoucherInsuranceType(request.getInsuranceType())) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("insuranceType must be CBHI, MUTUELLE, or MMI");
			return ret;
		}
		IntegrationResponse validation = validateVoucherRequest(request);
		if (validation != null) {
			return validation;
		}
		if (voucherProvider == null) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("RHIP voucher provider is not configured");
			return ret;
		}
		return voucherProvider.submitVoucher(request);
	}

	public IntegrationResponse submitVoucherForGlobalBill(GlobalBill globalBill) {
		return submitVoucherForGlobalBill(globalBill, null);
	}

	public IntegrationResponse submitVoucherForGlobalBill(GlobalBill globalBill, String eligibilityIdentifier) {
		if (hasExistingVoucherIdentifiers(globalBill)) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("RHIP voucher already exists for this global bill");
			return ret;
		}
		RhipVoucherRequest request = buildVoucherRequestFromGlobalBill(globalBill, eligibilityIdentifier);
		if (request == null) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("Unable to build voucher request from global bill");
			return ret;
		}
		IntegrationResponse validation = validateVoucherRequest(request);
		if (validation != null) {
			return validation;
		}
		if (!isMmiInsuranceType(request.getInsuranceType())) {
			IntegrationResponse practitionerCheck = ensurePractitionerRegistered(request, resolveProcessedBy(globalBill));
			if (practitionerCheck != null && practitionerCheck.getErrorMessage() != null) {
				return practitionerCheck;
			}
		}
		return submitVoucher(request);
	}

	public RhipVoucherRequest buildVoucherRequestFromGlobalBill(GlobalBill globalBill) {
		return buildVoucherRequestFromGlobalBill(globalBill, null);
	}

	public RhipVoucherRequest buildVoucherRequestFromGlobalBill(GlobalBill globalBill, String eligibilityIdentifier) {
		if (globalBill == null) {
			log.warn("No global bill provided for voucher submission");
			return null;
		}
		Insurance insurance = globalBill.getInsurance();
		if (!isSupportedVoucherInsurance(insurance)) {
			log.debug("Skipping voucher submission for unsupported insurance");
			return null;
		}
		Admission admission = globalBill.getAdmission();
		InsurancePolicy policy = admission == null ? null : admission.getInsurancePolicy();
		Patient patient = policy == null ? null : policy.getOwner();
		if (patient == null) {
			log.warn("Unable to resolve patient from global bill {}");
			return null;
		}

		RhipVoucherRequest request = new RhipVoucherRequest();
		String normalizedInsuranceType = normalizeVoucherInsuranceType(insurance);
		request.setInsuranceType(normalizedInsuranceType);
		String fosaId = resolveFosaId(globalBill);
		request.setFacilityFosaId(fosaId);
		request.setPatientIdentifier(resolvePatientIdentifier(policy, patient, insurance, fosaId, eligibilityIdentifier));
		if (isMmiInsuranceType(normalizedInsuranceType)) {
			request.setReceptionNumber(resolveMmiReceptionNumber(request.getPatientIdentifier(), patient));
		}
		request.setProcedures(buildProcedures(globalBill, admission));

		User processedBy = resolveProcessedBy(globalBill);
		String providerLicenseNumber = resolveProviderLicense(processedBy);
		request.setUserAccountCode(providerLicenseNumber);
		request.setPractitionerLicenseNumber(providerLicenseNumber);
		request.setProcessedBy(processedBy == null ? null : processedBy.getUsername());

		String diagnosisNotes = resolveDiagnosisNotes(patient, admission, globalBill);
		request.setNotes(diagnosisNotes);
		request.setDiagnosisIds(resolveDiagnosisIds(diagnosisNotes));
		if (request.getDiagnosisIds() == null || request.getDiagnosisIds().isEmpty()) {
			log.warn("No ICD-11 diagnosis codes resolved for RHIP voucher request; globalBillId="
					+ (globalBill == null ? null : globalBill.getGlobalBillId()));
		}
		request.setPatientType(resolvePatientType(admission));
		request.setHealthCareStayType(resolveHealthCareStayType(admission));
		request.setAdmissionDate(formatDate(admission == null ? null : admission.getAdmissionDate()));
		request.setDischargeDate(formatDate(resolveDischargeDate(globalBill, admission)));
		if (!isMmiInsuranceType(normalizedInsuranceType)) {
			request.setTreatmentForNewBorn(resolveTreatmentForNewBorn());
		}
		request.setPatientPhoneNumber(resolvePatientPhoneNumber(patient, globalBill));

		return request;
	}

	private boolean isCbhiInsurance(Insurance insurance) {
		if (insurance == null) {
			return false;
		}
		if (isCbhiInsuranceType(insurance.getCategory())) {
			return true;
		}
		return isCbhiInsuranceType(insurance.getName());
	}

	private boolean isCbhiInsuranceType(String type) {
		if (StringUtils.isBlank(type)) {
			return false;
		}
		String normalized = type.trim();
		return CBHI_INSURANCE_TYPE.equalsIgnoreCase(normalized) || "MUTUELLE".equalsIgnoreCase(normalized);
	}

	private boolean isMmiInsuranceType(String type) {
		return StringUtils.isNotBlank(type) && MMI_INSURANCE_TYPE.equalsIgnoreCase(type.trim());
	}

	private boolean isSupportedVoucherInsuranceType(String type) {
		return isCbhiInsuranceType(type) || isMmiInsuranceType(type);
	}

	private boolean isSupportedVoucherInsurance(Insurance insurance) {
		return isCbhiInsurance(insurance) || isMmiInsurance(insurance);
	}

	private String normalizeVoucherInsuranceType(Insurance insurance) {
		return isMmiInsurance(insurance) ? MMI_INSURANCE_TYPE : CBHI_INSURANCE_TYPE;
	}

	private boolean hasExistingVoucherIdentifiers(GlobalBill globalBill) {
		if (globalBill == null) {
			return false;
		}
		return StringUtils.isNotBlank(globalBill.getRhipVoucherCode())
		        || StringUtils.isNotBlank(globalBill.getRhipVoucherReferenceNumber());
	}

	private IntegrationResponse validateVoucherRequest(RhipVoucherRequest request) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && config.isVoucherEnabled());
		if (request == null) {
			ret.setErrorMessage("No voucher request provided");
			return ret;
		}
		List<String> errors = new ArrayList<>();
		if (!isSupportedVoucherInsuranceType(request.getInsuranceType())) {
			errors.add("insuranceType must be CBHI, MUTUELLE, or MMI");
		}
		if (StringUtils.isBlank(request.getFacilityFosaId())) {
			errors.add("facilityFosaId is required");
		}
		if (StringUtils.isBlank(request.getPatientIdentifier())) {
			errors.add("patientIdentifier is required");
		}
		if (!isMmiInsuranceType(request.getInsuranceType()) && StringUtils.isBlank(request.getPractitionerLicenseNumber())) {
			errors.add("practitionerLicenseNumber is required");
		}
		if (!isMmiInsuranceType(request.getInsuranceType()) && request.getTreatmentForNewBorn() == null) {
			errors.add("treatmentForNewBorn must be explicitly true or false");
		}
		if (!isMmiInsuranceType(request.getInsuranceType())
				&& (request.getDiagnosisIds() == null || request.getDiagnosisIds().isEmpty())) {
			errors.add("diagnosisIds is required and must contain at least one ICD-11 code");
		}
		if (isMmiInsuranceType(request.getInsuranceType()) && StringUtils.isBlank(request.getReceptionNumber())) {
			errors.add("receptionNumber is required for MMI voucher");
		}
		List<RhipVoucherProcedure> procedures = request.getProcedures();
		if (procedures == null || procedures.isEmpty()) {
			errors.add("At least one procedure is required");
		} else {
			for (int i = 0; i < procedures.size(); i++) {
				RhipVoucherProcedure procedure = procedures.get(i);
				int oneBased = i + 1;
				if (procedure == null) {
					errors.add("procedure #" + oneBased + " is null");
					continue;
				}
				if (StringUtils.isBlank(procedure.getCode())) {
					errors.add("procedure #" + oneBased + " code is required");
				}
				if (procedure.getQuantity() == null || procedure.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
					errors.add("procedure #" + oneBased + " quantity must be greater than 0");
				}
				if (procedure.getPrice() == null || procedure.getPrice().compareTo(BigDecimal.ZERO) < 0) {
					errors.add("procedure #" + oneBased + " price must be 0 or greater");
				}
			}
		}
		if (errors.isEmpty()) {
			return null;
		}
		ret.setErrorMessage("Invalid RHIP voucher request: " + StringUtils.join(errors, "; "));
		return ret;
	}

	private User resolveProcessedBy(GlobalBill globalBill) {
		if (globalBill.getClosedBy() != null) {
			return globalBill.getClosedBy();
		}
		return globalBill.getCreator();
	}

	private String resolveProviderLicense(User user) {
		if (user == null || providerService == null || config == null) {
			return null;
		}
		String attributeTypeUuid = config.getProviderLicenseAttributeTypeUuid();
		return resolveProviderAttributeValue(user, attributeTypeUuid);
	}

	private String resolveProviderAttributeValue(User user, String attributeTypeUuid) {
		if (StringUtils.isBlank(attributeTypeUuid)) {
			return null;
		}
			ProviderAttributeType attributeType = providerService.getProviderAttributeTypeByUuid(attributeTypeUuid);
			if (attributeType == null) {
				log.warn("Unable to find provider attribute type with uuid " + attributeTypeUuid);
				return null;
			}
		for (Provider provider : providerService.getProvidersByPerson(user.getPerson(), false)) {
			for (ProviderAttribute attribute : provider.getAttributes()) {
				if (attribute != null && attributeType.equals(attribute.getAttributeType())) {
					String value = attribute.getValueReference();
					if (StringUtils.isBlank(value) && attribute.getValue() != null) {
						value = attribute.getValue().toString();
					}
					if (StringUtils.isNotBlank(value)) {
						return value;
					}
				}
			}
		}
		return null;
	}

	private IntegrationResponse ensurePractitionerRegistered(RhipVoucherRequest request, User processedBy) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && config.isPractitionerIntegrationEnabled());
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Practitioner integration is not configured");
			return ret;
		}
		if (voucherProvider == null) {
			ret.setErrorMessage("RHIP voucher provider is not configured");
			return ret;
		}
		String insuranceType = normalizeInsuranceType(request == null ? null : request.getInsuranceType());
		String licenseNumber = request == null ? null : request.getPractitionerLicenseNumber();
		if (StringUtils.isBlank(insuranceType) || StringUtils.isBlank(licenseNumber)) {
			ret.setErrorMessage("Practitioner license number or insurance type is missing");
			return ret;
		}
		IntegrationResponse details = voucherProvider.getPractitionerDetails(insuranceType, licenseNumber);
		if (Boolean.TRUE.equals(isSuccessResponse(details))) {
			return ret;
		}
		log.info("RHIP practitioner not found (or not accessible); attempting registration for licenseNumber=" + licenseNumber
				+ ", insuranceType=" + insuranceType);
		PractitionerRegistration registration = buildPractitionerRegistration(insuranceType, licenseNumber, request, processedBy);
		if (registration.errorMessage != null) {
			log.warn("Unable to build RHIP practitioner registration payload: " + registration.errorMessage);
			ret.setErrorMessage(registration.errorMessage);
			return ret;
		}
		log.info("RHIP practitioner registration resolved fields: practitionerType=" + registration.practitionerType
				+ ", documentType=" + registration.documentType
				+ ", contractType=" + registration.contractType
				+ ", practitionerSubCategoryTypeId=" + registration.practitionerSubCategoryTypeId
				+ ", facilityFosaId=" + registration.facilityFosaId
				+ ", phoneNumber=" + (StringUtils.isBlank(registration.phoneNumber) ? "<missing>" : "<present>"));
		IntegrationResponse createResponse = voucherProvider.createPractitioner(
				insuranceType,
				registration.practitionerType,
				registration.documentNumber,
				registration.documentType,
				licenseNumber,
				registration.facilityFosaId,
				registration.phoneNumber,
				registration.practitionerSubCategoryTypeId,
				registration.contractType,
				registration.firstName,
				registration.lastName,
				registration.gender,
				registration.dateOfBirth);
		if (!Boolean.TRUE.equals(isSuccessResponse(createResponse))) {
			ret.setErrorMessage("Unable to register practitioner in RHIP");
		}
		return ret;
	}

	private Boolean isSuccessResponse(IntegrationResponse response) {
		if (response == null) {
			return null;
		}
		Object responseEntity = response.getResponseEntity();
		if (responseEntity == null) {
			return null;
		}
		String body = responseEntity.toString();
		try {
			JsonNode root = OBJECT_MAPPER.readTree(body);
			JsonNode success = root == null ? null : root.get("success");
			if (success != null && !success.isNull()) {
				if (success.isBoolean()) {
					return success.booleanValue();
				}
				if (success.isTextual()) {
					return Boolean.valueOf(success.asText());
				}
				if (success.isNumber()) {
					return success.asInt() != 0;
				}
			}
		} catch (Exception ignored) {
		}
		Matcher matcher = SUCCESS_PATTERN.matcher(body);
		if (matcher.find()) {
			return Boolean.valueOf(matcher.group(1));
		}
		return null;
	}

	private PractitionerRegistration buildPractitionerRegistration(String insuranceType, String licenseNumber,
	                                                               RhipVoucherRequest request, User processedBy) {
		PractitionerRegistration registration = new PractitionerRegistration();
		registration.practitionerType = resolvePractitionerType(processedBy);
		registration.documentType = resolvePractitionerDocumentType(processedBy);
		registration.contractType = resolvePractitionerContractType(processedBy);
		registration.practitionerSubCategoryTypeId = resolvePractitionerSubCategoryTypeId(insuranceType, processedBy);
		registration.documentNumber = resolvePractitionerDocumentNumber(processedBy, licenseNumber);
		registration.facilityFosaId = resolvePractitionerFosaId(request);
		registration.phoneNumber = resolvePractitionerPhoneNumber(processedBy);
		if (isForeignPractitionerType(registration.practitionerType)) {
			registration.firstName = resolvePractitionerFirstName(processedBy);
			registration.lastName = resolvePractitionerLastName(processedBy);
			registration.gender = resolvePractitionerGender(processedBy);
			registration.dateOfBirth = resolvePractitionerBirthDate(processedBy);
		}
		if (StringUtils.isBlank(registration.practitionerSubCategoryTypeId)) {
			registration.errorMessage = "Practitioner sub-category type id is missing or cannot be mapped";
		} else if (StringUtils.isBlank(registration.practitionerType)) {
			registration.errorMessage = "Practitioner type is missing";
		} else if (StringUtils.isBlank(registration.documentType)) {
			registration.errorMessage = "Practitioner document type is missing";
		} else if (StringUtils.isBlank(registration.contractType)) {
			registration.errorMessage = "Practitioner contract type is missing";
		} else if (StringUtils.isBlank(registration.documentNumber)) {
			registration.errorMessage = "Practitioner document number is missing";
		} else if (StringUtils.isBlank(registration.facilityFosaId)) {
			registration.errorMessage = "FOSA ID is missing for practitioner registration";
		} else if (StringUtils.isBlank(registration.phoneNumber)) {
			registration.errorMessage = "Practitioner phone number is missing";
		} else if (isForeignPractitionerType(registration.practitionerType)
				&& (StringUtils.isBlank(registration.firstName) || StringUtils.isBlank(registration.lastName))) {
			registration.errorMessage = "Practitioner name is missing";
		} else if (isForeignPractitionerType(registration.practitionerType) && StringUtils.isBlank(registration.gender)) {
			registration.errorMessage = "Practitioner gender is missing";
		} else if (isForeignPractitionerType(registration.practitionerType) && StringUtils.isBlank(registration.dateOfBirth)) {
			registration.errorMessage = "Practitioner date of birth is missing";
		}
		return registration;
	}

	private String resolvePractitionerDocumentNumber(User user, String fallback) {
		String value = resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerDocumentNumberProviderAttributeTypeUuid());
		return StringUtils.isBlank(value) ? fallback : value;
	}

	private String resolvePractitionerPhoneNumber(User user) {
		String value = resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerPhoneProviderAttributeTypeUuid());
		return StringUtils.isBlank(value) ? null : value;
	}

	private String resolvePractitionerType(User user) {
		String value = resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerTypeProviderAttributeTypeUuid());
		return defaultIfBlank(value, PRACTITIONER_TYPE_LOCAL);
	}

	private String resolvePractitionerDocumentType(User user) {
		String value = resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerDocumentTypeProviderAttributeTypeUuid());
		return defaultIfBlank(value, "NID");
	}

	private String resolvePractitionerContractType(User user) {
		String value = resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerContractTypeProviderAttributeTypeUuid());
		return defaultIfBlank(value, "FULL_TIME");
	}

	private String resolvePractitionerSubCategoryTypeId(String insuranceType, User user) {
		String value = resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerSubCategoryTypeIdProviderAttributeTypeUuid());
		if (StringUtils.isBlank(value)) {
			return null;
		}
		String trimmed = value.trim();
		if (looksLikeUuid(trimmed)) {
			return trimmed;
		}
		String mapped = mapPractitionerSubCategoryNameToId(insuranceType, trimmed);
		return StringUtils.isBlank(mapped) ? null : mapped;
	}

	private String mapPractitionerSubCategoryNameToId(String insuranceType, String name) {
		if (StringUtils.isBlank(name) || voucherProvider == null) {
			return null;
		}
		String key = name.trim().toLowerCase();
		String cached = practitionerSubCategoryNameToIdCache.get(key);
		if (StringUtils.isNotBlank(cached)) {
			return cached;
		}
		IntegrationResponse response = voucherProvider.getPractitionerTypes(insuranceType, null);
		if (!Boolean.TRUE.equals(isSuccessResponse(response))) {
			return findSubCategoryIdFromLocalStore(name);
		}
		Object entity = response.getResponseEntity();
		if (entity == null) {
			return findSubCategoryIdFromLocalStore(name);
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(entity.toString());
			JsonNode data = root == null ? null : root.get("data");
			if (data == null || !data.isArray()) {
				return null;
			}
			for (JsonNode item : data) {
				if (item == null || item.isNull()) {
					continue;
				}
				upsertLocalPractitionerTypeFromJson(item);
				String itemName = jsonText(item.get("name"));
				String itemId = jsonText(item.get("id"));
				if (StringUtils.isBlank(itemName) || StringUtils.isBlank(itemId)) {
					continue;
				}
				practitionerSubCategoryNameToIdCache.putIfAbsent(itemName.trim().toLowerCase(), itemId.trim());
			}
			String resolved = practitionerSubCategoryNameToIdCache.get(key);
			if (StringUtils.isNotBlank(resolved)) {
				return resolved;
			}
			return findSubCategoryIdFromLocalStore(name);
		} catch (Exception ignored) {
			return findSubCategoryIdFromLocalStore(name);
		}
	}

	private String findSubCategoryIdFromLocalStore(String name) {
		if (rhipPractitionerTypeService == null || StringUtils.isBlank(name)) {
			return null;
		}
		try {
			String resolved = rhipPractitionerTypeService.findSubCategoryRhipIdByName(name.trim());
			if (StringUtils.isNotBlank(resolved)) {
				practitionerSubCategoryNameToIdCache.putIfAbsent(name.trim().toLowerCase(), resolved.trim());
				return resolved.trim();
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private String jsonText(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isTextual()) {
			return node.asText();
		}
		return node.toString();
	}

	private boolean isForeignPractitionerType(String practitionerType) {
		return StringUtils.isNotBlank(practitionerType)
				&& PRACTITIONER_TYPE_FOREIGN.equalsIgnoreCase(practitionerType.trim());
	}

	private String resolvePersonAttributeValue(User user, String attributeTypeUuid) {
		if (user == null || personService == null || StringUtils.isBlank(attributeTypeUuid)) {
			return null;
		}
		if (user.getPerson() == null) {
			return null;
		}
		PersonAttributeType attributeType = personService.getPersonAttributeTypeByUuid(attributeTypeUuid);
		if (attributeType == null) {
			return null;
		}
		PersonAttribute attribute = user.getPerson().getAttribute(attributeType);
		if (attribute == null || attribute.getValue() == null) {
			return null;
		}
		String value = attribute.getValue().toString();
		return StringUtils.isBlank(value) ? null : value;
	}

	private String resolvePractitionerFosaId(RhipVoucherRequest request) {
		String fosaId = request == null ? null : request.getFacilityFosaId();
		if (StringUtils.isNotBlank(fosaId)) {
			return fosaId;
		}
		return config == null ? null : config.getDefaultFosaId();
	}

	private String resolvePractitionerFirstName(User user) {
		PersonName name = user == null || user.getPerson() == null ? null : user.getPerson().getPersonName();
		if (name != null && StringUtils.isNotBlank(name.getGivenName())) {
			return name.getGivenName();
		}
		return user == null ? null : user.getUsername();
	}

	private String resolvePractitionerLastName(User user) {
		PersonName name = user == null || user.getPerson() == null ? null : user.getPerson().getPersonName();
		if (name != null && StringUtils.isNotBlank(name.getFamilyName())) {
			return name.getFamilyName();
		}
		return null;
	}

	private String resolvePractitionerGender(User user) {
		String gender = user == null || user.getPerson() == null ? null : user.getPerson().getGender();
		if (StringUtils.isBlank(gender)) {
			return null;
		}
		String normalized = gender.trim();
		if ("M".equalsIgnoreCase(normalized) || "MALE".equalsIgnoreCase(normalized)) {
			return "MALE";
		}
		if ("F".equalsIgnoreCase(normalized) || "FEMALE".equalsIgnoreCase(normalized)) {
			return "FEMALE";
		}
		return null;
	}

	private String resolvePractitionerBirthDate(User user) {
		Date birthdate = user == null || user.getPerson() == null ? null : user.getPerson().getBirthdate();
		return birthdate == null ? null : formatDate(birthdate);
	}

	private String normalizeInsuranceType(String insuranceType) {
		if (StringUtils.isBlank(insuranceType)) {
			return null;
		}
		return insuranceType.trim();
	}

	private String defaultIfBlank(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value;
	}

	private boolean looksLikeUuid(String value) {
		return StringUtils.isNotBlank(value) && UUID_PATTERN.matcher(value.trim()).matches();
	}

	private static class PractitionerRegistration {
		private String practitionerType;
		private String documentNumber;
		private String documentType;
		private String contractType;
		private String practitionerSubCategoryTypeId;
		private String facilityFosaId;
		private String phoneNumber;
		private String firstName;
		private String lastName;
		private String gender;
		private String dateOfBirth;
		private String errorMessage;
	}

	private String resolvePatientIdentifier(InsurancePolicy policy, Patient patient, Insurance insurance, String fosaId,
	                                        String eligibilityIdentifier) {
		if (isMmiInsurance(insurance)) {
			if (policy != null && StringUtils.isNotBlank(policy.getInsuranceCardNo())) {
				return policy.getInsuranceCardNo();
			}
			if (StringUtils.isNotBlank(eligibilityIdentifier)) {
				return eligibilityIdentifier.trim();
			}
		}
		if (policy != null && StringUtils.isNotBlank(policy.getRhipPatientId())) {
			return policy.getRhipPatientId();
		}
		if (StringUtils.isNotBlank(eligibilityIdentifier)) {
			String trimmedIdentifier = eligibilityIdentifier.trim();
			storeRhipPatientId(policy, trimmedIdentifier);
			return trimmedIdentifier;
		}
		if (policy != null && StringUtils.isNotBlank(policy.getInsuranceCardNo())) {
			return policy.getInsuranceCardNo();
		}
		if (patient == null || conceptService == null) {
			return null;
		}
		String insuranceNumberConceptRef = Context.getAdministrationService().getGlobalProperty("registration.insuranceNumberConcept");
		if (StringUtils.isBlank(insuranceNumberConceptRef)) {
			log.warn("registration.insuranceNumberConcept global property is not configured");
			return null;
		}
		Concept insuranceNumberConcept = getConcept(insuranceNumberConceptRef);
		if (insuranceNumberConcept == null) {
			log.warn("registration.insuranceNumberConcept not found: " + insuranceNumberConceptRef);
			return null;
		}
		List<Obs> latestInsuranceObs = Utils.getLastNObservations(1, patient, insuranceNumberConcept, false);
		if (latestInsuranceObs.isEmpty()) {
			return null;
		}
		String insuranceCardNumber = latestInsuranceObs.get(0).getValueText();
		if (StringUtils.isBlank(insuranceCardNumber)) {
			return null;
		}
		return insuranceCardNumber;
	}

	@SuppressWarnings("unchecked")
	private String resolveMmiReceptionNumber(String patientIdentifier, Patient patient) {
		String identifier = StringUtils.trimToNull(patientIdentifier);
		if (identifier == null) {
			return null;
		}
		String escapedIdentifier = identifier.replace("'", "''");
		try {
			List<List<Object>> rows = Context.getAdministrationService().executeSQL(
					"select reception_number from mmi_patient_reception_log " +
							"where status = 'SUCCESS' and patient_identifier = '" + escapedIdentifier + "' " +
							"and reception_number is not null and reception_number <> '' " +
							"order by date_created desc limit 1",
					true
			);
			if (rows != null && !rows.isEmpty() && rows.get(0) != null && !rows.get(0).isEmpty() && rows.get(0).get(0) != null) {
				return StringUtils.trimToNull(String.valueOf(rows.get(0).get(0)));
			}
		}
		catch (Exception e) {
			log.warn("Unable to resolve MMI reception number by patient identifier", e);
		}
		if (patient != null) {
			try {
				List<List<Object>> rows = Context.getAdministrationService().executeSQL(
						"select reception_number from mmi_patient_reception_log " +
								"where status = 'SUCCESS' and patient_id = " + patient.getPatientId() + " " +
								"and reception_number is not null and reception_number <> '' " +
								"order by date_created desc limit 1",
						true
				);
				if (rows != null && !rows.isEmpty() && rows.get(0) != null && !rows.get(0).isEmpty() && rows.get(0).get(0) != null) {
					return StringUtils.trimToNull(String.valueOf(rows.get(0).get(0)));
				}
			}
			catch (Exception e) {
				log.warn("Unable to resolve MMI reception number by patient id", e);
			}
		}
		return null;
	}

	private boolean isMmiInsurance(Insurance insurance) {
		if (insurance == null) {
			return false;
		}
		return containsMmi(insurance.getCategory()) || containsMmi(insurance.getName());
	}

	private boolean containsMmi(String value) {
		return StringUtils.isNotBlank(value) && value.trim().toUpperCase().contains("MMI");
	}

	private void storeRhipPatientId(InsurancePolicy policy, String patientId) {
		if (policy == null || StringUtils.isBlank(patientId) || billingService == null) {
			return;
		}
		if (StringUtils.equals(patientId, policy.getRhipPatientId())) {
			return;
		}
		policy.setRhipPatientId(patientId);
		try {
			billingService.saveInsurancePolicy(policy);
		} catch (Exception e) {
			log.warn("Unable to persist RHIP patientId for policy " + policy.getInsurancePolicyId(), e);
		}
	}

	private List<RhipVoucherProcedure> buildProcedures(GlobalBill globalBill, Admission admission) {
		if (billingService == null) {
			return Collections.emptyList();
		}
		List<Consommation> consommations = billingService.getAllConsommationByGlobalBill(globalBill);
		if (consommations == null || consommations.isEmpty()) {
			return Collections.emptyList();
		}
		List<RhipVoucherProcedure> procedures = new ArrayList<>();
		for (Consommation consommation : consommations) {
			if (consommation == null || consommation.getBillItems() == null) {
				continue;
			}
			for (PatientServiceBill billItem : consommation.getBillItems()) {
				if (billItem == null || Boolean.TRUE.equals(billItem.getVoided())) {
					continue;
				}
				RhipVoucherProcedure procedure = toProcedure(billItem, admission);
				if (procedure != null) {
					procedures.add(procedure);
				}
			}
		}
		return procedures;
	}

	private RhipVoucherProcedure toProcedure(PatientServiceBill billItem, Admission admission) {
		BillableService service = billItem.getService();
		FacilityServicePrice facilityServicePrice = service == null ? null : service.getFacilityServicePrice();
		String code = extractProcedureCode(facilityServicePrice);
		if (StringUtils.isBlank(code)) {
			return null;
		}
		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode(code);
		procedure.setQuantity(defaultIfNull(billItem.getQuantity()));
		procedure.setPrice(resolvePrice(billItem, facilityServicePrice));
		Date serviceDate = billItem.getServiceDate();
		if (serviceDate == null) {
			serviceDate = billItem.getCreatedDate();
		}
		if (serviceDate == null && admission != null) {
			serviceDate = admission.getAdmissionDate();
		}
		procedure.setPrescribedAt(formatDate(serviceDate));
		return procedure;
	}

	private BigDecimal resolvePrice(PatientServiceBill billItem, FacilityServicePrice facilityServicePrice) {
		if (billItem.getUnitPrice() != null) {
			return billItem.getUnitPrice();
		}
		return facilityServicePrice == null ? null : facilityServicePrice.getFullPrice();
	}

	private BigDecimal defaultIfNull(BigDecimal value) {
		return value == null ? BigDecimal.ONE : value;
	}

	private String extractProcedureCode(FacilityServicePrice facilityServicePrice) {
		if (facilityServicePrice == null) {
			return null;
		}
		String name = facilityServicePrice.getName();
		if (StringUtils.isBlank(name)) {
			return null;
		}
		String[] parts = name.split("\\|", 2);
		if (parts.length == 2) {
			return parts[1].trim();
		}
		return name.trim();
	}

	private String resolveDiagnosisNotes(Patient patient, Admission admission, GlobalBill globalBill) {
		if (billingService == null) {
			return null;
		}
		String conceptIds = resolveDiagnosisConceptIds();
		if (StringUtils.isBlank(conceptIds) || patient == null) {
			return null;
		}
		String admissionDate = formatSqlDateTime(resolveAdmissionDate(globalBill, admission), false);
		String dischargeDate = formatSqlDateTime(resolveDischargeDate(globalBill, admission), true);
		if (StringUtils.isBlank(admissionDate) || StringUtils.isBlank(dischargeDate)) {
			return null;
		}
		return billingService.getDiagnosisFromAdmissionToDischarge(conceptIds, admissionDate, dischargeDate, patient.getPatientId());
	}

	private Date resolveAdmissionDate(GlobalBill globalBill, Admission admission) {
		if (admission != null && admission.getAdmissionDate() != null) {
			return admission.getAdmissionDate();
		}
		return globalBill == null ? null : globalBill.getCreatedDate();
	}

	private String resolveDiagnosisConceptIds() {
		String configured = config == null ? null : config.getDiagnosisConceptIds();
		if (StringUtils.isNotBlank(configured)) {
			return configured;
		}
		String finalDiagnosisIds = Context.getAdministrationService().getGlobalProperty(
				"billing.finalDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");
		String differentialDiagnosisIds = Context.getAdministrationService().getGlobalProperty(
				"billing.differentialDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");
		List<String> collected = new ArrayList<>();
		appendCsvValues(collected, finalDiagnosisIds);
		appendCsvValues(collected, differentialDiagnosisIds);
		if (collected.isEmpty()) {
			return null;
		}
		return StringUtils.join(collected, ",");
	}

	private void appendCsvValues(List<String> target, String csv) {
		if (target == null || StringUtils.isBlank(csv)) {
			return;
		}
		String[] values = csv.split(",");
		for (String value : values) {
			String trimmed = StringUtils.trimToNull(value);
			if (trimmed == null) {
				continue;
			}
			if (!target.contains(trimmed)) {
				target.add(trimmed);
			}
		}
	}

	private List<String> resolveDiagnosisIds(String diagnosisNotes) {
		if (StringUtils.isBlank(diagnosisNotes)) {
			return Collections.emptyList();
		}
		Set<String> diagnosisCodes = new LinkedHashSet<>();
		Matcher matcher = ICD_CODE_WITH_NAME_PATTERN.matcher(diagnosisNotes);
		while (matcher.find()) {
			String candidate = matcher.group(1);
			if (StringUtils.isBlank(candidate)) {
				continue;
			}
			String code = candidate.trim();
			if (ICD_CODE_PATTERN.matcher(code).matches()) {
				diagnosisCodes.add(code);
			}
		}

		String[] parts = diagnosisNotes.split("[,;\\n]+");
		for (String part : parts) {
			String code = extractDiagnosisCode(part);
			if (StringUtils.isNotBlank(code)) {
				diagnosisCodes.add(code);
			}
		}
		return diagnosisCodes.isEmpty() ? Collections.<String>emptyList() : new ArrayList<>(diagnosisCodes);
	}

	private String extractDiagnosisCode(String diagnosisLabel) {
		if (StringUtils.isBlank(diagnosisLabel)) {
			return null;
		}
		String value = diagnosisLabel.trim();
		int labelSeparator = value.lastIndexOf(':');
		if (labelSeparator >= 0 && labelSeparator < value.length() - 1) {
			value = value.substring(labelSeparator + 1).trim();
		}
		int separator = value.indexOf('-');
		String candidate = separator > 0 ? value.substring(0, separator).trim() : value;
		if (StringUtils.isBlank(candidate)) {
			return null;
		}
		if (!ICD_CODE_PATTERN.matcher(candidate).matches()) {
			return null;
		}
		return candidate;
	}

	private String resolvePatientType(Admission admission) {
		String defaultValue = config == null ? null : config.getDefaultPatientType();
		if (StringUtils.isNotBlank(defaultValue)) {
			return defaultValue;
		}
		return isAdmitted(admission) ? "HOUSEHOLD_MEMBER" : "SPECIAL_CASE";
	}

	private String resolveHealthCareStayType(Admission admission) {
		String defaultValue = config == null ? null : config.getDefaultHealthCareStayType();
		if (StringUtils.isNotBlank(defaultValue)) {
			return defaultValue;
		}
		return isAdmitted(admission) ? "IN_PATIENT" : "OUT_PATIENT";
	}

	private Boolean resolveTreatmentForNewBorn() {
		Boolean configuredValue = config == null ? null : config.getDefaultTreatmentForNewBorn();
		return configuredValue == null ? Boolean.FALSE : configuredValue;
	}

	private String resolvePatientPhoneNumber(Patient patient, GlobalBill globalBill) {
		if (patient == null || personService == null || config == null) {
			return null;
		}
		String attributeTypeUuid = config.getPatientPhoneAttributeTypeUuid();
		if (StringUtils.isBlank(attributeTypeUuid)) {
			return null;
		}
		PersonAttributeType attributeType = personService.getPersonAttributeTypeByUuid(attributeTypeUuid);
		PersonAttribute attribute = attributeType == null ? null : patient.getAttribute(attributeType);
		Object value = attribute == null ? null : attribute.getValue();
		if (value == null) {
			return null;
		}
		String text = value.toString();
		return StringUtils.isBlank(text) ? null : text;
	}

	private Concept getConcept(String conceptRef) {
		if (conceptService == null || StringUtils.isBlank(conceptRef)) {
			return null;
		}
		String trimmed = conceptRef.trim();
		Concept concept = conceptService.getConceptByUuid(trimmed);
		if (concept == null) {
			concept = conceptService.getConcept(trimmed);
		}
		return concept;
	}

	private String resolveFosaId(GlobalBill globalBill) {
		String fosaId = config == null ? null : config.getDefaultFosaId();
		if (StringUtils.isBlank(fosaId)) {
			log.warn("No FOSA ID configured for RHIP voucher submission");
		}
		return fosaId;
	}

	private Date resolveDischargeDate(GlobalBill globalBill, Admission admission) {
		if (admission != null && admission.getDischargingDate() != null) {
			return admission.getDischargingDate();
		}
		return globalBill.getClosingDate();
	}

	private boolean isAdmitted(Admission admission) {
		return admission != null && Boolean.TRUE.equals(admission.getIsAdmitted());
	}

	private String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		return new SimpleDateFormat(DATE_FORMAT).format(date);
	}

	private String formatSqlDateTime(Date date, boolean endOfDay) {
		String day = formatDate(date);
		if (StringUtils.isBlank(day)) {
			return null;
		}
		return endOfDay ? day + " 23:59:59" : day + " 00:00:00";
	}

	public void setVoucherProvider(RhipVoucherProvider voucherProvider) {
		this.voucherProvider = voucherProvider;
	}

	public void setConfig(RhipVoucherIntegrationConfig config) {
		this.config = config;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}


	public void setProviderService(ProviderService providerService) {
		this.providerService = providerService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setConceptService(ConceptService conceptService) {
		this.conceptService = conceptService;
	}

	public void setRhipPractitionerTypeService(RhipPractitionerTypeService rhipPractitionerTypeService) {
		this.rhipPractitionerTypeService = rhipPractitionerTypeService;
	}

	@Transactional(readOnly = true)
	public boolean hasPractitionerSubCategoryConceptAnswers() {
		if (conceptService == null) {
			return false;
		}
		Concept question = conceptService.getConceptByUuid(RhipPractitionerConceptMetadata.RHIP_PRACTITIONER_SUBCATEGORY_CONCEPT_UUID);
		if (question == null) {
			return false;
		}
		return question.getAnswers() != null && !question.getAnswers().isEmpty();
	}

	/**
	 * Syncs RHIP practitioner sub-category types (from {@code /practitioner/types}) into OpenMRS Concepts,
	 * and attaches them as answers to the {@code RHIP Practitioner Sub-Category Type} coded question.
	 *
	 * This enables building UIs that present a controlled list of sub-categories while still sending
	 * the RHIP UUID (as the answer concept UUID) to the integration.
	 */
	@Transactional
	public IntegrationResponse syncPractitionerSubCategoryConceptAnswers(String insuranceType, String categoryId) {
		IntegrationResponse ret = new IntegrationResponse();
		ret.setEnabled(config != null && StringUtils.isNotBlank(config.getPractitionerTypesUrl()));
		if (!ret.isEnabled()) {
			ret.setErrorMessage("Practitioner types endpoint is not configured");
			return ret;
		}
		if (voucherProvider == null) {
			ret.setErrorMessage("RHIP voucher provider is not configured");
			return ret;
		}
		if (conceptService == null) {
			ret.setErrorMessage("Concept service is not configured");
			return ret;
		}

		RhipPractitionerConceptMetadata.ensureInstalled();

		String normalizedInsuranceType = normalizeInsuranceType(insuranceType);
		IntegrationResponse typesResponse = voucherProvider.getPractitionerTypes(normalizedInsuranceType, categoryId);
		Object entity = typesResponse == null ? null : typesResponse.getResponseEntity();
		boolean useLocalStore = !Boolean.TRUE.equals(isSuccessResponse(typesResponse)) || entity == null;

		int createdConcepts = 0;
		int attachedAnswers = 0;

		Concept question = conceptService.getConceptByUuid(RhipPractitionerConceptMetadata.RHIP_PRACTITIONER_SUBCATEGORY_CONCEPT_UUID);
		if (question == null) {
			ret.setErrorMessage("RHIP Practitioner Sub-Category Type concept is missing");
			return ret;
		}

		try {
			if (useLocalStore) {
				if (rhipPractitionerTypeService == null) {
					ret.setEndpointAccessible(typesResponse != null && typesResponse.isEndpointAccessible());
					ret.setResponseCode(typesResponse == null ? null : typesResponse.getResponseCode());
					ret.setResponseEntity(typesResponse == null ? null : typesResponse.getResponseEntity());
					ret.setErrorMessage("Unable to fetch practitioner types from RHIP and local store is not configured");
					return ret;
				}
					List<org.openmrs.module.mohbilling.model.RhipPractitionerType> local =
							rhipPractitionerTypeService.getByType("SUB_CATEGORY");
					for (org.openmrs.module.mohbilling.model.RhipPractitionerType item : local) {
						if (item == null || StringUtils.isBlank(item.getRhipId()) || StringUtils.isBlank(item.getName())) {
							continue;
						}
						try {
							String id = item.getRhipId().trim();
							String name = item.getName().trim();
							if (!looksLikeUuid(id)) {
								continue;
							}
							int[] delta = ensureSubCategoryAnswer(question, id, name);
							createdConcepts += delta[0];
							attachedAnswers += delta[1];
						}
						catch (Exception e) {
							log.warn("Unable to sync RHIP sub-category concept answer from local store for rhipId=" + item.getRhipId(), e);
						}
					}
				if (attachedAnswers > 0) {
					conceptService.saveConcept(question);
				}
				ret.setEndpointAccessible(typesResponse != null && typesResponse.isEndpointAccessible());
				ret.setResponseCode(typesResponse == null ? null : typesResponse.getResponseCode());
				ret.setResponseEntity("Used local store. Created " + createdConcepts + " concepts; attached " + attachedAnswers + " answers");
				return ret;
			}

			JsonNode root = OBJECT_MAPPER.readTree(entity.toString());
			JsonNode data = root == null ? null : root.get("data");
			if (data == null || !data.isArray()) {
				ret.setErrorMessage("Invalid response from RHIP practitioner types endpoint: missing data array");
				return ret;
			}

				for (JsonNode item : data) {
					if (item == null || item.isNull()) {
						continue;
					}
					try {
						upsertLocalPractitionerTypeFromJson(item);
						String type = jsonText(item.get("type"));
						if (StringUtils.isBlank(type) || !"SUB_CATEGORY".equalsIgnoreCase(type.trim())) {
							continue;
						}
						String id = jsonText(item.get("id"));
						String name = jsonText(item.get("name"));
						if (StringUtils.isBlank(id) || StringUtils.isBlank(name) || !looksLikeUuid(id.trim())) {
							continue;
						}
						int[] delta = ensureSubCategoryAnswer(question, id.trim(), name.trim());
						createdConcepts += delta[0];
						attachedAnswers += delta[1];
					}
					catch (Exception e) {
						log.warn("Unable to sync RHIP sub-category concept answer for item: " + item, e);
					}
				}

			if (attachedAnswers > 0) {
				conceptService.saveConcept(question);
			}

			ret.setEndpointAccessible(true);
			ret.setResponseCode(typesResponse.getResponseCode());
			ret.setResponseEntity("Created " + createdConcepts + " concepts; attached " + attachedAnswers + " answers");
			return ret;
		}
		catch (Exception e) {
			ret.setErrorMessage(e.getMessage());
			return ret;
		}
	}

	private void upsertLocalPractitionerTypeFromJson(JsonNode item) {
		if (rhipPractitionerTypeService == null || item == null || item.isNull()) {
			return;
		}
		try {
			String id = jsonText(item.get("id"));
			String name = jsonText(item.get("name"));
			String type = jsonText(item.get("type"));
			if (StringUtils.isBlank(id) || StringUtils.isBlank(name) || StringUtils.isBlank(type)) {
				return;
			}
			String trimmedId = id.trim();
			if (!looksLikeUuid(trimmedId)) {
				return;
			}
			org.openmrs.module.mohbilling.model.RhipPractitionerType record = rhipPractitionerTypeService.getByRhipId(trimmedId);
			if (record == null) {
				record = new org.openmrs.module.mohbilling.model.RhipPractitionerType();
				record.setRhipId(trimmedId);
			}
			record.setName(name.trim());
			record.setType(type.trim());

			JsonNode category = item.get("category");
			String categoryId = jsonText(category == null ? null : category.get("id"));
			String categoryName = jsonText(category == null ? null : category.get("name"));
			record.setCategoryRhipId(StringUtils.isBlank(categoryId) ? null : categoryId.trim());
			record.setCategoryName(StringUtils.isBlank(categoryName) ? null : categoryName.trim());

			rhipPractitionerTypeService.saveOrUpdate(record);
		}
		catch (Exception e) {
			log.debug("Unable to upsert RHIP practitioner type into local store", e);
		}
	}

	private int[] ensureSubCategoryAnswer(Concept question, String id, String name) {
		int createdConcepts = 0;
		int attachedAnswers = 0;

		Concept answer = conceptService.getConceptByUuid(id);
		if (answer == null) {
			answer = new Concept();
			answer.setUuid(id);
			ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");
			ConceptClass miscClass = conceptService.getConceptClassByName("Misc");
			if (textDatatype == null || miscClass == null) {
				return new int[] { 0, 0 };
			}
			answer.setDatatype(textDatatype);
			answer.setConceptClass(miscClass);
			answer.addName(new ConceptName(buildSubCategoryConceptName(name, null), java.util.Locale.ENGLISH));
			try {
				answer = conceptService.saveConcept(answer);
				createdConcepts++;
			}
			catch (DuplicateConceptNameException e) {
				answer.getNames().clear();
				answer.addName(new ConceptName(buildSubCategoryConceptName(name, id), java.util.Locale.ENGLISH));
				answer = conceptService.saveConcept(answer);
				createdConcepts++;
			}
		}

		boolean alreadyAnswer = false;
		for (org.openmrs.ConceptAnswer existingAnswer : question.getAnswers()) {
			if (existingAnswer != null && existingAnswer.getAnswerConcept() != null
					&& StringUtils.equals(existingAnswer.getAnswerConcept().getUuid(), answer.getUuid())) {
				alreadyAnswer = true;
				break;
			}
		}
		if (!alreadyAnswer) {
			question.addAnswer(new org.openmrs.ConceptAnswer(answer));
			attachedAnswers++;
		}
		return new int[] { createdConcepts, attachedAnswers };
	}

	private String buildSubCategoryConceptName(String name, String id) {
		String base = "RHIP Sub-Category - " + (name == null ? "" : name.trim());
		if (StringUtils.isNotBlank(id)) {
			base = base + " (" + id.trim() + ")";
		}
		// concept_name.name is typically limited to 255 chars in OpenMRS
		if (base.length() <= 255) {
			return base;
		}
		String suffix = StringUtils.isNotBlank(id) ? " (" + id.trim() + ")" : "";
		String prefix = "RHIP Sub-Category - ";
		int maxNameLen = 255 - prefix.length() - suffix.length();
		if (maxNameLen < 0) {
			return base.substring(0, 255);
		}
		String trimmedName = name == null ? "" : name.trim();
		if (trimmedName.length() > maxNameLen) {
			trimmedName = trimmedName.substring(0, maxNameLen);
		}
		return prefix + trimmedName + suffix;
	}
}
