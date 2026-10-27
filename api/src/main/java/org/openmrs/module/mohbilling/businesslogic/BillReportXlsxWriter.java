package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.module.mohbilling.model.BillReportFacilityHeader;
import org.openmrs.module.mohbilling.model.BillReportItem;
import org.openmrs.module.mohbilling.model.Insurance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * CBHI Bill Report workbook: facility fields in the header, data rows without those columns.
 */
final class BillReportXlsxWriter {

	private static final String[] DATA_HEADERS = {
			"No",
			"Date of admission",
			"Date of discharge",
			"Ward Type",
			"Voucher ID",
			"Patient ID",
			"Patient's names",
			"Social registry level",
			"Patient NID / Application number",
			"NID of the head of household",
			"Patient Phone number",
			"Patient's DoB (dd/mm/yyyy)",
			"Patient's Gender",
			"Name of service provider",
			"Provider's Qualification",
			"Provider's phone number",
			"Registration number of health practitioner",
			"Primary Working Diagnosis",
			"Primary confirmed diagnosis by lab/imaging (Yes or No)",
			"ICD-11 Code",
			"Product name",
			"product_code",
			"product_type",
			"product_category",
			"product_unit",
			"Quantity",
			"product_cost",
			"Co-Payment",
			"RSSB payment (Total - Copayment)",
			"TOTAL invoice (100%)",
			"Transfer",
			"Validity Period Start and end date"
	};

	private BillReportXlsxWriter() {
	}

