/**
 * 
 */
package org.openmrs.module.mohbilling.testcases;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @author Kamonyo
 * 
 */
public class BillingAPIReportUtilTest extends BaseModuleContextSensitiveTest {

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
	@Verifies(value = "get Patient Bill By Beneficiary", method = "getPatientBillByBeneficiary")
	public final void testGetPatientBillByBeneficiary() {

		BillingService bs = Context.getService(BillingService.class);

		PatientBill bill = bs.getPatientBill(1);

		Assert.assertEquals(bill, ReportsUtil.getPatientBillByBeneficiary(
				InsurancePolicyUtil.getBeneficiaryByPolicyIdNo("F28329487"),
				new Date()));
	}

	@Test
	@Verifies(value = "get Paid Bills", method = "getPaidBills")
	public final void testGetPaidBills() {

		// Verify that the paid bills are 2
		Assert.assertEquals(2, ReportsUtil.getPaidBills(new Date(), true)
				.size());
	}

	@Test
	@Verifies(value = "get Paid Billable Services", method = "getPaidServices")
	public final void testGetPaidServices() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Verify that the paid billable services are 2
			Assert.assertEquals(2, ReportsUtil.getPaidServices(
					sdf.parse("2011-05-02"), new Date(), true).size());

			// Verify that the dates range is tested
			Assert.assertNotSame(2, ReportsUtil.getPaidServices(
					sdf.parse("2011-05-03"), new Date(), true).size());

		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}
	}

	@Test
	@Verifies(value = "get Monthly General Bills", method = "getMonthlyGeneralBills")
	public final void testGetMonthlyGeneralBills() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Verify that the monthly paid billable services are 2
			Assert.assertEquals(2, ReportsUtil.getMonthlyGeneralBills(
					sdf.parse("2011-05-02"), sdf.parse("2011-06-02"), true)
					.size());

			// Verify that the dates range is tested
			Assert.assertNotSame(2, ReportsUtil.getMonthlyGeneralBills(
					sdf.parse("2011-05-03"), sdf.parse("2011-06-03"), true)
					.size());

		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}
	}

	@Test
	@Verifies(value = "get Monthly Bills By Insurance", method = "getMonthlyBillsByInsurance")
	public final void testGetMonthlyBillsByInsurance() {

		BillingService bs = Context.getService(BillingService.class);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Verify that the monthly paid billable services are 2
			Assert.assertEquals(2, ReportsUtil.getMonthlyBillsByInsurance(
					bs.getInsurance(1), sdf.parse("2011-05-02"),
					sdf.parse("2011-06-02"), true).size());

			// Verify that the dates range is tested
			Assert.assertNotSame(2, ReportsUtil.getMonthlyBillsByInsurance(
					bs.getInsurance(1), sdf.parse("2011-05-03"),
					sdf.parse("2011-06-03"), true).size());

		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}
	}

	@Test
	@Verifies(value = "get Daily Payments", method = "getDailyPayments")
	public final void testGetDailyPayments() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Verify that the daily payments are 2
			Assert.assertEquals(2, ReportsUtil.getDailyPayments(
					sdf.parse("2011-05-01")).size());

			// Verify that the dates range is tested
			Assert.assertNotSame(2, ReportsUtil.getDailyPayments(
					sdf.parse("2011-05-02")).size());

		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}
	}

	@Test
	@Verifies(value = "get Monthly Report By Insurance", method = "getMonthlyReportByInsurance")
	public final void testGetMonthlyReportByInsurance() {

		BillingService bs = Context.getService(BillingService.class);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			// Verify that the monthly paid billable services are 2 (for
			// Mutuelle)
			Assert.assertEquals(2, ReportsUtil.getMonthlyReportByInsurance(
					bs.getInsurance(1), sdf.parse("2011-05-02"),
					sdf.parse("2011-06-02"), null).size());
			// Verify that the monthly paid billable services are 2 (for RAMA)
			Assert.assertEquals(2, ReportsUtil.getMonthlyReportByInsurance(
					bs.getInsurance(1), sdf.parse("2011-05-02"),
					sdf.parse("2011-06-02"), null).size());

			// Verify that the dates range is tested
			Assert.assertNotSame(2, ReportsUtil.getMonthlyReportByInsurance(
					bs.getInsurance(1), sdf.parse("2011-05-03"),
					sdf.parse("2011-06-03"), null).size());

		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}
	}

}
