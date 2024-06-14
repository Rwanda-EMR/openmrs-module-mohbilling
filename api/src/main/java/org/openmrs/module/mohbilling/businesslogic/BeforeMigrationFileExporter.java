package org.openmrs.module.mohbilling.businesslogic;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BeforeMigrationFileExporter {
	private Log log = LogFactory.getLog(this.getClass());
	
	public void printTransaction(HttpServletRequest request,	HttpServletResponse response, Transaction transaction,String filename) throws Exception{
		 response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		
		Document document = new Document();

	        try {
	            Font font = new Font(FontFamily.COURIER, 6, Font.NORMAL);
	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayTransaction(document, transaction, font);
	            document.add(new Paragraph("\n"));
	           // displayFooter(document,transaction.getPatientAccount().getPatient(), fontselector);
	            displayFooter(document, transaction.getPatientAccount().getPatient(), Context.getAuthenticatedUser(), null, fontselector);
	            document.close(); 

	        } catch (DocumentException e) {
	            e.printStackTrace();
	        } 
	}
	public void printPayment(HttpServletRequest request,HttpServletResponse response, BillPayment payment,Consommation consommation,String filename) throws Exception{
		
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
	           // displayFooter(document,consommation.getBeneficiary().getPatient(), fontselector);
	            displayFooter(document, consommation.getBeneficiary().getPatient(), Context.getAuthenticatedUser(), null, fontselector);
				document.add(new Paragraph("\n"));
				document.add(new Paragraph("\n"));
				String provider="Clinician: "+consommation.getCreator().getPersonName().toString();
				displayFooter(document,provider,fontselector);
	            document.close(); 
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        }
	}
	public void openFile(HttpServletRequest request,HttpServletResponse response,Document document) throws DocumentException, IOException{

 		PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
 		
 		writer.setBoxSize("art", PageSize.A4);
 		HeaderFooter event = new HeaderFooter();
 		writer.setPageEvent(event);
        document.open();  
            
	}
	public void displayHeader(Document document,FontSelector fontSelector) throws Exception{
		Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
		image.scaleToFit(40, 40);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		document.add(fontSelector.process("REPUBLIQUE DU RWANDA                                                        Date:"+df.format(new Date())+"\n"));

		document.add(image);		
		document.add(fontSelector.process(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME)+ "\n"));
        document.add(fontSelector.process(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS)+ "\n"));
        document.add(fontSelector.process(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL)+ "\n"));
	}
	
	public void displayTransaction(Document document,Transaction transaction,Font font) throws DocumentException {
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
	public void displayPaidItems(Document document,BillPayment payment,Consommation consommation,FontSelector fontSelector) throws DocumentException {
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
       		   paidItems= BillPaymentUtil.getOldPayments(payment);
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
			 
			
			String itemDetails = number+")"+itemName +" "+unitPrice +" X "+paidQty+" = "+formatter.format(unitPrice.multiply(paidQty));
		
			/* if(consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
				 itemDetails = number+")"+itemName +" "+formatter.format(unitPrice) +" X "+formatter.format(paidQty)+" = "+formatter.format(unitPrice.multiply(paidQty));
			 }*/
			
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

		for (BillPayment pay : consommation.getPatientBill().getPayments()) {
			if(pay.getVoidReason()==null) {

				totalPaid = totalPaid.add(pay.getAmountPaid());
			}
		}

		BigDecimal totale = new BigDecimal(0);
		BigDecimal dueAmountp = new BigDecimal(0);
		for (PatientServiceBill itemp : consommation.getBillItems())
		{
			if(!itemp.getVoided()) {

				totale = totale.add(itemp.getQuantity().multiply(itemp.getUnitPrice()));
				dueAmountp = totale.multiply(patientRate).divide(new BigDecimal(100));
			}
		}

		//BigDecimal dueAmount =consommation.getPatientBill().getAmount();

		 //if(!consommation.getVoided()&& consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){

			//dueAmount = consommation.getPatientBill().getAmount().multiply(patientRate).divide(new BigDecimal(100));
		 //}
		
		PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: "+formatter.format(dueAmountp)));

		c1.setBorder(Rectangle.NO_BORDER);
		serviceTotPat.addCell(c1);
		
		c1 = new PdfPCell(boldFont.process("Paid: "+payment.getAmountPaid()));
		if(consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
			c1 = new PdfPCell(boldFont.process("Paid: "+formatter.format(payment.getAmountPaid())));
		 }
		c1.setBorder(Rectangle.NO_BORDER);
		serviceTotPat.addCell(c1);
		
		c1 = new PdfPCell(boldFont.process(""));
		c1.setBorder(Rectangle.NO_BORDER);
		serviceTotPat.addCell(c1);
		
		String rest = formatter.format((dueAmountp).subtract(totalPaid));
		/*if(rest.compareTo(BigDecimal.ZERO)<0)
		c1 = new PdfPCell(boldFont.process("Rest: "+0));
		else
			c1 = new PdfPCell(boldFont.process("Rest: "+consommation.getPatientBill().getAmount().subtract(totalPaid)));*/
		c1 = new PdfPCell(boldFont.process("Rest: "+rest));
		c1.setBorder(Rectangle.NO_BORDER);
		serviceTotPat.addCell(c1);
		
		document.add(serviceTotPat);
		
		//displayFooter(document, consommation.getBeneficiary().getPatient(), fontSelector);
		User user = Context.getAuthenticatedUser();
		displayFooter(document,consommation.getBeneficiary().getPatient(), user, null, fontSelector);
	}
	public void displayAllItems(Document document,Consommation consommation,FontSelector fontSelector) throws Exception{
		
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
			//if(sr!=null&& sr.getDueAmount().compareTo(BigDecimal.ZERO)!=0){
				if(sr!=null && sr.getBillItems().size()!=0 ){

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
				 BigDecimal totalByService=new BigDecimal(0);
				 for (PatientServiceBill item : sr.getBillItems()) {
					 if(!item.getVoided()) {
						 cell = new PdfPCell(fontSelector.process("" + df.format(item.getServiceDate())));
						 table.addCell(cell);
						 cell = new PdfPCell(fontSelector.process("" + item.getService().getFacilityServicePrice().getName()));
						 table.addCell(cell);
						 cell = new PdfPCell(fontSelector.process("" + item.getQuantity()));
						 table.addCell(cell);

						 cell = new PdfPCell(fontSelector.process("" + item.getUnitPrice()));
						 table.addCell(cell);
//					 cell = new PdfPCell(fontSelector.process(""+formatter.format(item.getQuantity().multiply(item.getUnitPrice().multiply(patientRate.divide(new BigDecimal(100) ))))));
						 cell = new PdfPCell(fontSelector.process("" + formatter.format(item.getQuantity().multiply(item.getUnitPrice()))));
						 table.addCell(cell);

//					 totalByService=totalByService.add(item.getQuantity().multiply(item.getUnitPrice().multiply(patientRate.divide(new BigDecimal(100) ))));
						 totalByService = totalByService.add(item.getQuantity().multiply(item.getUnitPrice()));
						 total = total.add(item.getQuantity().multiply(item.getUnitPrice()));
					 }
				} 
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(boldFont.process(""+formatter.format(totalByService)));
				 table.addCell(cell);
			}
		}
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 cell = new PdfPCell(fontSelector.process("TOTAL FACTURE"));
		 table.addCell(cell);
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 cell = new PdfPCell(boldFont.process(""+formatter.format(total)));
		 table.addCell(cell);
		 
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
	public void displayFooter(Document document,Patient patient,User user,String other,FontSelector fontselector) throws DocumentException {
		// Table of signatures;
		PdfPTable table1 = new PdfPTable(3);
		table1.setWidthPercentage(100f);
		
		PdfPCell cell = new PdfPCell(fontselector.process(""));
		if(user!=null){
		cell = new PdfPCell(fontselector.process("Cashier Signature \n"+ user.getPersonName()+"\n\n........................."));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);
		}
				
		//for report where a patient has to sign on it
		if(patient!=null){
		cell = new PdfPCell(fontselector.process("                      "));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);	
		
		cell = new PdfPCell(fontselector.process("Beneficiary Signature \n"+patient.getPersonName()+"\n\n........................."));
		cell.setBorder(Rectangle.NO_BORDER);
		table1.addCell(cell);
		}
		
		if(other!=null){
			cell = new PdfPCell(fontselector.process("                      "));
			cell.setBorder(Rectangle.NO_BORDER);
			table1.addCell(cell);	
			
			cell = new PdfPCell(fontselector.process(other+"\n\n........................."));
			cell.setBorder(Rectangle.NO_BORDER);
			table1.addCell(cell);	
		}
		
		document.add(table1);
	}
	public void displayFooter(Document document,String other,FontSelector fontselector) throws DocumentException {
		// Table of signatures;
		PdfPTable table1 = new PdfPTable(2);
		table1.setWidthPercentage(100f);
		
		PdfPCell cell = new PdfPCell(fontselector.process("                      "));
			cell.setBorder(Rectangle.NO_BORDER);
			table1.addCell(cell);	
			
			cell = new PdfPCell(fontselector.process(other+"\n\n........................."));
			cell.setBorder(Rectangle.NO_BORDER);
			table1.addCell(cell);	
		
		document.add(table1);
	}
	public void printGlobalBill(HttpServletRequest request,	HttpServletResponse response, GlobalBill gb,List<ServiceRevenue> sr,String filename) throws Exception{
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		Document document = new Document();

	        try {

	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayServiceRevenues(document, gb,sr, fontselector);
	            document.add(new Paragraph("\n"));
	           // displayFooter(document,gb.getAdmission().getInsurancePolicy().getOwner(), fontselector);
	            User user = Context.getAuthenticatedUser();
	            displayFooter(document, gb.getAdmission().getInsurancePolicy().getOwner(), user, null, fontselector);
		
	            document.close(); 
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        }
	 }
	public void displayServiceRevenues(Document document,GlobalBill gb,List<ServiceRevenue> sr,FontSelector fontSelector) throws DocumentException {
		float[] colsWidt = {5f,20f,55f,15f,25f,25f,25f,25f};
		PdfPTable table = new PdfPTable(colsWidt);
		table.setWidthPercentage(100f);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		//header
		float[] colsWidt1 = { 25f, 25f,25f,25f}; 
		PdfPTable heading2Tab = new PdfPTable(colsWidt1);
		heading2Tab.setWidthPercentage(100f);
		
		PdfPCell head2 = new PdfPCell(fontSelector.process("Card: "+gb.getAdmission().getInsurancePolicy().getInsurance().getName()+"/"+gb.getAdmission().getInsurancePolicy().getInsuranceCardNo()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process("Beneficiary Names: "+gb.getAdmission().getInsurancePolicy().getOwner().getPersonName()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process("DOB: "+df.format(gb.getAdmission().getInsurancePolicy().getOwner().getBirthdate())+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		String admissionMode = "";
		if(gb.getAdmission().getIsAdmitted())
			admissionMode = "Non";
		else
			admissionMode = "Qui";
		
		head2 = new PdfPCell(fontSelector.process("Ambulant: "+admissionMode+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);		
		
		head2 = new PdfPCell(fontSelector.process("Sex: "+gb.getAdmission().getInsurancePolicy().getOwner().getGender()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);	
		
		head2 = new PdfPCell(fontSelector.process("Date d'entree: "+df.format(gb.getAdmission().getAdmissionDate())+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		String dischargeDate = "";
		if(gb.getClosingDate()!=null)
			dischargeDate = df.format(gb.getClosingDate());
		else
			dischargeDate = "-";
			
		head2 = new PdfPCell(fontSelector.process("Date de sortie: "+dischargeDate+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		head2 = new PdfPCell(fontSelector.process(""));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);
		
		document.add(heading2Tab);
		//end header
		
		Chunk chk = new Chunk("FACTURE DES PRESTATIONS DE SOINS DE SANTE #"+gb.getBillIdentifier()+" - "+df.format(gb.getCreatedDate()));
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));
		
		PdfPCell cell = new PdfPCell(boldFont.process("#"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Date "));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Service"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Qty "));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("UP"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("100%"));
		table.addCell(cell);
		
		Float insuranceRate = gb.getAdmission().getInsurancePolicy().getInsurance().getCurrentRate().getRate();

		Float patientRate = 100-insuranceRate;
		
		cell = new PdfPCell(boldFont.process(insuranceRate+"%"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process(patientRate+"%"));
		table.addCell(cell);
		
		NumberFormat formatter = new DecimalFormat("#0.00");
		
		BigDecimal totalBill = new BigDecimal(0);
		
		 for (ServiceRevenue r : sr) {
			 r.getBillItems();
			//if(r!=null&& r.getDueAmount().compareTo(BigDecimal.ZERO)!=0){
			 if(r!=null && r.getBillItems().size()!=0){
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(boldFont.process(""+r.getService()));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 cell = new PdfPCell(fontSelector.process(""));
				 table.addCell(cell);
				 int i=0;
				 BigDecimal totalByCategory = new BigDecimal(0);
				 for (PatientServiceBill item : r.getBillItems()) {
					 i++;
					 if(!item.getVoided()){
					cell = new PdfPCell(fontSelector.process(""+i));
					table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+df.format(item.getServiceDate())));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getService().getFacilityServicePrice().getName()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getQuantity()));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+item.getUnitPrice()));
					 table.addCell(cell);
					 BigDecimal total = item.getQuantity().multiply(item.getUnitPrice());
					 totalBill=totalBill.add(total);
					 cell = new PdfPCell(fontSelector.process(""+formatter.format(total)));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+formatter.format(total.multiply(new BigDecimal(insuranceRate).divide(new BigDecimal(100))))));
					 table.addCell(cell);
					 cell = new PdfPCell(fontSelector.process(""+formatter.format(total.multiply(new BigDecimal(patientRate).divide(new BigDecimal(100))))));
					 table.addCell(cell);
					 
					 totalByCategory=totalByCategory.add(total);
					 }
				} 
					PdfPCell c= new PdfPCell(fontSelector.process(""));
					table.addCell(c);
					c = new PdfPCell(fontSelector.process(""));
					table.addCell(c);
					c = new PdfPCell(fontSelector.process(""));
					table.addCell(c);
					c = new PdfPCell(fontSelector.process(""));
					table.addCell(c);
					c = new PdfPCell(fontSelector.process(""));
					table.addCell(c);
					c = new PdfPCell(boldFont.process(""+formatter.format(totalByCategory)));
					table.addCell(c);
					c = new PdfPCell(boldFont.process(""+formatter.format(totalByCategory.multiply(new BigDecimal(insuranceRate).divide(new BigDecimal(100))))));
					table.addCell(c);
					c = new PdfPCell(boldFont.process(""+formatter.format(totalByCategory.multiply(new BigDecimal(patientRate).divide(new BigDecimal(100))))));
					table.addCell(c);
			}
			
		}
		PdfPCell c= new PdfPCell(fontSelector.process(""));
		table.addCell(c);
		c = new PdfPCell(fontSelector.process(""));
		table.addCell(c);
		c = new PdfPCell(fontSelector.process(""));
		table.addCell(c);
		c = new PdfPCell(fontSelector.process(""));
		table.addCell(c);
		c = new PdfPCell(fontSelector.process(""));
		table.addCell(c);
		c = new PdfPCell(boldFont.process(""+formatter.format(totalBill)));
		table.addCell(c);
		c = new PdfPCell(boldFont.process(""+formatter.format(totalBill.multiply(new BigDecimal(insuranceRate).divide(new BigDecimal(100))))));
		table.addCell(c);
		c = new PdfPCell(boldFont.process(""+formatter.format(totalBill.multiply(new BigDecimal(patientRate).divide(new BigDecimal(100))))));
		table.addCell(c);
		document.add(table);
	}
	public void printCashierReport(HttpServletRequest request,	HttpServletResponse response,BigDecimal amount, List<PaidServiceRevenue> paidServiceRevenues,String filename,User collector,Date startDate,Date endDate) throws Exception{
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		Document document = new Document();

	        try {

	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayPaidServiceRevenues(document, amount,paidServiceRevenues, fontselector,startDate,endDate);
	            document.add(new Paragraph("\n"));
	          //  displayFooter(document,null, fontselector);
	            String chiefCashier = "Chief Cashier's names and Signature";
	            //displayFooter(document, null, Context.getAuthenticatedUser(), chiefCashier, fontselector);
				displayFooter(document, null, collector, chiefCashier, fontselector);

				document.close();
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        }
	 }
	public void printServicesRevenuesReport(HttpServletRequest request,	HttpServletResponse response, List<DepartmentRevenues> departRevenues,String filename) throws Exception{
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		Document document = new Document();

	        try {

	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayDepartRevenues(document, departRevenues, fontselector);
	            document.add(new Paragraph("\n"));
	            String reporter = "Reporter's names and Signature";
	            displayFooter(document, reporter, fontselector);
		
	            document.close(); 
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        }
	 }
	public void displayPaidServiceRevenues(Document document,BigDecimal amount,List<PaidServiceRevenue> paidServiceRevenues,FontSelector fontSelector,Date startDate,Date endDate) throws DocumentException {
		
		Chunk chk = new Chunk("SUMMARIZED CASHIER DAILY REPORT");
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));

		//====================Adding Reporting Period===========
		SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
		Chunk repPeriod = new Chunk("REPORTING PERIOD: From "+df2.format(startDate)+" To "+df2.format(endDate));
		repPeriod.setFont(new Font(FontFamily.COURIER, 6, Font.BOLD));
		Paragraph repPeriodPar = new Paragraph();
		repPeriodPar.add(repPeriod);
		repPeriodPar.setAlignment(Element.ALIGN_LEFT);
		document.add(repPeriodPar);
		document.add(new Paragraph("\n"));

		//===================================================


		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100f);
		
		 NumberFormat formatter = new DecimalFormat("#0.00");
		
		float[] colsWidt = {5f,75f,20f};
		table = new PdfPTable(colsWidt);
		table.setWidthPercentage(100f);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		PdfPCell cell = new PdfPCell(boldFont.process("#"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Service  Name"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Due Amount "));
		table.addCell(cell);

		BigDecimal chargedamount=new BigDecimal(0.0);

		int i=0;
		for (PaidServiceRevenue psr : paidServiceRevenues) {

			i++; 
			if(psr!=null && psr.getPaidAmount().compareTo(BigDecimal.ZERO)!=0){
			 cell = new PdfPCell(fontSelector.process(""+i));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+psr.getService()));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+formatter.format(psr.getPaidAmount())));
			 cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			 table.addCell(cell);
				chargedamount=chargedamount.add(psr.getPaidAmount());
			}
		}

		//======================== Adding charged amount=========================
		cell = new PdfPCell(fontSelector.process(""));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process("Total Due Amount"));
		//cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		cell = new PdfPCell(boldFont.process(""+formatter.format(chargedamount)));
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		table.addCell(cell);
		//=================================================



		cell = new PdfPCell(fontSelector.process(""));
		cell.setBorder(Rectangle.NO_BORDER);
		 table.addCell(cell);
		 
		 cell = new PdfPCell(boldFont.process("Total Received Amount"));
		 //cell.setBorder(Rectangle.NO_BORDER);
		 table.addCell(cell);
		 
		 cell = new PdfPCell(boldFont.process(""+formatter.format(amount)));
		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		 table.addCell(cell);
		
		document.add(table);

	}
	public void displayDepartRevenues(Document document,List<DepartmentRevenues> departRevenues,FontSelector fontSelector) throws DocumentException {
		document.setPageSize(PageSize.A4.rotate());
		
		Chunk chk = new Chunk("SERVICES REPORT");
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		
		
		float[] colsWidt = {15f,45f,45f};
		for (PaidServiceRevenue n : departRevenues.get(0).getPaidServiceRevenues()) {
			colsWidt = Arrays.copyOf(colsWidt, colsWidt.length + 1);
			colsWidt[colsWidt.length - 1] = 60f;
		}
		
		table = new PdfPTable(colsWidt);
		table.setWidthPercentage(100);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 6, Font.BOLD));
		
		PdfPCell cell = new PdfPCell(boldFont.process("#"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Service"));
		table.addCell(cell);
		
		for (PaidServiceRevenue dr : departRevenues.get(0).getPaidServiceRevenues()) {
			String s = dr.getService();
			if(s.length()>3 && s.split(" ").length==1) // if service name is made of letters greater than 3, display the substring from 0 to 4
				s=s.substring(0, 5); 
			else if(s.split(" ").length==2){ // if service name is made of 2 words, display the first word plus the 3 letters of the second
			 String[] parts = s.split(" ");
				s=parts[0]+"."+parts[1].substring(0, 3);
			}
		cell = new PdfPCell(boldFont.process(""+s));
		table.addCell(cell);
		}
		
		cell = new PdfPCell(boldFont.process("Tot "));
		table.addCell(cell);
		
		int i=0;
		for (DepartmentRevenues dr : departRevenues) {
			i++; 
			//if(psr!=null&&psr.getPaidAmount().compareTo(BigDecimal.ZERO)!=0){
			 cell = new PdfPCell(fontSelector.process(""+i));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+dr.getDepartment().getName()));
			 table.addCell(cell);
			 
			 for (PaidServiceRevenue psr : dr.getPaidServiceRevenues()) {
				 cell = new PdfPCell(fontSelector.process(""+psr.getPaidAmount()));
				 table.addCell(cell);
			}
			 cell = new PdfPCell(fontSelector.process(""+dr.getAmount()));
			 table.addCell(cell);
			//}
		}
		
		document.add(table);
	}
	public void printDepositReport(HttpServletRequest request,	HttpServletResponse response, List<Transaction> transactions,String filename) throws Exception{
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name
		Document document = new Document();

	        try {

	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
	    		openFile(request, response, document);
	            displayHeader(document, fontselector);
	            displayTransactions(document, transactions, fontselector);
	            document.add(new Paragraph("\n"));
	            String reporter = "Reported by "+ Context.getAuthenticatedUser()+" "+ Context.getAuthenticatedUser().getPersonName();
	           displayFooter(document, reporter, fontselector);
		
	            document.close(); 
	            
	        } catch (DocumentException e) {
	            e.printStackTrace();
	        }
	 }
	public void displayTransactions(Document document,List<Transaction> transactions,FontSelector fontSelector) throws DocumentException {
		Chunk chk = new Chunk("DEPOSIT REPORT");
		chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100f);
		
		
		float[] colsWidt = {5f,30f,30f,30f,30f,30f};
		
		table = new PdfPTable(colsWidt);
		table.setWidthPercentage(100f);
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		FontSelector boldFont = new FontSelector();
		boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
		
		PdfPCell cell = new PdfPCell(boldFont.process("#"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Date"));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Collector "));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Patient Names "));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Amount "));
		table.addCell(cell);
		
		cell = new PdfPCell(boldFont.process("Reason "));
		table.addCell(cell);
		
		int i=0;
		BigDecimal total = new BigDecimal(0);
		for (Transaction t : transactions) {
			total=total.add(t.getAmount());
			i++; 
			//if(psr!=null&&psr.getPaidAmount().compareTo(BigDecimal.ZERO)!=0){
			 cell = new PdfPCell(fontSelector.process(""+i));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+df.format(t.getTransactionDate())));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+t.getCollector().getPersonName()));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+t.getPatientAccount().getPatient().getPersonName()));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+t.getAmount()));
			 table.addCell(cell);
			 
			 cell = new PdfPCell(fontSelector.process(""+t.getReason()));
			 table.addCell(cell);
			//}
		}
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 cell = new PdfPCell(boldFont.process("TOTAL "));
		 table.addCell(cell);
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 cell = new PdfPCell(boldFont.process(""+total));
		 table.addCell(cell);
		 cell = new PdfPCell(fontSelector.process(""));
		 table.addCell(cell);
		 
		 
		document.add(table);
	}
		//export to excel
		public static void exportData(HttpServletRequest request, HttpServletResponse response, Insurance insurance, List<String> columns,List<AllServicesRevenue> listOfAllServicesRevenue)throws Exception,
			    Exception {
			    
			Date date = new Date();
			SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
				
			PrintWriter op = response.getWriter();
			
			response.setContentType("text/plain");
			response.setHeader("Content-Disposition", "attachment; filename=\"releve_"+f.format(date)+".csv\"");
					
			op.println(""+ Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME));
			op.println(""+ Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS));
			op.println(""+ Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL));
			op.println();
			op.println();

			
			op.println(","+","+","+"SUMMARY OF VOUCHERS FOR "+insurance.getName());
			op.println();
			op.println();
			
			op.print("#,Date,Card NUMBER,AGE,GENDER,BENEFICIARY'S NAMES");
			for (String col : columns) {
				op.print(","+col);
			}
			Float insRate = insurance.getCurrentRate().getRate();
			
			op.print(",100%");
			op.print(","+insRate+"%");
			op.println(","+(100-insRate.floatValue())+"%");
			
			
			int i=0;
			for (AllServicesRevenue asr : listOfAllServicesRevenue) {
				Consommation c = asr.getConsommation();
				Float insuranceRate = asr.getConsommation().getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();

				Float insuranceDue = asr.getAllDueAmounts().floatValue()*insuranceRate/100;

				Float patientDue= asr.getAllDueAmounts().floatValue()*((100-insuranceRate)/100);
				i++;
				op.print(i
						+","+f.format(c.getCreatedDate())
						+","+c.getBeneficiary().getInsurancePolicy().getInsuranceCardNo()
						+","+c.getBeneficiary().getPatient().getAge()
						+","+c.getBeneficiary().getPatient().getGender()
						+","+c.getBeneficiary().getPatient().getPersonName()
						);
				for (ServiceRevenue r : asr.getRevenues()) {
					if(r!=null)
					op.print(","+ ReportsUtil.roundTwoDecimals(r.getDueAmount().floatValue() * 100 / (100 - insuranceRate)));
					else
						op.print(","+0);
				}
				op.print(","+ ReportsUtil.roundTwoDecimals(asr.getAllDueAmounts().doubleValue())+","+ ReportsUtil.roundTwoDecimals(insuranceDue)+","+ ReportsUtil.roundTwoDecimals(patientDue));
				op.println();
			}
			op.println();
			
			op.flush();
			op.close();

        }
	}
