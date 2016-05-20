package org.openmrs.module.mohbilling.testsuite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openmrs.module.mohbilling.AdminListExtensionTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIAdministrationTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIFacilityPriceUtilTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIFacilityServicePriceTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIInsurancePolicyTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIInsurancePolicyUtilTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIInsuranceUtilTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIPatientBillTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIPatientBillUtilTest;
import org.openmrs.module.mohbilling.testcases.BillingAPIReportUtilTest;
import org.openmrs.test.BaseModuleContextSensitiveTest;

@RunWith(Suite.class)
@Suite.SuiteClasses( { BillingAPIReportUtilTest.class,
		BillingAPIInsurancePolicyUtilTest.class,
		BillingAPIFacilityPriceUtilTest.class,
		BillingAPIInsuranceUtilTest.class, BillingAPIAdministrationTest.class,
		BillingAPIInsurancePolicyTest.class,
		BillingAPIPatientBillUtilTest.class, BillingAPIPatientBillTest.class,
		BillingAPIFacilityServicePriceTest.class })
public class AllBillingTests extends BaseModuleContextSensitiveTest {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.openmrs.module.mohbilling");
		// $JUnit-BEGIN$
		suite.addTestSuite(AdminListExtensionTest.class);
		// $JUnit-END$
		return suite;
	}

}
