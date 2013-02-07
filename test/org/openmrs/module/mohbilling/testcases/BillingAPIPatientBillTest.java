package org.openmrs.module.mohbilling.testcases;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class BillingAPIPatientBillTest extends BaseModuleContextSensitiveTest {

	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		// this loads some patients and concepts. this is from core
		executeDataSet("org/openmrs/include/standardTestDataset.xml");
		executeDataSet("org/openmrs/module/mohbilling/include/Billing-data.xml");
	}

	@Test
	@Verifies(value = "should find a patient bill", method = "getPatientBill")
	public final void testGetPatientBill() {
		BillingService bs = Context.getService(BillingService.class);
		PatientBill pb = bs.getPatientBill(1);
		Assert.assertNotNull(pb);
		Assert.assertTrue(pb.getAmount().equals(BigDecimal.valueOf(4800)));
	}

	@Test
	@Verifies(value = "should test BillPayment", method = "getPatientBill, savePatientBill")
	public final void testGetAndSaveBillPayment() {
		BillingService bs = Context.getService(BillingService.class);
		PatientBill pb = bs.getPatientBill(1);
		Assert.assertEquals(2, pb.getPayments().size());

		// create a new payment and add to bill, save, and re-verify

		BillPayment bp = new BillPayment();
		bp.setAmountPaid(BigDecimal.valueOf(50));
		bp.setCollector(Context.getUserService().getUser(1));
		bp.setCreatedDate(new Date());
		bp.setDateReceived(new Date());
		bp.setVoided(false);
		pb.addBillPayment(bp);

		bs.savePatientBill(pb);
		Context.flushSession();
		Context.clearSession();

		pb = bs.getPatientBill(1);
		Assert.assertEquals(3, pb.getPayments().size());
	}

	@Test
	@Verifies(value = "should test BillPayment", method = "getPatientBill, savePatientBill")
	public final void testGetAndSavePatientServiceBill() {
		BillingService bs = Context.getService(BillingService.class);
		PatientBill pb = bs.getPatientBill(1);
		Assert.assertEquals(2, pb.getBillItems().size());
		PatientServiceBill psb = pb.getBillItems().iterator().next();
		Assert.assertEquals(psb.getUnitPrice(), BigDecimal.valueOf(387));

		// create a new service and add to bill, save, and re-verify

		PatientServiceBill newBill = new PatientServiceBill();
		newBill.setCreatedDate(new Date());
		newBill.setCreator(Context.getUserService().getUser(1));
		newBill.setQuantity(1);
		newBill.setServiceDate(new Date());
		newBill.setService(bs.getFacilityServicePrice(1).getBillableServices()
				.iterator().next());
		newBill.setUnitPrice(BigDecimal.valueOf(12));
		newBill.setVoided(false);
		pb.addBillItem(newBill);

		bs.savePatientBill(pb);

		Context.flushSession();
		Context.clearSession();

		pb = bs.getPatientBill(1);

		Assert.assertEquals(3, pb.getBillItems().size());
	}

}
