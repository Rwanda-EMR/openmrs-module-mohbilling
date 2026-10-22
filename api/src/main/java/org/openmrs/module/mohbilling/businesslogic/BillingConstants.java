package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;

public class BillingConstants {

	// public static EncounterType ENCOUNTER_TYPE_REGISTRATION;
	// public static EncounterType ENCOUNTER_TYPE_VITALS;
	// public static EncounterType ENCOUNTER_TYPE_DIAGNOSIS;
	// public static Privilege PRINT_BARCODE_OFFLINE_PRIVILEGE;
	public static final int NONE_CONCEPT_ID = 1107;
	public static final String PRIMARY_CARE_INSURANCE_EXPIRATION_DATE = Context
			.getAdministrationService().getGlobalProperty(
					"registration.insuranceExpirationDateConcept");
	public static final String PRIMARY_CARE_INSURANCE_COVERAGE_START_DATE = Context
			.getAdministrationService().getGlobalProperty(
					"registration.insuranceCoverageStartDateConcept");
	public static final String PRIMARY_CARE_SERVICE_REQUESTED = "registration.serviceRequestedConcept";
	public static final String GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE = "billing.primaryIdentifierType";
	public static final String GLOBAL_PROPERTY_OTHER_IDENTIFIER_TYPES = "billing.otherIdentifierTypes";
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCATION = "billing.defaultLocation";

	/** Final / primary diagnosis question concept ids (comma-separated) shown on Global Bill. */
	public static final String GLOBAL_PROPERTY_FINAL_DIAGNOSIS_CONCEPT_IDS =
			"billing.finalDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill";
	/** Bill Report primary diagnosis question concept UUID (obs search around admission date). */
	public static final String GLOBAL_PROPERTY_PRIMARY_DIAGNOSIS_CONCEPT_UUID =
			"mohbilling.billReport.primaryDiagnosisConceptUuid";
	/** Optional Yes/No concept ids for "Confirmed By lab/Imaging" on Bill Report. */
	public static final String GLOBAL_PROPERTY_CONFIRMED_BY_LAB_IMAGING_CONCEPT_IDS =
			"mohbilling.billReport.confirmedByLabImagingConceptIds";
	/** Default Rwanda EMR Primary ICD diagnosis question UUID. */
	public static final String PRIMARY_ICD_DIAGNOSIS_CONCEPT_UUID = "2dce81f9-3874-4247-b441-6369ca0725c2";
	/** Days before/after admission_date used when searching diagnosis obs. */
	public static final int BILL_REPORT_DIAGNOSIS_OBS_DAY_WINDOW = 2;
	
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO = "billing.healthFacilityLogo";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_NAME = "billing.healthFacilityName";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS = "billing.healthFacilityPhysicalAddress";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE = "billing.healthFacilityShortCode";
	public static final String GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL = "billing.healthFacilityEmail";
	// //TODO: the location code architecture is wrong -- all location codes
	// come from module
	// //TODO: this needs to correspond to a single default location
	// //TODO: registration clerk can override this in module.
	// public static final String GLOBAL_PROPERTY_RWANDA_LOCATION_CODE =
	// "registration.rwandaLocationCodes";
	// public static final String GLOBAL_PROPERTY_DEFAULT_LOCATION_CODE =
	// "registration.defaultLocationCode";
	public static final String GLOBAL_PROPERTY_INSURANCE_TYPE = "registration.insuranceTypeConcept";
	public static final String GLOBAL_PROPERTY_INSURANCE_NUMBER = "registration.insuranceNumberConcept";
	// public static final String GLOBAL_PROPERTY_NATIONAL_ID_TYPE =
	// "registration.nationalIdType";
	// public static final String SESSION_ATTRIBUTE_WORKSTATION_LOCATION =
	// "primaryCareWorkstationLocation";
	// public static final String GLOBAL_PROPERTY_HEALTH_CENTER_ATTRIBUTE_TYPE =
	// "registration.healthCenterPersonAttribute";
	// public static final String GLOBAL_PROPERTY_INSURANCE_TYPE_ANSWERS =
	// "registration.insuranceTypeConceptAnswers";
	// public static final String GLOBAL_PROPERTY_MOTHERS_NAME_CONCEPT =
	// "registration.mothersNameConceptId";
	// public static final String GLOBAL_PROPERTY_FATHERS_NAME_CONCEPT =
	// "registration.fathersNameConceptId";
	// public static final String GLOBAL_PROPERTY_SERVICE_REQUESTED_CONCEPT =
	// "registration.serviceRequestedConcept";
	// public final static String
	// GLOBAL_PROPERTY_PARENT_TO_CHILD_RELATIONSHIP_TYPE =
	// "registration.parentChildRelationshipTypeId";
	// public final static String GLOBAL_PROPERTY_RESTRICT_BY_HEALTH_CENTER =
	// "registration.restrictSearchByHealthCenter";
	// public static final String MOTHER_NAME_ATTRIBUTE_TYPE = "Mother's name";
	// public static final String FATHER_NAME_ATTRIBUTE_TYPE = "Father's name";
	public static EncounterType ENCOUNTER_TYPE_REGISTRATION;
	
