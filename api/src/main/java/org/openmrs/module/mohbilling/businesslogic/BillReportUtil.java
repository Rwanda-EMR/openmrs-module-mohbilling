package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
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
import org.openmrs.Visit;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds CBHI Bill Report rows from admissions in a date range.
 */
public final class BillReportUtil {

	/**
	 * ICD-10/11-ish token: starts with letter or digit, may include dots and optional
	 * /extension, and must contain at least one digit (e.g. Z33, E10, 1A00.1).
	 */
	private static final Pattern ICD_CODE_PATTERN =
			Pattern.compile("^(?=.*\\d)[A-Za-z0-9][A-Za-z0-9.]*(?:/[A-Za-z0-9.]+)?$");
	/** e.g. Pregnant state (Z33) */
	private static final Pattern DIAGNOSIS_NAME_WITH_PAREN_CODE =
			Pattern.compile("^(.+?)\\s*\\(\\s*([A-Za-z0-9][A-Za-z0-9./]*)\\s*\\)\\s*$");

	/** Insurance categories shown as one combined option (all policies under that category). */
	private static final List<String> COMBINED_INSURANCE_CATEGORIES = Collections.unmodifiableList(
			Arrays.asList("MUTUELLE", "RSSB"));

	private static final String FILTER_PREFIX_CATEGORY = "category:";
	private static final String FILTER_PREFIX_ID = "id:";

	/** Soft-lookup cache: lowercased facility service name → rpm_item.code ("" if none). */
	private static final Map<String, String> RPM_ITEM_CODE_BY_NAME = new ConcurrentHashMap<>();
	private static volatile Boolean rpmItemTableAvailable;

	/** Cached once per JVM; empty list means "no concept configured / not found". */
	private static volatile List<Integer> confirmedByLabImagingConceptIdsCache;
	private static volatile String confirmedByLabImagingConceptIdsGpCache;
	private static volatile Concept primaryDiagnosisConceptCache;
	private static volatile String primaryDiagnosisConceptUuidCache;
	private static volatile Concept transferIdConceptCache;
	private static volatile String transferIdConceptUuidCache;

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

