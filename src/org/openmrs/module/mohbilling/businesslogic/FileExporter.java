package org.openmrs.module.mohbilling.businesslogic;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohtracportal.util.ContextProvider;
import org.openmrs.module.mohtracportal.util.MohTracUtil;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
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
			HttpServletResponse response, Map<String,List<PatientServiceBill>> map, String filename,String title) throws Exception {
		
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
			
			for (String key : map.keySet()) {

				outputStream.println(key);
				for (PatientServiceBill psb : map.get(key)) {
					outputStream.println(psb.getServiceDate()+","+psb.getService().getFacilityServicePrice().getName());
				}

			}
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
			HttpServletResponse response, List<PatientBill> bills,Map<String,List<PatientServiceBill>> map, String filename,
			String title) throws Exception {

		Document document = new Document();

		/** Initializing image to be the logo if any... */
		Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(90, 90);
		
		/** END of Initializing image */
		filename = "ConsommationsReport.pdf";

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ filename + "\""); // file name

		PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());
		// writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));
		writer.setBoxSize("art", PageSize.A4);

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4);
		// document.setPageSize(new Rectangle(0, 0, 2382, 3369));

		document.addAuthor(Context.getAuthenticatedUser().getPersonName().toString());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 10.0f, Font.NORMAL));

		/** ------------- Report title ------------- */
		
		Chunk chk = new Chunk("Printed on : "+ (new SimpleDateFormat("dd-MMM-yyyy").format(new Date())));
		chk.setFont(new Font(FontFamily.COURIER, 10.0f, Font.NORMAL));
		Paragraph todayDate = new Paragraph();
		todayDate.setAlignment(Element.ALIGN_RIGHT);
		todayDate.add(chk);
		document.add(todayDate);

		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

		/** I would like a LOGO here!!! */
		document.add(image);
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService()
						.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
								BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)+ "\n"));
		
		/** ------------- End Report title ------------- */

		document.add(new Paragraph("\n"));
		chk = new Chunk("FACTURE");
		chk.setFont(new Font(FontFamily.COURIER, 12.0f, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));

		// title row
		FontSelector fontTitleSelector = new FontSelector();