	 public static final String GLOBAL_PROPERTY_REVENUE= "mohbilling.REVENUE";
	 public static final String GLOBAL_PROPERTY_IMAGERIE = "mohbilling.imagerie";
	 public static final String GLOBAL_PROPERTY_MEDICAL_ACTS = "mohbilling.ACTS";
	 public static final String GLOBAL_PROPERTY_VENTES_IMPRIMES = "mohbilling.venteImprimes";
	 
	 public static  String GLOBAL_PROPERTY_REPORT_TYPE ;
	
	 public  static final  String CONSOMMABLES ="CONSOM";
	 public  static final  String MEDICAMENTS ="MEDIC";
	 public  static final  String LABORATOIRE ="LABO";
	 public  static final  String CONSULTATION ="CONSULT";
	 public  static final  String HOSPITALISATION ="HOSPITAL";
	 public  static final  String KINESITHERAPIE ="KINES";	
	 public  static final  String MORGUE ="MORGUE";
	 public  static final  String MATERNITE ="MATERN";
	 public  static final  String OXYGENOTHERAPIE ="OXYGENO";
	 public  static final  String MEDICALACTS ="ACTS";
	 public static final   String IMAGERIE = "IMAGERIE";


	public static final String BLOBAL_PROPERTY_IREMBO_ENVIRONMENT = "mohbilling.irembopay.environement";
	public static final String BLOBAL_PROPERTY_IREMBO_PAY_SECRET= "mohbilling.irembopay.secretkey";
	public static final String BLOBAL_PROPERTY_ACCOUNT_IDENTIFIER = "mohbilling.irembopay.accountidentifier";
	public static final String BLOBAL_PROPERTY_PRODUCT_CODE = "mohbilling.irembopay.productcode";
	public static final String BLOBAL_PROPERTY_IREMBO_USER = "mohbilling.irembopay.username";
	public static final String GLOBAL_PROPERTY_IREMBO_MINIMUM_PAYMENT = "mohbilling.irembopay.minimumpayment";
	public static final String GLOBAL_PROPERTY_IREMBO_SCHEDULER_ENABLED = "mohbilling.irembopay.scheduler.enabled";
	public static final String GLOBAL_PROPERTY_IREMBO_SCHEDULER_INTERVAL_SECONDS = "mohbilling.irembopay.scheduler.intervalSeconds";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_SCHEDULER_ENABLED =
			"mohbilling.insuranceReportEtl.scheduler.enabled";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_SCHEDULER_INTERVAL_SECONDS =
			"mohbilling.insuranceReportEtl.scheduler.intervalSeconds";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_SCHEDULER_START_TIME =
			"mohbilling.insuranceReportEtl.scheduler.startTime";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_OVERLAP_DAYS =
			"mohbilling.insuranceReportEtl.overlapDays";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_IMAGING_SERVICES = "mohbilling.IMAGING";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_PROCEDURE_SERVICES = "mohbilling.PROCEDURES";
	public static final String GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_LOADED_SERVICE_CONFIGURATION =
			"mohbilling.insuranceReportEtl.loadedServiceConfiguration";
	public static final String GLOBAL_PROPERTY_CASHIER_REPORT_ETL_SCHEDULER_ENABLED =
			"mohbilling.cashierReportEtl.scheduler.enabled";
	public static final String GLOBAL_PROPERTY_CASHIER_REPORT_ETL_SCHEDULER_INTERVAL_SECONDS =
			"mohbilling.cashierReportEtl.scheduler.intervalSeconds";
	public static final String GLOBAL_PROPERTY_CASHIER_REPORT_ETL_SCHEDULER_START_TIME =
			"mohbilling.cashierReportEtl.scheduler.startTime";
	public static final String GLOBAL_PROPERTY_CASHIER_REPORT_ETL_LOADED_VERSION =
			"mohbilling.cashierReportEtl.loadedVersion";
	public static final String GLOBAL_PROPERTY_GLOBAL_BILL_PAGE_SIZE = "mohbilling.globalbill.pagesize";
	public static final String GLOBAL_PROPERTY_PHONENUMBER_PERSONAL_ATTRIBUTE = "mohbilling.irembopay.phone_attribute";
	public static final String GLOBAL_PROPERTY_IREMBO_AUTO_PHONE_NUMBER = "mohbilling.irembopay.auto_phone_number";
	public static final String GLOBAL_PROPERTY_AMBULANCE_DEPARTMENT_ID = "mohbilling.ambulance.departmentId";
	/**
	 * Exact facility service price name ({@code moh_bill_facility_service_price.name})
	 * used for ambulance KM billing. The insurance-specific BillableService is resolved
	 * from this facility service for each policy.
	 */
	public static final String GLOBAL_PROPERTY_AMBULANCE_FACILITY_SERVICE_PRICE_NAME = "mohbilling.ambulance.facilityServicePriceName";
	public static final String DEFAULT_AMBULANCE_FACILITY_SERVICE_PRICE_NAME = "Ambulance per 1km-RHIC-ADMN-002";

