package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceReportItem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streams a minimal XLSX package without depending on the POI/XML libraries loaded by other OpenMRS modules.
 */
final class InsuranceReportXlsxWriter {

	private static final String[] HEADERS = {
			"#", "Admission Date", "Closing Date", "BENEFICIARY'S NAMES", "HEAD HOUSEHOLD'S NAMES",
			"FAMILY'S CODE", "LEVEL", "GB#", "Card NUMBER", "COMPANY", "INSURANCE NAME", "AGE",
			"BIRTH DATE", "GENDER", "IDENTIFIER", "DOCTOR", "CONSULTATION", "LABORATOIRE",
			"HOSPITALISATION", "FORMALITES ADMINISTRATIVES", "AMBULANCE", "CONSOMMABLES",
			"MEDICAMENTS", "OXYGENOTHERAPIE", "AUTRES", "IMAGING", "PROCED.", "Total (100%)",
			"Insurance", "Patient share"
	};

	private InsuranceReportXlsxWriter() {
	}

	static void write(OutputStream outputStream, Insurance insurance, List<InsuranceReportItem> reportRecords,
			String facilityName, String facilityAddress, String facilityEmail) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		writeEntry(zip, "[Content_Types].xml", contentTypesXml());
		writeEntry(zip, "_rels/.rels", packageRelationshipsXml());
		writeEntry(zip, "xl/workbook.xml", workbookXml());
		writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
		writeEntry(zip, "xl/styles.xml", stylesXml());
		writeWorksheet(zip, insurance, reportRecords, facilityName, facilityAddress, facilityEmail);
		zip.finish();
		zip.flush();
	}

	private static void writeWorksheet(ZipOutputStream zip, Insurance insurance,
			List<InsuranceReportItem> reportRecords, String facilityName, String facilityAddress,
			String facilityEmail) throws IOException {
		zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writer.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		writer.write("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"9\" topLeftCell=\"A10\" "
				+ "activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
		writer.write("<cols><col min=\"6\" max=\"6\" width=\"22\" customWidth=\"1\"/>"
				+ "<col min=\"9\" max=\"9\" width=\"22\" customWidth=\"1\"/>"
				+ "<col min=\"15\" max=\"15\" width=\"22\" customWidth=\"1\"/></cols>");
		writer.write("<sheetData>");

		writeTextRow(writer, 1, 0, facilityName, false);
		writeTextRow(writer, 2, 0, facilityAddress, false);
		writeTextRow(writer, 3, 0, facilityEmail, false);
		String insuranceLabel = insurance != null ? insurance.getName() : "ALL INSURANCES";
		writeTextRow(writer, 6, 10, "SUMMARY OF VOUCHERS FOR " + insuranceLabel, false);

		writer.write("<row r=\"9\">");
		for (int column = 0; column < HEADERS.length; column++) {
			writeTextCell(writer, column, 9, HEADERS[column], false);
		}
		writer.write("</row>");

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		for (int i = 0; i < reportRecords.size(); i++) {
			writeDataRow(writer, i + 10, i + 1, reportRecords.get(i), formatter);
		}

		writer.write("</sheetData></worksheet>");
		writer.flush();
		zip.closeEntry();
	}

	private static void writeDataRow(BufferedWriter writer, int rowNumber, int sequence,
			InsuranceReportItem item, DateFormat formatter) throws IOException {
		writer.write("<row r=\"");
		writer.write(Integer.toString(rowNumber));
		writer.write("\">");
		writeNumberCell(writer, 0, rowNumber, Integer.toString(sequence));
		writeTextCell(writer, 1, rowNumber, FileExporter.formatDate(formatter, item.getAdmissionDate()), false);
		writeTextCell(writer, 2, rowNumber, FileExporter.formatDate(formatter, item.getClosingDate()), false);
		writeTextCell(writer, 3, rowNumber, item.getBeneficiaryName(), false);
		writeTextCell(writer, 4, rowNumber, item.getHouseholdHeadName(), false);
		writeTextCell(writer, 5, rowNumber, item.getFamilyCode(), true);
		writeNumberCell(writer, 6, rowNumber, integerValue(item.getBeneficiaryLevel()));
		writeTextCell(writer, 7, rowNumber, item.getGlobalBillIdentifier(), false);
		writeTextCell(writer, 8, rowNumber, item.getCardNumber(), true);
		writeTextCell(writer, 9, rowNumber, item.getCompanyName(), false);
		writeTextCell(writer, 10, rowNumber, item.getInsuranceName(), false);
		writeNumberCell(writer, 11, rowNumber, integerValue(item.getAge()));
		writeTextCell(writer, 12, rowNumber, FileExporter.formatDate(formatter, item.getBirthDate()), false);
		writeTextCell(writer, 13, rowNumber, item.getGender(), false);
		writeTextCell(writer, 14, rowNumber, item.getPatientIdentifier(), true);
		writeTextCell(writer, 15, rowNumber, item.getDoctorName(), false);
		writeNumberCell(writer, 16, rowNumber, decimalValue(item.getConsultation()));
		writeNumberCell(writer, 17, rowNumber, decimalValue(item.getLaboratoire()));
		writeNumberCell(writer, 18, rowNumber, decimalValue(item.getHospitalisation()));
		writeNumberCell(writer, 19, rowNumber, decimalValue(item.getFormaliteAdministratives()));
		writeNumberCell(writer, 20, rowNumber, decimalValue(item.getAmbulance()));
		writeNumberCell(writer, 21, rowNumber, decimalValue(item.getConsommables()));
		writeNumberCell(writer, 22, rowNumber, decimalValue(item.getMedicament()));
		writeNumberCell(writer, 23, rowNumber, decimalValue(item.getOxygenotherapie()));
		writeNumberCell(writer, 24, rowNumber, decimalValue(item.getAutres()));
		writeNumberCell(writer, 25, rowNumber, decimalValue(item.getImaging()));
		writeNumberCell(writer, 26, rowNumber, decimalValue(item.getProced()));
		writeNumberCell(writer, 27, rowNumber, decimalValue(item.getTotal100()));
		writeNumberCell(writer, 28, rowNumber, decimalValue(item.getTotalInsurance()));
		writeNumberCell(writer, 29, rowNumber, decimalValue(item.getTotalPatient()));
		writer.write("</row>");
	}

	private static void writeTextRow(BufferedWriter writer, int rowNumber, int column, String value,
			boolean textStyle) throws IOException {
		writer.write("<row r=\"");
		writer.write(Integer.toString(rowNumber));
		writer.write("\">");
		writeTextCell(writer, column, rowNumber, value, textStyle);
		writer.write("</row>");
	}

	private static void writeTextCell(BufferedWriter writer, int column, int row, String value,
			boolean textStyle) throws IOException {
		writer.write("<c r=\"");
		writer.write(columnName(column));
		writer.write(Integer.toString(row));
		writer.write("\" t=\"inlineStr\"");
		if (textStyle) {
			writer.write(" s=\"1\"");
		}
		writer.write("><is><t xml:space=\"preserve\">");
		writer.write(escapeXml(value));
		writer.write("</t></is></c>");
	}

	private static void writeNumberCell(BufferedWriter writer, int column, int row, String value)
			throws IOException {
		writer.write("<c r=\"");
		writer.write(columnName(column));
		writer.write(Integer.toString(row));
		writer.write("\"><v>");
		writer.write(value);
		writer.write("</v></c>");
	}

	private static String integerValue(Integer value) {
		return Integer.toString(value != null ? value : 0);
	}

	private static String decimalValue(Double value) {
		double safeValue = value != null && !value.isNaN() && !value.isInfinite() ? value : 0d;
		return BigDecimal.valueOf(safeValue).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
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
				+ "<sheets><sheet name=\"Insurance Report\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
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
				+ "<fonts count=\"1\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
				+ "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
				+ "<fill><patternFill patternType=\"gray125\"/></fill></fills>"
				+ "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
				+ "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
				+ "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
				+ "<xf numFmtId=\"49\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/></cellXfs>"
				+ "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
				+ "</styleSheet>";
	}
}
