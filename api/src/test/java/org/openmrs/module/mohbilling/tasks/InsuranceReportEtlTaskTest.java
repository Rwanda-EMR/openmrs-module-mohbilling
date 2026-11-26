package org.openmrs.module.mohbilling.tasks;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.service.BillingService;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InsuranceReportEtlTaskTest {

	@Test
	public void calculateRefreshFrom_shouldRequestFullLoadWhenEtlIsEmpty() {
		Assert.assertNull(InsuranceReportEtlTask.calculateRefreshFrom(null, 7));
	}

	@Test
	public void calculateRefreshFrom_shouldSubtractConfiguredOverlap() {
		Calendar latest = Calendar.getInstance();
		latest.clear();
		latest.set(2026, Calendar.SEPTEMBER, 1, 12, 30);

		Date refreshFrom = InsuranceReportEtlTask.calculateRefreshFrom(latest.getTime(), 7);
		Calendar actual = Calendar.getInstance();
		actual.setTime(refreshFrom);

		Assert.assertEquals(2026, actual.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.AUGUST, actual.get(Calendar.MONTH));
		Assert.assertEquals(25, actual.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(12, actual.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(30, actual.get(Calendar.MINUTE));
	}

	@Test
	public void calculateRefreshFrom_shouldTreatNegativeOverlapAsZero() {
		Date latest = new Date(123456789L);
		Assert.assertEquals(latest, InsuranceReportEtlTask.calculateRefreshFrom(latest, -1));
	}

	@Test
	public void laterOf_shouldPickTheLaterDate() {
		Calendar earlier = Calendar.getInstance();
		earlier.clear();
		earlier.set(2026, Calendar.JULY, 1);
		Calendar later = Calendar.getInstance();
		later.clear();
		later.set(2026, Calendar.AUGUST, 1);

		Assert.assertEquals(later.getTime(), InsuranceReportEtlTask.laterOf(earlier.getTime(), later.getTime()));
		Assert.assertEquals(later.getTime(), InsuranceReportEtlTask.laterOf(null, later.getTime()));
		Assert.assertEquals(earlier.getTime(), InsuranceReportEtlTask.laterOf(earlier.getTime(), null));
	}

	@Test
	public void parseStartDate_shouldDefaultToAugust2026() {
		Date start = InsuranceReportEtlTask.parseStartDate(null);
		Calendar actual = Calendar.getInstance();
		actual.setTime(start);
		Assert.assertEquals(2026, actual.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.AUGUST, actual.get(Calendar.MONTH));
		Assert.assertEquals(1, actual.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(BillingConstants.DEFAULT_INSURANCE_REPORT_ETL_START_DATE,
				InsuranceReportEtlTask.formatStartDateKey(start));
	}

	@Test
	public void parseStartDate_shouldAcceptYyyyMmDd() {
		Date start = InsuranceReportEtlTask.parseStartDate("2026-09-15");
		Calendar actual = Calendar.getInstance();
		actual.setTime(start);
		Assert.assertEquals(2026, actual.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.SEPTEMBER, actual.get(Calendar.MONTH));
		Assert.assertEquals(15, actual.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	public void normalizeServiceIds_shouldTrimDeduplicateSortAndIgnoreInvalidValues() {
		Assert.assertEquals("4,16,27",
				InsuranceReportEtlTask.normalizeServiceIds(" 16,4,invalid,27,4,-1,0 "));
	}

	@Test
	public void serviceConfiguration_shouldIncludeVersionStartAndServiceLists() {
		Assert.assertEquals("VERSION=10;START=2026-08-01;IMAGING=4,16;PROCEDURES=1,7,9",
				InsuranceReportEtlTask.serviceConfiguration("4,16", "1,7,9", "2026-08-01"));
	}

	@Test
	public void refreshAllInBatches_shouldWalkSmallIdWindowsForPerGlobalBillCommits() {
		List<String> ranges = new ArrayList<String>();
		final Date startDate = InsuranceReportEtlTask.parseStartDate("2026-08-01");
		BillingService billingService = (BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(), new Class<?>[] { BillingService.class },
				(proxy, method, args) -> {
					if ("getMinimumInsuranceReportSourceGlobalBillId".equals(method.getName())) {
						Assert.assertEquals(startDate, args[0]);
						return 5;
					}
					if ("getMaximumInsuranceReportSourceGlobalBillId".equals(method.getName())) {
						Assert.assertEquals(startDate, args[0]);
						return 1010;
					}
					if ("refreshInsuranceReportEtlByGlobalBillIdRange".equals(method.getName())) {
						ranges.add(args[0] + "-" + args[1]);
						Assert.assertEquals(startDate, args[4]);
						return 1;
					}
					throw new UnsupportedOperationException(method.getName());
				});

		int loaded = InsuranceReportEtlTask.refreshAllInBatches(billingService, "4,16", "1,7", startDate);

		Assert.assertEquals(3, loaded);
		Assert.assertEquals("5-504", ranges.get(0));
		Assert.assertEquals("505-1004", ranges.get(1));
		Assert.assertEquals("1005-1010", ranges.get(2));
	}
}
