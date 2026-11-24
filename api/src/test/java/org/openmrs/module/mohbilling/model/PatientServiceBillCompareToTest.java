package org.openmrs.module.mohbilling.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

public class PatientServiceBillCompareToTest {

	@Test
	public void compareTo_shouldKeepSiblingLinesWithNullCreatedDateInTreeSet() {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2026, Calendar.AUGUST, 15);
		Date serviceDate = calendar.getTime();

		PatientServiceBill first = new PatientServiceBill();
		first.setPatientServiceBillId(101);
		first.setVoided(false);
		first.setServiceDate(serviceDate);
		first.setCreatedDate(null);

		PatientServiceBill second = new PatientServiceBill();
		second.setPatientServiceBillId(102);
		second.setVoided(false);
		second.setServiceDate(serviceDate);
		second.setCreatedDate(null);

		Assert.assertTrue(first.compareTo(second) != 0);

		TreeSet<PatientServiceBill> items = new TreeSet<PatientServiceBill>();
		Assert.assertTrue(items.add(first));
		Assert.assertTrue(items.add(second));
		Assert.assertEquals(2, items.size());
	}
}
