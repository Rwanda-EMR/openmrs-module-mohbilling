package org.openmrs.module.mohbilling.service;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class BillingServiceTest extends BaseModuleContextSensitiveTest {

	@Test
	public void billingServiceMustBeInitialized() {
		Assert.assertNotNull(Context.getService(BillingService.class));
	}
}
