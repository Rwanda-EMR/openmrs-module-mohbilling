package org.openmrs.module.mohbilling.businesslogic;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.AllServicesRevenue;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ServiceRevenue;
import org.openmrs.module.mohbilling.model.Transaction;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class FileExporter {
	private Log log = LogFactory.getLog(this.getClass());
	
	public void printTransaction(HttpServletRequest request,	HttpServletResponse response, Transaction transaction,String filename) throws DocumentException, IOException{
		 response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		
		Document document = new Document();

	        try {
	            Font font = new Font(Font.FontFamily.COURIER, 6,Font.NORMAL);
	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayTransaction(document, transaction, font);
	            document.add(new Paragraph("\n"));	
	            displayFooter(document,transaction.getPatientAccount().getPatient(), fontselector);
		
	            document.close(); 

	        } catch (DocumentException e) {
	            e.printStackTrace();
	        } 
	}
	public void printPayment(HttpServletRequest request,	HttpServletResponse response, BillPayment payment,Consommation consommation,String filename) throws IOException{
		
		 response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		Document document = new Document();

	        try {

	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayPaidItems(document, payment, consommation, fontselector);
	            document.add(new Paragraph("............................................................................................................................................................\n"));
	            displayAllItems(document, consommation, fontselector);
	            document.add(new Paragraph("\n"));	
	            displayFooter(document,consommation.getBeneficiary().getPatient(), fontselector);
		
	            document.close(); 
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        }
	}
	public void openFile(HttpServletRequest request,HttpServletResponse response,Document document) throws DocumentException, IOException{

 		PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());
 		
 		writer.setBoxSize("art", PageSize.A4);
 		HeaderFooter event = new HeaderFooter();
 		writer.setPageEvent(event);
        document.open();  
            
	}
	public void displayHeader(Document document,FontSelector fontSelector) throws APIException, DocumentException{ 
		document.add(fontSelector.process(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)+ "\n"));
        document.add(fontSelector.process(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)+ "\n"));
        document.add(fontSelector.process(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)+ "\n")); 
	}
	
	public void displayTransaction(Document document,Transaction transaction,Font font) throws DocumentException{
		Chunk chk = new Chunk("RECU POUR CAUTION #"+transaction.getTransactionId());
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));	
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100f);
		
		
		FontSelector normal = new FontSelector();
		normal.addFont(new Font(FontFamily.COURIER, 8f, Font.NORMAL));
		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		
		PdfPTable tableLeft = new PdfPTable(1);
		tableLeft.setHorizontalAlignment(Element.ALIGN_LEFT);

		PdfPTable tableRight = new PdfPTable(1);
		tableRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
		
		PdfPTable transTable = new PdfPTable(2);		   
		
		PdfPCell c1 = new PdfPCell(normal.process("Amount : "+transaction.getAmount().abs()));
		c1.setBorder(Rectangle.NO_BORDER); 
		tableLeft.addCell(c1);
		
		
		c1 = new PdfPCell(normal.process("Received from :  "+transaction.getPatientAccount().getPatient().getPersonName()));
		c1.setBorder(Rectangle.NO_BORDER); 
		tableLeft.addCell(c1);
		
		c1 = new PdfPCell(normal.process("Received by : "+transaction.getCollector().getPersonName()));
		c1.setBorder(Rectangle.NO_BORDER);
		tableLeft.addCell(c1);
		
		c1 = new PdfPCell(normal.process("For : "+transaction.getReason()));
		c1.setBorder(Rectangle.NO_BORDER); 
		tableRight.addCell(c1);
		
		c1 = new PdfPCell(normal.process("Receipt No : "+transaction.getTransactionId()));
		c1.setBorder(Rectangle.NO_BORDER); 
		tableRight.addCell(c1);
		
		PdfPCell c = new PdfPCell(tableLeft);
		c.setBorder(Rectangle.NO_BORDER);
		transTable.addCell(c);
		
		c = new PdfPCell(tableRight);
		c.setBorder(Rectangle.NO_BORDER); 
		transTable.addCell(c);   
		
		document.add(transTable);
	}
	public void displayPaidItems(Document document,BillPayment payment,Consommation consommation,FontSelector fontSelector) throws DocumentException{
		NumberFormat formatter = new DecimalFormat("#0.00");
		Float insuranceRate = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
		float patRate = 100f - insuranceRate;
		BigDecimal patientRate = new BigDecimal(""+patRate);
		 
		Chunk chk = new Chunk("RECU POUR CONSOMMATION#"+consommation.getConsommationId()+" GB#"+consommation.getGlobalBill().getBillIdentifier()+" - "+consommation.getCreatedDate());
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100f);
		
		
		FontSelector normal = new FontSelector();
		normal.addFont(new Font(FontFamily.COURIER, 8f, Font.NORMAL));
		
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		PdfPCell cell =null;
		
		int number=0;
		
		PdfPTable tableLeft = new PdfPTable(1);
		tableLeft.setHorizontalAlignment(Element.ALIGN_LEFT);

		PdfPTable tableRight = new PdfPTable(1);
		tableRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
		
		String itemName;
		BigDecimal unitPrice,paidQty,itemPaidCost=new BigDecimal(0.0);
		BigDecimal totalToBePaidByPatient = new BigDecimal(0.0);
		BigDecimal totalPaid = new BigDecimal(0.0);
		List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);
		
    	   if(consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
       		   paidItems=BillPaymentUtil.getOldPayments(payment);
       	   }
		 
		for (PaidServiceBill service: paidItems) {	
			number++; 
			
			BigDecimal itemCost = service.getBillItem().getQuantity().multiply(service.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100)));
			totalToBePaidByPatient=totalToBePaidByPatient.add(itemCost);
			
			BigDecimal paid= service.getBillItem().getPaidQuantity().multiply(itemCost);
			//totalPaid=totalPaid.add(paid);
			
			 itemName = service.getBillItem().getService().getFacilityServicePrice().getName();
			 unitPrice = service.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
			 paidQty = service.getPaidQty();
			 itemPaidCost = paid;
			
			String itemDetails = number+")"+itemName +" "+unitPrice +" X "+paidQty+" = "+(unitPrice.multiply(paidQty));
		
			cell = new PdfPCell(fontSelector.process(""+itemDetails));
			cell.setBorder(Rectangle.NO_BORDER);
			if(number%2!=0)tableRight.addCell(cell);else tableLeft.addCell(cell);
		} 					   
		PdfPCell c = new PdfPCell(tableRight);
		c.setBorder(Rectangle.NO_BORDER);
		table.addCell(c);
		
		c = new PdfPCell(tableLeft);
		c.setBorder(Rectangle.NO_BORDER); 
		table.addCell(c);   
		
		document.add(table);
		
		PdfPTable serviceTotPat = new PdfPTable(4);
		PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: "+consommation.getPatientBill().getAmount()));
		 if(consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
			 c1 = new PdfPCell(boldFont.process("Due Amount: "+consommation.getPatientBill().getAmount().multiply(patientRate).divide(new BigDecimal(100))));
		 }
		c1.setBorder(Rectangle.NO_BORDER); 
		serviceTotPat.addCell(c1);
		
		for (BillPayment pay : consommation.getPatientBill().getPayments()) {
			totalPaid=totalPaid.add(pay.getAmountPaid());
		}
		c1 = new PdfPCell(boldFont.process("Paid: "+payment.getAmountPaid()));
		c1.setBorder(Rectangle.NO_BORDER); 
		serviceTotPat.addCell(c1);
		
		c1 = new PdfPCell(boldFont.process(""));
		c1.setBorder(Rectangle.NO_BORDER);
		serviceTotPat.addCell(c1);
		
		String rest = formatter.format((consommation.getPatientBill().getAmount().multiply(patientRate).divide(new BigDecimal(100))).subtract(totalPaid));
		/*if(rest.compareTo(BigDecimal.ZERO)<0)
		c1 = new PdfPCell(boldFont.process("Rest: "+0));
		else
			c1 = new PdfPCell(boldFont.process("Rest: "+consommation.getPatientBill().getAmount().subtract(totalPaid)));*/
		c1 = new PdfPCell(boldFont.process("Rest: "+rest));
		c1.setBorder(Rectangle.NO_BORDER); 
		serviceTotPat.addCell(c1);
		
		document.add(serviceTotPat);
		
		displayFooter(document, consommation.getBeneficiary().getPatient(), fontSelector);

	}
	public void displayAllItems(Document document,Consommation consommation,FontSelector fontSelector) throws DocumentException{
		
		displayHeader(document, fontSelector);
		//HEADING
		
		float[] colsWidt = { 25f, 25f,25f,25f};
		PdfPTable heading2Tab = new PdfPTable(colsWidt);
		heading2Tab.setWidthPercentage(100f);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		PdfPCell head2 = new PdfPCell(fontSelector.process("Card: "+consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName()+"/"+consommation.getBeneficiary().getPolicyIdNumber()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process("Beneficiary Names: "+consommation.getBeneficiary().getPatient().getPersonName()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process("DOB: "+df.format(consommation.getBeneficiary().getPatient().getBirthdate())+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process("Sex: "+consommation.getBeneficiary().getPatient().getGender()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process("Service: "+consommation.getDepartment().getName()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		String admissionMode = "";
		if(consommation.getGlobalBill().getAdmission().getIsAdmitted())
			admissionMode = "Non";
		else
			admissionMode = "Qui";
		
		head2 = new PdfPCell(fontSelector.process("Ambulant: "+admissionMode+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);		
		
		head2 = new PdfPCell(fontSelector.process("Date d'entree: "+df.format(consommation.getGlobalBill().getAdmission().getAdmissionDate())+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		String dischargeDate = "";
		if(consommation.getGlobalBill().getClosingDate()!=null)
			dischargeDate = df.format(consommation.getGlobalBill().getClosingDate());
		else
			dischargeDate = "-";
			
		head2 = new PdfPCell(fontSelector.process("Date de sortie: "+dischargeDate+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		document.add(heading2Tab);
		
		//END HEADING
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
		 
		// BigDecimal allGlobalAmount = servicesRevenu.getAllDueAmounts();
		 List<ServiceRevenue> revenueList = servicesRevenu.getRevenues();
		 
		 ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.IMAGING");
		 revenueList.add(imagingRevenue);
		 
		 ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.ACTS");
		 revenueList.add(actsRevenue);
		   
		 ServiceRevenue autresRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.AUTRES");
		 revenueList.add(autresRevenue);
		 
		 Float insRate = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
		 float patRate = 100f - insRate;
		 BigDecimal patientRate = new BigDecimal(""+patRate);
		 BigDecimal insuranceRate = new BigDecimal(""+insRate);
		 
		 BigDecimal total = new BigDecimal(0);
		 
		 NumberFormat formatter = new DecimalFormat("#0.00");		 
		 if(revenueList!=null)
		 for (ServiceRevenue sr : revenueList) {
			if(sr!=null&& sr.getDueAmount().compareTo(BigDecimal.ZERO)!=0){
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
					 if(!item.isVoided()){
					 cell = new PdfPCell(fontSelector.process(""+df.format(item.getServiceDate())));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getService().getFacilityServicePrice().getName()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getQuantity()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getUnitPrice().multiply(patientRate.divide(new BigDecimal(100)))));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+formatter.format(item.getQuantity().multiply(item.getUnitPrice().multiply(patientRate.divide(new BigDecimal(100) ))))));
					 table.addCell(cell);
					 }
					 total=total.add(item.getQuantity().multiply(item.getUnitPrice()));
				} 
			}
			
		}
		
		document.add(table);
		document.add(new Paragraph("\n"));	
		float[] width = { 4f, 3f, 3f};
		PdfPTable summaryTable = new PdfPTable(width);
		
		PdfPCell c = new PdfPCell(boldFont.process("Assurance"));
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
		
		
		c = new PdfPCell(fontSelector.process(""+formatter.format(total)));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process("100%"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process("Ticket Moderateur"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
//		c = new PdfPCell(fontSelector.process(""+consommation.getPatientBill().getAmount()));
		c = new PdfPCell(fontSelector.process(""+formatter.format(total.multiply(patientRate).divide(new BigDecimal(100)))));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(patientRate+"%"));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(""+consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName()));
		cell.setBorder(Rectangle.NO_BORDER);
		summaryTable.addCell(c);
		
		c = new PdfPCell(fontSelector.process(""+formatter.format(total.multiply(insuranceRate).divide(new BigDecimal(100)))));
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
