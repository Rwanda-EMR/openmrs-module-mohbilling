package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class ProformaUtilTest {

	@Test
	public void calculateShares_shouldSplitByPercentageRate() {
		BigDecimal[] shares = ProformaUtil.calculateShares(new BigDecimal("10000"), 85f, null);
		Assert.assertEquals(new BigDecimal("8500.00"), shares[0]);
		Assert.assertEquals(new BigDecimal("1500.00"), shares[1]);
	}

	@Test
	public void calculateShares_shouldChargePatientFlatFeeWhenConfigured() {
		BigDecimal[] shares = ProformaUtil.calculateShares(new BigDecimal("10000"), 90f, new BigDecimal("200"));
		Assert.assertEquals(new BigDecimal("9800.00"), shares[0]);
		Assert.assertEquals(new BigDecimal("200.00"), shares[1]);
	}

	@Test
	public void calculateShares_shouldCapFlatFeeAtTotal() {
		BigDecimal[] shares = ProformaUtil.calculateShares(new BigDecimal("150"), 90f, new BigDecimal("200"));
		Assert.assertEquals(new BigDecimal("0.00"), shares[0]);
		Assert.assertEquals(new BigDecimal("150.00"), shares[1]);
	}

	@Test
	public void calculateShares_shouldChargePatientEverythingWithoutRate() {
		BigDecimal[] shares = ProformaUtil.calculateShares(new BigDecimal("1234.50"), null, null);
		Assert.assertEquals(new BigDecimal("0.00"), shares[0]);
		Assert.assertEquals(new BigDecimal("1234.50"), shares[1]);
	}
}
