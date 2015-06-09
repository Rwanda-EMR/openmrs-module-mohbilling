/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.MohBillingTagUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
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
		// writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));
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
		Double totalToBePaidByInsurance = 0.0, totalToBePaidOnServiceByInsurance = 0.0;
		Double totalToBePaidByPatient = 0.0, totalToBePaidOnServiceByPatient = 0.0;

		PdfPTable serviceTb = new PdfPTable(2);
		float[] columnWidths = {50f,50f };
		serviceTb.setWidths(columnWidths);
		serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
		serviceTb.setWidthPercentage(100f);
		
		PdfPCell cell = new PdfPCell(fontTitleSelector.process(""));
		for (PatientServiceBill psb : pb.getBillItems()) {

			// initialize total amount to be paid on a service
			totalToBePaidOnService = 0.0;
			totalToBePaidOnServiceByInsurance = 0.0;
			totalToBePaidOnServiceByPatient = 0.0;
			
			Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity();
			totalToBePaidOnService+=serviceCost;
			
			totalToBePaidOnServiceByInsurance = ((totalToBePaidOnService * (pb
					.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate())) / 100);
			totalToBePaidByInsurance += totalToBePaidOnServiceByInsurance;

			totalToBePaidOnServiceByPatient = ((totalToBePaidOnService * (100 - pb
					.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate())) / 100);
			totalToBePaidByPatient += totalToBePaidOnServiceByPatient;
			
			float patientRate = 100 - (pb.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate());
		
			cell = new PdfPCell(fontTitleSelector.process(psb.getService().getFacilityServicePrice().getName()+" "+(psb.getUnitPrice().doubleValue()*patientRate/100)+" x "+psb.getQuantity()+" = "+serviceCost*patientRate/100+"\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			serviceTb.addCell(cell);

		}
		document.add(serviceTb);
		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
		
		PdfPTable serviceTotPat = new PdfPTable(1);
		PdfPCell c = new PdfPCell(boldFont.process("Total: "+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient)));
		c.setBorder(Rectangle.NO_BORDER); 
		serviceTotPat.addCell(c);
		document.add(serviceTotPat);
		
		
		document.add(new Paragraph("....................................................................................................................................................."));
		document.add(new Paragraph("\n"));
		
		
		
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
		//float[] colsWidth1 = { 6f, 10f, 2f, 2f, 2f};
		table = new PdfPTable(5);
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
	  private static void addEmptyLine(Paragraph paragraph, float number) {
	      for (int i = 0; i < number; i++) {
	        paragraph.add(new Paragraph(" "));
	      }
	    }
}
