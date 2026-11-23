package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.impl.BillingServiceImpl;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.ThirdParty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

public class InsurancePolicyUtilTest {

	@Test
	public void getThirdPartyRateError_shouldAllowRatesAddingUpTo100() {
		Assert.assertNull(InsurancePolicyUtil.getThirdPartyRateError(policy(90f, 10f)));
	}

	@Test
	public void getThirdPartyRateError_shouldAllowPolicyWithoutThirdParty() {
		InsurancePolicy card = policy(85f, null);
		card.setThirdParty(null);
		Assert.assertNull(InsurancePolicyUtil.getThirdPartyRateError(card));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectFullInsurancePlusThirdParty() {
		Assert.assertEquals(
				"TEST INSURANCE (100%) + TEST THIRD PARTY (10%) = 110%. Insurance + third party must equal 100%.",
				InsurancePolicyUtil.getThirdPartyRateError(policy(100f, 10f)));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectRatesAbove100() {
		Assert.assertEquals(
				"TEST INSURANCE (90%) + TEST THIRD PARTY (15%) = 105%. Insurance + third party must equal 100%.",
				InsurancePolicyUtil.getThirdPartyRateError(policy(90f, 15f)));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectZeroRateInsurancePlusThirdParty() {
		Assert.assertEquals(
				"TEST INSURANCE (0%) + TEST THIRD PARTY (10%) = 10%. Insurance + third party must equal 100%.",
				InsurancePolicyUtil.getThirdPartyRateError(policy(0f, 10f)));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectRatesBelow100() {
		Assert.assertEquals(
				"TEST INSURANCE (85%) + TEST THIRD PARTY (10%) = 95%. Insurance + third party must equal 100%.",
				InsurancePolicyUtil.getThirdPartyRateError(policy(85f, 10f)));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectInsuranceWithoutActiveRate() {
		InsurancePolicy card = policy(90f, 10f);
		card.getInsurance().getRates().clear();
		String error = InsurancePolicyUtil.getThirdPartyRateError(card);
		Assert.assertNotNull(error);
		Assert.assertTrue(error.startsWith("TEST INSURANCE has no active rate"));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectInsuranceWithOnlyRetiredRate() {
		InsurancePolicy card = policy(90f, 10f);
		InsuranceRate rate = card.getInsurance().getRates().iterator().next();
		rate.setRetiredDate(new Date(System.currentTimeMillis() - 86400000L));
		String error = InsurancePolicyUtil.getThirdPartyRateError(card);
		Assert.assertNotNull(error);
		Assert.assertTrue(error.startsWith("TEST INSURANCE has no active rate"));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectInsuranceWithNullRates() {
		InsurancePolicy card = policy(90f, 10f);
		card.getInsurance().setRates(null);
		Assert.assertNotNull(InsurancePolicyUtil.getThirdPartyRateError(card));
	}

	@Test
	public void getThirdPartyRateError_shouldRejectThirdPartyWithoutRate() {
		String error = InsurancePolicyUtil.getThirdPartyRateError(policy(90f, null));
		Assert.assertEquals("The third party TEST THIRD PARTY has no rate. Please configure the third party rate first.",
				error);
	}

	@Test
	public void getThirdPartyRateError_shouldRejectThirdPartyWithoutInsurance() {
		InsurancePolicy card = policy(90f, 10f);
		card.setInsurance(null);
		Assert.assertNotNull(InsurancePolicyUtil.getThirdPartyRateError(card));
	}

	@Test
	public void getThirdPartyRateError_shouldNotCheckRetiredPolicy() {
		InsurancePolicy card = policy(100f, 10f);
		card.setRetired(true);
		Assert.assertNull(InsurancePolicyUtil.getThirdPartyRateError(card));
	}

	@Test
	public void saveInsurancePolicy_shouldRejectInvalidCombinationWithoutSaving() {
		CountingDAOHandler dao = new CountingDAOHandler();
		BillingServiceImpl service = serviceWith(dao);
		try {
			service.saveInsurancePolicy(policy(100f, 10f));
			Assert.fail("Expected APIException");
		}
		catch (APIException e) {
			Assert.assertEquals(
					"TEST INSURANCE (100%) + TEST THIRD PARTY (10%) = 110%. Insurance + third party must equal 100%.",
					e.getMessage());
		}
		Assert.assertEquals(0, dao.saveCalls);
	}

	@Test
	public void saveInsurancePolicy_shouldSaveValidCombination() {
		CountingDAOHandler dao = new CountingDAOHandler();
		BillingServiceImpl service = serviceWith(dao);
		service.saveInsurancePolicy(policy(90f, 10f));
		Assert.assertEquals(1, dao.saveCalls);
	}

	private static InsurancePolicy policy(Float insuranceRate, Float thirdPartyRate) {
		InsuranceRate rate = new InsuranceRate();
		rate.setRate(insuranceRate);
		rate.setStartDate(new Date(System.currentTimeMillis() - 86400000L));

		Insurance insurance = new Insurance();
		insurance.setName("TEST INSURANCE");
		insurance.setCategory("TEST");
		insurance.addInsuranceRate(rate);

		ThirdParty thirdParty = new ThirdParty();
		thirdParty.setName("TEST THIRD PARTY");
		thirdParty.setRate(thirdPartyRate);

		InsurancePolicy card = new InsurancePolicy();
		card.setInsuranceCardNo("TEST-0001");
		card.setInsurance(insurance);
		card.setThirdParty(thirdParty);
		return card;
	}

	private static BillingServiceImpl serviceWith(CountingDAOHandler handler) {
		BillingServiceImpl service = new BillingServiceImpl();
		service.setBillingDAO((BillingDAO) Proxy.newProxyInstance(BillingDAO.class.getClassLoader(),
				new Class[] { BillingDAO.class }, handler));
		return service;
	}

	private static class CountingDAOHandler implements InvocationHandler {

		private int saveCalls;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			if ("saveInsurancePolicy".equals(method.getName())) {
				saveCalls++;
			}
			return null;
		}
	}
}
