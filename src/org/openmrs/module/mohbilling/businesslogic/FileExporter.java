package org.openmrs.module.mohbilling.businesslogic;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
	            response.setContentType("application/pdf");
	    		response.setHeader("Content-Disposition", "attachment; filename=\""+ filename + "\""); // file name

	    		PdfWriter writer = PdfWriter.getInstance(document,response.getOutputStream());

	    		writer.setBoxSize("art", PageSize.A4);
	    		HeaderFooter event = new HeaderFooter();
	    		writer.setPageEvent(event);
	            document.open();

	            Font font = new Font(Font.FontFamily.COURIER, 6,Font.NORMAL);
	            FontSelector fontselector = new FontSelector();
	    		fontselector.addFont(new Font(FontFamily.COURIER, 10, Font.NORMAL));
	            
	            displayHeader(document,fontselector);
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
	public void displayHeader(Document document,FontSelector fontTitle) throws DocumentException, IOException{
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
