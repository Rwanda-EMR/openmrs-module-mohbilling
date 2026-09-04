package org.openmrs.module.mohbilling.businesslogic;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.InsuranceReportItem;
import org.openmrs.module.mohbilling.model.PaidServiceRevenue;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PaymentRevenue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

public class FileExporterTest {

	@Test
	public void writeCashierReportWorkbook_shouldCreateXlsxWithDynamicServicesAndTotals() throws Exception {
		PaidServiceRevenue service = new PaidServiceRevenue();
		service.setService("Laboratory & Tests");
		service.setPaidAmount(new BigDecimal("50.25"));
		BillPayment payment = new BillPayment();
		payment.setAmountPaid(new BigDecimal("75.50"));
		PatientBill patientBill = new PatientBill();
		patientBill.setTransactionStatus("SUCCESSFUL");
		patientBill.setPhoneNumber("0788000000");
		payment.setPatientBill(patientBill);
		Patient patient = new Patient();
		PersonName patientName = new PersonName("Test", null, "Patient");
		patientName.setPreferred(true);
		patient.addName(patientName);
		Beneficiary beneficiary = new Beneficiary();
		beneficiary.setPatient(patient);
		PaymentRevenue paymentRevenue = new PaymentRevenue();
		paymentRevenue.setPayment(payment);
		paymentRevenue.setBeneficiary(beneficiary);
		paymentRevenue.setPaidServiceRevenues(Collections.singletonList(service));
		paymentRevenue.setAmount(new BigDecimal("50.25"));

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		FileExporter.writeCashierReportWorkbook(outputStream, Collections.singletonList(paymentRevenue),
				Collections.singletonList(new BigDecimal("50.25")), new BigDecimal("50.25"),
				new BigDecimal("75.50"), "Revenue Amount From 01/09/2026 To 02/09/2026",
				"Test Cashier", "Test Facility");

		byte[] xlsx = outputStream.toByteArray();
		Assert.assertTrue(xlsx.length > 0);
		Assert.assertEquals('P', xlsx[0]);
		Assert.assertEquals('K', xlsx[1]);
		Map<String, String> entries = unzipTextEntries(xlsx);
		String sheet = entries.get("xl/worksheets/sheet1.xml");
		Assert.assertTrue(sheet.contains("Laboratory &amp; Tests"));
		Assert.assertTrue(sheet.contains("<c r=\"D7\" s=\"3\"><v>50.25</v></c>"));
		Assert.assertTrue(sheet.contains("<c r=\"G7\" t=\"inlineStr\" s=\"1\"><is><t xml:space=\"preserve\">0788000000"));
		Assert.assertTrue(sheet.contains("<c r=\"D8\" s=\"4\"><v>50.25</v></c>"));
		Assert.assertTrue(entries.get("xl/workbook.xml").contains("name=\"Cashier Report\""));
		assertXmlEntriesAreWellFormed(entries);
	}

	@Test
	public void writeInsuranceReportWorkbook_shouldCreateReadableXlsxWithData() throws Exception {
		InsuranceReportItem reportItem = new InsuranceReportItem();
		reportItem.setBeneficiaryName("Test Patient");
		reportItem.setHouseholdHeadName("Test Household");
		reportItem.setFamilyCode("0012345678901234");
		reportItem.setBeneficiaryLevel(1);
		reportItem.setGlobalBillIdentifier("GB-1");
		reportItem.setCardNumber("0098765432101234");
		reportItem.setCompanyName("Test Company");
		reportItem.setInsuranceName("Test Insurance");
		reportItem.setAge(30);
		reportItem.setGender("F");
		reportItem.setPatientIdentifier("0000123456789012");
		reportItem.setDoctorName("Test Doctor");
		reportItem.setConsultation(100.0);
		reportItem.setLaboratoire(200.0);
		reportItem.setHospitalisation(0.0);
		reportItem.setFormaliteAdministratives(0.0);
		reportItem.setAmbulance(0.0);
		reportItem.setConsommables(0.0);
		reportItem.setMedicament(0.0);
		reportItem.setOxygenotherapie(0.0);
		reportItem.setAutres(0.0);
		reportItem.setImaging(0.0);
		reportItem.setProced(0.0);
		reportItem.setTotal100(300.0);
		reportItem.setTotalInsurance(270.0);
		reportItem.setTotalPatient(30.0);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		FileExporter.writeInsuranceReportWorkbook(outputStream, null,
				Collections.nCopies(250, reportItem), "Test Facility", "Kigali", "test@example.org");

		byte[] xlsx = outputStream.toByteArray();
		Assert.assertTrue("The generated XLSX must contain data", xlsx.length > 0);
		Assert.assertEquals('P', xlsx[0]);
		Assert.assertEquals('K', xlsx[1]);

		Map<String, String> entries = unzipTextEntries(xlsx);
		Assert.assertTrue(entries.containsKey("[Content_Types].xml"));
		Assert.assertTrue(entries.containsKey("xl/workbook.xml"));
		Assert.assertTrue(entries.containsKey("xl/styles.xml"));
		Assert.assertTrue(entries.containsKey("xl/worksheets/sheet1.xml"));

		String sheet = entries.get("xl/worksheets/sheet1.xml");
		Assert.assertTrue(sheet.contains("<c r=\"O9\" t=\"inlineStr\"><is><t xml:space=\"preserve\">IDENTIFIER</t>"));
		Assert.assertTrue(sheet.contains("<c r=\"D10\" t=\"inlineStr\"><is><t xml:space=\"preserve\">Test Patient</t>"));
		Assert.assertTrue(sheet.contains("<c r=\"O10\" t=\"inlineStr\" s=\"1\"><is><t xml:space=\"preserve\">0000123456789012</t>"));
		Assert.assertTrue(sheet.contains("<c r=\"D259\" t=\"inlineStr\"><is><t xml:space=\"preserve\">Test Patient</t>"));
		Assert.assertFalse(sheet.contains("<f>"));
		Assert.assertTrue(entries.get("xl/styles.xml").contains("numFmtId=\"49\""));
	}

	private static Map<String, String> unzipTextEntries(byte[] xlsx) throws Exception {
		Map<String, String> entries = new HashMap<String, String>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(xlsx))) {
			ZipEntry entry;
			byte[] buffer = new byte[4096];
			while ((entry = zip.getNextEntry()) != null) {
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				int read;
				while ((read = zip.read(buffer)) != -1) {
					content.write(buffer, 0, read);
				}
				entries.put(entry.getName(), new String(content.toByteArray(), StandardCharsets.UTF_8));
			}
		}
		return entries;
	}

	private static void assertXmlEntriesAreWellFormed(Map<String, String> entries) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			if (entry.getKey().endsWith(".xml") || entry.getKey().endsWith(".rels")) {
				factory.newDocumentBuilder().parse(new ByteArrayInputStream(
						entry.getValue().getBytes(StandardCharsets.UTF_8)));
			}
		}
	}
}
