package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.Patient;
import org.openmrs.module.mohbilling.model.AllServicesRevenue;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.ServiceRevenue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streams the third party report as XLSX without adding an Excel library to the OpenMRS classpath.
 * The amounts are the same as the ones shown on mohBillingThirdPartyReport.jsp.
 */
final class ThirdPartyReportXlsxWriter {

	private static final int HEADER_ROW = 5;

	private static final int FIRST_SERVICE_COLUMN = 9;

	private static final BigDecimal HUNDRED = new BigDecimal(100);

	private ThirdPartyReportXlsxWriter() {
	}

	static void write(OutputStream outputStream, List<AllServicesRevenue> reportRows, List<String> columns,
			Float thirdPartyRate, String reportTitle, String facilityName) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		writeEntry(zip, "[Content_Types].xml", contentTypesXml());
		writeEntry(zip, "_rels/.rels", packageRelationshipsXml());
		writeEntry(zip, "xl/workbook.xml", workbookXml());
		writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
		writeEntry(zip, "xl/styles.xml", stylesXml());
		writeWorksheet(zip, reportRows, columns, thirdPartyRate, reportTitle, facilityName);
		zip.finish();
		zip.flush();
	}

	private static void writeWorksheet(ZipOutputStream zip, List<AllServicesRevenue> reportRows,
			List<String> columns, Float thirdPartyRate, String reportTitle, String facilityName) throws IOException {
		List<AllServicesRevenue> rows = reportRows != null ? reportRows : Collections.<AllServicesRevenue>emptyList();
		List<String> services = columns != null ? columns : Collections.<String>emptyList();
		BigDecimal thirdParty = thirdPartyRate != null ? new BigDecimal(Float.toString(thirdPartyRate))
				: BigDecimal.ZERO;
		int lastColumn = FIRST_SERVICE_COLUMN + services.size() + 3;
		int totalsRow = HEADER_ROW + rows.size() + 1;

		zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writer.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		writer.write("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"" + HEADER_ROW + "\" topLeftCell=\"A"
				+ (HEADER_ROW + 1) + "\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
		writer.write("<cols><col min=\"1\" max=\"1\" width=\"7\" customWidth=\"1\"/>"
				+ "<col min=\"2\" max=\"2\" width=\"12\" customWidth=\"1\"/>"
				+ "<col min=\"3\" max=\"3\" width=\"20\" customWidth=\"1\"/>"
				+ "<col min=\"4\" max=\"5\" width=\"8\" customWidth=\"1\"/>"
				+ "<col min=\"6\" max=\"6\" width=\"32\" customWidth=\"1\"/>"
				+ "<col min=\"7\" max=\"7\" width=\"20\" customWidth=\"1\"/>"
				+ "<col min=\"8\" max=\"9\" width=\"10\" customWidth=\"1\"/>"
				+ "<col min=\"10\" max=\"" + (lastColumn + 1) + "\" width=\"16\" customWidth=\"1\"/></cols>");
		writer.write("<sheetData>");

		writeTextRow(writer, 1, valueOrDefault(facilityName, ""), 6);
		writeTextRow(writer, 2, valueOrDefault(reportTitle, "Third Party Report"), 6);
		writeTextRow(writer, 3, "Third party rate: " + decimalValue(thirdParty) + "%", 0);
		writeHeaderRow(writer, services, thirdParty);

		BigDecimal[] totals = new BigDecimal[services.size() + 4];
		for (int i = 0; i < totals.length; i++) {
			totals[i] = BigDecimal.ZERO;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < rows.size(); i++) {
			writeReportRow(writer, HEADER_ROW + i + 1, i + 1, rows.get(i), services.size(), thirdParty, totals,
					dateFormat);
		}
		writeTotalsRow(writer, totalsRow, totals);

		writer.write("</sheetData><autoFilter ref=\"A" + HEADER_ROW + ":"
				+ columnName(lastColumn) + Math.max(HEADER_ROW, totalsRow - 1) + "\"/></worksheet>");
		writer.flush();
		zip.closeEntry();
	}

	private static void writeHeaderRow(BufferedWriter writer, List<String> services, BigDecimal thirdPartyRate)
			throws IOException {
		writer.write("<row r=\"" + HEADER_ROW + "\">");
		writeTextCell(writer, 0, HEADER_ROW, "#", 2);
		writeTextCell(writer, 1, HEADER_ROW, "Date", 2);
		writeTextCell(writer, 2, HEADER_ROW, "CARD NUMBER", 2);
		writeTextCell(writer, 3, HEADER_ROW, "AGE", 2);
		writeTextCell(writer, 4, HEADER_ROW, "Gender", 2);
		writeTextCell(writer, 5, HEADER_ROW, "BENEFICIARY'S NAMES", 2);
		writeTextCell(writer, 6, HEADER_ROW, "Insurance Name", 2);
		writeTextCell(writer, 7, HEADER_ROW, "Insurance Rate", 2);
		writeTextCell(writer, 8, HEADER_ROW, "Patient Rate", 2);
		int column = FIRST_SERVICE_COLUMN;
		for (String service : services) {
			writeTextCell(writer, column++, HEADER_ROW,
					"FORMALITES ADMINISTRATIVES".equals(service) ? "OTHERCONSUM." : valueOrDefault(service, ""), 2);
		}
		writeTextCell(writer, column++, HEADER_ROW, "100%", 2);
		writeTextCell(writer, column++, HEADER_ROW, "Insurance Amount", 2);
		writeTextCell(writer, column++, HEADER_ROW, "Third Party Amount on: " + decimalValue(thirdPartyRate) + "%", 2);
		writeTextCell(writer, column, HEADER_ROW, "Patient Amount", 2);
		writer.write("</row>");
	}

	private static void writeReportRow(BufferedWriter writer, int rowNumber, int sequence, AllServicesRevenue row,
			int serviceCount, BigDecimal thirdPartyRate, BigDecimal[] totals, DateFormat dateFormat)
			throws IOException {
		Consommation consommation = row != null ? row.getConsommation() : null;
		InsurancePolicy policy = consommation != null && consommation.getBeneficiary() != null
				? consommation.getBeneficiary().getInsurancePolicy() : null;
		Patient patient = consommation != null && consommation.getBeneficiary() != null
				? consommation.getBeneficiary().getPatient() : null;
		BigDecimal insuranceRate = insuranceRate(policy);
		BigDecimal patientRate = HUNDRED.subtract(insuranceRate).subtract(thirdPartyRate);

		writer.write("<row r=\"" + rowNumber + "\">");
		writeNumberCell(writer, 0, rowNumber, Integer.toString(sequence), 5);
		writeTextCell(writer, 1, rowNumber,
				consommation != null ? FileExporter.formatDate(dateFormat, consommation.getCreatedDate()) : "", 5);
		writeTextCell(writer, 2, rowNumber, policy != null ? valueOrDefault(policy.getInsuranceCardNo(), "") : "", 5);
		if (patient != null && patient.getAge() != null) {
			writeNumberCell(writer, 3, rowNumber, patient.getAge().toString(), 5);
		} else {
			writeTextCell(writer, 3, rowNumber, "", 5);
		}
		writeTextCell(writer, 4, rowNumber, patient != null ? valueOrDefault(patient.getGender(), "") : "", 5);
		writeTextCell(writer, 5, rowNumber,
				patient != null && patient.getPersonName() != null ? patient.getPersonName().toString() : "", 5);
		writeTextCell(writer, 6, rowNumber,
				policy != null && policy.getInsurance() != null ? valueOrDefault(policy.getInsurance().getName(), "")
						: "", 5);
		writeNumberCell(writer, 7, rowNumber, decimalValue(insuranceRate), 5);
		writeNumberCell(writer, 8, rowNumber, decimalValue(patientRate), 5);

		List<ServiceRevenue> revenues = row != null && row.getRevenues() != null ? row.getRevenues()
				: Collections.<ServiceRevenue>emptyList();
		int column = FIRST_SERVICE_COLUMN;
		for (int i = 0; i < serviceCount; i++) {
			ServiceRevenue revenue = i < revenues.size() ? revenues.get(i) : null;
			BigDecimal amount = thirdPartyServiceAmount(revenue != null ? revenue.getDueAmount() : null,
					insuranceRate, thirdPartyRate);
			totals[i] = totals[i].add(amount);
			writeNumberCell(writer, column++, rowNumber, decimalValue(amount), 3);
		}

		BigDecimal total = row != null && row.getAllDueAmounts() != null ? row.getAllDueAmounts() : BigDecimal.ZERO;
		BigDecimal[] shares = { amount(total), share(total, insuranceRate), share(total, thirdPartyRate),
				share(total, patientRate) };
		for (int i = 0; i < shares.length; i++) {
			totals[serviceCount + i] = totals[serviceCount + i].add(shares[i]);
			writeNumberCell(writer, column++, rowNumber, decimalValue(shares[i]), 3);
		}
		writer.write("</row>");
	}

	private static void writeTotalsRow(BufferedWriter writer, int rowNumber, BigDecimal[] totals)
			throws IOException {
		writer.write("<row r=\"" + rowNumber + "\">");
		writeTextCell(writer, 0, rowNumber, "TOTAL", 2);
		for (int column = 1; column < FIRST_SERVICE_COLUMN; column++) {
			writeTextCell(writer, column, rowNumber, "", 2);
		}
		int column = FIRST_SERVICE_COLUMN;
		for (BigDecimal total : totals) {
			writeNumberCell(writer, column++, rowNumber, decimalValue(total), 4);
		}
		writer.write("</row>");
	}

	/**
	 * The service revenue holds the patient part (100 - insurance rate), as on the screen the third party
	 * amount of a service is that part * third party rate / (100 - insurance rate)
	 */
	static BigDecimal thirdPartyServiceAmount(BigDecimal patientDueAmount, BigDecimal insuranceRate,
			BigDecimal thirdPartyRate) {
		BigDecimal notCoveredRate = HUNDRED.subtract(insuranceRate);
		if (patientDueAmount == null || notCoveredRate.signum() == 0) {
			return amount(BigDecimal.ZERO);
		}
		return amount(patientDueAmount.multiply(thirdPartyRate).divide(notCoveredRate, 10, RoundingMode.HALF_UP));
	}

	private static BigDecimal insuranceRate(InsurancePolicy policy) {
		if (policy == null || policy.getInsurance() == null || policy.getInsurance().getRates() == null) {
			return BigDecimal.ZERO;
		}
		InsuranceRate rate = policy.getInsurance().getCurrentRate();
		return rate != null && rate.getRate() != null ? new BigDecimal(Float.toString(rate.getRate()))
				: BigDecimal.ZERO;
	}

	private static BigDecimal share(BigDecimal total, BigDecimal rate) {
		return amount(total.multiply(rate).divide(HUNDRED, 10, RoundingMode.HALF_UP));
	}

	private static BigDecimal amount(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	private static void writeTextRow(BufferedWriter writer, int rowNumber, String value, int style)
			throws IOException {
		writer.write("<row r=\"" + rowNumber + "\">");
		writeTextCell(writer, 0, rowNumber, value, style);
		writer.write("</row>");
	}

	private static void writeTextCell(BufferedWriter writer, int column, int row, String value, int style)
			throws IOException {
		writer.write("<c r=\"" + columnName(column) + row + "\" t=\"inlineStr\"");
		if (style > 0) {
			writer.write(" s=\"" + style + "\"");
		}
		writer.write("><is><t xml:space=\"preserve\">" + escapeXml(value) + "</t></is></c>");
	}

	private static void writeNumberCell(BufferedWriter writer, int column, int row, String value, int style)
			throws IOException {
		writer.write("<c r=\"" + columnName(column) + row + "\" s=\"" + style + "\"><v>"
				+ value + "</v></c>");
	}

	private static String decimalValue(BigDecimal value) {
		return (value != null ? value : BigDecimal.ZERO).stripTrailingZeros().toPlainString();
	}

	private static String valueOrDefault(String value, String fallback) {
		return value != null && !value.trim().isEmpty() ? value : fallback;
	}

	private static String columnName(int zeroBasedColumn) {
		StringBuilder name = new StringBuilder();
		int column = zeroBasedColumn + 1;
		while (column > 0) {
			column--;
			name.insert(0, (char) ('A' + column % 26));
			column /= 26;
		}
		return name.toString();
	}

	private static String escapeXml(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder escaped = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch == '&') {
				escaped.append("&amp;");
			} else if (ch == '<') {
				escaped.append("&lt;");
			} else if (ch == '>') {
				escaped.append("&gt;");
			} else if (ch == '\"') {
				escaped.append("&quot;");
			} else if (ch == '\'') {
				escaped.append("&apos;");
			} else if (ch == '\t' || ch == '\n' || ch == '\r' || ch >= 0x20) {
				escaped.append(ch);
			}
		}
		return escaped.toString();
	}

	private static void writeEntry(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}

	private static String contentTypesXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
				+ "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
				+ "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
				+ "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
				+ "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
				+ "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
				+ "</Types>";
	}

	private static String packageRelationshipsXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
				+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
				+ "</Relationships>";
	}

	private static String workbookXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
				+ "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<sheets><sheet name=\"Third Party Report\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
	}

	private static String workbookRelationshipsXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
				+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
				+ "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
				+ "</Relationships>";
	}

	private static String stylesXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
				+ "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"#,##0.00\"/></numFmts>"
				+ "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
				+ "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
				+ "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill>"
				+ "<fill><patternFill patternType=\"gray125\"/></fill>"
				+ "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9EAD3\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
				+ "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>"
				+ "<border><left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/><diagonal/></border></borders>"
				+ "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
				+ "<cellXfs count=\"7\">"
				+ "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
				+ "<xf numFmtId=\"49\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\"/>"
				+ "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\" wrapText=\"1\"/></xf>"
				+ "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\"/>"
				+ "<xf numFmtId=\"164\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\"/>"
				+ "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\"/>"
				+ "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
				+ "</cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
				+ "</styleSheet>";
	}
}
