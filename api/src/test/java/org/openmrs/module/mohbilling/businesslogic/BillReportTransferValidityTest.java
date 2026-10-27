package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class BillReportTransferValidityTest {

	@Test
	public void formatTransferValidityPeriod_shouldAddOneMonthToDecisionDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2026, Calendar.AUGUST, 15);
		Date decision = calendar.getTime();

		Assert.assertEquals("15/08/2026 - 15/09/2026",
				BillReportUtil.formatTransferValidityPeriod(decision));
	}

	@Test
	public void formatTransferValidityPeriod_shouldReturnBlankWhenDecisionMissing() {
		Assert.assertEquals("", BillReportUtil.formatTransferValidityPeriod(null));
	}

	@Test
	public void addMonths_shouldHandleMonthEnd() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2026, Calendar.JANUARY, 31);
		Date end = BillReportUtil.addMonths(calendar.getTime(), 1);

		calendar.setTime(end);
		Assert.assertEquals(2026, calendar.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH));
		Assert.assertEquals(28, calendar.get(Calendar.DAY_OF_MONTH));
	}
}
