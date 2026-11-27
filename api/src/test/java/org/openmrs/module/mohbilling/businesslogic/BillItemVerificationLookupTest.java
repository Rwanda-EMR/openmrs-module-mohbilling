package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;

public class BillItemVerificationLookupTest {

	@Test
	public void shouldLabelTheStatusesSavedByRwandaemr() {
		Assert.assertEquals("Rejected by insurer", BillItemVerificationLookup.label("INSURER_REJECTED"));
		Assert.assertEquals("Accepted after dispute", BillItemVerificationLookup.label("FINAL_ACCEPTED"));
		Assert.assertEquals("Rejection accepted by facility", BillItemVerificationLookup.label("REJECTION_ACCEPTED"));
	}

	@Test
	public void shouldShowItemsNeverVerifiedAsPending() {
		Assert.assertEquals("Pending", BillItemVerificationLookup.label(null));
	}

	@Test
	public void shouldShowAnUnknownStatusAsSaved() {
		Assert.assertEquals("SOME_NEW_STATUS", BillItemVerificationLookup.label("SOME_NEW_STATUS"));
	}
}
