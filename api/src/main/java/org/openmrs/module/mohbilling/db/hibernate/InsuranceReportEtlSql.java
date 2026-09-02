package org.openmrs.module.mohbilling.db.hibernate;

/**
 * Native SQL used to materialize the insurance report from MoH Billing tables.
 */
final class InsuranceReportEtlSql {

	private static final String TABLE = "moh_bill_insurance_report_etl";
	private static final String CATEGORY = "UPPER(TRIM(COALESCE(hs.name, sc.name, fsp.category, 'AUTRES')))";

	private InsuranceReportEtlSql() {
	}

	static String deleteSql(boolean bounded) {
		return bounded ? "DELETE FROM " + TABLE + " WHERE closing_date >= :refreshFrom"
				: "DELETE FROM " + TABLE;
	}

	static String deleteByGlobalBillIdRangeSql() {
		return "DELETE FROM " + TABLE
				+ " WHERE global_bill_id >= :globalBillIdFrom AND global_bill_id <= :globalBillIdTo";
	}

	static String latestClosingDateSql() {
		return "SELECT MAX(closing_date) FROM " + TABLE;
	}

	static String sourceGlobalBillIdSql(boolean minimum) {
		return "SELECT " + (minimum ? "MIN" : "MAX") + "(global_bill_id) FROM moh_bill_global_bill "
				+ "WHERE closed = 1 AND COALESCE(voided, 0) = 0 AND closing_date IS NOT NULL";
	}

	static String reportSql(boolean filterByInsurance) {
		return "SELECT report_etl.admission_date, report_etl.closing_date, report_etl.beneficiary_name, "
					+ "report_etl.household_head_name, report_etl.family_code, report_etl.beneficiary_level, "
					+ "report_etl.card_number, report_etl.company_name, report_etl.insurance_name, "
					+ "report_etl.age, report_etl.birth_date, report_etl.gender, "
					+ "report_etl.primary_identifier, report_etl.doctor_name, "
					+ "report_etl.insurance_id, report_etl.global_bill_id, report_etl.global_bill_identifier, "
					+ "report_etl.insurance_rate, report_etl.insurance_flat_fee, "
					+ "report_etl.medicaments AS MEDICAMENTS, report_etl.consultation AS CONSULTATION, "
					+ "report_etl.hospitalisation AS HOSPITALISATION, report_etl.laboratoire AS LABORATOIRE, "
					+ "report_etl.consommables AS CONSOMMABLES, report_etl.ambulance AS AMBULANCE, "
					+ "report_etl.oxygenotherapie AS OXYGENOTHERAPIE, report_etl.autres AS AUTRES, "
					+ "report_etl.formalites_administratives AS `FORMALITES ADMINISTRATIVES`, "
					+ "report_etl.imaging AS IMAGING, report_etl.procedures AS `PROCED.`, report_etl.total_100, "
					+ "report_etl.total_insurance, report_etl.total_patient FROM " + TABLE + " report_etl "
					+ "WHERE " + (filterByInsurance ? "report_etl.insurance_id = :insuranceId AND " : "")
					+ "report_etl.closing_date BETWEEN :startDate AND :endDate "
					+ "ORDER BY report_etl.closing_date, report_etl.global_bill_id";
	}

	static String insertSql(boolean bounded) {
		return insertSql(bounded, false);
	}

	static String insertByGlobalBillIdRangeSql() {
		return insertSql(false, true);
	}

