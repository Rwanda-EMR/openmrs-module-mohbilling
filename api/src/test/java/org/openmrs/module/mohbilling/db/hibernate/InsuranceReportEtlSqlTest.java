package org.openmrs.module.mohbilling.db.hibernate;

import org.junit.Assert;
import org.junit.Test;

public class InsuranceReportEtlSqlTest {

	@Test
	public void insertSql_shouldBuildAnIdempotentDirectBillingRefresh() {
		String sql = InsuranceReportEtlSql.insertSql(true);

		Assert.assertTrue(sql.contains("FROM moh_bill_global_bill gb"));
		Assert.assertTrue(sql.contains("SELECT MIN(c.beneficiary_id) FROM moh_bill_consommation c"));
		Assert.assertTrue(sql.contains("c.global_bill_id = gb.global_bill_id"));
		Assert.assertTrue(sql.contains("JOIN moh_bill_patient_service_bill psb"));
		Assert.assertTrue(sql.contains("COALESCE(psb.item_type, 1) <> 2"));
		Assert.assertTrue(sql.contains("JOIN moh_bill_global_bill filtered_gb"));
		Assert.assertTrue(sql.contains("report_period_month"));
		Assert.assertTrue(sql.contains("MIN(classified.item_created) AS min_created"));
		Assert.assertTrue(sql.contains("MAX(classified.item_created) AS max_created"));
		Assert.assertTrue(sql.contains("GROUP BY classified.global_bill_id, classified.report_period_month"));
		Assert.assertTrue(sql.contains("FIND_IN_SET(CAST(psb.service_id AS CHAR), :imagingServiceIds)"));
		Assert.assertTrue(sql.contains("FIND_IN_SET(CAST(psb.service_id AS CHAR), :procedureServiceIds)"));
		Assert.assertTrue(sql.contains("insurance_name"));
		Assert.assertTrue(sql.contains("primary_identifier"));
		Assert.assertTrue(sql.contains("pi.identifier_type = :primaryIdentifierTypeId"));
		Assert.assertTrue(sql.contains("closing_user.user_id = gb.closed_by"));
		Assert.assertTrue(sql.contains("closing_user_name"));
		Assert.assertFalse(sql.contains("psb.creator"));
		Assert.assertFalse(sql.contains("GROUP_CONCAT"));
		Assert.assertTrue(sql.contains("amounts.total_100 - ("));
		Assert.assertTrue(sql.contains("psb.created_date >= :refreshFrom"));
		Assert.assertTrue(sql.contains(">= :etlStartDate"));
		Assert.assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
		Assert.assertFalse(sql.toLowerCase().contains("mamba"));
		Assert.assertFalse(sql.contains("gb.closed = 1"));
	}

	@Test
	public void insertByGlobalBillIdRangeSql_shouldBoundSourceAggregationAndOuterInsert() {
		String sql = InsuranceReportEtlSql.insertByGlobalBillIdRangeSql();

		Assert.assertTrue(sql.contains("c.global_bill_id >= :globalBillIdFrom"));
		Assert.assertTrue(sql.contains("c.global_bill_id <= :globalBillIdTo"));
		Assert.assertTrue(sql.contains("gb.global_bill_id >= :globalBillIdFrom"));
		Assert.assertTrue(sql.contains("gb.global_bill_id <= :globalBillIdTo"));
		Assert.assertTrue(sql.contains("ORDER BY pn.preferred DESC"));
		Assert.assertFalse(sql.contains("GROUP BY pn.person_id"));
		Assert.assertTrue(InsuranceReportEtlSql.deleteByGlobalBillIdRangeSql()
				.contains("global_bill_id >= :globalBillIdFrom"));
	}

