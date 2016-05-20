/**
 * 
 */
package org.openmrs.module.mohbilling.testcases;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @author Kamonyo
 * 
 */
public class BillingAPIInsuranceUtilTest extends BaseModuleContextSensitiveTest {

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
	@Verifies(value = "save an insurance or editing an existing one with a new rate", method = "createInsurance")
	public final void testCreateInsurance() {

		BillingService bs = Context.getService(BillingService.class);

		// Insurance
		Insurance insurance = new Insurance();
		insurance.setName("MMI");
		insurance.setAddress("Rue Depute Kajangwe");
		insurance.setConcept(Context.getConceptService().getConcept(4));
		insurance.setVoided(false);
		insurance.setCreator(Context.getUserService().getUser(501));

		// Insurance Rate
		InsuranceRate rate = new InsuranceRate();
		rate.setInsurance(insurance);
		rate.setRate(Float.valueOf(15));
		rate.setFlatFee(new BigDecimal(0));
		rate.setRetired(false);
		rate.setCreator(Context.getUserService().getUser(501));
		rate.setCreatedDate(new Date());

		insurance = InsuranceUtil.createInsurance(insurance, rate);

		// reload insurance, now there should be three insurance rates
		Insurance ins = bs.getInsurance(3);
		Assert.assertEquals(ins.getName(), "MMI");
		Assert.assertEquals(ins.getRates().size(), 1);
		Assert.assertEquals(bs.getAllInsurances().size(), 3);
		Assert.assertEquals(ins.getAddress(), "Rue Depute Kajangwe");

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}

	@Test
	@Verifies(value = "Gets insurances considering the provided validity", method = "getInsurances")
	public final void testGetInsurances() {

		BillingService bs = Context.getService(BillingService.class);

		Assert.assertEquals(InsuranceUtil.getInsurances(true).size(), 2);

		// Insurance
		Insurance insurance = new Insurance();
		insurance.setName("MMI");
		insurance.setAddress("Rue Depute Kajangwe");
		insurance.setConcept(Context.getConceptService().getConcept(4));
		insurance.setVoided(true);
		insurance.setCreator(Context.getUserService().getUser(501));

		bs.saveInsurance(insurance);
		Assert.assertEquals(InsuranceUtil.getInsurances(false).size(), 1);

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}

	@Test
	@Verifies(value = "Gets service categories considering the provided insurance", method = "getServiceCategoriesByInsurance")
	public final void testGetServiceCategoriesByInsurance() {

		BillingService bs = Context.getService(BillingService.class);

		Assert.assertEquals(InsuranceUtil.getServiceCategoriesByInsurance(
				bs.getInsurance(1), true).size(), 2);
		Assert.assertEquals(InsuranceUtil.getServiceCategoriesByInsurance(
				bs.getInsurance(2), true).size(), 1);

		// Adding a retired Service category
		ServiceCategory category = new ServiceCategory();
		category.setName("SURGERY");
		category.setDescription("All related surgery services");
		category.setRetired(true);
		category.setRetiredDate(new Date());
		category.setRetiredBy(Context.getUserService().getUser(501));
		category.setCreatedDate(new Date());
		category.setRetireReason("No reason, because this is just for testing");

		// Insurance
		Insurance insurance = bs.getInsurance(2);
		insurance.addServiceCategory(category);

		bs.saveInsurance(insurance);

		Assert.assertEquals(InsuranceUtil.getServiceCategoriesByInsurance(
				insurance, false).size(), 1);

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}

	@Test
	@Verifies(value = "Gets service categories considering the provided insurance", method = "getServiceCategoriesByInsurance")
	public final void testGetServiceCategoriesByServiceCategory() {
		BillingService bs = Context.getService(BillingService.class);

		Assert.assertEquals(InsuranceUtil.getServiceCategoriesByInsurance(
				bs.getInsurance(1), true).size(), 2);
		Assert.assertEquals(InsuranceUtil.getServiceCategoriesByInsurance(
				bs.getInsurance(2), true).size(), 1);
	}

	@Test
	@Verifies(value = "gets the Service Category by provided ID", method = "getValidServiceCategory")
	public final void testGetValidServiceCategory() {

		// Testing whether it is not null
		Assert.assertNotNull(InsuranceUtil.getValidServiceCategory(1));
		Assert.assertEquals(InsuranceUtil.getValidServiceCategory(1).getName(),
				"CONSULTATION");

		// Testing whether it is not null
		Assert.assertNotNull(InsuranceUtil.getValidServiceCategory(2));
		Assert.assertEquals(InsuranceUtil.getValidServiceCategory(2).getName(),
				"PHARMACY");
	}

	@Test
	@Verifies(value = "gets all the Service Category per provided date and retired condition", method = "getAllBillableServices")
	public final void testGetAllBillableServices() {

		// Testing whether the 4 billable services are not retired
		Assert.assertEquals(InsuranceUtil.getAllBillableServices(new Date(),
				false).size(), 4);

		// Adding a new Billable service that is retired
		BillableService bs = new BillableService();
		bs.setCreatedDate(new Date());
		bs.setCreator(Context.getUserService().getUser(501));
		bs.setInsurance(Context.getService(BillingService.class)
				.getInsurance(1));
		bs.setFacilityServicePrice(Context.getService(BillingService.class)
				.getFacilityServicePrice(1));
		bs.setStartDate(new Date());
		bs.setRetired(true);
		bs.setServiceCategory(Context.getService(BillingService.class)
				.getInsurance(1).getCategories().iterator().next());
		bs.setRetiredBy(Context.getUserService().getUser(501));
		bs.setRetiredDate(new Date());

		// Adding the new Billable Service to the corresponding Facility Service
		Context.getService(BillingService.class).getFacilityServicePrice(1)
				.addBillableService(bs);

		// Saving the Facility Service into the DB
		Context.getService(BillingService.class).saveFacilityServicePrice(
				Context.getService(BillingService.class)
						.getFacilityServicePrice(1));

		// Testing whether the retired billable service was added
		Assert.assertEquals(InsuranceUtil.getAllBillableServices(new Date(),
				true).size(), 1);

		// Clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}

	@Test
	@Verifies(value = "save Billable Service", method = "saveBillableService")
	public final void testSaveBillableService() {

		BillingService bs = Context.getService(BillingService.class);

		Insurance insurance = bs.getInsurance(1);
		BillableService service = new BillableService();
		service.setCreatedDate(new Date());
		service.setCreator(Context.getUserService().getUser(501));
		service.setFacilityServicePrice(bs.getFacilityServicePrice(1));
		service.setInsurance(insurance);
		service.setRetired(false);
		service.setStartDate(new Date());
		service.setServiceCategory(InsuranceUtil
				.getServiceCategoriesByInsurance(insurance, true).get(0));

		BillableService savedBS = InsuranceUtil.saveBillableService(service);

		// Testing whether the number of billable services increased
		Assert.assertEquals(3, bs.getFacilityServicePrice(1)
				.getBillableServices().size());
		// Testing whether the saved Facility Service Price.
		Assert.assertNotNull(savedBS.getFacilityServicePrice());
		// Testing whether maximaToPay was calculated based on the formula.
		Assert.assertEquals(new BigDecimal(2500), savedBS.getMaximaToPay());

	}
}
