package org.openmrs.module.mohbilling.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.RecoveryReport;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.web.controller.MohBillingCohortBuilderFormController.HeaderFooter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class MohBillingReportRecovery extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		BillingService billingService = Context
				.getService(BillingService.class);
		//RecoveryUtil recoveryUtil = new RecoveryUtil();

		mav.addObject("allInsurances", billingService.getAllInsurances());

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = null;
		Date endDate = null;
		Insurance insurance = null;
		String insuranceStr = request.getParameter("insurance"), startDateStr = request
				.getParameter("startDate"), endDateStr = request
				.getParameter("endDate");
		Integer insuranceIdInt = null;
		if (startDateStr != null && !startDateStr.equals("")) {
			startDate = (Date) formatter.parse(startDateStr);
		}

		if (endDateStr != null && !endDateStr.equals("")) {
			endDate = (Date) formatter.parse(endDateStr);

		}

		if (insuranceStr != null && !insuranceStr.equals("")) {
			insuranceIdInt = Integer.parseInt(insuranceStr);
			insurance = billingService.getInsurance(insuranceIdInt);
			mav.addObject("insurance", insurance);
		}

		List<RecoveryReport> recoveryReports = new ArrayList<RecoveryReport>();
		float totalAmountTobePaid = 0;
		float totalPaidAmount = 0;
		float totalRemainingAmount = 0;

		/*
		 * if(insurance == null && startDate != null && endDate != null ){//&&
		 * insuranceStr != null && !insuranceStr.equals("")){
		 * 
		 * 
		 * for(Insurance oneInsurance : billingService.getAllInsurances()){
		 * float insuranceDueAmount = new
		 * ReportsUtil().getMonthlyInsuranceDueAmount(oneInsurance, startDate,
		 * endDate, false); float paidAmount =
		 * billingService.getPaidAmountPerInsuranceAndPeriod(oneInsurance ,
		 * startDate, endDate); float remainingAmount = insuranceDueAmount -
		 * paidAmount;
		 * 
		 * RecoveryReport recoveryReport = new RecoveryReport();
		 * recoveryReport.setInsuranceName(oneInsurance.getName());
		 * recoveryReport.setStartDateStr(startDateStr);
		 * recoveryReport.setEndDateStr(endDateStr);
		 * recoveryReport.setInsuranceDueAmount(insuranceDueAmount);
		 * recoveryReport.setPaidAmount(paidAmount);
		 * recoveryReport.setRemainingAmount(remainingAmount);
		 * recoveryReports.add(recoveryReport); totalAmountTobePaid =
		 * totalAmountTobePaid + insuranceDueAmount; totalPaidAmount =
		 * totalPaidAmount + paidAmount; totalRemainingAmount =
		 * totalRemainingAmount + remainingAmount; }
		 * 
		 * if(request.getParameter("print") != null &&
		 * !request.getParameter("print").equals("")){
		 * 
		 * printRecoveryForAllInsurances(request,response,recoveryReports,
		 * totalAmountTobePaid,totalPaidAmount,totalRemainingAmount); } } else
		 */

		if (insurance != null && startDate != null && endDate != null) {
       // log.info("  fffffffffffffffffffffffff "+insurance+"jjjjjjjjjjj"+startDate+"gddddddddddddddddd"+endDateStr);
			List<Recovery> recoveries = billingService
					.getAllPaidAmountPerInsuranceAndPeriod(insurance,
							startDate, endDate);
			// log.info(" ffffffffffff 1 recoveries ffffffffffff "+recoveries+"wwwwwwwwww");
/*			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat monthFormatter = new SimpleDateFormat("MM");*/
			//Date date = (Date)formatter.parse(recoveryReport.getStartDateStr()); 
			 
			for (Recovery recovery : recoveries) {
				float insuranceDueAmount = ReportsUtil
						.getMonthlyInsuranceDueAmount(insurance, recovery
								.getStartPeriod(), recovery.getEndPeriod(),
								false);
				float paidAmount = recovery.getPaidAmount();
				float remainingAmount = insuranceDueAmount - paidAmount;
                //int month =
				if (recoveryReports.size() > 0) {

					for (RecoveryReport oneRecoveryReport : recoveryReports) {

						if (oneRecoveryReport.getStartDateStr().equals(
								recovery.getStartPeriod().toString())
								&& oneRecoveryReport.getEndDateStr().equals(
										recovery.getEndPeriod().toString())) {

							insuranceDueAmount = 0;

							remainingAmount = oneRecoveryReport
									.getRemainingAmount()
									- paidAmount;
						}
					}
				}
				
				RecoveryReport recoveryReport = new RecoveryReport();
				recoveryReport.setInsuranceName(insurance.getName());
				//DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				DateFormat monthFormatter = new SimpleDateFormat("MM");
				
				String month =monthFormatter.format(recovery.getStartPeriod());
				mav.addObject("month",month);
				recoveryReport.setMonth(Integer.parseInt(month));
				
				
				recoveryReport.setStartDateStr(recovery.getStartPeriod()
						.toString());
				recoveryReport
						.setEndDateStr(recovery.getEndPeriod().toString());
				if (insuranceDueAmount != 0) {
					recoveryReport.setInsuranceDueAmount(insuranceDueAmount);
				} else {
					recoveryReport
							.setInsuranceDueAmount(" Same Period as Before");
				}
				recoveryReport.setPaidAmount(paidAmount);
				recoveryReport.setRemainingAmount(remainingAmount);
				recoveryReport.setPaidDate(recovery.getPayementDate()
						.toString());

				totalPaidAmount = totalPaidAmount + paidAmount;
				totalAmountTobePaid = totalAmountTobePaid + insuranceDueAmount;
				totalRemainingAmount = totalAmountTobePaid - totalPaidAmount;

				recoveryReports.add(recoveryReport);

			}

			mav.addObject("totalAmountTobePaid", totalAmountTobePaid);
			mav.addObject("totalPaidAmount", totalPaidAmount);
			mav.addObject("totalRemainingAmount", totalRemainingAmount);
			mav.addObject("recoveryReports", recoveryReports);

			if (request.getParameter("print") != null
					&& !request.getParameter("print").equals("")) {

				printRecoveryForAllInsurances(request, response,
						recoveryReports, totalAmountTobePaid, totalPaidAmount,
						totalRemainingAmount);
			}

			// /mav.addObject("allPaymentsPerInsurance",billingService.getAllPaidAmountPerInsuranceAndPeriod(insurance,
			// startDate, endDate));
			/*
			 * if(billingService.getAllPaidAmountPerInsuranceAndPeriod(insurance,
			 * startDate, endDate).size() > 0)log.info(
			 * "dedededeedededede tttttttttttttttttttttttt      new  yyyyyyyyyyyyyyyyyyyyyy hhhhhhhhhhhhhhhhhh  "
			 * +billingService.getAllPaidAmountPerInsuranceAndPeriod(insurance,
			 * startDate, endDate).get(0).getPaidAmount());
			 */
		}
		mav.addObject("insuranceIdInt", insuranceIdInt);
		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);

		

		return mav;
	}

	private void printRecoveryForAllInsurances(HttpServletRequest request,
			HttpServletResponse response, List<RecoveryReport> recoveryReports,
			float totalAmountTobePaid, float totalPaidAmount,
			float totalRemainingAmount) throws Exception {

		Document document = new Document();

		// List<PatientBill> patientBills =
		// (List<PatientBill>)request.getAttribute("reportedPatientBillsPrint");

		/*
		 * PatientBill pb = null;
		 * 
		 * pb = Context.getService(BillingService.class).getPatientBill(
		 * Integer.parseInt(request.getParameter("patientBills")));
		 * 
		 * String filename = pb.getBeneficiary().getPatient().getPersonName()
		 * .toString().replace(" ", "_"); filename =
		 * pb.getBeneficiary().getPolicyIdNumber().replace(" ", "_") + "_" +
		 * filename + ".pdf";
		 */

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "Recovery Report"); // file name

		
		
		PdfWriter writer = PdfWriter.getInstance(document, response
				.getOutputStream());
		writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));
		writer.setBoxSize("art", PageSize.A4);

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		document.setPageSize(PageSize.A4);
		// document.setPageSize(new Rectangle(0, 0, 2382, 3369));

		document.addAuthor(Context.getAuthenticatedUser().getPersonName()
				.toString());// the name of the author

		FontSelector fontTitle = new FontSelector();
		fontTitle.addFont(new Font(FontFamily.COURIER, 10.0f, Font.BOLD));

		// Report title
		Chunk chk = new Chunk("Printed on : "
				+ (new SimpleDateFormat("dd-MMM-yyyy").format(new Date())));
		chk.setFont(new Font(FontFamily.COURIER, 10.0f, Font.BOLD));
		Paragraph todayDate = new Paragraph();
		todayDate.setAlignment(Element.ALIGN_RIGHT);
		todayDate.add(chk);
		document.add(todayDate);

		document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));
		try {
			Image image = Image
					.getInstance("C://WORKS.BILLING/moh-billing/police.jpg");
			image.setAlignment(Image.ALIGN_LEFT);
			image.setBorder(4900);
			document.add(image);
		} catch (Exception e) {
			log.info("error loading image...... " + e.getMessage());
		}
		document.add(fontTitle.process("POLICE NATIONALE\n"));
		document.add(fontTitle.process("KACYIRU POLICE HOSPITAL\n"));
		document.add(fontTitle.process("B.P. 6183 KIGALI\n"));
		document.add(fontTitle.process("Tél : 584897\n"));
		document.add(fontTitle.process("E-mail : medical@police.gov.rw"));
		// End Report title

		document.add(new Paragraph("\n"));
		chk = new Chunk("Report on Recovery");
		chk.setFont(new Font(FontFamily.COURIER, 10.0f, Font.BOLD));
		chk.setUnderline(0.2f, -2f);
		Paragraph pa = new Paragraph();
		pa.add(chk);
		pa.setAlignment(Element.ALIGN_CENTER);
		document.add(pa);
		document.add(new Paragraph("\n"));

		// title row
		FontSelector fontTitleSelector = new FontSelector();
		fontTitleSelector.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));

		// Table of identification;
		PdfPTable table = null;
		table = new PdfPTable(2);
		table.setWidthPercentage(100f);

		PdfPCell cell = new PdfPCell(fontTitleSelector
				.process("Compagnie d'Assurance : " + 543));
		cell.setBorder(Rectangle.NO_BORDER);
		table.addCell(cell);

		// tableHeader.addCell(table);

		// document.add(tableHeader);

		document.add(new Paragraph("\n"));

		// Table of bill items;
		float[] colsWidth = { 4f, 4f,4f,3f, 6f, 5f, 4f, 4f, 4f };
		table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		BaseColor bckGroundTitle = new BaseColor(170, 170, 170);

		// table Header
		cell = new PdfPCell(fontTitleSelector.process("No"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// ---------------------------------------------------------------------------
		cell = new PdfPCell(fontTitleSelector.process("Insurance Name"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);
		cell = new PdfPCell(fontTitleSelector.process("Month"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);
		cell = new PdfPCell(fontTitleSelector.process("Start period"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);
		
		cell = new PdfPCell(fontTitleSelector.process("End Period"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Amount To Pay"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Paid Amount"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Remaining Amount"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Date"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

		// empty row
		FontSelector fontTotals = new FontSelector();
		fontTotals.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));

		int count = 1;
		for (RecoveryReport recoveryReport : recoveryReports) {

			cell = new PdfPCell(fontselector.process(count + ""));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(recoveryReport
					.getInsuranceName()));
		  	table.addCell(cell);
			
			
			
			  
			 cell = new PdfPCell(fontselector.process(""
					 +recoveryReport.getMonth()));
				table.addCell(cell);
				
			cell = new PdfPCell(fontselector.process(""
					+recoveryReport.getStartDateStr()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(""
					+ recoveryReport.getEndDateStr()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(""
					+ recoveryReport.getInsuranceDueAmount()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(""
					+ recoveryReport.getPaidAmount()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(""
					+ recoveryReport.getRemainingAmount()));
			table.addCell(cell);

			cell = new PdfPCell(fontselector.process(""
					+ recoveryReport.getPaidDate()));
			table.addCell(cell);

			count++;
		}

		/*
		 * // empty row FontSelector fontTotals = new FontSelector();
		 * fontTotals.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
		 */

		cell = new PdfPCell(fontTotals.process("TOTALS"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontselector.process(""));
		table.addCell(cell);

		cell = new PdfPCell(fontselector.process(""));
		table.addCell(cell);

		cell = new PdfPCell(fontselector.process(""));
		table.addCell(cell);

		cell = new PdfPCell(fontTotals.process("" + totalAmountTobePaid));
		table.addCell(cell);

		cell = new PdfPCell(fontTotals.process("" + totalPaidAmount));
		table.addCell(cell);

		cell = new PdfPCell(fontTotals.process("" + totalRemainingAmount));
		table.addCell(cell);

		table.addCell(cell);

		document.add(table);

		document.close();

	}
	/*
	 * static class HeaderFooter extends PdfPageEventHelper { public void
	 * onEndPage(PdfWriter writer, Document document) { Rectangle rect =
	 * writer.getBoxSize("art");
	 * 
	 * Phrase header = new Phrase(String.format("- %d -", writer
	 * .getPageNumber())); header.setFont(new Font(FontFamily.COURIER, 4,
	 * Font.NORMAL));
	 * 
	 * if (document.getPageNumber() > 1) {
	 * ColumnText.showTextAligned(writer.getDirectContent(),
	 * Element.ALIGN_CENTER, header, (rect.getLeft() + rect .getRight()) / 2,
	 * rect.getTop() + 40, 0); }
	 * 
	 * Phrase footer = new Phrase(String.format("- %d -", writer
	 * .getPageNumber())); footer.setFont(new Font(FontFamily.COURIER, 4,
	 * Font.NORMAL));
	 * 
	 * ColumnText.showTextAligned(writer.getDirectContent(),
	 * Element.ALIGN_CENTER, footer, (rect.getLeft() + rect .getRight()) / 2,
	 * rect.getBottom() - 40, 0);
	 * 
	 * }
	 */
}
