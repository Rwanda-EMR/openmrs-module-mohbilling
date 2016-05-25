package org.openmrs.module.mohbilling.testcases;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * This is a test class that tests the InsurancePolicy domain area of the data
 * model InsurancePolicy is the parent class, Beneficiaries is the child
 * 
 * 
 * @author dthomas
 * 
 */
public class BillingAPIInsurancePolicyTest extends
		BaseModuleContextSensitiveTest {

	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet("org/openmrs/module/mohbilling/include/Billing-data.xml");
	}

	@Test
	@Verifies(value = "testInsurancePolicy", method = "getInsurancePolicy(id)")
	public final void testGetInsurancePolicy() {
		BillingService bs = Context.getService(BillingService.class);
		InsurancePolicy ip = bs.getInsurancePolicy(1);
		Assert.assertNotNull(ip);
		Assert.assertEquals("F28329487", ip.getInsuranceCardNo());
		InsurancePolicy ip2 = bs.getInsurancePolicy(2);
		Assert.assertNotNull(ip2);
		Assert.assertEquals("Fbbbbbbbb", ip2.getInsuranceCardNo());
	}

	@Test
	@Verifies(value = "load insurancePolicy, add new beneficiary, verify save ", method = "getAllInsurancePolicies")
	public final void testGetInsurancePolicies() {
		BillingService bs = Context.getService(BillingService.class);

		// There are 2 insurance policies in the DB
		Assert.assertEquals(2, bs.getAllInsurancePolicies().size());

		InsurancePolicy i = bs.getInsurancePolicy(2);
		// there were 2 beneficiaries
		Assert.assertEquals(2, i.getBeneficiaries().size());

		// create a new beneficiary and save
		Beneficiary b = new Beneficiary();
		b.setCreatedDate(new Date());
		b.setCreator(Context.getUserService().getUser(1));

		// from standardTestDataset in core
		b.setPatient(Context.getPatientService().getPatient(6));
		b.setPolicyIdNumber("sdlkf3");
		b.setRetired(false);
		i.addBeneficiary(b);
		bs.saveInsurancePolicy(i);

		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		// reload insurancePolicy. verify new beneficiary was saved
		i = bs.getInsurancePolicy(2);
		Assert.assertEquals(i.getBeneficiaries().size(), 3);
	}

	@Test
	@Verifies(value = "save insurancePolicy to the DB", method = "saveInsurancePolicy")
	public final void testSaveInsurancePolicy() {

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

		bs.saveInsurancePolicy(card);

		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		// reload insurancePolicies. Verify new card was saved
		Assert.assertEquals(bs.getAllInsurancePolicies().size(), 3);

	}
}
