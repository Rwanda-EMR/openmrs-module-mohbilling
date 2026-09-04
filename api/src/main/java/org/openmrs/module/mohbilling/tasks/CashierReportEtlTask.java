package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes the materialized cashier report from local billing payment tables.
 */
public class CashierReportEtlTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(CashierReportEtlTask.class);
	private static final int FULL_REFRESH_BATCH_SIZE = 25000;
	private static final String MATERIALIZATION_VERSION = "2";
	private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

	@Override
	public void execute() {
		if (!RUNNING.compareAndSet(false, true)) {
			log.warn("Skipped cashier report ETL refresh because another refresh is still running.");
			return;
		}

		try {
			BillingService billingService = Context.getService(BillingService.class);
			Integer latestPaymentId = billingService.getMaximumCashierReportEtlPaymentId();
			String materializationConfiguration = materializationConfiguration(InsurancePolicyUtil
					.getPrimaryPatientIdentiferType().getPatientIdentifierTypeId());
			String loadedVersion = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_CASHIER_REPORT_ETL_LOADED_VERSION, "");
			boolean fullRefresh = latestPaymentId == null || !materializationConfiguration.equals(loadedVersion);
			int loaded = fullRefresh
					? refreshAllInBatches(billingService)
					: refreshNewInBatches(billingService, latestPaymentId);
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_CASHIER_REPORT_ETL_LOADED_VERSION,
					materializationConfiguration);
			log.info("Cashier report ETL task loaded " + loaded + " rows after bill_payment_id=" + latestPaymentId);
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

		return refreshRangeInBatches(billingService, minimumPaymentId, maximumPaymentId);
	}

	static int refreshNewInBatches(BillingService billingService, int latestPaymentId) {
		Integer maximumPaymentId = billingService.getMaximumCashierReportSourcePaymentId();
		if (maximumPaymentId == null || latestPaymentId >= maximumPaymentId) {
			return 0;
		}
		return refreshRangeInBatches(billingService, latestPaymentId + 1, maximumPaymentId);
	}

	static String materializationConfiguration(int primaryIdentifierTypeId) {
		return MATERIALIZATION_VERSION + ":" + primaryIdentifierTypeId;
	}

	private static int refreshRangeInBatches(BillingService billingService, int paymentIdFrom,
			int maximumPaymentId) {
		int totalLoaded = 0;
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
}
