package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherIntegrationConfig;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillReportFacilityHeader;
import org.openmrs.module.mohbilling.model.BillReportItem;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds CBHI Bill Report rows from admissions in a date range.
 */
public final class BillReportUtil {

	private static final Pattern ICD_CODE_PATTERN = Pattern.compile("^(?=.*\\d)[A-Za-z0-9.]+$");
	private static final Pattern ICD_CODE_WITH_NAME_PATTERN = Pattern.compile("([A-Za-z0-9.]+)\\s*-");

	/**
	 * Temporary test limit: only process the first N distinct patients in the date range.
	 * Raise or remove after diagnosis lookup is verified/optimized.
	 */
	private static final int TEMP_MAX_PATIENTS_FOR_REPORT = 10;

	private static final Log log = LogFactory.getLog(BillReportUtil.class);

	private BillReportUtil() {
	}

	private static BillingService getService() {
		return Context.getService(BillingService.class);
	}

	/** Clinical diagnosis fields resolved for one admission (shared across bill-item rows). */
	public static final class ClinicalDiagnosis {
		private final String primaryDiagnosis;
		private final String confirmedByLabImaging;
		private final String icd11Code;

		public ClinicalDiagnosis(String primaryDiagnosis, String confirmedByLabImaging, String icd11Code) {
			this.primaryDiagnosis = StringUtils.defaultString(primaryDiagnosis);
			this.confirmedByLabImaging = StringUtils.defaultString(confirmedByLabImaging);
			this.icd11Code = StringUtils.defaultString(icd11Code);
		}

		public String getPrimaryDiagnosis() {
			return primaryDiagnosis;
		}

		public String getConfirmedByLabImaging() {
			return confirmedByLabImaging;
		}

		public String getIcd11Code() {
			return icd11Code;
		}

		public static ClinicalDiagnosis empty() {
			return new ClinicalDiagnosis("", "", "");
		}
	}

