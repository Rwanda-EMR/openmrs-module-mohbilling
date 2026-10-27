package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes the materialized CBHI Bill Report from local billing and soft-joined clinical tables.
 */
public class BillReportEtlTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(BillReportEtlTask.class);
	private static final int DEFAULT_OVERLAP_DAYS = 7;
	/** Keep batches small so each service call commits and COUNT(*) can rise during a run. */
	private static final int FULL_REFRESH_BATCH_SIZE = 500;
	private static final String MATERIALIZATION_VERSION = "2";
	private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

	@Override
	public void execute() {
		if (!RUNNING.compareAndSet(false, true)) {
			log.warn("Skipped bill report ETL refresh because another refresh is still running.");
			return;
		}

		log.info("Bill report ETL task starting.");
		try {
			BillingService billingService = Context.getService(BillingService.class);
			Date startDate = resolveStartDate();
			String startDateKey = formatStartDateKey(startDate);
			Date latestAdmissionDate = billingService.getLatestBillReportEtlAdmissionDate();
			String materializationConfiguration = materializationConfiguration(InsurancePolicyUtil
					.getPrimaryPatientIdentiferType().getPatientIdentifierTypeId(), startDateKey);
			String loadedVersion = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_LOADED_VERSION, "");
			boolean fullRefresh = latestAdmissionDate == null || !materializationConfiguration.equals(loadedVersion);
			int overlapDays = getOverlapDays();
			log.info("Bill report ETL plan: fullRefresh=" + fullRefresh + "; startDate=" + startDateKey
					+ "; overlapDays=" + overlapDays + "; latestEtlAdmissionDate=" + latestAdmissionDate
					+ "; loadedVersion=" + loadedVersion);
			int loaded;
			if (fullRefresh) {
				billingService.deleteBillReportEtlBeforeAdmissionDate(startDate);
				loaded = refreshAllInBatches(billingService, startDate);
			} else {
				Date refreshFrom = laterOf(calculateRefreshFrom(latestAdmissionDate, overlapDays), startDate);
				log.info("Bill report ETL incremental refreshFrom=" + refreshFrom);
				loaded = billingService.refreshBillReportEtl(refreshFrom);
			}
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_LOADED_VERSION,
					materializationConfiguration);
			log.info("Bill report ETL task loaded " + loaded + " rows; fullRefresh=" + fullRefresh
					+ "; startDate=" + startDateKey + "; overlapDays=" + overlapDays);
		} catch (Exception e) {
			log.error("Bill report ETL task failed", e);
		} finally {
			RUNNING.set(false);
		}
	}

	static int refreshAllInBatches(BillingService billingService, Date startDate) {
		Integer minimumAdmissionId = billingService.getMinimumBillReportSourceAdmissionId(startDate);
		Integer maximumAdmissionId = billingService.getMaximumBillReportSourceAdmissionId(startDate);
		if (minimumAdmissionId == null || maximumAdmissionId == null) {
			log.info("Bill report ETL found no source admissions on/after " + formatStartDateKey(startDate));
			return 0;
		}
		log.info("Bill report ETL full refresh admission_id range " + minimumAdmissionId + ".."
				+ maximumAdmissionId);
		return refreshRangeInBatches(billingService, minimumAdmissionId, maximumAdmissionId, startDate);
	}

	static Date calculateRefreshFrom(Date latestAdmissionDate, int overlapDays) {
		if (latestAdmissionDate == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(latestAdmissionDate);
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

	static String materializationConfiguration(int primaryIdentifierTypeId, String startDateKey) {
		return MATERIALIZATION_VERSION + ":" + primaryIdentifierTypeId + ":start=" + startDateKey;
	}

	static Date parseStartDate(String configured) {
		String value = StringUtils.trimToNull(configured);
		if (value == null) {
			value = BillingConstants.DEFAULT_BILL_REPORT_ETL_START_DATE;
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
			log.warn("Invalid bill report ETL start date '" + configured + "'. Using "
					+ BillingConstants.DEFAULT_BILL_REPORT_ETL_START_DATE + ".");
			if (!BillingConstants.DEFAULT_BILL_REPORT_ETL_START_DATE.equals(value)) {
				return parseStartDate(BillingConstants.DEFAULT_BILL_REPORT_ETL_START_DATE);
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

	private static int refreshRangeInBatches(BillingService billingService, int admissionIdFrom,
			int maximumAdmissionId, Date startDate) {
		int totalLoaded = 0;
		while (admissionIdFrom <= maximumAdmissionId) {
			int admissionIdTo = (int) Math.min((long) maximumAdmissionId,
					(long) admissionIdFrom + FULL_REFRESH_BATCH_SIZE - 1L);
			int batchLoaded = billingService.refreshBillReportEtlByAdmissionIdRange(admissionIdFrom, admissionIdTo,
					startDate);
			totalLoaded += batchLoaded;
			log.info("Bill report ETL batch admission_id " + admissionIdFrom + ".." + admissionIdTo
					+ " loaded " + batchLoaded + " rows (total " + totalLoaded + ")");
			try {
				Context.flushSession();
				Context.clearSession();
			} catch (Exception ignored) {
				// Best-effort; batch already committed via the service transaction.
			}
			if (admissionIdTo == maximumAdmissionId) {
				break;
			}
			admissionIdFrom = admissionIdTo + 1;
		}
		return totalLoaded;
	}

	private Date resolveStartDate() {
		return parseStartDate(Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_START_DATE,
				BillingConstants.DEFAULT_BILL_REPORT_ETL_START_DATE));
	}

	private int getOverlapDays() {
		String configured = Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_OVERLAP_DAYS,
				String.valueOf(DEFAULT_OVERLAP_DAYS));
		try {
			int value = Integer.parseInt(configured);
			return value >= 0 ? value : DEFAULT_OVERLAP_DAYS;
		} catch (NumberFormatException e) {
			log.warn("Invalid bill report ETL overlap days: " + configured
					+ ". Using " + DEFAULT_OVERLAP_DAYS + ".");
			return DEFAULT_OVERLAP_DAYS;
		}
	}
}
