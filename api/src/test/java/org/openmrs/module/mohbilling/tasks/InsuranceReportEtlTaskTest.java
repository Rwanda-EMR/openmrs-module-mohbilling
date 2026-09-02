package org.openmrs.module.mohbilling.tasks;

import org.junit.Assert;
import org.junit.Test;
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
	public void normalizeServiceIds_shouldTrimDeduplicateSortAndIgnoreInvalidValues() {
		Assert.assertEquals("4,16,27",
				InsuranceReportEtlTask.normalizeServiceIds(" 16,4,invalid,27,4,-1,0 "));
	}

	@Test
	public void serviceConfiguration_shouldDistinguishImagingAndProcedureLists() {
		Assert.assertEquals("VERSION=2;IMAGING=4,16;PROCEDURES=1,7,9",
				InsuranceReportEtlTask.serviceConfiguration("4,16", "1,7,9"));
	}

	@Test
	public void refreshAllInBatches_shouldCommitBoundedGlobalBillRanges() {
		List<String> ranges = new ArrayList<String>();
		BillingService billingService = (BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(), new Class<?>[] { BillingService.class },
				(proxy, method, args) -> {
					if ("getMinimumInsuranceReportSourceGlobalBillId".equals(method.getName())) {
						return 5;
					}
					if ("getMaximumInsuranceReportSourceGlobalBillId".equals(method.getName())) {
						return 50010;
					}
					if ("refreshInsuranceReportEtlByGlobalBillIdRange".equals(method.getName())) {
						ranges.add(args[0] + "-" + args[1]);
						return 1;
					}
					throw new UnsupportedOperationException(method.getName());
				});

		int loaded = InsuranceReportEtlTask.refreshAllInBatches(billingService, "4,16", "1,7");

		Assert.assertEquals(3, loaded);
		Assert.assertEquals("5-25004", ranges.get(0));
		Assert.assertEquals("25005-50004", ranges.get(1));
		Assert.assertEquals("50005-50010", ranges.get(2));
	}
}
