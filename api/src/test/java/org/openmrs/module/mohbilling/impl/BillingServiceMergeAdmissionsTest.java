package org.openmrs.module.mohbilling.impl;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.model.Admission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BillingServiceMergeAdmissionsTest {

	@Test
	public void mergeAdmissionsById_shouldUnionAndDedupePreservingPrimaryOrder() {
		Admission a1 = admission(1);
		Admission a2 = admission(2);
		Admission a3 = admission(3);
		Admission a2Dup = admission(2);

		List<Admission> merged = BillingServiceImpl.mergeAdmissionsById(
				Arrays.asList(a1, a2),
				Arrays.asList(a2Dup, a3));

		Assert.assertEquals(3, merged.size());
		Assert.assertEquals(Integer.valueOf(1), merged.get(0).getAdmissionId());
		Assert.assertEquals(Integer.valueOf(2), merged.get(1).getAdmissionId());
		Assert.assertSame(a2, merged.get(1));
		Assert.assertEquals(Integer.valueOf(3), merged.get(2).getAdmissionId());
	}

	@Test
	public void mergeAdmissionsById_shouldTolerateNullLists() {
		Admission a1 = admission(1);
		Assert.assertEquals(Collections.singletonList(a1),
				BillingServiceImpl.mergeAdmissionsById(Collections.singletonList(a1), null));
		Assert.assertEquals(Collections.singletonList(a1),
				BillingServiceImpl.mergeAdmissionsById(null, Collections.singletonList(a1)));
		Assert.assertTrue(BillingServiceImpl.mergeAdmissionsById(null, null).isEmpty());
	}

	private static Admission admission(int id) {
		Admission admission = new Admission();
		admission.setAdmissionId(id);
		return admission;
	}
}
