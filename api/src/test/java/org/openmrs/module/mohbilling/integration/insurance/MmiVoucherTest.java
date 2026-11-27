package org.openmrs.module.mohbilling.integration.insurance;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.RhipVoucherItemRecord;
import org.openmrs.module.mohbilling.model.RhipVoucherSubmission;
import org.openmrs.module.mohbilling.service.BillingService;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * MMI encounter (voucher) behaviour from the RHIP Unified Insurance Integration Guide: MMI, section 9
 */
public class MmiVoucherTest {

	private static final String VOUCHER_WITH_ISSUES = "{\"success\":true,"
			+ "\"message\":\"Voucher created successfully (2 procedure(s) not recorded)\","
			+ "\"data\":{\"voucherCode\":\"HF-1-87\",\"voucherReferenceNumber\":\"HF-1-87\",\"status\":\"CREATED\","
			+ "\"issues\":["
			+ "{\"type\":\"PROCEDURE_BLOCKED\",\"code\":\"RHIC-OPTH-072\",\"reason\":\"Needs an approval request first\"},"
			+ "{\"type\":\"UNRESOLVED_CODE\",\"code\":\"N02BE01001\",\"reason\":\"Several products\",\"products\":["
			+ "{\"medProcCode\":\"MP-1101\",\"name\":\"Paracetamol 500mg\",\"facilityPrice\":20},"
			+ "{\"medProcCode\":\"MP-1102\",\"name\":\"Paracetamol 1g\",\"facilityPrice\":35}]},"
			+ "{\"type\":\"MEDICINE_WARNING\",\"code\":\"J01CA04001\",\"reason\":\"Above usual duration\"},"
			+ "{\"type\":\"UNRESOLVED_DIAGNOSIS\",\"code\":\"XX99\",\"reason\":\"Unknown ICD-11 code\"}]}}";

	@Test
	public void mmiPayload_shouldOnlyCarryFieldsMmiReads() throws Exception {
		RhipVoucherRequest request = mmiRequest();
		request.setUserAccountCode("LIC-01");
		request.setProcessedBy("admin");
		request.setNotes("notes");
		request.setPatientType("1");
		request.setAdmissionDate("2026-11-20");
		request.setDischargeDate("2026-11-24");
		request.setPatientPhoneNumber("0788000000");
		request.setReferralFacilityId("REF");
		request.setHealthCareStayType("OUT_PATIENT");

		RhipVoucherProcedure consultation = procedure("RHIC-CTHS-020", false, new BigDecimal("5000"));
		RhipVoucherProcedure fromDelivery = procedure("G03HA01001", true, new BigDecimal("150"));
		fromDelivery.setInstructions("1 tablet oral, twice a day");
		fromDelivery.setDurationDays(5);
		fromDelivery.setDeliveryReferenceNumber("GRN-2026-041");
		RhipVoucherProcedure declaredPrice = procedure("J01CA04001", true, new BigDecimal("95"));
		declaredPrice.setInstructions("1 capsule every 8 hours");
		request.setProcedures(Arrays.asList(consultation, fromDelivery, declaredPrice));

		Map<String, Object> payload = new RhipVoucherProvider().buildMmiVoucherPayload(request);

		Assert.assertEquals(Arrays.asList("insuranceType", "facilityFosaId", "patientIdentifier", "visitReferenceNumber",
				"practitionerLicenseNumber", "diagnosisIds", "healthCareStayType", "procedures"),
				new ArrayList<String>(payload.keySet()));
		Assert.assertEquals("mmi", payload.get("insuranceType"));
		Assert.assertEquals("REC-001", payload.get("visitReferenceNumber"));
		List<?> lines = (List<?>) payload.get("procedures");
		Map<?, ?> consultationLine = (Map<?, ?>) lines.get(0);
		Assert.assertFalse("MMI prices procedures itself", consultationLine.containsKey("price"));
		Assert.assertFalse(consultationLine.containsKey("instructions"));
		Assert.assertEquals("RHIP takes whole quantities", Integer.valueOf(1), consultationLine.get("quantity"));
		Assert.assertFalse("hasPurchaseOrder is for drugs and consumables only", consultationLine.containsKey("hasPurchaseOrder"));
		Map<?, ?> deliveryLine = (Map<?, ?>) lines.get(1);
		Assert.assertEquals(Boolean.TRUE, deliveryLine.get("hasPurchaseOrder"));
		Assert.assertEquals("GRN-2026-041", deliveryLine.get("deliveryReferenceNumber"));
		Assert.assertFalse("A delivery line is billed at the delivery price", deliveryLine.containsKey("price"));
		Assert.assertEquals(5, deliveryLine.get("durationDays"));
		Map<?, ?> declaredLine = (Map<?, ?>) lines.get(2);
		Assert.assertEquals(Boolean.FALSE, declaredLine.get("hasPurchaseOrder"));
		Assert.assertEquals(new BigDecimal("95"), declaredLine.get("price"));
		Assert.assertEquals("1 capsule every 8 hours", declaredLine.get("instructions"));
		Assert.assertFalse(declaredLine.containsKey("durationDays"));
		Assert.assertFalse(declaredLine.containsKey("deliveryReferenceNumber"));
	}

