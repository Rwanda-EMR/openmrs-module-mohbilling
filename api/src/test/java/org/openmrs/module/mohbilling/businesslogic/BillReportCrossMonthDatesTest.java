package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class BillReportCrossMonthDatesTest {

	@Test
	public void isCrossMonthItem_shouldDetectLaterCreatedMonth() {
		Assert.assertTrue(BillReportUtil.isCrossMonthItem(date(2026, Calendar.MAY, 12), date(2026, Calendar.AUGUST, 3)));
		Assert.assertFalse(BillReportUtil.isCrossMonthItem(date(2026, Calendar.AUGUST, 1), date(2026, Calendar.AUGUST, 20)));
		Assert.assertFalse(BillReportUtil.isCrossMonthItem(date(2026, Calendar.AUGUST, 1), date(2026, Calendar.JULY, 20)));
	}

	@Test
	public void resolveCrossMonthReportDates_shouldUseMinMaxWhenMonthIsNotCurrent() {
		BillReportUtil.MonthCreatedBounds bounds = new BillReportUtil.MonthCreatedBounds();
		bounds.include(date(2026, Calendar.AUGUST, 3, 10, 0, 0));
		bounds.include(date(2026, Calendar.AUGUST, 27, 15, 30, 0));
		Date realClosing = date(2026, Calendar.SEPTEMBER, 2);
		Date today = date(2026, Calendar.NOVEMBER, 23);

		BillReportUtil.ReportPeriodDates dates = BillReportUtil.resolveCrossMonthReportDates(bounds, realClosing,
				today);

		Assert.assertEquals(bounds.getMinCreated(), dates.getAdmissionDate());
		Assert.assertEquals(bounds.getMaxCreated(), dates.getDischargeDate());
	}

	@Test
	public void resolveCrossMonthReportDates_shouldKeepRealClosingWhenMonthIsCurrent() {
		BillReportUtil.MonthCreatedBounds bounds = new BillReportUtil.MonthCreatedBounds();
		bounds.include(date(2026, Calendar.AUGUST, 3));
		bounds.include(date(2026, Calendar.AUGUST, 27));
		Date realClosing = date(2026, Calendar.AUGUST, 31);
		Date today = date(2026, Calendar.AUGUST, 15);

		BillReportUtil.ReportPeriodDates dates = BillReportUtil.resolveCrossMonthReportDates(bounds, realClosing,
				today);

		Assert.assertEquals(bounds.getMinCreated(), dates.getAdmissionDate());
		Assert.assertEquals(realClosing, dates.getDischargeDate());
	}

	@Test
	public void resolveCrossMonthReportDates_shouldFallBackToLastItemCreatedWhenOpenInCurrentMonth() {
		BillReportUtil.MonthCreatedBounds bounds = new BillReportUtil.MonthCreatedBounds();
		bounds.include(date(2026, Calendar.AUGUST, 3));
		bounds.include(date(2026, Calendar.AUGUST, 20));
		Date today = date(2026, Calendar.AUGUST, 15);

		BillReportUtil.ReportPeriodDates dates = BillReportUtil.resolveCrossMonthReportDates(bounds, null, today);

		Assert.assertEquals(bounds.getMinCreated(), dates.getAdmissionDate());
		Assert.assertEquals(bounds.getMaxCreated(), dates.getDischargeDate());
	}

	@Test
	public void sameYearMonth_shouldCompareCalendarMonths() {
		Assert.assertTrue(BillReportUtil.sameYearMonth(date(2026, Calendar.AUGUST, 1), date(2026, Calendar.AUGUST, 31)));
		Assert.assertFalse(BillReportUtil.sameYearMonth(date(2026, Calendar.JULY, 31), date(2026, Calendar.AUGUST, 1)));
	}

	private static Date date(int year, int month, int day) {
		return date(year, month, day, 0, 0, 0);
	}

	private static Date date(int year, int month, int day, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month, day, hour, minute, second);
		return calendar.getTime();
	}
}
