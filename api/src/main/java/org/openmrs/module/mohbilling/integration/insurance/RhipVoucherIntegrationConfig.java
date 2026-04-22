package org.openmrs.module.mohbilling.integration.insurance;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

import java.util.LinkedHashSet;
import java.util.Set;

public class RhipVoucherIntegrationConfig {

	private static final String VOUCHER_PREFIX = "mohbilling.rhipVoucher.";
	private static final String DEFAULT_ENABLED_VOUCHER_INSURANCE_KEYS = "MUTUELLE,MMI";
	public static final String VOUCHER_URL = VOUCHER_PREFIX + "url";
	public static final String VOUCHER_API_KEY = VOUCHER_PREFIX + "apiKey";
	public static final String VOUCHER_API_ORIGIN = VOUCHER_PREFIX + "apiOrigin";
	public static final String VOUCHER_ENABLED_INSURANCE_KEYS = VOUCHER_PREFIX + "enabledInsuranceKeys";
	public static final String PROVIDER_LICENSE_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "providerLicenseAttributeTypeUuid";
	public static final String PATIENT_IDENTIFIER_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "patientIdentifierAttributeTypeUuid";
	public static final String DIAGNOSIS_CONCEPT_IDS = VOUCHER_PREFIX + "diagnosisConceptIds";
	public static final String DEFAULT_FOSA_ID = VOUCHER_PREFIX + "defaultFosaId";
	public static final String DEFAULT_PATIENT_TYPE = VOUCHER_PREFIX + "defaultPatientType";
	public static final String DEFAULT_HEALTHCARE_STAY_TYPE = VOUCHER_PREFIX + "defaultHealthCareStayType";
	public static final String DEFAULT_TREATMENT_FOR_NEW_BORN = VOUCHER_PREFIX + "defaultTreatmentForNewBorn";
	public static final String FOSA_ID_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "fosaIdAttributeTypeUuid";
	public static final String PATIENT_PHONE_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "patientPhoneAttributeTypeUuid";
	public static final String PRACTITIONER_DETAILS_URL = VOUCHER_PREFIX + "practitionerDetailsUrl";
	public static final String PRACTITIONER_CREATE_URL = VOUCHER_PREFIX + "practitionerCreateUrl";
	public static final String PRACTITIONER_TYPES_URL = VOUCHER_PREFIX + "practitionerTypesUrl";
	public static final String PRACTITIONER_TYPE_PROVIDER_ATTRIBUTE_TYPE_UUID =
			VOUCHER_PREFIX + "practitionerTypeProviderAttributeTypeUuid";
	public static final String PRACTITIONER_DOCUMENT_TYPE_PROVIDER_ATTRIBUTE_TYPE_UUID =
			VOUCHER_PREFIX + "practitionerDocumentTypeProviderAttributeTypeUuid";
	public static final String PRACTITIONER_CONTRACT_TYPE_PROVIDER_ATTRIBUTE_TYPE_UUID =
			VOUCHER_PREFIX + "practitionerContractTypeProviderAttributeTypeUuid";
	public static final String PRACTITIONER_SUBCATEGORY_TYPE_ID_PROVIDER_ATTRIBUTE_TYPE_UUID =
			VOUCHER_PREFIX + "practitionerSubCategoryTypeIdProviderAttributeTypeUuid";
	public static final String PRACTITIONER_DOCUMENT_NUMBER_PROVIDER_ATTRIBUTE_TYPE_UUID =
			VOUCHER_PREFIX + "practitionerDocumentNumberProviderAttributeTypeUuid";
	public static final String PRACTITIONER_PHONE_PROVIDER_ATTRIBUTE_TYPE_UUID =
			VOUCHER_PREFIX + "practitionerPhoneProviderAttributeTypeUuid";