	/** Rwanda National ID if present, otherwise NID Application Number. */
	public static String resolvePatientNid(Patient patient) {
		if (patient == null) {
			return "";
		}
		String nid = getIdentifierByTypeNames(patient, "Rwanda National ID");
		if (StringUtils.isNotBlank(nid)) {
			return nid;
		}
		return StringUtils.defaultString(getIdentifierByTypeNames(patient, "NID Application Number"));
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
	 * Dropdown options for Bill Report insurance filter.
	 * MUTUELLE and RSSB are one option each (by category); all other insurances
	 * are listed individually by name.
	 *
	 * @return maps with keys {@code value} (e.g. {@code category:MUTUELLE} / {@code id:12})
	 *         and {@code label}
	 */
	public static List<Map<String, String>> buildInsuranceFilterOptions() {
		List<Insurance> all = InsuranceUtil.getAllInsurances();
		if (all == null) {
			all = Collections.emptyList();
		}

		Set<String> presentCombinedCategories = new HashSet<>();
		List<Insurance> individual = new ArrayList<>();
		for (Insurance insurance : all) {
			if (insurance == null || Boolean.TRUE.equals(insurance.isVoided())) {
				continue;
			}
			String category = StringUtils.trimToEmpty(insurance.getCategory());
			if (isCombinedInsuranceCategory(category)) {
				presentCombinedCategories.add(category.toUpperCase(Locale.ROOT));
			} else {
				individual.add(insurance);
			}
		}

		List<Map<String, String>> options = new ArrayList<>();
		for (String category : COMBINED_INSURANCE_CATEGORIES) {
			if (presentCombinedCategories.contains(category)) {
				Map<String, String> option = new LinkedHashMap<>();
				option.put("value", FILTER_PREFIX_CATEGORY + category);
				option.put("label", category);
				options.add(option);
			}
		}

		Collections.sort(individual, new Comparator<Insurance>() {
			@Override
			public int compare(Insurance a, Insurance b) {
				String nameA = a.getName() != null ? a.getName() : "";
				String nameB = b.getName() != null ? b.getName() : "";
				return nameA.compareToIgnoreCase(nameB);
			}
		});
		for (Insurance insurance : individual) {
			if (insurance.getInsuranceId() == null) {
				continue;
			}
			Map<String, String> option = new LinkedHashMap<>();
			option.put("value", FILTER_PREFIX_ID + insurance.getInsuranceId());
			option.put("label", StringUtils.isNotBlank(insurance.getName()) ? insurance.getName()
					: ("Insurance #" + insurance.getInsuranceId()));
			options.add(option);
		}
		return options;
	}

	private static boolean isCombinedInsuranceCategory(String category) {
		if (StringUtils.isBlank(category)) {
			return false;
		}
		for (String combined : COMBINED_INSURANCE_CATEGORIES) {
			if (combined.equalsIgnoreCase(category.trim())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Resolves a Bill Report insurance filter value into either a category or a
	 * specific insurance. Exactly one of the returned fields is set (or both null
	 * for "all").
	 */
	public static InsuranceFilter resolveInsuranceFilter(String filterValue) {
		InsuranceFilter filter = new InsuranceFilter();
		if (StringUtils.isBlank(filterValue)) {
			return filter;
		}
		String value = filterValue.trim();
		if (value.startsWith(FILTER_PREFIX_CATEGORY)) {
			String category = value.substring(FILTER_PREFIX_CATEGORY.length()).trim();
			if (StringUtils.isNotBlank(category)) {
				filter.category = category.toUpperCase(Locale.ROOT);
			}
			return filter;
		}
		if (value.startsWith(FILTER_PREFIX_ID)) {
			String idPart = value.substring(FILTER_PREFIX_ID.length()).trim();
			try {
				filter.insurance = InsuranceUtil.getInsurance(Integer.valueOf(idPart));
			} catch (NumberFormatException e) {
				log.warn("Invalid insurance filter id: " + filterValue);
			}
			return filter;
		}
		// Backward compatibility: plain numeric insuranceId
		try {
			filter.insurance = InsuranceUtil.getInsurance(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			log.warn("Unrecognized insurance filter: " + filterValue);
		}
		return filter;
	}

	/** Parsed Bill Report insurance dropdown selection. */
	public static final class InsuranceFilter {
		private Insurance insurance;
		private String category;

		public Insurance getInsurance() {
			return insurance;
		}

		public String getCategory() {
			return category;
		}
	}

	/**
	 * Builds UI rows for Bill Report from the materialized ETL table (one row per
	 * bill line). Facility header fields stay live via {@link #buildFacilityHeader()}.
	 */
	public static List<Map<String, Object>> buildAdmissionDisplayRows(Date startDate, Date endDate,
			InsuranceFilter insuranceFilter) {
		List<BillReportItem> items = buildReportItems(startDate, endDate, insuranceFilter);
		if (items.isEmpty()) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> rows = new ArrayList<>(items.size());
		for (BillReportItem item : items) {
			rows.add(toDisplayRowFromEtl(item));
		}
		return rows;
	}

	/**
	 * Maps an ETL report item into the denormalized keys used by the Bill Report JSP.
	 */
	static Map<String, Object> toDisplayRowFromEtl(BillReportItem item) {
		Map<String, Object> row = new HashMap<>();
		if (item == null) {
			return row;
		}
		Admission admissionStub = new Admission();
		admissionStub.setAdmissionId(item.getAdmissionId());
		admissionStub.setAdmissionDate(item.getAdmissionDate());
		row.put("admission", admissionStub);
		row.put("admissionDate", item.getAdmissionDate());
		row.put("dischargeDate", item.getDischargeDate());
		row.put("admissionStatus", item.getAdmissionStatus());
		row.put("patientId", item.getPatientId());
		row.put("patientNames", item.getPatientNames());
		row.put("patientNid", item.getPatientNid());
		row.put("householdNid", item.getHouseholdHeadNid());
		row.put("patientDob", item.getPatientDob());
		row.put("socialRegistryLevel", item.getSocialRegistryLevel());
		row.put("patientPhone", item.getPatientPhoneNumber());
		row.put("patientGender", item.getPatientGender());
		row.put("insuranceName", item.getInsuranceName());
		row.put("cardNumber", item.getCardNumber());
		row.put("voucherId", item.getVoucherId());
		row.put("providerName", item.getProviderName());
		row.put("providerQualification", item.getProviderQualification());
		row.put("providerPhone", item.getProviderPhoneNumber());
		row.put("providerRegistration", item.getProviderRegistrationNumber());
		row.put("primaryDiagnosis", item.getPrimaryWorkingDiagnosis());
		row.put("confirmedByLabImaging", item.getPrimaryConfirmedByLabImaging());
		row.put("icd11Code", item.getIcd11Code());
		row.put("productName", item.getProductName());
		row.put("productCode", item.getProductCode());
		row.put("productType", item.getProductType());
		row.put("productUnit", item.getProductUnit());
		row.put("productQuantity", item.getProductQuantity());
		row.put("productCost", item.getProductCost());
		row.put("coPayment", item.getCoPayment());
		row.put("rssbPayment", item.getRssbPayment());
		row.put("totalInvoice", item.getTotalInvoice());
		row.put("transferAvailable", item.getTransferAvailable());
		row.put("transferId", item.getTransferId());
		row.put("transferValidity", item.getTransferValidity());
		row.put("wardType", item.getWardType());
		return row;
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
		row.put("patientPhone", resolvePatientPhone(patient));
		row.put("patientGender", patient != null ? patient.getGender() : null);

		User billCreator = globalBill != null ? globalBill.getCreator() : null;
		row.put("providerName", resolveProviderName(billCreator));
		row.put("providerQualification", resolveProviderQualification(billCreator));
		row.put("providerPhone", resolveProviderPhone(billCreator));
		row.put("providerRegistration", resolveProviderRegistration(billCreator));

		BigDecimal insuranceRatePercent = resolveInsuranceRatePercent(admission, globalBill);
		row.put("socialRegistryLevel", resolveSocialRegistryLevel(insuranceRatePercent));

		String productName = "";
		String productCode = "";
		String productType = "";
		String productUnit = "";
		BigDecimal productQuantity = null;
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
			String[] nameAndCode = resolveProductNameAndCode(item, fsp);
			productName = nameAndCode[0];
			productCode = nameAndCode[1];
			productType = resolveProductType(item, fsp);
			if (StringUtils.isBlank(productType) && item.getHopService() != null) {
				productType = StringUtils.defaultString(item.getHopService().getName());
			}
			productUnit = "Item";

			BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
			BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
			productQuantity = quantity;
			productCost = unitPrice.setScale(2, RoundingMode.HALF_UP);
			totalInvoice = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

			// Co-payment share = (100 - insuranceRate)% of total product cost
			BigDecimal coPayPercent = BigDecimal.valueOf(100).subtract(
					insuranceRatePercent != null ? insuranceRatePercent : BigDecimal.ZERO);
			coPayment = totalInvoice.multiply(coPayPercent)
					.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			rssbPayment = totalInvoice.subtract(coPayment).setScale(2, RoundingMode.HALF_UP);
		}

		Map<String, String> transferInfo = resolveTransferInfo(admission, patient);
		row.put("primaryDiagnosis", clinicalDiagnosis.getPrimaryDiagnosis());
		row.put("confirmedByLabImaging", clinicalDiagnosis.getConfirmedByLabImaging());
		row.put("icd11Code", clinicalDiagnosis.getIcd11Code());
		row.put("productName", productName);
		row.put("productCode", productCode);
		row.put("productType", productType);
		row.put("productUnit", productUnit);
		row.put("productQuantity", productQuantity);
		row.put("productCost", productCost);
		row.put("coPayment", coPayment);
		row.put("rssbPayment", rssbPayment);
		row.put("totalInvoice", totalInvoice);
		row.put("transferAvailable", transferInfo.get("available"));
		row.put("transferId", transferInfo.get("transferId"));
		row.put("transferValidity", transferInfo.get("validity"));
		return row;
	}

	/**
	 * Social Registry Level: Fully Sponsored when insurance covers 100%, otherwise Not Fully Sponsored.
	 */
	private static String resolveSocialRegistryLevel(BigDecimal insuranceRatePercent) {
		BigDecimal rate = insuranceRatePercent != null ? insuranceRatePercent : BigDecimal.ZERO;
		if (rate.compareTo(BigDecimal.valueOf(100)) == 0) {
			return "Fully Sponsored";
		}
		return "Not Fully Sponsored";
	}

	/**
	 * Transfer Available = Yes when a Transfer ID obs has value_text (used at reception).
	 * When {@code admission.visit} is set, only obs on that visit are considered.
	 * Validity period = decision_to_transfer_at through decision_to_transfer_at + 1 month
	 * (soft match on transferapp {@code transfers.hie_transfer_id}, also maternity/neonatal);
	 * missing local row still keeps Available = Yes.
	 */
	private static Map<String, String> resolveTransferInfo(Admission admission, Patient patient) {
		Map<String, String> result = new HashMap<>();
		result.put("available", "No");
		result.put("transferId", "");
		result.put("validity", "");
		if (patient == null || patient.getPatientId() == null) {
			return result;
		}

		String transferUuid = resolveTransferIdFromObs(admission, patient);
		if (StringUtils.isBlank(transferUuid)) {
			return result;
		}
		result.put("available", "Yes");
		result.put("transferId", transferUuid);

		List<Object> transferRow = findTransferDatesByHieTransferId(transferUuid.trim());
		if (transferRow == null || transferRow.isEmpty()) {
			log.info("Bill Report transfer validity empty: hie_transfer_id=" + transferUuid.trim()
					+ " not found in transfers/maternity_transfers/neonatal_transfers");
			return result;
		}
		// decision_to_transfer_at, fallback date_created
		Date decisionDate = toSqlDate(transferRow.size() > 0 ? transferRow.get(0) : null);
		if (decisionDate == null) {
			decisionDate = toSqlDate(transferRow.size() > 1 ? transferRow.get(1) : null);
		}
		result.put("validity", formatTransferValidityPeriod(decisionDate));
		return result;
	}

	/**
	 * Soft-lookup transfer decision date by {@code hie_transfer_id} (trimmed match).
	 * Tries {@code transfers}, then maternity/neonatal tables when present.
	 * Row columns: decision_to_transfer_at, date_created.
	 */
	private static List<Object> findTransferDatesByHieTransferId(String hieTransferId) {
		if (StringUtils.isBlank(hieTransferId)) {
			return null;
		}
		String escaped = hieTransferId.replace("'", "''");
		String[] tableQueries = new String[] {
				"SELECT decision_to_transfer_at, date_created FROM transfers "
						+ "WHERE TRIM(hie_transfer_id) = TRIM('" + escaped + "') "
						+ "AND COALESCE(voided, 0) = 0 LIMIT 1",
				"SELECT decision_to_transfer_at, date_created FROM maternity_transfers "
						+ "WHERE TRIM(hie_transfer_id) = TRIM('" + escaped + "') "
						+ "AND COALESCE(voided, 0) = 0 LIMIT 1",
				"SELECT decision_to_transfer_at, date_created FROM neonatal_transfers "
						+ "WHERE TRIM(hie_transfer_id) = TRIM('" + escaped + "') "
						+ "AND COALESCE(voided, 0) = 0 LIMIT 1"
		};
		for (String sql : tableQueries) {
			try {
				List<List<Object>> rows = Context.getAdministrationService().executeSQL(sql, true);
				if (rows != null && !rows.isEmpty() && rows.get(0) != null && !rows.get(0).isEmpty()) {
					return rows.get(0);
				}
			} catch (Exception e) {
				// table/module may be absent — try next
				log.debug("Bill Report transfer date lookup skipped: " + e.getMessage());
			}
		}
		return null;
	}

	private static Date toSqlDate(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return (Date) value;
		}
		if (value instanceof Calendar) {
			return ((Calendar) value).getTime();
		}
		String text = StringUtils.trimToNull(String.valueOf(value));
		if (text == null) {
			return null;
		}
		String[] patterns = new String[] {
				"yyyy-MM-dd HH:mm:ss.S",
				"yyyy-MM-dd HH:mm:ss",
				"yyyy-MM-dd'T'HH:mm:ss",
				"yyyy-MM-dd"
		};
		for (String pattern : patterns) {
			try {
				return new java.text.SimpleDateFormat(pattern).parse(text);
			} catch (Exception ignored) {
				// try next
			}
		}
		return null;
	}

	/**
	 * Finds non-blank Transfer ID obs value_text.
	 * Prefer obs on {@code admission.visit}; otherwise fall back to admission-date window.
	 */
	private static String resolveTransferIdFromObs(Admission admission, Patient patient) {
		Concept concept = resolveTransferIdConcept();
		if (concept == null) {
			return null;
		}
		Visit visit = admission != null ? admission.getVisit() : null;
		List<Obs> obsList = collectObsForConcept(patient, concept, visit);
		if (obsList == null || obsList.isEmpty()) {
			return null;
		}

		boolean restrictToVisit = visit != null && visit.getVisitId() != null;
		Date admissionDate = admission != null ? admission.getAdmissionDate() : null;
		int dayWindow = BillingConstants.BILL_REPORT_TRANSFER_OBS_DAY_WINDOW;
		Date rangeStart = !restrictToVisit && admissionDate != null
				? startOfDay(addDays(admissionDate, -dayWindow)) : null;
		Date rangeEnd = !restrictToVisit && admissionDate != null
				? endOfDay(addDays(admissionDate, dayWindow)) : null;

		Obs bestInWindow = null;
		Obs bestAny = null;
		for (Obs obs : obsList) {
			if (obs == null || Boolean.TRUE.equals(obs.getVoided())) {
				continue;
			}
			String valueText = StringUtils.trimToNull(obs.getValueText());
			if (valueText == null) {
				continue;
			}
			if (bestAny == null || isNewerObs(obs, bestAny)) {
				bestAny = obs;
			}
			if (rangeStart != null && rangeEnd != null && obs.getObsDatetime() != null
					&& !obs.getObsDatetime().before(rangeStart) && !obs.getObsDatetime().after(rangeEnd)) {
				if (bestInWindow == null || isNewerObs(obs, bestInWindow)) {
					bestInWindow = obs;
				}
			}
		}
		Obs chosen = restrictToVisit ? bestAny : (bestInWindow != null ? bestInWindow : bestAny);
		return chosen != null ? StringUtils.trimToNull(chosen.getValueText()) : null;
	}

	private static boolean isNewerObs(Obs candidate, Obs current) {
		Date candidateDate = candidate.getObsDatetime();
		Date currentDate = current.getObsDatetime();
		if (candidateDate == null) {
			return false;
		}
		if (currentDate == null) {
			return true;
		}
		return candidateDate.after(currentDate);
	}

	private static Concept resolveTransferIdConcept() {
		String uuid = StringUtils.trimToNull(Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_TRANSFER_ID_CONCEPT_UUID));
		if (uuid == null) {
			uuid = StringUtils.trimToNull(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GP_TRANSFERAPP_RECEIVED_TRANSFER_CONCEPT_UUID,
					BillingConstants.DEFAULT_TRANSFER_ID_CONCEPT_UUID));
		}
		if (uuid == null) {
			uuid = StringUtils.trimToNull(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GP_RWANDAEMR_TRANSFER_ID_CONCEPT_UUID));
		}
		if (uuid == null) {
			uuid = BillingConstants.DEFAULT_TRANSFER_ID_CONCEPT_UUID;
		}
		Concept cached = transferIdConceptCache;
		if (cached != null && uuid.equals(transferIdConceptUuidCache)) {
			return cached;
		}
		if (cached == null && uuid.equals(transferIdConceptUuidCache)) {
			return null;
		}
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		transferIdConceptUuidCache = uuid;
		transferIdConceptCache = concept;
		if (concept == null) {
			log.warn("Bill Report Transfer ID concept not found for uuid=" + uuid);
		}
		return concept;
	}

