/**
 * 
 */
package org.openmrs.module.mohbilling.testcases;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @author Kamonyo
 * 
 */
public class BillingAPIInsurancePolicyUtilTest extends
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
	@Verifies(value = "save a Beneficiary and its parent InsurancePolicy in the database", method = "createBeneficiary")
	public final void testCreateBeneficiary() {

		BillingService bs = Context.getService(BillingService.class);
		InsurancePolicy card = bs.getInsurancePolicy(1);

		Beneficiary beneficiary = new Beneficiary();
		beneficiary.setPatient(Context.getPatientService().getPatient(2));
		beneficiary.setPolicyIdNumber("2355hhj");
		beneficiary.setInsurancePolicy(card);

		InsurancePolicyUtil.createBeneficiary(beneficiary);

		// The number of beneficiaries was 2, now it's 3
		Assert.assertEquals(3, card.getBeneficiaries().size());

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}

	@Test
	@Verifies(value = "create an insurance policy and save it in the database", method = "createInsurancePolicy")
	public final void testCreateInsurancePolicy() {

		BillingService bs = Context.getService(BillingService.class);

		InsurancePolicy card = new InsurancePolicy();
		card.setInsuranceCardNo("KR-0987");
		card.setInsurance(bs.getInsurance(1));
		card.setCoverageStartDate(new Date());
		card.setExpirationDate(new Date());
		card.setRetired(false);
		card.setCreatedDate(new Date());
		card.setOwner(Context.getPatientService().getPatient(2));
		card.setCreator(Context.getUserService().getUser(501));

		InsurancePolicyUtil.createInsurancePolicy(card);

		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		// reload insurancePolicies. Verify new card was saved
		Assert.assertEquals(bs.getAllInsurancePolicies().size(), 3);

		// verify that the new Beneficiary was added automatically
		Assert.assertNotNull(bs.getInsurancePolicy(3).getBeneficiaries());
		Assert.assertEquals(1, bs.getInsurancePolicy(3).getBeneficiaries()
				.size());

		// Verify that the Beneficiary PolicyIdNumber equals the InsuranceCardNo
		// (Because he is the owner)
		Assert.assertEquals("KR-0987", bs.getInsurancePolicy(3)
				.getBeneficiaries().iterator().next().getPolicyIdNumber());
	}

	@Test
	@Verifies(value = "edit an insurance policy", method = "editInsurancePolicy")
	public final void testEditInsurancePolicy() {

		BillingService bs = Context.getService(BillingService.class);

		InsurancePolicy card = bs.getInsurancePolicy(1);
		// Checking the current InsuranceCardNo before changes
		Assert.assertEquals("F28329487", card.getInsuranceCardNo());
		Assert.assertEquals("F28329487", InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487").getPolicyIdNumber());

		card.setInsuranceCardNo("KR-0987");
		card.setInsurance(bs.getInsurance(2));
		card.setCoverageStartDate(new Date());
		card.setExpirationDate(new Date());
		card.setOwner(Context.getPatientService().getPatient(2));

		InsurancePolicyUtil.editInsurancePolicy(card);

		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		// Verify the card was changed. InsuranceCardNo after changes
		Assert.assertEquals("KR-0987", bs.getInsurancePolicy(1)
				.getInsuranceCardNo());

	}

	@Test
	@Verifies(value = "retire an existing insurance policy", method = "retireInsurancePolicy")
	public final void testRetireInsurancePolicy() {

		BillingService bs = Context.getService(BillingService.class);

		InsurancePolicy card = bs.getInsurancePolicy(1);
		// Checking the current InsuranceCardNo before changes
		Assert.assertFalse(card.isRetired());
		Assert.assertFalse(InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(
				"F28329487").isRetired());

		// Retiring the card
		InsurancePolicyUtil.retireInsurancePolicy(card, "Retire reason");

		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		// Verify the card was changed. InsuranceCardNo after changes
		Assert.assertTrue(bs.getInsurancePolicy(1).isRetired());
		Assert.assertEquals("Retire reason", bs.getInsurancePolicy(1)
				.getRetireReason());
		Assert.assertTrue(InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(
				"F28329487").isRetired());
		Assert.assertEquals("Retire reason", InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487").getRetireReason());

	}

	@Test
	@Verifies(value = "retire an existing beneficiary", method = "retireBeneficiary")
	public final void testRetireBeneficiary() {

		// Checking the current Beneficiary before changes
		Assert.assertNull(InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(
				"F28329487-02").getRetireReason());

		Beneficiary beneficiary = InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487-02");

		// Retire the beneficiary
		InsurancePolicyUtil.retireBeneficiary(beneficiary,
				"Beneficiary is retired");

		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		Assert.assertTrue(InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(
				"F28329487-02").isRetired());
		Assert.assertEquals("Beneficiary is retired", InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487-02").getRetireReason());
	}

	@Test
	@Verifies(value = "get an insurance policy by a specified beneficiary", method = "getInsurancePolicyByBeneficiary")
	public final void testGetInsurancePolicyByBeneficiary() {

		BillingService bs = Context.getService(BillingService.class);

		InsurancePolicy card = InsurancePolicyUtil
				.getInsurancePolicyByBeneficiary(InsurancePolicyUtil
						.getBeneficiaryByPolicyIdNo("F28329487-02"));

		Assert.assertNotNull(card);
		Assert.assertEquals(card, bs.getInsurancePolicy(1));
	}

	@Test
	@Verifies(value = "get a Beneficiary By a given PolicyIdNo", method = "getBeneficiaryByPolicyIdNo")
	public final void testGetBeneficiaryByPolicyIdNo() {
		BillingService bs = Context.getService(BillingService.class);

		// Testing whether the beneficiary matches the existing one.
		Assert.assertNotNull(InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487"));
		Assert.assertNotNull(InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("F28329487-02"));
		Assert.assertNotNull(InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("Fbbbbbbbb"));
		Assert.assertNotNull(InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo("Fbbbbbbbb-01"));

		// Testing whether the beneficiary matched is the existing one.
		Assert.assertEquals(InsurancePolicyUtil
				.getInsurancePolicyByBeneficiary(InsurancePolicyUtil
						.getBeneficiaryByPolicyIdNo("F28329487")), bs
				.getInsurancePolicy(1));
		Assert.assertEquals(InsurancePolicyUtil
				.getInsurancePolicyByBeneficiary(InsurancePolicyUtil
						.getBeneficiaryByPolicyIdNo("Fbbbbbbbb")), bs
				.getInsurancePolicy(2));
	}
	
}
