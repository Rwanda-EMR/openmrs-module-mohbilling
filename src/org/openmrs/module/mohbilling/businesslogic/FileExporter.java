package org.openmrs.module.mohbilling.businesslogic;

import java.io.IOException;
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
import org.openmrs.module.mohbilling.model.PatientBill;
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
import com.sun.mail.imap.Rights.Right;

public class FileExporter {
	/*private Log log = LogFactory.getLog(this.getClass());

	*//**
	 * gets a list and export it in csv/pdf
	 * 
	 * @param request
	 * @param response
	 * @param res
	 * @param filename
	 * @param title
	 * @throws Exception
	 *//*
	public void exportToCSVFile() throws Exception {
		
  
	}

	*//**
	 * @param request
	 * @param response
	 * @param res
	 * @param filename
	 * @param title
	 * @throws Exception
	 *//*

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

		*//** I would like a LOGO here!!! *//*
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
		*//** Initializing image to be the logo if any... *//*
		Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(40, 40);
		
		*//** END of Initializing image *//*

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

		*//** ------------- Report title ------------- *//*

		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

		
		*//** ------------- End Report title ------------- *//*

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
		
		public PdfPTable displayRecoveryPartItems(){
			
			return null;
			
			
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
		*//**
		 * displays last even item (just one item)
		 * @param psb
		 * @param odd
		 * @return
		 *//*
		public PdfPTable displayLastElementIfEvenItems(PatientBill pb){
			FontSelector font = (FontSelector) getFonts().get("NORMAL");
			PdfPCell c = new PdfPCell(font.process(""));
			int number = 0;
			PdfPTable table = new PdfPTable(1);
			 for (PatientServiceBill psb : pb.getBillItems()) {
				 number++;
				 Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity().doubleValue();
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
		public void pdfPrintRefundReport(HttpServletRequest request, HttpServletResponse response,Set<PatientBill> patientBill, String filename, String title) throws Exception {
			FileExporter fexp = new FileExporter();
			Rectangle pagesize = new Rectangle(360f, 720f);
			Document document = new Document(pagesize, 36f, 72f, 109f, 180f);
		
			//Document document = new Document();
			
			document.setPageSize(PageSize.A4.rotate());
			*//** Initializing image to be the logo if any... *//*
			Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
			image.scaleToFit(40, 40);
			
			*//** END of Initializing image *//*

			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\""	+ filename + "\""); // file name

			PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());
			writer.setBoxSize("art", PageSize.A4.rotate());

			HeaderFooter event = new HeaderFooter();
			writer.setPageEvent(event);
			
			FontSelector font = (FontSelector) getFonts().get("NORMAL");
			FontSelector boldFont = (FontSelector) getFonts().get("BOLD");

			document.open();
			FontSelector fontSelector = (FontSelector) getFonts().get("NORMAL");
			document.add(fontSelector.process("REPUBLIQUE OF RWANDA                                                    Printed on: "+new Date()+"\n"));
			document.add(getImage());
			document.add(fontSelector.process(getAddress()));
			
			Chunk chk = new Chunk("Refund Report");
			chk.setFont(new Font(FontFamily.COURIER, 12, Font.BOLD));
			chk.setUnderline(0.2f, -2f);
			Paragraph pa = new Paragraph();
			pa.add(chk);
			pa.setAlignment(Element.ALIGN_CENTER);
			document.add(pa);
			document.add(new Paragraph("\n"));	
			float[] colsWidth = { 5f,5f, 10f, 10f, 10f, 60f};
			PdfPTable table = new PdfPTable(colsWidth);
			table.setWidthPercentage(100f);
			
			
			PdfPCell cell = new PdfPCell(font.process("#"));
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process("Bill No"));
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process("Beneficiary"));
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process("Creator"));
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process("Created Date"));
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process("Payments Status"));
			table.addCell(cell);
			
			int number=0;
			Double total = 0.0;
			for (PatientBill pb: patientBill) {	
				number++;
				cell = new PdfPCell(font.process(""+number));
				table.addCell(cell);

				cell = new PdfPCell(font.process(""+pb.getPatientBillId()));
				table.addCell(cell);
				
				Patient patient = pb.getBeneficiary().getPatient();
				
				cell = new PdfPCell(font.process(""+patient.getFamilyName()+" "+patient.getGivenName()));
				table.addCell(cell);

				cell = new PdfPCell(font.process("" + pb.getCreator().getFamilyName()+" "+pb.getCreator().getGivenName()));
				table.addCell(cell);

				cell = new PdfPCell(font.process("" +pb.getCreatedDate()));
				table.addCell(cell);
				
				int paymentNbr=0;
				float[] colsWidth2 = { 5f, 10f, 20f, 20f}; 
				PdfPTable payTable = new PdfPTable(colsWidth2);
				PdfPCell c = new PdfPCell(boldFont.process("No"));
				c.setBorder(Rectangle.NO_BORDER);
				payTable.addCell(c);
				c = new PdfPCell(boldFont.process("Paid Amount"));
				c.setBorder(Rectangle.NO_BORDER);
				payTable.addCell(c);
				c = new PdfPCell(boldFont.process("Collector"));
				c.setBorder(Rectangle.NO_BORDER);
				payTable.addCell(c);
				c = new PdfPCell(boldFont.process("Payment Date"));
				c.setBorder(Rectangle.NO_BORDER);
				payTable.addCell(c);
				for (BillPayment pay : pb.getPayments()) {

					if(pay.getAmountPaid().doubleValue()<1)
					total+=pay.getAmountPaid().doubleValue();
					
					paymentNbr++;
					c = new PdfPCell(font.process(paymentNbr+")"));
					c.setBorder(Rectangle.NO_BORDER);
					payTable.addCell(c);
					
					c = new PdfPCell(font.process(""+pay.getAmountPaid()));
					c.setBorder(Rectangle.NO_BORDER);
					payTable.addCell(c);
					
					c = new PdfPCell(font.process(""+pay.getCollector().getFamilyName()+" "+pay.getCollector().getGivenName()));
					c.setBorder(Rectangle.NO_BORDER);
					payTable.addCell(c);
					
					c = new PdfPCell(font.process(""+pay.getCreatedDate()));
					c.setBorder(Rectangle.NO_BORDER);
					payTable.addCell(c);
				}
				table.addCell(payTable);
			} 			
			
//			float[] colsWidth2 = { 6}; 
			PdfPTable tableTotal = new PdfPTable(6);
			PdfPCell c = new PdfPCell(boldFont.process(""));
			c.setBorder(Rectangle.NO_BORDER);
			tableTotal.addCell(c);
			 c = new PdfPCell(boldFont.process(""));
			c.setBorder(Rectangle.NO_BORDER);
			tableTotal.addCell(c);
			 c = new PdfPCell(boldFont.process(""));
			c.setBorder(Rectangle.NO_BORDER);
			tableTotal.addCell(c);
			 c = new PdfPCell(boldFont.process(""));
			c.setBorder(Rectangle.NO_BORDER);
			tableTotal.addCell(c);
			 c = new PdfPCell(boldFont.process(""));
			c.setBorder(Rectangle.NO_BORDER);
			tableTotal.addCell(c);
			
			c = new PdfPCell(boldFont.process(""+Math.abs(total)+" FRW"));
			tableTotal.addCell(c);
//			tableTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			
			document.add(table);
			document.add(tableTotal);
			document.close();
			
		}*/
	  
	}
