package org.openmrs.module.mohbilling.utils;

import org.junit.Assert;
import org.junit.Test;

public class RhicCodeTest {

	@Test
	public void fromName_shouldReadTheCodeAfterAPipe() {
		Assert.assertEquals("RHIC-OPTH-072", RhicCode.fromName("Cataract surgery|RHIC-OPTH-072"));
		Assert.assertEquals("RLTC-CHEM-178", RhicCode.fromName("SERUM PROTEIN ELECTOPHORESIS | rltc-chem-178 "));
	}

	@Test
	public void fromName_shouldReadTheCodeAfterADash() {
		Assert.assertEquals("RHIC-XRAY-012", RhicCode.fromName("Knee x-ray - RHIC-XRAY-012"));
		Assert.assertEquals("RHIC-XRAY-012", RhicCode.fromName("Knee x-ray-RHIC-XRAY-012"));
	}

	@Test
	public void fromName_shouldBeNullWhenTheNameHasNoCode() {
		Assert.assertNull(RhicCode.fromName("AMOXICILLIN 500MG CAPSULE"));
		Assert.assertNull(RhicCode.fromName("Club foot correction (1 foot)"));
		Assert.assertNull(RhicCode.fromName("NORHIC-OPTH-072"));
		Assert.assertNull(RhicCode.fromName(null));
	}

	@Test
	public void normalize_shouldAcceptOnlyRhicAndRltcCodes() {
		Assert.assertEquals("RHIC-OPTH-072", RhicCode.normalize(" rhic-opth-072 "));
		Assert.assertNull(RhicCode.normalize("J01CA0405E"));
		Assert.assertNull(RhicCode.normalize("RHIC"));
		Assert.assertNull(RhicCode.normalize(""));
	}
}
