package org.openmrs.module.mohbilling.db.hibernate;

/**
 * Native SQL used to materialize and read the cashier report.
 */
final class CashierReportEtlSql {

	private static final String TABLE = "moh_bill_cashier_report_etl";

	private CashierReportEtlSql() {
	}

	static String deleteSql(boolean bounded) {
		return bounded ? "DELETE FROM " + TABLE + " WHERE date_received >= :refreshFrom"
				: "DELETE FROM " + TABLE;
	}

	static String deleteByPaymentIdRangeSql() {
		return "DELETE FROM " + TABLE
				+ " WHERE bill_payment_id >= :paymentIdFrom AND bill_payment_id <= :paymentIdTo";
	}

	static String latestPaymentDateSql() {
		return "SELECT MAX(date_received) FROM " + TABLE + " WHERE service_id = 0";
	}

	static String sourcePaymentIdSql(boolean minimum) {
		return "SELECT " + (minimum ? "MIN" : "MAX") + "(bill_payment_id) FROM moh_bill_payment "
				+ "WHERE COALESCE(voided, 0) = 0 AND date_received IS NOT NULL";
	}

	static String headerInsertSql(boolean bounded) {
		return headerInsertSql(bounded, false);
	}

	static String headerInsertByPaymentIdRangeSql() {
		return headerInsertSql(false, true);
	}

	private static String headerInsertSql(boolean dateBounded, boolean paymentIdBounded) {
		StringBuilder sql = new StringBuilder();
		sql.append(insertPrefix())
				.append("SELECT payment.bill_payment_id, payment.patient_bill_id, payment.collector, ")
				.append(paymentTypeExpression()).append(", payment.date_received, payment.amount_paid, ")
				.append("payment.void_reason, beneficiary.patient_id, ")
				.append(personNameExpression("patient_name")).append(", patient_bill.phoneNumber, ")
				.append("patient_bill.transactionStatus, 0, '', 0, 0, NOW() ")
				.append(paymentSourceJoins())
				.append("WHERE COALESCE(payment.voided, 0) = 0 AND payment.date_received IS NOT NULL ");
		appendBounds(sql, dateBounded, paymentIdBounded);
		sql.append("ON DUPLICATE KEY UPDATE ").append(updateAssignments());
		return sql.toString();
	}

	static String serviceInsertSql(boolean bounded) {
		return serviceInsertSql(bounded, false);
	}

	static String serviceInsertByPaymentIdRangeSql() {
		return serviceInsertSql(false, true);
	}

