package org.openmrs.module.mohbilling.businesslogic;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
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
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)!=null)
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME));
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)!=null)
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS));
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE)!=null)
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE));
		if(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)!=null)
		op.println(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL));

		op.println();
		
		op.println("FACTURE DES SOINS MEDICAUX ");
		op.println();
		
		// display report column names
		op.print("Billing Date,Card Number,Names");
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
		
		op.print(","+","+","+totalConsult+","+totalLabo+","+totalImagery+","+totalActs+","+ReportsUtil.roundTwoDecimals(totalMedica)+","+ReportsUtil.roundTwoDecimals(totalConsomm)+","+totalAmbul+","+totalAutres+","+totalHosp+","+ReportsUtil.roundTwoDecimals(total100)+","+totalTickMod+","+totalRate);
		
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
	public void exportToPDF(HttpServletRequest request,	HttpServletResponse response,PatientInvoice patientInvoice, String filename,
			String title) throws Exception {
		
		PatientBill pb = patientInvoice.getPatientBill();
		Document document =creadPdfHeader(request, response, null, filename, title);
//		document.add(new Paragraph("\n"));
		
		Font catFont = new Font(Font.FontFamily.COURIER, 6,Font.NORMAL);
		Paragraph header = new Paragraph();
		header.add(new Paragraph("Patient No : "+pb.getBeneficiary().getPatient().getIdentifiers(), catFont));
		addEmptyLine(header, 0);
		header.add(new Paragraph("No de la carte : "+pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo(), catFont));
		addEmptyLine(header, 0);
		header.add(new Paragraph("Names : "+pb.getBeneficiary().getPatient().getPersonName(), catFont));
		addEmptyLine(header, 0);
		document.add(header);


		document.add(new Paragraph("\n"));
		
		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));

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

		cell = new PdfPCell(fontTitleSelector.process("Cost"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Qty"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));

		// empty row
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 6, Font.BOLD));

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		for (String key : patientInvoice.getInvoiceMap().keySet()) {
			
			List<Consommation> consommations = patientInvoice.getInvoiceMap().get(key).getConsommationList();			
			if(consommations.size()>0){	
				
			cell = new PdfPCell(fontselector.process(" "));
			table.addCell(cell);
			
			cell = new PdfPCell(boldFont.process(key));
//			cell.setColspan(5);;
			table.addCell(cell);
			
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);			
			
			Double subTotal=patientInvoice.getInvoiceMap().get(key).getSubTotal();
			for (Consommation cons : consommations) {

			cell = new PdfPCell(fontselector.process(""+df.format(cons.getRecordDate())));
			table.addCell(cell);
			
			cell = new PdfPCell(fontselector.process(cons.getLibelle()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process("" + cons.getQuantity()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process("" + cons.getUnitCost()));
			table.addCell(cell);
			
			cell = new PdfPCell(fontselector.process("" + ReportsUtil.roundTwoDecimals(cons.getCost())));
			table.addCell(cell);
		}
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);
			cell = new PdfPCell(fontselector.process(""));
			table.addCell(cell);

		    cell = new PdfPCell(boldFont.process("" + ReportsUtil.roundTwoDecimals(subTotal)));
			table.addCell(cell);		
			
			} //end if 			
			
		}
		
		
		cell = new PdfPCell(fontselector.process(""));
		table.addCell(cell);
		cell = new PdfPCell(boldFont.process("TOTAL FACTURE "));
		table.addCell(cell);
		cell = new PdfPCell(fontselector.process(""));
		table.addCell(cell);
		cell = new PdfPCell(fontselector.process(""));
		table.addCell(cell);
		cell = new PdfPCell(boldFont.process("" + ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount())));
		table.addCell(cell);
		
		document.add(table);

		document.add(new Paragraph("\n"));
		
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
		
		cell = new PdfPCell(fontTitleSelector.process(""+ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount())));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("TICKET MODERATEUR"));
		table.addCell(cell);
		
		float insuranceRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
		float ticketModer = 100-insuranceRate;
		
		cell = new PdfPCell(fontTitleSelector.process(""+(100-insuranceRate)));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount()*ticketModer/100)));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+pb.getBeneficiary().getInsurancePolicy().getInsurance().getName()));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+insuranceRate));
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process(""+ReportsUtil.roundTwoDecimals(patientInvoice.getTotalAmount()*insuranceRate/100)));
		table.addCell(cell);
		
		document.add(table);
		
		document.add(new Paragraph("\n"));

		// Table of signatures;
		table = new PdfPTable(3);
		table.setWidthPercentage(100f);

		
		cell = new PdfPCell(fontTitleSelector.process("Signature du Patient \n"+ pb.getBeneficiary().getPatient().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		

		cell = new PdfPCell(fontTitleSelector.process("Signature du Prestataire \n"));
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
	public void printCashierReport(HttpServletRequest request,	HttpServletResponse response,LinkedHashMap<String, Map<String, Double>> basedDateReport, String filename, String title) throws Exception {

		
		Document document =creadPdfHeader(request, response, basedDateReport, filename, title);
		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 7, Font.BOLD));

		// Table of bill items;
		float[] colsWidth = {9f,9f,7f,9f,9f,9f,7f,9f,10f,9f,9f,9f,9f,9f,10f,9f,9f,9f,10f,10f,9f,8f};
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

		/** I would like a LOGO here!!! */
		document.add(image);		
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService()
						.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)+ "\n"));
		
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


	  

	  
	}
