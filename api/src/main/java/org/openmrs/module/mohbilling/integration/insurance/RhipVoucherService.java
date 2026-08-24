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
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
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
import org.openmrs.module.mohbilling.model.RhipIntegrationLog;
import org.openmrs.module.mohbilling.model.RhipVoucherItemRecord;
import org.openmrs.module.mohbilling.model.RhipVoucherSubmission;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.service.RhipPractitionerTypeService;
import org.openmrs.module.mohbilling.utils.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RhipVoucherService {

	private static final Log log = LogFactory.getLog(RhipVoucherService.class);
	private static final String CBHI_INSURANCE_TYPE = "CBHI";
	private static final String RAMA_INSURANCE_TYPE = "RAMA";
	private static final String MMI_INSURANCE_TYPE = "MMI";
	private static final String SPECIAL_CASE_INSURANCE_TYPE = "SPECIAL_CASE";
	private static final String DEFAULT_RAMA_PRESCRIPTION_DESTINATION = "FACILITY_DISPENSE";
	private static final String MEDICAMENTS_SERVICE_CATEGORY = "MEDICAMENTS";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String PRACTITIONER_TYPE_LOCAL = "LOCAL";
	private static final String PRACTITIONER_TYPE_FOREIGN = "FOREIGN";
	private static final String MMI_RECEPTION_NUMBER_VISIT_ATTRIBUTE_KEY = "visitAttribute.mmiReceptionNumber.uuid";
	private static final String MMI_RECEPTION_NUMBER_VISIT_ATTRIBUTE_GP =
			"rwandaemr.visitAttribute.mmiReceptionNumber.uuid";
	private static final Pattern SUCCESS_PATTERN =
			Pattern.compile("\"success\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
	private static final Pattern UUID_PATTERN = Pattern.compile(
			"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	private static final Pattern ICD_CODE_PATTERN = Pattern.compile("^(?=.*\\d)[A-Za-z0-9.]+$");
	private static final Pattern ICD_CODE_WITH_NAME_PATTERN =
			Pattern.compile("([A-Za-z0-9.]+)\\s*-");
	private static final Pattern DASH_SEPARATED_PROCEDURE_CODE =
			Pattern.compile(".*?-([A-Z]{4}(?:-[A-Z0-9]+)+-[0-9]+)$");
	private static final Pattern PRESCRIPTION_DOSE_PATTERN =
			Pattern.compile("(?:^|;)\\s*Dose\\s*:\\s*([^;]+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PRESCRIPTION_ROUTE_PATTERN =
			Pattern.compile("(?:^|;)\\s*Route\\s*:\\s*([^;]+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PRESCRIPTION_FREQUENCY_PATTERN =
			Pattern.compile("(?:^|;)\\s*Frequency\\s*:\\s*([^;]+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PRESCRIPTION_DURATION_PATTERN = Pattern.compile(
			"(?:^|;)\\s*Duration\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*([A-Za-z]+)",
			Pattern.CASE_INSENSITIVE);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private RhipVoucherProvider voucherProvider;
	private RhipVoucherIntegrationConfig config;
	private BillingService billingService;
	private ProviderService providerService;
	private PersonService personService;
	private ConceptService conceptService;
	private final Map<String, String> practitionerSubCategoryNameToIdCache = new ConcurrentHashMap<>();
	private RhipPractitionerTypeService rhipPractitionerTypeService;
	private RpmVoucherItemResolver rpmVoucherItemResolver;

	public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
		if (request != null && !isSupportedVoucherInsuranceType(request.getInsuranceType())) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("insuranceType must be CBHI, MUTUELLE, SPECIAL_CASE, RAMA, RSSB, or MMI");
			return ret;
		}
		normalizeMmiMedicineDetails(request);
		IntegrationResponse validation = validateVoucherRequest(request);
		if (validation != null) {
			persistLocalVoucherValidationLog(request, validation);
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
		RhipVoucherRequest request;
		try {
			request = buildVoucherRequestFromGlobalBill(globalBill, eligibilityIdentifier);
		}
		catch (IllegalStateException e) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("Invalid RHIP voucher request: " + e.getMessage());
			return ret;
		}
		if (request == null) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("Unable to build voucher request from global bill");
			return ret;
		}
		normalizeMmiMedicineDetails(request);
		rejectNonPositivePriceProcedures(globalBill, request);
		if (!isRamaInsuranceType(request.getInsuranceType())
				&& (request.getProcedures() == null || request.getProcedures().isEmpty())) {
			IntegrationResponse ret = new IntegrationResponse();
			ret.setEnabled(config != null && config.isVoucherEnabled());
			ret.setErrorMessage("Unable to submit RHIP voucher: all voucher items have zero or missing prices");
			return ret;
		}
		IntegrationResponse validation = validateVoucherRequest(request);
		if (validation != null) {
			persistLocalVoucherValidationLog(request, validation);
			return validation;
		}
		if (requiresPractitionerRegistration(request.getInsuranceType())) {
			IntegrationResponse practitionerCheck = ensurePractitionerRegistered(request, resolveProcessedBy(globalBill));
			if (practitionerCheck != null && practitionerCheck.getErrorMessage() != null) {
				return practitionerCheck;
			}
		}
		IntegrationResponse response = submitVoucher(request);
		return handlePartialVoucherSubmission(globalBill, request, response);
	}

	public RhipVoucherSubmission submitVoucherForGlobalBillWithAudit(GlobalBill globalBill) {
		return submitVoucherForGlobalBillWithAudit(globalBill, null);
	}

	public RhipVoucherSubmission submitVoucherForGlobalBillWithAudit(GlobalBill globalBill, String eligibilityIdentifier) {
		User currentUser = getAuthenticatedUserSafely();
		RhipVoucherSubmission submission = newVoucherSubmission(globalBill, currentUser);
		if (billingService == null) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("Billing service is not configured");
			return submission;
		}
		if (globalBill == null) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("Global bill is required");
			return billingService.saveRhipVoucherSubmission(submission);
		}
		if (hasExistingVoucherIdentifiers(globalBill)
				|| billingService.getSuccessfulRhipVoucherSubmission(globalBill) != null) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("RHIP voucher already exists for this global bill");
			return billingService.saveRhipVoucherSubmission(submission);
		}
		if (!Boolean.TRUE.equals(globalBill.getClosed())) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("Global Bill must be discharged before sending a voucher");
			return billingService.saveRhipVoucherSubmission(submission);
		}

		RhipVoucherRequest request;
		try {
			request = buildVoucherRequestFromGlobalBill(globalBill, eligibilityIdentifier);
		}
		catch (IllegalStateException e) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("Invalid RHIP voucher request: " + e.getMessage());
			return billingService.saveRhipVoucherSubmission(submission);
		}
		submission.setRequestPayload(toJson(request));
		if (request == null) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("Unable to build voucher request from global bill");
			return billingService.saveRhipVoucherSubmission(submission);
		}
		normalizeMmiMedicineDetails(request);
		rejectNonPositivePriceProcedures(globalBill, request);
		submission.setRequestPayload(toJson(request));
		if (!isRamaInsuranceType(request.getInsuranceType())
				&& (request.getProcedures() == null || request.getProcedures().isEmpty())) {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage("Unable to submit RHIP voucher: all voucher items have zero or missing prices");
			return billingService.saveRhipVoucherSubmission(submission);
		}
		IntegrationResponse validation = validateVoucherRequest(request);
		if (validation != null) {
			persistLocalVoucherValidationLog(request, validation);
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setResponseCode(validation.getResponseCode());
			submission.setResponsePayload(toJson(validation.getResponseEntity()));
			submission.setErrorMessage(validation.getErrorMessage());
			return billingService.saveRhipVoucherSubmission(submission);
		}
		if (requiresPractitionerRegistration(request.getInsuranceType())) {
			IntegrationResponse practitionerCheck = ensurePractitionerRegistered(request, resolveProcessedBy(globalBill));
			if (practitionerCheck != null && practitionerCheck.getErrorMessage() != null) {
				submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
				submission.setResponseCode(practitionerCheck.getResponseCode());
				submission.setResponsePayload(toJson(practitionerCheck.getResponseEntity()));
				submission.setErrorMessage(practitionerCheck.getErrorMessage());
				return billingService.saveRhipVoucherSubmission(submission);
			}
		}

		IntegrationResponse response = submitVoucher(request);
		response = handlePartialVoucherSubmission(globalBill, request, response);
		submission.setResponseCode(response == null ? null : response.getResponseCode());
		submission.setResponsePayload(toJson(response == null ? null : response.getResponseEntity()));
		boolean success = Boolean.TRUE.equals(isSuccessResponse(response));
		if (success) {
			VoucherIdentifiers identifiers = extractVoucherIdentifiers(response);
			submission.setStatus(RhipVoucherSubmission.STATUS_SENT);
			submission.setVoucherCode(identifiers.voucherCode);
			submission.setVoucherReferenceNumber(identifiers.voucherReferenceNumber);
			if (StringUtils.isNotBlank(identifiers.voucherCode)) {
				globalBill.setRhipVoucherCode(identifiers.voucherCode);
			}
			if (StringUtils.isNotBlank(identifiers.voucherReferenceNumber)) {
				globalBill.setRhipVoucherReferenceNumber(identifiers.voucherReferenceNumber);
			}
			billingService.saveGlobalBill(globalBill);
		} else {
			submission.setStatus(RhipVoucherSubmission.STATUS_FAILED);
			submission.setErrorMessage(resolveVoucherSubmissionError(response));
		}
		return billingService.saveRhipVoucherSubmission(submission);
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
			String receptionNumber = resolveMmiReceptionNumber(request.getPatientIdentifier(), patient, admission);
			request.setReceptionNumber(receptionNumber);
			request.setVisitReferenceNumber(receptionNumber);
		}
		request.setProcedures(buildProcedures(globalBill, admission, normalizedInsuranceType));

		User processedBy = resolveProcessedBy(globalBill);
		String providerLicenseNumber = resolveProviderLicense(processedBy);
		request.setUserAccountCode(providerLicenseNumber);
		request.setPractitionerLicenseNumber(providerLicenseNumber);
		request.setProcessedBy(processedBy == null ? null : processedBy.getUsername());

		String diagnosisNotes = resolveDiagnosisNotes(patient, admission, globalBill);
		request.setNotes(diagnosisNotes);
		request.setDiagnosisIds(resolveDiagnosisIds(diagnosisNotes));
		request.setReferralFacilityId(resolveReferralFacilityId(globalBill));
		if (request.getDiagnosisIds() == null || request.getDiagnosisIds().isEmpty()) {
			log.warn("No ICD-11 diagnosis codes resolved for RHIP voucher request; globalBillId="
					+ (globalBill == null ? null : globalBill.getGlobalBillId()));
		}
		if (!isRamaInsuranceType(normalizedInsuranceType)) {
			request.setPatientType(resolvePatientType(admission));
		}
		request.setHealthCareStayType(resolveHealthCareStayType(admission));
		request.setAdmissionDate(formatDate(admission == null ? null : admission.getAdmissionDate()));
		request.setDischargeDate(formatDate(resolveDischargeDate(globalBill, admission)));
		if (!isMmiInsuranceType(normalizedInsuranceType)) {
			request.setTreatmentForNewBorn(resolveTreatmentForNewBorn());
		}
		request.setPatientPhoneNumber(resolvePatientPhoneNumber(patient, globalBill));
		if (isRamaInsuranceType(normalizedInsuranceType)) {
			request.setPrescriptionDestination(DEFAULT_RAMA_PRESCRIPTION_DESTINATION);
			request.setVisitReferenceNumber(resolveRamaVisitReferenceNumber(globalBill, fosaId));
		}

		return request;
	}

	private boolean isCbhiInsurance(Insurance insurance) {
		if (insurance == null) {
			return false;
		}
		if (isCbhiInsuranceType(insurance.getCategory()) || isSpecialCaseInsuranceType(insurance.getCategory())) {
			return true;
		}
		return isCbhiInsuranceType(insurance.getName()) || isSpecialCaseInsuranceType(insurance.getName());
	}

	private boolean isRamaInsurance(Insurance insurance) {
		if (insurance == null) {
			return false;
		}
		return isRamaInsuranceType(insurance.getCategory()) || isRamaInsuranceType(insurance.getName());
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

	private boolean isSpecialCaseInsuranceType(String type) {
		if (StringUtils.isBlank(type)) {
			return false;
		}
		String normalized = type.trim().replace(' ', '_');
		return SPECIAL_CASE_INSURANCE_TYPE.equalsIgnoreCase(normalized);
	}

	private boolean isRamaInsuranceType(String type) {
		if (StringUtils.isBlank(type)) {
			return false;
		}
		String normalized = type.trim();
		return RAMA_INSURANCE_TYPE.equalsIgnoreCase(normalized)
		        || "RSSB".equalsIgnoreCase(normalized)
		        || normalized.toUpperCase().contains("RAMA");
	}

	private boolean isSupportedVoucherInsuranceType(String type) {
		return isCbhiInsuranceType(type) || isSpecialCaseInsuranceType(type)
		        || isRamaInsuranceType(type) || isMmiInsuranceType(type);
	}

	private boolean isSupportedVoucherInsurance(Insurance insurance) {
		return isCbhiInsurance(insurance) || isRamaInsurance(insurance) || isMmiInsurance(insurance);
	}

	private boolean requiresPractitionerRegistration(String insuranceType) {
		return !isMmiInsuranceType(insuranceType) && !isRamaInsuranceType(insuranceType);
	}

	private String normalizeVoucherInsuranceType(Insurance insurance) {
		if (isMmiInsurance(insurance)) {
			return MMI_INSURANCE_TYPE;
		}
		if (isRamaInsurance(insurance)) {
			return RAMA_INSURANCE_TYPE;
		}
		if (isSpecialCaseInsuranceType(insurance == null ? null : insurance.getCategory())
				|| isSpecialCaseInsuranceType(insurance == null ? null : insurance.getName())) {
			return SPECIAL_CASE_INSURANCE_TYPE;
		}
		return CBHI_INSURANCE_TYPE;
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
			errors.add("insuranceType must be CBHI, MUTUELLE, SPECIAL_CASE, RAMA, RSSB, or MMI");
		}
		if (StringUtils.isBlank(request.getFacilityFosaId())) {
			errors.add("facilityFosaId is required");
		}
		if (StringUtils.isBlank(request.getPatientIdentifier())) {
			errors.add("patientIdentifier is required");
		}
		if (requiresPatientType(request.getInsuranceType()) && StringUtils.isBlank(request.getPatientType())) {
			errors.add("patientType is required for CBHI and SPECIAL_CASE vouchers");
		}
		if (StringUtils.isBlank(request.getPractitionerLicenseNumber())) {
			errors.add("practitionerLicenseNumber is required");
		}
		if (requiresTreatmentForNewBorn(request.getInsuranceType()) && request.getTreatmentForNewBorn() == null) {
			errors.add("treatmentForNewBorn must be explicitly true or false");
		}
		if (requiresDiagnosisIds(request.getInsuranceType())
				&& (request.getDiagnosisIds() == null || request.getDiagnosisIds().isEmpty())) {
			errors.add("diagnosisIds is required and must contain at least one ICD-11 code");
		}
		if (isMmiInsuranceType(request.getInsuranceType()) && StringUtils.isBlank(resolveMmiVoucherVisitReferenceNumber(request))) {
			errors.add("visitReferenceNumber is required for MMI voucher");
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
				if (requiresProcedurePrice(request.getInsuranceType())
						&& (procedure.getPrice() == null || procedure.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
					errors.add("procedure #" + oneBased + " price must be greater than 0");
				}
				if (isMmiInsuranceType(request.getInsuranceType()) && isMmiMedicineProcedure(procedure)) {
					if (StringUtils.isBlank(procedure.getInstructions())) {
						errors.add("procedure #" + oneBased + " instructions is required for MMI medicine lines");
					}
					if (procedure.getDurationDays() == null) {
						errors.add("procedure #" + oneBased + " durationDays is required for MMI medicine lines");
					}
				}
			}
		}
		if (errors.isEmpty()) {
			return null;
		}
		ret.setErrorMessage("Invalid RHIP voucher request: " + StringUtils.join(errors, "; "));
		return ret;
	}

	private boolean requiresTreatmentForNewBorn(String insuranceType) {
		return isCbhiInsuranceType(insuranceType) || isSpecialCaseInsuranceType(insuranceType);
	}

	private boolean requiresPatientType(String insuranceType) {
		return isCbhiInsuranceType(insuranceType) || isSpecialCaseInsuranceType(insuranceType);
	}

	private boolean requiresProcedurePrice(String insuranceType) {
		return isCbhiInsuranceType(insuranceType) || isSpecialCaseInsuranceType(insuranceType);
	}

	private boolean requiresDiagnosisIds(String insuranceType) {
		return isCbhiInsuranceType(insuranceType) || isSpecialCaseInsuranceType(insuranceType);
	}

	private boolean isMmiMedicineProcedure(RhipVoucherProcedure procedure) {
		if (procedure == null || StringUtils.isBlank(procedure.getCode())) {
			return false;
		}
		return !procedure.getCode().trim().toUpperCase().startsWith("RHIC-");
	}

	private String resolveMmiVoucherVisitReferenceNumber(RhipVoucherRequest request) {
		if (request == null) {
			return null;
		}
		if (StringUtils.isNotBlank(request.getVisitReferenceNumber())) {
			return request.getVisitReferenceNumber();
		}
		return request.getReceptionNumber();
	}

	private void persistLocalVoucherValidationLog(RhipVoucherRequest request, IntegrationResponse validation) {
		if (billingService == null) {
			return;
		}
		try {
			User currentUser = Context.getAuthenticatedUser();
			RhipIntegrationLog logEntry = new RhipIntegrationLog();
			logEntry.setDateCreated(new Date());
			logEntry.setCreator(currentUser);
			logEntry.setSenderUsername(resolveSenderUsername(request, currentUser));
			logEntry.setOperationType("VOUCHER_VALIDATE");
			logEntry.setEndpointUrl(config == null ? null : config.getVoucherUrl());
			logEntry.setRequestPayload(toJson(request));
			logEntry.setResponseCode(validation == null ? null : validation.getResponseCode());
			logEntry.setResponseStatus("ERROR");
			logEntry.setResponseBody(toJson(validation == null ? null : validation.getResponseEntity()));
			logEntry.setErrorMessage(validation == null ? "No validation response" : validation.getErrorMessage());
			logEntry.setUuid(UUID.randomUUID().toString());
			billingService.saveRhipIntegrationLog(logEntry);
		}
		catch (Exception e) {
			log.warn("Unable to persist local RHIP voucher validation log", e);
		}
	}

	private String resolveSenderUsername(RhipVoucherRequest request, User currentUser) {
		if (request != null && StringUtils.isNotBlank(request.getProcessedBy())) {
			return request.getProcessedBy().trim();
		}
		return currentUser == null ? null : currentUser.getUsername();
	}

	private String toJson(Object value) {
		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		}
		catch (Exception e) {
			log.warn("Unable to serialize RHIP voucher validation payload", e);
			return null;
		}
	}

	private RhipVoucherSubmission newVoucherSubmission(GlobalBill globalBill, User currentUser) {
		RhipVoucherSubmission submission = new RhipVoucherSubmission();
		submission.setGlobalBill(globalBill);
		submission.setStatus(RhipVoucherSubmission.STATUS_PROCESSING);
		submission.setSubmittedBy(currentUser);
		submission.setDateSubmitted(new Date());
		submission.setAttemptNumber(resolveNextVoucherSubmissionAttemptNumber(globalBill));
		submission.setUuid(UUID.randomUUID().toString());
		return submission;
	}

	private Integer resolveNextVoucherSubmissionAttemptNumber(GlobalBill globalBill) {
		if (billingService == null || globalBill == null) {
			return 1;
		}
		RhipVoucherSubmission latest = billingService.getLatestRhipVoucherSubmission(globalBill);
		if (latest == null || latest.getAttemptNumber() == null) {
			return 1;
		}
		return latest.getAttemptNumber() + 1;
	}

	private User getAuthenticatedUserSafely() {
		try {
			return Context.getAuthenticatedUser();
		}
		catch (Exception ignored) {
			return null;
		}
	}

	private String resolveVoucherSubmissionError(IntegrationResponse response) {
		if (response == null) {
			return "RHIP voucher submission failed: no response received";
		}
		if (!response.isEnabled()) {
			return "RHIP voucher submission failed: integration is disabled";
		}
		if (StringUtils.isNotBlank(response.getErrorMessage())) {
			return response.getErrorMessage();
		}
		String rhipMessage = extractRhipMessage(response.getResponseEntity());
		if (StringUtils.isNotBlank(rhipMessage)) {
			return rhipMessage;
		}
		if (response.getResponseCode() == null && response.getResponseEntity() == null) {
			return "RHIP voucher submission failed: empty response from RHIP";
		}
		return "RHIP did not return a successful voucher response";
	}

	private String extractRhipMessage(Object responseEntity) {
		JsonNode root = toJsonNode(responseEntity);
		if (root == null) {
			return null;
		}
		String message = jsonText(root.get("message"));
		if (StringUtils.isNotBlank(message)) {
			return message;
		}
		JsonNode error = root.get("error");
		if (error != null && !error.isNull()) {
			return jsonText(error);
		}
		JsonNode errors = root.get("errors");
		if (errors != null && errors.isArray() && errors.size() > 0) {
			List<String> messages = new ArrayList<String>();
			for (JsonNode item : errors) {
				String itemMessage = jsonText(item == null ? null : item.get("message"));
				if (StringUtils.isNotBlank(itemMessage)) {
					messages.add(itemMessage);
				}
			}
			if (!messages.isEmpty()) {
				return StringUtils.join(messages, "; ");
			}
		}
		return null;
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
	private String resolveMmiReceptionNumber(String patientIdentifier, Patient patient, Admission admission) {
		String visitAttributeReceptionNumber = resolveMmiReceptionNumberFromVisitAttribute(patient, admission);
		if (StringUtils.isNotBlank(visitAttributeReceptionNumber)) {
			return visitAttributeReceptionNumber;
		}
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

	private String resolveMmiReceptionNumberFromVisitAttribute(Patient patient, Admission admission) {
		if (patient == null) {
			return null;
		}
		VisitAttributeType attributeType = resolveMmiReceptionNumberVisitAttributeType();
		if (attributeType == null) {
			return null;
		}
		List<Visit> visits = Context.getVisitService().getActiveVisitsByPatient(patient);
		if (visits == null || visits.isEmpty()) {
			return null;
		}
		Visit bestVisit = resolveBestVisitForAdmission(visits, admission);
		String receptionNumber = resolveMmiReceptionNumberFromVisit(bestVisit, attributeType);
		if (StringUtils.isNotBlank(receptionNumber)) {
			return receptionNumber;
		}
		for (Visit visit : visits) {
			if (visit != null && !visit.equals(bestVisit)) {
				receptionNumber = resolveMmiReceptionNumberFromVisit(visit, attributeType);
				if (StringUtils.isNotBlank(receptionNumber)) {
					return receptionNumber;
				}
			}
		}
		return null;
	}

	private Visit resolveBestVisitForAdmission(List<Visit> visits, Admission admission) {
		if (visits == null || visits.isEmpty()) {
			return null;
		}
		if (admission == null || admission.getAdmissionDate() == null) {
			return visits.get(0);
		}
		for (Visit visit : visits) {
			if (visit == null || visit.getStartDatetime() == null) {
				continue;
			}
			if (isSameDay(visit.getStartDatetime(), admission.getAdmissionDate())) {
				return visit;
			}
		}
		return visits.get(0);
	}

	private boolean isSameDay(Date first, Date second) {
		if (first == null || second == null) {
			return false;
		}
		Calendar firstCal = Calendar.getInstance();
		firstCal.setTime(first);
		Calendar secondCal = Calendar.getInstance();
		secondCal.setTime(second);
		return firstCal.get(Calendar.YEAR) == secondCal.get(Calendar.YEAR)
				&& firstCal.get(Calendar.DAY_OF_YEAR) == secondCal.get(Calendar.DAY_OF_YEAR);
	}

	private String resolveMmiReceptionNumberFromVisit(Visit visit, VisitAttributeType attributeType) {
		if (visit == null || attributeType == null) {
			return null;
		}
		for (VisitAttribute attribute : visit.getActiveAttributes()) {
			if (attributeType.equals(attribute.getAttributeType())) {
				Object value = attribute.getValue();
				return value == null ? null : StringUtils.trimToNull(value.toString());
			}
		}
		return null;
	}

	private VisitAttributeType resolveMmiReceptionNumberVisitAttributeType() {
		try {
			String uuid = StringUtils.trimToNull(Context.getAdministrationService()
					.getGlobalProperty(MMI_RECEPTION_NUMBER_VISIT_ATTRIBUTE_GP));
			if (uuid == null) {
				uuid = resolveInitializerValue(MMI_RECEPTION_NUMBER_VISIT_ATTRIBUTE_KEY);
			}
			if (uuid != null) {
				VisitAttributeType attributeType = Context.getVisitService().getVisitAttributeTypeByUuid(uuid);
				if (attributeType != null) {
					return attributeType;
				}
				log.warn("Unable to find MMI reception number visit attribute type with uuid " + uuid);
			}
			return resolveMmiReceptionNumberVisitAttributeTypeByName();
		}
		catch (Exception e) {
			log.warn("Unable to resolve MMI reception number visit attribute type", e);
			return null;
		}
	}

	private String resolveInitializerValue(String key) {
		try {
			Object initializerService = Context.getService(
					Class.forName("org.openmrs.module.initializer.api.InitializerService"));
			Object value = initializerService.getClass().getMethod("getValueFromKey", String.class)
					.invoke(initializerService, key);
			return value == null ? null : StringUtils.trimToNull(value.toString());
		}
		catch (Exception ignored) {
			return null;
		}
	}

	private VisitAttributeType resolveMmiReceptionNumberVisitAttributeTypeByName() {
		List<VisitAttributeType> attributeTypes = Context.getVisitService().getAllVisitAttributeTypes();
		if (attributeTypes == null) {
			return null;
		}
		for (VisitAttributeType attributeType : attributeTypes) {
			if (attributeType == null || attributeType.getName() == null) {
				continue;
			}
			String name = attributeType.getName().toLowerCase();
			if (name.contains("mmi") && name.contains("reception")) {
				return attributeType;
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

	private List<RhipVoucherProcedure> buildProcedures(GlobalBill globalBill, Admission admission, String insuranceType) {
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
				RhipVoucherProcedure procedure = resolveProcedure(globalBill, consommation, billItem, admission, insuranceType);
				if (procedure != null) {
					procedure.setPatientServiceBillId(billItem.getPatientServiceBillId());
					procedures.add(procedure);
				}
			}
		}
		return procedures;
	}

	private RhipVoucherProcedure resolveProcedure(GlobalBill globalBill, Consommation consommation,
	                                             PatientServiceBill billItem, Admission admission,
	                                             String insuranceType) {
		if (rpmVoucherItemResolver != null && rpmVoucherItemResolver.supports(billItem)) {
			RhipVoucherProcedure rpmProcedure = rpmVoucherItemResolver.resolve(globalBill, consommation, billItem,
					admission, insuranceType);
			if (rpmProcedure != null) {
				if (isMedicamentService(billItem.getService())) {
					String instructions = resolveBillingItemInstructions(billItem);
					rpmProcedure.setInstructions(StringUtils.defaultIfBlank(rpmProcedure.getInstructions(), instructions));
					applyCombinedPrescriptionDetails(rpmProcedure, billItem.getDrugFrequency(), instructions);
				}
				return rpmProcedure;
			}
		}
		return toProcedure(billItem, admission);
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
		if (isMedicamentService(service)) {
			String instructions = resolveBillingItemInstructions(billItem);
			procedure.setFrequency(StringUtils.trimToNull(billItem.getDrugFrequency()));
			procedure.setInstructions(instructions);
			applyCombinedPrescriptionDetails(procedure, billItem.getDrugFrequency(), instructions);
		}
		return procedure;
	}

	private void normalizeMmiMedicineDetails(RhipVoucherRequest request) {
		if (request == null || !isMmiInsuranceType(request.getInsuranceType()) || request.getProcedures() == null) {
			return;
		}
		for (RhipVoucherProcedure procedure : request.getProcedures()) {
			if (isMmiMedicineProcedure(procedure)) {
				applyCombinedPrescriptionDetails(procedure, procedure.getFrequency(), procedure.getInstructions(),
						procedure.getPosology());
				procedure.setFrequency(normalizeRhipFrequency(procedure.getFrequency()));
			}
		}
	}

	private void applyCombinedPrescriptionDetails(RhipVoucherProcedure procedure, String... prescriptionTexts) {
		if (procedure == null) {
			return;
		}
		String dose = extractPrescriptionValue(PRESCRIPTION_DOSE_PATTERN, prescriptionTexts);
		String route = extractPrescriptionValue(PRESCRIPTION_ROUTE_PATTERN, prescriptionTexts);
		if (StringUtils.isBlank(procedure.getPosology()) && StringUtils.isNotBlank(dose)) {
			procedure.setPosology(StringUtils.trimToNull(dose + (StringUtils.isBlank(route) ? "" : " " + route)));
		}

		String frequency = extractPrescriptionValue(PRESCRIPTION_FREQUENCY_PATTERN, prescriptionTexts);
		if (StringUtils.isNotBlank(frequency)) {
			procedure.setFrequency(normalizeRhipFrequency(frequency));
		}
		if (procedure.getDurationDays() == null) {
			procedure.setDurationDays(extractDurationDays(prescriptionTexts));
		}
	}

	private String normalizeRhipFrequency(String frequency) {
		String value = StringUtils.trimToNull(frequency);
		if (value == null) {
			return null;
		}
		String normalized = value.toUpperCase(Locale.ENGLISH).replaceAll("[^A-Z0-9]+", "_")
				.replaceAll("^_+|_+$", "");
		if ("QD".equals(normalized) || "ONCE_A_DAY".equals(normalized) || "ONCE_DAILY".equals(normalized)
				|| "DAILY".equals(normalized) || "EVERY_DAY".equals(normalized) || "OD".equals(normalized)) {
			return "QD";
		}
		if ("BID".equals(normalized) || "TWICE_A_DAY".equals(normalized)
				|| "TWO_TIMES_A_DAY".equals(normalized) || "TWICE_DAILY".equals(normalized)) {
			return "BID";
		}
		if ("TID".equals(normalized) || "THREE_A_DAY".equals(normalized)
				|| "THREE_TIMES_A_DAY".equals(normalized) || "THRICE_A_DAY".equals(normalized)
				|| "EVERY_8_HOURS".equals(normalized) || "Q8H".equals(normalized)) {
			return "TID";
		}
		if ("QID".equals(normalized) || "FOUR_TIMES_A_DAY".equals(normalized)
				|| "FOUR_A_DAY".equals(normalized)) {
			return "QID";
		}
		if ("FIVE_TIMES_A_DAY".equals(normalized) || "FIVE_A_DAY".equals(normalized)) {
			return "FIVE_TIMES_A_DAY";
		}
		if ("Q4H".equals(normalized) || "EVERY_4_HOURS".equals(normalized)) {
			return "Q4H";
		}
		if ("Q6H".equals(normalized) || "EVERY_6_HOURS".equals(normalized)) {
			return "Q6H";
		}
		if ("QOD".equals(normalized) || "EVERY_OTHER_DAY".equals(normalized)
				|| "ALTERNATE_DAYS".equals(normalized)) {
			return "QOD";
		}
		if ("QHS".equals(normalized) || "AT_BEDTIME".equals(normalized)
				|| "EVERY_NIGHT".equals(normalized)) {
			return "QHS";
		}
		if ("PRN".equals(normalized) || "AS_NEEDED".equals(normalized)
				|| "WHEN_NEEDED".equals(normalized)) {
			return "PRN";
		}
		if ("ONCE_A_WEEK".equals(normalized) || "WEEKLY".equals(normalized)) {
			return "ONCE_A_WEEK";
		}
		if ("CUSTOM_HOURS".equals(normalized)) {
			return "CUSTOM_HOURS";
		}
		return null;
	}

	private String extractPrescriptionValue(Pattern pattern, String... prescriptionTexts) {
		if (pattern == null || prescriptionTexts == null) {
			return null;
		}
		for (String text : prescriptionTexts) {
			if (StringUtils.isBlank(text)) {
				continue;
			}
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				return StringUtils.trimToNull(matcher.group(1));
			}
		}
		return null;
	}

	private Integer extractDurationDays(String... prescriptionTexts) {
		if (prescriptionTexts == null) {
			return null;
		}
		for (String text : prescriptionTexts) {
			if (StringUtils.isBlank(text)) {
				continue;
			}
			Matcher matcher = PRESCRIPTION_DURATION_PATTERN.matcher(text);
			if (!matcher.find()) {
				continue;
			}
			BigDecimal duration = new BigDecimal(matcher.group(1));
			String units = matcher.group(2).toLowerCase();
			if (units.startsWith("week") || units.equals("wk") || units.equals("wks")) {
				duration = duration.multiply(BigDecimal.valueOf(7));
			} else if (units.startsWith("month") || units.equals("mo") || units.equals("mos")) {
				duration = duration.multiply(BigDecimal.valueOf(30));
			} else if (units.startsWith("hour") || units.equals("h") || units.equals("hr") || units.equals("hrs")) {
				duration = duration.divide(BigDecimal.valueOf(24), 10, RoundingMode.CEILING);
			} else if (!units.startsWith("day") && !units.equals("d")) {
				return null;
			}
			return duration.setScale(0, RoundingMode.CEILING).intValue();
		}
		return null;
	}

	private String resolveBillingItemInstructions(PatientServiceBill billItem) {
		if (billItem == null) {
			return null;
		}
		String description = StringUtils.trimToNull(billItem.getServiceOtherDescription());
		if (description != null) {
			return description;
		}
		return StringUtils.trimToNull(billItem.getDrugFrequency());
	}

	private List<RhipVoucherProcedure> rejectNonPositivePriceProcedures(GlobalBill globalBill, RhipVoucherRequest request) {
		if (request == null || !requiresProcedurePrice(request.getInsuranceType()) || request.getProcedures() == null) {
			return Collections.emptyList();
		}
		List<RhipVoucherProcedure> acceptedProcedures = new ArrayList<RhipVoucherProcedure>();
		List<RhipVoucherProcedure> rejectedProcedures = new ArrayList<RhipVoucherProcedure>();
		for (RhipVoucherProcedure procedure : request.getProcedures()) {
			if (procedure == null) {
				continue;
			}
			if (procedure.getPrice() == null || procedure.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
				rejectedProcedures.add(procedure);
			} else {
				acceptedProcedures.add(procedure);
			}
		}
		if (!rejectedProcedures.isEmpty()) {
			request.setProcedures(acceptedProcedures);
			Map<String, String> rejectionReasonsByCode = new HashMap<String, String>();
			for (RhipVoucherProcedure procedure : rejectedProcedures) {
				if (procedure != null && StringUtils.isNotBlank(procedure.getCode())) {
					rejectionReasonsByCode.put(procedure.getCode().trim(), "Price must be a positive number");
				}
			}
			persistVoucherItemRecords(globalBill, rejectedProcedures, RhipVoucherItemRecord.STATUS_REJECTED,
					null, null, rejectionReasonsByCode);
		}
		return rejectedProcedures;
	}

	private IntegrationResponse handlePartialVoucherSubmission(GlobalBill globalBill, RhipVoucherRequest originalRequest,
	                                                          IntegrationResponse firstResponse) {
		if (globalBill == null || originalRequest == null || firstResponse == null
				|| Boolean.TRUE.equals(isSuccessResponse(firstResponse))) {
			return firstResponse;
		}
		VoucherValidationResult validationResult = extractVoucherValidationResult(firstResponse);
		if (validationResult == null || validationResult.validByCode.isEmpty() || validationResult.invalidByCode.isEmpty()) {
			return firstResponse;
		}
		List<RhipVoucherProcedure> validProcedures = new ArrayList<RhipVoucherProcedure>();
		List<RhipVoucherProcedure> invalidProcedures = new ArrayList<RhipVoucherProcedure>();
		List<RhipVoucherProcedure> procedures = originalRequest.getProcedures();
		if (procedures != null) {
			for (RhipVoucherProcedure procedure : procedures) {
				if (procedure == null || StringUtils.isBlank(procedure.getCode())) {
					continue;
				}
				String code = procedure.getCode().trim();
				if (validationResult.invalidByCode.containsKey(code)) {
					invalidProcedures.add(procedure);
				} else if (validationResult.validByCode.containsKey(code)) {
					validProcedures.add(procedure);
				}
			}
		}
		if (validProcedures.isEmpty()) {
			persistVoucherItemRecords(globalBill, invalidProcedures, RhipVoucherItemRecord.STATUS_REJECTED,
					null, null, validationResult.invalidByCode);
			return firstResponse;
		}

		RhipVoucherRequest retryRequest = copyVoucherRequest(originalRequest);
		retryRequest.setProcedures(validProcedures);
		IntegrationResponse retryResponse = submitVoucher(retryRequest);
		if (Boolean.TRUE.equals(isSuccessResponse(retryResponse))) {
			VoucherIdentifiers identifiers = extractVoucherIdentifiers(retryResponse);
			persistVoucherItemRecords(globalBill, validProcedures, RhipVoucherItemRecord.STATUS_SENT,
					identifiers.voucherCode, identifiers.voucherReferenceNumber, null);
			persistVoucherItemRecords(globalBill, invalidProcedures, RhipVoucherItemRecord.STATUS_REJECTED,
					null, null, validationResult.invalidByCode);
			log.info("RHIP voucher submitted partially for global bill " + globalBill.getGlobalBillId()
					+ ": sent=" + validProcedures.size() + ", rejected=" + invalidProcedures.size());
			return retryResponse;
		}
		persistVoucherItemRecords(globalBill, invalidProcedures, RhipVoucherItemRecord.STATUS_REJECTED,
				null, null, validationResult.invalidByCode);
		return retryResponse;
	}

	private VoucherValidationResult extractVoucherValidationResult(IntegrationResponse response) {
		JsonNode root = toJsonNode(response == null ? null : response.getResponseEntity());
		JsonNode errors = root == null ? null : root.get("errors");
		if (errors == null || !errors.isArray()) {
			return null;
		}
		VoucherValidationResult result = new VoucherValidationResult();
		for (JsonNode error : errors) {
			if (error == null || error.isNull()) {
				continue;
			}
			String productId = jsonText(error.get("productId"));
			if (StringUtils.isBlank(productId)) {
				continue;
			}
			String code = productId.trim();
			String message = jsonText(error.get("message"));
			boolean isValid = error.has("isValid") && error.get("isValid").asBoolean(false);
			if (isValid) {
				result.validByCode.put(code, StringUtils.defaultIfBlank(message, "Prescription is valid"));
			} else {
				result.invalidByCode.put(code, StringUtils.defaultIfBlank(message, "Rejected by RHIP"));
			}
		}
		return result.validByCode.isEmpty() && result.invalidByCode.isEmpty() ? null : result;
	}

	private JsonNode toJsonNode(Object entity) {
		if (entity == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.readTree(entity.toString());
		} catch (Exception ignored) {
			return null;
		}
	}

	private RhipVoucherRequest copyVoucherRequest(RhipVoucherRequest source) {
		RhipVoucherRequest copy = new RhipVoucherRequest();
		copy.setInsuranceType(source.getInsuranceType());
		copy.setFacilityFosaId(source.getFacilityFosaId());
		copy.setPatientIdentifier(source.getPatientIdentifier());
		copy.setReceptionNumber(source.getReceptionNumber());
		copy.setProcedures(source.getProcedures());
		copy.setUserAccountCode(source.getUserAccountCode());
		copy.setProcessedBy(source.getProcessedBy());
		copy.setNotes(source.getNotes());
		copy.setPractitionerLicenseNumber(source.getPractitionerLicenseNumber());
		copy.setPatientType(source.getPatientType());
		copy.setHealthCareStayType(source.getHealthCareStayType());
		copy.setAdmissionDate(source.getAdmissionDate());
		copy.setDischargeDate(source.getDischargeDate());
		copy.setTreatmentForNewBorn(source.getTreatmentForNewBorn());
		copy.setDiagnosisIds(source.getDiagnosisIds());
		copy.setReferralFacilityId(source.getReferralFacilityId());
		copy.setPatientPhoneNumber(source.getPatientPhoneNumber());
		copy.setPrescriptionDestination(source.getPrescriptionDestination());
		copy.setVisitReferenceNumber(source.getVisitReferenceNumber());
		return copy;
	}

	private VoucherIdentifiers extractVoucherIdentifiers(IntegrationResponse response) {
		VoucherIdentifiers identifiers = new VoucherIdentifiers();
		JsonNode root = toJsonNode(response == null ? null : response.getResponseEntity());
		JsonNode data = root == null ? null : root.get("data");
		identifiers.voucherCode = jsonText(data == null ? null : data.get("voucherCode"));
		identifiers.voucherReferenceNumber = jsonText(data == null ? null : data.get("voucherReferenceNumber"));
		return identifiers;
	}

	private void persistVoucherItemRecords(GlobalBill globalBill, List<RhipVoucherProcedure> procedures, String status,
	                                       String voucherCode, String voucherReferenceNumber,
	                                       Map<String, String> rejectionReasonsByCode) {
		if (billingService == null || globalBill == null || procedures == null || procedures.isEmpty()) {
			return;
		}
		User currentUser = null;
		try {
			currentUser = Context.getAuthenticatedUser();
		} catch (Exception ignored) {
		}
		for (RhipVoucherProcedure procedure : procedures) {
			if (procedure == null || procedure.getPatientServiceBillId() == null || StringUtils.isBlank(procedure.getCode())) {
				continue;
			}
			try {
				RhipVoucherItemRecord record = new RhipVoucherItemRecord();
				record.setGlobalBill(globalBill);
				PatientServiceBill item = new PatientServiceBill();
				item.setPatientServiceBillId(procedure.getPatientServiceBillId());
				record.setPatientServiceBill(item);
				record.setProductCode(procedure.getCode().trim());
				record.setQuantity(procedure.getQuantity());
				record.setPrice(procedure.getPrice());
				record.setStatus(status);
				record.setVoucherCode(voucherCode);
				record.setVoucherReferenceNumber(voucherReferenceNumber);
				if (rejectionReasonsByCode != null) {
					record.setRejectionReason(rejectionReasonsByCode.get(procedure.getCode().trim()));
				}
				record.setDateCreated(new Date());
				record.setCreator(currentUser);
				record.setUuid(UUID.randomUUID().toString());
				billingService.saveRhipVoucherItemRecord(record);
			} catch (Exception e) {
				log.warn("Unable to persist RHIP voucher item record for product " + procedure.getCode(), e);
			}
		}
	}

	private static class VoucherValidationResult {
		private final Map<String, String> validByCode = new HashMap<String, String>();
		private final Map<String, String> invalidByCode = new HashMap<String, String>();
	}

	private static class VoucherIdentifiers {
		private String voucherCode;
		private String voucherReferenceNumber;
	}

	private boolean isMedicamentService(BillableService service) {
		if (service == null || service.getServiceCategory() == null) {
			return false;
		}
		String categoryName = service.getServiceCategory().getName();
		return StringUtils.isNotBlank(categoryName)
		        && MEDICAMENTS_SERVICE_CATEGORY.equalsIgnoreCase(categoryName.trim());
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
		String trimmedName = name.trim();
		Matcher matcher = DASH_SEPARATED_PROCEDURE_CODE.matcher(trimmedName);
		if (matcher.matches()) {
			return matcher.group(1).trim();
		}
		return trimmedName;
	}

	private String resolveDiagnosisNotes(Patient patient, Admission admission, GlobalBill globalBill) {
		if (billingService == null) {
			return null;
		}
		String conceptIds = resolveDiagnosisConceptIds();
		if (StringUtils.isBlank(conceptIds) || patient == null) {
			return null;
		}
		Date admissionDate = getStartOfDay(resolveAdmissionDate(globalBill, admission));
		Date dischargeDate = getEndOfDay(resolveDischargeDate(globalBill, admission));
		if (admissionDate == null || dischargeDate == null) {
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

	private String resolveReferralFacilityId(GlobalBill globalBill) {
		return config == null ? null : StringUtils.trimToNull(config.getDefaultReferralFacilityId());
	}

	private String resolveRamaVisitReferenceNumber(GlobalBill globalBill, String fosaId) {
		if (globalBill == null || StringUtils.isBlank(globalBill.getBillIdentifier()) || StringUtils.isBlank(fosaId)) {
			return null;
		}
		return globalBill.getBillIdentifier().trim() + fosaId.trim();
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

	private Date getStartOfDay(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private Date getEndOfDay(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
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

	public RpmVoucherItemResolver getRpmVoucherItemResolver() {
		return rpmVoucherItemResolver;
	}

	public void setRpmVoucherItemResolver(RpmVoucherItemResolver rpmVoucherItemResolver) {
		this.rpmVoucherItemResolver = rpmVoucherItemResolver;
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
}
