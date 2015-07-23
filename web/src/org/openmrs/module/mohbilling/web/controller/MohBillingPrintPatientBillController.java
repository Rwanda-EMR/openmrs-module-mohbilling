/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.MohBillingTagUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
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
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingPrintPatientBillController extends AbstractController {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal
	 *      (javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		if(request.getParameter("type")!=null && !request.getParameter("type").equals(""))
			epsonPrinter(request, response);
		else
			printPatientBillToPDF(request, response);

		return null;
	}

	private void printPatientBillToPDF(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Document document = new Document();

		/** Initializing image to be the logo if any... */
		Image image = Image.getInstance(Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(40, 40);
		
		/** END of Initializing image */

		PatientBill pb = null;

		pb = Context.getService(BillingService.class).getPatientBill(
				Integer.parseInt(request.getParameter("patientBillId")));
		
		PatientInvoice patientInvoice = PatientBillUtil.getPatientInvoice(pb, null);

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

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4);
		// document.setPageSize(new Rectangle(0, 0, 2382, 3369));

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
				.toString());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 9.0f, Font.NORMAL));

		/** ------------- Report title ------------- */
		Font catFont = new Font(Font.FontFamily.COURIER, 8,Font.NORMAL);
		
		
		String insuranceDetails =pb.getBeneficiary().getInsurancePolicy().getInsurance().getName()+
				" Card Nbr: "+ pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo();

         String patientDetails = pb.getBeneficiary().getPatient().getFamilyName()+" "
         + pb.getBeneficiary().getPatient().getGivenName()
         +"( DOB:"+ new SimpleDateFormat("dd-MMM-yyyy").format(pb.getBeneficiary().getPatient().getBirthdate())+")";
		
		
		String topRightMsg = "Printed on : "+(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()))+"\n\n"
		+insuranceDetails+"\n"+patientDetails;	
		
		
		/** logo,name and address */
		String rwanda = "REPUBLIQUE DU RWANDA\n";
