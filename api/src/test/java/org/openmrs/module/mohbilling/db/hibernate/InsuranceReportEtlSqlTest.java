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
		Assert.assertTrue(sql.contains("filtered_gb.closing_date >= :refreshFrom"));
		Assert.assertTrue(sql.contains("FIND_IN_SET(CAST(psb.service_id AS CHAR), :imagingServiceIds)"));
		Assert.assertTrue(sql.contains("FIND_IN_SET(CAST(psb.service_id AS CHAR), :procedureServiceIds)"));
		Assert.assertTrue(sql.contains("ir2.start_date <= DATE(gb.closing_date)"));
		Assert.assertTrue(sql.contains("insurance_name"));
		Assert.assertTrue(sql.contains("primary_identifier"));
		Assert.assertTrue(sql.contains("pi.identifier_type = :primaryIdentifierTypeId"));
		Assert.assertTrue(sql.contains("closing_user.user_id = gb.closed_by"));
		Assert.assertTrue(sql.contains("closing_user_name"));
		Assert.assertFalse(sql.contains("psb.creator"));
		Assert.assertFalse(sql.contains("GROUP_CONCAT"));
		Assert.assertTrue(sql.contains("amounts.total_100 - ("));
		Assert.assertTrue(sql.contains("AND gb.closing_date >= :refreshFrom"));
		Assert.assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
		Assert.assertFalse(sql.toLowerCase().contains("mamba"));
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
	public void reportSql_shouldFilterByInsuranceAndClosingDate() {
		String sql = InsuranceReportEtlSql.reportSql(true);

		Assert.assertTrue(sql.contains("report_etl.insurance_id = :insuranceId"));
		Assert.assertTrue(sql.contains("report_etl.closing_date BETWEEN :startDate AND :endDate"));
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
		Assert.assertTrue(sql.contains("report_etl.closing_date BETWEEN :startDate AND :endDate"));
	}
}
