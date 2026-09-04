package org.openmrs.module.mohbilling.db.hibernate;

import org.junit.Assert;
import org.junit.Test;

public class CashierReportEtlSqlTest {

	@Test
	public void headerInsertSql_shouldMaterializeEveryNonVoidedPayment() {
		String sql = CashierReportEtlSql.headerInsertSql(true);

		Assert.assertTrue(sql.contains("FROM moh_bill_payment payment"));
		Assert.assertTrue(sql.contains("LEFT JOIN moh_bill_cash_payment cash_payment"));
		Assert.assertTrue(sql.contains("LEFT JOIN moh_bill_deposit_payment deposit_payment"));
		Assert.assertTrue(sql.contains("payment.date_received >= :refreshFrom"));
		Assert.assertTrue(sql.contains("SELECT MIN(consumption_match.consommation_id)"));
		Assert.assertTrue(sql.contains("0, '', 0, 0, NOW()"));
		Assert.assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
	}

	@Test
	public void serviceInsertSql_shouldMatchCashierDueCalculation() {
		String sql = CashierReportEtlSql.serviceInsertSql(false);

		Assert.assertTrue(sql.contains("JOIN moh_bill_paid_service_bill paid_item"));
		Assert.assertTrue(sql.contains("COALESCE(paid_item.voided, 0) = 0"));
		Assert.assertTrue(sql.contains("COALESCE(bill_item.voided, 0) = 0"));
		Assert.assertTrue(sql.contains("paid_item.paid_quantity"));
		Assert.assertTrue(sql.contains("bill_item.unit_price"));
		Assert.assertTrue(sql.contains("100 - insurance_rate.rate - COALESCE(third_party.rate, 0)"));
		Assert.assertTrue(sql.contains("rate.start_date <= DATE(COALESCE(bill_item.service_date"));
		Assert.assertTrue(sql.contains("GROUP BY payment.bill_payment_id"));
		Assert.assertFalse(sql.contains(":refreshFrom"));
	}

	@Test
	public void batchSql_shouldBoundDeleteAndBothInsertsByPaymentId() {
		String header = CashierReportEtlSql.headerInsertByPaymentIdRangeSql();
		String services = CashierReportEtlSql.serviceInsertByPaymentIdRangeSql();

		Assert.assertTrue(header.contains("payment.bill_payment_id >= :paymentIdFrom"));
		Assert.assertTrue(header.contains("payment.bill_payment_id <= :paymentIdTo"));
		Assert.assertTrue(services.contains("payment.bill_payment_id >= :paymentIdFrom"));
		Assert.assertTrue(services.contains("payment.bill_payment_id <= :paymentIdTo"));
		Assert.assertTrue(CashierReportEtlSql.deleteByPaymentIdRangeSql().contains(":paymentIdFrom"));
	}

	@Test
	public void reportSql_shouldPreserveHeadersAndFilterOrdinaryServices() {
		String sql = CashierReportEtlSql.reportSql(true, true, true, "NO_DCP_Report");

		Assert.assertTrue(sql.contains("report_etl.date_received BETWEEN :startDate AND :endDate"));
		Assert.assertTrue(sql.contains("report_etl.collector_id = :collectorId"));
		Assert.assertTrue(sql.contains("report_etl.payment_type = :paymentType"));
		Assert.assertTrue(sql.contains("report_etl.service_id IN (:serviceIds)"));
		Assert.assertTrue(sql.contains("report_etl.item_type <> 2"));
		Assert.assertTrue(sql.contains("report_etl.service_id = 0 OR"));
	}

	@Test
	public void reportSql_shouldFilterDcpAndAllowAllItemTypes() {
		String dcp = CashierReportEtlSql.reportSql(false, false, true, "DCP_Report");
		String all = CashierReportEtlSql.reportSql(false, false, false, "All");

		Assert.assertTrue(dcp.contains("report_etl.item_type = 2"));
		Assert.assertFalse(all.contains("report_etl.item_type = 2"));
		Assert.assertFalse(all.contains("report_etl.item_type <> 2"));
		Assert.assertFalse(all.contains("report_etl.service_id = 0 "));
		Assert.assertTrue(all.contains("report_etl.service_name"));
	}

	@Test
	public void totalPaidSql_shouldCountEachPaymentHeaderOnce() {
		String sql = CashierReportEtlSql.totalPaidSql(true, false);

		Assert.assertTrue(sql.contains("report_etl.service_id = 0"));
		Assert.assertTrue(sql.contains("report_etl.payment_void_reason IS NULL"));
		Assert.assertTrue(sql.contains("report_etl.collector_id = :collectorId"));
	}
}