	private static String insertSql(boolean dateBounded, boolean globalBillIdBounded) {
		String totalInsurance = "CASE WHEN COALESCE(ir.flatFee, 0) > 0 "
				+ "THEN GREATEST(amounts.total_100 - ir.flatFee, 0) "
				+ "ELSE ROUND(amounts.total_100 * COALESCE(ir.rate, 0) / 100, 2) END";
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(TABLE).append(" (")
				.append("global_bill_id, insurance_id, patient_id, admission_date, closing_date, beneficiary_name, ")
				.append("household_head_name, family_code, beneficiary_level, card_number, company_name, insurance_name, age, ")
				.append("birth_date, gender, primary_identifier, doctor_name, global_bill_identifier, insurance_rate, insurance_flat_fee, ")
				.append("medicaments, consultation, hospitalisation, laboratoire, consommables, ambulance, ")
				.append("oxygenotherapie, autres, formalites_administratives, imaging, procedures, total_100, ")
				.append("total_insurance, total_patient, etl_loaded_at) ")
				.append("SELECT gb.global_bill_id, gb.insurance_id, b.patient_id, admission.admission_date, gb.closing_date, ")
					.append(personNameExpression("patient_name"))
					.append(", NULLIF(TRIM(b.owner_name), ''), NULLIF(TRIM(b.owner_code), ''), ")
				.append("b.level, COALESCE(NULLIF(TRIM(policy.insurance_card_no), ''), NULLIF(TRIM(b.policy_id_number), '')), ")
				.append("NULLIF(TRIM(b.company), ''), insurance.name, ")
				.append("TIMESTAMPDIFF(YEAR, person.birthdate, DATE(gb.closing_date)), ")
				.append("person.birthdate, person.gender, primary_identifier.identifier, ")
				.append("COALESCE(NULLIF(").append(personNameExpression("closing_user_name"))
				.append(", ''), closing_user.username), gb.bill_identifier, ")
				.append("COALESCE(ir.rate, 0), COALESCE(ir.flatFee, 0), amounts.medicaments, amounts.consultation, ")
				.append("amounts.hospitalisation, amounts.laboratoire, amounts.consommables, amounts.ambulance, ")
				.append("amounts.oxygenotherapie, amounts.autres, amounts.formalites_administratives, amounts.imaging, ")
				.append("amounts.procedures, amounts.total_100, ").append(totalInsurance).append(", ")
				.append("amounts.total_100 - (").append(totalInsurance).append("), NOW() ")
				.append("FROM moh_bill_global_bill gb ")
				.append("JOIN moh_bill_admission admission ON admission.admission_id = gb.admission_id ")
				.append("JOIN moh_bill_beneficiary b ON b.beneficiary_id = (")
				.append("SELECT MIN(c.beneficiary_id) FROM moh_bill_consommation c ")
				.append("WHERE c.global_bill_id = gb.global_bill_id AND COALESCE(c.voided, 0) = 0) ")
				.append("JOIN moh_bill_insurance_policy policy ON policy.insurance_policy_id = b.insurance_policy_id ")
				.append("JOIN moh_bill_insurance insurance ON insurance.insurance_id = gb.insurance_id ")
				.append("JOIN person person ON person.person_id = b.patient_id ")
				.append(selectedPersonNameJoin("patient_name", "b.patient_id"))
				.append("LEFT JOIN patient_identifier primary_identifier ON primary_identifier.patient_identifier_id = (")
				.append("SELECT pi.patient_identifier_id FROM patient_identifier pi ")
				.append("WHERE pi.patient_id = b.patient_id AND pi.identifier_type = :primaryIdentifierTypeId ")
				.append("AND COALESCE(pi.voided, 0) = 0 ")
				.append("ORDER BY pi.preferred DESC, pi.patient_identifier_id DESC LIMIT 1) ")
				.append("LEFT JOIN users closing_user ON closing_user.user_id = gb.closed_by ")
				.append(selectedPersonNameJoin("closing_user_name", "closing_user.person_id"))
				.append("JOIN (").append(amountsSql(dateBounded, globalBillIdBounded))
				.append(") amounts ON amounts.global_bill_id = gb.global_bill_id ")
				.append("LEFT JOIN moh_bill_insurance_rate ir ON ir.insurance_rate_id = (")
				.append("SELECT ir2.insurance_rate_id FROM moh_bill_insurance_rate ir2 ")
				.append("WHERE ir2.insurance_id = gb.insurance_id AND ir2.start_date <= DATE(gb.closing_date) ")
				.append("ORDER BY ir2.start_date DESC, ir2.insurance_rate_id DESC LIMIT 1) ")
				.append("WHERE gb.closed = 1 AND COALESCE(gb.voided, 0) = 0 AND gb.closing_date IS NOT NULL ");
		if (dateBounded) {
			sql.append("AND gb.closing_date >= :refreshFrom ");
		}
		if (globalBillIdBounded) {
			sql.append("AND gb.global_bill_id >= :globalBillIdFrom ")
					.append("AND gb.global_bill_id <= :globalBillIdTo ");
		}
		sql.append("ON DUPLICATE KEY UPDATE ")
				.append(updateAssignments());
		return sql.toString();
	}

