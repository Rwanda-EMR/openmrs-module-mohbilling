package org.openmrs.module.mohbilling.integration.insurance;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

public class RhipVoucherIntegrationConfig {

	private static final String VOUCHER_PREFIX = "mohbilling.rhipVoucher.";
	public static final String VOUCHER_URL = VOUCHER_PREFIX + "url";
	public static final String VOUCHER_API_KEY = VOUCHER_PREFIX + "apiKey";
	public static final String VOUCHER_API_ORIGIN = VOUCHER_PREFIX + "apiOrigin";
	public static final String PROVIDER_LICENSE_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "providerLicenseAttributeTypeUuid";
	public static final String PATIENT_IDENTIFIER_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "patientIdentifierAttributeTypeUuid";
	public static final String DIAGNOSIS_CONCEPT_IDS = VOUCHER_PREFIX + "diagnosisConceptIds";
	public static final String DEFAULT_FOSA_ID = VOUCHER_PREFIX + "defaultFosaId";
	public static final String DEFAULT_PATIENT_TYPE = VOUCHER_PREFIX + "defaultPatientType";
	public static final String DEFAULT_HEALTHCARE_STAY_TYPE = VOUCHER_PREFIX + "defaultHealthCareStayType";
	public static final String DEFAULT_TREATMENT_FOR_NEW_BORN = VOUCHER_PREFIX + "defaultTreatmentForNewBorn";
	public static final String FOSA_ID_ATTRIBUTE_TYPE_UUID = VOUCHER_PREFIX + "fosaIdAttributeTypeUuid";

	public String getVoucherUrl() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_URL);
	}

	public String getVoucherApiKey() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_API_KEY);
	}

	public String getVoucherApiOrigin() {
		return Context.getAdministrationService().getGlobalProperty(VOUCHER_API_ORIGIN);
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
