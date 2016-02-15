package org.openmrs.module.mohbilling.businesslogic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
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
			HttpServletResponse response, LinkedHashMap<PatientBill, PatientInvoice> map, String filename,String title) throws Exception {
		
        PrintWriter op = response.getWriter();
		
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment; filename=\"releve.csv\"");
		
		Set<String> serviceCategories = null;
		Double insuranceRate = null;
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		
		
		//display Header
//		Image image = Image.getInstance(Context.getAdministrationService()
//				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
//		
//		op.println(image);
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)!="")
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME));
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)!="")
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS));
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE)!="")
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE));
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)!="")
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL));

		op.println();
		
		op.println(","+","+","+","+","+"FACTURE DES SOINS MEDICAUX ");
		op.println();
		
		// display report column names
		op.print("Date,Card Number,Names");
		for (PatientBill pb : map.keySet()) {
			serviceCategories=(Set<String>) map.get(pb).getInvoiceMap().keySet();
			insuranceRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();
	    }
//		String totalCateg_="";
		for (String cat : serviceCategories) {
			op.print(","+cat);
//			totalCateg_=totalCateg_+cat;
//			log.info("dynamicccccccccccccccccccccccc "+totalCateg_);
		}
		op.print(",100%");
		op.print(","+(100-insuranceRate)+"%");
		op.print(","+insuranceRate+"%");
		op.println();
		
		//display content
		Double totalConsult = 0.0;
		Double totalLabo = 0.0;
		Double totalImagery = 0.0;
		Double totalActs = 0.0;
		Double totalMedica = 0.0;
		Double totalConsomm = 0.0;
		Double totalAmbul = 0.0;
		Double totalAutres = 0.0;
		Double totalHosp = 0.0;
		Double total100 = 0.0;
		Double totalTickMod = 0.0;
		Double totalRate = 0.0;
		
		for (PatientBill pb : map.keySet()) {
			Double totalBill=0.0;
			
			op.print(df.format(pb.getCreatedDate())+","+pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo()+","+pb.getBeneficiary().getPatient().getFamilyName()+" "+pb.getBeneficiary().getPatient().getGivenName());
			for (String st : map.get(pb).getInvoiceMap().keySet()) {
			op.print(","+map.get(pb).getInvoiceMap().get(st).getSubTotal());
			totalBill+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
//			String total="total"+"_"+st;
//			log.info("dynamicccccccccccccccccccccccc "+total);
			
			total100+=map.get(pb).getInvoiceMap().get(st).getSubTotal();;
			
			if(st.equals("CONSULTATION"))
			totalConsult+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("LABORATOIRE"))
			totalLabo+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("IMAGERIE"))
			totalImagery+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("ACTS"))
			totalActs+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("MEDICAMENTS"))
			totalMedica+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("CONSOMMABLES"))
			totalConsomm+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("AMBULANCE"))
			totalAmbul+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("AUTRES"))
			totalAutres+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
			if(st.equals("HOSPITALISATION"))
			totalHosp+=map.get(pb).getInvoiceMap().get(st).getSubTotal();
		}
			op.print(","+ ReportsUtil.roundTwoDecimals(totalBill)+","+ ReportsUtil.roundTwoDecimals(totalBill*(100-insuranceRate)/100)+","+ ReportsUtil.roundTwoDecimals(totalBill*insuranceRate)/100);
			op.println();
		}
		totalTickMod = ReportsUtil.roundTwoDecimals(total100*(100-insuranceRate)/100);
		totalRate = ReportsUtil.roundTwoDecimals(total100*(insuranceRate/100));
		
		op.print("TOTAL"+","+","+","+totalConsult+","+totalLabo+","+totalImagery+","+totalActs+","+ReportsUtil.roundTwoDecimals(totalMedica)+","+ReportsUtil.roundTwoDecimals(totalConsomm)+","+totalAmbul+","+totalAutres+","+totalHosp+","+ReportsUtil.roundTwoDecimals(total100)+","+totalTickMod+","+totalRate);
		
		op.flush();
		op.close();
	}

	/**
	 * @param request
	 * @param response
	 * @param res
	 * @param filename
	 * @param title
	 * @throws Exception
	 */
	public void exportPatientBillToPDF(HttpServletRequest request,	HttpServletResponse response,PatientInvoice patientInvoice, String filename,
			String title) throws Exception {
		
		PatientBill pb = patientInvoice.getPatientBill();
		
		Font headingFont = new Font(Font.FontFamily.COURIER, 8,Font.NORMAL);
		FontSelector fontSelector = (FontSelector) getFonts().get("NORMAL");
		FileExporter fexp = new FileExporter();
		
		//create/open a document
		Document document = fexp.makeReportHeader(request, response, pb);
		
		//add report heading
		document.add(fontSelector.process("REPUBLIQUE DU RWANDA\n")); 
		document.add(fexp.getImage());
		
		//add hc physical address
		document.add(fexp.displayPysicalAddress(headingFont, pb));
		
		//report title
		Chunk chk = new Chunk("FACTURES DES PRESTATIONS DES SOINS DE SANTE\n\n");
		chk.setFont(new Font(FontFamily.COURIER, 10, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph par = new Paragraph();
		par.add(chk);
		par.setAlignment(Element.ALIGN_CENTER);
		document.add(par);
		
		//add table of items
		document.add(fexp.displayRecoveryPartItems(patientInvoice));
		document.add(fontSelector.process("\n")); 
		document.add(fexp.displayPercentages(fontSelector, patientInvoice));
		document.add(fontSelector.process("\n"));
		document.add(fexp.displayFooter(pb, fontSelector));
		document.close();
		
	}
	public void printCashierReport(HttpServletRequest request,	HttpServletResponse response,LinkedHashMap<String, Map<String, Double>> basedDateReport, String filename, String title) throws Exception {

		
		Document document =creadPdfHeader(request, response, basedDateReport, filename, title);
		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 7, Font.BOLD));

		// Table of bill items;
		float[] colsWidth = {9f,9f,7f,9f,9f,9f,7f,9f,10f,9f,9f,9f,9f,9f,10f,9f,9f,9f,10f,10f,10f,9f,8f};
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(106f);
		BaseColor bckGroundTitle = new BaseColor(255, 255, 255);
		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));

		// empty row
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 6, Font.BOLD));
		
		List<String> reportColumns = BillingGlobalProperties.getreportColumns();
	//String[] reportColumns = {"DATE","CHIR","CONSOMM","CONSULT","ECHOG","FORMAL","HOSPITAL","KINE","LABO","MATERN","MEDICAM","OXGYENO","OPHTAL","RADIO","SOINS INF","STOMAT","AMBULAN","DOC.LEGAUX","MORGUE","DUE.TOT","RECEIVD","PART.PAID"};
		PdfPCell cell=null;
		for (String colName : reportColumns) {
			
			cell = new PdfPCell(fontTitleSelector.process(colName));
			cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);
			
		}
		
		for (String dateStr : basedDateReport.keySet()) {
			
			cell = new PdfPCell(boldFont.process(dateStr));
//			cell.setColspan(5);
			table.addCell(cell);
			
			Map<String, Double> mappedReport =basedDateReport.get(dateStr);
			
			for (String categStr : mappedReport.keySet()) {
				Double amount =mappedReport.get(categStr);
				
				cell = new PdfPCell(boldFont.process(""+amount));
//				cell.setColspan(5);
				table.addCell(cell);				
				}			
					
		}
		
		document.add(table);
		document.add(new Paragraph("\n"));		
		// Table of signatures;	
		
		//document.add(new Paragraph("\n"));

		// Table of signatures;
		table = new PdfPTable(3);
		table.setWidthPercentage(109f);
				
		cell = new PdfPCell(fontTitleSelector.process("Cashier signature \n"));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		

		cell = new PdfPCell(fontTitleSelector.process("                      "));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
	
			

		cell = new PdfPCell(fontTitleSelector.process("Chief Cashier Signature \n"));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);


		document.add(table);

		document.close();
		
		
		
	}
	public void pdfPrintPaymentsReport(HttpServletRequest request,	HttpServletResponse response,List<BillPayment> payments, String filename, String title) throws Exception {

		Document document = new Document();
		
		filename = filename + ".pdf";

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ filename + "\""); // file name

		PdfWriter writer = PdfWriter.getInstance(document,
				response.getOutputStream());

		writer.setBoxSize("art", PageSize.A4);

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4);
		// document.setPageSize(new Rectangle(0, 0, 2382, 3369));

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
				.toString());// the name of the author
	
		document.setPageSize(PageSize.A4.rotate());
		
		// title row
		FontSelector titleFont = new FontSelector();
		titleFont.addFont(new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD));

		// Define my Table;
		float[] colsWidth = { 0.5f, 2f, 3f, 2f, 2f};
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		BaseColor bckGroundTitle = new BaseColor(255, 255, 255);
		
		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 10, Font.NORMAL));

		// empty row
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 10, Font.BOLD));
		
		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 10, Font.NORMAL));
		
		//diplay lago and address
		
		Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(40, 40);
		
		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

		/** I would like a LOGO here!!! */
		document.add(image);		
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService()
						.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)+ "\n"));
		
		//display other report details
		Chunk chk = new Chunk("DEPOSIT REPORT");
		chk.setFont(new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));	

		// table Header
		PdfPCell cell = new PdfPCell(boldFont.process("No"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Date"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Patient Names"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Collector"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Received Amount"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		int number=0;
		Double total = 0.0;
		for (BillPayment pay: payments) {	
			number++;
			total+=pay.getAmountPaid().doubleValue();
			cell = new PdfPCell(fontselector.process(""+number));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(""+df.format(pay.getDateReceived())));
			table.addCell(cell);
			
			Patient patient = pay.getPatientBill().getBeneficiary().getPatient();
			
			cell = new PdfPCell(fontselector.process(""+patient.getFamilyName()+" "+patient.getGivenName()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process("" + pay.getCollector()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process("" + pay.getAmountPaid()));
			table.addCell(cell);	
			
			} 			
//		document.add(table);
		
//		PdfPTable table1 = new PdfPTable(1);
//		table1.setWidthPercentage(109f);
		
		cell = new PdfPCell(boldFont.process(""));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process(""));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process(""));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process(""));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process(""+ total));
		table.addCell(cell);
		document.add(table);
		
		document.add(new Paragraph("\n\n"));	
		
		// Table of signatures;
		PdfPTable table1 = new PdfPTable(3);
		table1.setWidthPercentage(100f);
		
		cell = new PdfPCell(fontselector.process("Signature du Caissier\n"+ Context.getAuthenticatedUser()+"\n\n........................."));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);
				

		cell = new PdfPCell(fontselector.process("                      "));
		cell.setBorder(Rectangle.NO_BORDER);
				table1.addCell(cell);

		cell = new PdfPCell(fontselector.process("Chief Cashier Signature \n\n........................."));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);



		document.add(table1);

		document.close();
		
	}
	public static Document creadPdfHeader(HttpServletRequest request,	HttpServletResponse response,LinkedHashMap<String, Map<String, Double>> basedDateReport, String filename,
			String title)throws Exception {
		Rectangle pagesize = new Rectangle(360f, 720f);
		Document document = new Document(pagesize, 36f, 72f, 109f, 180f);
	
		//Document document = new Document();
		
		document.setPageSize(PageSize.A4.rotate());
		/** Initializing image to be the logo if any... */
		Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(40, 40);
		
		/** END of Initializing image */

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""	+ filename + "\""); // file name

		PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());
		writer.setBoxSize("art", PageSize.A4.rotate());

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4.rotate());
	
		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));

		/** ------------- Report title ------------- */

		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

		
		/** ------------- End Report title ------------- */

