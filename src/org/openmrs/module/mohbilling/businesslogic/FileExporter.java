package org.openmrs.module.mohbilling.businesslogic;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohtracportal.util.ContextProvider;
import org.openmrs.module.mohtracportal.util.MohTracUtil;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
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

public class FileExporter {
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * gets a list and export it in csv/pdf
	 * 
	 * @param request
	 * @param response
	 * @param res
	 * @param filename
	 * @param title
	 * @throws Exception
	 */
	public void exportToCSVFile(HttpServletRequest request,
			HttpServletResponse response, Map<Integer, String> testMap, String filename,String title) throws Exception {
		log.info(" isss the >>>>>>>>>map size"+testMap.size());
		
		ServletOutputStream outputStream = null;

		//try {
			SimpleDateFormat sdf = Context.getDateFormat();
			outputStream = response.getOutputStream();
			ObsService os = Context.getObsService();

			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\""	+ filename + "\"");

			// header
			outputStream.println(MohTracUtil.getMessage("Billing Report", null)
					+ ", : " + title);
			outputStream.println();
//			if (request.getParameter("reason") != null
//					&& request.getParameter("reason").trim().compareTo("") != 0) {
//				Integer conceptId = Integer.parseInt(request.getParameter("reason"));
//				outputStream.println(MohTracUtil.getMessage("tracpatienttransfer.report.reasonofexit", null)+ ", : "
//						+ TransferOutInPatientTag.getConceptNameById(""
//								+ conceptId));
//			}

//			if (request.getParameter("location") != null
//					&& request.getParameter("location").trim().compareTo("") != 0) {
//				Integer locationId = Integer.parseInt(request
//						.getParameter("location"));
//				outputStream.println(MohTracUtil.getMessage(
//						"tracpatienttransfer.report.location", null)
//						+ ", : "
//						+ Context.getLocationService().getLocation(locationId)
//								.getName());
//				outputStream.println();
//			}
//			outputStream.println("\n"
//					+ MohTracUtil.getMessage(
//							"tracpatienttransfer.report.createdon", null)
//					+ ", : " + sdf.format(new Date()));// Report date
//			outputStream.println("\n"
//					+ MohTracUtil.getMessage(
//							"tracpatienttransfer.report.createdby", null)
//					+ ", : " + Context.getAuthenticatedUser().getPersonName());// Report
//
//			outputStream.println(MohTracUtil.getMessage(
//					"tracpatienttransfer.report.numberofpatient", null)
//					+ ", " + res.size());
//			outputStream.println();


			// column header
//			outputStream.println(MohTracUtil.getMessage("tracpatienttransfer.general.number", null)+ ", "+ TransferOutInPatientTag.getIdentifierTypeNameById(""
//							+ TracPatientTransferConfigurationUtil.getTracNetIdentifierTypeId())
//					+ ", "
//					+ TransferOutInPatientTag.getIdentifierTypeNameById(""+ TracPatientTransferConfigurationUtil
//									.getLocalHealthCenterIdentifierTypeId())+ ", "
//					+ ((hasRoleToViewPatientsNames) ? MohTracUtil.getMessage("tracpatienttransfer.general.names", null)+ ", " : "")
//					+ MohTracUtil.getMessage("tracpatienttransfer.general.reasonofexit", null)
//					+ ", "+ MohTracUtil.getMessage("tracpatienttransfer.general.exitwhen", null)+ " ?(dd/MM/yyyy), "
//					+ MohTracUtil.getMessage("Encounter.location", null) + "");
//			outputStream.println();


			log.info(">>>>>>>>>>>>>> Trying to create a CSV file...");
			
			//for (String key : testMap.keySet()) {

				//outputStream.println(key);
				for (Integer nb : testMap.keySet()) {
				
				}

			//}
			outputStream.flush();
			log.info(">>>>>>>>>>>>>> A CSV file was created successfully.");
		
	}

	/**
	 * @param request
	 * @param response
	 * @param res
	 * @param filename
	 * @param title
	 * @throws Exception
	 */
	public void exportToPDF(Map<Integer,String> model, Document doc,
            PdfWriter writer, HttpServletRequest request, HttpServletResponse response) throws Exception {

         
	        doc.add(new Paragraph("Recommended books for Spring framework"));
	         
	        PdfPTable table = new PdfPTable(5);
	        table.setWidthPercentage(100.0f);
	        table.setWidths(new float[] {3.0f, 2.0f, 2.0f, 2.0f, 1.0f});
	        table.setSpacingBefore(10);
	         
	        // define font for table header row
	        Font font = FontFactory.getFont(FontFactory.HELVETICA);
	        font.setColor(BaseColor.WHITE);
	         
	        // define table header cell
	        PdfPCell cell = new PdfPCell();
	        cell.setBackgroundColor(BaseColor.BLUE);
	        cell.setPadding(5);
	         
	        // write table header
	        cell.setPhrase(new Phrase("Book Title", font));
	        table.addCell(cell);
	         
	        cell.setPhrase(new Phrase("Author", font));
	        table.addCell(cell);
	 
	        cell.setPhrase(new Phrase("ISBN", font));
	        table.addCell(cell);
	         
	        cell.setPhrase(new Phrase("Published Date", font));
	        table.addCell(cell);
	         
	        cell.setPhrase(new Phrase("Price", font));
	        table.addCell(cell);
	         
//	        // write table row data
	        for (Integer b : model.keySet()) {
	            table.addCell(b+""+model.get(b));
	        }
	         
	        doc.add(table);
	         
	    }
	 


	static class HeaderFooter extends PdfPageEventHelper {
		private PdfPTable table = null;

		public HeaderFooter(PdfPTable tb) {
			table = tb;
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");

			PdfPCell pdfPCells[] = table.getRow(table.getRows().size() - 1)
					.getCells();
			for (PdfPCell pdfPCell : pdfPCells) {
				pdfPCell.setBorder(PdfPCell.BOTTOM);
			}

			Phrase header = new Phrase(String.format("- %d -", writer
					.getPageNumber()));
			header.setFont(new Font(FontFamily.HELVETICA, 5, Font.NORMAL));

			if (document.getPageNumber() > 1) {
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_CENTER, header, (rect.getLeft() + rect
								.getRight()) / 2, rect.getTop(), 0);
			}

			Phrase footer = new Phrase(String.format("- %d -", writer
					.getPageNumber()));
			footer.setFont(new Font(FontFamily.HELVETICA, 5, Font.NORMAL));

			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, footer, (rect.getBottom() + rect
							.getTop()) / 2, rect.getBottom() - 40, 0);

		}
	}
}
