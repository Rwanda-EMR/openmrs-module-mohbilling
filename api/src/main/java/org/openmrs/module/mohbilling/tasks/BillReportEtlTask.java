package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Refreshes the materialized CBHI Bill Report from local billing and soft-joined clinical tables.
 */
public class BillReportEtlTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(BillReportEtlTask.class);
	private static final int DEFAULT_OVERLAP_DAYS = 7;
	/** Keep batches small so each service call commits and COUNT(*) can rise during a run. */
	private static final int FULL_REFRESH_BATCH_SIZE = 500;
	/**
	 * Bumped when start-floor / insurance allow-deny / DCP exclusion rematerialize semantics change.
	 * Bill ETL excludes {@code item_type = 2} (DCP) to match Insurance Report ETL; Insurance ETL
	 * continues to materialize all insurers (no allow/deny list).
	 */
	private static final String MATERIALIZATION_VERSION = "16";
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
			String insuranceIdsKey = normalizeInsuranceIds(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_INSURANCE_IDS, ""));
			String excludedInsuranceIdsKey = normalizeInsuranceIds(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_EXCLUDED_INSURANCE_IDS, ""));
			Date latestAdmissionDate = billingService.getLatestBillReportEtlAdmissionDate();
			String materializationConfiguration = materializationConfiguration(InsurancePolicyUtil
					.getPrimaryPatientIdentiferType().getPatientIdentifierTypeId(), startDateKey, insuranceIdsKey,
					excludedInsuranceIdsKey);
			String loadedVersion = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_LOADED_VERSION, "");
			boolean fullRefresh = latestAdmissionDate == null || !materializationConfiguration.equals(loadedVersion);
			int overlapDays = getOverlapDays();
			log.info("Bill report ETL plan: fullRefresh=" + fullRefresh + "; startDate=" + startDateKey
					+ "; insuranceIds=" + insuranceIdsKey + "; excludedInsuranceIds=" + excludedInsuranceIdsKey
					+ "; overlapDays=" + overlapDays + "; latestEtlAdmissionDate=" + latestAdmissionDate
					+ "; loadedVersion=" + loadedVersion);
			int loaded;
			if (fullRefresh) {
				billingService.deleteBillReportEtlBeforeAdmissionDate(startDate);
				loaded = refreshAllInBatches(billingService, startDate);
				// Pre-floor admissions with items created on/after startDate (same as Insurance).
				int lateCreated = refreshAdmissionsWithItemsCreatedOnOrAfter(billingService, startDate);
				loaded += lateCreated;
				log.info("Bill report ETL late-created-item admissions loaded " + lateCreated + " rows");
			} else {
				Date refreshFrom = laterOf(calculateRefreshFrom(latestAdmissionDate, overlapDays), startDate);
				log.info("Bill report ETL incremental refreshFrom=" + refreshFrom);
				loaded = billingService.refreshBillReportEtl(refreshFrom);
			}
			// Drop any report rows whose report admission_date is still before the floor
			// (e.g. July lines from a pre-August admission that also had August creates).
			billingService.deleteBillReportEtlBeforeAdmissionDate(startDate);
			int purged = billingService.deleteBillReportEtlOutsideInsuranceFilter(
					parseInsuranceIdSet(insuranceIdsKey), parseInsuranceIdSet(excludedInsuranceIdsKey));
			if (purged > 0) {
				log.info("Bill report ETL purged " + purged + " rows outside insurance allow/deny filter");
			}
			// Only mark the materialization version after a clean run. Otherwise a schema/SQL
			// failure would stick on v9 with incomplete rows and never full-refresh again.
			Context.getAdministrationService().setGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_LOADED_VERSION,
					materializationConfiguration);
			log.info("Bill report ETL task loaded " + loaded + " rows; fullRefresh=" + fullRefresh
					+ "; startDate=" + startDateKey + "; overlapDays=" + overlapDays);
		} catch (Exception e) {
			log.error("Bill report ETL task failed; materialization version was not updated", e);
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

	/**
	 * Rematerializes admissions that have non-voided bill lines created on/after {@code startDate},
	 * including those whose real admission_date is before the floor. Cross-month August (etc.) lines
	 * get synthetic report admission dates and remain after {@code deleteBillReportEtlBeforeAdmissionDate}.
	 */
	static int refreshAdmissionsWithItemsCreatedOnOrAfter(BillingService billingService, Date startDate) {
		if (billingService == null || startDate == null) {
			return 0;
		}
		List<Admission> admissions = billingService.getAdmissionsByPatientServiceBillCreatedDate(startDate);
		if (admissions == null || admissions.isEmpty()) {
			return 0;
		}
		int totalLoaded = 0;
		int considered = 0;
		for (Admission admission : admissions) {
			if (admission == null || admission.getAdmissionId() == null) {
				continue;
			}
			// Admissions already on/after the floor were covered by the ID-range full refresh.
			if (admission.getAdmissionDate() != null && !admission.getAdmissionDate().before(startDate)) {
				continue;
			}
			considered++;
			totalLoaded += billingService.refreshBillReportEtlForAdmission(admission.getAdmissionId());
		}
		log.info("Bill report ETL pre-floor admissions with late-created items: considered=" + considered
				+ ", loaded=" + totalLoaded);
		return totalLoaded;
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
		return materializationConfiguration(primaryIdentifierTypeId, startDateKey, "", "");
	}

	static String materializationConfiguration(int primaryIdentifierTypeId, String startDateKey,
			String insuranceIdsKey, String excludedInsuranceIdsKey) {
		return MATERIALIZATION_VERSION + ":" + primaryIdentifierTypeId + ":start=" + startDateKey
				+ ":ins=" + StringUtils.defaultString(insuranceIdsKey)
				+ ":ex=" + StringUtils.defaultString(excludedInsuranceIdsKey);
	}

	/**
	 * Parses a comma-separated insurance ID list into a sorted unique set (positive ints only).
	 */
	static Set<Integer> parseInsuranceIdSet(String configured) {
		Set<Integer> ids = new TreeSet<Integer>();
		if (configured == null) {
			return ids;
		}
		for (String token : configured.split(",")) {
			String trimmed = token.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			try {
				int insuranceId = Integer.parseInt(trimmed);
				if (insuranceId > 0) {
					ids.add(insuranceId);
				}
			} catch (NumberFormatException ignored) {
				// Invalid tokens cannot identify an insurance and are ignored.
			}
		}
		return ids;
	}

	/** Stable normalized CSV for materialization keys (sorted, unique, positive). */
	static String normalizeInsuranceIds(String configured) {
		Set<Integer> ids = parseInsuranceIdSet(configured);
		if (ids.isEmpty()) {
			return "";
		}
		StringBuilder normalized = new StringBuilder();
		for (Integer insuranceId : ids) {
			if (normalized.length() > 0) {
				normalized.append(',');
			}
			normalized.append(insuranceId);
		}
		return normalized.toString();
	}

	/**
	 * Effective filter: allowlist (empty = all) minus denylist (always excluded).
	 * Null / missing insurance_id is only allowed when the allowlist is empty.
	 */
	public static boolean isInsuranceIdAllowed(Integer insuranceId, Set<Integer> allowedInsuranceIds,
			Set<Integer> excludedInsuranceIds) {
		if (insuranceId != null && excludedInsuranceIds != null && excludedInsuranceIds.contains(insuranceId)) {
			return false;
		}
		if (allowedInsuranceIds == null || allowedInsuranceIds.isEmpty()) {
			return true;
		}
		return insuranceId != null && allowedInsuranceIds.contains(insuranceId);
	}

	/** Reads the configured allowlist from global properties (empty = all). */
	public static Set<Integer> configuredInsuranceAllowIds() {
		return parseInsuranceIdSet(Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_INSURANCE_IDS, ""));
	}

	/** Reads the configured denylist from global properties. */
	public static Set<Integer> configuredInsuranceExcludeIds() {
		return parseInsuranceIdSet(Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_BILL_REPORT_ETL_EXCLUDED_INSURANCE_IDS, ""));
	}

	static Set<Integer> effectiveInsuranceIds(Set<Integer> allowedInsuranceIds,
			Set<Integer> excludedInsuranceIds) {
		if (allowedInsuranceIds == null || allowedInsuranceIds.isEmpty()) {
			return Collections.emptySet();
		}
		Set<Integer> effective = new LinkedHashSet<Integer>(allowedInsuranceIds);
		if (excludedInsuranceIds != null) {
			effective.removeAll(excludedInsuranceIds);
		}
		return effective;
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
