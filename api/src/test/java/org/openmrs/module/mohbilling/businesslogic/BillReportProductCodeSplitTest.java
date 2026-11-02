package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;

/**
 * Fixed-length product code suffix rules for non-medicament / non-consumable names.
 */
public class BillReportProductCodeSplitTest {

	@Test
	public void splitProductNameAndCode_shouldTakeLast13ForStandardRhicCode() {
		String[] result = BillReportUtil.splitProductNameAndCode(
				"Consultation by a general practioner-RHIC-CONS-013");

		Assert.assertEquals("Consultation by a general practioner", result[0]);
		Assert.assertEquals("RHIC-CONS-013", result[1]);
		Assert.assertEquals(13, result[1].length());
	}

	@Test
	public void splitProductNameAndCode_shouldTakeLast13ForRltcCode() {
		String[] result = BillReportUtil.splitProductNameAndCode("SGPT –ALAT*-RLTC-CHEM-181");

		Assert.assertEquals("SGPT –ALAT*", result[0]);
		Assert.assertEquals("RLTC-CHEM-181", result[1]);
		Assert.assertEquals(13, result[1].length());
	}

	@Test
	public void splitProductNameAndCode_shouldTakeLast14WhenNameHoldsRhicGastr() {
		String[] result = BillReportUtil.splitProductNameAndCode("Upper endoscopy-RHIC-GASTR-001");

		Assert.assertEquals("Upper endoscopy", result[0]);
		Assert.assertEquals("RHIC-GASTR-001", result[1]);
		Assert.assertEquals(14, result[1].length());
	}

	@Test
	public void splitProductNameAndCode_shouldLeaveBlankCodeWhenNameShorterThanExpected() {
		String[] result = BillReportUtil.splitProductNameAndCode("Short");

		Assert.assertEquals("Short", result[0]);
		Assert.assertEquals("", result[1]);
	}

	@Test
	public void splitProductNameAndCode_shouldTrimTrailingSpacesFromCode() {
		String[] result = BillReportUtil.splitProductNameAndCode(
				"Consultation by a GP-RHIC-CONS-013   ");

		Assert.assertEquals("Consultation by a GP", result[0]);
		Assert.assertEquals("RHIC-CONS-013", result[1]);
		Assert.assertFalse(result[1].endsWith(" "));
	}

	@Test
	public void splitProductNameAndCode_shouldTrimTrailingSpacesFromRhicGastrCode() {
		String[] result = BillReportUtil.splitProductNameAndCode(
				"Colonoscopy-RHIC-GASTR-012 ");

		Assert.assertEquals("Colonoscopy", result[0]);
		Assert.assertEquals("RHIC-GASTR-012", result[1]);
		Assert.assertEquals(14, result[1].length());
		Assert.assertFalse(result[1].endsWith(" "));
	}

	@Test
	public void splitProductNameAndCode_shouldStripTrailingPipeFromName() {
		String[] result = BillReportUtil.splitProductNameAndCode(
				"Consultation by a GP|RHIC-CONS-013");

		Assert.assertEquals("Consultation by a GP", result[0]);
		Assert.assertEquals("RHIC-CONS-013", result[1]);
	}

	@Test
	public void splitProductNameAndCode_shouldStripTrailingDashPipeAndSpacesFromName() {
		String[] result = BillReportUtil.splitProductNameAndCode(
				"Upper endoscopy -| RHIC-GASTR-001");

		Assert.assertEquals("Upper endoscopy", result[0]);
		Assert.assertEquals("RHIC-GASTR-001", result[1]);
	}
}
