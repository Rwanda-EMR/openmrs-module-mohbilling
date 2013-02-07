/**
 * 
 */
package org.openmrs.module.mohbilling.testcases;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * @author Kamonyo
 * 
 */
public class BillingAPIFacilityPriceUtilTest extends
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
	@Verifies(value = "load all existing valid billable services on a Date at a facility ", method = "getBillableServices")
	public final void testGetBillabeServices() {

		BillingService bs = Context.getService(BillingService.class);
		Insurance insurance = bs.getInsurance(1);
		Assert.assertTrue(insurance != null);

		List<BillableService> services = FacilityServicePriceUtil
				.getBillableServicesByInsurance(insurance, new Date());

		// Tests whether the method is return the expected size of List, or it
		// is not NULL
		Assert.assertNotNull(services);
		Assert.assertEquals(2, services.size());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			services = FacilityServicePriceUtil.getBillableServicesByInsurance(
					insurance, sdf.parse("2010-12-12"));

			// 2010-12-12 is within the date range of the billable service.
			Assert.assertEquals(2, services.size());
		} catch (Exception ex) {
			throw new RuntimeException(
					"test is failing.. can't parse string date.");
		}

	}

	@Test
	@Verifies(value = "save a facility service with a new billable service for non insured patient", method = "createFacilityService")
	public final void testCreateFacilityService() {

		BillingService bs = Context.getService(BillingService.class);

		FacilityServicePrice fsp = new FacilityServicePrice();
		fsp.setConcept(Context.getConceptService().getConcept(3));
		fsp.setLocation(Context.getLocationService().getLocation(2));
		fsp.setName("Pansement");
		fsp.setShortName("Pans");
		fsp.setFullPrice(new BigDecimal(200));
		fsp.setCreatedDate(new Date());
		fsp.setCreator(Context.getUserService().getUser(501));
		fsp.setDescription("Pansement");
		fsp.setRetired(false);
		fsp.setStartDate(new Date());

		FacilityServicePriceUtil.createFacilityService(fsp);

		Assert.assertEquals(bs.getAllFacilityServicePrices().size(), 3);
		Assert.assertEquals(bs.getFacilityServicePrice(3).getName(),
				"Pansement");
		Assert.assertEquals(bs.getFacilityServicePrice(3).getFullPrice(),
				new BigDecimal(200));
		Assert.assertEquals(bs.getFacilityServicePrice(3).getLocation(),
				Context.getLocationService().getLocation(2));
		Assert.assertEquals(bs.getFacilityServicePrice(3).getConcept(), Context
				.getConceptService().getConcept(3));

		// Testing the Billable service insertion
		Assert.assertEquals(bs.getFacilityServicePrice(3).getBillableServices()
				.size(), 1);
		Assert.assertEquals(bs.getFacilityServicePrice(3).getBillableServices()
				.iterator().next().getMaximaToPay(), new BigDecimal(200));

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}

	@Test
	@Verifies(value = "load facility services that are valid or not according to the provided condition", method = "getFacilityService")
	public final void testGetFacilityService() {

		Assert.assertEquals(FacilityServicePriceUtil.getFacilityServices(true)
				.size(), 2);

		// Adding a new Facility service that is retired
		FacilityServicePrice fsp = new FacilityServicePrice();
		fsp.setConcept(Context.getConceptService().getConcept(3));
		fsp.setLocation(Context.getLocationService().getLocation(2));
		fsp.setName("Pansement");
		fsp.setShortName("Pans");
		fsp.setFullPrice(new BigDecimal(200));
		fsp.setCreatedDate(new Date());
		fsp.setCreator(Context.getUserService().getUser(501));
		fsp.setDescription("Pansement");
		fsp.setRetired(true);
		fsp.setStartDate(new Date());

		FacilityServicePriceUtil.createFacilityService(fsp);

		Assert.assertEquals(FacilityServicePriceUtil.getFacilityServices(false)
				.size(), 1);

		// clear hibernate, thus pushing the new billService to the database.
		Context.flushSession();
		Context.clearSession();
	}
}
