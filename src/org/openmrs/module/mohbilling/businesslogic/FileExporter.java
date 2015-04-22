package org.openmrs.module.mohbilling.businesslogic;

import java.io.PrintWriter;
import java.text.DateFormat;
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
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
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
			HttpServletResponse response,Map<String,List<PatientServiceBill>> map, String filename,
			String title) throws Exception {

		Document document = new Document();
		
		Patient patient =null;
		if(request.getParameter("patientId")!=null &&!request.getParameter("patientId").equals(""))
		patient = Context.getPatientService().getPatient(Integer.parseInt(request.getParameter("patientId")));
		
		String startDateStr,startHourStr = null,startMinute=null,endDateStr=null,endHourStr = null, endMinuteStr = null;
		Date startDate=null,endDate=null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		startHourStr = request.getParameter("startHour");
		startMinute = request.getParameter("startMinute"); 
		endHourStr = request.getParameter("endHour");
		endMinuteStr = request.getParameter("endMinute");
		

		if(request.getParameter("startDate") != null && !request.getParameter("startDate").equals("")) {
			String startTimeStr = startHourStr + ":" + startMinute + ":00";
			startDateStr = request.getParameter("startDate");
			startDate = sdf.parse(startDateStr.split("/")[2] + "-"
					+ startDateStr.split("/")[1] + "-"
					+ startDateStr.split("/")[0] + " " + startTimeStr);
		}
		if(request.getParameter("endDate") != null && !request.getParameter("endDate").equals("")) {
			String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
			endDateStr = request.getParameter("endDate");
			endDate = sdf.parse(endDateStr.split("/")[2] + "-"
					+ endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0]
					+ " " + endTimeStr);
		}
		PatientBill pb = Context.getService(BillingService.class).getBills(patient, startDate, endDate);

		/** Initializing image to be the logo if any... */
		Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(90, 90);
		
		
		/** END of Initializing image */

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ filename + "\""); // file name

		PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());
		writer.setBoxSize("art", PageSize.A4);

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4);

		document.addAuthor(Context.getAuthenticatedUser().getPersonName().toString());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 10.0f, Font.NORMAL));

		/** ------------- Report title ------------- */
		
//		Chunk chk = new Chunk("Printed on : "+ (new SimpleDateFormat("dd-MMM-yyyy").format(new Date())));
//		chk.setFont(new Font(FontFamily.COURIER, 10.0f, Font.NORMAL));
//		Paragraph todayDate = new Paragraph();
//		todayDate.add(chk);
//		document.add(todayDate);

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
		Chunk chk = new Chunk("FACTURES DES PRESTATIONS DES SOINS DE SANTE");
		chk.setFont(new Font(FontFamily.COURIER, 12.0f, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));
		
		Font catFont = new Font(Font.FontFamily.COURIER, 12.0f,Font.NORMAL);
		Paragraph header = new Paragraph();
		header.add(new Paragraph("Patient No : "+pb.getBeneficiary().getPatient().getIdentifiers(), catFont));
		addEmptyLine(header, 0);
		header.add(new Paragraph("No de la carte : "+pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo(), catFont));
		addEmptyLine(header, 0);
		header.add(new Paragraph("Names : "+pb.getBeneficiary().getPatient().getPersonName(), catFont));
		addEmptyLine(header, 0);
		document.add(header);

		document.add(new Paragraph(" "));
		chk = new Chunk("DETAILS DES SOINS RECUS");
		chk.setFont(new Font(FontFamily.COURIER, 12.0f, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa1 = new Paragraph();
		pa1.add(chk);
		pa1.setAlignment(Element.ALIGN_CENTER);
		document.add(pa1);
		document.add(new Paragraph("\n"));
		
		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 9, Font.NORMAL));

		// Table of bill items;
		float[] colsWidth = { 6f, 10f, 2f, 2f, 2f};
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		BaseColor bckGroundTitle = new BaseColor(255, 255, 255);

		// table Header
//		PdfPCell cell = new PdfPCell();
		PdfPCell cell = new PdfPCell(fontTitleSelector.process("Recording date"));
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

		Double totalToBePaidByPatient = 0.0;

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
					for (String key : map.keySet()) {
						cell = new PdfPCell(fontselector.process(key));
						cell.setColspan(5);
						table.addCell(cell);
						Double subTotal = 0.0;
						    for (PatientServiceBill item : map.get(key)) {
						    	Double up = item.getUnitPrice().doubleValue();
								Integer qty=item.getQuantity();
								Double cost = up*qty;
								subTotal+=up*qty;
								
								cell = new PdfPCell(fontselector.process(""+df.format(item.getServiceDate())));
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
						    totalToBePaidByPatient+=subTotal;
						    
						    cell = new PdfPCell(fontselector.process("" + ReportsUtil.roundTwoDecimals(subTotal)));
						    cell.setColspan(5);
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							table.addCell(cell);
					}
					cell = new PdfPCell(fontselector.process("" + ReportsUtil.roundTwoDecimals(totalToBePaidByPatient)));
				    cell.setColspan(5);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
		document.add(table);

		document.add(new Paragraph("\n\n"));
		
		// Table of signatures;
		table = new PdfPTable(3);
		table.setWidthPercentage(70f);
		
		cell = new PdfPCell(fontTitleSelector.process("ASSURANCE"));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("%"));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("Montant"));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("TOTAL FACTURE"));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("100%"));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient)));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("TICKET MODERATEUR"));
		table.addCell(cell);
		
		float insuranceRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
		float ticketModer = 100-insuranceRate;
		
		cell = new PdfPCell(fontTitleSelector.process(""+(100-insuranceRate)));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient*ticketModer/100)));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+pb.getBeneficiary().getInsurancePolicy().getInsurance().getName()));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+insuranceRate));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient*insuranceRate/100)));
		table.addCell(cell);
		
		document.add(table);
		
		document.add(new Paragraph("\n\n"));

		// Table of signatures;
		table = new PdfPTable(3);
		table.setWidthPercentage(100f);

		
		cell = new PdfPCell(fontTitleSelector.process("Signature du Patient \n"+ pb.getBeneficiary().getPatient().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
//		List<User> clinicians = new ArrayList<User>();
//		for (Encounter enc : Context.getEncounterService().getEncounters(patient, startDate, endDate)) {
//			User user = Context.getUserService().getUser(enc.getProvider().getPersonId());
//			if(user.hasRole("Clinician"))
//				clinicians.add(user);
//		}
//		log.info("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww "+clinicians.get(0).getPersonName());
		cell = new PdfPCell(fontTitleSelector.process("Signature du Dr \n"));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
			
//		if(pb.getCreator().hasRole("Cashier")){
		cell = new PdfPCell(fontTitleSelector.process("Signature du Caissier\n"+ pb.getCreator().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
//		}

		document.add(table);

		document.close();
		
	}

	  private static void addEmptyLine(Paragraph paragraph, float number) {
	      for (int i = 0; i < number; i++) {
	        paragraph.add(new Paragraph(" "));
	      }
	    }
	}
