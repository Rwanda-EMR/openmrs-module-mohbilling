package org.openmrs.module.mohbilling.db.hibernate;

import org.junit.Assert;
import org.junit.Test;

public class BillReportEtlSqlTest {

	@Test
	public void insertSql_shouldUpsertOnPatientServiceBillId() {
		String sql = BillReportEtlSql.insertSql();

		Assert.assertTrue(sql.contains("INSERT INTO moh_bill_bill_report_etl"));
		Assert.assertTrue(sql.contains("patient_service_bill_id"));
		Assert.assertTrue(sql.contains("primary_diagnosis"));
		Assert.assertTrue(sql.contains("transfer_available"));
		Assert.assertTrue(sql.contains("transfer_validity"));
		Assert.assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
		Assert.assertTrue(sql.contains("etl_loaded_at = NOW()"));
	}

	@Test
	public void deleteByAdmissionIdRangeSql_shouldBoundAdmissionIds() {
		String sql = BillReportEtlSql.deleteByAdmissionIdRangeSql();

		Assert.assertTrue(sql.contains(":admissionIdFrom"));
		Assert.assertTrue(sql.contains(":admissionIdTo"));
	}

	@Test
	public void deleteByAdmissionDateSql_shouldBoundRefreshFrom() {
		Assert.assertTrue(BillReportEtlSql.deleteByAdmissionDateSql().contains(":refreshFrom"));
	}

	@Test
	public void reportSql_shouldFilterByAdmissionDateAndOptionalInsurance() {
		String byId = BillReportEtlSql.reportSql(true, false);
		Assert.assertTrue(byId.contains("admission_date >= :startDate"));
		Assert.assertTrue(byId.contains("insurance_id = :insuranceId"));
		Assert.assertFalse(byId.contains(":insuranceCategory"));

		String byCategory = BillReportEtlSql.reportSql(false, true);
		Assert.assertTrue(byCategory.contains("LOWER(insurance_category) = LOWER(:insuranceCategory)"));
		Assert.assertFalse(byCategory.contains("insurance_id = :insuranceId"));

		String unfiltered = BillReportEtlSql.reportSql(false, false);
		Assert.assertFalse(unfiltered.contains("insurance_id = :insuranceId"));
		Assert.assertFalse(unfiltered.contains(":insuranceCategory"));
	}

	@Test
	public void sourceAdmissionIdSql_shouldReadNonVoidedAdmissionsFromStartDate() {
		String min = BillReportEtlSql.sourceAdmissionIdSql(true);
		Assert.assertTrue(min.contains("MIN(admission_id)"));
		Assert.assertTrue(min.contains("moh_bill_admission"));
		Assert.assertTrue(min.contains("COALESCE(voided, 0) = 0"));
		Assert.assertTrue(min.contains("admission_date >= :startDate"));

		String max = BillReportEtlSql.sourceAdmissionIdSql(false);
		Assert.assertTrue(max.contains("MAX(admission_id)"));
		Assert.assertTrue(max.contains("admission_date >= :startDate"));
	}

	@Test
	public void deleteBeforeAdmissionDateSql_shouldBoundStartDate() {
		Assert.assertTrue(BillReportEtlSql.deleteBeforeAdmissionDateSql().contains(":startDate"));
	}
}