	@Test
	public void insertSql_shouldClassifyEnglishServiceNamesIntoTheirReportColumns() {
		String sql = InsuranceReportEtlSql.insertSql(false);

		Assert.assertTrue(sql.contains("IN ('HOSPITALISATION', 'HOSPITAL', 'HOSPITALIZATION') THEN 'HOSPITALISATION'"));
		Assert.assertTrue(sql.contains("IN ('LABORATOIRE', 'LABO', 'LABORATORY') THEN 'LABORATOIRE'"));
		Assert.assertTrue(sql.contains("IN ('FORMALITES ADMINISTRATIVES', 'ADMINISTRATION') "
				+ "THEN 'FORMALITES ADMINISTRATIVES'"));
	}

	@Test
	public void insertSql_shouldClassifyUnknownServicesAsAutresWithoutDoubleCounting() {
		String sql = InsuranceReportEtlSql.insertSql(false);

		Assert.assertTrue(sql.contains("ELSE 'AUTRES' END AS report_category"));
		Assert.assertTrue(sql.contains("classified.report_category = 'AUTRES'"));
		Assert.assertTrue(sql.contains("SUM(classified.amount)"));
		Assert.assertTrue(sql.contains("AS total_100"));
		Assert.assertFalse(sql.contains(":refreshFrom"));
	}

	@Test
	public void insertSql_shouldGiveConfiguredImagingServicesPrecedenceOverProceduresAndNames() {
		String sql = InsuranceReportEtlSql.insertSql(false);

		int imaging = sql.indexOf(":imagingServiceIds");
		int procedures = sql.indexOf(":procedureServiceIds");
		int namedCategory = sql.indexOf("IN ('MEDICAMENTS'");
		Assert.assertTrue(imaging >= 0);
		Assert.assertTrue(imaging < procedures);
		Assert.assertTrue(procedures < namedCategory);
	}

	@Test
	public void insertSql_shouldUseSyntheticDatesForCrossMonthPeriods() {
		String sql = InsuranceReportEtlSql.insertSql(false);

		Assert.assertTrue(sql.contains("ELSE amounts.min_created END"));
		Assert.assertTrue(sql.contains("ELSE amounts.max_created END"));
		Assert.assertTrue(sql.contains("DATE_FORMAT(CURDATE(), '%Y-%m')"));
	}

	@Test
	public void insertSql_shouldSkipItemsWithoutCreatedDateAndUseCreatedForCrossMonthClosing() {
		String sql = InsuranceReportEtlSql.insertSql(false);

		Assert.assertTrue(sql.contains("psb.created_date IS NOT NULL"));
		Assert.assertTrue(sql.contains("psb.created_date AS item_created"));
		Assert.assertTrue(sql.contains("ELSE amounts.max_created END"));
		Assert.assertTrue(sql.contains("COALESCE(gb.closing_date, amounts.max_created)"));
		// Must not stamp real GB closing onto past cross-month slices (wrong month).
		Assert.assertFalse(sql.contains("amounts.min_created, gb.closing_date)"));
	}

	@Test
	public void sourceAndListSql_shouldRequireCreatedDate() {
		Assert.assertTrue(InsuranceReportEtlSql.sourceGlobalBillIdSql(true).contains("psb.created_date IS NOT NULL"));
		Assert.assertTrue(InsuranceReportEtlSql.listSourceGlobalBillIdsSql(false, false)
				.contains("psb.created_date IS NOT NULL"));
	}

	@Test
	public void deleteSql_shouldClearByClosingOrAdmissionNotWholePeriodMonth() {
		String sql = InsuranceReportEtlSql.deleteSql(true);
		Assert.assertTrue(sql.contains("closing_date >= :refreshFrom"));
		Assert.assertTrue(sql.contains("admission_date >= :refreshFrom"));
		Assert.assertFalse(sql.contains("report_period_month >="));
	}