	@Test
	public void auditCopy_shouldShowHasPurchaseOrderOnDrugAndConsumableLines() throws Exception {
		RhipVoucherProcedure fromDelivery = procedure("G03HA01001", true, BigDecimal.TEN);
		fromDelivery.setDeliveryReferenceNumber("GRN-2026-041");
		RhipVoucherProcedure declaredPrice = procedure("J01CA04001", true, BigDecimal.TEN);
		RhipVoucherProcedure consultation = procedure("RHIC-CTHS-020", false, BigDecimal.TEN);
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

		Assert.assertTrue(mapper.writeValueAsString(fromDelivery).contains("\"hasPurchaseOrder\":true"));
		Assert.assertTrue(mapper.writeValueAsString(declaredPrice).contains("\"hasPurchaseOrder\":false"));
		Assert.assertTrue(mapper.writeValueAsString(consultation).contains("\"hasPurchaseOrder\":null"));
	}

	@Test
	public void icd11Diagnoses_shouldBeReadForThePatientBetweenAdmissionAndDischargeDays() throws Exception {
		Icd11TableService service = new Icd11TableService(Arrays.asList("DA01.0", "1F4Z", "DA01.0"));
		org.openmrs.Patient patient = new org.openmrs.Patient(42);
		org.openmrs.module.mohbilling.model.Admission admission = new org.openmrs.module.mohbilling.model.Admission();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
		admission.setAdmissionDate(format.parse("2026-11-25 09:30"));
		admission.setDischargingDate(format.parse("2026-11-26 15:00"));

		List<String> codes = service.resolveIcd11DiagnosisCodes(patient, admission, new GlobalBill());

		Assert.assertEquals(Arrays.asList("DA01.0", "1F4Z"), codes);
		Assert.assertTrue(service.lastSql.contains("from rwandaemr_icd11_patient_diagnosis d"));
		Assert.assertTrue(service.lastSql.contains("d.patient_id = 42 "));
		Assert.assertTrue(service.lastSql.contains("d.voided = 0 and e.voided = 0"));
		Assert.assertTrue(service.lastSql.contains("e.encounter_datetime >= '2026-11-25 00:00:00'"));
		Assert.assertTrue(service.lastSql.contains("e.encounter_datetime <= '2026-11-26 23:59:59'"));
	}

	@Test
	public void icd11Diagnoses_shouldBeEmptyWithoutPatient() {
		Icd11TableService service = new Icd11TableService(Collections.singletonList("1F4Z"));
		Assert.assertTrue(service.resolveIcd11DiagnosisCodes(null, null, new GlobalBill()).isEmpty());
		Assert.assertNull("No query is run without a patient", service.lastSql);
	}

	@Test
	public void diagnosisIds_shouldMergeObsAndIcd11CodesOnce() {
		Assert.assertEquals(Arrays.asList("1F4Z", "CA40.Z", "DA01.0"), RhipVoucherService.mergeDiagnosisIds(
				Arrays.asList("1F4Z", "CA40.Z"), Arrays.asList("CA40.Z", "DA01.0")));
		Assert.assertEquals(Collections.singletonList("DA01.0"),
				RhipVoucherService.mergeDiagnosisIds(Collections.<String>emptyList(), Collections.singletonList("DA01.0")));
		Assert.assertTrue(RhipVoucherService.mergeDiagnosisIds(null, null).isEmpty());
	}

	private static class Icd11TableService extends RhipVoucherService {

		private final List<String> codes;

		private String lastSql;

		private Icd11TableService(List<String> codes) {
			this.codes = codes;
		}

		@Override
		protected List<List<Object>> executeSql(String sql) {
			lastSql = sql;
			List<List<Object>> rows = new ArrayList<List<Object>>();
			for (String code : codes) {
				rows.add(Collections.<Object>singletonList(code));
			}
			return rows;
		}
	}

	@Test
	public void submitVoucher_shouldRequireDiagnosisForMmi() {
		RhipVoucherService service = new RhipVoucherService();
		service.setConfig(new EnabledConfig());
		service.setVoucherProvider(new FixedProvider("{\"success\":true}"));
		RhipVoucherRequest request = mmiRequest();
		request.setDiagnosisIds(null);
		request.setProcedures(Collections.singletonList(procedure("RHIC-CTHS-020", false, BigDecimal.TEN)));

		IntegrationResponse response = service.submitVoucher(request);

		Assert.assertTrue(response.getErrorMessage().contains("diagnosisIds is required"));
	}

