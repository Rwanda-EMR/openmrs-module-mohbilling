package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes the materialized cashier report from local billing payment tables.
 */
public class CashierReportEtlTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(CashierReportEtlTask.class);
	private static final int DEFAULT_OVERLAP_DAYS = 7;
	private static final int FULL_REFRESH_BATCH_SIZE = 25000;
	private static final String MATERIALIZATION_VERSION = "1";
	private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

	@Override
	public void execute() {
		if (!RUNNING.compareAndSet(false, true)) {
			log.warn("Skipped cashier report ETL refresh because another refresh is still running.");
			return;
		}

		try {
			BillingService billingService = Context.getService(BillingService.class);
			Date latestPaymentDate = billingService.getLatestCashierReportEtlPaymentDate();
			String loadedVersion = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_CASHIER_REPORT_ETL_LOADED_VERSION, "");
			boolean fullRefresh = latestPaymentDate == null || !MATERIALIZATION_VERSION.equals(loadedVersion);
			Date refreshFrom = fullRefresh ? null : calculateRefreshFrom(latestPaymentDate, getOverlapDays());
			int loaded = fullRefresh
					? refreshAllInBatches(billingService)
					: billingService.refreshCashierReportEtl(refreshFrom);
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_CASHIER_REPORT_ETL_LOADED_VERSION, MATERIALIZATION_VERSION);
			log.info("Cashier report ETL task loaded " + loaded + " rows; refreshFrom=" + refreshFrom);
		} catch (Exception e) {
			log.error("Cashier report ETL task failed", e);
		} finally {
			RUNNING.set(false);
		}
	}

	static int refreshAllInBatches(BillingService billingService) {
		Integer minimumPaymentId = billingService.getMinimumCashierReportSourcePaymentId();
		Integer maximumPaymentId = billingService.getMaximumCashierReportSourcePaymentId();
		if (minimumPaymentId == null || maximumPaymentId == null) {
			return 0;
		}

		int totalLoaded = 0;
		int paymentIdFrom = minimumPaymentId;
		while (paymentIdFrom <= maximumPaymentId) {
			int paymentIdTo = (int) Math.min((long) maximumPaymentId,
					(long) paymentIdFrom + FULL_REFRESH_BATCH_SIZE - 1L);
			totalLoaded += billingService.refreshCashierReportEtlByPaymentIdRange(paymentIdFrom, paymentIdTo);
			if (paymentIdTo == maximumPaymentId) {
				break;
			}
			paymentIdFrom = paymentIdTo + 1;
		}
		return totalLoaded;
	}

	static Date calculateRefreshFrom(Date latestPaymentDate, int overlapDays) {
		if (latestPaymentDate == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(latestPaymentDate);
		calendar.add(Calendar.DAY_OF_MONTH, -Math.max(0, overlapDays));
		return calendar.getTime();
	}

	private int getOverlapDays() {
		String configured = Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_CASHIER_REPORT_ETL_OVERLAP_DAYS,
				String.valueOf(DEFAULT_OVERLAP_DAYS));
		try {
			int value = Integer.parseInt(configured);
			return value >= 0 ? value : DEFAULT_OVERLAP_DAYS;
		} catch (NumberFormatException e) {
			log.warn("Invalid cashier report ETL overlap days: " + configured
					+ ". Using " + DEFAULT_OVERLAP_DAYS + ".");
			return DEFAULT_OVERLAP_DAYS;
		}
	}
}
