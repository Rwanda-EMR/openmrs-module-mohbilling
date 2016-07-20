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
 * @author rbcemr
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
}}