package org.openmrs.module.mohbilling.tasks;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.service.BillingService;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class CashierReportEtlTaskTest {

	@Test
	public void materializationConfiguration_shouldIncludePrimaryIdentifierType() {
		Assert.assertEquals("2:3", CashierReportEtlTask.materializationConfiguration(3));
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

	@Test
	public void refreshNewInBatches_shouldStartAfterLatestLoadedPaymentId() {
		List<String> ranges = new ArrayList<String>();
		BillingService billingService = paymentRangeService(75010, ranges);

		int loaded = CashierReportEtlTask.refreshNewInBatches(billingService, 50010);

		Assert.assertEquals(1, loaded);
		Assert.assertEquals(1, ranges.size());
		Assert.assertEquals("50011-75010", ranges.get(0));
	}

	@Test
	public void refreshNewInBatches_shouldDoNothingWhenNoNewPaymentExists() {
		List<String> ranges = new ArrayList<String>();

		int loaded = CashierReportEtlTask.refreshNewInBatches(paymentRangeService(50010, ranges), 50010);

		Assert.assertEquals(0, loaded);
		Assert.assertTrue(ranges.isEmpty());
	}

	private BillingService paymentRangeService(int maximumPaymentId, List<String> ranges) {
		return (BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(), new Class<?>[] { BillingService.class },
				(proxy, method, args) -> {
					if ("getMaximumCashierReportSourcePaymentId".equals(method.getName())) {
						return maximumPaymentId;
					}
					if ("refreshCashierReportEtlByPaymentIdRange".equals(method.getName())) {
						ranges.add(args[0] + "-" + args[1]);
						return 1;
					}
					throw new UnsupportedOperationException(method.getName());
				});
	}
}
