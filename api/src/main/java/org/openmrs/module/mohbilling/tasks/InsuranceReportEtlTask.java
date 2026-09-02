package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes the local materialized insurance report from billing source tables.
 */
public class InsuranceReportEtlTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(InsuranceReportEtlTask.class);
	private static final int DEFAULT_OVERLAP_DAYS = 7;
	private static final int FULL_REFRESH_BATCH_SIZE = 25000;
	private static final int MATERIALIZATION_VERSION = 2;
	private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

	@Override
	public void execute() {
		if (!RUNNING.compareAndSet(false, true)) {
			log.warn("Skipped insurance report ETL refresh because another refresh is still running.");
			return;
		}

		try {
			BillingService billingService = Context.getService(BillingService.class);
			String imagingServiceIds = normalizeServiceIds(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_IMAGING_SERVICES, ""));
			String procedureServiceIds = normalizeServiceIds(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_PROCEDURE_SERVICES, ""));
			String serviceConfiguration = serviceConfiguration(imagingServiceIds, procedureServiceIds);
			String loadedServiceConfiguration = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_LOADED_SERVICE_CONFIGURATION, "");
			Date latestClosingDate = billingService.getLatestInsuranceReportEtlClosingDate();
			int overlapDays = getOverlapDays();
			boolean serviceConfigurationChanged = !serviceConfiguration.equals(loadedServiceConfiguration);
			boolean fullRefresh = serviceConfigurationChanged || latestClosingDate == null;
			Date refreshFrom = fullRefresh ? null : calculateRefreshFrom(latestClosingDate, overlapDays);
			int loaded = fullRefresh
					? refreshAllInBatches(billingService, imagingServiceIds, procedureServiceIds)
					: billingService.refreshInsuranceReportEtl(refreshFrom, imagingServiceIds, procedureServiceIds);
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_LOADED_SERVICE_CONFIGURATION,
					serviceConfiguration);
			log.info("Insurance report ETL task loaded " + loaded + " rows; refreshFrom=" + refreshFrom);
		} catch (Exception e) {
			log.error("Insurance report ETL task failed", e);
		} finally {
			RUNNING.set(false);
		}
	}

	static int refreshAllInBatches(BillingService billingService, String imagingServiceIds,
			String procedureServiceIds) {
		Integer minimumGlobalBillId = billingService.getMinimumInsuranceReportSourceGlobalBillId();
		Integer maximumGlobalBillId = billingService.getMaximumInsuranceReportSourceGlobalBillId();
		if (minimumGlobalBillId == null || maximumGlobalBillId == null) {
			return 0;
		}

		int totalLoaded = 0;
		int globalBillIdFrom = minimumGlobalBillId;
		while (globalBillIdFrom <= maximumGlobalBillId) {
			int globalBillIdTo = (int) Math.min((long) maximumGlobalBillId,
					(long) globalBillIdFrom + FULL_REFRESH_BATCH_SIZE - 1L);
			totalLoaded += billingService.refreshInsuranceReportEtlByGlobalBillIdRange(globalBillIdFrom,
					globalBillIdTo, imagingServiceIds, procedureServiceIds);
			if (globalBillIdTo == maximumGlobalBillId) {
				break;
			}
			globalBillIdFrom = globalBillIdTo + 1;
		}
		return totalLoaded;
	}

	static Date calculateRefreshFrom(Date latestClosingDate, int overlapDays) {
		if (latestClosingDate == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(latestClosingDate);
		calendar.add(Calendar.DAY_OF_MONTH, -Math.max(0, overlapDays));
		return calendar.getTime();
	}

	static String normalizeServiceIds(String configured) {
		Set<Integer> serviceIds = new TreeSet<Integer>();
		if (configured != null) {
			for (String token : configured.split(",")) {
				try {
					int serviceId = Integer.parseInt(token.trim());
					if (serviceId > 0) {
						serviceIds.add(serviceId);
					}
				} catch (NumberFormatException ignored) {
					// Invalid entries cannot identify a HopService and are ignored.
				}
			}
		}
		StringBuilder normalized = new StringBuilder();
		for (Integer serviceId : serviceIds) {
			if (normalized.length() > 0) {
				normalized.append(',');
			}
			normalized.append(serviceId);
		}
		return normalized.toString();
	}

	static String serviceConfiguration(String imagingServiceIds, String procedureServiceIds) {
		return "VERSION=" + MATERIALIZATION_VERSION + ";IMAGING=" + imagingServiceIds
				+ ";PROCEDURES=" + procedureServiceIds;
	}

	private int getOverlapDays() {
		String configured = Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_OVERLAP_DAYS,
				String.valueOf(DEFAULT_OVERLAP_DAYS));
		try {
			int value = Integer.parseInt(configured);
			return value >= 0 ? value : DEFAULT_OVERLAP_DAYS;
		} catch (NumberFormatException e) {
			log.warn("Invalid insurance report ETL overlap days: " + configured
					+ ". Using " + DEFAULT_OVERLAP_DAYS + ".");
			return DEFAULT_OVERLAP_DAYS;
		}
	}
}
