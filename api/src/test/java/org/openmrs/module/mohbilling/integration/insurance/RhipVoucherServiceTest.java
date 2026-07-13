package org.openmrs.module.mohbilling.integration.insurance;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class RhipVoucherServiceTest {

	@Test
	public void submitVoucher_shouldRejectInvalidRequestBeforeProviderCall() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("CBHI");
		request.setFacilityFosaId("FOSA-001");
		// patientIdentifier intentionally missing
		request.setPractitionerLicenseNumber("LIC-01");
		request.setProcedures(Collections.emptyList());

		IntegrationResponse response = service.submitVoucher(request);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrorMessage());
		Assert.assertTrue(response.getErrorMessage().contains("patientIdentifier is required"));
		Assert.assertTrue(response.getErrorMessage().contains("At least one procedure is required"));
		Assert.assertEquals(0, provider.submitVoucherCalls);
	}

	@Test
	public void submitVoucherForGlobalBill_shouldRejectWhenVoucherAlreadyExists() {
		RhipVoucherService service = new RhipVoucherService();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(new CountingProvider());

		GlobalBill globalBill = new GlobalBill();
		globalBill.setRhipVoucherCode("VCH-12345");

		IntegrationResponse response = service.submitVoucherForGlobalBill(globalBill);
		Assert.assertNotNull(response);
		Assert.assertEquals("RHIP voucher already exists for this global bill", response.getErrorMessage());
	}

	@Test
	public void submitVoucher_shouldCallProviderWhenRequestIsValid() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("CBHI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setTreatmentForNewBorn(Boolean.FALSE);
		request.setDiagnosisIds(Collections.singletonList("XM1QR3"));

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("PROC-001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.TEN);
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);
		Assert.assertNotNull(response);
		Assert.assertNull(response.getErrorMessage());
		Assert.assertEquals(1, provider.submitVoucherCalls);
	}

	@Test
	public void submitVoucher_shouldRejectWhenRequiredFieldsAreMissingForAnyInsuranceType() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("CBHI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setPractitionerLicenseNumber("LIC-01");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("PROC-001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.ONE);
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrorMessage());
		Assert.assertTrue(response.getErrorMessage().contains("treatmentForNewBorn"));
		Assert.assertTrue(response.getErrorMessage().contains("diagnosisIds"));
		Assert.assertEquals(0, provider.submitVoucherCalls);
	}

	@Test
	public void submitVoucher_shouldAllowWhenRequiredFieldsArePresentForAnyInsuranceType() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("CBHI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setTreatmentForNewBorn(Boolean.FALSE);
		request.setDiagnosisIds(Collections.singletonList("XM1QR3"));

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("PROC-001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.ONE);
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);
		Assert.assertNotNull(response);
		Assert.assertNull(response.getErrorMessage());
		Assert.assertEquals(1, provider.submitVoucherCalls);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void resolveDiagnosisIds_shouldExtractCodesFromLabeledDiagnosisText() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		Method method = RhipVoucherService.class.getDeclaredMethod("resolveDiagnosisIds", String.class);
		method.setAccessible(true);

		List<String> diagnosisIds = (List<String>) method.invoke(service,
				"FINAL DIAGNOSIS: XN7K1-Plasmodium malariae, CA40.1-Viral\npneumonia");

		Assert.assertEquals(2, diagnosisIds.size());
		Assert.assertEquals("XN7K1", diagnosisIds.get(0));
		Assert.assertEquals("CA40.1", diagnosisIds.get(1));
	}

	@Test
	public void normalizeVoucherInsuranceType_shouldResolveRamaInsuranceSeparatelyFromCbhi() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		Insurance insurance = new Insurance();
		insurance.setCategory("RSSB");
		insurance.setName("RAMA");

		Method method = RhipVoucherService.class.getDeclaredMethod("normalizeVoucherInsuranceType", Insurance.class);
		method.setAccessible(true);

		Assert.assertEquals("RAMA", method.invoke(service, insurance));
	}

	private static class CountingProvider extends RhipVoucherProvider {

		private int submitVoucherCalls = 0;

		@Override
		public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
			submitVoucherCalls++;
			IntegrationResponse response = new IntegrationResponse();
			response.setEnabled(true);
			response.setEndpointAccessible(true);
			response.setResponseCode(200);
			response.setResponseEntity("{\"success\":true}");
			return response;
		}
	}

	private static class TestConfig extends RhipVoucherIntegrationConfig {

		private final boolean voucherEnabled;

		private TestConfig(boolean voucherEnabled) {
			this.voucherEnabled = voucherEnabled;
		}

		@Override
		public boolean isVoucherEnabled() {
			return voucherEnabled;
		}
	}
}