//		document.add(new Paragraph("\n"));
		Chunk chk = new Chunk("Daily cashier report");
		chk.setFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));	
		return document;	
		
	}	

	  private static void addEmptyLine(Paragraph paragraph, float number) {
	      for (int i = 0; i < number; i++) {
	        paragraph.add(new Paragraph(" "));
	      }
	    }

	  public PdfPTable displayFooter(PatientBill pb,FontSelector font){

			PdfPTable table = new PdfPTable(3);
			table.setWidthPercentage(100f);

			PdfPCell cell = new PdfPCell(font.process("Signature Patient: "+ pb.getBeneficiary().getPatient().getPersonName()+"........\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell); 
					
			cell = new PdfPCell(font.process("Prestataire:............... \n\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			cell = new PdfPCell(font.process("Caissier: "+ Context.getAuthenticatedUser().getPersonName()+"........."));
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			return table;
		}
		public PdfPTable displayPysicalAddress(Font font,PatientBill pb) throws DocumentException{
			
			String rwanda = "REPUBLIQUE DU RWANDA\n";
			String topLeftMsg =getAddress();

			PdfPTable table = new PdfPTable(2);
			float[] colWidths = {30f,30f };
			table.setWidths(colWidths);
			table.setHorizontalAlignment(Element.ALIGN_LEFT);
			table.setWidthPercentage(100f);
			
	        PdfPCell cell1 = new PdfPCell(new Paragraph(rwanda,font));       
	        cell1 = new PdfPCell(new Paragraph(topLeftMsg,font));
	        cell1.setBorder(Rectangle.NO_BORDER);
	        
	        String insuranceDetails =pb.getBeneficiary().getInsurancePolicy().getInsurance().getName()+
					" Card Nbr: "+ pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo();

	         String patientDetails = pb.getBeneficiary().getPatient().getFamilyName()+" "
	         + pb.getBeneficiary().getPatient().getGivenName()
	         +"( DOB:"+ new SimpleDateFormat("dd-MMM-yyyy").format(pb.getBeneficiary().getPatient().getBirthdate())+")";
	         
	        String topRightMsg = "Printed on : "+(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()))+"\n\n"
	        			+insuranceDetails+"\n"+patientDetails;
	         
	        PdfPCell cell2 = new PdfPCell(new Paragraph(topRightMsg,font));
	        cell2.setBorder(Rectangle.NO_BORDER);

	        
	        table.addCell(cell1);
	        table.addCell(cell2);
	        
			return table;
		}
		
		public PdfPTable displayRecoveryPartItems(PatientInvoice patientInvoice){
			float[] colsWidth1 = { 3f, 14f, 2f, 2f, 2f};
			PdfPTable table = new PdfPTable(colsWidth1);
			table.setWidthPercentage(100f);
			
			FontSelector boldFont = (FontSelector) getFonts().get("BOLD");
			FontSelector normalFont = (FontSelector) getFonts().get("NORMAL");
			
			// table Header
			PdfPCell cell = new PdfPCell();
			cell = new PdfPCell(boldFont.process("Recording date"));
			//cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);

			cell = new PdfPCell(boldFont.process("Libelle"));
//			cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process("Qty"));
//			cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);

			cell = new PdfPCell(boldFont.process("Unit Price"));
//			cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);

			cell = new PdfPCell(boldFont.process("Tot"));
//			cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			for (String key : patientInvoice.getInvoiceMap().keySet()) {
				List<Consommation> consommations = patientInvoice.getInvoiceMap().get(key).getConsommationList();			
				if(consommations.size()>0){	
					
				cell = new PdfPCell(normalFont.process(" "));
				table.addCell(cell);
				
				cell = new PdfPCell(boldFont.process(key));
				table.addCell(cell);
				
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);
				
				Double subTotal=patientInvoice.getInvoiceMap().get(key).getSubTotal();
				for (Consommation cons : consommations) {

				cell = new PdfPCell(normalFont.process(""+df.format(cons.getRecordDate())));
				table.addCell(cell);
				
				cell = new PdfPCell(normalFont.process(cons.getLibelle()));
				table.addCell(cell);

				cell = new PdfPCell(normalFont.process("" + cons.getQuantity()));
				table.addCell(cell);

				cell = new PdfPCell(normalFont.process("" + cons.getUnitCost()));
				table.addCell(cell);
				
				cell = new PdfPCell(normalFont.process("" + ReportsUtil.roundTwoDecimals(cons.getCost())));
				table.addCell(cell);
			}
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);
				cell = new PdfPCell(normalFont.process(""));
				table.addCell(cell);

			    cell = new PdfPCell(boldFont.process("" + ReportsUtil.roundTwoDecimals(subTotal)));
				table.addCell(cell);		
				
				} 		
			}
			
			cell = new PdfPCell(normalFont.process(""));
			table.addCell(cell);
			cell = new PdfPCell(boldFont.process("TOTAL FACTURE "));
			table.addCell(cell);
			cell = new PdfPCell(normalFont.process(""));
			table.addCell(cell);
			cell = new PdfPCell(normalFont.process(""));
			table.addCell(cell);
			cell = new PdfPCell(boldFont.process("" + ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount())));
			table.addCell(cell);
			
			return table;
		}

		public PdfPTable displayPercentages(FontSelector font,PatientInvoice patientInvoice){
			PdfPTable table = new PdfPTable(3);
			table.setWidthPercentage(70f);
			
			PdfPCell cell = new PdfPCell(font.process("ASSURANCE"));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process("%"));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process("Montant"));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process("TOTAL FACTURE"));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process("100%"));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process(""+ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount())));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process("TICKET MODERATEUR"));
			table.addCell(cell);
			
			float insuranceRate = patientInvoice.getPatientBill().getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
			float ticketModer = 100-insuranceRate;
			
			cell = new PdfPCell(font.process(""+(100-insuranceRate)));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process(""+ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount()*ticketModer/100)));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process(""+patientInvoice.getPatientBill().getBeneficiary().getInsurancePolicy().getInsurance().getName()));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process(""+insuranceRate));
			table.addCell(cell);
			
			cell = new PdfPCell(font.process(""+ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount()*insuranceRate/100)));
			table.addCell(cell);
			return table;
		}
		public HashMap getFonts(){
			HashMap<String, FontSelector> fontsMap = new HashMap<String, FontSelector>();
			
			FontSelector normal = new FontSelector();
			normal.addFont(new Font(FontFamily.COURIER, 9.0f, Font.NORMAL));
			
			FontSelector boldFont = new FontSelector();
			boldFont.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
			
			fontsMap.put("NORMAL", normal);
			fontsMap.put("BOLD",boldFont);
			return fontsMap;
		}
		public String getAddress(){
			String facilityName = Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME);
			String facilityPhysicAddress = Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS);
			String facilityEmail = Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL);
			
			String fullAddress =facilityName +"\n"+facilityPhysicAddress+"\n"+facilityEmail;
			return fullAddress;
		}
		public Image getImage() throws Exception, MalformedURLException, APIException, IOException{
			Image image = Image.getInstance(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
			image.scaleToFit(40, 40);
			return image;
		}
		public Document makeReportHeader(HttpServletRequest request,
				HttpServletResponse response,PatientBill pb) throws DocumentException, IOException{
			Document document = new Document();
			
			String filename = pb.getBeneficiary().getPatient().getPersonName()
					.toString().replace(" ", "_");
			filename = pb.getBeneficiary().getPolicyIdNumber().replace(" ", "_")
					+ "_" + filename + ".pdf";

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ filename + "\""); // file name

			PdfWriter writer = PdfWriter.getInstance(document,
					response.getOutputStream());

			writer.setBoxSize("art", PageSize.A4);

			document.open();
			return document;
		}
		/**
		 * displays last even item (just one item)
		 * @param psb
		 * @param odd
		 * @return
		 */
		public PdfPTable displayLastElementIfEvenItems(PatientBill pb){
			FontSelector font = (FontSelector) getFonts().get("NORMAL");
			PdfPCell c = new PdfPCell(font.process(""));
			int number = 0;
			PdfPTable table = new PdfPTable(1);
			 for (PatientServiceBill psb : pb.getBillItems()) {
				 number++;
				 Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity();
			     c = new PdfPCell(font.process(number+")"+psb.getService().getFacilityServicePrice().getName()+" "+ReportsUtil.roundTwoDecimals(psb.getUnitPrice().doubleValue()*getPatientRate(pb)/100)+" x "+psb.getQuantity()+" = "+serviceCost*getPatientRate(pb)/100+"\n"));
					
			}
			table.setHorizontalAlignment(Element.ALIGN_LEFT);
			c.setBorder(Rectangle.NO_BORDER);
			table.addCell(c); 
			return table;
		}
		public float getPatientRate(PatientBill pb){
			return 100 - (pb.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate());
		}

	  
	}
