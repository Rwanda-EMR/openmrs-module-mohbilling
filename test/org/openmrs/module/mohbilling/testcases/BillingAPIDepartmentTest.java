/**
 * 
 */
package org.openmrs.module.mohbilling.testcases;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;
/**
 * @author emr
 *
 */
public class BillingAPIDepartmentTest extends BaseModuleContextSensitiveTest {
	@Before
	public void setupDatabase() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet("org/openmrs/module/mohbilling/include/Billing-data.xml");
	}
	@Test
	@Verifies(value="Save Department to the DB", method="saveDepartement")
	public final void testSaveDepartment(){
	
		BillingService bs = Context.getService(BillingService.class);
		Department dpt = new Department();
		dpt.setName("RBC");
		dpt.setCreatedDate(new Date());
		dpt.setVoided(false);
		dpt.setDescription("OOOOLKKKK");
		bs.saveDepartement(dpt);
		// clear hibernate, thus pushing stuff to the database.
		Context.flushSession();
		Context.clearSession();

		// reload insurancePolicies. Verify new card was saved
		Assert.assertEquals(bs.getAllDepartements().size(), 2);
	
	}
	@Test
	@Verifies(value = "should find an Department by its id", method = "getDepartement")
	public final void testGetDepartment() throws Exception {
		
		BillingService bs = Context.getService(BillingService.class);
		Department dp = bs.getDepartement(1);
		Assert.assertNotNull(dp);
		Assert.assertEquals("MEDECINE INTERNE",dp.getName());
		}
	public final void testGetAllDepartments(){
		BillingService bs = Context.getService(BillingService.class);
		List<Department> departments = bs.getAllDepartements();
	    Assert.assertEquals(departments.size(),27);
		
		
	}

}