	static void write(OutputStream outputStream, Insurance insurance, BillReportFacilityHeader facilityHeader,
			List<BillReportItem> rows, Date startDate, Date endDate) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		writeEntry(zip, "[Content_Types].xml", contentTypesXml());
		writeEntry(zip, "_rels/.rels", packageRelationshipsXml());
		writeEntry(zip, "xl/workbook.xml", workbookXml());
		writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
		writeEntry(zip, "xl/styles.xml", stylesXml());
		writeWorksheet(zip, insurance, facilityHeader, rows, startDate, endDate);
		zip.finish();
		zip.flush();
	}

	private static void writeWorksheet(ZipOutputStream zip, Insurance insurance,
			BillReportFacilityHeader facilityHeader, List<BillReportItem> rows, Date startDate, Date endDate)
			throws IOException {
		zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writer.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		writer.write("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"13\" topLeftCell=\"A14\" "
				+ "activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
		writer.write("<sheetData>");

		DateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
		String insuranceLabel = insurance != null && insurance.getName() != null ? insurance.getName()
				: "ALL INSURANCES";
		String period = formatPeriod(dayFormat, startDate, endDate);

		writeTextRow(writer, 1, 0, "Hospital inpatient/outpatient — Bill Report");
		writeLabelValueRow(writer, 2, "Insurance", insuranceLabel);
		writeLabelValueRow(writer, 3, "Period", period);
		writeLabelValueRow(writer, 4, "HF CODE", value(facilityHeader != null ? facilityHeader.getHfCode() : null));
		writeLabelValueRow(writer, 5, "Facility",
				value(facilityHeader != null ? facilityHeader.getFacilityName() : null));
		writeLabelValueRow(writer, 6, "Type",
				value(facilityHeader != null ? facilityHeader.getFacilityType() : null));
		writeLabelValueRow(writer, 7, "Category",
				value(facilityHeader != null ? facilityHeader.getFacilityCategory() : null));
		writeLabelValueRow(writer, 8, "Level of Care",
				value(facilityHeader != null ? facilityHeader.getFacilityLevelOfCare() : null));
		writeLabelValueRow(writer, 9, "Ownership",
				value(facilityHeader != null ? facilityHeader.getFacilityOwnershipType() : null));
		writeLabelValueRow(writer, 10, "Province",
				value(facilityHeader != null ? facilityHeader.getProvinceName() : null));
		writeLabelValueRow(writer, 11, "District",
				value(facilityHeader != null ? facilityHeader.getDistrictName() : null));
		writeLabelValueRow(writer, 12, "Sector",
				value(facilityHeader != null ? facilityHeader.getSectorName() : null));

		writer.write("<row r=\"13\">");
		for (int column = 0; column < DATA_HEADERS.length; column++) {
			writeTextCell(writer, column, 13, DATA_HEADERS[column], false);
		}
		writer.write("</row>");

		for (int i = 0; i < rows.size(); i++) {
			writeDataRow(writer, i + 14, rows.get(i), dayFormat);
		}

		writer.write("</sheetData></worksheet>");
		writer.flush();
		zip.closeEntry();
	}

	private static void writeDataRow(BufferedWriter writer, int rowNumber, BillReportItem item,
			DateFormat dayFormat) throws IOException {
		writer.write("<row r=\"");
		writer.write(Integer.toString(rowNumber));
		writer.write("\">");
		writeNumberCell(writer, 0, rowNumber, integerValue(item.getRowNumber()));
		writeTextCell(writer, 1, rowNumber, FileExporter.formatDate(dayFormat, item.getAdmissionDate()), false);
		writeTextCell(writer, 2, rowNumber, FileExporter.formatDate(dayFormat, item.getDischargeDate()), false);
		writeTextCell(writer, 3, rowNumber, item.getWardType(), false);
		writeTextCell(writer, 4, rowNumber, item.getVoucherId(), true);
		writeTextCell(writer, 5, rowNumber, item.getPatientId(), true);
		writeTextCell(writer, 6, rowNumber, item.getPatientNames(), false);
		writeTextCell(writer, 7, rowNumber, item.getSocialRegistryLevel(), false);
		writeTextCell(writer, 8, rowNumber, item.getPatientNid(), true);
		writeTextCell(writer, 9, rowNumber, item.getHouseholdHeadNid(), true);
		writeTextCell(writer, 10, rowNumber, item.getPatientPhoneNumber(), true);
		writeTextCell(writer, 11, rowNumber, FileExporter.formatDate(dayFormat, item.getPatientDob()), false);
		writeTextCell(writer, 12, rowNumber, item.getPatientGender(), false);
		writeTextCell(writer, 13, rowNumber, item.getProviderName(), false);
		writeTextCell(writer, 14, rowNumber, item.getProviderQualification(), false);
		writeTextCell(writer, 15, rowNumber, item.getProviderPhoneNumber(), false);
		writeTextCell(writer, 16, rowNumber, item.getProviderRegistrationNumber(), false);
		writeTextCell(writer, 17, rowNumber, item.getPrimaryWorkingDiagnosis(), false);
		writeTextCell(writer, 18, rowNumber, item.getPrimaryConfirmedByLabImaging(), false);
		writeTextCell(writer, 19, rowNumber, item.getIcd11Code(), false);
		writeTextCell(writer, 20, rowNumber, item.getProductName(), false);
		writeTextCell(writer, 21, rowNumber, item.getProductCode(), true);
		writeTextCell(writer, 22, rowNumber, item.getProductType(), false);
		writeTextCell(writer, 23, rowNumber, item.getProductCategory(), false);
		writeTextCell(writer, 24, rowNumber, item.getProductUnit(), false);
		writeNumberCell(writer, 25, rowNumber, decimalValue(item.getProductQuantity()));
		writeNumberCell(writer, 26, rowNumber, decimalValue(item.getProductCost()));
		writeNumberCell(writer, 27, rowNumber, decimalValue(item.getCoPayment()));
		writeNumberCell(writer, 28, rowNumber, decimalValue(item.getRssbPayment()));
		writeNumberCell(writer, 29, rowNumber, decimalValue(item.getTotalInvoice()));
		writeTextCell(writer, 30, rowNumber, item.getTransferAvailable(), false);
		writeTextCell(writer, 31, rowNumber, item.getTransferValidity(), false);
		writer.write("</row>");
	}

	private static void writeLabelValueRow(BufferedWriter writer, int rowNumber, String label, String value)
			throws IOException {
		writer.write("<row r=\"");
		writer.write(Integer.toString(rowNumber));
		writer.write("\">");
		writeTextCell(writer, 0, rowNumber, label, false);
		writeTextCell(writer, 1, rowNumber, value, false);
		writer.write("</row>");
	}

	private static void writeTextRow(BufferedWriter writer, int rowNumber, int column, String value)
			throws IOException {
		writer.write("<row r=\"");
		writer.write(Integer.toString(rowNumber));
		writer.write("\">");
		writeTextCell(writer, column, rowNumber, value, false);
		writer.write("</row>");
	}

	private static void writeTextCell(BufferedWriter writer, int column, int row, String value, boolean textStyle)
			throws IOException {
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

	private static String formatPeriod(DateFormat dayFormat, Date startDate, Date endDate) {
		String start = startDate != null ? dayFormat.format(startDate) : "";
		String end = endDate != null ? dayFormat.format(endDate) : "";
		if (start.isEmpty() && end.isEmpty()) {
			return "";
		}
		return start + " - " + end;
	}

	private static String value(String text) {
		return text != null ? text : "";
	}

	private static String integerValue(Integer value) {
		return Integer.toString(value != null ? value : 0);
	}

	private static String decimalValue(BigDecimal value) {
		BigDecimal safe = value != null ? value : BigDecimal.ZERO;
		return safe.setScale(2, RoundingMode.HALF_UP).toPlainString();
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
				+ "<sheets><sheet name=\"Bill Report\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
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