	@Test
	public void issues_shouldSplitBlockingLinesFromWarnings() {
		RhipVoucherService service = new RhipVoucherService();
		RhipVoucherService.MmiVoucherIssues issues = service.extractMmiVoucherIssues(response(VOUCHER_WITH_ISSUES));

		Assert.assertEquals(Arrays.asList("RHIC-OPTH-072", "N02BE01001"), new ArrayList<String>(issues.blockingByCode.keySet()));
		Assert.assertEquals("Needs MMI approval first: Needs an approval request first", issues.blockingByCode.get("RHIC-OPTH-072"));
		Assert.assertTrue(issues.blockingByCode.get("N02BE01001").contains(
				"map the item to one of: MP-1101 Paracetamol 500mg (facility price 20), MP-1102 Paracetamol 1g (facility price 35)"));
		Assert.assertEquals(Collections.singletonList("J01CA04001"), new ArrayList<String>(issues.warningByCode.keySet()));
		Assert.assertEquals(1, issues.other.size());
		Assert.assertTrue(issues.other.get(0).contains("[XX99]"));
	}

	@Test
	public void issues_shouldBeEmptyWhenMmiRecordedEverything() {
		RhipVoucherService service = new RhipVoucherService();
		String clean = "{\"success\":true,\"data\":{\"voucherCode\":\"HF-1-87\",\"procedures\":[]}}";
		Assert.assertNull(service.describeMmiVoucherIssues(response(clean)));
	}

	@Test
	public void recordMmiVoucherLines_shouldRecordDroppedLinesAsRejected() {
		RecordingBillingService billing = new RecordingBillingService();
		RhipVoucherService service = new RhipVoucherService();
		service.setBillingService(billing.proxy());
		RhipVoucherRequest request = mmiRequest();
		RhipVoucherProcedure consultation = procedure("RHIC-CTHS-020", false, BigDecimal.TEN);
		consultation.setPatientServiceBillId(1);
		RhipVoucherProcedure blocked = procedure("RHIC-OPTH-072", false, BigDecimal.TEN);
		blocked.setPatientServiceBillId(2);
		RhipVoucherProcedure warned = procedure("J01CA04001", true, BigDecimal.TEN);
		warned.setPatientServiceBillId(3);
		request.setProcedures(Arrays.asList(consultation, blocked, warned));

		service.recordMmiVoucherLines(new GlobalBill(), request, response(VOUCHER_WITH_ISSUES));

		Assert.assertEquals(3, billing.savedRecords.size());
		RhipVoucherItemRecord consultationRecord = billing.recordFor(1);
		Assert.assertEquals(RhipVoucherItemRecord.STATUS_SENT, consultationRecord.getStatus());
		Assert.assertEquals("HF-1-87", consultationRecord.getVoucherCode());
		Assert.assertNull(consultationRecord.getRejectionReason());
		RhipVoucherItemRecord blockedRecord = billing.recordFor(2);
		Assert.assertEquals(RhipVoucherItemRecord.STATUS_REJECTED, blockedRecord.getStatus());
		Assert.assertTrue(blockedRecord.getRejectionReason().startsWith("Needs MMI approval first"));
		RhipVoucherItemRecord warnedRecord = billing.recordFor(3);
		Assert.assertEquals(RhipVoucherItemRecord.STATUS_SENT, warnedRecord.getStatus());
		Assert.assertTrue(warnedRecord.getRejectionReason().startsWith("Recorded with a warning"));
	}

	@Test
	public void resubmitMmiRejectedLines_shouldSendOnlyLinesWhoseLatestRecordIsRejected() {
		RecordingBillingService billing = new RecordingBillingService();
		billing.existingRecords.add(record(1, RhipVoucherItemRecord.STATUS_SENT, 1000L));
		billing.existingRecords.add(record(2, RhipVoucherItemRecord.STATUS_REJECTED, 1000L));
		billing.existingRecords.add(record(3, RhipVoucherItemRecord.STATUS_REJECTED, 1000L));
		billing.existingRecords.add(record(3, RhipVoucherItemRecord.STATUS_SENT, 2000L));
		FixedProvider provider = new FixedProvider("{\"success\":true,\"data\":{\"voucherCode\":\"HF-1-87\","
				+ "\"voucherReferenceNumber\":\"HF-1-87\"}}");
		ThreeLineService service = new ThreeLineService();
		service.setConfig(new EnabledConfig());
		service.setVoucherProvider(provider);
		service.setBillingService(billing.proxy());

		RhipVoucherSubmission submission = service.resubmitMmiRejectedLines(mmiGlobalBill());

		Assert.assertEquals(RhipVoucherSubmission.STATUS_SENT, submission.getStatus());
		Assert.assertEquals(1, provider.lastRequest.getProcedures().size());
		Assert.assertEquals(Integer.valueOf(2), provider.lastRequest.getProcedures().get(0).getPatientServiceBillId());
		Assert.assertEquals(RhipVoucherItemRecord.STATUS_SENT, billing.recordFor(2).getStatus());
	}