	private static String serviceInsertSql(boolean dateBounded, boolean paymentIdBounded) {
		String patientRate = "(100 - insurance_rate.rate - COALESCE(third_party.rate, 0)) / 100";
		String dueAmount = "GREATEST(ROUND(SUM(CASE WHEN insurance_rate.rate IS NULL THEN 0 ELSE "
				+ "COALESCE(paid_item.paid_quantity, 0) * COALESCE(bill_item.unit_price, 0) * "
				+ patientRate + " END), 2), 0)";
		StringBuilder sql = new StringBuilder();
		sql.append(insertPrefix())
				.append("SELECT payment.bill_payment_id, payment.patient_bill_id, payment.collector, ")
				.append(paymentTypeExpression()).append(", payment.date_received, payment.amount_paid, ")
				.append("payment.void_reason, beneficiary.patient_id, ")
				.append(personNameExpression("patient_name")).append(", patient_bill.phoneNumber, ")
				.append("patient_bill.transactionStatus, hospital_service.service_id, hospital_service.name, ")
				.append("COALESCE(bill_item.item_type, 1), ").append(dueAmount).append(", NOW() ")
				.append(paymentSourceJoins())
				.append("JOIN moh_bill_paid_service_bill paid_item ")
				.append("ON paid_item.bill_payment_id = payment.bill_payment_id ")
				.append("AND COALESCE(paid_item.voided, 0) = 0 ")
				.append("JOIN moh_bill_patient_service_bill bill_item ")
				.append("ON bill_item.patient_service_bill_id = paid_item.patient_service_bill_id ")
				.append("AND COALESCE(bill_item.voided, 0) = 0 ")
				.append("JOIN moh_bill_hop_service hospital_service ")
				.append("ON hospital_service.service_id = bill_item.service_id ")
				.append("LEFT JOIN moh_bill_insurance_policy policy ")
				.append("ON policy.insurance_policy_id = beneficiary.insurance_policy_id ")
				.append("LEFT JOIN moh_bill_third_party third_party ")
				.append("ON third_party.third_party_id = policy.third_party_id ")
				.append("LEFT JOIN moh_bill_insurance_rate insurance_rate ON insurance_rate.insurance_rate_id = (")
				.append("SELECT rate.insurance_rate_id FROM moh_bill_insurance_rate rate ")
				.append("WHERE rate.insurance_id = policy.insurance_id ")
				.append("AND rate.start_date <= DATE(COALESCE(bill_item.service_date, payment.date_received)) ")
				.append("ORDER BY rate.start_date DESC, rate.insurance_rate_id DESC LIMIT 1) ")
				.append("WHERE COALESCE(payment.voided, 0) = 0 AND payment.date_received IS NOT NULL ");
		appendBounds(sql, dateBounded, paymentIdBounded);
		sql.append("GROUP BY payment.bill_payment_id, payment.patient_bill_id, payment.collector, ")
				.append("cash_payment.cash_payment_id, deposit_payment.deposit_payment_id, ")
				.append("payment.date_received, payment.amount_paid, payment.void_reason, beneficiary.patient_id, ")
				.append("patient_name.given_name, patient_name.middle_name, patient_name.family_name, ")
				.append("patient_bill.phoneNumber, patient_bill.transactionStatus, hospital_service.service_id, ")
				.append("hospital_service.name, COALESCE(bill_item.item_type, 1) ")
				.append("ON DUPLICATE KEY UPDATE ").append(updateAssignments());
		return sql.toString();
	}

