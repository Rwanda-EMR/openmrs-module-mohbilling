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
	public void exportToPDF(HttpServletRequest request,
			HttpServletResponse response, Map<Integer,String> map, String filename,
			String title) throws Exception {

		SimpleDateFormat sdf = Context.getDateFormat();

		Document document = new Document();

		
	
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ filename + "\""); // file name
		
		
		PdfWriter writer = PdfWriter.getInstance(document, response
				.getOutputStream());
		writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));

		float[] colsWidth = { 1.6f, 2.7f, 2.7f, 8f, 10.5f, 4f, 5f, 7.5f};//, 9.3f };
		PdfPTable table = new PdfPTable(colsWidth); // column number

		HeaderFooter event = new HeaderFooter(table);
		writer.setPageEvent(event);

		document.setPageSize(PageSize.A4.rotate());
		document.open();

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
						.getFamilyName()
						+ " "
						+ Context.getAuthenticatedUser().getPersonName()
								.getGivenName());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.HELVETICA, 8.0f, Font.BOLD));
		
		document.add(fontTitle.process(MohTracUtil.getMessage(
				"billing report", null)
				+ "    : " + title));// Report title
		document.add(fontTitle.process("\n"
				+ MohTracUtil.getMessage(
						"created on", null) + " : "+ sdf.format(new Date())));// Report date
		document.add(fontTitle.process("\n"+ MohTracUtil.getMessage(
						"tracpatienttransfer.report.createdby", null) + " : "
				+ Context.getAuthenticatedUser().getPersonName()));// Report
		// author
		document.add(new Paragraph("\n"));

		Paragraph para = new Paragraph("" + title.toUpperCase());
		para.setAlignment(Element.ALIGN_CENTER);
		para.setFont(new Font(FontFamily.HELVETICA, 8.0f, Font.BOLD));
		document.add(para);

		table.setWidthPercentage(100.0f);

		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.HELVETICA, 8, Font.BOLD));


		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.HELVETICA, 7, Font.NORMAL));

		// empty row
		FontSelector fontEmptyCell = new FontSelector();
		fontEmptyCell.addFont(new Font(FontFamily.HELVETICA, 7, Font.NORMAL));

		int ids = 0;
		System.out.println("getffffffffffbeforeeeeefffffffffvalueee"+map.get(10)+"file name:"+filename);
		for (Integer  key : map.keySet()) {
			System.out.println("get keyyueyyeyeyye"+key);
			table.addCell(key+ "");
			table.addCell(map.get(key)+ "");
			
		//	for (PatientServiceBill psb:map.get(key)) {
				/*table.addCell(psb.getServiceDate()+"");
				table.addCell(psb.getService().getFacilityServicePrice().getName()+"");
				table.addCell(psb.getUnitPrice()+"");
				table.addCell(psb.getQuantity()+"");
				table.addCell(psb.getUnitPrice().doubleValue()*psb.getQuantity()+"");
			//}
			*/
		}

		document.add(table);
		document.close();

		log.info("pdf file created");
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
