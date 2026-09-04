package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.PersonName;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PaidServiceRevenue;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PaymentRevenue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Streams the cashier report as XLSX without adding an Excel library to the OpenMRS classpath.
 */
final class CashierReportXlsxWriter {

	private static final int HEADER_ROW = 6;

	private CashierReportXlsxWriter() {
	}

	static void write(OutputStream outputStream, List<PaymentRevenue> paymentRevenues,
			List<BigDecimal> subTotals, BigDecimal bigTotal, BigDecimal totalPaid,
			String reportTitle, String collectorName, String facilityName) throws IOException {
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		writeEntry(zip, "[Content_Types].xml", contentTypesXml());
		writeEntry(zip, "_rels/.rels", packageRelationshipsXml());
		writeEntry(zip, "xl/workbook.xml", workbookXml());
		writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationshipsXml());
		writeEntry(zip, "xl/styles.xml", stylesXml());
		writeWorksheet(zip, paymentRevenues, subTotals, bigTotal, totalPaid,
				reportTitle, collectorName, facilityName);
		zip.finish();
		zip.flush();
	}

	private static void writeWorksheet(ZipOutputStream zip, List<PaymentRevenue> paymentRevenues,
			List<BigDecimal> subTotals, BigDecimal bigTotal, BigDecimal totalPaid,
			String reportTitle, String collectorName, String facilityName) throws IOException {
		List<PaymentRevenue> payments = paymentRevenues != null
				? paymentRevenues : Collections.<PaymentRevenue>emptyList();
		List<PaidServiceRevenue> services = payments.isEmpty()
				|| payments.get(0).getPaidServiceRevenues() == null
						? Collections.<PaidServiceRevenue>emptyList()
						: payments.get(0).getPaidServiceRevenues();
		int lastColumn = services.size() + 5;
		int totalsRow = HEADER_ROW + payments.size() + 1;

		zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writer.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
		writer.write("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"6\" topLeftCell=\"A7\" "
				+ "activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
		writer.write("<cols><col min=\"1\" max=\"1\" width=\"7\" customWidth=\"1\"/>"
				+ "<col min=\"2\" max=\"2\" width=\"21\" customWidth=\"1\"/>"
				+ "<col min=\"3\" max=\"3\" width=\"32\" customWidth=\"1\"/>"
				+ "<col min=\"4\" max=\"" + (lastColumn + 1)
				+ "\" width=\"18\" customWidth=\"1\"/></cols>");
		writer.write("<sheetData>");

		writeTextRow(writer, 1, valueOrDefault(facilityName, ""), 6);
		writeTextRow(writer, 2, valueOrDefault(reportTitle, "Detailed Cashier Daily Report"), 6);
		writeTextRow(writer, 3, "Collector: " + valueOrDefault(collectorName, "All collectors"), 0);
		writeTextRow(writer, 4, "Total collected: " + decimalValue(totalPaid), 0);
		writeHeaderRow(writer, services);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < payments.size(); i++) {
			writePaymentRow(writer, HEADER_ROW + i + 1, i + 1, payments.get(i), services.size(), dateFormat);
		}
		writeTotalsRow(writer, totalsRow, services.size(), subTotals, bigTotal, totalPaid);

		writer.write("</sheetData><autoFilter ref=\"A" + HEADER_ROW + ":"
				+ columnName(lastColumn) + Math.max(HEADER_ROW, totalsRow - 1) + "\"/></worksheet>");
		writer.flush();
		zip.closeEntry();
	}

	private static void writeHeaderRow(BufferedWriter writer, List<PaidServiceRevenue> services)
			throws IOException {
		writer.write("<row r=\"" + HEADER_ROW + "\">");
		writeTextCell(writer, 0, HEADER_ROW, "#", 2);
		writeTextCell(writer, 1, HEADER_ROW, "DATE", 2);
		writeTextCell(writer, 2, HEADER_ROW, "Patient Names", 2);
		int column = 3;
		for (PaidServiceRevenue service : services) {
			writeTextCell(writer, column++, HEADER_ROW,
					service != null ? service.getService() : "", 2);
		}
		writeTextCell(writer, column++, HEADER_ROW, "TOTAL Due", 2);
		writeTextCell(writer, column++, HEADER_ROW, "TOTAL Paid", 2);
		writeTextCell(writer, column, HEADER_ROW, "Payment through API", 2);
		writer.write("</row>");
	}

	private static void writePaymentRow(BufferedWriter writer, int rowNumber, int sequence,
			PaymentRevenue paymentRevenue, int serviceCount, DateFormat dateFormat) throws IOException {
		writer.write("<row r=\"" + rowNumber + "\">");
		writeNumberCell(writer, 0, rowNumber, Integer.toString(sequence), 5);
		BillPayment payment = paymentRevenue != null ? paymentRevenue.getPayment() : null;
		writeTextCell(writer, 1, rowNumber,
				payment != null ? FileExporter.formatDate(dateFormat, payment.getDateReceived()) : "", 5);
		writeTextCell(writer, 2, rowNumber, patientName(paymentRevenue), 5);

		List<PaidServiceRevenue> rowServices = paymentRevenue != null
				&& paymentRevenue.getPaidServiceRevenues() != null
						? paymentRevenue.getPaidServiceRevenues()
						: Collections.<PaidServiceRevenue>emptyList();
		int column = 3;
		for (int i = 0; i < serviceCount; i++) {
			BigDecimal amount = i < rowServices.size() && rowServices.get(i) != null
					? rowServices.get(i).getPaidAmount() : BigDecimal.ZERO;
			writeNumberCell(writer, column++, rowNumber, decimalValue(amount), 3);
		}
		writeNumberCell(writer, column++, rowNumber,
				decimalValue(paymentRevenue != null ? paymentRevenue.getAmount() : null), 3);
		writeNumberCell(writer, column++, rowNumber,
				decimalValue(payment != null ? payment.getAmountPaid() : null), 3);
		writeTextCell(writer, column, rowNumber, apiPaymentPhone(payment), 1);
		writer.write("</row>");
	}

	private static void writeTotalsRow(BufferedWriter writer, int rowNumber, int serviceCount,
			List<BigDecimal> subTotals, BigDecimal bigTotal, BigDecimal totalPaid) throws IOException {
		writer.write("<row r=\"" + rowNumber + "\">");
		writeTextCell(writer, 0, rowNumber, "TOTAL", 2);
		writeTextCell(writer, 1, rowNumber, "", 2);
		writeTextCell(writer, 2, rowNumber, "", 2);
		int column = 3;
		for (int i = 0; i < serviceCount; i++) {
			BigDecimal amount = subTotals != null && i < subTotals.size() ? subTotals.get(i) : BigDecimal.ZERO;
			writeNumberCell(writer, column++, rowNumber, decimalValue(amount), 4);
		}
		writeNumberCell(writer, column++, rowNumber, decimalValue(bigTotal), 4);
		writeNumberCell(writer, column++, rowNumber, decimalValue(totalPaid), 4);
		writeTextCell(writer, column, rowNumber, "", 2);
		writer.write("</row>");
	}

	private static String patientName(PaymentRevenue paymentRevenue) {
		if (paymentRevenue == null || paymentRevenue.getBeneficiary() == null
				|| paymentRevenue.getBeneficiary().getPatient() == null) {
			return "";
		}
		PersonName name = paymentRevenue.getBeneficiary().getPatient().getPersonName();
		return name != null ? name.toString() : "";
	}

	private static String apiPaymentPhone(BillPayment payment) {
		PatientBill patientBill = payment != null ? payment.getPatientBill() : null;
		return patientBill != null && "SUCCESSFUL".equals(patientBill.getTransactionStatus())
				? valueOrDefault(patientBill.getPhoneNumber(), "") : "";
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
				+ "<sheets><sheet name=\"Cashier Report\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
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
