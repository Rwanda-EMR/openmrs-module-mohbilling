package org.openmrs.module.mohbilling.db.hibernate;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.model.BillInsuranceReconciliationItem;

public class BillInsuranceReconciliationSqlTest {

	@Test
	public void reconciliationSql_shouldCompareBothEtlTablesByGlobalBillId() {
		String sql = BillInsuranceReconciliationSql.reconciliationSql();

		Assert.assertTrue(sql.contains("moh_bill_bill_report_etl"));
		Assert.assertTrue(sql.contains("moh_bill_insurance_report_etl"));
		Assert.assertTrue(sql.contains("UNION ALL"));
		Assert.assertTrue(sql.contains(":startDate"));
		Assert.assertTrue(sql.contains(":endDate"));
		Assert.assertTrue(sql.contains("'Bill Report' AS appears_in"));
		Assert.assertTrue(sql.contains("'Insurance Report' AS appears_in"));
		Assert.assertTrue(sql.contains("Missing from Insurance Report (not closed)"));
		Assert.assertTrue(sql.contains("Missing from Bill Report"));
		Assert.assertTrue(sql.contains(":excludedInsuranceName"));
		Assert.assertFalse(sql.contains("insurance_id ="));
		Assert.assertFalse(sql.contains("insurance_category"));
	}

	@Test
	public void appearsInConstants_shouldMatchSqlLabels() {
		Assert.assertEquals("Bill Report", BillInsuranceReconciliationItem.APPEARS_IN_BILL_REPORT);
		Assert.assertEquals("Insurance Report", BillInsuranceReconciliationItem.APPEARS_IN_INSURANCE_REPORT);
	}
}
