package org.openmrs.module.mohbilling.integration.insurance;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
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
import org.openmrs.module.mohbilling.model.PatientBill;
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
import java.util.HashSet;
import java.util.Set;

public class RhipVoucherService {

	private static final Log log = LogFactory.getLog(RhipVoucherService.class);
	private static final String CBHI_INSURANCE_TYPE = "CBHI";
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private RhipVoucherProvider voucherProvider;
	private RhipVoucherIntegrationConfig config;
	private BillingService billingService;
	private ProviderService providerService;
	private PersonService personService;
	private LocationService locationService;
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
		if (StringUtils.isBlank(attributeTypeUuid)) {
			return null;
		}
		ProviderAttributeType attributeType = providerService.getProviderAttributeTypeByUuid(attributeTypeUuid);
		if (attributeType == null) {
			log.warn("Unable to find provider attribute type for license with uuid {}");
			return null;
		}
		for (Provider provider : providerService.getProvidersByPerson(user.getPerson(), false)) {
			for (ProviderAttribute attribute : provider.getAttributes()) {
				if (attribute != null && attributeType.equals(attribute.getAttributeType())
						&& StringUtils.isNotBlank(attribute.getValueReference())) {
					return attribute.getValueReference();
				}
			}
		}
		return null;
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
		return isAdmitted(admission) ? "IN_PATIENT" : "OUT_PATIENT";
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
		if (patient == null) {
			return null;
		}
		String phoneNumber = resolvePatientBillPhoneNumber(globalBill);
		if (StringUtils.isNotBlank(phoneNumber)) {
			return phoneNumber;
		}
		if (conceptService == null) {
			return null;
		}
		String epaymentPhoneNumberConceptRef = Context.getAdministrationService().getGlobalProperty("registration.ePaymentPhoneNumberConcept");
		if (StringUtils.isBlank(epaymentPhoneNumberConceptRef)) {
			log.warn("registration.ePaymentPhoneNumberConcept global property is not configured");
			return null;
		}
		Concept epaymentPhoneNumberConcept = getConcept(epaymentPhoneNumberConceptRef);
		if (epaymentPhoneNumberConcept == null) {
			log.warn("registration.ePaymentPhoneNumberConcept not found: {}");
			return null;
		}
		List<Obs> currentPhoneNumbers = Utils.getLastNObservations(1, patient, epaymentPhoneNumberConcept, false);
		String currentPhoneNumber = null;
		if (!currentPhoneNumbers.isEmpty()) {
			currentPhoneNumber = currentPhoneNumbers.get(0).getValueText();
		}
		return StringUtils.isBlank(currentPhoneNumber) ? null : currentPhoneNumber;
	}

	private String resolvePatientBillPhoneNumber(GlobalBill globalBill) {
		if (billingService == null || globalBill == null) {
			return null;
		}
		List<Consommation> consommations = billingService.getAllConsommationByGlobalBill(globalBill);
		if (consommations == null) {
			return null;
		}
		for (Consommation consommation : consommations) {
			PatientBill patientBill = consommation == null ? null : consommation.getPatientBill();
			if (patientBill != null && StringUtils.isNotBlank(patientBill.getPhoneNumber())) {
				return patientBill.getPhoneNumber();
			}
		}
		return null;
	}

	private Concept getConcept(String conceptRef) {
		String trimmed = conceptRef.trim();
		Concept concept = conceptService.getConceptByUuid(trimmed);
		if (concept == null) {
			concept = conceptService.getConcept(trimmed);
		}
		return concept;
	}

	private String resolveFosaId(GlobalBill globalBill) {
		Set<Location> candidateLocations = new HashSet<>();
		List<Consommation> consommations = billingService == null ? null : billingService.getAllConsommationByGlobalBill(globalBill);
		if (consommations != null) {
			for (Consommation consommation : consommations) {
				if (consommation == null || consommation.getBillItems() == null) {
					continue;
				}
				for (PatientServiceBill billItem : consommation.getBillItems()) {
					BillableService service = billItem == null ? null : billItem.getService();
					FacilityServicePrice facilityServicePrice = service == null ? null : service.getFacilityServicePrice();
					Location location = facilityServicePrice == null ? null : facilityServicePrice.getLocation();
					if (location != null) {
						candidateLocations.add(location);
					}
				}
			}
		}
		String fosaId = resolveFosaIdFromLocations(candidateLocations);
		if (StringUtils.isNotBlank(fosaId)) {
			return fosaId;
		}
		if (locationService != null) {
			Location defaultLocation = locationService.getDefaultLocation();
			if (defaultLocation != null) {
				fosaId = resolveFosaIdFromLocations(Collections.singleton(defaultLocation));
				if (StringUtils.isNotBlank(fosaId)) {
					return fosaId;
				}
			}
		}
		return config == null ? null : config.getDefaultFosaId();
	}

	private String resolveFosaIdFromLocations(Set<Location> locations) {
		if (locations == null || locations.isEmpty() || locationService == null || config == null) {
			return null;
		}
		String attributeTypeUuid = config.getFosaIdAttributeTypeUuid();
		if (StringUtils.isBlank(attributeTypeUuid)) {
			return null;
		}
		LocationAttributeType attributeType = locationService.getLocationAttributeTypeByUuid(attributeTypeUuid);
		if (attributeType == null) {
			log.warn("No FOSA ID location attribute type found for uuid {}");
			return null;
		}
		Set<String> fosaIdsFound = new HashSet<>();
		for (Location location : locations) {
			for (LocationAttribute attribute : location.getActiveAttributes()) {
				if (attributeType.equals(attribute.getAttributeType()) && StringUtils.isNotBlank(attribute.getValueReference())) {
					fosaIdsFound.add(attribute.getValueReference());
				}
			}
		}
		if (fosaIdsFound.size() == 1) {
			return fosaIdsFound.iterator().next();
		}
		if (fosaIdsFound.size() > 1) {
			log.warn("Multiple FOSA IDs are found across candidate locations");
		} else {
			log.warn("No FOSA IDs are associated with candidate locations");
		}
		return null;
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

	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}

	public void setConceptService(ConceptService conceptService) {
		this.conceptService = conceptService;
	}
}
