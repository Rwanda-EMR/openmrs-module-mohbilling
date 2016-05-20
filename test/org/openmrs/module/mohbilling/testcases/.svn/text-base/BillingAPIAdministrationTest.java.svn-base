package org.openmrs.module.mohbilling.testcases;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Test Class that tests all objects in the Insurance domain area. The two
 * parent objects are Insurance (owns InsuranceRate and ServiceCategory), and
 * FacilityServicePrice (owns BillableService)
 * 
 * @author dthomas
 * 
 */
public class BillingAPIAdministrationTest extends
		BaseModuleContextSensitiveTest {

	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet("org/openmrs/module/mohbilling/include/Billing-data.xml");
	}

	@Test
	@Verifies(value = "should find an insurance by its id", method = "getInsurance")
	public final void testGetInsurance() {
		BillingService bs = Context.getService(BillingService.class);
		Insurance i = bs.getInsurance(1);
		Assert.assertNotNull(i);
		Assert.assertEquals("Mutuelle", i.getName());
		Assert.assertEquals("0832-88238", i.getPhone());
	}

	@Test
	@Verifies(value = "load insurance, test for insurance rates, add another insurance rate, save, and reload to verify ", method = "getInsurance")
	public final void testGetInsuranceRates() {
		BillingService bs = Context.getService(BillingService.class);
		Insurance i = bs.getInsurance(1);
		Set<InsuranceRate> rs = i.getRates();
		Assert.assertEquals(rs.size(), 2); // there were 2 rates already in
		// insurance

		// create a new insurance rate and save
		InsuranceRate ir1 = createInsuranceRate(new Date(5999), new BigDecimal(
				4000), new Float(.84));
		i.addInsuranceRate(ir1);
		bs.saveInsurance(i);

		// clear hibernate, thus pushing the new insurance rate to the database.
		Context.flushSession();
		Context.clearSession();

		// reload insurance, now there should be three insurance rates
		i = bs.getInsurance(1);
		rs = i.getRates();
		Assert.assertEquals(rs.size(), 3);
	}

	@Test
	@Verifies(value = "check FacilityServicePrice ", method = "getFacilityServicePrice, saveFacilityServicePrice")
	public final void testFacilityServicePrice() {
		BillingService bs = Context.getService(BillingService.class);
		FacilityServicePrice fsp = bs.getFacilityServicePrice(1);
		Assert.assertNotNull(fsp);

		// change the fullPrice
		fsp.setFullPrice(new BigDecimal(10000));

		// save and clear session
		bs.saveFacilityServicePrice(fsp);
		Context.flushSession();
		Context.clearSession();

		fsp = bs.getFacilityServicePrice(1);
		Assert.assertEquals(BigDecimal.valueOf(10000), fsp.getFullPrice());
	}

	@Test
	@Verifies(value = "load insurance, verify ServiceCategory get and set", method = "getInsurance")
	public final void testServiceCategories() {
		BillingService bs = Context.getService(BillingService.class);
		Insurance i = bs.getInsurance(1);
		Set<ServiceCategory> categories = i.getCategories();
		Assert.assertEquals(categories.size(), 2); // there were 2 S already

		// create a new ServiceCategory and save
		ServiceCategory sc = new ServiceCategory();
		sc.setCreatedDate(new Date());
		sc.setCreator(Context.getUserService().getUser(1));
		sc.setInsurance(i);
		sc.setName("LABORATORY");
		sc.setRetired(false);
		i.addServiceCategory(sc);
		bs.saveInsurance(i);

		// clear hibernate, thus pushing the new category
		Context.flushSession();
		Context.clearSession();

		// reload insurance, now there should be three categories
		i = bs.getInsurance(1);
		Assert.assertEquals(i.getCategories().size(), 3);

	}

	@Test
	@Verifies(value = "load facility service price and check billable services  ", method = "getInsurance")
	public final void testBillableServices() {
		BillingService bs = Context.getService(BillingService.class);
		FacilityServicePrice fsp = bs.getFacilityServicePrice(1);
		Set<BillableService> services = fsp.getBillableServices();
		Assert.assertEquals(services.size(), 2); // there were 2
		// BillableServices already

		// create a new Bs and save
		BillableService billService = new BillableService();
		billService.setCreatedDate(new Date());
		billService.setInsurance(bs.getInsurance(1));
		billService.setMaximaToPay(new BigDecimal(500));
		billService.setRetired(false);
		billService.setServiceCategory(bs.getInsurance(1).getCategories()
				.iterator().next());
		fsp.addBillableService(billService);
		bs.saveFacilityServicePrice(fsp);

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();

		// reload insurance, now there should be three insurance rates
		fsp = bs.getFacilityServicePrice(1);
		Assert.assertEquals(fsp.getBillableServices().size(), 3);

	}

	@Test
	@Verifies(value = "load all existing insurances from the DB ", method = "getAllInsurances")
	public final void testGetAllInsurances() {
		BillingService bs = Context.getService(BillingService.class);

		Assert.assertNotNull(bs.getAllInsurances());
		Assert.assertEquals(bs.getAllInsurances().size(), 2);
	}

	@Test
	@Verifies(value = "load all existing facility service price from the DB ", method = "getAllFacilityServicePrices")
	public final void testGetAllFacilityServicePrices() {
		BillingService bs = Context.getService(BillingService.class);

		Assert.assertNotNull(bs.getAllFacilityServicePrices());
		Assert.assertEquals(bs.getAllFacilityServicePrices().size(), 2);
	}

	@Test
	@Verifies(value = "load all existing PatientBills from the DB ", method = "getAllPatientBills")
	public final void testGetAllPatientBills() {
		BillingService bs = Context.getService(BillingService.class);

		Assert.assertNotNull(bs.getAllPatientBills());
		Assert.assertEquals(bs.getAllPatientBills().size(), 5);
	}

	@Test
	@Verifies(value = "load all existing insurance policies from the DB ", method = "getAllInsurancePolicies")
	public final void testGetAllInsurancePolicies() {
		BillingService bs = Context.getService(BillingService.class);

		Assert.assertNotNull(bs.getAllInsurancePolicies());
		Assert.assertEquals(bs.getAllInsurancePolicies().size(), 2);
	}

	private InsuranceRate createInsuranceRate(Date startDate,
			BigDecimal flatFee, Float rate) {
		InsuranceRate ir = new InsuranceRate();
		ir.setCreatedDate(new Date());
		ir.setCreator(Context.getUserService().getUser(1));
		ir.setFlatFee(flatFee);
		ir.setRate(rate);
		ir.setRetired(false);
		ir.setStartDate(startDate);
		return ir;
	}

}
