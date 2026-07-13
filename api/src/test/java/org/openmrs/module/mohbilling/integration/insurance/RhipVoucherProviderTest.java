package org.openmrs.module.mohbilling.integration.insurance;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.Collections;

public class RhipVoucherProviderTest {

	@Test
	public void submitVoucher_shouldReturnFriendlyErrorOnConnectionFailure() {
		RhipVoucherProvider provider = new RhipVoucherProvider();
		provider.setConfig(new TestConfig("http://127.0.0.1:1/unreachable"));

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

		IntegrationResponse response = provider.submitVoucher(request);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrorMessage());
		Assert.assertTrue(response.getErrorMessage().contains("Cannot connect to RHIP")
		        || response.getErrorMessage().contains("Error calling RHIP")
		        || response.getErrorMessage().contains("Timed out"));
	}

	@Test
	public void buildVoucherJson_shouldUseSwaggerInsuranceTypeEnumAndDefaultRamaPrescriptionDestination() throws Exception {
		RhipVoucherProvider provider = new RhipVoucherProvider();
		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("RAMA");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setVisitReferenceNumber("VR-2026-000123");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("PROC-001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.ONE);
		request.setProcedures(Collections.singletonList(procedure));

		Method method = RhipVoucherProvider.class.getDeclaredMethod("buildVoucherJson", RhipVoucherRequest.class);
		method.setAccessible(true);

		String json = (String) method.invoke(provider, request);

		Assert.assertTrue(json.contains("\"insuranceType\":\"rama\""));
		Assert.assertTrue(json.contains("\"prescriptionDestination\":\"FACILITY_DISPENSE\""));
		Assert.assertTrue(json.contains("\"visitReferenceNumber\":\"VR-2026-000123\""));
	}

	private static class TestConfig extends RhipVoucherIntegrationConfig {

		private final String voucherUrl;

		private TestConfig(String voucherUrl) {
			this.voucherUrl = voucherUrl;
		}

		@Override
		public String getVoucherUrl() {
			return voucherUrl;
		}

		@Override
		public boolean isVoucherEnabled() {
			return true;
		}

		@Override
		public String getVoucherApiKey() {
			return null;
		}

		@Override
		public String getVoucherApiOrigin() {
			return null;
		}
	}
}
