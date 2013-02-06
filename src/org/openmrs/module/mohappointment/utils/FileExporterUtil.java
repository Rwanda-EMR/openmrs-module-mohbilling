/**
 * 
 */
package org.openmrs.module.mohappointment.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.AppointmentView;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author Yves GAKUBA
 * 
 */
public class FileExporterUtil {
	private Log log = LogFactory.getLog(this.getClass());

	public void exportToCSVFile(HttpServletRequest request,
			HttpServletResponse response, String title) throws IOException {

		ServletOutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			SimpleDateFormat sdf = new SimpleDateFormat(request
					.getParameter("dateFormat"));

			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ title.toLowerCase().replace(" ", "_") + ".csv\"");
			outputStream.println("Report Title, : " + title);

			outputStream.println("Report Date, : " + sdf.format(new Date()));// Report
			// date
			outputStream.println("Created by, : "
					+ Context.getAuthenticatedUser().getPersonName());// Report
			// author

			outputStream.println();

			outputStream.println();

			outputStream.flush();
			log.info(">>>>>>>>>>> CSV File created !");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (null != outputStream)
				outputStream.close();
		}
	}

	public void exportAppointments(HttpServletRequest request,
			HttpServletResponse response, List<AppointmentView> appointments)
			throws IOException {

		try {
			if (request.getParameter("fileFormat").compareTo("csv") == 0)
				exportAppointmentsToCSV(request, response, appointments);
			else if (request.getParameter("fileFormat").compareTo("xls") == 0)
				exportAppointmentsToXLS(request, response, appointments);
			else if (request.getParameter("fileFormat").compareTo("pdf") == 0)
				exportAppointmentsToPDF(request, response, appointments);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void exportAppointmentsToCSV(HttpServletRequest request,
			HttpServletResponse response, List<AppointmentView> appointments)
			throws IOException {

		ServletOutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			SimpleDateFormat sdf = new SimpleDateFormat(request
					.getParameter("dateFormat"));
			Locale locale = new Locale(request.getParameter("reportLanguage"));

			String title = (request.getParameter("reportName").trim()
					.compareTo("") != 0) ? request.getParameter("reportName")
					.trim() : "appointment_report_" + (new Date());

			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ title.toLowerCase().replace(" ", "_") + ".csv\"");

			outputStream.println(ContextProvider.getMessage(
					"mohappointment.report", null, locale));

			outputStream.println();

			outputStream.println(ContextProvider.getMessage(
					"mohappointment.export.report.title", null, locale)
					+ ", : " + request.getParameter("reportName"));

			outputStream.println(ContextProvider.getMessage(
					"mohappointment.export.report.date", null, locale)
					+ ", : " + sdf.format(new Date()));// Report
			// date
			// outputStream.println("Number of Appointments, : "
			// + appointments.size());// Number of appointments

			outputStream.println(ContextProvider.getMessage(
					"mohappointment.export.report.creator", null, locale)
					+ ", : " + Context.getAuthenticatedUser().getPersonName());// Report
			// author

			outputStream.println();

			outputStream.println(ContextProvider.getMessage(
					"mohappointment.general.appointmentdate", null, locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.number", null, locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.identifier", null, locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.names", null, locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.provider", null, locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.reasonofappointment", null,
							locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.clinicalareatosee", null,
							locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.location", null, locale)
					+ ", "
					+ ContextProvider.getMessage(
							"mohappointment.general.stateofappointment", null,
							locale));

			int id = 1;

			for (AppointmentView iap : appointments) {
				outputStream.println(sdf.format(iap.getAppointmentDate())
						+ ", " + (id++) + "., "
						+ iap.getPatient().getPatientIdentifier() + ", "
						+ iap.getPatient().getPersonName() + ", "
						+ iap.getProvider().getPersonName() + ", "
						+ iap.getReason().getValueAsString(Context.getLocale())
						+ ", " + iap.getService().getName() + ", "
						+ iap.getLocation().getName() + ", "
						+ iap.getAppointmentState().getDescription());
			}

			outputStream.flush();
			log.info(">>>>>>>>>>> CSV File created !");
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (null != outputStream)
				outputStream.close();
		}
	}

	public void exportAppointmentsToXLS(HttpServletRequest request,
			HttpServletResponse response, List<AppointmentView> appointments)
			throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat(request
				.getParameter("dateFormat"));

		Locale locale = new Locale(request.getParameter("reportLanguage"));

		String title = (request.getParameter("reportName").trim().compareTo("") != 0) ? request
				.getParameter("reportName").trim()
				: "appointment_report_" + (new Date());

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ title.toLowerCase().replace(" ", "_") + ".xls\"");

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet worksheet = workbook.createSheet(title);
		worksheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

		// level one header
		HSSFFont level1Font = workbook.createFont();
		level1Font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		level1Font.setColor(HSSFFont.COLOR_NORMAL);
		level1Font.setFontHeightInPoints((short) 14);
		level1Font.setFontName(HSSFFont.FONT_ARIAL);

		// level two header
		HSSFFont level2Font = workbook.createFont();
		level2Font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		level2Font.setColor(HSSFFont.COLOR_NORMAL);
		level2Font.setFontHeightInPoints((short) 11);
		level2Font.setFontName(HSSFFont.FONT_ARIAL);

		// level three header
		HSSFFont level3Font = workbook.createFont();
		// level3Font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		level3Font.setColor(HSSFFont.COLOR_NORMAL);
		level3Font.setFontHeightInPoints((short) 10);
		level3Font.setFontName(HSSFFont.FONT_ARIAL);

		HSSFRow rowReportHeader = worksheet.createRow((short) 1);

		HSSFCell cellHeader = rowReportHeader.createCell((short) 0);
		cellHeader.setCellValue(ContextProvider.getMessage(
				"mohappointment.report", null, locale));
		HSSFCellStyle headerCellStyle = workbook.createCellStyle();
		// cellStyle.setFillForegroundColor(HSSFColor.GOLD.index);
		// cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		headerCellStyle.setFont(level1Font);
		cellHeader.setCellStyle(headerCellStyle);

		HSSFRow rowReportDate = worksheet.createRow((short) 3);

		HSSFCell rptDateLabel = rowReportDate.createCell((short) 0);
		rptDateLabel.setCellValue(ContextProvider.getMessage(
				"mohappointment.export.report.date", null, locale));
		HSSFCellStyle cellStyleRptDateLabel = workbook.createCellStyle();
		cellStyleRptDateLabel.setFont(level2Font);
		rptDateLabel.setCellStyle(cellStyleRptDateLabel);
		HSSFCell rptDateValue = rowReportDate.createCell((short) 1);
		rptDateValue.setCellValue("" + sdf.format(new Date()));

		HSSFRow rowReportCreator = worksheet.createRow((short) 4);

		HSSFCell rptCreatorLabel = rowReportCreator.createCell((short) 0);
		rptCreatorLabel.setCellValue(ContextProvider.getMessage(
				"mohappointment.export.report.creator", null, locale));
		HSSFCellStyle cellStyleRptCreatorLabel = workbook.createCellStyle();
		cellStyleRptCreatorLabel.setFont(level2Font);
		rptCreatorLabel.setCellStyle(cellStyleRptCreatorLabel);
		HSSFCell rptReportCreatorValue = rowReportCreator.createCell((short) 1);
		rptReportCreatorValue.setCellValue(Context.getAuthenticatedUser()
				.getPersonName().toString());

		HSSFRow rowReportTitle = worksheet.createRow((short) 7);

		HSSFCell rptLabel = rowReportTitle.createCell((short) 0);
		rptLabel.setCellValue(title);
		HSSFCellStyle cellStyleRptLabel = workbook.createCellStyle();
		cellStyleRptLabel
				.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleRptLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleRptLabel.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyleRptLabel.setFont(level2Font);
		rptLabel.setCellStyle(cellStyleRptLabel);
		// HSSFCell rptValue = rowReportTitle.createCell((short) 1);
		// rptValue.setCellValue();

		worksheet.addMergedRegion(new CellRangeAddress(7, 7, 0, 7));

		HSSFRow rowColumnHeader = worksheet.createRow((short) 8);

		HSSFCell cellColHd1 = rowColumnHeader.createCell((short) 0);
		cellColHd1.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.appointmentdate", null, locale));
		HSSFCellStyle cellStyleColHd1 = workbook.createCellStyle();
		cellStyleColHd1.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd1.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd1.setFont(level2Font);
		cellColHd1.setCellStyle(cellStyleColHd1);

		HSSFCell cellColHd2 = rowColumnHeader.createCell((short) 1);
		cellColHd2.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.number", null, locale));
		HSSFCellStyle cellStyleColHd2 = workbook.createCellStyle();
		cellStyleColHd2.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd2.setFont(level2Font);
		cellColHd2.setCellStyle(cellStyleColHd2);

		HSSFCell cellColHd3 = rowColumnHeader.createCell((short) 2);
		cellColHd3.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.identifier", null, locale));
		HSSFCellStyle cellStyleColHd3 = workbook.createCellStyle();
		cellStyleColHd3.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd3.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd3.setFont(level2Font);
		cellColHd3.setCellStyle(cellStyleColHd3);

		HSSFCell cellColHd4 = rowColumnHeader.createCell((short) 3);
		cellColHd4.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.names", null, locale));
		HSSFCellStyle cellStyleColHd4 = workbook.createCellStyle();
		cellStyleColHd4.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd4.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd4.setFont(level2Font);
		cellColHd4.setCellStyle(cellStyleColHd4);

		HSSFCell cellColHd5 = rowColumnHeader.createCell((short) 4);
		cellColHd5.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.provider", null, locale));
		HSSFCellStyle cellStyleColHd5 = workbook.createCellStyle();
		cellStyleColHd5.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd5.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd5.setFont(level2Font);
		cellColHd5.setCellStyle(cellStyleColHd5);

		HSSFCell cellColHd6 = rowColumnHeader.createCell((short) 5);
		cellColHd6.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.reasonofappointment", null, locale));
		HSSFCellStyle cellStyleColHd6 = workbook.createCellStyle();
		cellStyleColHd6.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd6.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd6.setFont(level2Font);
		cellColHd6.setCellStyle(cellStyleColHd6);

		HSSFCell cellColHd7 = rowColumnHeader.createCell((short) 6);
		cellColHd7.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.clinicalareatosee", null, locale));
		HSSFCellStyle cellStyleColHd7 = workbook.createCellStyle();
		cellStyleColHd7.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd7.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd7.setFont(level2Font);
		cellColHd7.setCellStyle(cellStyleColHd7);

		HSSFCell cellColHd8 = rowColumnHeader.createCell((short) 7);
		cellColHd8.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.location", null, locale));
		HSSFCellStyle cellStyleColHd8 = workbook.createCellStyle();
		cellStyleColHd8.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd8.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd8.setFont(level2Font);
		cellColHd8.setCellStyle(cellStyleColHd8);

		HSSFCell cellColHd9 = rowColumnHeader.createCell((short) 8);
		cellColHd8.setCellValue(ContextProvider.getMessage(
				"mohappointment.general.stateofappointment", null, locale));
		HSSFCellStyle cellStyleColHd9 = workbook.createCellStyle();
		cellStyleColHd9.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cellStyleColHd9.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyleColHd9.setFont(level2Font);
		cellColHd9.setCellStyle(cellStyleColHd9);

		int id = 1;

		for (AppointmentView iap : appointments) {

			HSSFRow rowValues = worksheet.createRow((short) 8 + id);

			HSSFCell appDate = rowValues.createCell((short) 0);
			appDate.setCellValue(sdf.format(iap.getAppointmentDate()));
			// HSSFCellStyle cellStyleColHd1 = workbook.createCellStyle();
			// cellStyleColHd1.setFont(level2Font);
			// cellColHd1.setCellStyle(cellStyleColHd1);

			HSSFCell idCell = rowValues.createCell((short) 1);
			idCell.setCellValue(id++);

			HSSFCell ptIdentifierCell = rowValues.createCell((short) 2);
			ptIdentifierCell.setCellValue(""
					+ iap.getPatient().getPatientIdentifier());

			HSSFCell ptName = rowValues.createCell((short) 3);
			ptName.setCellValue(iap.getPatient().getPersonName().toString());

			HSSFCell provName = rowValues.createCell((short) 4);
			provName.setCellValue(iap.getProvider().getPersonName().toString());

			HSSFCell reason = rowValues.createCell((short) 5);
			reason.setCellValue(iap.getReason().getValueAsString(locale));

			HSSFCell service = rowValues.createCell((short) 6);
			service.setCellValue(iap.getService().getName());

			HSSFCell location = rowValues.createCell((short) 7);
			location.setCellValue(iap.getLocation().getName());

			HSSFCell state = rowValues.createCell((short) 7);
			state.setCellValue(iap.getAppointmentState().getDescription());

		}

		for (int i = 0; i < 8; i++)
			worksheet.autoSizeColumn(i);

		OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

	}

	public void exportAppointmentsToPDF(HttpServletRequest request,
			HttpServletResponse response, List<AppointmentView> appointments)
			throws IOException, Exception {

		SimpleDateFormat sdf = new SimpleDateFormat(request
				.getParameter("dateFormat"));

		Locale locale = new Locale(request.getParameter("reportLanguage"));

		Document document = new Document();

		String title = (request.getParameter("reportName").trim().compareTo("") != 0) ? request
				.getParameter("reportName").trim()
				: "appointment_report_" + (new Date());

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ title.toLowerCase().replace(" ", "_") + "\""); // file name

		PdfWriter writer = PdfWriter.getInstance(document, response
				.getOutputStream());
		writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4);

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
				.toString());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 10.0f, Font.BOLD));

		FontSelector bigTitle = new FontSelector();
		bigTitle.addFont(new Font(FontFamily.COURIER, 12.0f, Font.BOLD));

		document.add(bigTitle.process(ContextProvider.getMessage(
				"mohappointment.report", null, locale)
				+ "\n\n"));

		String rptTitle = ContextProvider.getMessage(
				"mohappointment.export.report.title", null, locale)
				+ " : " + request.getParameter("reportName").trim();
		String underLine = "";
		int count = 0;
		while (count < rptTitle.length()) {
			count += 1;
			underLine += "_";
		}

		document.add(fontTitle.process(rptTitle));// Report title

		document.add(fontTitle.process("\n"
				+ ContextProvider.getMessage(
						"mohappointment.export.report.date", null, locale)
				+ "  : " + sdf.format(new Date())));// Report date
		document.add(fontTitle.process("\n"
				+ ContextProvider.getMessage(
						"mohappointment.export.report.creator", null, locale)
				+ "   : " + Context.getAuthenticatedUser().getPersonName()));// Report
		// author
		document.add(fontTitle.process("\n" + underLine));// Report title
		document.add(new Paragraph("\n\n"));

		// PdfLine line;
		PdfPTable table = null;
		float[] colsWidth = { 1.2f, 5f, 2.7f, 2.7f, 4.2f, 2.7f, 3.1f, 2.1f };
		table = new PdfPTable(colsWidth);

		// column number
		table.setTotalWidth(540f);

		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
		BaseColor bckGroundTitle = new BaseColor(170, 170, 170);

		// table Header
		PdfPCell cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.stateofappointment", null,
						locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.number", null, locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.identifier", null, locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.names", null, locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.provider", null, locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.reasonofappointment", null,
						locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.clinicalareatosee", null,
						locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process(ContextProvider
				.getMessage("mohappointment.general.stateofappointment", null,
						locale)));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		document.add(table);
		document.close();

		log.info(">>>>>>>>>>> PDF file created");
	}

	static class HeaderFooter extends PdfPageEventHelper {
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");

			Phrase header = new Phrase(String.format("- %d -", writer
					.getPageNumber()));
			header.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			if (document.getPageNumber() > 1) {
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_CENTER, header, (rect.getLeft() + rect
								.getRight()) / 2, rect.getTop() + 40, 0);
			}

			Phrase footer = new Phrase(String.format("- %d -", writer
					.getPageNumber()));
			footer.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, footer, (rect.getLeft() + rect
							.getRight()) / 2, rect.getBottom() - 40, 0);

		}
	}

}