//		document.add(image);

		String fname = Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME);
		String fPhysicAddress = Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS);
		String email = Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL);
		
		String topLeftMsg =fname +"\n"+fPhysicAddress+"\n"+email;

		PdfPTable table = new PdfPTable(2);
		float[] colWidths = {30f,30f };
		table.setWidths(colWidths);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setWidthPercentage(100f);

        PdfPCell cell1 = new PdfPCell(new Paragraph(rwanda,catFont));       
        cell1 = new PdfPCell(new Paragraph(topLeftMsg,catFont));
        cell1.setBorder(Rectangle.NO_BORDER);
         
        PdfPCell cell2 = new PdfPCell(new Paragraph(topRightMsg,catFont));
        cell2.setBorder(Rectangle.NO_BORDER);

        
        PdfPCell imageCell = new PdfPCell();
        imageCell.addElement(new Chunk(image, 20, -20));
         
        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(imageCell);

        document.add(table);
		
		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 9, Font.NORMAL));
		
		 
		PdfPTable tableHeader = new PdfPTable(1);
		
		/** ------------- End Report title ------------- */
		tableHeader = new PdfPTable(1);
		tableHeader.setWidthPercentage(100f); 
		document.add(tableHeader);

		document.add(new Paragraph("\n"));

		// Table of bill items;
		float[] colsWidth = { 2f, 15f, 2f, 3.5f, 5f, 5f, 5f };
		table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		BaseColor bckGroundTitle = new BaseColor(255, 255, 255);


		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

		// empty row
		FontSelector fontTotals = new FontSelector();
		fontTotals.addFont(new Font(FontFamily.COURIER, 9, Font.NORMAL));

		Double totalToBePaidOnService = 0.0;


		PdfPTable serviceTb = new PdfPTable(2);
		float[] columnWidths = {50f,50f };
		serviceTb.setWidths(columnWidths);
		serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
		serviceTb.setWidthPercentage(100f);
		
		PdfPCell cell = new PdfPCell(fontTitleSelector.process(""));
		int itemSize= 0;
		float patientRate = 0;
		Double totalToBePaidByInsurance = 0.0;
		Double totalToBePaidByPatient = 0.0;
		for (PatientServiceBill psb : pb.getBillItems()) {
			itemSize++;
			
			// initialize total amount to be paid on a service
//			totalToBePaidOnService = 0.0;
//			totalToBePaidOnServiceByInsurance = 0.0;
//			totalToBePaidOnServiceByPatient = 0.0;
			
			Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity();
			totalToBePaidOnService+=serviceCost;

			
			patientRate = 100 - (pb.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate());
		
			cell = new PdfPCell(fontTitleSelector.process(itemSize+")"+psb.getService().getFacilityServicePrice().getName()+" "+(psb.getUnitPrice().doubleValue()*patientRate/100)+" x "+psb.getQuantity()+" = "+serviceCost*patientRate/100+"\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			serviceTb.addCell(cell);

		}
		totalToBePaidByInsurance = ((totalToBePaidOnService * (pb
				.getBeneficiary().getInsurancePolicy().getInsurance()
				.getCurrentRate().getRate())) / 100);


		totalToBePaidByPatient = ((totalToBePaidOnService * (100 - pb
				.getBeneficiary().getInsurancePolicy().getInsurance()
				.getCurrentRate().getRate())) / 100);
		
		document.add(serviceTb);
		
		// THESE CODES TO FIX: if items are even/impaire the last was not viewed
		PdfPTable evenItemsTable = new PdfPTable(1);
		if(pb.getBillItems().size()==itemSize && itemSize%2==1){
			Set<PatientServiceBill> services = pb.getBillItems();
			PdfPCell c = new PdfPCell(fontTitleSelector.process(""));
			for (PatientServiceBill psb : services) {
				Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity();
				c = new PdfPCell(fontTitleSelector.process(itemSize+")"+psb.getService().getFacilityServicePrice().getName()+" "+(psb.getUnitPrice().doubleValue()*patientRate/100)+" x "+psb.getQuantity()+" = "+serviceCost*patientRate/100+"\n"));
				
			}
			evenItemsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
			c.setBorder(Rectangle.NO_BORDER);
			evenItemsTable.addCell(c);
		}
		document.add(evenItemsTable);

		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
			
		// display total
		Double totalPaid = 0.0;
		
		Set<BillPayment> payments =  pb.getPayments();
		for (BillPayment pay : payments) {
			totalPaid+=pay.getAmountPaid().doubleValue();
		}
		
			PdfPTable serviceTotPat = new PdfPTable(4);
			PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: "+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient)));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
			
			c1 = new PdfPCell(boldFont.process("Paid: "+ReportsUtil.roundTwoDecimals(totalPaid)));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
//			c1 = new PdfPCell(boldFont.process("Insurance: "+ReportsUtil.roundTwoDecimals(totalToBePaidByInsurance)));
			c1 = new PdfPCell(boldFont.process(""));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
			c1 = new PdfPCell(boldFont.process("Rest: "+MohBillingTagUtil.getTotalAmountNotPaidByPatientBill(pb.getPatientBillId())));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
			document.add(serviceTotPat);
		
		
		document.add(new Paragraph(".....................................................................................................................................................\n"));
		//document.add(new Paragraph("\n"));
		
		
		
/** -------------  print insurance part -----------------------------------------------*/
		
	/** ------------- Report title ------------- */
//		chk = new Chunk("Printed on : "
//				+ (new SimpleDateFormat("dd-MMM-yyyy").format(new Date())));
//		chk.setFont(new Font(FontFamily.COURIER, 10.0f, Font.NORMAL));
//		Paragraph todayDate1 = new Paragraph();
//		todayDate1.setAlignment(Element.ALIGN_RIGHT);
//		todayDate1.add(chk);
//		document.add(todayDate1);

        
		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));
		
		/** I would like a LOGO here!!! */
		Image image1 = Image.getInstance(Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image1.scaleToFit(90, 90);
		
		document.add(image1);
		document.add(fontTitle.process(Context.getAdministrationService()
				.getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)
				+ "\n"));
		document.add(fontTitle
				.process(Context
						.getAdministrationService()
						.getGlobalProperty(
								BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)
						+ "\n"));
		document.add(fontTitle
				.process(Context
						.getAdministrationService()
						.getGlobalProperty(
								BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE)
						+ "\n"));
		document.add(fontTitle.process(Context.getAdministrationService()
				.getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)
				+ "\n"));
		
		/** ------------- End Report title ------------- */

		Chunk chk = new Chunk("FACTURES DES PRESTATIONS DES SOINS DE SANTE");
		chk.setFont(new Font(FontFamily.COURIER, 10, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph par = new Paragraph();
		par.add(chk);
		par.setAlignment(Element.ALIGN_CENTER);
		document.add(par);
		// Table of bill items;
		float[] colsWidth1 = { 3f, 14f, 2f, 2f, 2f};
		table = new PdfPTable(colsWidth1);
		table.setWidthPercentage(100f);
		
		Paragraph header = new Paragraph();
		header.add(new Paragraph("Patient No : "+pb.getBeneficiary().getPatient().getIdentifiers(), catFont));
		addEmptyLine(header, 0);
		header.add(new Paragraph("No de la carte : "+pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo(), catFont));
		addEmptyLine(header, 0);
		header.add(new Paragraph("Names : "+pb.getBeneficiary().getPatient().getPersonName(), catFont));
		addEmptyLine(header, 0);
		document.add(header);
		
		document.add(new Paragraph("\n"));
		
		// table Header
		cell = new PdfPCell();
		cell = new PdfPCell(fontTitleSelector.process("Recording date"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Libelle"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("Qty"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Unit Price"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Tot"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// normal row
		//FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));


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
		//end >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> print insurance part
		

		document.add(new Paragraph("\n\n"));

		// Table of signatures;
		table = new PdfPTable(3);
		table.setWidthPercentage(100f);

		cell = new PdfPCell(
				fontTitleSelector.process("Signature du Patient :\n"
						+ pb.getBeneficiary().getPatient().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell); 
		
		cell = new PdfPCell(fontTitleSelector.process("Signature du Prestataire \n"));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(
				fontTitleSelector.process("Noms et Signature du Caissier\n"
						+ Context.getAuthenticatedUser().getPersonName()));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		document.add(table);

		document.close();

		// Mark the Bill as Printed
		PatientBillUtil.printBill(pb);

	}
	
	private void epsonPrinter(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
//		Document document = new Document();
		  Rectangle pagesize = new Rectangle(216f, 1300f);
	        Document document = new Document(pagesize, 16f, 16f, 0f, 0f);
		
	        //document.setMargins(16, 14, 14, 14);

		/** Initializing image to be the logo if any... */
		Image image = Image.getInstance(Context.getAdministrationService()
				.getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
//		image.setAbsolutePosition(0, 0);
		image.scaleToFit(60, 60);
		/** END of Initializing image */

		PatientBill pb = null;

		pb = Context.getService(BillingService.class).getPatientBill(
				Integer.parseInt(request.getParameter("patientBillId")));

		String filename = pb.getBeneficiary().getPatient().getPersonName()
				.toString().replace(" ", "_");
		filename = pb.getBeneficiary().getPolicyIdNumber().replace(" ", "_")
				+ "_" + filename + ".pdf";

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ filename + "\""); // file name

		PdfWriter writer = PdfWriter.getInstance(document,
				response.getOutputStream());
		 writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));
//		writer.setBoxSize("art", PageSize.A4);
		 
		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);
		writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);
		
		document.open();
		document.setPageSize(PageSize.A4);

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
				.toString());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
		
		FontSelector fontTotals = new FontSelector();
		fontTotals.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

		/** ------------- Report title ------------- */
		
		Chunk chk = new Chunk("Printed on : "
				+ (new SimpleDateFormat("dd-MMM-yyyy").format(new Date())));
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
		Paragraph todayDate = new Paragraph();
		todayDate.setAlignment(Element.ALIGN_RIGHT);
		todayDate.add(chk);
		document.add(todayDate);
		document.add(fontTitle.process("\n"));
		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

		/** I would like a LOGO here!!! */
		document.add(image);
		if(!Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME).equals(""))
		document.add(fontTitle.process(Context.getAdministrationService()
				.getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)
				+ "\n"));
		if(!Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS).equals(""))
		document.add(fontTitle
				.process(Context
						.getAdministrationService()
						.getGlobalProperty(
								BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)
						+ "\n"));
		if(!Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE).equals(""))
		document.add(fontTitle
				.process(Context
						.getAdministrationService()
						.getGlobalProperty(
								BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE)
						+ "\n"));
		if(!Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL).equals(""))
		document.add(fontTitle.process(Context.getAdministrationService()
				.getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)
				+ "\n"));
		
		/** ------------- End Report title ------------- */

		//document.add(new Paragraph("\n"));
		chk = new Chunk("FACTURE");
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));

		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

		PdfPTable tableHeader = new PdfPTable(1);
		tableHeader.setWidthPercentage(100f);
		
		
		String insuranceDetails =pb.getBeneficiary().getInsurancePolicy().getInsurance().getName()+"\n"+
				" Card Nbr: "+ pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo();

         String patientDetails = pb.getBeneficiary().getPatient().getFamilyName()+" "
         + pb.getBeneficiary().getPatient().getGivenName()+"\n"
         +"( DOB:"+ new SimpleDateFormat("dd-MMM-yyyy").format(pb.getBeneficiary().getPatient().getBirthdate())+")";
		
         
         document.add(fontTitle.process(insuranceDetails+"\n"));
         document.add(fontTitle.process(patientDetails+"\n"));
         document.add(fontTitle.process("--------------------\n"));


		// Table of bill items;
		float[] colsWidth = { 2f, 15f, 2f, 3.5f, 5f, 5f, 5f };
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);

		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));


		int ids = 0;
		Double totalToBePaidOnService = 0.0;
		Double totalToBePaidByInsurance = 0.0;
		Double totalToBePaidByPatient = 0.0;
		
		PdfPTable serviceTb = new PdfPTable(1);
		serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
		serviceTb.setWidthPercentage(100);
