package org.openmrs.module.mohbilling.automation;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.OrderType;

public class MohBillingOrderHandlerTest {

	private static final String RADIOLOGY_UUID = "5a3a8d2e-97c3-4797-a6a8-5417e6e699ec";

	private OrderType radiologyOrderType() {
		OrderType orderType = new OrderType(17);
		orderType.setUuid(RADIOLOGY_UUID);
		orderType.setName("Radiology Order");
		return orderType;
	}

	@Test
	public void shouldMapAnOrderTypeGivenByUuidIdOrNameToItsDepartment() {
		OrderType radiology = radiologyOrderType();
		Assert.assertEquals("RADIOLOGIE", MohBillingOrderHandler.getDepartmentNameForOrderType(radiology,
				RADIOLOGY_UUID + "=RADIOLOGIE"));
		Assert.assertEquals("RADIOLOGIE", MohBillingOrderHandler.getDepartmentNameForOrderType(radiology,
				"Lab Order=LABORATOIRE, 17 = RADIOLOGIE"));
		Assert.assertEquals("Radiology", MohBillingOrderHandler.getDepartmentNameForOrderType(radiology,
				"radiology order=Radiology"));
	}

	@Test
	public void shouldNotMapOtherOrderTypesOrBadConfiguration() {
		OrderType radiology = radiologyOrderType();
		Assert.assertNull(MohBillingOrderHandler.getDepartmentNameForOrderType(radiology, "Lab Order=LABORATOIRE"));
		Assert.assertNull(MohBillingOrderHandler.getDepartmentNameForOrderType(radiology, ""));
		Assert.assertNull(MohBillingOrderHandler.getDepartmentNameForOrderType(radiology, null));
		Assert.assertNull(MohBillingOrderHandler.getDepartmentNameForOrderType(radiology, RADIOLOGY_UUID + "="));
		Assert.assertNull(MohBillingOrderHandler.getDepartmentNameForOrderType(radiology, "RADIOLOGIE"));
		Assert.assertNull(MohBillingOrderHandler.getDepartmentNameForOrderType(null, RADIOLOGY_UUID + "=RADIOLOGIE"));
	}
}
