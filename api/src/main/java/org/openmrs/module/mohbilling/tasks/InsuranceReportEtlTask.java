package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes the local materialized insurance report from billing source tables.
 * Materialization is floored at {@link BillingConstants#DEFAULT_INSURANCE_REPORT_ETL_START_DATE}
 * (August 2026 by default), matching Bill Report.
 */
public class InsuranceReportEtlTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(InsuranceReportEtlTask.class);
	private static final int DEFAULT_OVERLAP_DAYS = 7;
	/** ID-window size for listing source GBs; each GB still commits on its own. */
	private static final int FULL_REFRESH_BATCH_SIZE = 500;
	/** Bumped when start-floor / created-date / rebuild semantics change. */
	private static final int MATERIALIZATION_VERSION = 9;
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
			Date etlStartDate = resolveStartDate();
			String startDateKey = formatStartDateKey(etlStartDate);
			String serviceConfiguration = serviceConfiguration(imagingServiceIds, procedureServiceIds, startDateKey);
			String loadedServiceConfiguration = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_LOADED_SERVICE_CONFIGURATION, "");
			Date latestClosingDate = billingService.getLatestInsuranceReportEtlClosingDate();
			int overlapDays = getOverlapDays();
			boolean serviceConfigurationChanged = !serviceConfiguration.equals(loadedServiceConfiguration);
			boolean fullRefresh = serviceConfigurationChanged || latestClosingDate == null;
			Date refreshFrom = fullRefresh ? null
					: laterOf(calculateRefreshFrom(latestClosingDate, overlapDays), etlStartDate);
			log.info("Insurance report ETL plan: fullRefresh=" + fullRefresh + "; etlStartDate=" + startDateKey
					+ "; refreshFrom=" + refreshFrom);
			int loaded;
			if (fullRefresh) {
				billingService.deleteInsuranceReportEtlBeforeStartDate(etlStartDate);
				loaded = refreshAllInBatches(billingService, imagingServiceIds, procedureServiceIds, etlStartDate);
			} else {
				loaded = billingService.refreshInsuranceReportEtl(refreshFrom, imagingServiceIds, procedureServiceIds,
						etlStartDate);
			}
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_LOADED_SERVICE_CONFIGURATION,
					serviceConfiguration);
			log.info("Insurance report ETL task loaded " + loaded + " rows; refreshFrom=" + refreshFrom
					+ "; etlStartDate=" + startDateKey);
		} catch (Exception e) {
			log.error("Insurance report ETL task failed", e);
		} finally {
			RUNNING.set(false);
		}
	}

	static int refreshAllInBatches(BillingService billingService, String imagingServiceIds,
			String procedureServiceIds, Date etlStartDate) {
		Integer minimumGlobalBillId = billingService.getMinimumInsuranceReportSourceGlobalBillId(etlStartDate);
		Integer maximumGlobalBillId = billingService.getMaximumInsuranceReportSourceGlobalBillId(etlStartDate);
		if (minimumGlobalBillId == null || maximumGlobalBillId == null) {
			return 0;
		}

		int totalLoaded = 0;
		int globalBillIdFrom = minimumGlobalBillId;
		while (globalBillIdFrom <= maximumGlobalBillId) {
			int globalBillIdTo = (int) Math.min((long) maximumGlobalBillId,
					(long) globalBillIdFrom + FULL_REFRESH_BATCH_SIZE - 1L);
			try {
				totalLoaded += billingService.refreshInsuranceReportEtlByGlobalBillIdRange(globalBillIdFrom,
						globalBillIdTo, imagingServiceIds, procedureServiceIds, etlStartDate);
			} catch (Exception e) {
				// One bad id-window must not abort the whole full refresh (e.g. a single open GB
				// with null item dates). Continue; rows from earlier windows stay committed.
				log.error("Insurance report ETL batch failed for globalBillId " + globalBillIdFrom + ".."
						+ globalBillIdTo + " (continuing)", e);
			}
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

	static Date laterOf(Date left, Date right) {
		if (left == null) {
			return right;
		}
		if (right == null) {
			return left;
		}
		return left.after(right) ? left : right;
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

	static String serviceConfiguration(String imagingServiceIds, String procedureServiceIds, String startDateKey) {
		return "VERSION=" + MATERIALIZATION_VERSION + ";START=" + startDateKey + ";IMAGING=" + imagingServiceIds
				+ ";PROCEDURES=" + procedureServiceIds;
	}

	static Date parseStartDate(String configured) {
		String value = StringUtils.trimToNull(configured);
		if (value == null) {
			value = BillingConstants.DEFAULT_INSURANCE_REPORT_ETL_START_DATE;
		}
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			format.setLenient(false);
			Date parsed = format.parse(value);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(parsed);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar.getTime();
		} catch (ParseException e) {
			log.warn("Invalid insurance report ETL start date '" + configured + "'. Using "
					+ BillingConstants.DEFAULT_INSURANCE_REPORT_ETL_START_DATE + ".");
			if (!BillingConstants.DEFAULT_INSURANCE_REPORT_ETL_START_DATE.equals(value)) {
				return parseStartDate(BillingConstants.DEFAULT_INSURANCE_REPORT_ETL_START_DATE);
			}
			Calendar fallback = Calendar.getInstance();
			fallback.clear();
			fallback.set(2026, Calendar.AUGUST, 1);
			return fallback.getTime();
		}
	}

	static String formatStartDateKey(Date startDate) {
		return new SimpleDateFormat("yyyy-MM-dd").format(startDate);
	}

	private Date resolveStartDate() {
		return parseStartDate(Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_START_DATE,
				BillingConstants.DEFAULT_INSURANCE_REPORT_ETL_START_DATE));
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
