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
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @author Kamonyo
 * 
 */
public class BillingAPIFacilityServicePriceTest extends
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

	// - getAllFacilityServicePrices();

	@Test
	@Verifies(value = "Load all existing facility services from the DB ", method = "getAllFacilityServicePrices")
	public final void testGetAllFacilityServicePrices() {
		BillingService bs = Context.getService(BillingService.class);

		Assert.assertNotNull(bs.getAllFacilityServicePrices());
		Assert.assertEquals(bs.getAllFacilityServicePrices().size(), 2);
	}

	// - saveFacilityServicePrice(FacilityServicePrice fsp);

	@Test
	@Verifies(value = "Saves a Facility service and see if it goes to the DB", method = "saveFacilityServicePrice")
	public final void testSaveFacilityServicePrice() {

		BillingService bs = Context.getService(BillingService.class);

		FacilityServicePrice fsp = new FacilityServicePrice();
		fsp.setName("Pansement");
		fsp.setFullPrice(new BigDecimal(200));
		fsp.setConcept(Context.getConceptService().getConcept(3));
		fsp.setLocation(Context.getLocationService().getLocation(2));
		fsp.setCreatedDate(new Date());
		fsp.setCreator(Context.getUserService().getUser(501));
		fsp.setRetired(false);

		bs.saveFacilityServicePrice(fsp);

		// After adding a new Facility Service, the size will increase (Before
		// it was 2)
		Assert.assertEquals(3, bs.getAllFacilityServicePrices().size());

		Context.flushSession();
		Context.clearSession();

		FacilityServicePrice fsp1 = bs.getFacilityServicePrice(1);

		// Just added a billable service from a different Facility Service Price
		fsp1.addBillableService(bs.getFacilityServicePrice(2)
				.getBillableServices().iterator().next());

		bs.saveFacilityServicePrice(fsp1);

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();

		// After adding a Billable Service to the fsp the size should increase
		// (Before it was 2, and now it should be 3)
		Assert.assertEquals(fsp1.getBillableServices().size(), 3);
		Assert.assertEquals(bs.getAllFacilityServicePrices().size(), 3);

		// Testing the attributes entered
		// Assert.assertEquals(bs.getFacilityServicePrice(2).getName(),
		// "Pansement");
		// Assert.assertEquals(bs.getFacilityServicePrice(2).getFullPrice(),
		// new BigDecimal(200));
		// Assert.assertEquals(bs.getFacilityServicePrice(2).getLocation(),
		// Context.getLocationService().getLocation(2));
		// Assert.assertEquals(bs.getFacilityServicePrice(2).getConcept(),
		// Context
		// .getConceptService().getConcept(3));
	}

	// - getFacilityServicePrice(Integer id);

	@Test
	@Verifies(value = "Loads an Facility service by passing its ID", method = "getFacilityServicePrice")
	public final void testGetFacilityServicePrice() {

		BillingService bs = Context.getService(BillingService.class);

		FacilityServicePrice fsp = bs.getFacilityServicePrice(1);
		Assert.assertNotNull(fsp);
		Assert.assertEquals(fsp.getName(), "consultation");
		Assert.assertEquals(fsp.getFullPrice(), new BigDecimal(5000));
		Assert.assertEquals(fsp.getStartDate().toString(),
				"2005-12-01 00:00:00.0");
		Assert.assertEquals(fsp.getEndDate().toString(),
				"2011-05-01 00:00:00.0");
	}

}
