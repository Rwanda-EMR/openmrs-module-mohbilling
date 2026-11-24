package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;

import java.util.Calendar;
import java.util.Date;

public class BillReportDcpAndItemDateTest {

	@Test
	public void isDcpItem_shouldTreatNullAndOrdinaryAsNonDcp() {
		PatientServiceBill ordinary = new PatientServiceBill();
		ordinary.setItemType(1);
		Assert.assertFalse(BillReportUtil.isDcpItem(ordinary));

		PatientServiceBill unset = new PatientServiceBill();
		Assert.assertFalse(BillReportUtil.isDcpItem(unset));
		Assert.assertFalse(BillReportUtil.isDcpItem(null));
	}

	@Test
	public void isDcpItem_shouldDetectItemTypeTwo() {
		PatientServiceBill dcp = new PatientServiceBill();
		dcp.setItemType(2);
		Assert.assertTrue(BillReportUtil.isDcpItem(dcp));
	}

	@Test
	public void isDcpItem_shouldFallBackToFacilityServiceItemType() {
		FacilityServicePrice fsp = new FacilityServicePrice();
		fsp.setItemType(2);
		BillableService billable = new BillableService();
		billable.setFacilityServicePrice(fsp);
		PatientServiceBill item = new PatientServiceBill();
		item.setService(billable);
		Assert.assertTrue(BillReportUtil.isDcpItem(item));
	}

	@Test
	public void clampItemDateToAdmissionMonth_shouldKeepSameMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2026, Calendar.AUGUST, 10);
		Date admission = calendar.getTime();
		calendar.set(2026, Calendar.AUGUST, 25, 14, 30, 0);
		Date item = calendar.getTime();

		Assert.assertEquals(item, BillReportUtil.clampItemDateToAdmissionMonth(item, admission));
	}

	@Test
	public void clampItemDateToAdmissionMonth_shouldUseLastDayOfAdmissionMonthWhenOutside() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2026, Calendar.AUGUST, 10);
		Date admission = calendar.getTime();
		calendar.set(2026, Calendar.SEPTEMBER, 3, 9, 15, 30);
		calendar.set(Calendar.MILLISECOND, 0);
		Date item = calendar.getTime();

		Date clamped = BillReportUtil.clampItemDateToAdmissionMonth(item, admission);
		calendar.setTime(clamped);
		Assert.assertEquals(2026, calendar.get(Calendar.YEAR));
		Assert.assertEquals(Calendar.AUGUST, calendar.get(Calendar.MONTH));
		Assert.assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(15, calendar.get(Calendar.MINUTE));
		Assert.assertEquals(30, calendar.get(Calendar.SECOND));
	}

	@Test
	public void clampItemDateToAdmissionMonth_shouldPassThroughNulls() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2026, Calendar.AUGUST, 10);
		Date admission = calendar.getTime();
		Assert.assertNull(BillReportUtil.clampItemDateToAdmissionMonth(null, admission));
		Assert.assertEquals(admission, BillReportUtil.clampItemDateToAdmissionMonth(admission, null));
	}
}
