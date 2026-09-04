package org.openmrs.module.mohbilling.db.hibernate;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.model.HopService;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HibernateBillingDAOCashierReportTest {

	@Test
	public void cashierReportColumns_shouldDiscoverColumnsWhenGlobalPropertyIsEmpty() {
		Map<Integer, String> available = new LinkedHashMap<Integer, String>();
		available.put(8, "Laboratory");
		available.put(12, "Pharmacy");

		List<HopService> columns = HibernateBillingDAO.cashierReportColumns(
				Collections.<HopService>emptyList(), available, true);

		Assert.assertEquals(2, columns.size());
		Assert.assertEquals(Integer.valueOf(8), columns.get(0).getServiceId());
		Assert.assertEquals("Laboratory", columns.get(0).getName());
		Assert.assertEquals(Integer.valueOf(12), columns.get(1).getServiceId());
	}

	@Test
	public void cashierReportColumns_shouldKeepConfiguredOrderAndAppendMissingColumnsForAllReport() {
		HopService configured = new HopService();
		configured.setServiceId(12);
		configured.setName("Configured pharmacy");
		Map<Integer, String> available = new LinkedHashMap<Integer, String>();
		available.put(8, "Laboratory");
		available.put(12, "Stored pharmacy");

		List<HopService> columns = HibernateBillingDAO.cashierReportColumns(
				Arrays.asList(configured), available, true);

		Assert.assertEquals(2, columns.size());
		Assert.assertSame(configured, columns.get(0));
		Assert.assertEquals(Integer.valueOf(8), columns.get(1).getServiceId());
	}
}