	public String getVoucherUrl() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_URL);
	}

	public String getVoucherApiKey() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_API_KEY);
	}

	public String getVoucherApiOrigin() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_API_ORIGIN);
	}

	public String getVoucherEnabledInsuranceKeysRaw() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_ENABLED_INSURANCE_KEYS);
	}

	public Set<String> getVoucherEnabledInsuranceKeys() {
		String configured = getVoucherEnabledInsuranceKeysRaw();
		if (StringUtils.isBlank(configured)) {
			configured = DEFAULT_ENABLED_VOUCHER_INSURANCE_KEYS;
		}
		Set<String> keys = new LinkedHashSet<String>();
		String[] values = configured.split(",");
		for (String value : values) {
			if (StringUtils.isBlank(value)) {
				continue;
			}
			keys.add(value.trim().toUpperCase());
		}
		return keys;
	}

	public boolean isVoucherButtonEnabledForInsurance(String insuranceCategory, String insuranceName) {
		Set<String> enabledKeys = getVoucherEnabledInsuranceKeys();
		if (enabledKeys.isEmpty()) {
			return false;
		}
		String normalizedCategory = StringUtils.trimToEmpty(insuranceCategory).toUpperCase();
		String normalizedName = StringUtils.trimToEmpty(insuranceName).toUpperCase();
		for (String key : enabledKeys) {
			if (StringUtils.isBlank(key)) {
				continue;
			}
			if (StringUtils.isNotBlank(normalizedCategory) && normalizedCategory.equals(key)) {
				return true;
			}
			if (StringUtils.isNotBlank(normalizedName) && normalizedName.contains(key)) {
				return true;
			}
		}
		return false;
	}

	public String getProviderLicenseAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PROVIDER_LICENSE_ATTRIBUTE_TYPE_UUID);
	}

	public String getPatientIdentifierAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PATIENT_IDENTIFIER_ATTRIBUTE_TYPE_UUID);
	}

	public String getDiagnosisConceptIds() {
		return Context.getAdministrationService().getGlobalProperty(DIAGNOSIS_CONCEPT_IDS);
	}

	public String getDefaultFosaId() {
		return Context.getAdministrationService().getGlobalProperty(DEFAULT_FOSA_ID);
	}

	public String getFosaIdAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(FOSA_ID_ATTRIBUTE_TYPE_UUID);
	}

	public String getPatientPhoneAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PATIENT_PHONE_ATTRIBUTE_TYPE_UUID);
	}

	public String getPractitionerDetailsUrl() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_DETAILS_URL);
	}

	public String getPractitionerCreateUrl() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_CREATE_URL);
	}

	public String getPractitionerTypesUrl() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_TYPES_URL);
	}

	public String getPractitionerTypeProviderAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_TYPE_PROVIDER_ATTRIBUTE_TYPE_UUID);
	}

	public String getPractitionerDocumentTypeProviderAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_DOCUMENT_TYPE_PROVIDER_ATTRIBUTE_TYPE_UUID);
	}

	public String getPractitionerContractTypeProviderAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_CONTRACT_TYPE_PROVIDER_ATTRIBUTE_TYPE_UUID);
	}

	public String getPractitionerSubCategoryTypeIdProviderAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_SUBCATEGORY_TYPE_ID_PROVIDER_ATTRIBUTE_TYPE_UUID);
	}

	public String getPractitionerDocumentNumberProviderAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_DOCUMENT_NUMBER_PROVIDER_ATTRIBUTE_TYPE_UUID);
	}

	public String getPractitionerPhoneProviderAttributeTypeUuid() {
		return Context.getAdministrationService().getGlobalProperty(PRACTITIONER_PHONE_PROVIDER_ATTRIBUTE_TYPE_UUID);
	}

	public boolean isPractitionerIntegrationEnabled() {
		return StringUtils.isNotBlank(getPractitionerDetailsUrl()) && StringUtils.isNotBlank(getPractitionerCreateUrl());
	}

	public String getDefaultPatientType() {
		return Context.getAdministrationService().getGlobalProperty(DEFAULT_PATIENT_TYPE);
	}

	public String getDefaultHealthCareStayType() {
		return Context.getAdministrationService().getGlobalProperty(DEFAULT_HEALTHCARE_STAY_TYPE);
	}

	public Boolean getDefaultTreatmentForNewBorn() {
		String value = Context.getAdministrationService().getGlobalProperty(DEFAULT_TREATMENT_FOR_NEW_BORN);
		return StringUtils.isBlank(value) ? null : Boolean.valueOf(value);
	}

	public boolean isVoucherEnabled() {
		return StringUtils.isNotBlank(getVoucherUrl());
	}
}