	static String reportSql(boolean filterByCollector, boolean filterByPaymentType, boolean filterByServices,
			String reportType) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT report_etl.bill_payment_id, report_etl.patient_bill_id, report_etl.date_received, ")
				.append("report_etl.amount_paid, report_etl.patient_id, report_etl.beneficiary_name, ")
				.append("report_etl.phone_number, report_etl.transaction_status, report_etl.service_id, ")
				.append("report_etl.service_name, SUM(report_etl.due_amount) AS due_amount FROM ")
				.append(TABLE).append(" report_etl ")
				.append("WHERE report_etl.date_received BETWEEN :startDate AND :endDate ");
		if (filterByCollector) {
			sql.append("AND report_etl.collector_id = :collectorId ");
		}
		if (filterByPaymentType) {
			sql.append("AND report_etl.payment_type = :paymentType ");
		}
		if (filterByServices) {
			sql.append("AND (report_etl.service_id = 0 OR report_etl.service_id IN (:serviceIds)) ");
		}
		if ("NO_DCP_Report".equals(reportType)) {
			sql.append("AND (report_etl.service_id = 0 OR report_etl.item_type <> 2) ");
		} else if ("DCP_Report".equals(reportType)) {
			sql.append("AND (report_etl.service_id = 0 OR report_etl.item_type = 2) ");
		}
		sql.append("GROUP BY report_etl.bill_payment_id, report_etl.patient_bill_id, report_etl.date_received, ")
				.append("report_etl.amount_paid, report_etl.patient_id, report_etl.beneficiary_name, ")
				.append("report_etl.phone_number, report_etl.transaction_status, report_etl.service_id, ")
				.append("report_etl.service_name ")
				.append("ORDER BY report_etl.date_received, report_etl.bill_payment_id, report_etl.service_id");
		return sql.toString();
	}

	static String totalPaidSql(boolean filterByCollector, boolean filterByPaymentType) {
		StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(report_etl.amount_paid), 0) FROM ")
				.append(TABLE).append(" report_etl WHERE report_etl.service_id = 0 ")
				.append("AND report_etl.payment_void_reason IS NULL ")
				.append("AND report_etl.date_received BETWEEN :startDate AND :endDate ");
		if (filterByCollector) {
			sql.append("AND report_etl.collector_id = :collectorId ");
		}
		if (filterByPaymentType) {
			sql.append("AND report_etl.payment_type = :paymentType ");
		}
		return sql.toString();
	}

	private static String insertPrefix() {
		return "INSERT INTO " + TABLE + " (bill_payment_id, patient_bill_id, collector_id, payment_type, "
				+ "date_received, amount_paid, payment_void_reason, patient_id, beneficiary_name, phone_number, "
				+ "transaction_status, service_id, service_name, item_type, due_amount, etl_loaded_at) ";
	}

	private static String paymentSourceJoins() {
		return "FROM moh_bill_payment payment "
				+ "LEFT JOIN moh_bill_cash_payment cash_payment "
				+ "ON cash_payment.cash_payment_id = payment.bill_payment_id "
				+ "LEFT JOIN moh_bill_deposit_payment deposit_payment "
				+ "ON deposit_payment.deposit_payment_id = payment.bill_payment_id "
				+ "LEFT JOIN moh_bill_patient_bill patient_bill "
				+ "ON patient_bill.patient_bill_id = payment.patient_bill_id "
				+ "LEFT JOIN moh_bill_consommation consumption ON consumption.consommation_id = ("
				+ "SELECT MIN(consumption_match.consommation_id) FROM moh_bill_consommation consumption_match "
				+ "WHERE consumption_match.patient_bill_id = payment.patient_bill_id) "
				+ "LEFT JOIN moh_bill_beneficiary beneficiary "
				+ "ON beneficiary.beneficiary_id = consumption.beneficiary_id "
				+ selectedPersonNameJoin("patient_name", "beneficiary.patient_id");
	}

	private static String paymentTypeExpression() {
		return "CASE WHEN cash_payment.cash_payment_id IS NOT NULL THEN 'CASH' "
				+ "WHEN deposit_payment.deposit_payment_id IS NOT NULL THEN 'DEPOSIT' ELSE 'OTHER' END";
	}

	private static void appendBounds(StringBuilder sql, boolean dateBounded, boolean paymentIdBounded) {
		if (dateBounded) {
			sql.append("AND payment.date_received >= :refreshFrom ");
		}
		if (paymentIdBounded) {
			sql.append("AND payment.bill_payment_id >= :paymentIdFrom ")
					.append("AND payment.bill_payment_id <= :paymentIdTo ");
		}
	}

	private static String selectedPersonNameJoin(String alias, String personIdExpression) {
		return "LEFT JOIN person_name " + alias + " ON " + alias + ".person_name_id = ("
				+ "SELECT name.person_name_id FROM person_name name WHERE name.person_id = " + personIdExpression + " "
				+ "AND COALESCE(name.voided, 0) = 0 "
				+ "ORDER BY name.preferred DESC, name.person_name_id DESC LIMIT 1) ";
	}

	private static String personNameExpression(String alias) {
		return "TRIM(CONCAT_WS(' ', NULLIF(" + alias + ".given_name, ''), NULLIF(" + alias
				+ ".middle_name, ''), NULLIF(" + alias + ".family_name, '')))";
	}

	private static String updateAssignments() {
		String[] columns = {
				"patient_bill_id", "collector_id", "payment_type", "date_received", "amount_paid",
				"payment_void_reason", "patient_id", "beneficiary_name", "phone_number", "transaction_status",
				"service_name", "due_amount", "etl_loaded_at"
		};
		StringBuilder assignments = new StringBuilder();
		for (String column : columns) {
			if (assignments.length() > 0) {
				assignments.append(", ");
			}
			assignments.append(column).append(" = VALUES(").append(column).append(")");
		}
		return assignments.toString();
	}
}