	private static String amountsSql(boolean dateBounded, boolean globalBillIdBounded) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT classified.global_bill_id, ")
				.append(categorySum("medicaments", "MEDICAMENTS")).append(", ")
				.append(categorySum("consultation", "CONSULTATION")).append(", ")
				.append(categorySum("hospitalisation", "HOSPITALISATION")).append(", ")
				.append(categorySum("laboratoire", "LABORATOIRE")).append(", ")
				.append(categorySum("consommables", "CONSOMMABLES")).append(", ")
				.append(categorySum("ambulance", "AMBULANCE")).append(", ")
				.append(categorySum("oxygenotherapie", "OXYGENOTHERAPIE")).append(", ")
				.append(categorySum("autres", "AUTRES")).append(", ")
				.append(categorySum("formalites_administratives", "FORMALITES ADMINISTRATIVES")).append(", ")
				.append(categorySum("imaging", "IMAGING")).append(", ")
				.append(categorySum("procedures", "PROCEDURES")).append(", ")
				.append("ROUND(SUM(classified.amount), 2) AS total_100 FROM (")
				.append("SELECT c.global_bill_id, ")
				.append("COALESCE(psb.quantity, 0) * COALESCE(psb.unit_price, 0) AS amount, ")
				.append(reportCategorySql()).append(" AS report_category ")
					.append("FROM moh_bill_consommation c ")
					.append(dateBounded
							? "JOIN moh_bill_global_bill filtered_gb ON filtered_gb.global_bill_id = c.global_bill_id "
							: "")
					.append("JOIN moh_bill_patient_service_bill psb ON psb.consommation_id = c.consommation_id ")
				.append("LEFT JOIN moh_bill_hop_service hs ON hs.service_id = psb.service_id ")
				.append("LEFT JOIN moh_bill_billable_service bs ON bs.billable_service_id = psb.billable_service_id ")
				.append("LEFT JOIN moh_bill_service_category sc ON sc.service_category_id = bs.service_category_id ")
				.append("LEFT JOIN moh_bill_facility_service_price fsp ON fsp.facility_service_price_id = bs.facility_service_price_id ")
				.append("WHERE COALESCE(c.voided, 0) = 0 AND COALESCE(psb.voided, 0) = 0 ")
				.append("AND COALESCE(psb.item_type, 1) <> 2 ");
		if (dateBounded) {
			sql.append("AND filtered_gb.closed = 1 AND COALESCE(filtered_gb.voided, 0) = 0 ")
					.append("AND filtered_gb.closing_date >= :refreshFrom ");
		}
		if (globalBillIdBounded) {
			sql.append("AND c.global_bill_id >= :globalBillIdFrom ")
					.append("AND c.global_bill_id <= :globalBillIdTo ");
		}
		sql.append(") classified GROUP BY classified.global_bill_id");
		return sql.toString();
	}

	private static String reportCategorySql() {
		return "CASE "
				+ "WHEN FIND_IN_SET(CAST(psb.service_id AS CHAR), :imagingServiceIds) > 0 THEN 'IMAGING' "
				+ "WHEN FIND_IN_SET(CAST(psb.service_id AS CHAR), :procedureServiceIds) > 0 THEN 'PROCEDURES' "
				+ "WHEN " + CATEGORY + " IN ('MEDICAMENTS', 'MEDICAMENT', 'MEDIC', 'PHARMACY') THEN 'MEDICAMENTS' "
				+ "WHEN " + CATEGORY + " IN ('CONSULTATION', 'CONSULT') THEN 'CONSULTATION' "
				+ "WHEN " + CATEGORY + " IN ('HOSPITALISATION', 'HOSPITAL') THEN 'HOSPITALISATION' "
				+ "WHEN " + CATEGORY + " IN ('LABORATOIRE', 'LABO') THEN 'LABORATOIRE' "
				+ "WHEN " + CATEGORY + " IN ('CONSOMMABLES', 'CONSOM') THEN 'CONSOMMABLES' "
				+ "WHEN " + CATEGORY + " = 'AMBULANCE' THEN 'AMBULANCE' "
				+ "WHEN " + CATEGORY + " IN ('OXYGENOTHERAPIE', 'OXYGENO') THEN 'OXYGENOTHERAPIE' "
				+ "WHEN " + CATEGORY + " = 'FORMALITES ADMINISTRATIVES' THEN 'FORMALITES ADMINISTRATIVES' "
				+ "ELSE 'AUTRES' END";
	}

	private static String selectedPersonNameJoin(String alias, String personIdExpression) {
		return "LEFT JOIN person_name " + alias + " ON " + alias + ".person_name_id = ("
				+ "SELECT pn.person_name_id FROM person_name pn WHERE pn.person_id = " + personIdExpression + " "
				+ "AND COALESCE(pn.voided, 0) = 0 ORDER BY pn.preferred DESC, pn.person_name_id DESC LIMIT 1) ";
	}

	private static String personNameExpression(String alias) {
		return "TRIM(CONCAT_WS(' ', NULLIF(" + alias + ".given_name, ''), NULLIF(" + alias
				+ ".middle_name, ''), NULLIF(" + alias + ".family_name, '')))";
	}

	private static String categorySum(String alias, String reportCategory) {
		return "ROUND(SUM(CASE WHEN classified.report_category = '" + reportCategory
				+ "' THEN classified.amount ELSE 0 END), 2) AS " + alias;
	}

	private static String updateAssignments() {
		String[] columns = {
				"insurance_id", "patient_id", "admission_date", "closing_date", "beneficiary_name",
				"household_head_name", "family_code", "beneficiary_level", "card_number", "company_name",
				"insurance_name", "age", "birth_date", "gender", "primary_identifier", "doctor_name",
				"global_bill_identifier", "insurance_rate",
				"insurance_flat_fee", "medicaments", "consultation", "hospitalisation", "laboratoire",
				"consommables", "ambulance", "oxygenotherapie", "autres", "formalites_administratives",
				"imaging", "procedures", "total_100", "total_insurance", "total_patient", "etl_loaded_at"
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
