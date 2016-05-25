/**
 * 
 */
package org.openmrs.module.mohbilling.testcases;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @author Kamonyo
 * 
 */
public class BillingAPIPatientBillUtilTest extends
		BaseModuleContextSensitiveTest {

	@Before
	public void setupDatabase() throws Exception {

		initializeInMemoryDatabase();
		authenticate();

		// this loads some patients and concepts. this is from core
		executeDataSet("org/openmrs/include/standardTestDataset.xml");
		// This loads some information about Billing
		executeDataSet("org/openmrs/module/mohbilling/include/Billing-data.xml");
	}

	@Test
	@Verifies(value = "get Bills By Beneficiary On Date", method = "getBillsByBeneficiaryOnDate")
	public final void testGetBillsByBeneficiaryOnDate() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Verify that the patient (beneficiary #1) bills are 2 on
			// "2011-05-02"
			Assert.assertEquals(2, PatientBillUtil
					.getBillsByBeneficiaryOnDate(
							InsurancePolicyUtil
									.getBeneficiaryByPolicyIdNo("F28329487"),
							sdf.parse("2011-05-02")).size());

		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}
	}

	@Test
	@Verifies(value = "create Patient Bill", method = "createPatientBill")
	public final void testCreatePatientBill() {

		BillingService bs = Context.getService(BillingService.class);

		// Testing the number of bills before adding a new one.
		Assert.assertEquals(5, bs.getAllPatientBills().size());

		PatientBill bill = new PatientBill();
		bill.setAmount(new BigDecimal(3000));
		bill.setBeneficiary(InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487"));
		bill.setCreator(Context.getUserService().getUser(501));
		bill.setCreatedDate(new Date());
		bill.setDescription("Facture du jour");
		bill.setIsPaid(true);
		bill.setVoided(false);
		bill.setPrinted(false);

		// This returns not null because the bill has been saved successfully
		Assert.assertNotNull(PatientBillUtil.savePatientBill(bill));

		// Testing whether the number of bills are of 6 (before it was 5)
		Assert.assertEquals(6, bs.getAllPatientBills().size());
	}

	@Test
	@Verifies(value = "create Patient Service Bill", method = "createPatientServiceBill")
	public final void testCreatePatientServiceBill() {

		BillingService bs = Context.getService(BillingService.class);

		PatientServiceBill psb = new PatientServiceBill();
		psb.setCreatedDate(new Date());
		psb.setCreator(Context.getUserService().getUser(501));
		psb.setPatientBill(bs.getPatientBill(1));
		psb.setQuantity(2);
		psb.setService(InsuranceUtil.getValidBillableService(1));
		psb.setServiceDate(new Date());
		psb.setUnitPrice(new BigDecimal(500));
		psb.setVoided(false);

		// This returns not null because the PatientServiceBill has been saved
		// successfully
		Assert.assertNotNull(PatientBillUtil.createPatientServiceBill(psb));

		// Testing whether the number of PatientServiceBill has increased to 3
		// for the PatientBill #1
		Assert.assertEquals(3, bs.getPatientBill(1).getBillItems().size());
	}
}
