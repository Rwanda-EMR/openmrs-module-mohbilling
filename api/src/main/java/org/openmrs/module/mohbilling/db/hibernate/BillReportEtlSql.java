package org.openmrs.module.mohbilling.db.hibernate;

/**
 * Native SQL used to materialize and read the CBHI Bill Report ETL table.
 */
final class BillReportEtlSql {

	private static final String TABLE = "moh_bill_bill_report_etl";

	private BillReportEtlSql() {
	}

	static String deleteAllSql() {
		return "DELETE FROM " + TABLE;
	}

	static String deleteByAdmissionIdRangeSql() {
		return "DELETE FROM " + TABLE
				+ " WHERE admission_id >= :admissionIdFrom AND admission_id <= :admissionIdTo";
	}

	static String deleteByAdmissionIdSql() {
		return "DELETE FROM " + TABLE + " WHERE admission_id = :admissionId";
	}

	static String deleteByAdmissionDateSql() {
		return "DELETE FROM " + TABLE + " WHERE admission_date >= :refreshFrom";
	}

	static String deleteBeforeAdmissionDateSql() {
		return "DELETE FROM " + TABLE + " WHERE admission_date < :startDate";
	}

	static String latestAdmissionDateSql() {
		return "SELECT MAX(admission_date) FROM " + TABLE;
	}

	static String maximumAdmissionIdSql() {
		return "SELECT MAX(admission_id) FROM " + TABLE;
	}

	static String sourceAdmissionIdSql(boolean minimum) {
		return "SELECT " + (minimum ? "MIN" : "MAX") + "(admission_id) FROM moh_bill_admission "
				+ "WHERE COALESCE(voided, 0) = 0 AND admission_date IS NOT NULL "
				+ "AND admission_date >= :startDate";
	}

	static String insertSql() {
		return "INSERT INTO " + TABLE + " ("
				+ "patient_service_bill_id, admission_id, visit_id, global_bill_id, consommation_id, "
				+ "admission_date, discharge_date, admission_status, ward_type, "
				+ "insurance_id, insurance_category, insurance_name, card_number, voucher_id, "
				+ "patient_id, patient_names, patient_nid, household_head_nid, patient_phone, "
				+ "patient_dob, patient_gender, social_registry_level, "
				+ "provider_name, provider_qualification, provider_phone, provider_registration, "
				+ "primary_diagnosis, confirmed_by_lab_imaging, icd11_code, "
				+ "product_name, product_code, product_type, product_category, product_unit, "
				+ "product_quantity, product_cost, co_payment, rssb_payment, total_invoice, "
				+ "transfer_available, transfer_id, transfer_validity, etl_loaded_at"
				+ ") VALUES ("
				+ ":patientServiceBillId, :admissionId, :visitId, :globalBillId, :consommationId, "
				+ ":admissionDate, :dischargeDate, :admissionStatus, :wardType, "
				+ ":insuranceId, :insuranceCategory, :insuranceName, :cardNumber, :voucherId, "
				+ ":patientId, :patientNames, :patientNid, :householdHeadNid, :patientPhone, "
				+ ":patientDob, :patientGender, :socialRegistryLevel, "
				+ ":providerName, :providerQualification, :providerPhone, :providerRegistration, "
				+ ":primaryDiagnosis, :confirmedByLabImaging, :icd11Code, "
				+ ":productName, :productCode, :productType, :productCategory, :productUnit, "
				+ ":productQuantity, :productCost, :coPayment, :rssbPayment, :totalInvoice, "
				+ ":transferAvailable, :transferId, :transferValidity, NOW()"
				+ ") ON DUPLICATE KEY UPDATE "
				+ "admission_id = VALUES(admission_id), "
				+ "visit_id = VALUES(visit_id), "
				+ "global_bill_id = VALUES(global_bill_id), "
				+ "consommation_id = VALUES(consommation_id), "
				+ "admission_date = VALUES(admission_date), "
				+ "discharge_date = VALUES(discharge_date), "
				+ "admission_status = VALUES(admission_status), "
				+ "ward_type = VALUES(ward_type), "
				+ "insurance_id = VALUES(insurance_id), "
				+ "insurance_category = VALUES(insurance_category), "
				+ "insurance_name = VALUES(insurance_name), "
				+ "card_number = VALUES(card_number), "
				+ "voucher_id = VALUES(voucher_id), "
				+ "patient_id = VALUES(patient_id), "
				+ "patient_names = VALUES(patient_names), "
				+ "patient_nid = VALUES(patient_nid), "
				+ "household_head_nid = VALUES(household_head_nid), "
				+ "patient_phone = VALUES(patient_phone), "
				+ "patient_dob = VALUES(patient_dob), "
				+ "patient_gender = VALUES(patient_gender), "
				+ "social_registry_level = VALUES(social_registry_level), "
				+ "provider_name = VALUES(provider_name), "
				+ "provider_qualification = VALUES(provider_qualification), "
				+ "provider_phone = VALUES(provider_phone), "
				+ "provider_registration = VALUES(provider_registration), "
				+ "primary_diagnosis = VALUES(primary_diagnosis), "
				+ "confirmed_by_lab_imaging = VALUES(confirmed_by_lab_imaging), "
				+ "icd11_code = VALUES(icd11_code), "
				+ "product_name = VALUES(product_name), "
				+ "product_code = VALUES(product_code), "
				+ "product_type = VALUES(product_type), "
				+ "product_category = VALUES(product_category), "
				+ "product_unit = VALUES(product_unit), "
				+ "product_quantity = VALUES(product_quantity), "
				+ "product_cost = VALUES(product_cost), "
				+ "co_payment = VALUES(co_payment), "
				+ "rssb_payment = VALUES(rssb_payment), "
				+ "total_invoice = VALUES(total_invoice), "
				+ "transfer_available = VALUES(transfer_available), "
				+ "transfer_id = VALUES(transfer_id), "
				+ "transfer_validity = VALUES(transfer_validity), "
				+ "etl_loaded_at = NOW()";
	}

	static String reportSql(boolean filterInsuranceId, boolean filterCategory) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT patient_service_bill_id, admission_id, visit_id, global_bill_id, consommation_id, ")
				.append("admission_date, discharge_date, admission_status, ward_type, ")
				.append("insurance_id, insurance_category, insurance_name, card_number, voucher_id, ")
				.append("patient_id, patient_names, patient_nid, household_head_nid, patient_phone, ")
				.append("patient_dob, patient_gender, social_registry_level, ")
				.append("provider_name, provider_qualification, provider_phone, provider_registration, ")
				.append("primary_diagnosis, confirmed_by_lab_imaging, icd11_code, ")
				.append("product_name, product_code, product_type, product_category, product_unit, ")
				.append("product_quantity, product_cost, co_payment, rssb_payment, total_invoice, ")
				.append("transfer_available, transfer_id, transfer_validity ")
				.append("FROM ").append(TABLE).append(" ")
				.append("WHERE admission_date >= :startDate AND admission_date <= :endDate ");
		if (filterCategory) {
			sql.append("AND LOWER(insurance_category) = LOWER(:insuranceCategory) ");
		} else if (filterInsuranceId) {
			sql.append("AND insurance_id = :insuranceId ");
		}
		sql.append("ORDER BY admission_date ASC, patient_service_bill_id ASC");
		return sql.toString();
	}
}