//		serviceTb.setTotalWidth(document.right() - document.left());

		PdfPCell cell = new PdfPCell(fontTitleSelector.process(""));
		
		for (PatientServiceBill psb : pb.getBillItems()) {
			ids += 1;

			// calculate the total amount 			
			totalToBePaidOnService +=  (psb.getQuantity() * psb.getUnitPrice()
						.doubleValue());

			// single service cost
			Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity();

			
			float patientRate = 100 - (pb.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate());
			
			
			cell = new PdfPCell(fontTitleSelector.process(ids+") "+psb.getService().getFacilityServicePrice().getName()+" "+ReportsUtil.roundTwoDecimals(psb.getUnitPrice().doubleValue()*patientRate/100)+" x "+ReportsUtil.roundTwoDecimals(psb.getQuantity())+" = "+ReportsUtil.roundTwoDecimals(serviceCost*patientRate/100)+"\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			serviceTb.addCell(cell);

		}
		document.add(serviceTb);
		
		document.add(fontTitle.process("--------------------\n"));
		
		
		totalToBePaidByInsurance = totalToBePaidOnService * (pb
				.getBeneficiary().getInsurancePolicy().getInsurance()
				.getCurrentRate().getRate()) / 100;

		totalToBePaidByPatient = totalToBePaidOnService * (100-pb
				.getBeneficiary().getInsurancePolicy().getInsurance()
				.getCurrentRate().getRate()) / 100;

		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		
		Double totalPaid = 0.0;
		
		Set<BillPayment> payments =  pb.getPayments();
		for (BillPayment pay : payments) {
			totalPaid+=pay.getAmountPaid().doubleValue();
		}
		
		
		 document.add(fontTotals.process("Due Amount: "+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient)+"\n"));
		 document.add(fontTotals.process("Paid: "+ReportsUtil.roundTwoDecimals(totalPaid)+"\n"));
		// document.add(fontTotals.process("Insurance: "+ReportsUtil.roundTwoDecimals(totalToBePaidByInsurance)+"\n"));
		 document.add(fontTotals.process("Rest :"+MohBillingTagUtil.getTotalAmountNotPaidByPatientBill(pb.getPatientBillId())));
		

		// Table of signatures;
		table = new PdfPTable(1);
		table.setWidthPercentage(100f);

		cell = new PdfPCell(fontTitleSelector.process("Signature Patient: "+ pb.getBeneficiary().getPatient().getPersonName()+"........\n"));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell); 
				
		cell = new PdfPCell(fontTitleSelector.process("Prestataire:............... \n\n"));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Caissier: "+ Context.getAuthenticatedUser().getPersonName()+"........."));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		document.add(table);

		document.close();

		// Mark the Bill as Printed
		PatientBillUtil.printBill(pb);
	}

	static class HeaderFooter extends PdfPageEventHelper {
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");
			rect.setBorder(Rectangle.BOX);
			rect.setBorderWidth(2);

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
	  private static void addEmptyLine(Paragraph paragraph, float number) {
	      for (int i = 0; i < number; i++) {
	        paragraph.add(new Paragraph(" "));
	      }
	    }
}