//		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 9, Font.NORMAL));


		PdfPTable tableHeader = new PdfPTable(1);
		tableHeader.setWidthPercentage(100f);

		// Table of identification;
		PdfPTable table = null;
		table = new PdfPTable(2);
		table.setWidthPercentage(100f);
		
		if(bills.size()==1){
		
		PatientBill pb = bills.get(0);

		PdfPCell cell = new PdfPCell(fontTitleSelector.process("Patient IDENTIFIER : "+pb.getBeneficiary().getPatient().getPatientIdentifier(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE))));
		
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("No de la carte : "
				+ pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("NOM ET PRENOM DU BENEFICIAIRE DE SOINS : "
				+ pb.getBeneficiary().getPatient().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		tableHeader.addCell(table);
		}

		document.add(tableHeader);

		document.add(new Paragraph("\n"));

		// Table of bill items;
		float[] colsWidth = { 6f, 10f, 2f, 2f, 2f};
		table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		BaseColor bckGroundTitle = new BaseColor(255, 255, 255);

		// table Header
		PdfPCell cell = new PdfPCell();
		cell = new PdfPCell(fontTitleSelector.process("Recording date"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Libelle"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("Unit Cost"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Qty"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Cost"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

		// empty row
		FontSelector fontTotals = new FontSelector();
		fontTotals.addFont(new Font(FontFamily.COURIER, 9, Font.NORMAL));

		int ids = 0;
		Double totalToBePaidOnService = 0.0;
		Double totalToBePaidByInsurance = 0.0, totalToBePaidOnServiceByInsurance = 0.0;
		Double totalToBePaidByPatient = 0.0, totalToBePaidOnServiceByPatient = 0.0;

		Double subTotal = 0.0;
		
					for (String key : map.keySet()) {
						cell = new PdfPCell(fontselector.process(key));
						cell.setColspan(5);
						table.addCell(cell);
						    for (PatientServiceBill item : map.get(key)) {
						    	Double up = item.getUnitPrice().doubleValue();
								Integer qty=item.getQuantity();
								Double cost = up*qty;
								subTotal+=up*qty;
								
								cell = new PdfPCell(fontselector.process(""+item.getServiceDate()));
								table.addCell(cell);
								
								cell = new PdfPCell(fontselector.process(item.getService().getFacilityServicePrice().getName()));
								table.addCell(cell);
			
								cell = new PdfPCell(fontselector.process("" + item.getQuantity()));
								table.addCell(cell);
					
								cell = new PdfPCell(fontselector.process("" + item.getUnitPrice()));
								table.addCell(cell);
								
								cell = new PdfPCell(fontselector.process("" + ReportsUtil.roundTwoDecimals(cost)));
								table.addCell(cell);
							}

					}
		
		
//		for (PatientServiceBill psb : pb.getBillItems()) {
//			ids += 1;
//
//			// initialize total amount to be paid on a service
//			totalToBePaidOnService = 0.0;
//			totalToBePaidOnServiceByInsurance = 0.0;
//			totalToBePaidOnServiceByPatient = 0.0;
//
//			cell = new PdfPCell(fontselector.process(ids + "."));
//			table.addCell(cell);
//
//			cell = new PdfPCell(fontselector.process(psb.getService()
//					.getFacilityServicePrice().getName()));
//			table.addCell(cell);
//
//			cell = new PdfPCell(fontselector.process("" + psb.getQuantity()));
//			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//			table.addCell(cell);
//
//			cell = new PdfPCell(fontselector.process("" + psb.getUnitPrice()));
//			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//			table.addCell(cell);
//
//			totalToBePaidOnService = (psb.getQuantity() * psb.getUnitPrice()
//					.doubleValue());
//
//			cell = new PdfPCell(fontselector.process(""
//					+ totalToBePaidOnService));
//			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//			table.addCell(cell);
//
//			totalToBePaidOnServiceByInsurance = ((totalToBePaidOnService * (pb
//					.getBeneficiary().getInsurancePolicy().getInsurance()
//					.getCurrentRate().getRate())) / 100);
//			totalToBePaidByInsurance += totalToBePaidOnServiceByInsurance;
//			cell = new PdfPCell(fontselector.process(""
//					+ totalToBePaidOnServiceByInsurance));
//			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//			table.addCell(cell);
//
//			totalToBePaidOnServiceByPatient = ((totalToBePaidOnService * (100 - pb
//					.getBeneficiary().getInsurancePolicy().getInsurance()
//					.getCurrentRate().getRate())) / 100);
//			totalToBePaidByPatient += totalToBePaidOnServiceByPatient;
//			cell = new PdfPCell(fontselector.process(""
//					+ totalToBePaidOnServiceByPatient));
//			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//			table.addCell(cell);
//		}

//		// Total for each part, insurance & patient
//		cell = new PdfPCell(fontselector.process("Total : "));
//		cell.setColspan(5);
//		cell.setBorder(Rectangle.NO_BORDER);
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		table.addCell(cell);
//
//		cell = new PdfPCell(fontTotals.process("" + totalToBePaidByInsurance));
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		cell.setBackgroundColor(new BaseColor(255, 255, 255));
//		table.addCell(cell);
//
//		cell = new PdfPCell(fontTotals.process("" + totalToBePaidByPatient));
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		cell.setBackgroundColor(new BaseColor(255, 255, 255));
//		table.addCell(cell);
//		

//		// Amount Paid
//		cell = new PdfPCell(fontselector.process("Amount Paid : "));
//		cell.setColspan(6);
//		cell.setBorder(Rectangle.NO_BORDER);
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		table.addCell(cell);
//
//		cell = new PdfPCell(fontTotals.process(""
//				+ MohBillingTagUtil.getTotalAmountPaidByPatientBill(pb
//						.getPatientBillId())));
//		
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		cell.setBackgroundColor(new BaseColor(255, 255, 255));
//		table.addCell(cell);
//
//		// Rest to be Paid
//		cell = new PdfPCell(fontselector.process("Rest : "));
//		cell.setColspan(6);
//		cell.setBorder(Rectangle.NO_BORDER);
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		table.addCell(cell);
//
//		cell = new PdfPCell(fontTotals.process(""
//				+ MohBillingTagUtil.getTotalAmountNotPaidByPatientBill(pb
//						.getPatientBillId())));
//		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//		cell.setBackgroundColor(new BaseColor(255, 255, 255));
//		table.addCell(cell);

		document.add(table);

		document.add(new Paragraph("\n\n"));

		// Table of signatures;
		if(bills.size()==1){
		table = new PdfPTable(2);
		table.setWidthPercentage(100f);

		PatientBill pb = bills.get(0);
		cell = new PdfPCell(
				fontTitleSelector.process("Signature du Patient :\n"
						+ pb.getBeneficiary().getPatient().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(
				fontTitleSelector.process("Noms et Signature du Caissier\n"
						+ Context.getAuthenticatedUser().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		}

		document.add(table);

		document.close();
	}

	static class HeaderFooter extends PdfPageEventHelper {
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");

			Phrase header = new Phrase(String.format("- %d -",
					writer.getPageNumber()));
			header.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			if (document.getPageNumber() > 1) {
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_CENTER, header,
						(rect.getLeft() + rect.getRight()) / 2,
						rect.getTop() + 40, 0);
			}

			Phrase footer = new Phrase(String.format("- %d -",
					writer.getPageNumber()));

			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, footer,
					(rect.getLeft() + rect.getRight()) / 2,
					rect.getBottom() - 40, 0);

		}
	}
	}
