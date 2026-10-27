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

public class BillReportEtlTaskTest {

	@Test
	public void materializationConfiguration_shouldIncludePrimaryIdentifierAndStartDate() {
		Assert.assertEquals("2:8:start=2026-08-01",
				BillReportEtlTask.materializationConfiguration(8, "2026-08-01"));
	}

	@Test
	public void parseStartDate_shouldDefaultToConfiguredFloor() {
		Date start = BillReportEtlTask.parseStartDate(null);
		Assert.assertEquals(BillingConstants.DEFAULT_BILL_REPORT_ETL_START_DATE,
				BillReportEtlTask.formatStartDateKey(start));
	}

	@Test
	public void parseStartDate_shouldAcceptYyyyMmDd() {
		Date start = BillReportEtlTask.parseStartDate("2026-09-15");
		Assert.assertEquals("2026-09-15", BillReportEtlTask.formatStartDateKey(start));
	}

	@Test
	public void laterOf_shouldPreferTheLaterDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2026, Calendar.AUGUST, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 10);
		Date later = calendar.getTime();

		Assert.assertEquals(later, BillReportEtlTask.laterOf(start, later));
		Assert.assertEquals(later, BillReportEtlTask.laterOf(later, start));
		Assert.assertEquals(start, BillReportEtlTask.laterOf(null, start));
	}

	@Test
	public void calculateRefreshFrom_shouldSubtractOverlapDays() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2026, Calendar.OCTOBER, 20, 12, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date latest = calendar.getTime();

		Date refreshFrom = BillReportEtlTask.calculateRefreshFrom(latest, 7);

		calendar.add(Calendar.DAY_OF_MONTH, -7);
		Assert.assertEquals(calendar.getTime(), refreshFrom);
	}

	@Test
	public void calculateRefreshFrom_shouldReturnNullWhenLatestMissing() {
		Assert.assertNull(BillReportEtlTask.calculateRefreshFrom(null, 7));
	}

	@Test
	public void refreshAllInBatches_shouldCommitBoundedAdmissionRangesWithStartDate() {
		List<String> ranges = new ArrayList<String>();
		Calendar calendar = Calendar.getInstance();
		calendar.set(2026, Calendar.AUGUST, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date startDate = calendar.getTime();

		BillingService billingService = (BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(), new Class<?>[] { BillingService.class },
				(proxy, method, args) -> {
					if ("getMinimumBillReportSourceAdmissionId".equals(method.getName())) {
						Assert.assertEquals(startDate, args[0]);
						return 1;
					}
					if ("getMaximumBillReportSourceAdmissionId".equals(method.getName())) {
						Assert.assertEquals(startDate, args[0]);
						return 10005;
					}
					if ("refreshBillReportEtlByAdmissionIdRange".equals(method.getName())) {
						Assert.assertEquals(startDate, args[2]);
						ranges.add(args[0] + "-" + args[1]);
						return 2;
					}
					throw new UnsupportedOperationException(method.getName());
				});

		int loaded = BillReportEtlTask.refreshAllInBatches(billingService, startDate);

		Assert.assertEquals(42, loaded);
		Assert.assertEquals(21, ranges.size());
		Assert.assertEquals("1-500", ranges.get(0));
		Assert.assertEquals("5001-5500", ranges.get(10));
		Assert.assertEquals("10001-10005", ranges.get(20));
	}
}
