package org.openmrs.module.mohbilling.tasks;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.service.BillingService;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CashierReportEtlTaskTest {

	@Test
	public void calculateRefreshFrom_shouldRequestFullLoadWhenEtlIsEmpty() {
		Assert.assertNull(CashierReportEtlTask.calculateRefreshFrom(null, 7));
	}

	@Test
	public void calculateRefreshFrom_shouldSubtractConfiguredOverlap() {
		Calendar latest = Calendar.getInstance();
		latest.clear();
		latest.set(2026, Calendar.SEPTEMBER, 1, 12, 30);

		Date refreshFrom = CashierReportEtlTask.calculateRefreshFrom(latest.getTime(), 7);
		Calendar actual = Calendar.getInstance();
		actual.setTime(refreshFrom);

		Assert.assertEquals(2026, actual.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.AUGUST, actual.get(Calendar.MONTH));
		Assert.assertEquals(25, actual.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(12, actual.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(30, actual.get(Calendar.MINUTE));
	}

	@Test
	public void refreshAllInBatches_shouldCommitBoundedPaymentRanges() {
		List<String> ranges = new ArrayList<String>();
		BillingService billingService = (BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(), new Class<?>[] { BillingService.class },
				(proxy, method, args) -> {
					if ("getMinimumCashierReportSourcePaymentId".equals(method.getName())) {
						return 5;
					}
					if ("getMaximumCashierReportSourcePaymentId".equals(method.getName())) {
						return 50010;
					}
					if ("refreshCashierReportEtlByPaymentIdRange".equals(method.getName())) {
						ranges.add(args[0] + "-" + args[1]);
						return 1;
					}
					throw new UnsupportedOperationException(method.getName());
				});

		int loaded = CashierReportEtlTask.refreshAllInBatches(billingService);

		Assert.assertEquals(3, loaded);
		Assert.assertEquals("5-25004", ranges.get(0));
		Assert.assertEquals("25005-50004", ranges.get(1));
		Assert.assertEquals("50005-50010", ranges.get(2));
	}
}