	@Test
	public void approvalLabel_shouldShowTheApprovalStateOfABilledLine() {
		java.util.Set<String> required = new java.util.HashSet<String>(Arrays.asList(
				"RHIC-MRSC-001", "RHIC-OPTH-072", "RHIC-PLAS-011", "RHIC-ANES-001"));
		java.util.Map<String, RhipVoucherService.MmiApproval> approvals = new java.util.HashMap<String, RhipVoucherService.MmiApproval>();
		approvals.put("RHIC-OPTH-072", new RhipVoucherService.MmiApproval("PENDING", null));
		approvals.put("RHIC-PLAS-011", new RhipVoucherService.MmiApproval("PARTIALLY_APPROVED", BigDecimal.ONE));
		approvals.put("RHIC-ANES-001", new RhipVoucherService.MmiApproval("REJECTED", BigDecimal.ZERO));

		Assert.assertNull(RhipVoucherService.approvalLabel("RHIC-CTHS-020", BigDecimal.ONE, required, approvals));
		Assert.assertNull(RhipVoucherService.approvalLabel(null, BigDecimal.ONE, required, approvals));
		RhipVoucherService.ApprovalLabel notRequested = RhipVoucherService.approvalLabel("rhic-mrsc-001", BigDecimal.ONE, required, approvals);
		Assert.assertEquals("WAITING", notRequested.getState());
		Assert.assertEquals("Waiting for MMI approval", notRequested.getLabel());
		Assert.assertEquals("Approval not requested yet", notRequested.getDetail());
		Assert.assertEquals("Requested, MMI has not decided yet",
				RhipVoucherService.approvalLabel("RHIC-OPTH-072", BigDecimal.ONE, required, approvals).getDetail());
		RhipVoucherService.ApprovalLabel partly = RhipVoucherService.approvalLabel("RHIC-PLAS-011", new BigDecimal("2.00"), required, approvals);
		Assert.assertEquals("APPROVED", partly.getState());
		Assert.assertEquals("Only 1 of 2 approved; the rest is not sent", partly.getDetail());
		Assert.assertNull(RhipVoucherService.approvalLabel("RHIC-PLAS-011", BigDecimal.ONE, required, approvals).getDetail());
		Assert.assertEquals("REJECTED", RhipVoucherService.approvalLabel("RHIC-ANES-001", BigDecimal.ONE, required, approvals).getState());
		Assert.assertEquals("An approval request is enough when RHIP's list could not be obtained", "WAITING",
				RhipVoucherService.approvalLabel("RHIC-OPTH-072", BigDecimal.ONE, null, approvals).getState());
	}

	@Test
	public void applyApprovalRule_shouldBillAProcedureNeedingApprovalOnlyOnceApproved() {
		RhipVoucherProcedure consultation = procedure("RHIC-CTHS-020", false, BigDecimal.TEN);
		RhipVoucherProcedure notRequested = procedure("RHIC-MRSC-001", false, BigDecimal.TEN);
		RhipVoucherProcedure pending = procedure("RHIC-OPTH-072", false, BigDecimal.TEN);
		RhipVoucherProcedure approved = procedure("RHIC-PLAS-011", false, BigDecimal.TEN);
		approved.setQuantity(new BigDecimal("2"));
		RhipVoucherProcedure rejected = procedure("RHIC-ANES-001", false, BigDecimal.TEN);
		java.util.Set<String> required = new java.util.HashSet<String>(Arrays.asList(
				"RHIC-MRSC-001", "RHIC-OPTH-072", "RHIC-PLAS-011", "RHIC-ANES-001"));
		java.util.Map<String, RhipVoucherService.MmiApproval> approvals = new java.util.HashMap<String, RhipVoucherService.MmiApproval>();
		approvals.put("RHIC-OPTH-072", new RhipVoucherService.MmiApproval("PENDING", null));
		approvals.put("RHIC-PLAS-011", new RhipVoucherService.MmiApproval("PARTIALLY_APPROVED", BigDecimal.ONE));
		approvals.put("RHIC-ANES-001", new RhipVoucherService.MmiApproval("REJECTED", BigDecimal.ZERO));
		java.util.Map<String, String> reasons = new java.util.HashMap<String, String>();

		java.util.List<RhipVoucherProcedure> held = RhipVoucherService.applyApprovalRule(
				Arrays.asList(consultation, notRequested, pending, approved, rejected), required, approvals, reasons);

		Assert.assertEquals(Arrays.asList(notRequested, pending, rejected), held);
		Assert.assertEquals(BigDecimal.ONE, approved.getQuantity());
		Assert.assertEquals("Needs MMI approval first: no approval requested yet", reasons.get("RHIC-MRSC-001"));
		Assert.assertEquals("Needs MMI approval first: waiting for MMI's decision", reasons.get("RHIC-OPTH-072"));
		Assert.assertEquals("Needs MMI approval first: MMI rejected the approval", reasons.get("RHIC-ANES-001"));
	}

	@Test
	public void applyApprovalRule_shouldHoldLinesWithAnApprovalRequestWhenRhipsListIsUnavailable() {
		RhipVoucherProcedure consultation = procedure("RHIC-CTHS-020", false, BigDecimal.TEN);
		RhipVoucherProcedure pending = procedure("RHIC-OPTH-072", false, BigDecimal.TEN);
		java.util.Map<String, RhipVoucherService.MmiApproval> approvals = new java.util.HashMap<String, RhipVoucherService.MmiApproval>();
		approvals.put("RHIC-OPTH-072", new RhipVoucherService.MmiApproval("PENDING", null));

		java.util.List<RhipVoucherProcedure> held = RhipVoucherService.applyApprovalRule(
				Arrays.asList(consultation, pending), null, approvals, new java.util.HashMap<String, String>());

		Assert.assertEquals(Collections.singletonList(pending), held);
	}