	/**
	 * Transfer validity display: decision date through decision date + 1 calendar month.
	 */
	static String formatTransferValidityPeriod(Date decisionDate) {
		if (decisionDate == null) {
			return "";
		}
		Date endDate = addMonths(decisionDate, 1);
		return formatTransferValidity(decisionDate, endDate);
	}

	private static String formatTransferValidity(Date start, Date end) {
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy");
		if (start == null && end == null) {
			return "";
		}
		String startStr = start != null ? df.format(start) : "";
		String endStr = end != null ? df.format(end) : "";
		if (StringUtils.isBlank(startStr)) {
			return endStr;
		}
		if (StringUtils.isBlank(endStr)) {
			return startStr;
		}
		return startStr + " - " + endStr;
	}

	/**
	 * Resolves primary diagnosis / confirmed-by-lab / ICD-11 for an admission.
	 * When {@code admission.visit} is set, only obs on that visit are used; otherwise
	 * obs within admission_date +/- 2 days.
	 */
	public static ClinicalDiagnosis resolveClinicalDiagnosis(Admission admission, GlobalBill globalBill,
			Patient patient) {
		if (admission == null || patient == null) {
			return ClinicalDiagnosis.empty();
		}
		Visit visit = admission.getVisit();
		boolean restrictToVisit = visit != null && visit.getVisitId() != null;
		if (!restrictToVisit && admission.getAdmissionDate() == null) {
			return ClinicalDiagnosis.empty();
		}
		try {
			int dayWindow = BillingConstants.BILL_REPORT_DIAGNOSIS_OBS_DAY_WINDOW;
			Date rangeStart = !restrictToVisit
					? startOfDay(addDays(admission.getAdmissionDate(), -dayWindow)) : null;
			Date rangeEnd = !restrictToVisit
					? endOfDay(addDays(admission.getAdmissionDate(), dayWindow)) : null;

			Concept diagnosisConcept = resolvePrimaryDiagnosisConcept();
			List<String> diagnosisLabels = collectObsLabels(patient, diagnosisConcept, visit, rangeStart, rangeEnd);

			List<Integer> confirmedConceptIds = resolveConfirmedByLabImagingConceptIds();
			String confirmed = resolveConfirmedByLabImaging(patient, confirmedConceptIds, visit, rangeStart, rangeEnd);

			LinkedHashSet<String> diagnosisNames = new LinkedHashSet<>();
			LinkedHashSet<String> icdCodes = new LinkedHashSet<>();
			for (String label : diagnosisLabels) {
				DiagnosisParts parts = splitDiagnosisNameAndCode(label);
				if (StringUtils.isNotBlank(parts.name)) {
					diagnosisNames.add(parts.name);
				}
				if (StringUtils.isNotBlank(parts.code)) {
					icdCodes.add(parts.code);
				}
			}
			String primaryDiagnosis = diagnosisNames.isEmpty() ? "" : StringUtils.join(diagnosisNames, ", ");
			String icd11 = icdCodes.isEmpty() ? "" : StringUtils.join(icdCodes, ", ");
			if (StringUtils.isBlank(primaryDiagnosis) && StringUtils.isBlank(icd11) && log.isDebugEnabled()) {
				log.debug("Bill Report diagnosis empty: admissionId="
						+ admission.getAdmissionId() + ", patientId=" + patient.getPatientId()
						+ ", visitId=" + (restrictToVisit ? visit.getVisitId() : null)
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
		uuid = uuid.trim();
		Concept cached = primaryDiagnosisConceptCache;
		if (cached != null && uuid.equals(primaryDiagnosisConceptUuidCache)) {
			return cached;
		}
		if (cached == null && uuid.equals(primaryDiagnosisConceptUuidCache)) {
			// Previously resolved as missing for this uuid
			return null;
		}
		Concept concept = Context.getConceptService().getConceptByUuid(uuid);
		primaryDiagnosisConceptUuidCache = uuid;
		primaryDiagnosisConceptCache = concept;
		if (concept == null) {
			log.warn("Bill Report primary diagnosis concept not found for uuid=" + uuid);
		}
		return concept;
	}

	private static List<String> collectObsLabels(Patient patient, Concept concept, Visit visit, Date rangeStart,
			Date rangeEnd) {
		List<String> labels = new ArrayList<>();
		if (patient == null || concept == null) {
			return labels;
		}
		boolean restrictToVisit = visit != null && visit.getVisitId() != null;
		if (!restrictToVisit && (rangeStart == null || rangeEnd == null)) {
			return labels;
		}
		List<Obs> obsList = collectObsForConcept(patient, concept, visit);
		if (obsList == null || obsList.isEmpty()) {
			return labels;
		}
		for (Obs obs : obsList) {
			if (obs == null || Boolean.TRUE.equals(obs.getVoided())) {
				continue;
			}
			if (!restrictToVisit) {
				if (obs.getObsDatetime() == null || obs.getObsDatetime().before(rangeStart)
						|| obs.getObsDatetime().after(rangeEnd)) {
					continue;
				}
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

	private static String resolveConfirmedByLabImaging(Patient patient, List<Integer> confirmedConceptIds,
			Visit visit, Date rangeStart, Date rangeEnd) {
		if (patient == null || confirmedConceptIds == null || confirmedConceptIds.isEmpty()) {
			return "";
		}
		boolean restrictToVisit = visit != null && visit.getVisitId() != null;
		if (!restrictToVisit && (rangeStart == null || rangeEnd == null)) {
			return "";
		}
		for (Integer conceptId : confirmedConceptIds) {
			Concept concept = Context.getConceptService().getConcept(conceptId);
			if (concept == null) {
				continue;
			}
			List<Obs> obsList = collectObsForConcept(patient, concept, visit);
			if (obsList == null) {
				continue;
			}
			for (Obs obs : obsList) {
				if (obs == null || Boolean.TRUE.equals(obs.getVoided())) {
					continue;
				}
				if (!restrictToVisit) {
					if (obs.getObsDatetime() == null || obs.getObsDatetime().before(rangeStart)
							|| obs.getObsDatetime().after(rangeEnd)) {
						continue;
					}
				}
				String asYesNo = toYesNo(obs);
				if (StringUtils.isNotBlank(asYesNo)) {
					return asYesNo;
				}
			}
		}
		return "";
	}

	/**
	 * Loads obs for a concept; when {@code visit} is set, only obs whose encounter belongs
	 * to that visit (via {@code admission.visit_id} FK).
	 */
	private static List<Obs> collectObsForConcept(Patient patient, Concept concept, Visit visit) {
		if (patient == null || concept == null) {
			return Collections.emptyList();
		}
		try {
			if (visit != null && visit.getVisitId() != null) {
				List<Encounter> encounters = Context.getEncounterService().getEncountersByVisit(visit, false);
				if (encounters == null || encounters.isEmpty()) {
					return Collections.emptyList();
				}
				List<Obs> onVisit = new ArrayList<>();
				List<Obs> byPerson = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
				if (byPerson == null || byPerson.isEmpty()) {
					return Collections.emptyList();
				}
				Set<Integer> encounterIds = new HashSet<>();
				for (Encounter encounter : encounters) {
					if (encounter != null && encounter.getEncounterId() != null) {
						encounterIds.add(encounter.getEncounterId());
					}
				}
				for (Obs obs : byPerson) {
					if (obs == null || Boolean.TRUE.equals(obs.getVoided()) || obs.getEncounter() == null
							|| obs.getEncounter().getEncounterId() == null) {
						continue;
					}
					if (encounterIds.contains(obs.getEncounter().getEncounterId())) {
						onVisit.add(obs);
					}
				}
				return onVisit;
			}
			List<Obs> obsList = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
			return obsList != null ? obsList : Collections.emptyList();
		} catch (Exception e) {
			log.debug("Bill Report obs lookup failed for conceptId="
					+ (concept != null ? concept.getConceptId() : null) + ": " + e.getMessage());
			return Collections.emptyList();
		}
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
		String gpValue = StringUtils.defaultString(Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_CONFIRMED_BY_LAB_IMAGING_CONCEPT_IDS));
		List<Integer> cached = confirmedByLabImagingConceptIdsCache;
		if (cached != null && gpValue.equals(confirmedByLabImagingConceptIdsGpCache)) {
			return cached;
		}

		List<Integer> ids = parseConceptIds(gpValue);
		if (ids.isEmpty()) {
			// Name fallback once only — avoid OpenMRS WARN spam on every admission during ETL.
			Concept byName = Context.getConceptService()
					.getConceptByName("Primary confirmed diagnosis by lab/imaging");
			if (byName == null) {
				byName = Context.getConceptService().getConceptByName("Confirmed By lab/Imaging");
			}
			if (byName != null && byName.getConceptId() != null) {
				ids = Collections.singletonList(byName.getConceptId());
			} else {
				log.warn("Bill Report Confirmed By lab/Imaging concept not found by name and "
						+ BillingConstants.GLOBAL_PROPERTY_CONFIRMED_BY_LAB_IMAGING_CONCEPT_IDS
						+ " is empty; column will stay blank until configured.");
				ids = Collections.emptyList();
			}
		}
		confirmedByLabImagingConceptIdsGpCache = gpValue;
		confirmedByLabImagingConceptIdsCache = ids;
		return ids;
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

	/**
	 * Splits a diagnosis label into display name + ICD code.
	 * Supports common EMR formats:
	 * <ul>
	 *   <li>{@code Pregnant state - Z33}</li>
	 *   <li>{@code Z33 - Pregnant state}</li>
	 *   <li>{@code Pregnant state (Z33)}</li>
	 *   <li>{@code Pregnant state: Z33}</li>
	 *   <li>code-only or name-only</li>
	 * </ul>
	 */
	static DiagnosisParts splitDiagnosisNameAndCode(String diagnosisLabel) {
		DiagnosisParts parts = new DiagnosisParts();
		if (StringUtils.isBlank(diagnosisLabel)) {
			return parts;
		}
		String value = diagnosisLabel.trim();

		Matcher paren = DIAGNOSIS_NAME_WITH_PAREN_CODE.matcher(value);
		if (paren.matches() && isIcdCode(paren.group(2))) {
			parts.name = paren.group(1).trim();
			parts.code = paren.group(2).trim();
			return parts;
		}

		// "Name: CODE" when the right side is an ICD token (avoid stripping times etc.)
		int colon = value.lastIndexOf(':');
		if (colon > 0 && colon < value.length() - 1) {
			String left = value.substring(0, colon).trim();
			String right = value.substring(colon + 1).trim();
			if (isIcdCode(right) && StringUtils.isNotBlank(left) && !isIcdCode(left)) {
				parts.name = left;
				parts.code = right;
				return parts;
			}
			if (isIcdCode(left) && StringUtils.isNotBlank(right) && !isIcdCode(right)) {
				parts.name = right;
				parts.code = left;
				return parts;
			}
		}

		// Prefer "Name - CODE" (code after last dash)
		for (int i = value.length() - 1; i >= 1; i--) {
			char c = value.charAt(i);
			if (c != '-' && c != '\u2013' && c != '\u2014') {
				continue;
			}
			String left = value.substring(0, i).trim();
			String right = value.substring(i + 1).trim();
			if (isIcdCode(right) && StringUtils.isNotBlank(left) && !isIcdCode(left)) {
				parts.name = left;
				parts.code = right;
				return parts;
			}
		}

		// Then "CODE - Name" (code before first dash)
		for (int i = 1; i < value.length() - 1; i++) {
			char c = value.charAt(i);
			if (c != '-' && c != '\u2013' && c != '\u2014') {
				continue;
			}
			String left = value.substring(0, i).trim();
			String right = value.substring(i + 1).trim();
			if (isIcdCode(left) && StringUtils.isNotBlank(right)) {
				parts.name = isIcdCode(right) ? "" : right;
				parts.code = left;
				return parts;
			}
		}

		if (isIcdCode(value)) {
			parts.code = value;
			return parts;
		}
		parts.name = value;
		return parts;
	}

	private static boolean isIcdCode(String candidate) {
		return StringUtils.isNotBlank(candidate) && ICD_CODE_PATTERN.matcher(candidate.trim()).matches();
	}

	/** Parsed diagnosis display name + ICD code from one obs/concept label. */
	static final class DiagnosisParts {
		private String name = "";
		private String code = "";
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

	static Date addMonths(Date date, int months) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, months);
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

	/**
	 * Known embedded product-code prefixes in facility service names
	 * ({@code -RHIC-} consultations/procedures, {@code -RLTC-} laboratory).
	 */
	private static final String[] PRODUCT_CODE_MARKERS = { "RHIC", "RLTC" };

	/**
	 * Resolves product name/code for a bill line.
	 * MEDICAMENTS / CONSOMMABLES: keep facility service name; soft-join {@code rpm_item}
	 * by name for code.
	 * Others: split embedded {@code -RHIC-}/{@code -RLTC-} suffixes when present.
	 *
	 * @return {@code [productName, productCode]}
	 */
	static String[] resolveProductNameAndCode(PatientServiceBill item, FacilityServicePrice fsp) {
		String rawName = "";
		if (fsp != null && StringUtils.isNotBlank(fsp.getName())) {
			rawName = fsp.getName().trim();
		} else if (item != null && item.getHopService() != null
				&& StringUtils.isNotBlank(item.getHopService().getName())) {
			rawName = item.getHopService().getName().trim();
		} else if (item != null && StringUtils.isNotBlank(item.getServiceOther())) {
			rawName = item.getServiceOther().trim();
		}
		if (StringUtils.isBlank(rawName)) {
			return new String[] { "", "" };
		}
		if (isRpmCatalogProduct(item, fsp)) {
			String rpmCode = resolveRpmItemCodeByName(rawName);
			if (StringUtils.isBlank(rpmCode) && fsp != null && StringUtils.isNotBlank(fsp.getShortName())) {
				rpmCode = resolveRpmItemCodeByName(fsp.getShortName().trim());
			}
			return new String[] { rawName, StringUtils.defaultString(rpmCode) };
		}
		return splitProductNameAndCode(rawName);
	}

	/** MEDICAMENTS and CONSOMMABLES resolve product code from {@code rpm_item}. */
	private static boolean isRpmCatalogProduct(PatientServiceBill item, FacilityServicePrice fsp) {
		if (fsp != null && isRpmCatalogLabel(fsp.getCategory())) {
			return true;
		}
		if (item != null && item.getHopService() != null && isRpmCatalogLabel(item.getHopService().getName())) {
			return true;
		}
		if (item != null && item.getService() != null && item.getService().getServiceCategory() != null
				&& isRpmCatalogLabel(item.getService().getServiceCategory().getName())) {
			return true;
		}
		return false;
	}

	private static boolean isRpmCatalogLabel(String label) {
		if (StringUtils.isBlank(label)) {
			return false;
		}
		String value = label.trim();
		return "MEDICAMENTS".equalsIgnoreCase(value) || "MEDICAMENT".equalsIgnoreCase(value)
				|| "MEDIC".equalsIgnoreCase(value) || "PHARMACY".equalsIgnoreCase(value)
				|| "CONSOMMABLES".equalsIgnoreCase(value) || "CONSOMMABLE".equalsIgnoreCase(value)
				|| "CONSOM".equalsIgnoreCase(value);
	}

	/**
	 * Soft lookup of {@code rpm_item.code} by exact name match (case-insensitive).
	 * Returns blank when the Rwanda Pharmacy Management table/module is absent or no row matches.
	 */
	static String resolveRpmItemCodeByName(String facilityServiceName) {
		if (StringUtils.isBlank(facilityServiceName)) {
			return "";
		}
		String cacheKey = facilityServiceName.trim().toLowerCase(Locale.ROOT);
		String cached = RPM_ITEM_CODE_BY_NAME.get(cacheKey);
		if (cached != null) {
			return cached;
		}
		if (!isRpmItemTableAvailable()) {
			RPM_ITEM_CODE_BY_NAME.put(cacheKey, "");
			return "";
		}
		String code = "";
		try {
			String escaped = facilityServiceName.trim().replace("'", "''");
			String sql = "SELECT code FROM rpm_item WHERE LOWER(TRIM(name)) = LOWER(TRIM('" + escaped + "')) "
					+ "AND COALESCE(voided, 0) = 0 ORDER BY item_id LIMIT 1";
			List<List<Object>> rows = Context.getAdministrationService().executeSQL(sql, true);
			if (rows != null && !rows.isEmpty() && rows.get(0) != null && !rows.get(0).isEmpty()
					&& rows.get(0).get(0) != null) {
				code = StringUtils.trimToEmpty(String.valueOf(rows.get(0).get(0)));
			}
		} catch (Exception e) {
			log.debug("Bill Report rpm_item lookup skipped for name=" + facilityServiceName + ": "
					+ e.getMessage());
			rpmItemTableAvailable = Boolean.FALSE;
		}
		RPM_ITEM_CODE_BY_NAME.put(cacheKey, code);
		return code;
	}

	private static boolean isRpmItemTableAvailable() {
		Boolean available = rpmItemTableAvailable;
		if (available != null) {
			return available;
		}
		synchronized (BillReportUtil.class) {
			if (rpmItemTableAvailable != null) {
				return rpmItemTableAvailable;
			}
			try {
				String sql = "SELECT 1 FROM information_schema.tables "
						+ "WHERE table_schema = DATABASE() AND table_name = 'rpm_item' LIMIT 1";
				List<List<Object>> rows = Context.getAdministrationService().executeSQL(sql, true);
				rpmItemTableAvailable = rows != null && !rows.isEmpty();
			} catch (Exception e) {
				log.debug("Bill Report rpm_item table check failed: " + e.getMessage());
				rpmItemTableAvailable = Boolean.FALSE;
			}
			return rpmItemTableAvailable;
		}
	}

	/**
	 * Splits facility service names that embed a coded suffix, e.g.
	 * <ul>
	 *   <li>{@code Consultation by a general practioner-RHIC-CONS-013}
	 *       → name / {@code RHIC-CONS-013}</li>
	 *   <li>{@code SGPT –ALAT*-RLTC-CHEM-181}
	 *       → {@code SGPT –ALAT*} / {@code RLTC-CHEM-181}</li>
	 * </ul>
	 * Other formats are left as full name with blank code for now.
	 *
	 * @return {@code [productName, productCode]}
	 */
	static String[] splitProductNameAndCode(String rawName) {
		String name = StringUtils.defaultString(rawName).trim();
		if (StringUtils.isBlank(name)) {
			return new String[] { "", "" };
		}
		for (String marker : PRODUCT_CODE_MARKERS) {
			String token = "-" + marker + "-";
			int markerIndex = name.indexOf(token);
			if (markerIndex <= 0) {
				continue;
			}
			String productName = name.substring(0, markerIndex).trim();
			String suffix = name.substring(markerIndex + token.length()).trim();
			String productCode = (marker + "-" + suffix).trim();
			if (StringUtils.isBlank(productName) || (marker + "-").equals(productCode)) {
				continue;
			}
			return new String[] { productName, productCode };
		}
		return new String[] { name, "" };
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
	 * Reads Bill Report rows from the materialized ETL table.
	 */
	public static List<BillReportItem> buildReportItems(Date startDate, Date endDate,
			InsuranceFilter insuranceFilter) {
		Insurance insurance = insuranceFilter != null ? insuranceFilter.getInsurance() : null;
		String category = insuranceFilter != null ? insuranceFilter.getCategory() : null;
		Integer insuranceId = insurance != null ? insurance.getInsuranceId() : null;
		List<BillReportItem> rows = getService().getBillReportFromEtl(startDate, endDate, insuranceId, category);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		int sequence = 1;
		for (BillReportItem row : rows) {
			row.setRowNumber(sequence++);
		}
		return rows;
	}

	/**
	 * Live (non-ETL) expansion used by the ETL refresh task so soft-join rules stay
	 * in one place ({@link #toReportItem}).
	 */
	public static List<BillReportItem> buildReportItemsLiveFromAdmissions(List<Admission> admissions) {
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
		row.setPatientServiceBillId(item.getPatientServiceBillId());
		row.setAdmissionId(admission.getAdmissionId());
		if (admission.getVisit() != null) {
			row.setVisitId(admission.getVisit().getVisitId());
		}
		if (globalBill != null) {
			row.setGlobalBillId(globalBill.getGlobalBillId());
		}
		row.setConsommationId(consommation.getConsommationId());
		row.setAdmissionDate(admission.getAdmissionDate());
		// Discharge is recorded on moh_bill_global_bill.closing_date
		row.setDischargeDate(globalBill != null ? globalBill.getClosingDate() : null);
		row.setAdmissionStatus(Boolean.TRUE.equals(admission.getIsAdmitted()) ? "Inpatient" : "Outpatient");
		row.setWardType(consommation.getDepartment() != null ? consommation.getDepartment().getName() : null);
		Insurance insurance = null;
		if (globalBill != null && globalBill.getInsurance() != null) {
			insurance = globalBill.getInsurance();
		} else if (admission.getInsurancePolicy() != null) {
			insurance = admission.getInsurancePolicy().getInsurance();
		}
		if (insurance != null) {
			row.setInsuranceId(insurance.getInsuranceId());
			row.setInsuranceCategory(insurance.getCategory());
			row.setInsuranceName(insurance.getName());
		}
		InsurancePolicy policy = admission.getInsurancePolicy();
		if (policy != null) {
			row.setCardNumber(policy.getInsuranceCardNo());
		}
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
		row.setSocialRegistryLevel(resolveSocialRegistryLevel(insuranceRatePercent));

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
		String[] nameAndCode = resolveProductNameAndCode(item, fsp);
		row.setProductName(nameAndCode[0]);
		row.setProductCode(nameAndCode[1]);
		if (fsp != null) {
			row.setProductCategory(fsp.getCategory());
		}
		row.setProductType(resolveProductType(item, fsp));
		row.setProductUnit("Item");
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

		row.setProductQuantity(quantity);
		row.setProductCost(unitPrice.setScale(2, RoundingMode.HALF_UP));
		row.setCoPayment(coPay);
		row.setRssbPayment(rssb);
		row.setTotalInvoice(total);

		Map<String, String> transferInfo = resolveTransferInfo(admission, patient);
		row.setTransferAvailable(transferInfo.get("available"));
		row.setTransferId(transferInfo.get("transferId"));
		row.setTransferValidity(transferInfo.get("validity"));
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
