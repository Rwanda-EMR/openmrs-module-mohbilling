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
	public void buildVoucherJson_shouldUseTopLevelRamaVoucherFieldsAndOmitUnneededFields() throws Exception {
		RhipVoucherProvider provider = new RhipVoucherProvider();
		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("RAMA");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setPatientType("HOUSEHOLD_MEMBER");
		request.setVisitReferenceNumber("VR-2026-000123");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setHealthCareStayType("IN_PATIENT");
		request.setAdmissionDate("2026-07-12");
		request.setDiagnosisIds(Collections.singletonList("1F4Z"));
		request.setPatientPhoneNumber("0788967651");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("PROC-001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.ONE);
		procedure.setPrescribedAt("2026-07-12");
		procedure.setFrequency("BID");
		request.setProcedures(Collections.singletonList(procedure));

		Method method = RhipVoucherProvider.class.getDeclaredMethod("buildVoucherJson", RhipVoucherRequest.class);
		method.setAccessible(true);

		String json = (String) method.invoke(provider, request);

		Assert.assertTrue(json.contains("\"insuranceType\":\"rama\""));
		Assert.assertTrue(json.contains("\"patientIdentifier\":\"P-001\""));
		Assert.assertTrue(json.contains("\"procedures\""));
		Assert.assertTrue(json.contains("\"prescribedAt\":\"2026-07-12\""));
		Assert.assertTrue(json.contains("\"frequency\":\"BID\""));
		Assert.assertTrue(json.contains("\"prescriptionDestination\":\"FACILITY_DISPENSE\""));
		Assert.assertTrue(json.contains("\"visitReferenceNumber\":\"VR-2026-000123\""));
		Assert.assertFalse(json.contains("\"patientType\""));
		Assert.assertFalse(json.contains("\"receptionNumber\""));
		Assert.assertFalse(json.contains("\"userAccountCode\""));
		Assert.assertFalse(json.contains("\"processedBy\""));
		Assert.assertFalse(json.contains("\"notes\""));
		Assert.assertFalse(json.contains("\"dischargeDate\""));
		Assert.assertFalse(json.contains("\"treatmentForNewBorn\""));
		Assert.assertFalse(json.contains("\"price\""));
	}

	@Test
	public void buildVoucherJson_shouldUseVisitReferenceNumberForMmiReception() throws Exception {
		RhipVoucherProvider provider = new RhipVoucherProvider();
		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("MMI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("MMI-001");
		request.setReceptionNumber("REC-001");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setHealthCareStayType("OUT_PATIENT");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("RHIC-CTHS-020");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPosology("Review");
		request.setProcedures(Collections.singletonList(procedure));

		Method method = RhipVoucherProvider.class.getDeclaredMethod("buildVoucherJson", RhipVoucherRequest.class);
		method.setAccessible(true);

		String json = (String) method.invoke(provider, request);

		Assert.assertTrue(json.contains("\"insuranceType\":\"mmi\""));
		Assert.assertTrue(json.contains("\"visitReferenceNumber\":\"REC-001\""));
		Assert.assertFalse(json.contains("\"receptionNumber\""));
	}

	@Test
	public void buildVoucherJson_shouldNormalizeSpecialCaseInsuranceType() throws Exception {
		RhipVoucherProvider provider = new RhipVoucherProvider();
		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("SPECIAL_CASE");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("PROC-001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.ONE);
		request.setProcedures(Collections.singletonList(procedure));

		Method method = RhipVoucherProvider.class.getDeclaredMethod("buildVoucherJson", RhipVoucherRequest.class);
		method.setAccessible(true);

		String json = (String) method.invoke(provider, request);

		Assert.assertTrue(json.contains("\"insuranceType\":\"special_case\""));
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
