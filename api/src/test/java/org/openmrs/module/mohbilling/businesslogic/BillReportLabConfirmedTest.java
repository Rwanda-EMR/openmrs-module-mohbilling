package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;

public class BillReportLabConfirmedTest {

	@Test
	public void isCompletedOrderStatus_shouldAcceptCompletedVariants() {
		Assert.assertTrue(BillReportUtil.isCompletedOrderStatus("Completed"));
		Assert.assertTrue(BillReportUtil.isCompletedOrderStatus("COMPLETED"));
		Assert.assertTrue(BillReportUtil.isCompletedOrderStatus("completed"));
		Assert.assertFalse(BillReportUtil.isCompletedOrderStatus("In Progress"));
		Assert.assertFalse(BillReportUtil.isCompletedOrderStatus("Result available"));
		Assert.assertFalse(BillReportUtil.isCompletedOrderStatus(null));
		Assert.assertFalse(BillReportUtil.isCompletedOrderStatus(""));
	}
}