	@Test
	public void reportSql_shouldFilterByInsuranceAndAdmissionDate() {
		String sql = InsuranceReportEtlSql.reportSql(true);

		Assert.assertTrue(sql.contains("report_etl.insurance_id = :insuranceId"));
		Assert.assertTrue(sql.contains("report_etl.admission_date BETWEEN :startDate AND :endDate"));
		Assert.assertFalse(sql.contains("report_etl.closing_date BETWEEN :startDate AND :endDate"));
		Assert.assertTrue(sql.contains("procedures AS `PROCED.`"));
		Assert.assertTrue(sql.contains("report_etl.insurance_name"));
		Assert.assertTrue(sql.contains("report_etl.primary_identifier"));
		Assert.assertFalse(sql.contains("JOIN moh_bill_insurance"));
		Assert.assertFalse(sql.contains("JOIN patient_identifier"));
		Assert.assertFalse(sql.contains(":primaryIdentifierTypeId"));
	}

	@Test
	public void reportSql_shouldAllowAllInsurances() {
		String sql = InsuranceReportEtlSql.reportSql(false);

		Assert.assertFalse(sql.contains("report_etl.insurance_id = :insuranceId"));
		Assert.assertTrue(sql.contains("report_etl.admission_date BETWEEN :startDate AND :endDate"));
		Assert.assertFalse(sql.contains("report_etl.closing_date BETWEEN :startDate AND :endDate"));
	}

	@Test
	public void sourceGlobalBillIdSql_shouldIncludeOpenBillsWithLines() {
		String sql = InsuranceReportEtlSql.sourceGlobalBillIdSql(true);
		Assert.assertTrue(sql.contains("EXISTS"));
		Assert.assertTrue(sql.contains("COALESCE(psb.item_type, 1) <> 2"));
		Assert.assertTrue(sql.contains(":etlStartDate"));
		Assert.assertFalse(sql.contains("closed = 1"));
	}

	@Test
	public void listSourceGlobalBillIdsSql_shouldSupportIdAndDateBounds() {
		String both = InsuranceReportEtlSql.listSourceGlobalBillIdsSql(true, true);
		Assert.assertTrue(both.contains("DISTINCT gb.global_bill_id"));
		Assert.assertTrue(both.contains("gb.global_bill_id >= :globalBillIdFrom"));
		Assert.assertTrue(both.contains("psb.created_date >= :refreshFrom"));
		Assert.assertTrue(both.contains("gb.closing_date >= :refreshFrom"));
		Assert.assertTrue(both.contains(":etlStartDate"));
		// Void corrections must rematerialize even when the GB closed before refreshFrom.
		Assert.assertTrue(both.contains("psb.voided_date >= DATE(:refreshFrom)"));
		Assert.assertTrue(both.contains("COALESCE(psb.voided, 0) = 1"));

		String idsOnly = InsuranceReportEtlSql.listSourceGlobalBillIdsSql(true, false);
		Assert.assertTrue(idsOnly.contains(":globalBillIdFrom"));
		Assert.assertTrue(idsOnly.contains(":etlStartDate"));
		Assert.assertFalse(idsOnly.contains(":refreshFrom"));
		Assert.assertFalse(idsOnly.contains("voided_date"));
	}

	@Test
	public void listSourceGlobalBillIdsSql_dateBounded_shouldNotRequireAllowDenyInsuranceFilter() {
		String sql = InsuranceReportEtlSql.listSourceGlobalBillIdsSql(false, true);
		Assert.assertFalse(sql.contains("insuranceIds"));
		Assert.assertFalse(sql.contains("excludedInsurance"));
		Assert.assertTrue(sql.contains("voided_date"));
	}

	@Test
	public void deleteBeforeEtlStartDateSql_shouldClearPreFloorRows() {
		String sql = InsuranceReportEtlSql.deleteBeforeEtlStartDateSql();
		Assert.assertTrue(sql.contains("admission_date < :etlStartDate"));
		Assert.assertTrue(sql.contains("report_period_month < :etlStartDate"));
	}

	@Test
	public void deleteByGlobalBillIdSql_shouldTargetOneBill() {
		Assert.assertTrue(InsuranceReportEtlSql.deleteByGlobalBillIdSql()
				.contains("WHERE global_bill_id = :globalBillId"));
	}
}
