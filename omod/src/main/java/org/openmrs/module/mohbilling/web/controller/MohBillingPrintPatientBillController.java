/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.MohBillingTagUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RadioCheckField;

/**
 * @author MOH
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

		try {
			if(request.getParameter("type")!=null && !request.getParameter("type").equals(""))
				epsonPrinter(request, response);
			else
				printPatientBillToPDF(request, response);
		} catch (Exception e) {
			// TODO: handle exception
		}
		

		return null;
	}

	/**
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void printPatientBillToPDF(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Document document = new Document();

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
		FileExporter fexp = new FileExporter();
		float patientRate = fexp.getPatientRate(pb);

		document.open();
		document.setPageSize(PageSize.A4);
		// document.setPageSize(new Rectangle(0, 0, 2382, 3369));

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
				.toString());// the name of the author

		FontSelector fontTitle = (FontSelector) fexp.getFonts().get("NORMAL");

		/** ------------- Report title ------------- */
		Font catFont = new Font(Font.FontFamily.COURIER, 8,Font.NORMAL);
		
		
		String insuranceDetails =pb.getBeneficiary().getInsurancePolicy().getInsurance().getName()+
				" Card Nbr: "+ pb.getBeneficiary().getInsurancePolicy().getInsuranceCardNo();

         String patientDetails = pb.getBeneficiary().getPatient().getFamilyName()+" "
         + pb.getBeneficiary().getPatient().getGivenName()
         +"( DOB:"+ new SimpleDateFormat("dd-MMM-yyyy").format(pb.getBeneficiary().getPatient().getBirthdate())+")";
		
		/** logo,name and address */
		String rwanda = "REPUBLIQUE DU RWANDA\n";
        document.add(fexp.displayPysicalAddress(catFont, pb));
		
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
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);

		Double totalToBePaidOnService = 0.0;


		PdfPTable serviceTb = new PdfPTable(2);
		float[] columnWidths = {50f,50f };
		serviceTb.setWidths(columnWidths);
		serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
		serviceTb.setWidthPercentage(100f);
		
		PdfPCell cell = new PdfPCell(fontTitleSelector.process(""));
		int itemSize= 0;
		for (PatientServiceBill psb : pb.getBillItems()) {
			itemSize++;
			
			Double serviceCost = !psb.getService().getFacilityServicePrice().getCategory().equals("AUTRES")?psb.getUnitPrice().doubleValue()*psb.getQuantity().doubleValue()*patientRate/100:psb.getUnitPrice().doubleValue()*psb.getQuantity().doubleValue();
			totalToBePaidOnService+=serviceCost;

			Double unitPrice = !psb.getService().getFacilityServicePrice().getCategory().equals("AUTRES")?psb.getUnitPrice().doubleValue()*patientRate/100:psb.getUnitPrice().doubleValue();
			
			cell = new PdfPCell(fontTitleSelector.process(itemSize+")"+psb.getService().getFacilityServicePrice().getName()+" "+ReportsUtil.roundTwoDecimals(unitPrice)+" x "+psb.getQuantity()+" = "+ReportsUtil.roundTwoDecimals(serviceCost)+"\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			serviceTb.addCell(cell);

		}
		
		document.add(serviceTb);
		
		// THESE CODES TO FIX: if items are even/impaire the last was not viewed
		PdfPTable evenItemsTable = new PdfPTable(1);
		if(pb.getBillItems().size()==itemSize && itemSize%2==1)
		 evenItemsTable = fexp.displayLastElementIfEvenItems(pb);
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
//			PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: "+ReportsUtil.roundTwoDecimals(totalToBePaidByPatient)));
			PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: "+ReportsUtil.roundTwoDecimals(totalToBePaidOnService)));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
			
			c1 = new PdfPCell(boldFont.process("Paid: "+ReportsUtil.roundTwoDecimals(totalPaid)));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
			c1 = new PdfPCell(boldFont.process(""));
			c1.setBorder(Rectangle.NO_BORDER);
			serviceTotPat.addCell(c1);
			
			c1 = new PdfPCell(boldFont.process("Rest: "+MohBillingTagUtil.getTotalAmountNotPaidByPatientBill(pb.getPatientBillId())));
			c1.setBorder(Rectangle.NO_BORDER); 
			serviceTotPat.addCell(c1);
			
			document.add(serviceTotPat);
		
			PdfPTable cashierTab = new PdfPTable(1);
			cashierTab.setWidthPercentage(100f); 
			c1 = new PdfPCell(fontTitleSelector.process("Name and Signature of Cashier\n"
					+ Context.getAuthenticatedUser().getPersonName()));
			c1.setBorder(Rectangle.NO_BORDER);
			cashierTab.addCell(c1);
			document.add(cashierTab);
		
		document.add(new Paragraph(".....................................................................................................................................................\n"));

		//==================================print services to be paid at 100% ===============================================
		PdfPTable privateServicesTable = new PdfPTable(2);
		float[] privColWidths = {30f,30f };
		privateServicesTable.setWidths(privColWidths);
		privateServicesTable.setHorizontalAlignment(Element.ALIGN_LEFT);
		privateServicesTable.setWidthPercentage(100f);
		
		String topLeftMsg =fexp.getAddress();

		PdfPCell privCell = new PdfPCell(new Paragraph(rwanda,catFont));       
		privCell = new PdfPCell(new Paragraph(topLeftMsg,catFont));
		privCell.setBorder(Rectangle.NO_BORDER);
		privateServicesTable.addCell(privCell);
		         
		String topRightMsgPrivServices = "Printed on : "+(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()))+"\n\n"
				+"Facture No:"+pb.getPatientBillId()+"\n"+patientDetails;	
		privCell = new PdfPCell(new Paragraph(topRightMsgPrivServices,catFont)); 
	    privCell.setBorder(Rectangle.NO_BORDER);
		privateServicesTable.addCell(privCell);

		privCell = new PdfPCell();
	    privCell.addElement(new Chunk(fexp.getImage(), 20, -20));
		privateServicesTable.addCell(privCell);      

//		document.add(privateServicesTable);

		PdfPTable privateItemsTable = new PdfPTable(2);
		float[] width = {30f,30f };
		privateItemsTable.setWidths(width);
		privateItemsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
		privateItemsTable.setWidthPercentage(100f);
				
		Double totalPrivServices = 0.0;
		
		//display private to be paid 100%, items with odd indexes
		int privItemsSize = 0;
		List<PatientServiceBill> privItems = new ArrayList<PatientServiceBill>();
		PdfPCell c = new PdfPCell(fontTitleSelector.process(""));
		for (PatientServiceBill psb : pb.getBillItems()) {
			  if(psb.getService().getFacilityServicePrice().getCategory().equals("AUTRES")){
				privItems.add(psb);
				privItemsSize++;
				
				 Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity().doubleValue();
				 c = new PdfPCell(fontTitleSelector.process(privItemsSize+")"+psb.getService().getFacilityServicePrice().getName()+" "+ReportsUtil.roundTwoDecimals(psb.getUnitPrice().doubleValue())+" x "+psb.getQuantity()+" = "+serviceCost+"\n"));
				 c.setBorder(Rectangle.NO_BORDER);
				 privateItemsTable.addCell(c);
				
				 totalPrivServices+=serviceCost;
			  }
	    }
		 if(privItems.size()!=0)
		 document.add(privateServicesTable);
		 document.add(privateItemsTable);
		 
		//THESE CODES TO FIX: if items are even/impaire the last was not viewed
		 PdfPTable evenItemsTable1 = new PdfPTable(1); 
			if(privItems.size()==privItemsSize && privItemsSize%2==1)
				evenItemsTable1=fexp.displayLastElementIfEvenItems(pb);
				document.add(evenItemsTable1);
			
				
		PdfPTable patientSignTable = new PdfPTable(2);
		patientSignTable.setWidthPercentage(100f); 
		cell = new PdfPCell(
					boldFont.process("Total Services @100%:"
								+totalPrivServices ));
				cell.setBorder(Rectangle.NO_BORDER);
				patientSignTable.addCell(cell); 
				cell = new PdfPCell(
						boldFont.process("Client Name/Sign.:"
								+ pb.getBeneficiary().getPatient().getPersonName()+"............."));
				cell.setBorder(Rectangle.NO_BORDER);
				patientSignTable.addCell(cell); 
				if(privItems.size()!=0){
				document.add(patientSignTable);
		//===============================end printing services to be paid at 100% ========================================
				document.add(new Paragraph(".....................................................................................................................................................\n"));
				}
	/** -------------  print insurance part -----------------------------------------------*/
		
		//=======================SIMPLE HEADING==========================================================
		document.add(fontTitle.process(rwanda)); 
		document.add(fexp.getImage());

		document.add(fexp.displayPysicalAddress(catFont, pb));
		
		Chunk chk = new Chunk("FACTURES DES PRESTATIONS DES SOINS DE SANTE");
		chk.setFont(new Font(FontFamily.COURIER, 10, Font.NORMAL));
		chk.setUnderline(0.2f, -2f);
		Paragraph par = new Paragraph();
		par.add(chk);
		par.setAlignment(Element.ALIGN_CENTER);
		document.add(par);

		table = new PdfPTable(5);
		table.setWidthPercentage(100f);
			
		document.add(new Paragraph("\n"));

		//========================END SIMPLE HEADING=====================================================

		//=======================DISPLAY TABLE FOR RECOVERY PART ITEMS===================================
		FontSelector normalFont = (FontSelector) fexp.getFonts().get("NORMAL");
		document.add(fexp.displayRecoveryPartItems(patientInvoice));
		document.add(new Paragraph("\n"));
		document.add(fexp.displayPercentages(normalFont, patientInvoice));
		//===============================================================================================
		//end >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> print insurance part

		document.add(new Paragraph("\n"));

		////////////////////////////SIMPLE FOOTER
		document.add(fexp.displayFooter(pb,normalFont));		

		document.close();

		// Mark the Bill as Printed
		PatientBillUtil.printBill(pb);

	}
	
	private void epsonPrinter(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
//		Document document = new Document();
		  Rectangle pagesize = new Rectangle(216f, 1300f);
	        Document document = new Document(pagesize, 16f, 16f, 0f, 0f);
	    FileExporter fexp = new FileExporter();
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

		FontSelector fontTitle = (FontSelector) fexp.getFonts().get("NORMAL");
		
		FontSelector fontTotals = (FontSelector) fexp.getFonts().get("BOLD");
		

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
		document.add(fexp.getImage());

		Font catFont = new Font(Font.FontFamily.COURIER, 8,Font.NORMAL);
		
		PdfPTable table = new PdfPTable(2);
		float[] privColWidths = {30f,30f };
		table.setWidths(privColWidths);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setWidthPercentage(100f);
		
		String topLeftMsg =fexp.getAddress();

		PdfPCell cell = new PdfPCell(new Paragraph(fexp.getAddress(),catFont));       
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);
		
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
		table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);

		int ids = 0;
		Double totalToBePaidOnService = 0.0;
		Double totalToBePaidByPatient = 0.0;
		
		PdfPTable serviceTb = new PdfPTable(1);
		serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
		serviceTb.setWidthPercentage(100);

		FontSelector normalFont = (FontSelector) fexp.getFonts().get("NORML");
		PdfPCell c = new PdfPCell(normalFont.process(""));
		
		for (PatientServiceBill psb : pb.getBillItems()) {
			ids += 1;

			// calculate the total amount 			
			totalToBePaidOnService +=  (psb.getQuantity().doubleValue() * psb.getUnitPrice()
						.doubleValue());

			// single service cost
			Double serviceCost = psb.getUnitPrice().doubleValue()*psb.getQuantity().doubleValue();

			
			float patientRate = fexp.getPatientRate(pb);
			
			
			c = new PdfPCell(normalFont.process(ids+") "+psb.getService().getFacilityServicePrice().getName()+" "+ReportsUtil.roundTwoDecimals(psb.getUnitPrice().doubleValue()*patientRate/100)+" x "+ReportsUtil.roundTwoDecimals(psb.getQuantity().doubleValue())+" = "+ReportsUtil.roundTwoDecimals(serviceCost*patientRate/100)+"\n"));
			c.setBorder(Rectangle.NO_BORDER);
			serviceTb.addCell(c);

		}
		document.add(serviceTb);
		
		document.add(fontTitle.process("--------------------\n"));

		totalToBePaidByPatient = totalToBePaidOnService * (100-pb
				.getBeneficiary().getInsurancePolicy().getInsurance()
				.getCurrentRate().getRate()) / 100;
		
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
		document.add(fexp.displayFooter(pb,normalFont));

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


class CheckboxCellEvent implements PdfPCellEvent {
    // The name of the check box field
    protected String name;
    // We create a cell event
    public CheckboxCellEvent(String name) {
        this.name = name;
    }
    // We create and add the check box field
    @Override
    public void cellLayout(PdfPCell cell, Rectangle position,
        PdfContentByte[] canvases) {
        PdfWriter writer = canvases[0].getPdfWriter(); 
        // define the coordinates of the middle
        float x = (position.getLeft() + position.getRight()) / 2;
        float y = (position.getTop() + position.getBottom()) / 2;
        // define the position of a check box that measures 20 by 20
        Rectangle rect = new Rectangle(x - 10, y - 10, x + 10, y + 10);
        // define the check box
        RadioCheckField checkbox = new RadioCheckField(
                writer, rect, name, "Yes");
        // add the check box as a field
        try {
            writer.addAnnotation(checkbox.getCheckField());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
}