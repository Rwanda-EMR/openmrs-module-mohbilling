package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mohbilling.model.BillReportItem;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Verifies ETL display mapping preserves BillReportUtil field names/rules expected by the UI/Excel.
 */
public class BillReportEtlParityTest {

	@Test
	public void toDisplayRowFromEtl_shouldExposeSameKeysAsLiveBillReportUi() {
		BillReportItem item = sampleItem();

		Map<String, Object> row = BillReportUtil.toDisplayRowFromEtl(item);

		Assert.assertEquals(item.getAdmissionDate(), row.get("admissionDate"));
		Assert.assertEquals(item.getDischargeDate(), row.get("dischargeDate"));
		Assert.assertEquals("Inpatient", row.get("admissionStatus"));
		Assert.assertEquals("P-100", row.get("patientId"));
		Assert.assertEquals("Jane Doe", row.get("patientNames"));
		Assert.assertEquals("1199988776655443322", row.get("patientNid"));
		Assert.assertEquals("HH-NID", row.get("householdNid"));
		Assert.assertEquals("Fully Sponsored", row.get("socialRegistryLevel"));
		Assert.assertEquals("0788000000", row.get("patientPhone"));
		Assert.assertEquals("F", row.get("patientGender"));
		Assert.assertEquals("CBHI", row.get("insuranceName"));
		Assert.assertEquals("CARD-1", row.get("cardNumber"));
		Assert.assertEquals("VCH-9", row.get("voucherId"));
		Assert.assertEquals("Dr Provider", row.get("providerName"));
		Assert.assertEquals("GP", row.get("providerQualification"));
		Assert.assertEquals("0788111111", row.get("providerPhone"));
		Assert.assertEquals("REG-1", row.get("providerRegistration"));
		Assert.assertEquals("Malaria", row.get("primaryDiagnosis"));
		Assert.assertEquals("Yes", row.get("confirmedByLabImaging"));
		Assert.assertEquals("1A00", row.get("icd11Code"));
		Assert.assertEquals("Paracetamol", row.get("productName"));
		Assert.assertEquals("RHIC-MED-001", row.get("productCode"));
		Assert.assertEquals("MEDICAMENTS", row.get("productType"));
		Assert.assertEquals("Item", row.get("productUnit"));
		Assert.assertEquals(new BigDecimal("2.0000"), row.get("productQuantity"));
		Assert.assertEquals(new BigDecimal("100.00"), row.get("productCost"));
		Assert.assertEquals(new BigDecimal("0.00"), row.get("coPayment"));
		Assert.assertEquals(new BigDecimal("200.00"), row.get("rssbPayment"));
		Assert.assertEquals(new BigDecimal("200.00"), row.get("totalInvoice"));
		Assert.assertEquals("Yes", row.get("transferAvailable"));
		Assert.assertEquals("TR-1", row.get("transferId"));
		Assert.assertEquals("2026-01-01", row.get("transferValidity"));
		Assert.assertNotNull(row.get("admission"));
	}

	@Test
	public void sampleEtlItem_shouldCarryGrainKeysUsedByMaterializedTable() {
		BillReportItem item = sampleItem();

		Assert.assertEquals(Integer.valueOf(55), item.getPatientServiceBillId());
		Assert.assertEquals(Integer.valueOf(10), item.getAdmissionId());
		Assert.assertEquals(Integer.valueOf(20), item.getVisitId());
		Assert.assertEquals(Integer.valueOf(30), item.getGlobalBillId());
		Assert.assertEquals(Integer.valueOf(40), item.getConsommationId());
		Assert.assertEquals(Integer.valueOf(7), item.getInsuranceId());
		Assert.assertEquals("MUTUELLE", item.getInsuranceCategory());
	}

	private static BillReportItem sampleItem() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2026, Calendar.JANUARY, 15, 9, 30, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date admissionDate = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, 2);
		Date dischargeDate = calendar.getTime();
		calendar.set(1990, Calendar.MAY, 1, 0, 0, 0);
		Date dob = calendar.getTime();

		BillReportItem item = new BillReportItem();
		item.setPatientServiceBillId(55);
		item.setAdmissionId(10);
		item.setVisitId(20);
		item.setGlobalBillId(30);
		item.setConsommationId(40);
		item.setAdmissionDate(admissionDate);
		item.setDischargeDate(dischargeDate);
		item.setAdmissionStatus("Inpatient");
		item.setWardType("OPD");
		item.setInsuranceId(7);
		item.setInsuranceCategory("MUTUELLE");
		item.setInsuranceName("CBHI");
		item.setCardNumber("CARD-1");
		item.setVoucherId("VCH-9");
		item.setPatientId("P-100");
		item.setPatientNames("Jane Doe");
		item.setPatientNid("1199988776655443322");
		item.setHouseholdHeadNid("HH-NID");
		item.setPatientPhoneNumber("0788000000");
		item.setPatientDob(dob);
		item.setPatientGender("F");
		item.setSocialRegistryLevel("Fully Sponsored");
		item.setProviderName("Dr Provider");
		item.setProviderQualification("GP");
		item.setProviderPhoneNumber("0788111111");
		item.setProviderRegistrationNumber("REG-1");
		item.setPrimaryWorkingDiagnosis("Malaria");
		item.setPrimaryConfirmedByLabImaging("Yes");
		item.setIcd11Code("1A00");
		item.setProductName("Paracetamol");
		item.setProductCode("RHIC-MED-001");
		item.setProductType("MEDICAMENTS");
		item.setProductCategory("MEDICAMENTS");
		item.setProductUnit("Item");
		item.setProductQuantity(new BigDecimal("2.0000"));
		item.setProductCost(new BigDecimal("100.00"));
		item.setCoPayment(new BigDecimal("0.00"));
		item.setRssbPayment(new BigDecimal("200.00"));
		item.setTotalInvoice(new BigDecimal("200.00"));
		item.setTransferAvailable("Yes");
		item.setTransferId("TR-1");
		item.setTransferValidity("2026-01-01");
		return item;
	}
}