	public static BillReportFacilityHeader buildFacilityHeader() {
		BillReportFacilityHeader header = new BillReportFacilityHeader();
		Location location = resolveReportLocation();

		if (location != null) {
			header.setHfCode(resolveHfCode(location));
			header.setFacilityName(StringUtils.defaultString(location.getName()));
			header.setFacilityType(getLocationAttributeValue(location,
					BillingConstants.LOCATION_ATTR_FACILITY_TYPE_UUID, "Facility Type"));
			header.setFacilityCategory(getLocationAttributeValue(location,
					BillingConstants.LOCATION_ATTR_FACILITY_CATEGORY_UUID, "Facility Category"));
			header.setFacilityLevelOfCare(getLocationAttributeValue(location,
					BillingConstants.LOCATION_ATTR_LEVEL_OF_CARE_UUID, "Level of Care"));
			header.setFacilityOwnershipType(getLocationAttributeValue(location,
					BillingConstants.LOCATION_ATTR_OWNER_TYPE_UUID, "Owner Type"));
			// Geographic fields come from OpenMRS Location address columns
			header.setProvinceName(StringUtils.defaultString(location.getStateProvince()));
			header.setDistrictName(StringUtils.defaultString(location.getCountyDistrict()));
			header.setSectorName(StringUtils.defaultString(location.getCityVillage()));
		}

		if (StringUtils.isBlank(header.getHfCode())) {
			header.setHfCode(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE));
		}
		return header;
	}

	/**
	 * Saves one Facility settings field onto the report location as a location attribute.
	 * Returns the stored value.
	 */
	public static String saveFacilitySetting(String fieldKey, String value) {
		if (StringUtils.isBlank(fieldKey)) {
			throw new IllegalArgumentException("fieldKey is required");
		}
		Location location = resolveReportLocation();
		if (location == null) {
			throw new IllegalStateException("No location configured for Bill Report facility settings");
		}
		String trimmed = value != null ? value.trim() : "";
		String key = fieldKey.trim();

		if ("hfCode".equals(key)) {
			saveLocationAttribute(location, null, BillingConstants.LOCATION_ATTR_FOSA_ID_NAME, trimmed);
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE, trimmed);
		} else if ("facilityType".equals(key)) {
			saveLocationAttribute(location, BillingConstants.LOCATION_ATTR_FACILITY_TYPE_UUID, "Facility Type",
					trimmed);
		} else if ("facilityCategory".equals(key)) {
			saveLocationAttribute(location, BillingConstants.LOCATION_ATTR_FACILITY_CATEGORY_UUID,
					"Facility Category", trimmed);
		} else if ("facilityLevelOfCare".equals(key)) {
			saveLocationAttribute(location, BillingConstants.LOCATION_ATTR_LEVEL_OF_CARE_UUID, "Level of Care",
					trimmed);
		} else if ("facilityOwnershipType".equals(key)) {
			saveLocationAttribute(location, BillingConstants.LOCATION_ATTR_OWNER_TYPE_UUID, "Owner Type", trimmed);
		} else {
			throw new IllegalArgumentException("Unsupported facility setting: " + fieldKey);
		}
		return trimmed;
	}

	/** UPID if present, otherwise Primary Care ID (billing.primaryIdentifierType). */
	public static String resolvePatientId(Patient patient) {
		if (patient == null) {
			return "";
		}
		String upid = getIdentifierByTypeNames(patient, "UPID");
		if (StringUtils.isNotBlank(upid)) {
			return upid;
		}
		try {
			PatientIdentifierType primaryType = InsurancePolicyUtil.getPrimaryPatientIdentiferType();
			if (primaryType != null) {
				PatientIdentifier identifier = patient.getPatientIdentifier(primaryType);
				if (identifier != null && StringUtils.isNotBlank(identifier.getIdentifier())) {
					return identifier.getIdentifier();
				}
			}
		} catch (Exception ignored) {
			// fall through
		}
		String primaryCare = getIdentifierByTypeNames(patient, "Primary Care ID", "Primary Care");
		return StringUtils.defaultString(primaryCare);
	}

	/** NID if present, otherwise Application Number. */
	public static String resolvePatientNid(Patient patient) {
		if (patient == null) {
			return "";
		}
		String nid = getIdentifierByTypeNames(patient, "NID", "National ID", "National Identifier");
		if (StringUtils.isNotBlank(nid)) {
			return nid;
		}
		return StringUtils.defaultString(
				getIdentifierByTypeNames(patient, "Application Number", "NID Application Number"));
	}

	public static String resolvePatientPhone(Patient patient) {
		if (patient == null) {
			return "";
		}
		String fromGp = resolvePhone(patient);
		if (StringUtils.isNotBlank(fromGp)) {
			return fromGp;
		}
		String phoneUuid = new RhipVoucherIntegrationConfig().getPatientPhoneAttributeTypeUuid();
		if (StringUtils.isNotBlank(phoneUuid)) {
			PersonAttributeType type = Context.getPersonService().getPersonAttributeTypeByUuid(phoneUuid);
			if (type != null) {
				PersonAttribute attribute = patient.getAttribute(type);
				if (attribute != null && StringUtils.isNotBlank(attribute.getValue())) {
					return attribute.getValue();
				}
			}
		}
		return StringUtils.defaultString(getPersonAttributeByTypeNames(patient, "Phone Number", "Telephone Number",
				"Telephone", "phone number"));
	}

	public static String resolveProviderName(User creator) {
		if (creator == null || creator.getPersonName() == null) {
			return "";
		}
		return StringUtils.defaultString(creator.getPersonName().getFullName());
	}

	public static String resolveProviderQualification(User creator) {
		return resolveProviderAttributeByNamesOrUuid(creator, null, "Qualification");
	}

	public static String resolveProviderPhone(User creator) {
		RhipVoucherIntegrationConfig config = new RhipVoucherIntegrationConfig();
		return resolveProviderAttributeByNamesOrUuid(creator, config.getPractitionerPhoneProviderAttributeTypeUuid(),
				"Phone Number", "Telephone Number", "Telephone", "Provider Phone");
	}

	public static String resolveProviderRegistration(User creator) {
		RhipVoucherIntegrationConfig config = new RhipVoucherIntegrationConfig();
		return resolveProviderAttributeByNamesOrUuid(creator, config.getProviderLicenseAttributeTypeUuid(),
				"License", "Provider License", "License Number");
	}

	/** Household NID from moh_bill_beneficiary.owner_code. */
	public static String resolveHouseholdNid(InsurancePolicy policy) {
		if (policy == null) {
			return "";
		}
		try {
			if (StringUtils.isNotBlank(policy.getInsuranceCardNo())) {
				Beneficiary byCard = InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(policy.getInsuranceCardNo());
				if (byCard != null && StringUtils.isNotBlank(byCard.getOwnerCode())) {
					return byCard.getOwnerCode();
				}
			}
		} catch (Exception ignored) {
			// fall through to beneficiaries on policy
		}
		if (policy.getBeneficiaries() != null) {
			for (Beneficiary beneficiary : policy.getBeneficiaries()) {
				if (beneficiary != null && !Boolean.TRUE.equals(beneficiary.isRetired())
						&& StringUtils.isNotBlank(beneficiary.getOwnerCode())) {
					return beneficiary.getOwnerCode();
				}
			}
		}
		return "";
	}

	/**
	 * Builds UI rows for Bill Report: one row per non-voided patient service bill
	 * under each admission's consommations. If the bill has no consommation (or no
	 * bill items), still emit one admission-level row with blank product fields.
	 */
	public static List<Map<String, Object>> buildAdmissionDisplayRows(Date startDate, Date endDate,
			Insurance insurance) {
		List<Admission> admissions = getService().getAdmissionsByAdmissionDate(startDate, endDate, insurance);
		if (admissions == null || admissions.isEmpty()) {
			return Collections.emptyList();
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		Set<Integer> includedPatientIds = new LinkedHashSet<>();
		for (Admission admission : admissions) {
			if (admission == null || Boolean.TRUE.equals(admission.getVoided())) {
				continue;
			}

			Patient patientForDx = admission.getInsurancePolicy() != null
					? admission.getInsurancePolicy().getOwner()
					: null;
			Integer patientId = patientForDx != null ? patientForDx.getPatientId() : null;
			// Temporary: only first N distinct patients (keep all their bill items)
			if (patientId != null) {
				if (!includedPatientIds.contains(patientId)
						&& includedPatientIds.size() >= TEMP_MAX_PATIENTS_FOR_REPORT) {
					continue;
				}
				includedPatientIds.add(patientId);
			} else if (includedPatientIds.size() >= TEMP_MAX_PATIENTS_FOR_REPORT) {
				continue;
			}

			GlobalBill globalBill = getService().getGlobalBillByAdmission(admission);
			List<Consommation> consommations = globalBill != null
					? getService().getAllConsommationByGlobalBill(globalBill)
					: null;
			if (consommations == null) {
				consommations = Collections.emptyList();
			}

			ClinicalDiagnosis clinicalDiagnosis = resolveClinicalDiagnosis(admission, globalBill, patientForDx);

			boolean addedBillItemRow = false;
			for (Consommation consommation : consommations) {
				if (consommation == null || Boolean.TRUE.equals(consommation.getVoided())) {
					continue;
				}
				Set<PatientServiceBill> items = consommation.getBillItems();
				if (items == null || items.isEmpty()) {
					continue;
				}
				for (PatientServiceBill item : items) {
					if (item == null || Boolean.TRUE.equals(item.getVoided())) {
						continue;
					}
					rows.add(toDisplayRow(admission, globalBill, consommation, item, clinicalDiagnosis));
					addedBillItemRow = true;
				}
			}

			// No consommation / no bill items yet — still show the bill for now
			if (!addedBillItemRow) {
				rows.add(toDisplayRow(admission, globalBill, null, null, clinicalDiagnosis));
			}
		}
		return rows;
	}

	private static Map<String, Object> toDisplayRow(Admission admission, GlobalBill globalBill,
			Consommation consommation, PatientServiceBill item, ClinicalDiagnosis clinicalDiagnosis) {
		Map<String, Object> row = new HashMap<>();
		InsurancePolicy policy = admission.getInsurancePolicy();
		Patient patient = null;
		if (consommation != null && consommation.getBeneficiary() != null) {
			patient = consommation.getBeneficiary().getPatient();
		}
		if (patient == null && policy != null) {
			patient = policy.getOwner();
		}
		if (clinicalDiagnosis == null) {
			clinicalDiagnosis = ClinicalDiagnosis.empty();
		}

		row.put("admission", admission);
		row.put("policy", policy);
		row.put("patient", patient);
		row.put("cardNumber", policy != null ? policy.getInsuranceCardNo() : null);
		row.put("insuranceName",
				policy != null && policy.getInsurance() != null ? policy.getInsurance().getName() : null);
		row.put("globalBill", globalBill);
		row.put("consommation", consommation);
		row.put("billItem", item);
		row.put("voucherId", globalBill != null ? globalBill.getBillIdentifier() : null);
		row.put("dischargeDate", globalBill != null ? globalBill.getClosingDate() : null);
		row.put("admissionStatus", Boolean.TRUE.equals(admission.getIsAdmitted()) ? "Inpatient" : "Outpatient");
		row.put("patientId", resolvePatientId(patient));
		row.put("patientNid", resolvePatientNid(patient));
		row.put("patientDob", patient != null ? patient.getBirthdate() : null);
		row.put("householdNid", resolveHouseholdNid(policy));
		row.put("socialRegistryLevel", "");
		row.put("patientPhone", resolvePatientPhone(patient));
		row.put("patientGender", patient != null ? patient.getGender() : null);

		User billCreator = globalBill != null ? globalBill.getCreator() : null;
		row.put("providerName", resolveProviderName(billCreator));
		row.put("providerQualification", resolveProviderQualification(billCreator));
		row.put("providerPhone", resolveProviderPhone(billCreator));
		row.put("providerRegistration", resolveProviderRegistration(billCreator));

		String productName = "";
		String productCode = "";
		String productType = "";
		BigDecimal productCost = null;
		BigDecimal coPayment = null;
		BigDecimal rssbPayment = null;
		BigDecimal totalInvoice = null;
		if (item != null) {
			FacilityServicePrice fsp = null;
			BillableService billable = item.getService();
			if (billable != null) {
				fsp = billable.getFacilityServicePrice();
			}
			if (fsp != null) {
				productName = StringUtils.defaultString(fsp.getName());
				productType = resolveProductType(item, fsp);
			} else if (item.getHopService() != null) {
				productName = StringUtils.defaultString(item.getHopService().getName());
				productType = productName;
			} else if (StringUtils.isNotBlank(item.getServiceOther())) {
				productName = item.getServiceOther();
			}
			if (StringUtils.isBlank(productType) && item.getHopService() != null) {
				productType = StringUtils.defaultString(item.getHopService().getName());
			}

			BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
			BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
			productCost = unitPrice.setScale(2, RoundingMode.HALF_UP);
			totalInvoice = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

			BigDecimal insuranceRatePercent = resolveInsuranceRatePercent(admission, globalBill);
			// Co-payment share = (100 - insuranceRate)% of total product cost
			BigDecimal coPayPercent = BigDecimal.valueOf(100).subtract(
					insuranceRatePercent != null ? insuranceRatePercent : BigDecimal.ZERO);
			coPayment = totalInvoice.multiply(coPayPercent)
					.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			rssbPayment = totalInvoice.subtract(coPayment).setScale(2, RoundingMode.HALF_UP);
		}
		row.put("primaryDiagnosis", clinicalDiagnosis.getPrimaryDiagnosis());
		row.put("confirmedByLabImaging", clinicalDiagnosis.getConfirmedByLabImaging());
		row.put("icd11Code", clinicalDiagnosis.getIcd11Code());
		row.put("productName", productName);
		row.put("productCode", productCode);
		row.put("productType", productType);
		row.put("productCost", productCost);
		row.put("coPayment", coPayment);
		row.put("rssbPayment", rssbPayment);
		row.put("totalInvoice", totalInvoice);
		return row;
	}

	/**
	 * Resolves primary diagnosis / confirmed-by-lab / ICD-11 for an admission.
	 * Primary diagnosis: obs for GP concept UUID within admission_date +/- 2 days.
	 */
	public static ClinicalDiagnosis resolveClinicalDiagnosis(Admission admission, GlobalBill globalBill,
			Patient patient) {
		if (admission == null || patient == null || admission.getAdmissionDate() == null) {
			return ClinicalDiagnosis.empty();
		}
		try {
			int dayWindow = BillingConstants.BILL_REPORT_DIAGNOSIS_OBS_DAY_WINDOW;
			Date rangeStart = startOfDay(addDays(admission.getAdmissionDate(), -dayWindow));
			Date rangeEnd = endOfDay(addDays(admission.getAdmissionDate(), dayWindow));

			Concept diagnosisConcept = resolvePrimaryDiagnosisConcept();
			List<String> diagnosisLabels = collectObsLabelsInRange(patient, diagnosisConcept, rangeStart, rangeEnd);

			List<Integer> confirmedConceptIds = resolveConfirmedByLabImagingConceptIds();
			String confirmed = resolveConfirmedFromDateWindow(patient, confirmedConceptIds, rangeStart, rangeEnd);

			String primaryDiagnosis = diagnosisLabels.isEmpty() ? ""
					: StringUtils.join(new LinkedHashSet<>(diagnosisLabels), ", ");
			String icd11 = extractIcd11Codes(primaryDiagnosis);
			if (StringUtils.isBlank(primaryDiagnosis)) {
				// Temporary INFO while verifying diagnosis lookup (downgrade after fix)
				log.info("Bill Report diagnosis empty: admissionId="
						+ admission.getAdmissionId() + ", patientId=" + patient.getPatientId()
						+ ", conceptUuid=" + (diagnosisConcept != null ? diagnosisConcept.getUuid() : null)
						+ ", conceptId=" + (diagnosisConcept != null ? diagnosisConcept.getConceptId() : null)
						+ ", range=" + rangeStart + ".." + rangeEnd);
			}
			return new ClinicalDiagnosis(primaryDiagnosis, confirmed, icd11);
		} catch (Exception e) {
			log.warn("Bill Report diagnosis lookup failed for admissionId="
					+ (admission != null ? admission.getAdmissionId() : null), e);
			return ClinicalDiagnosis.empty();
		}
	}

	private static Concept resolvePrimaryDiagnosisConcept() {
		String uuid = Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_PRIMARY_DIAGNOSIS_CONCEPT_UUID);
		if (StringUtils.isBlank(uuid)) {
			uuid = BillingConstants.PRIMARY_ICD_DIAGNOSIS_CONCEPT_UUID;
		}
		Concept concept = Context.getConceptService().getConceptByUuid(uuid.trim());
		if (concept == null) {
			log.warn("Bill Report primary diagnosis concept not found for uuid=" + uuid);
		}
		return concept;
	}

	private static List<String> collectObsLabelsInRange(Patient patient, Concept concept, Date rangeStart,
			Date rangeEnd) {
		List<String> labels = new ArrayList<>();
		if (patient == null || concept == null || rangeStart == null || rangeEnd == null) {
			return labels;
		}
		List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obsList == null || obsList.isEmpty()) {
			return labels;
		}
		for (Obs obs : obsList) {
			if (obs == null || Boolean.TRUE.equals(obs.getVoided()) || obs.getObsDatetime() == null) {
				continue;
			}
			if (obs.getObsDatetime().before(rangeStart) || obs.getObsDatetime().after(rangeEnd)) {
				continue;
			}
			String label = null;
			if (obs.getValueCoded() != null && obs.getValueCoded().getName() != null) {
				label = obs.getValueCoded().getName().getName();
			} else if (StringUtils.isNotBlank(obs.getValueText())) {
				label = obs.getValueText();
			}
			if (StringUtils.isNotBlank(label)) {
				labels.add(label.trim());
			}
		}
		return labels;
	}

	private static String resolveConfirmedFromDateWindow(Patient patient, List<Integer> confirmedConceptIds,
			Date rangeStart, Date rangeEnd) {
		if (patient == null || confirmedConceptIds == null || confirmedConceptIds.isEmpty()) {
			return "";
		}
		for (Integer conceptId : confirmedConceptIds) {
			Concept concept = Context.getConceptService().getConcept(conceptId);
			if (concept == null) {
				continue;
			}
			List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
			if (obsList == null) {
				continue;
			}
			for (Obs obs : obsList) {
				if (obs == null || Boolean.TRUE.equals(obs.getVoided()) || obs.getObsDatetime() == null) {
					continue;
				}
				if (obs.getObsDatetime().before(rangeStart) || obs.getObsDatetime().after(rangeEnd)) {
					continue;
				}
				String asYesNo = toYesNo(obs);
				if (StringUtils.isNotBlank(asYesNo)) {
					return asYesNo;
				}
			}
		}
		return "";
	}

	private static String toYesNo(Obs obs) {
		if (obs == null) {
			return "";
		}
		if (obs.getValueBoolean() != null) {
			return Boolean.TRUE.equals(obs.getValueBoolean()) ? "Yes" : "No";
		}
		if (obs.getValueCoded() != null && obs.getValueCoded().getName() != null) {
			String name = obs.getValueCoded().getName().getName();
			if (StringUtils.isBlank(name)) {
				return "";
			}
			if ("yes".equalsIgnoreCase(name) || "true".equalsIgnoreCase(name) || "1".equals(name)) {
				return "Yes";
			}
			if ("no".equalsIgnoreCase(name) || "false".equalsIgnoreCase(name) || "0".equals(name)) {
				return "No";
			}
			return name;
		}
		if (StringUtils.isNotBlank(obs.getValueText())) {
			String text = obs.getValueText().trim();
			if ("yes".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text) || "1".equals(text)) {
				return "Yes";
			}
			if ("no".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text) || "0".equals(text)) {
				return "No";
			}
			return text;
		}
		if (obs.getValueNumeric() != null) {
			return obs.getValueNumeric().doubleValue() != 0d ? "Yes" : "No";
		}
		return "";
	}

	private static List<Integer> resolveConfirmedByLabImagingConceptIds() {
		List<Integer> ids = parseConceptIds(Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_CONFIRMED_BY_LAB_IMAGING_CONCEPT_IDS));
		if (!ids.isEmpty()) {
			return ids;
		}
		Concept byName = Context.getConceptService()
				.getConceptByName("Primary confirmed diagnosis by lab/imaging");
		if (byName == null) {
			byName = Context.getConceptService().getConceptByName("Confirmed By lab/Imaging");
		}
		if (byName != null && byName.getConceptId() != null) {
			return Collections.singletonList(byName.getConceptId());
		}
		return Collections.emptyList();
	}

	private static List<Integer> parseConceptIds(String csv) {
		List<Integer> result = new ArrayList<>();
		if (StringUtils.isBlank(csv)) {
			return result;
		}
		for (String part : csv.split(",")) {
			if (StringUtils.isBlank(part)) {
				continue;
			}
			try {
				result.add(Integer.valueOf(part.trim()));
			} catch (NumberFormatException ignored) {
				// skip invalid
			}
		}
		return result;
	}

	private static String extractIcd11Codes(String diagnosisNotes) {
		if (StringUtils.isBlank(diagnosisNotes)) {
			return "";
		}
		Set<String> codes = new LinkedHashSet<>();
		Matcher matcher = ICD_CODE_WITH_NAME_PATTERN.matcher(diagnosisNotes);
		while (matcher.find()) {
			String candidate = matcher.group(1);
			if (StringUtils.isNotBlank(candidate) && ICD_CODE_PATTERN.matcher(candidate.trim()).matches()) {
				codes.add(candidate.trim());
			}
		}
		for (String part : diagnosisNotes.split("[,;\\n]+")) {
			String code = extractSingleIcdCode(part);
			if (StringUtils.isNotBlank(code)) {
				codes.add(code);
			}
		}
		return codes.isEmpty() ? "" : StringUtils.join(codes, ", ");
	}

	private static String extractSingleIcdCode(String diagnosisLabel) {
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
		if (StringUtils.isBlank(candidate) || !ICD_CODE_PATTERN.matcher(candidate).matches()) {
			return null;
		}
		return candidate;
	}

	private static Date startOfDay(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private static Date endOfDay(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal.getTime();
	}

	private static Date addDays(Date date, int days) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}

	private static String resolveProductType(PatientServiceBill item, FacilityServicePrice fsp) {
		if (item != null && item.getHopService() != null && StringUtils.isNotBlank(item.getHopService().getName())) {
			return item.getHopService().getName();
		}
		if (fsp != null && StringUtils.isNotBlank(fsp.getCategory())) {
			return fsp.getCategory();
		}
		Integer itemType = item != null && item.getItemType() != null ? item.getItemType()
				: (fsp != null ? fsp.getItemType() : null);
		if (itemType != null) {
			return Integer.valueOf(1).equals(itemType) ? "Ordinary" : "DCP";
		}
		return "";
	}

	private static String getIdentifierByTypeNames(Patient patient, String... typeNames) {
		if (patient == null || typeNames == null) {
			return "";
		}
		for (String typeName : typeNames) {
			if (StringUtils.isBlank(typeName)) {
				continue;
			}
			PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByName(typeName);
			if (type == null) {
				continue;
			}
			PatientIdentifier identifier = patient.getPatientIdentifier(type);
			if (identifier != null && StringUtils.isNotBlank(identifier.getIdentifier())) {
				return identifier.getIdentifier();
			}
		}
		return "";
	}

	private static String getPersonAttributeByTypeNames(Patient patient, String... typeNames) {
		if (patient == null || typeNames == null) {
			return "";
		}
		for (String typeName : typeNames) {
			PersonAttributeType type = Context.getPersonService().getPersonAttributeTypeByName(typeName);
			if (type == null) {
				continue;
			}
			PersonAttribute attribute = patient.getAttribute(type);
			if (attribute != null && StringUtils.isNotBlank(attribute.getValue())) {
				return attribute.getValue();
			}
		}
		return "";
	}

	private static String resolveProviderAttributeByNamesOrUuid(User user, String attributeTypeUuid,
			String... typeNames) {
		if (user == null || user.getPerson() == null) {
			return "";
		}
		ProviderService providerService = Context.getProviderService();
		ProviderAttributeType byUuid = StringUtils.isNotBlank(attributeTypeUuid)
				? providerService.getProviderAttributeTypeByUuid(attributeTypeUuid)
				: null;
		for (Provider provider : providerService.getProvidersByPerson(user.getPerson(), false)) {
			if (provider == null) {
				continue;
			}
			if (byUuid != null) {
				String value = getProviderAttributeValue(provider, byUuid);
				if (StringUtils.isNotBlank(value)) {
					return value;
				}
			}
			if (typeNames != null) {
				for (String typeName : typeNames) {
					ProviderAttributeType type = findProviderAttributeTypeByName(typeName);
					if (type == null) {
						continue;
					}
					String value = getProviderAttributeValue(provider, type);
					if (StringUtils.isNotBlank(value)) {
						return value;
					}
				}
			}
		}
		return "";
	}

	private static ProviderAttributeType findProviderAttributeTypeByName(String typeName) {
		if (StringUtils.isBlank(typeName)) {
			return null;
		}
		for (ProviderAttributeType candidate : Context.getProviderService().getAllProviderAttributeTypes()) {
			if (candidate != null && typeName.equalsIgnoreCase(candidate.getName())) {
				return candidate;
			}
		}
		return null;
	}

	private static String getProviderAttributeValue(Provider provider, ProviderAttributeType type) {
		if (provider == null || type == null) {
			return "";
		}
		for (ProviderAttribute attribute : provider.getActiveAttributes(type)) {
			if (attribute == null) {
				continue;
			}
			String value = attribute.getValueReference();
			if (StringUtils.isBlank(value) && attribute.getValue() != null) {
				value = String.valueOf(attribute.getValue());
			}
			if (StringUtils.isNotBlank(value)) {
				return value;
			}
		}
		return "";
	}

	private static void saveLocationAttribute(Location location, String typeUuid, String typeName, String value) {
		LocationService locationService = Context.getLocationService();
		LocationAttributeType type = resolveOrCreateAttributeType(typeUuid, typeName);
		LocationAttribute existing = null;
		for (LocationAttribute attribute : location.getActiveAttributes(type)) {
			existing = attribute;
			break;
		}
		if (existing != null) {
			existing.setValue(value);
			location.setAttribute(existing);
		} else {
			LocationAttribute attribute = new LocationAttribute();
			attribute.setAttributeType(type);
			attribute.setValue(value);
			location.setAttribute(attribute);
		}
		locationService.saveLocation(location);
	}

	private static LocationAttributeType resolveOrCreateAttributeType(String typeUuid, String typeName) {
		LocationService locationService = Context.getLocationService();
		LocationAttributeType type = null;
		if (StringUtils.isNotBlank(typeUuid)) {
			type = locationService.getLocationAttributeTypeByUuid(typeUuid);
		}
		if (type == null && StringUtils.isNotBlank(typeName)) {
			for (LocationAttributeType candidate : locationService.getAllLocationAttributeTypes()) {
				if (candidate != null && typeName.equalsIgnoreCase(candidate.getName())) {
					type = candidate;
					break;
				}
			}
		}
		if (type != null) {
			return type;
		}
		type = new LocationAttributeType();
		type.setName(typeName);
		type.setDescription(typeName + " for CBHI Bill Report");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		type.setMinOccurs(0);
		type.setMaxOccurs(1);
		if (StringUtils.isNotBlank(typeUuid)) {
			type.setUuid(typeUuid);
		}
		return locationService.saveLocationAttributeType(type);
	}

	/**
	 * Loads admissions in range (optionally filtered by insurance) and expands each
	 * non-voided bill item into a report row.
	 */
	public static List<BillReportItem> buildReportItems(Date startDate, Date endDate, Insurance insurance) {
		List<Admission> admissions = getService().getAdmissionsByAdmissionDate(startDate, endDate, insurance);
		if (admissions == null || admissions.isEmpty()) {
			return Collections.emptyList();
		}

		List<BillReportItem> rows = new ArrayList<>();
		int sequence = 1;
		for (Admission admission : admissions) {
			if (admission == null || Boolean.TRUE.equals(admission.getVoided())) {
				continue;
			}
			GlobalBill globalBill = getService().getGlobalBillByAdmission(admission);
			List<Consommation> consommations = globalBill != null
					? getService().getAllConsommationByGlobalBill(globalBill)
					: Collections.emptyList();
			if (consommations == null || consommations.isEmpty()) {
				continue;
			}

			BigDecimal insuranceRatePercent = resolveInsuranceRatePercent(admission, globalBill);
			Patient patientForDx = admission.getInsurancePolicy() != null
					? admission.getInsurancePolicy().getOwner()
					: null;
			ClinicalDiagnosis clinicalDiagnosis = resolveClinicalDiagnosis(admission, globalBill, patientForDx);

			for (Consommation consommation : consommations) {
				if (consommation == null || Boolean.TRUE.equals(consommation.getVoided())) {
					continue;
				}
				Set<PatientServiceBill> items = consommation.getBillItems();
				if (items == null || items.isEmpty()) {
					continue;
				}
				for (PatientServiceBill item : items) {
					if (item == null || Boolean.TRUE.equals(item.getVoided())) {
						continue;
					}
					BillReportItem row = toReportItem(sequence++, admission, globalBill, consommation, item,
							insuranceRatePercent, clinicalDiagnosis);
					rows.add(row);
				}
			}
		}
		return rows;
	}

	private static BillReportItem toReportItem(int sequence, Admission admission, GlobalBill globalBill,
			Consommation consommation, PatientServiceBill item, BigDecimal insuranceRatePercent,
			ClinicalDiagnosis clinicalDiagnosis) {
		BillReportItem row = new BillReportItem();
		row.setRowNumber(sequence);
		row.setAdmissionDate(admission.getAdmissionDate());
		// Discharge is recorded on moh_bill_global_bill.closing_date
		row.setDischargeDate(globalBill != null ? globalBill.getClosingDate() : null);
		row.setWardType(consommation.getDepartment() != null ? consommation.getDepartment().getName() : null);
		if (globalBill != null) {
			row.setVoucherId(StringUtils.defaultIfBlank(globalBill.getRhipVoucherCode(),
					globalBill.getRhipVoucherReferenceNumber()));
		}

		Beneficiary beneficiary = consommation.getBeneficiary();
		Patient patient = beneficiary != null ? beneficiary.getPatient() : null;
		if (patient != null) {
			row.setPatientId(resolvePatientId(patient));
			row.setPatientNames(patient.getPersonName() != null ? patient.getPersonName().getFullName() : null);
			row.setPatientDob(patient.getBirthdate());
			row.setPatientGender(patient.getGender());
			row.setPatientPhoneNumber(resolvePatientPhone(patient));
			row.setPatientNid(resolvePatientNid(patient));
		}
		if (beneficiary != null) {
			row.setHouseholdHeadNid(beneficiary.getOwnerCode());
		}
		// Social Registry Level source TBD — leave blank for now
		row.setSocialRegistryLevel("");

		User billCreator = globalBill != null ? globalBill.getCreator() : null;
		row.setProviderName(resolveProviderName(billCreator));
		row.setProviderQualification(resolveProviderQualification(billCreator));
		row.setProviderPhoneNumber(resolveProviderPhone(billCreator));
		row.setProviderRegistrationNumber(resolveProviderRegistration(billCreator));

		if (clinicalDiagnosis == null) {
			clinicalDiagnosis = ClinicalDiagnosis.empty();
		}
		row.setPrimaryWorkingDiagnosis(clinicalDiagnosis.getPrimaryDiagnosis());
		row.setPrimaryConfirmedByLabImaging(clinicalDiagnosis.getConfirmedByLabImaging());
		row.setIcd11Code(clinicalDiagnosis.getIcd11Code());

		FacilityServicePrice fsp = null;
		BillableService billable = item.getService();
		if (billable != null) {
			fsp = billable.getFacilityServicePrice();
		}
		if (fsp != null) {
			row.setProductName(fsp.getName());
			row.setProductCategory(fsp.getCategory());
			row.setProductUnit(fsp.getItemType() != null ? String.valueOf(fsp.getItemType()) : "");
		} else if (item.getHopService() != null) {
			row.setProductName(item.getHopService().getName());
		} else if (StringUtils.isNotBlank(item.getServiceOther())) {
			row.setProductName(item.getServiceOther());
		}
		// Product Code source TBD — leave blank for now
		row.setProductCode("");
		row.setProductType(resolveProductType(item, fsp));
		if (StringUtils.isBlank(row.getProductCategory()) && item.getHopService() != null) {
			row.setProductCategory(item.getHopService().getName());
		}

		BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
		BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
		BigDecimal total = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
		BigDecimal rate = insuranceRatePercent != null ? insuranceRatePercent : BigDecimal.ZERO;
		// Co-payment share = (100 - insuranceRate)% of total product cost
		BigDecimal coPayPercent = BigDecimal.valueOf(100).subtract(rate);
		BigDecimal coPay = total.multiply(coPayPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal rssb = total.subtract(coPay).setScale(2, RoundingMode.HALF_UP);

		row.setProductCost(unitPrice.setScale(2, RoundingMode.HALF_UP));
		row.setCoPayment(coPay);
		row.setRssbPayment(rssb);
		row.setTotalInvoice(total);
		return row;
	}

	private static BigDecimal resolveInsuranceRatePercent(Admission admission, GlobalBill globalBill) {
		Insurance insurance = null;
		if (globalBill != null && globalBill.getInsurance() != null) {
			insurance = globalBill.getInsurance();
		} else if (admission.getInsurancePolicy() != null) {
			insurance = admission.getInsurancePolicy().getInsurance();
		}
		if (insurance == null) {
			return BigDecimal.ZERO;
		}
		InsuranceRate rate = insurance.getCurrentRate();
		if (rate == null || rate.getRate() == null) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(rate.getRate().doubleValue());
	}

	private static Location resolveReportLocation() {
		LocationService locationService = Context.getLocationService();
		String defaultLocationId = Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION);
		if (StringUtils.isNotBlank(defaultLocationId)) {
			try {
				Location byId = locationService.getLocation(Integer.valueOf(defaultLocationId.trim()));
				if (byId != null) {
					return byId;
				}
			} catch (NumberFormatException ignored) {
				Location byName = locationService.getLocation(defaultLocationId.trim());
				if (byName != null) {
					return byName;
				}
			}
		}
		if (Context.getUserContext() != null && Context.getUserContext().getLocation() != null) {
			return Context.getUserContext().getLocation();
		}
		return locationService.getDefaultLocation();
	}

	private static String resolveHfCode(Location location) {
		String fosaUuid = new RhipVoucherIntegrationConfig().getFosaIdAttributeTypeUuid();
		String fromConfiguredUuid = getLocationAttributeValue(location, fosaUuid, null);
		if (StringUtils.isNotBlank(fromConfiguredUuid)) {
			return fromConfiguredUuid;
		}
		return getLocationAttributeValue(location, null, BillingConstants.LOCATION_ATTR_FOSA_ID_NAME);
	}

	private static String getLocationAttributeValue(Location location, String typeUuid, String typeName) {
		if (location == null || location.getActiveAttributes() == null) {
			return "";
		}
		LocationService locationService = Context.getLocationService();
		LocationAttributeType type = null;
		if (StringUtils.isNotBlank(typeUuid)) {
			type = locationService.getLocationAttributeTypeByUuid(typeUuid);
		}
		if (type == null && StringUtils.isNotBlank(typeName)) {
			for (LocationAttributeType candidate : locationService.getAllLocationAttributeTypes()) {
				if (candidate != null && typeName.equalsIgnoreCase(candidate.getName())) {
					type = candidate;
					break;
				}
			}
		}
		if (type == null) {
			return "";
		}
		for (LocationAttribute attribute : location.getActiveAttributes(type)) {
			if (attribute != null && attribute.getValue() != null) {
				return String.valueOf(attribute.getValue());
			}
		}
		return "";
	}

	private static String resolvePhone(Patient patient) {
		String attributeId = Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_PHONENUMBER_PERSONAL_ATTRIBUTE);
		if (StringUtils.isBlank(attributeId)) {
			return "";
		}
		try {
			PersonAttributeType type = Context.getPersonService()
					.getPersonAttributeType(Integer.valueOf(attributeId.trim()));
			if (type == null) {
				return "";
			}
			PersonAttribute attribute = patient.getAttribute(type);
			return attribute != null ? attribute.getValue() : "";
		} catch (Exception ignored) {
			return "";
		}
	}
}
