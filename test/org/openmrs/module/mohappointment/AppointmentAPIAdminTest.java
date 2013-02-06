package org.openmrs.module.mohappointment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.service.IAppointmentService;
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
public class AppointmentAPIAdminTest extends BaseModuleContextSensitiveTest {

	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		// This loads some patients and concepts. this is from core
		executeDataSet("org/openmrs/include/standardTestDataset.xml");
		// This loads some information about Appointment
		executeDataSet("org/openmrs/module/mohappointment/include/appointment-data.xml");
	}

	@Test
	@Verifies(value = "should find an appointment by its id", method = "getAppointment")
	public final void testGetAppointment() {
		IAppointmentService bs = Context.getService(IAppointmentService.class);
		Appointment appointment = bs.getAppointmentById(1);

		// Testing whether the returned appointment is not null.
		Assert.assertNotNull(appointment);
		// Testing whether the patient is the one that has the appointment.
		Assert.assertEquals(Context.getPatientService().getPatient(6),
				appointment.getPatient());
		// Testing whether the appointment provider is the one.
		Assert.assertEquals(Context.getPersonService().getPerson(1),
				appointment.getProvider());
	}

	@Test
	@Verifies(value = "check Appointment save ", method = "saveAppointment")
	public final void testSaveAppointment() {
		// BillingService bs = Context.getService(BillingService.class);
		// FacilityServicePrice fsp = bs.getFacilityServicePrice(1);
		Assert.assertNotNull("kamonyo");
		//
		// // change the fullPrice
		// fsp.setFullPrice(new BigDecimal(10000));
		//
		// // save and clear session
		// bs.saveFacilityServicePrice(fsp);
		// Context.flushSession();
		// Context.clearSession();
		//
		// fsp = bs.getFacilityServicePrice(1);
		// Assert.assertEquals(BigDecimal.valueOf(10000), fsp.getFullPrice());
	}

}
