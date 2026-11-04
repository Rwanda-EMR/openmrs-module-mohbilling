package org.openmrs.module.mohbilling.db.hibernate;

/**
 * Native SQL comparing Bill Report ETL vs Insurance Report ETL by {@code global_bill_id}.
 */
final class BillInsuranceReconciliationSql {

	private static final String BILL_ETL = "moh_bill_bill_report_etl";
	private static final String INSURANCE_ETL = "moh_bill_insurance_report_etl";

	private BillInsuranceReconciliationSql() {
	}

	/**
	 * Bills present in one report ETL but not the other for the same admission-date window.
	 * Column {@code appears_in} is {@code Bill Report} or {@code Insurance Report}.
	 * Bind {@code :excludedInsuranceName} from {@code rwandaemr.autoCreateInsuranceType}
	 * (empty string = no exclusion).
	 */
	static String reconciliationSql() {
		return "SELECT appears_in, reason, global_bill_id, voucher_id, admission_date, closing_date, "
				+ "patient_names, patient_id, card_number, insurance_name, total_amount "
				+ "FROM ("
				+ onlyInBillReportSql()
				+ " UNION ALL "
				+ onlyInInsuranceReportSql()
				+ ") recon "
				+ "ORDER BY admission_date ASC, global_bill_id ASC, appears_in ASC";
	}

	/** Case-insensitive insurance name exclusion; no-op when {@code :excludedInsuranceName} is blank. */
	private static String excludeAutoCreateInsurance(String insuranceNameExpression) {
		return " AND (:excludedInsuranceName = '' OR LOWER(TRIM(" + insuranceNameExpression
				+ ")) <> LOWER(:excludedInsuranceName)) ";
	}

	private static String onlyInBillReportSql() {
		return "SELECT 'Bill Report' AS appears_in, "
				+ "CASE WHEN COALESCE(gb.closed, 0) = 0 "
				+ "THEN 'Missing from Insurance Report (not closed)' "
				+ "ELSE 'Missing from Insurance Report' END AS reason, "
				+ "bill.global_bill_id AS global_bill_id, "
				+ "bill.voucher_id AS voucher_id, "
				+ "bill.admission_date AS admission_date, "
				+ "COALESCE(gb.closing_date, bill.discharge_date) AS closing_date, "
				+ "bill.patient_names AS patient_names, "
				+ "bill.patient_id AS patient_id, "
				+ "bill.card_number AS card_number, "
				+ "bill.insurance_name AS insurance_name, "
				+ "bill.total_amount AS total_amount "
				+ "FROM ("
				+ "SELECT global_bill_id, "
				+ "MAX(voucher_id) AS voucher_id, "
				+ "MIN(admission_date) AS admission_date, "
				+ "MAX(discharge_date) AS discharge_date, "
				+ "MAX(patient_names) AS patient_names, "
				+ "MAX(patient_id) AS patient_id, "
				+ "MAX(card_number) AS card_number, "
				+ "MAX(insurance_name) AS insurance_name, "
				+ "SUM(COALESCE(total_invoice, 0)) AS total_amount "
				+ "FROM " + BILL_ETL + " "
				+ "WHERE admission_date >= :startDate AND admission_date <= :endDate "
				+ "AND global_bill_id IS NOT NULL "
				+ excludeAutoCreateInsurance("insurance_name")
				+ "GROUP BY global_bill_id"
				+ ") bill "
				+ "LEFT JOIN " + INSURANCE_ETL + " ins "
				+ "ON ins.global_bill_id = bill.global_bill_id "
				+ "AND ins.admission_date >= :startDate AND ins.admission_date <= :endDate "
				+ "LEFT JOIN moh_bill_global_bill gb ON gb.global_bill_id = bill.global_bill_id "
				+ "WHERE ins.global_bill_id IS NULL";
	}

	private static String onlyInInsuranceReportSql() {
		return "SELECT 'Insurance Report' AS appears_in, "
				+ "'Missing from Bill Report' AS reason, "
				+ "ins.global_bill_id AS global_bill_id, "
				+ "ins.global_bill_identifier AS voucher_id, "
				+ "ins.admission_date AS admission_date, "
				+ "ins.closing_date AS closing_date, "
				+ "ins.beneficiary_name AS patient_names, "
				+ "ins.primary_identifier AS patient_id, "
				+ "ins.card_number AS card_number, "
				+ "ins.insurance_name AS insurance_name, "
				+ "ins.total_100 AS total_amount "
				+ "FROM " + INSURANCE_ETL + " ins "
				+ "LEFT JOIN ("
				+ "SELECT DISTINCT global_bill_id FROM " + BILL_ETL + " "
				+ "WHERE admission_date >= :startDate AND admission_date <= :endDate "
				+ "AND global_bill_id IS NOT NULL "
				+ excludeAutoCreateInsurance("insurance_name")
				+ ") bill ON bill.global_bill_id = ins.global_bill_id "
				+ "WHERE ins.admission_date >= :startDate AND ins.admission_date <= :endDate "
				+ excludeAutoCreateInsurance("ins.insurance_name")
				+ "AND bill.global_bill_id IS NULL";
	}
}