	@Test
	public void readProducts_shouldReadTheApprovalRequiredList() throws Exception {
		java.util.List<RhipApprovalRequiredProducts.Product> products = RhipApprovalRequiredProducts.readProducts(
				new com.fasterxml.jackson.databind.ObjectMapper().readTree("{\"success\":true,\"data\":{\"products\":["
						+ "{\"productCode\":\"rhic-opth-072\",\"productName\":\"Cataract surgery\",\"productType\":\"Act\","
						+ "\"tariffPrice\":250000},{\"productCode\":null},{\"productCode\":\"RHIC-OPTH-072\"}]}}"));
		Assert.assertEquals(1, products.size());
		Assert.assertEquals("RHIC-OPTH-072", products.get(0).code);
		Assert.assertEquals("Cataract surgery", products.get(0).name);
		Assert.assertEquals(0, new BigDecimal("250000").compareTo(products.get(0).tariff));
	}

	@Test
	public void readProducts_shouldBeNullForAFailedAnswerAndEmptyForAnEmptyList() throws Exception {
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		Assert.assertNull(RhipApprovalRequiredProducts.readProducts(mapper.readTree("{\"success\":false,\"data\":null}")));
		Assert.assertTrue(RhipApprovalRequiredProducts.readProducts(
				mapper.readTree("{\"success\":true,\"data\":{\"products\":[]}}")).isEmpty());
	}

	@Test
	public void getLatestApprovalBlockedCodes_shouldListOnlyLinesStillWaitingForApproval() {
		RecordingBillingService billing = new RecordingBillingService();
		RhipVoucherItemRecord blocked = record(1, RhipVoucherItemRecord.STATUS_REJECTED, 1000L);
		blocked.setProductCode("RHIC-OPTH-072");
		blocked.setRejectionReason("Needs MMI approval first: Requires approval");
		RhipVoucherItemRecord unknownCode = record(2, RhipVoucherItemRecord.STATUS_REJECTED, 1000L);
		unknownCode.setProductCode("RHIC-X");
		unknownCode.setRejectionReason("MMI does not recognise this code");
		RhipVoucherItemRecord blockedThenSent = record(3, RhipVoucherItemRecord.STATUS_REJECTED, 1000L);
		blockedThenSent.setProductCode("RHIC-ANES-001");
		blockedThenSent.setRejectionReason("Needs MMI approval first");
		RhipVoucherItemRecord sent = record(3, RhipVoucherItemRecord.STATUS_SENT, 2000L);
		sent.setProductCode("RHIC-ANES-001");
		billing.existingRecords.addAll(Arrays.asList(blocked, unknownCode, blockedThenSent, sent));
		RhipVoucherService service = new RhipVoucherService();
		service.setBillingService(billing.proxy());

		Assert.assertEquals(Collections.singleton("RHIC-OPTH-072"), service.getLatestApprovalBlockedCodes(mmiGlobalBill()));
	}

	@Test
	public void resubmitMmiRejectedLines_shouldRefuseWhenNothingWasRejected() {
		RecordingBillingService billing = new RecordingBillingService();
		billing.existingRecords.add(record(1, RhipVoucherItemRecord.STATUS_SENT, 1000L));
		FixedProvider provider = new FixedProvider("{\"success\":true}");
		ThreeLineService service = new ThreeLineService();
		service.setConfig(new EnabledConfig());
		service.setVoucherProvider(provider);
		service.setBillingService(billing.proxy());

		RhipVoucherSubmission submission = service.resubmitMmiRejectedLines(mmiGlobalBill());

		Assert.assertEquals(RhipVoucherSubmission.STATUS_FAILED, submission.getStatus());
		Assert.assertEquals("No rejected MMI line to resend", submission.getErrorMessage());
		Assert.assertNull(provider.lastRequest);
	}

	@Test
	public void deliveryReference_shouldComeFromTheDeliveryThatBroughtTheDispensedBatch() {
		ScriptedResolver resolver = new ScriptedResolver();
		resolver.recordedAtMmi = true;
		Assert.assertEquals("GRN-2026-041", resolver.resolveMmiDeliveryReference(null, consommation(), 7));
		Assert.assertTrue(resolver.lastBatchLookup.contains("drl.item_id = 7"));
		Assert.assertTrue(resolver.lastReceiptLookup.contains("l.batch_number = 'B-001'"));
		Assert.assertTrue(resolver.lastLogLookup.contains("'%\"referenceNumber\":\"GRN-2026-041\"%'"));
	}

	@Test
	public void deliveryReference_shouldBeLeftOutWhenMmiDidNotRecordTheDelivery() {
		ScriptedResolver resolver = new ScriptedResolver();
		resolver.recordedAtMmi = false;
		Assert.assertNull(resolver.resolveMmiDeliveryReference(null, consommation(), 7));
	}

	@Test
	public void deliveryReference_shouldBeLeftOutWhenTheDispenseHasNoBatch() {
		ScriptedResolver resolver = new ScriptedResolver();
		resolver.batch = null;
		resolver.recordedAtMmi = true;
		Assert.assertNull(resolver.resolveMmiDeliveryReference(null, consommation(), 7));
	}

