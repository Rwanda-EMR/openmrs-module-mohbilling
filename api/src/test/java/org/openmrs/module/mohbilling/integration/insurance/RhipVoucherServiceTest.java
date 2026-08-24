package org.openmrs.module.mohbilling.integration.insurance;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.RhipVoucherSubmission;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
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
		request.setPatientType("HOUSEHOLD_MEMBER");
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
	public void submitMmiVoucherForGlobalBillOnDischarge_shouldSubmitVoucherImmediately() {
		TestableRhipVoucherService service = new TestableRhipVoucherService();
		CountingProvider provider = new CountingProvider();
		TestBillingServiceHandler billingServiceHandler = new TestBillingServiceHandler();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);
		service.setBillingService((BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(),
				new Class[] { BillingService.class },
				billingServiceHandler));

		Insurance insurance = new Insurance();
		insurance.setName("MMI");
		insurance.setCategory("MMI");
		GlobalBill globalBill = new GlobalBill();
		globalBill.setGlobalBillId(10);
		globalBill.setInsurance(insurance);
		globalBill.setClosed(true);

		RhipVoucherSubmission submission = service.submitMmiVoucherForGlobalBillOnDischarge(globalBill);

		Assert.assertNotNull(submission);
		Assert.assertEquals(RhipVoucherSubmission.STATUS_SENT, submission.getStatus());
		Assert.assertNotNull(submission.getRequestPayload());
		Assert.assertEquals(1, billingServiceHandler.saveCalls);
		Assert.assertEquals(1, provider.submitVoucherCalls);
	}

	@Test
	public void closeVoucherForGlobalBillWithAudit_shouldCallVoucherClosureProvider() {
		TestableRhipVoucherService service = new TestableRhipVoucherService();
		CountingProvider provider = new CountingProvider();
		TestBillingServiceHandler billingServiceHandler = new TestBillingServiceHandler();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);
		service.setBillingService((BillingService) Proxy.newProxyInstance(
				BillingService.class.getClassLoader(),
				new Class[] { BillingService.class },
				billingServiceHandler));

		Insurance insurance = new Insurance();
		insurance.setName("MMI");
		insurance.setCategory("MMI");
		GlobalBill globalBill = new GlobalBill();
		globalBill.setGlobalBillId(10);
		globalBill.setInsurance(insurance);
		globalBill.setClosed(true);
		globalBill.setRhipVoucherCode("HF-1212222-22-20260727-1-87");
		globalBill.setRhipVoucherReferenceNumber("HF-1212222-22-20260727-1-87");

		RhipVoucherSubmission submission = service.closeVoucherForGlobalBillWithAudit(globalBill);

		Assert.assertNotNull(submission);
		Assert.assertEquals(RhipVoucherSubmission.STATUS_CONFIRMED, submission.getStatus());
		Assert.assertNotNull(submission.getRequestPayload());
		Assert.assertEquals(1, provider.closeVoucherCalls);
		Assert.assertEquals("HF-1212222-22-20260727-1-87", provider.lastClosedVoucherCode);
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
		request.setPatientType("HOUSEHOLD_MEMBER");
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
	public void submitVoucher_shouldRejectZeroPriceBeforeProviderCall() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = validVoucherRequest();
		RhipVoucherProcedure procedure = voucherProcedure("PROC-001", 1);
		procedure.setPrice(BigDecimal.ZERO);
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);

		Assert.assertNotNull(response);
		Assert.assertTrue(response.getErrorMessage().contains("price must be greater than 0"));
		Assert.assertEquals(0, provider.submitVoucherCalls);
	}

	@Test
	public void rejectNonPositivePriceProcedures_shouldRemoveZeroPriceItemsFromGlobalBillVoucherRequest() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		RhipVoucherRequest request = validVoucherRequest();
		RhipVoucherProcedure positivePriceProcedure = voucherProcedure("VALID-001", 1);
		RhipVoucherProcedure zeroPriceProcedure = voucherProcedure("ZERO-001", 2);
		zeroPriceProcedure.setPrice(BigDecimal.ZERO);
		request.setProcedures(Arrays.asList(positivePriceProcedure, zeroPriceProcedure));

		Method method = RhipVoucherService.class.getDeclaredMethod("rejectNonPositivePriceProcedures",
				GlobalBill.class, RhipVoucherRequest.class);
		method.setAccessible(true);

		List<?> rejectedProcedures = (List<?>) method.invoke(service, new GlobalBill(), request);

		Assert.assertEquals(1, rejectedProcedures.size());
		Assert.assertEquals(1, request.getProcedures().size());
		Assert.assertEquals("VALID-001", request.getProcedures().get(0).getCode());
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
		request.setPatientType("HOUSEHOLD_MEMBER");
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
		request.setPatientType("HOUSEHOLD_MEMBER");
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
	public void submitVoucher_shouldAllowRamaWhenVisitReferenceNumberIsMissing() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("RAMA");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setPrescriptionDestination("FACILITY_DISPENSE");

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
	public void submitVoucher_shouldAllowSpecialCaseWhenRequiredFieldsArePresent() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = validVoucherRequest();
		request.setInsuranceType("SPECIAL_CASE");
		request.setProcedures(Collections.singletonList(voucherProcedure("PROC-001", 1)));

		IntegrationResponse response = service.submitVoucher(request);

		Assert.assertNotNull(response);
		Assert.assertNull(response.getErrorMessage());
		Assert.assertEquals(1, provider.submitVoucherCalls);
	}

	@Test
	public void submitVoucher_shouldRequireMmiReceptionAsVisitReferenceNumber() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("MMI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("MMI-001");
		request.setPractitionerLicenseNumber("LIC-01");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("RHIC-CTHS-020");
		procedure.setQuantity(BigDecimal.ONE);
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);

		Assert.assertNotNull(response);
		Assert.assertTrue(response.getErrorMessage().contains("visitReferenceNumber is required for MMI voucher"));
		Assert.assertEquals(0, provider.submitVoucherCalls);
	}

	@Test
	public void submitVoucher_shouldRequireInstructionsAndDurationForMmiMedicineLines() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("MMI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("MMI-001");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setVisitReferenceNumber("REC-001");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("G03HA01001");
		procedure.setQuantity(BigDecimal.ONE);
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);

		Assert.assertNotNull(response);
		Assert.assertTrue(response.getErrorMessage().contains("instructions is required for MMI medicine lines"));
		Assert.assertTrue(response.getErrorMessage().contains("durationDays is required for MMI medicine lines"));
		Assert.assertEquals(0, provider.submitVoucherCalls);
	}

	@Test
	public void submitVoucher_shouldSplitCombinedMmiMedicineInstructions() {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("MMI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("MMI-001");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setVisitReferenceNumber("REC-001");

		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode("G03HA01001");
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setInstructions("Dose: 1; Route: Intrathecal; Frequency: ONCE A DAY; "
				+ "Duration: 5 DAYS; Anytime you feel pain");
		procedure.setFrequency(procedure.getInstructions());
		request.setProcedures(Collections.singletonList(procedure));

		IntegrationResponse response = service.submitVoucher(request);

		Assert.assertNull(response.getErrorMessage());
		Assert.assertEquals(1, provider.submitVoucherCalls);
		Assert.assertEquals("1 Intrathecal", provider.lastRequest.getProcedures().get(0).getPosology());
		Assert.assertEquals("QD", provider.lastRequest.getProcedures().get(0).getFrequency());
		Assert.assertEquals(Integer.valueOf(5), provider.lastRequest.getProcedures().get(0).getDurationDays());
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

	@Test
	public void requiresPractitionerRegistration_shouldSkipRama() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		Method method = RhipVoucherService.class.getDeclaredMethod("requiresPractitionerRegistration", String.class);
		method.setAccessible(true);

		Assert.assertEquals(Boolean.FALSE, method.invoke(service, "RAMA"));
		Assert.assertEquals(Boolean.TRUE, method.invoke(service, "CBHI"));
	}

	@Test
	public void resolveRamaVisitReferenceNumber_shouldUseBillIdentifierPlusFosaId() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		GlobalBill globalBill = new GlobalBill();
		globalBill.setBillIdentifier("BILL-123");

		Method method = RhipVoucherService.class.getDeclaredMethod("resolveRamaVisitReferenceNumber",
				GlobalBill.class, String.class);
		method.setAccessible(true);

		Assert.assertEquals("BILL-123417", method.invoke(service, globalBill, "417"));
	}

	@Test
	public void toProcedure_shouldSetFrequencyForMedicamentCategory() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		ServiceCategory category = new ServiceCategory();
		category.setName("MEDICAMENTS");
		FacilityServicePrice facilityServicePrice = new FacilityServicePrice();
		facilityServicePrice.setName("Medication|A07DA03001");
		facilityServicePrice.setFullPrice(BigDecimal.ONE);
		BillableService billableService = new BillableService();
		billableService.setServiceCategory(category);
		billableService.setFacilityServicePrice(facilityServicePrice);
		PatientServiceBill billItem = new PatientServiceBill();
		billItem.setService(billableService);
		billItem.setQuantity(BigDecimal.ONE);
		billItem.setDrugFrequency("BID");

		Method method = RhipVoucherService.class.getDeclaredMethod("toProcedure", PatientServiceBill.class,
				org.openmrs.module.mohbilling.model.Admission.class);
		method.setAccessible(true);

		RhipVoucherProcedure procedure = (RhipVoucherProcedure) method.invoke(service, billItem, null);

		Assert.assertNotNull(procedure);
		Assert.assertEquals("A07DA03001", procedure.getCode());
		Assert.assertEquals("BID", procedure.getFrequency());
	}

	@Test
	public void toProcedure_shouldSplitCombinedPrescriptionForMedicamentCategory() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		ServiceCategory category = new ServiceCategory();
		category.setName("MEDICAMENTS");
		FacilityServicePrice facilityServicePrice = new FacilityServicePrice();
		facilityServicePrice.setName("Medication|G03HA01001");
		facilityServicePrice.setFullPrice(BigDecimal.ONE);
		BillableService billableService = new BillableService();
		billableService.setServiceCategory(category);
		billableService.setFacilityServicePrice(facilityServicePrice);
		PatientServiceBill billItem = new PatientServiceBill();
		billItem.setService(billableService);
		billItem.setQuantity(BigDecimal.ONE);
		billItem.setDrugFrequency("Dose: 1; Route: Intrathecal; Frequency: ONCE A DAY; "
				+ "Duration: 5 DAYS; Anytime you feel pain");

		Method method = RhipVoucherService.class.getDeclaredMethod("toProcedure", PatientServiceBill.class,
				org.openmrs.module.mohbilling.model.Admission.class);
		method.setAccessible(true);

		RhipVoucherProcedure procedure = (RhipVoucherProcedure) method.invoke(service, billItem, null);

		Assert.assertNotNull(procedure);
		Assert.assertEquals("1 Intrathecal", procedure.getPosology());
		Assert.assertEquals("QD", procedure.getFrequency());
		Assert.assertEquals(Integer.valueOf(5), procedure.getDurationDays());
		Assert.assertTrue(procedure.getInstructions().contains("Anytime you feel pain"));
	}

	@Test
	public void normalizeRhipFrequency_shouldMapHumanLabelsToRhipEnumValues() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		Method method = RhipVoucherService.class.getDeclaredMethod("normalizeRhipFrequency", String.class);
		method.setAccessible(true);

		Assert.assertEquals("QD", method.invoke(service, "ONCE A DAY"));
		Assert.assertEquals("TID", method.invoke(service, "THREE A DAY"));
		Assert.assertEquals("BID", method.invoke(service, "twice daily"));
		Assert.assertEquals("Q6H", method.invoke(service, "Every 6 hours"));
		Assert.assertNull(method.invoke(service, "an unsupported frequency"));
	}

	@Test
	public void extractProcedureCode_shouldKeepPipeSeparatedCodeAsPreferredFormat() throws Exception {
		FacilityServicePrice facilityServicePrice = new FacilityServicePrice();
		facilityServicePrice.setName("Medication|A07DA03001");

		Assert.assertEquals("A07DA03001", extractProcedureCode(facilityServicePrice));
	}

	@Test
	public void extractProcedureCode_shouldExtractDashSeparatedRhipCodeSuffix() throws Exception {
		FacilityServicePrice procedurePrice = new FacilityServicePrice();
		procedurePrice.setName("Above elbow cast-RHIC-MINS-012");
		FacilityServicePrice labPrice = new FacilityServicePrice();
		labPrice.setName("Ag HBS-RLTC-SERO-005");

		Assert.assertEquals("RHIC-MINS-012", extractProcedureCode(procedurePrice));
		Assert.assertEquals("RLTC-SERO-005", extractProcedureCode(labPrice));
	}

	@Test
	public void rpmVoucherItemResolver_shouldSupportMedicamentAndConsommablesCategories() {
		RpmVoucherItemResolver resolver = new RpmVoucherItemResolver();

		Assert.assertTrue(resolver.supports(billItemWithCategory("MEDICAMENTS")));
		Assert.assertTrue(resolver.supports(billItemWithCategory("CONSOMMABLES")));
		Assert.assertFalse(resolver.supports(billItemWithCategory("LABORATOIRE")));
	}

	@Test
	public void rpmVoucherItemResolver_shouldResolveNpcCodeByExactBillingItemName() {
		TestRpmVoucherItemResolver resolver = new TestRpmVoucherItemResolver();
		PatientServiceBill billItem = billItemWithCategory("MEDICAMENTS");
		FacilityServicePrice facilityServicePrice = new FacilityServicePrice();
		facilityServicePrice.setName("PARACETAMOL 250MG SUPPOSITORY");
		billItem.getService().setFacilityServicePrice(facilityServicePrice);
		billItem.setQuantity(new BigDecimal("5"));
		PatientBill patientBill = new PatientBill();
		patientBill.setPatientBillId(42);
		Consommation consommation = new Consommation();
		consommation.setPatientBill(patientBill);

		RhipVoucherProcedure procedure = resolver.resolve(null, consommation, billItem, null, "MMI");

		Assert.assertEquals("NPC-PARA-250-SUP", procedure.getCode());
		Assert.assertEquals(new BigDecimal("5"), procedure.getQuantity());
		Assert.assertEquals("2026-08-18", procedure.getDispensingDate());
		Assert.assertTrue(resolver.itemLookupSql.contains("lower(trim(`name`))"));
		Assert.assertTrue(resolver.itemLookupSql.contains("PARACETAMOL 250MG SUPPOSITORY"));
		Assert.assertTrue(resolver.dispensingLookupSql.contains("r.patient_bill_id = 42"));
		Assert.assertTrue(resolver.dispensingLookupSql.contains("drl.item_id = 1"));
	}

	@Test
	public void handlePartialVoucherSubmission_shouldRetryWithOnlyValidProcedures() throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		CountingProvider provider = new CountingProvider();
		service.setConfig(new TestConfig(true));
		service.setVoucherProvider(provider);

		RhipVoucherProcedure validProcedure = voucherProcedure("VALID-001", 1);
		RhipVoucherProcedure invalidProcedure = voucherProcedure("INVALID-001", 2);
		RhipVoucherRequest request = validVoucherRequest();
		request.setProcedures(Arrays.asList(validProcedure, invalidProcedure));

		IntegrationResponse firstResponse = new IntegrationResponse();
		firstResponse.setEnabled(true);
		firstResponse.setResponseCode(200);
		firstResponse.setResponseEntity("{\"success\":false,\"errors\":["
				+ "{\"productId\":\"VALID-001\",\"message\":\"Prescription is valid\",\"isValid\":true},"
				+ "{\"productId\":\"INVALID-001\",\"message\":\"Product not found\",\"isValid\":false}"
				+ "]}");

		GlobalBill globalBill = new GlobalBill();
		globalBill.setGlobalBillId(10);
		Method method = RhipVoucherService.class.getDeclaredMethod("handlePartialVoucherSubmission",
				GlobalBill.class, RhipVoucherRequest.class, IntegrationResponse.class);
		method.setAccessible(true);

		IntegrationResponse response = (IntegrationResponse) method.invoke(service, globalBill, request, firstResponse);

		Assert.assertNotNull(response);
		Assert.assertEquals(1, provider.submitVoucherCalls);
		Assert.assertNotNull(provider.lastRequest);
		Assert.assertEquals(1, provider.lastRequest.getProcedures().size());
		Assert.assertEquals("VALID-001", provider.lastRequest.getProcedures().get(0).getCode());
	}

	private String extractProcedureCode(FacilityServicePrice facilityServicePrice) throws Exception {
		RhipVoucherService service = new RhipVoucherService();
		Method method = RhipVoucherService.class.getDeclaredMethod("extractProcedureCode", FacilityServicePrice.class);
		method.setAccessible(true);
		return (String) method.invoke(service, facilityServicePrice);
	}

	private PatientServiceBill billItemWithCategory(String categoryName) {
		ServiceCategory category = new ServiceCategory();
		category.setName(categoryName);
		BillableService billableService = new BillableService();
		billableService.setServiceCategory(category);
		PatientServiceBill billItem = new PatientServiceBill();
		billItem.setService(billableService);
		return billItem;
	}

	private RhipVoucherRequest validVoucherRequest() {
		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("CBHI");
		request.setFacilityFosaId("FOSA-001");
		request.setPatientIdentifier("P-001");
		request.setPatientType("HOUSEHOLD_MEMBER");
		request.setPractitionerLicenseNumber("LIC-01");
		request.setTreatmentForNewBorn(Boolean.FALSE);
		request.setDiagnosisIds(Collections.singletonList("XM1QR3"));
		return request;
	}

	private RhipVoucherProcedure voucherProcedure(String code, int itemId) {
		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode(code);
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(BigDecimal.TEN);
		procedure.setPatientServiceBillId(itemId);
		return procedure;
	}

	private static class TestableRhipVoucherService extends RhipVoucherService {

		@Override
		public RhipVoucherRequest buildVoucherRequestFromGlobalBill(GlobalBill globalBill, String eligibilityIdentifier) {
			RhipVoucherRequest request = new RhipVoucherRequest();
			request.setInsuranceType("MMI");
			request.setFacilityFosaId("FOSA-001");
			request.setPatientIdentifier("P-001");
			request.setPractitionerLicenseNumber("LIC-01");
			request.setVisitReferenceNumber("VISIT-001");
			request.setProcedures(Collections.singletonList(voucherProcedureForSubmit()));
			return request;
		}

		private RhipVoucherProcedure voucherProcedureForSubmit() {
			RhipVoucherProcedure procedure = new RhipVoucherProcedure();
			procedure.setCode("RHIC-001");
			procedure.setQuantity(BigDecimal.ONE);
			procedure.setPrice(BigDecimal.TEN);
			return procedure;
		}
	}

	private static class TestBillingServiceHandler implements java.lang.reflect.InvocationHandler {

		private int saveCalls = 0;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			if ("getLatestRhipVoucherSubmission".equals(method.getName())
					|| "getSuccessfulRhipVoucherSubmission".equals(method.getName())) {
				return null;
			}
			if ("getRhipVoucherSubmissionsByGlobalBill".equals(method.getName())) {
				return Collections.emptyList();
			}
			if ("saveRhipVoucherSubmission".equals(method.getName())) {
				saveCalls++;
				return args[0];
			}
			if (method.getReturnType().equals(Boolean.TYPE)) {
				return false;
			}
			if (method.getReturnType().equals(Integer.TYPE)) {
				return 0;
			}
			return null;
		}
	}

	private static class CountingProvider extends RhipVoucherProvider {

		private int submitVoucherCalls = 0;
		private int closeVoucherCalls = 0;
		private RhipVoucherRequest lastRequest;
		private String lastClosedVoucherCode;

		@Override
		public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
			submitVoucherCalls++;
			lastRequest = request;
			IntegrationResponse response = new IntegrationResponse();
			response.setEnabled(true);
			response.setEndpointAccessible(true);
			response.setResponseCode(200);
			response.setResponseEntity("{\"success\":true}");
			return response;
		}

		@Override
		public IntegrationResponse closeVoucher(String insuranceType, String facilityFosaId, String voucherCode,
				String verifiedBy, String processedBy) {
			closeVoucherCalls++;
			lastClosedVoucherCode = voucherCode;
			IntegrationResponse response = new IntegrationResponse();
			response.setEnabled(true);
			response.setEndpointAccessible(true);
			response.setResponseCode(200);
			response.setResponseEntity("{\"success\":true}");
			return response;
		}
	}

	private static class TestRpmVoucherItemResolver extends RpmVoucherItemResolver {

		private String itemLookupSql;
		private String dispensingLookupSql;

		@Override
		protected List<List<Object>> executeSql(String sql) {
			if (sql.contains("information_schema.tables") && sql.contains("table_name = 'rpm_item'")) {
				return Collections.<List<Object>>singletonList(Collections.<Object>singletonList("rpm_item"));
			}
			if (sql.contains("information_schema.tables")) {
				return Collections.<List<Object>>singletonList(Collections.<Object>singletonList("rpm_item"));
			}
			if (sql.contains("information_schema.columns")) {
				return Arrays.<List<Object>>asList(
						Collections.<Object>singletonList("item_id"),
						Collections.<Object>singletonList("name"),
						Collections.<Object>singletonList("code"),
						Collections.<Object>singletonList("voided"));
			}
			if (sql.contains(" from `rpm_item` ")) {
				itemLookupSql = sql;
				return Collections.<List<Object>>singletonList(Arrays.<Object>asList(
						1, "NPC-PARA-250-SUP", "PARACETAMOL 250MG SUPPOSITORY"));
			}
			if (sql.contains("from rpm_dispense_record dr")) {
				dispensingLookupSql = sql;
				return Collections.<List<Object>>singletonList(
						Collections.<Object>singletonList("2026-08-18 14:35:00"));
			}
			return Collections.emptyList();
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

		@Override
		public String getVoucherClosureUrl() {
			return "http://rhip.example/insurance_integration/api/v2/voucher-closure";
		}

		@Override
		public String getDefaultFosaId() {
			return "10023";
		}
	}
}
