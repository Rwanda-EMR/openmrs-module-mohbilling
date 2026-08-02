package org.openmrs.module.mohbilling.integration.insurance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
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
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RhipVoucherService {

	private static final Log log = LogFactory.getLog(RhipVoucherService.class);
	private static final String CBHI_INSURANCE_TYPE = "CBHI";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String PRACTITIONER_TYPE_LOCAL = "LOCAL";
	private static final String PRACTITIONER_TYPE_FOREIGN = "FOREIGN";
	private static final Pattern SUCCESS_PATTERN =
			Pattern.compile("\"success\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private RhipVoucherProvider voucherProvider;
	private RhipVoucherIntegrationConfig config;
	private BillingService billingService;
	private ProviderService providerService;
	private PersonService personService;
	private ConceptService conceptService;

	public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
		if (request != null && !isCbhiInsuranceType(request.getInsuranceType())) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("Only CBHI vouchers are supported at this time");
			return ret;
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
		RhipVoucherRequest request = buildVoucherRequestFromGlobalBill(globalBill, eligibilityIdentifier);
		if (request == null) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("Unable to build voucher request from global bill");
			return ret;
		}
		IntegrationResponse practitionerCheck = ensurePractitionerRegistered(request, resolveProcessedBy(globalBill));
		if (practitionerCheck != null && practitionerCheck.getErrorMessage() != null) {
			return practitionerCheck;
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
		if (!isCbhiInsurance(insurance)) {
			log.debug("Skipping voucher submission for non-CBHI insurance");
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
		request.setInsuranceType(CBHI_INSURANCE_TYPE);
		String fosaId = resolveFosaId(globalBill);
		request.setFacilityFosaId(fosaId);
		request.setPatientIdentifier(resolvePatientIdentifier(policy, patient, insurance, fosaId, eligibilityIdentifier));
		request.setProcedures(buildProcedures(globalBill, admission));

		User processedBy = resolveProcessedBy(globalBill);
		String providerLicenseNumber = resolveProviderLicense(processedBy);
		request.setUserAccountCode(providerLicenseNumber);
		request.setPractitionerLicenseNumber(providerLicenseNumber);
		request.setProcessedBy(processedBy == null ? null : processedBy.getUsername());

		request.setNotes(resolveDiagnosisNotes(patient, admission, globalBill));
		request.setPatientType(resolvePatientType(admission));
		request.setHealthCareStayType(resolveHealthCareStayType(admission));
		request.setAdmissionDate(formatDate(admission == null ? null : admission.getAdmissionDate()));
		request.setDischargeDate(formatDate(resolveDischargeDate(globalBill, admission)));
		request.setTreatmentForNewBorn(resolveTreatmentForNewBorn());
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
		PractitionerRegistration registration = buildPractitionerRegistration(insuranceType, licenseNumber, request, processedBy);
		if (registration.errorMessage != null) {
			ret.setErrorMessage(registration.errorMessage);
			return ret;
		}
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
		registration.practitionerSubCategoryTypeId = resolvePractitionerSubCategoryTypeId(processedBy);
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
			registration.errorMessage = "Practitioner sub-category type id is not configured";
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

	private String resolvePractitionerSubCategoryTypeId(User user) {
		return resolveProviderAttributeValue(user,
				config == null ? null : config.getPractitionerSubCategoryTypeIdProviderAttributeTypeUuid());
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
		return insuranceType.trim().toLowerCase();
	}

	private String defaultIfBlank(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value;
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
		String conceptIds = config == null ? null : config.getDiagnosisConceptIds();
		if (StringUtils.isBlank(conceptIds)) {
			conceptIds = Context.getAdministrationService().getGlobalProperty(
					"billing.finalDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");
		}
		if (StringUtils.isBlank(conceptIds) || patient == null) {
			return null;
		}
		String admissionDate = formatDate(admission == null ? null : admission.getAdmissionDate());
		String dischargeDate = formatDate(resolveDischargeDate(globalBill, admission));
		if (StringUtils.isBlank(admissionDate) || StringUtils.isBlank(dischargeDate)) {
			return null;
		}
		return billingService.getDiagnosisFromAdmissionToDischarge(conceptIds, admissionDate, dischargeDate, patient.getPatientId());
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
}