	private static final String CLOSURE_PREVIEW = "{\"success\":true,"
			+ "\"message\":\"Payment preview calculated; nothing billed yet\",\"insuranceType\":\"mmi\","
			+ "\"data\":{\"verified\":false,\"verificationType\":\"PAYMENT_PREVIEW\",\"billStatus\":\"Pending\","
			+ "\"costSplit\":{\"totalAmount\":256200,\"insuranceAmount\":217770,\"patientAmount\":38430,"
			+ "\"otherAmount\":0,\"insurancePercentage\":85.0,\"patientPercentage\":15.0,\"otherPercentage\":0.0},"
			+ "\"lines\":["
			+ "{\"code\":\"RHIC-CTHS-020\",\"name\":\"Consultation\",\"lineTotal\":5000,\"priceSource\":\"Tariff\"},"
			+ "{\"code\":\"RHIC-OPTH-072\",\"name\":\"Cataract surgery\",\"lineTotal\":250000,"
			+ "\"priceSource\":\"Approved price\",\"fromApproval\":true}]}}";

	@Test
	public void closurePreview_shouldReadTheSplitAndLines() {
		MmiClosurePreview preview = new MmiClosurePreview();
		new RhipVoucherService().readClosurePreview(preview, response(CLOSURE_PREVIEW));

		Assert.assertTrue(preview.isSuccess());
		Assert.assertEquals("Pending", preview.getBillStatus());
		Assert.assertEquals(new BigDecimal("256200"), preview.getTotalAmount());
		Assert.assertEquals(new BigDecimal("217770"), preview.getInsuranceAmount());
		Assert.assertEquals(new BigDecimal("38430"), preview.getPatientAmount());
		Assert.assertEquals(new BigDecimal("85.0"), preview.getInsurancePercentage());
		Assert.assertEquals(2, preview.getLines().size());
		Assert.assertFalse(preview.getLines().get(0).isFromApproval());
		Assert.assertTrue(preview.getLines().get(1).isFromApproval());
		Assert.assertEquals("Approved price", preview.getLines().get(1).getPriceSource());
	}

	@Test
	public void closurePreview_shouldCarryMmiRefusal() {
		IntegrationResponse refused = response("{\"success\":false,\"message\":\"Bill HF-1-87 is already closed\",\"data\":null}");
		refused.setResponseCode(400);
		MmiClosurePreview preview = new MmiClosurePreview();
		new RhipVoucherService().readClosurePreview(preview, refused);

		Assert.assertFalse(preview.isSuccess());
		Assert.assertEquals("Bill HF-1-87 is already closed", preview.getMessage());
	}

	@Test
	public void previewVoucherClosureForGlobalBill_shouldSendPreviewAndCountRejectedLines() {
		RecordingBillingService billing = new RecordingBillingService();
		billing.existingRecords.add(record(2, RhipVoucherItemRecord.STATUS_REJECTED, 1000L));
		CapturingProvider provider = new CapturingProvider(CLOSURE_PREVIEW);
		provider.setConfig(new EnabledConfig());
		RhipVoucherService service = new RhipVoucherService();
		service.setConfig(new EnabledConfig());
		service.setVoucherProvider(provider);
		service.setBillingService(billing.proxy());
		GlobalBill globalBill = mmiGlobalBill();
		globalBill.setRhipVoucherCode("HF-1-87");

		MmiClosurePreview preview = service.previewVoucherClosureForGlobalBill(globalBill);

		Assert.assertTrue(preview.isSuccess());
		Assert.assertEquals("HF-1-87", preview.getVoucherCode());
		Assert.assertEquals(1, preview.getRejectedLineCount());
		Assert.assertEquals("VOUCHER_CLOSURE_PREVIEW", provider.operationType);
		Assert.assertTrue(provider.payload.contains("\"voucherCode\":\"HF-1-87\""));
		Assert.assertTrue(provider.payload.contains("\"preview\":true"));
		Assert.assertFalse("A preview does not verify anything", provider.payload.contains("verifiedBy"));
	}

	@Test
	public void previewVoucherClosureForGlobalBill_shouldRefuseWithoutVoucher() {
		RecordingBillingService billing = new RecordingBillingService();
		CapturingProvider provider = new CapturingProvider(CLOSURE_PREVIEW);
		RhipVoucherService service = new RhipVoucherService();
		service.setVoucherProvider(provider);
		service.setBillingService(billing.proxy());

		MmiClosurePreview preview = service.previewVoucherClosureForGlobalBill(mmiGlobalBill());

		Assert.assertFalse(preview.isSuccess());
		Assert.assertEquals("RHIP voucher must be created before confirmation", preview.getMessage());
		Assert.assertNull(provider.payload);
	}