	public static final String GLOBAL_PROPERTY_AUTOPAY_ZERO_BILL = "mohbilling.autopayzerobill";
	public static final String GLOBAL_PROPERTY_AUTOPAY_USER = "mohbilling.autopayuser";
	public static final String GLOBAL_PROPERTY_AUTOPAY_TIME = "mohbilling.autopaytime";
	public static final String GLOBAL_PROPERTY_AUTOPAY_DELAYS = "mohbilling.autopaydelays";
	public static final long DEFAULT_AUTOPAY_TIME_SECONDS = 86400L;
	public static final long DEFAULT_AUTOPAY_DELAYS_SECONDS = 43200L;

	/** When true, bills of the private insurance are left out of the Find Bills report */
	public static final String GLOBAL_PROPERTY_ACTIVATE_NONE_FEATURE = "billing.activateNoneFeature";
	/** Name of the private insurance, set by the rwandaemr module */
	public static final String GLOBAL_PROPERTY_PRIVATE_INSURANCE_NAME = "rwandaemr.autoCreateInsuranceType";

	/** Location attribute type UUIDs created by mohbilling liquibase for CBHI Bill Report */
	public static final String LOCATION_ATTR_FACILITY_TYPE_UUID = "dc3f9f53-730a-4fc6-928c-a113e1089e7e";
	public static final String LOCATION_ATTR_FACILITY_CATEGORY_UUID = "23200719-eb8d-4532-854f-75ba9dcf796e";
	public static final String LOCATION_ATTR_LEVEL_OF_CARE_UUID = "0dfb14f2-9759-4c88-8938-3d9999bc9942";
	public static final String LOCATION_ATTR_OWNER_TYPE_UUID = "3acba29f-5aef-4620-a72e-46eb971b4bb1";
	public static final String LOCATION_ATTR_FACILITY_NAME_UUID = "a3c3be95-d1a6-4fe5-9d88-7555f850416a";
	public static final String LOCATION_ATTR_PROVINCE_NAME_UUID = "f647cb4d-060c-4159-90df-42658365fa9a";
	public static final String LOCATION_ATTR_DISTRICT_NAME_UUID = "7df320d4-b4d7-4add-b3e9-6742e877fb3e";
	public static final String LOCATION_ATTR_SECTOR_NAME_UUID = "0699c216-f0f9-4961-bc7b-31f5904a7459";
	/** Common FOSA ID location attribute type name (rwandaemr / initializer) */
	public static final String LOCATION_ATTR_FOSA_ID_NAME = "FOSA ID";

}
