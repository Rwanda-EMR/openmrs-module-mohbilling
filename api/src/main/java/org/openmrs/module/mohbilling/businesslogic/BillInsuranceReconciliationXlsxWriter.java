package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.module.mohbilling.model.BillInsuranceReconciliationItem;

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
 * Excel export for Bill Report vs Insurance Report reconciliation.
 */
final class BillInsuranceReconciliationXlsxWriter {

	private static final String[] DATA_HEADERS = {
			"No",
			"Appears In",
			"Reason",
			"Voucher ID",
			"Global Bill ID",
			"Admission Date",
			"Closing / Discharge Date",
			"Patient",
			"Patient ID",
			"Card No",
			"Insurance",
			"Total"
	};

	private BillInsuranceReconciliationXlsxWriter() {
	}

	static void write(OutputStream outputStream, List<BillInsuranceReconciliationItem> rows, Date startDate,
			Date endDate) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		writeEntry(zip, "[Content_Types].xml", contentTypesXml());
		writeEntry(zip, "_rels/.rels", packageRelationshipsXml());
		writeEntry(zip, "xl/workbook.xml", workbookXml());
		writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
		writeEntry(zip, "xl/styles.xml", stylesXml());
		writeWorksheet(zip, rows, startDate, endDate);
		zip.finish();
		zip.flush();
	}

	private static void writeWorksheet(ZipOutputStream zip, List<BillInsuranceReconciliationItem> rows,
			Date startDate, Date endDate) throws IOException {
		zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writer.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		writer.write("<sheetData>");

		DateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
		writeTextRow(writer, 1, 0, "Bill Report vs Insurance Report reconciliation");
		writeLabelValueRow(writer, 2, "Period", formatPeriod(dayFormat, startDate, endDate));
		writeLabelValueRow(writer, 3, "Rows", Integer.toString(rows != null ? rows.size() : 0));

		writer.write("<row r=\"5\">");
		for (int column = 0; column < DATA_HEADERS.length; column++) {
			writeTextCell(writer, column, 5, DATA_HEADERS[column], false);
		}
		writer.write("</row>");

		if (rows != null) {
			for (int i = 0; i < rows.size(); i++) {
				writeDataRow(writer, i + 6, rows.get(i), dayFormat);
			}
		}

		writer.write("</sheetData></worksheet>");
		writer.flush();
		zip.closeEntry();
	}

	private static void writeDataRow(BufferedWriter writer, int rowNumber, BillInsuranceReconciliationItem item,
			DateFormat dayFormat) throws IOException {
		writer.write("<row r=\"");
		writer.write(Integer.toString(rowNumber));
		writer.write("\">");
		writeNumberCell(writer, 0, rowNumber, Integer.toString(item.getRowNumber() != null ? item.getRowNumber() : 0));
		writeTextCell(writer, 1, rowNumber, item.getAppearsIn(), false);
		writeTextCell(writer, 2, rowNumber, item.getReason(), false);
		writeTextCell(writer, 3, rowNumber, item.getVoucherId(), true);
		writeTextCell(writer, 4, rowNumber,
				item.getGlobalBillId() != null ? item.getGlobalBillId().toString() : "", true);
		writeTextCell(writer, 5, rowNumber, FileExporter.formatDate(dayFormat, item.getAdmissionDate()), false);
		writeTextCell(writer, 6, rowNumber, FileExporter.formatDate(dayFormat, item.getClosingDate()), false);
		writeTextCell(writer, 7, rowNumber, item.getPatientNames(), false);
		writeTextCell(writer, 8, rowNumber, item.getPatientId(), true);
		writeTextCell(writer, 9, rowNumber, item.getCardNumber(), true);
		writeTextCell(writer, 10, rowNumber, item.getInsuranceName(), false);
		writeNumberCell(writer, 11, rowNumber, decimalValue(item.getTotalAmount()));
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
				+ "<sheets><sheet name=\"Reconciliation\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
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
