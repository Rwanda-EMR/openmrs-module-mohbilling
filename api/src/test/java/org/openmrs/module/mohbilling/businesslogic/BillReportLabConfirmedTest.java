package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;

import java.util.Date;

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

	@Test
	public void resolveItemDate_shouldUseCreatedDateWhenNoOrderLink() {
		Date created = new Date(1_700_000_000_000L);
		PatientServiceBill item = new PatientServiceBill();
		item.setCreatedDate(created);
		Assert.assertEquals(created, BillReportUtil.resolveItemDate(item));
		Assert.assertNull(BillReportUtil.resolveItemDate(null));
		Assert.assertNull(BillReportUtil.resolveLinkedOrderPerformedAt(null));
	}

	@Test
	public void resolveMedicationDispenseByOrderId_shouldReturnNullWithoutOrderId() {
		Assert.assertNull(BillReportUtil.resolveMedicationDispenseByOrderId(null));
		Assert.assertNull(BillReportUtil.resolvePharmacyStatusByOrderId(null));
		Assert.assertNull(BillReportUtil.resolveMedicationDispenseDateTime(null));
		Assert.assertNull(BillReportUtil.resolvePerformedDateTime(null));
	}

	@Test
	public void resolvePaymentPhone_shouldReturnBlankWithoutConsommationOrBill() {
		Assert.assertEquals("", BillReportUtil.resolvePaymentPhone(null));
	}

	@Test
	public void isDischarged_shouldRequireClosedGlobalBillOrAdmissionDischarge() {
		Assert.assertFalse(BillReportUtil.isDischarged(null, null));
		Assert.assertFalse(BillReportUtil.isDischarged(new Admission(), new GlobalBill()));

		GlobalBill closed = new GlobalBill();
		closed.setClosed(true);
		Assert.assertTrue(BillReportUtil.isDischarged(new Admission(), closed));

		GlobalBill withClosingDate = new GlobalBill();
		withClosingDate.setClosingDate(new Date());
		Assert.assertTrue(BillReportUtil.isDischarged(new Admission(), withClosingDate));

		Admission discharged = new Admission();
		discharged.setDischargingDate(new Date());
		Assert.assertTrue(BillReportUtil.isDischarged(discharged, null));
	}

	@Test
	public void resolveReportProvider_shouldPreferAdmissionDischargedBy() {
		User dischargedBy = new User();
		dischargedBy.setUserId(11);
		User closedBy = new User();
		closedBy.setUserId(22);
		User creator = new User();
		creator.setUserId(33);

		Admission admission = new Admission();
		admission.setDischargedBy(dischargedBy);
		GlobalBill globalBill = new GlobalBill();
		globalBill.setClosedBy(closedBy);
		globalBill.setCreator(creator);

		Assert.assertEquals(dischargedBy, BillReportUtil.resolveReportProvider(admission, globalBill));

		admission.setDischargedBy(null);
		Assert.assertEquals(closedBy, BillReportUtil.resolveReportProvider(admission, globalBill));

		globalBill.setClosedBy(null);
		Assert.assertNull(BillReportUtil.resolveReportProvider(admission, globalBill));
	}
}