	@Test
	public void closure_shouldFlagLinesMmiLeftOpen() {
		RhipVoucherService service = new RhipVoucherService();
		String partial = "{\"success\":true,\"message\":\"2 procedure(s) billed.\",\"data\":{\"verified\":true,"
				+ "\"billStatus\":\"Partially paid\",\"remainingUnbilled\":[{\"code\":\"J01CA04001\","
				+ "\"name\":\"Amoxicillin 500mg capsule\"}]}}";
		Assert.assertEquals("MMI only partly settled the bill (Partially paid); still open at MMI: J01CA04001 "
				+ "Amoxicillin 500mg capsule", service.describeMmiClosure(response(partial)));
		String paid = "{\"success\":true,\"data\":{\"verified\":true,\"billStatus\":\"Paid\"}}";
		Assert.assertNull(service.describeMmiClosure(response(paid)));
	}

	@Test
	public void visitClinicianLicense_shouldComeFromTheLatestEncounterWithALicensedProvider() {
		org.openmrs.ProviderAttributeType licenseType = new org.openmrs.ProviderAttributeType();
		licenseType.setName(RhipVoucherService.PROVIDER_LICENSE_ATTRIBUTE_TYPE_NAME);
		org.openmrs.Visit visit = new org.openmrs.Visit();
		visit.addEncounter(encounter(new Date(1000L), provider(licenseType, "RW-MD-OLD")));
		visit.addEncounter(encounter(new Date(3000L), provider(licenseType, null)));
		visit.addEncounter(encounter(new Date(2000L), provider(licenseType, "RW-MD-416806")));
		org.openmrs.module.mohbilling.model.Admission admission = new org.openmrs.module.mohbilling.model.Admission();
		admission.setVisit(visit);
		RhipVoucherService service = new RhipVoucherService();
		service.setProviderService(providerService(licenseType));

		Assert.assertEquals("RW-MD-416806", service.resolveVisitClinicianLicense(admission));
		Assert.assertNull(service.resolveVisitClinicianLicense(new org.openmrs.module.mohbilling.model.Admission()));
	}

	private static org.openmrs.Encounter encounter(Date date, org.openmrs.Provider provider) {
		org.openmrs.Encounter encounter = new org.openmrs.Encounter();
		encounter.setEncounterDatetime(date);
		org.openmrs.EncounterProvider encounterProvider = new org.openmrs.EncounterProvider();
		encounterProvider.setEncounter(encounter);
		encounterProvider.setEncounterRole(new org.openmrs.EncounterRole());
		encounterProvider.setProvider(provider);
		encounter.setEncounterProviders(new java.util.HashSet<org.openmrs.EncounterProvider>(
				Collections.singletonList(encounterProvider)));
		return encounter;
	}

	private static org.openmrs.Provider provider(org.openmrs.ProviderAttributeType licenseType, String license) {
		org.openmrs.Provider provider = new org.openmrs.Provider();
		if (license != null) {
			org.openmrs.ProviderAttribute attribute = new org.openmrs.ProviderAttribute();
			attribute.setAttributeType(licenseType);
			attribute.setValueReferenceInternal(license);
			provider.addAttribute(attribute);
		}
		return provider;
	}

