package org.openmrs.module.mohbilling.businesslogic;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
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
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.AllServicesRevenue;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ServiceRevenue;
import org.openmrs.module.mohbilling.model.Transaction;

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
	private Log log = LogFactory.getLog(this.getClass());
	
	public void printTransaction(HttpServletRequest request,	HttpServletResponse response, Transaction transaction,String filename) throws DocumentException, IOException{
		 Document document = new Document();

	        try {
	            Font font = new Font(Font.FontFamily.COURIER, 6,Font.NORMAL);
	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	            
	            displayHeader(request, response, document, filename, fontselector);
	            displayTransaction(document, transaction, font);
	            document.add(new Paragraph("\n"));	
	            displayFooter(document,transaction.getPatientAccount().getPatient(), fontselector);
		
	            document.close(); // no need to close PDFwriter?

	        } catch (DocumentException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }

	}
	public void printPayment(HttpServletRequest request,	HttpServletResponse response, BillPayment payment,Consommation consommation,String filename) throws IOException{
		 Document document = new Document();

	        try {

	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	            displayHeader(request, response, document, filename, fontselector);
	            displayPaidItems(document, payment, consommation, fontselector);
	            document.add(new Paragraph("............................................................................................................................................................\n"));
	            displayAllItems(document, consommation, fontselector);
	            document.add(new Paragraph("\n"));	
	            displayFooter(document,consommation.getBeneficiary().getPatient(), fontselector);
		
	            document.close(); 
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }


	}
	public void displayHeader(HttpServletRequest request,	HttpServletResponse response,Document document,String filename,FontSelector fontTitle) throws DocumentException, IOException{
		 response.setContentType("application/pdf");
 		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name

 		PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());

 		writer.setBoxSize("art", PageSize.A4);
 		HeaderFooter event = new HeaderFooter();
 		writer.setPageEvent(event);
        document.open();  
		document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)+ "\n"));
        document.add(fontTitle.process(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)+ "\n"));
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)+ "\n"));
            
	}
	
	public void displayTransaction(Document document,Transaction transaction,Font font) throws DocumentException{
		Chunk chk = new Chunk("DEPOSIT RECEIPT");
		chk.setFont(new Font(FontFamily.COURIER, 12, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));	
		
		Paragraph p = new Paragraph();
		p.add(new Paragraph("AMOUNT : "+transaction.getAmount().abs(), font));
		p.add(new Paragraph("RECEIVED FROM : "+transaction.getPatientAccount().getPatient().getPersonName(), font));
		p.add(new Paragraph("FOR : "+transaction.getReason(), font));
		p.add(new Paragraph("RECEIVED BY : "+transaction.getCollector(), font));
		p.add(new Paragraph("RECEIPT No : "+transaction.getTransactionId(), font));
		document.add(p);
	}
	public void displayPaidItems(Document document,BillPayment payment,Consommation consommation,FontSelector fontSelector) throws DocumentException{
			
		Chunk chk = new Chunk("RECU POUR FACTURE #"+consommation.getGlobalBill().getBillIdentifier()+" - "+consommation.getCreatedDate());
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));
		
		float[] colsWidth = { 5f,15f, 60f, 10f, 10f, 10f,10f};
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		
		FontSelector normal = new FontSelector();
		normal.addFont(new Font(FontFamily.COURIER, 8f, Font.NORMAL));
		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		
		// table Header
		PdfPCell cell = new PdfPCell(boldFont.process("No"));
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Date"));
		table.addCell(cell);
				
		cell = new PdfPCell(boldFont.process("Libelle"));
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Billed Qty"));
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Paid Qty"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Unit Price"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Total"));
		table.addCell(cell);
		
		int number=0;
		Double total = 0.0;
		for (PaidServiceBill service: BillPaymentUtil.getPaidItemsByBillPayment(payment)) {	
			number++;
			//total+=pay.getAmountPaid().doubleValue();
			cell = new PdfPCell(fontSelector.process(""+number));
			table.addCell(cell);

			cell = new PdfPCell(fontSelector.process(""+df.format(service.getCreatedDate())));
			table.addCell(cell);
			
			
			cell = new PdfPCell(fontSelector.process(""+service.getBillItem().getService().getFacilityServicePrice().getName()));
			table.addCell(cell);
			
			cell = new PdfPCell(fontSelector.process("" + service.getBillItem().getQuantity()));
			table.addCell(cell);

			cell = new PdfPCell(fontSelector.process("" + service.getPaidQty()));
			table.addCell(cell);
			
			cell = new PdfPCell(fontSelector.process("" + service.getBillItem().getUnitPrice()));
			table.addCell(cell);

			cell = new PdfPCell(fontSelector.process("" + service.getPaidQty().multiply(service.getBillItem().getUnitPrice())));
			table.addCell(cell);
			
			} 				
		document.add(table);
	}
	public void displayAllItems(Document document,Consommation consommation,FontSelector fontSelector) throws DocumentException{
		Chunk chk = new Chunk("FACTURE DES PRESTATIONS DE SOINS DE SANTE #"+consommation.getGlobalBill().getBillIdentifier()+" - "+consommation.getCreatedDate());
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));
		
		float[] colsWidth = { 15f,50f, 15f, 15f, 15f};
		PdfPTable table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		
		FontSelector normal = new FontSelector();
		normal.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		
		// table Header
		PdfPCell cell = new PdfPCell(boldFont.process("Date"));
		table.addCell(cell);
				
		cell = new PdfPCell(boldFont.process("Libelle"));
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Quantity"));
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("P.U"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("P.T"));
		table.addCell(cell);
		
		
		 AllServicesRevenue servicesRevenu = ReportsUtil.getAllServicesRevenue(consommation, "mohbilling.REVENUE");
		 
		 BigDecimal allGlobalAmount = servicesRevenu.getAllDueAmounts();
		 
		 List<ServiceRevenue> revenueList = servicesRevenu.getRevenues();
		 
		  ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.ACTS");
		  revenueList.add(actsRevenue);
		  ServiceRevenue autresRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.AUTRES");
		 revenueList.add(autresRevenue);
		
		 //allGlobalAmount = allGlobalAmount.add(actsRevenue.getDueAmount()); 
		 
		 NumberFormat formatter = new DecimalFormat("#0.00");		 
		 if(revenueList!=null)
		 for (ServiceRevenue sr : revenueList) {
			if(sr!=null){
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(boldFont.process(""+sr.getService()));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);

				 for (PatientServiceBill item : sr.getBillItems()) {
					 cell = new PdfPCell(fontSelector.process(""+df.format(item.getCreatedDate())));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getService().getFacilityServicePrice().getName()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getQuantity()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getUnitPrice()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+formatter.format(item.getQuantity().multiply(item.getUnitPrice()))));
					 table.addCell(cell);
				} 
			}
			
		}
		
		document.add(table);
		document.add(new Paragraph("\n"));	
		float[] width = { 4f, 3f, 3f};
		PdfPTable summaryTable = new PdfPTable(width);
		
		PdfPCell c = new PdfPCell(boldFont.process("Assurances"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(boldFont.process("Montant"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(boldFont.process("%"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process("Total"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(""+consommation.getPatientBill().getAmount().add(consommation.getInsuranceBill().getAmount())));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process("100%"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process("Ticket Moderateur"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(""+consommation.getPatientBill().getAmount()));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		Float insuranceRate = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
		float patientRate = 100f - insuranceRate;
		
		c = new PdfPCell(fontSelector.process(patientRate+"%"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(""+consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName()));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(""+consommation.getInsuranceBill().getAmount()));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(insuranceRate+"%"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		document.add(summaryTable);
	}
	public void displayFooter(Document document,Patient patient,FontSelector fontselector) throws DocumentException{
		// Table of signatures;
		PdfPTable table1 = new PdfPTable(3);
		table1.setWidthPercentage(100f);
		
		PdfPCell cell = new PdfPCell(fontselector.process("Cashier Signature \n"+ Context.getAuthenticatedUser()+"\n\n........................."));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);
				
		cell = new PdfPCell(fontselector.process("                      "));
		cell.setBorder(Rectangle.NO_BORDER);
				table1.addCell(cell);

		//for report where a patient has to sign on it
		cell = new PdfPCell(fontselector.process("Beneficiary Signature \n"+patient.getPersonName()+"\n\n........................."));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);
		
		document.add(table1);
	}


	}
