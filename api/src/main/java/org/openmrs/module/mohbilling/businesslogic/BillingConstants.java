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

}