	private static org.openmrs.api.ProviderService providerService(final org.openmrs.ProviderAttributeType licenseType) {
		return (org.openmrs.api.ProviderService) Proxy.newProxyInstance(org.openmrs.api.ProviderService.class.getClassLoader(),
				new Class[] { org.openmrs.api.ProviderService.class }, new java.lang.reflect.InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if ("getAllProviderAttributeTypes".equals(method.getName())) {
							return Collections.singletonList(licenseType);
						}
						return null;
					}
				});
	}

	private static class CapturingProvider extends RhipVoucherProvider {

		private final String body;

		private String payload;

		private String operationType;

		private CapturingProvider(String body) {
			this.body = body;
		}

		@Override
		protected IntegrationResponse executePost(String url, String payload, String operationType, String senderUsername) {
			this.payload = payload;
			this.operationType = operationType;
			return response(body);
		}
	}

	private static RhipVoucherRequest mmiRequest() {
		RhipVoucherRequest request = new RhipVoucherRequest();
		request.setInsuranceType("MMI");
		request.setFacilityFosaId("10023");
		request.setPatientIdentifier("1212222-22");
		request.setPractitionerLicenseNumber("RW-MD-416806");
		request.setVisitReferenceNumber("REC-001");
		request.setDiagnosisIds(Collections.singletonList("CA40.Z"));
		return request;
	}

	private static RhipVoucherProcedure procedure(String code, boolean medicine, BigDecimal price) {
		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode(code);
		procedure.setQuantity(BigDecimal.ONE);
		procedure.setPrice(price);
		procedure.setMedicineLine(medicine);
		if (medicine) {
			procedure.setInstructions("as directed");
		}
		return procedure;
	}

	private static IntegrationResponse response(String body) {
		IntegrationResponse response = new IntegrationResponse();
		response.setEnabled(true);
		response.setEndpointAccessible(true);
		response.setResponseCode(200);
		response.setResponseEntity(body);
		return response;
	}

	private static GlobalBill mmiGlobalBill() {
		Insurance insurance = new Insurance();
		insurance.setName("MMI");
		insurance.setCategory("MMI");
		GlobalBill globalBill = new GlobalBill();
		globalBill.setGlobalBillId(10);
		globalBill.setInsurance(insurance);
		globalBill.setClosed(true);
		return globalBill;
	}

	private static RhipVoucherItemRecord record(Integer itemId, String status, long time) {
		RhipVoucherItemRecord record = new RhipVoucherItemRecord();
		PatientServiceBill item = new PatientServiceBill();
		item.setPatientServiceBillId(itemId);
		record.setPatientServiceBill(item);
		record.setStatus(status);
		record.setDateCreated(new Date(time));
		return record;
	}

	private static Consommation consommation() {
		PatientBill patientBill = new PatientBill();
		patientBill.setPatientBillId(55);
		Consommation consommation = new Consommation();
		consommation.setPatientBill(patientBill);
		return consommation;
	}

	/**
	 * A global bill with a consultation (item 1), a procedure (item 2) and a medicine (item 3)
	 */
	private static class ThreeLineService extends RhipVoucherService {

		@Override
		public RhipVoucherRequest buildVoucherRequestFromGlobalBill(GlobalBill globalBill, String eligibilityIdentifier) {
			RhipVoucherRequest request = mmiRequest();
			RhipVoucherProcedure consultation = procedure("RHIC-CTHS-020", false, BigDecimal.TEN);
			consultation.setPatientServiceBillId(1);
			RhipVoucherProcedure surgery = procedure("RHIC-OPTH-072", false, BigDecimal.TEN);
			surgery.setPatientServiceBillId(2);
			RhipVoucherProcedure medicine = procedure("J01CA04001", true, BigDecimal.TEN);
			medicine.setPatientServiceBillId(3);
			request.setProcedures(new ArrayList<RhipVoucherProcedure>(Arrays.asList(consultation, surgery, medicine)));
			return request;
		}
	}

	private static class ScriptedResolver extends RpmVoucherItemResolver {

		private String batch = "B-001";

		private boolean recordedAtMmi;

		private String lastBatchLookup = "";

		private String lastReceiptLookup = "";

		private String lastLogLookup = "";

		@Override
		protected List<List<Object>> executeSql(String sql) {
			if (sql.contains("information_schema.tables")) {
				return rows(new Object[] { "table" });
			}
			if (sql.startsWith("select drl.batch_number")) {
				lastBatchLookup = sql;
				return batch == null ? rows() : rows(new Object[] { batch });
			}
			if (sql.startsWith("select r.receipt_number")) {
				lastReceiptLookup = sql;
				return rows(new Object[] { "GRN-2026-041", 3 });
			}
			if (sql.contains("from rpm_integration_log")) {
				lastLogLookup = sql;
				return rows(new Object[] { recordedAtMmi ? 1 : 0 });
			}
			return rows();
		}

		private static List<List<Object>> rows(Object[]... rows) {
			List<List<Object>> ret = new ArrayList<List<Object>>();
			for (Object[] row : rows) {
				ret.add(Arrays.asList(row));
			}
			return ret;
		}
	}

	private static class EnabledConfig extends RhipVoucherIntegrationConfig {

		@Override
		public String getVoucherUrl() {
			return "http://rhip.example/insurance_integration/api/v2/voucher";
		}

		@Override
		public String getVoucherClosureUrl() {
			return "http://rhip.example/insurance_integration/api/v2/voucher-closure";
		}

		@Override
		public String getDefaultFosaId() {
			return "10023";
		}

		@Override
		public boolean isVoucherEnabled() {
			return true;
		}
	}

	private static class FixedProvider extends RhipVoucherProvider {

		private final String body;

		private RhipVoucherRequest lastRequest;

		private FixedProvider(String body) {
			this.body = body;
		}

		@Override
		public IntegrationResponse submitVoucher(RhipVoucherRequest request) {
			lastRequest = request;
			return response(body);
		}
	}

	private static class RecordingBillingService implements java.lang.reflect.InvocationHandler {

		private final List<RhipVoucherItemRecord> existingRecords = new ArrayList<RhipVoucherItemRecord>();

		private final List<RhipVoucherItemRecord> savedRecords = new ArrayList<RhipVoucherItemRecord>();

		private BillingService proxy() {
			return (BillingService) Proxy.newProxyInstance(BillingService.class.getClassLoader(),
					new Class[] { BillingService.class }, this);
		}

		private RhipVoucherItemRecord recordFor(Integer itemId) {
			RhipVoucherItemRecord ret = null;
			for (RhipVoucherItemRecord record : savedRecords) {
				if (itemId.equals(record.getPatientServiceBill().getPatientServiceBillId())) {
					ret = record;
				}
			}
			return ret;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			String name = method.getName();
			if ("saveRhipVoucherItemRecord".equals(name)) {
				savedRecords.add((RhipVoucherItemRecord) args[0]);
				return args[0];
			}
			if ("getRhipVoucherItemRecordsByGlobalBill".equals(name)) {
				return existingRecords;
			}
			if ("saveRhipVoucherSubmission".equals(name)) {
				return args[0];
			}
			if ("getRhipVoucherSubmissionsByGlobalBill".equals(name)) {
				return Collections.emptyList();
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
}
