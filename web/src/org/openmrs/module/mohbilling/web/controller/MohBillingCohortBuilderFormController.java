/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
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
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class MohBillingCohortBuilderFormController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		BillingService billingService = Context
				.getService(BillingService.class);
		List<ServiceCategory> serviceCategory = billingService.getAllServiceCategories();

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", billingService.getAllInsurances());
		mav.addObject("serviceCategory", serviceCategory);

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = null;

		if (request.getMethod().equalsIgnoreCase("post")) {
			String patientIdStr = request.getParameter("patientId"), insuranceStr = request
					.getParameter("insurance"), startDateStr = request
					.getParameter("startDate"), endDateStr = request
					.getParameter("endDate"), serviceId = request
					.getParameter("serviceId");

			List<PatientBill> reportedPatientBills = new ArrayList<PatientBill>();
			PatientService PatientService = Context.getPatientService();

			Integer insuranceIdInt = null;
			Integer patientId = null;
			Date endDate = null;
			Insurance insurance = null;
			
			FacilityServicePrice facilityService = null;

			if (startDateStr != null && !startDateStr.equals("")) {
				startDate = (Date) formatter.parse(startDateStr);
			}

			if (endDateStr != null && !endDateStr.equals("")) {
				endDate = (Date) formatter.parse(endDateStr);
			}
			/*
			 * Date endDate = null; Insurance insurance = null; String
			 * patientIdStr = null;
			 */

				if (!request.getParameter("patientId").equals(null) && !request.getParameter("patientId").equals("")) {

					patientIdStr = request.getParameter("patientId");
					patientId=Integer.parseInt(patientIdStr);
					mav.addObject("patientIdStr", patientIdStr);
				}
				 
//				if (!request.getParameter("patientIdnew").equals(null)) {
//					patientIdStr = request.getParameter("patientIdnew");
//				}

				/*
				 * String insuranceStr = request.getParameter("insurance"),
				 * startDateStr=request.getParameter("startDate"),
				 * endDateStr=request.getParameter("endDate"),
				 * serviceId=request.getParameter("serviceId");
				 */

				if (request.getParameter("insurance") != null && !request.getParameter("insurance").equals("")) {
					insuranceIdInt = Integer.parseInt(insuranceStr);
					insurance = billingService.getInsurance(insuranceIdInt);
				}

				mav.addObject("startDateStr", startDateStr);
				mav.addObject("endDateStr", endDateStr);
				mav.addObject("serviceId", serviceId);
				mav.addObject("insuranceStr", insuranceStr);
//				mav.addObject("patientName",PatientService.getPatient(patientId).getNames());

				if (serviceId != null && !serviceId.equals(""))
					facilityService = billingService
							.getFacilityServicePrice(Integer
									.parseInt(serviceId));

				// ========================startdate,enddate as parameters=========================
				if (startDate != null && endDate == null && insurance == null && patientId == null) {
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil
							.getPatientBillsInDates(startDate, null);
				}
				if (startDate == null && endDate != null && insurance == null
						&& patientId == null) {
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil
							.getPatientBillsInDates(null, endDate);
				}
				if (startDate != null && endDate != null && insurance == null
						&& patientId == null) {
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil
							.getPatientBillsInDates(startDate, endDate); 
				}

				/*
				 * List<PatientBill> reportedPatientBills = new
				 * ArrayList<PatientBill>();
				 * 
				 * PatientService PatientService= Context.getPatientService();
				 * 
				 * 
				 * Integer insuranceIdInt=null; Integer patientId =null;
				 * FacilityServicePrice facilityService=null;
				 */

				if (startDateStr != null && !startDateStr.equals("")) {
					startDate = (Date) formatter.parse(startDateStr);
				}

				// ========================================startdate,enddate and
				// insurance as parameters============================
				if (startDate != null && endDate == null && insurance != null
						&& patientId == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getMonthlyReportByInsurance(insurance, startDate,
									null,null);
				}
				if (startDate == null && endDate != null && insurance != null
						&& patientId == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getMonthlyReportByInsurance(insurance, null,
									endDate,null);
				}
				if (startDate != null && endDate != null && insurance != null
						&& patientId == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getMonthlyReportByInsurance(insurance, startDate,
									endDate,null);
				}
				if (startDate == null && endDate == null && insurance != null
						&& patientId == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getMonthlyReportByInsurance(insurance, null, null,null);
				}
				if (startDate != null && endDate != null && insurance != null
						&& patientId != null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getMonthlyReportByInsurance(insurance, startDate, endDate,patientId);
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				// ========================================startdate,enddate,insurance and patient as parameters===================
				/*if (startDate != null && endDate == null && insurance == null
						&& patientId != null) {
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil.getBillsByPatient(
							PatientService.getPatient(Integer
									.valueOf(patientIdStr)), startDate, null,
							true);
				}
				if (startDate != null && endDate != null && insurance == null
						&& patientId != null) {
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil.getBillsByPatient(
							PatientService.getPatient(Integer
									.valueOf(patientIdStr)), startDate,
							endDate, true);
				}
				if (startDate != null && endDate != null && insurance == null&& patientId != null) {
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil.getBillsByPatient(PatientService.getPatient(Integer.valueOf(patientIdStr)), startDate,
							endDate, true);
				}
				if (startDate == null && endDate != null && insurance != null&& patientId != null) {
					System.out.println(" i m here in this condition ");
					new PatientBillUtil();
					reportedPatientBills = PatientBillUtil.getBillsByPatient(PatientService.getPatient(Integer.valueOf(patientIdStr)), null,
							endDate, true);
				}

				// ==========================================startdate,enddate,patient,insurance,facilityService===========================
				if (startDate == null && endDate == null && patientId == null
						&& facilityService != null && insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getBillsByServiceCategory(facilityService, null,
									null, null, null);
				}

				if (startDate != null && endDate == null && patientId == null
						&& facilityService != null && insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getBillsByServiceCategory(facilityService,
									startDate, null, null, null);
				}
				if (startDate == null && endDate != null && patientId == null
						&& facilityService != null && insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getBillsByServiceCategory(facilityService, null,
									endDate, null, null);
				}

				if (startDate != null && endDate != null && patientId == null
						&& facilityService != null && insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getBillsByServiceCategory(facilityService,
									startDate, endDate, null, null);
				}

				if (startDate == null && endDate == null && patientId != null
						&& facilityService != null && insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getBillsByServiceCategory(facilityService, null,
									null, PatientService.getPatient(patientId),
									null);
				}

				if (startDate != null && endDate != null && patientId != null
						&& facilityService != null && insurance != null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil
							.getBillsByServiceCategory(facilityService,
									startDate, endDate, PatientService
											.getPatient(patientId), insurance);
				}
				if (facilityService == null && startDate == null && endDate == null && patientId != null
						&&  insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil.getBillsByServiceCategory(null,null, null, PatientService.getPatient(patientId), null);
					
				}
				if (facilityService == null && startDate == null && endDate == null && patientId != null
						&& insurance != null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil.getBillsByServiceCategory(null,null, null, null, insurance);
				}
				if (facilityService == null && startDate == null && endDate != null && patientId != null
						&& insurance == null) {
					new ReportsUtil();
					reportedPatientBills = ReportsUtil.getBillsByServiceCategory(null,null, endDate, PatientService.getPatient(patientId), null);
				}

				
				if (endDateStr != null && !endDateStr.equals("")) {
					endDate = (Date) formatter.parse(endDateStr);
				}

				if (insuranceStr != null && !insuranceStr.equals("")) {
					insuranceIdInt = Integer.parseInt(insuranceStr);
					insurance = billingService.getInsurance(insuranceIdInt);
				}
				if (serviceId != null && !serviceId.equals(""))
					facilityService = billingService
							.getFacilityServicePrice(Integer
									.parseInt(serviceId));
*/
				/*
				 * //=======================================startdate,enddate as
				 * parameters========================================= if
				 * (startDate!=null && endDate==null && insurance==null &&
				 * patientId==null) reportedPatientBills=new
				 * PatientBillUtil().getPatientBillsInDates(startDate,null); if
				 * (startDate==null && endDate!=null && insurance==null &&
				 * patientId==null) reportedPatientBills=new
				 * PatientBillUtil().getPatientBillsInDates(null,endDate); if
				 * (startDate!=null && endDate!=null && insurance==null &&
				 * patientId==null) reportedPatientBills=new
				 * PatientBillUtil().getPatientBillsInDates(startDate,endDate);
				 */

				/*
				 * //========================================startdate,enddate
				 * and insurance as parameters============================ if
				 * (startDate!=null && endDate==null && insurance!=null &&
				 * patientId==null) reportedPatientBills= new
				 * ReportsUtil().getMonthlyReportByInsurance(insurance,
				 * startDate, null); if (startDate==null && endDate!=null &&
				 * insurance!=null && patientId==null) reportedPatientBills= new
				 * ReportsUtil().getMonthlyReportByInsurance(insurance, null,
				 * endDate); if (startDate!=null && endDate!=null &&
				 * insurance!=null && patientId==null) reportedPatientBills= new
				 * ReportsUtil().getMonthlyReportByInsurance(insurance,
				 * startDate, endDate); if (startDate==null && endDate==null &&
				 * insurance!=null && patientId==null) reportedPatientBills= new
				 * ReportsUtil().getMonthlyReportByInsurance(insurance, null,
				 * null);
				 */

				/*
				 * //========================================startdate,enddate,insurance
				 * and patient as parameters=================== if
				 * (startDate!=null && endDate==null && insurance==null &&
				 * patientId!=null ) reportedPatientBills=new
				 * PatientBillUtil().getBillsByPatient
				 * (PatientService.getPatient(
				 * Integer.valueOf(patientIdStr)),startDate,null,true) ; if
				 * (startDate==null && endDate!=null && insurance==null &&
				 * patientId!=null) reportedPatientBills=new
				 * PatientBillUtil().getBillsByPatient
				 * (PatientService.getPatient(
				 * Integer.valueOf(patientIdStr)),null,endDate,true) ; if
				 * (startDate!=null && endDate!=null && insurance==null &&
				 * patientId!=null) reportedPatientBills=new
				 * PatientBillUtil().getBillsByPatient
				 * (PatientService.getPatient(
				 * Integer.valueOf(patientIdStr)),startDate,endDate,true) ;
				 */

				/*
				 * //==========================================startdate,enddate,
				 * patient,insurance,facilityService===========================
				 * if(startDate==null && endDate==null && patientId==null &&
				 * facilityService!=null && insurance==null)
				 * reportedPatientBills=new
				 * ReportsUtil().getBillsByServiceCategory(facilityService,
				 * null, null, null, null);
				 * 
				 * if(startDate!=null && endDate==null && patientId==null &&
				 * facilityService!=null && insurance==null)
				 * reportedPatientBills=new
				 * ReportsUtil().getBillsByServiceCategory(facilityService,
				 * startDate, null, null, null); if(startDate==null &&
				 * endDate!=null && patientId==null && facilityService!=null &&
				 * insurance==null) reportedPatientBills=new
				 * ReportsUtil().getBillsByServiceCategory(facilityService,
				 * null, endDate, null, null);
				 * 
				 * if(startDate!=null && endDate!=null && patientId==null &&
				 * facilityService!=null && insurance==null)
				 * reportedPatientBills=new
				 * ReportsUtil().getBillsByServiceCategory(facilityService,
				 * startDate, endDate, null, null);
				 * 
				 * if(startDate==null && endDate==null && patientId!=null &&
				 * facilityService!=null && insurance==null)
				 * reportedPatientBills=new
				 * ReportsUtil().getBillsByServiceCategory(facilityService,
				 * null, null, PatientService.getPatient(patientId), null);
				 * 
				 * if(startDate!=null && endDate!=null && patientId!=null &&
				 * facilityService!=null && insurance!=null)
				 * reportedPatientBills=new
				 * ReportsUtil().getBillsByServiceCategory(facilityService,
				 * startDate, endDate, PatientService.getPatient(patientId),
				 * insurance);
				 */

				double totalAmount = 0;
				List<Object[]> billObj = new ArrayList<Object[]>();

				for (PatientBill bill : reportedPatientBills) {

					Date serviceDate = null;
					for (PatientServiceBill item : bill.getBillItems()) {
						serviceDate = item.getServiceDate();
					}
					billObj
							.add(new Object[] {
									serviceDate,
									bill.getBeneficiary().getPolicyIdNumber(),
									bill.getBeneficiary().getPatient()
											.getGivenName()
											+ " "
											+ bill.getBeneficiary()
													.getPatient()
													.getFamilyName(),
									bill.getBillItems(),
									bill.getBeneficiary().getInsurancePolicy()
											.getInsurance().getName(),
									bill.getBeneficiary().getInsurancePolicy()
											.getInsurance().getCurrentRate()
											.getRate().doubleValue()
											* bill.getAmount().doubleValue()
											/ 100,
									bill.getAmount().doubleValue()
											- (bill.getBeneficiary()
													.getInsurancePolicy()
													.getInsurance()
													.getCurrentRate().getRate()
													* bill.getAmount()
															.doubleValue() / 100),
									bill.getAmount() });
					totalAmount += bill.getAmount().doubleValue();
				}

				mav.addObject("totalAmount", totalAmount);
				mav.addObject("billObj", billObj);
				mav.addObject("reportedPatientBills", reportedPatientBills);
				mav.addObject("startDate", request.getParameter("startDate"));
				mav.addObject("endDate", request.getParameter("endDate"));
				mav.addObject("insurance", insurance);

				List<String> serviceNames = new ArrayList<String>();
				new ReportsUtil();
			//	log.info("zdfzfd hhhhhhbbbbbbbbbbbbbbbbbbbhhhhhhhhhhhhhhhhhhhhhhhh"+reportedPatientBills);
				Map<String, String> billServiceNames = ReportsUtil
						.getAllBillItems(reportedPatientBills);
				
				
				for (String key : billServiceNames.keySet()) {
					serviceNames.add(billServiceNames.get(key));
				}
                 
				mav.addObject("serviceNames", serviceNames);

				/*
				 * List<String> serviceNames = new ArrayList<String>();
				 * Map<String, String> billServiceNames=new
				 * ReportsUtil().getAllBillItems(reportedPatientBills);
				 */
				for (String key : billServiceNames.keySet()) {
					serviceNames.add(billServiceNames.get(key));
				}

				mav.addObject("serviceNames", serviceNames);

				// if(request.getParameter("print") != null &&
				// !request.getParameter("print").equals("")){

				if (request.getParameter("print") != null
						&& !request.getParameter("print").equals("")) {

					printPatientBillToPDF(request, response,
							reportedPatientBills);
				}

				// }

		}

		mav.setViewName(getViewName());

		return mav;

	}

	private void printPatientBillToPDF(HttpServletRequest request,
			HttpServletResponse response, List<PatientBill> reportedPatientBills)
			throws Exception {
		Document document = new Document();
		// List<PatientBill> patientBills =
		// (List<PatientBill>)request.getAttribute("reportedPatientBillsPrint");

		/*
		 * PatientBill pb = null;
		 * 
		 * pb = Context.getService(BillingService.class).getPatientBill(
		 * Integer.parseInt(request.getParameter("patientBills")));
		 */
		/*
		 * String filename = pb.getBeneficiary().getPatient().getPersonName()
		 * .toString().replace(" ", "_"); filename =
		 * pb.getBeneficiary().getPolicyIdNumber().replace(" ", "_") + "_" +
		 * filename + ".pdf";
		 */
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "report"); // file name

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
		chk = new Chunk("Report on patient bills");
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
		float[] colsWidth = { 4f, 4f, 3f, 6f, 5f, 4f, 4f, 3f };
		table = new PdfPTable(colsWidth);
		table.setWidthPercentage(100f);
		BaseColor bckGroundTitle = new BaseColor(170, 170, 170);

		// table Header
		cell = new PdfPCell(fontTitleSelector.process("No"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// ---------------------------------------------------------------------------
		cell = new PdfPCell(fontTitleSelector.process("Beneficiary"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Gender"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Policy Id Number"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Insurance Name"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Insurance due"));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		cell = new PdfPCell(fontTitleSelector.process("Patient due "));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		/*
		 * cell = new PdfPCell(fontTitleSelector.process("Date "));
		 * cell.setBackgroundColor(bckGroundTitle); table.addCell(cell);
		 */

		cell = new PdfPCell(fontTitleSelector.process("Amount "));
		cell.setBackgroundColor(bckGroundTitle);
		table.addCell(cell);

		// normal row
		FontSelector fontselector = new FontSelector();
		fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

		// empty row
		FontSelector fontTotals = new FontSelector();
		fontTotals.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));

		int ids = 0;
		Double totalToBePaidOnService = 0.0;
		Double totalToBePaidByInsurance = 0.0, totalToBePaidOnServiceByInsurance = 0.0;
		Double totalToBePaidByPatient = 0.0, totalToBePaidOnServiceByPatient = 0.0;

		// ===========================================================
		int count = 1;
		for (PatientBill pb : reportedPatientBills) {
			/*
			 * ids += 1;
			 * 
			 * // initialize total amount to be paid on a service
			 * totalToBePaidOnService = 0.0; totalToBePaidOnServiceByInsurance =
			 * 0.0; totalToBePaidOnServiceByPatient = 0.0;
			 * 
			 * cell = new PdfPCell(fontselector.process(ids + "."));
			 * table.addCell(cell);
			 * 
			 * cell = new
			 * PdfPCell(fontselector.process(pb.getBeneficiary().getPatient
			 * ().getNames().toString())); table.addCell(cell);
			 * 
			 * cell = new PdfPCell(fontselector.process("" +
			 * pb.getBeneficiary().getBeneficiaryId()));
			 * //cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			 * table.addCell(cell);
			 * 
			 * cell = new PdfPCell(fontselector.process("" + pb.getAmount()));
			 * //cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			 * table.addCell(cell);
			 * 
			 * // totalToBePaidOnService = (pb.getQuantity() pb.getUnitPrice()
			 * // .doubleValue());
			 * 
			 * cell = new PdfPCell(fontselector.process("" +
			 * totalToBePaidOnService));
			 * //cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			 * table.addCell(cell);
			 * 
			 * totalToBePaidOnServiceByInsurance = ((totalToBePaidOnService (pb
			 * .getBeneficiary().getInsurancePolicy().getInsurance()
			 * .getCurrentRate().getRate())) / 100); totalToBePaidByInsurance +=
			 * totalToBePaidOnServiceByInsurance; cell = new
			 * PdfPCell(fontselector.process("" +
			 * totalToBePaidOnServiceByInsurance));
			 * cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			 * table.addCell(cell);
			 * 
			 * totalToBePaidOnServiceByPatient = ((totalToBePaidOnService (100 -
			 * pb .getBeneficiary().getInsurancePolicy().getInsurance()
			 * .getCurrentRate().getRate())) / 100); totalToBePaidByPatient +=
			 * totalToBePaidOnServiceByPatient; cell = new
			 * PdfPCell(fontselector.process("" +
			 * totalToBePaidOnServiceByPatient));
			 * cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			 * table.addCell(cell);
			 */

			// BaseColor bckGroundTitle = new BaseColor(170, 170, 170);
			// table Header
			cell = new PdfPCell(fontTitleSelector.process("" + count));

			table.addCell(cell);

			// ----------------------------------------------
			cell = new PdfPCell(fontTitleSelector.process(""
					+ pb.getBeneficiary().getPatient().getPersonName()));

			table.addCell(cell);

			cell = new PdfPCell(fontTitleSelector.process(""
					+ pb.getBeneficiary().getPatient().getGender()));

			table.addCell(cell);

			cell = new PdfPCell(fontTitleSelector.process(""
					+ pb.getBeneficiary().getPolicyIdNumber()));

			table.addCell(cell);

			cell = new PdfPCell(fontTitleSelector.process(""
					+ pb.getBeneficiary().getInsurancePolicy().getInsurance()
							.getName()));

			table.addCell(cell);
			Float a = pb.getBeneficiary().getInsurancePolicy().getInsurance()
					.getCurrentRate().getRate();
			BigDecimal b = pb.getAmount();

			Float bFloat = Float.parseFloat(b.toString());
			Float c = a * bFloat;
			cell = new PdfPCell(fontTitleSelector.process("" + c / 100));

			table.addCell(cell);

			cell = new PdfPCell(fontTitleSelector.process(""
					+ (bFloat - (c / 100))));

			table.addCell(cell);

			// for(PatientServiceBill patientServiceBill :pb.getBillItems())

			/*
			 * cell = new PdfPCell(fontTitleSelector.process(""));
			 * //cell.setBackgroundColor(bckGroundTitle); table.addCell(cell);
			 */

			cell = new PdfPCell(fontTitleSelector.process("" + pb.getAmount()));
			// cell.setBackgroundColor(bckGroundTitle);
			table.addCell(cell);

			// normal row
			// FontSelector fontselector = new FontSelector();
			fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

			// empty row
			// FontSelector fontTotals = new FontSelector();
			fontTotals.addFont(new Font(FontFamily.COURIER, 9, Font.BOLD));
			count++;
		}
		// ================================================================
		table.addCell(cell);

		document.add(table);

		document.close();

	}

	static class HeaderFooter extends PdfPageEventHelper {
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");

			Phrase header = new Phrase(String.format("- %d -", writer
					.getPageNumber()));
			header.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			if (document.getPageNumber() > 1) {
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_CENTER, header, (rect.getLeft() + rect
								.getRight()) / 2, rect.getTop() + 40, 0);
			}

			Phrase footer = new Phrase(String.format("- %d -", writer
					.getPageNumber()));
			footer.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, footer, (rect.getLeft() + rect
							.getRight()) / 2, rect.getBottom() - 40, 0);

		}
	}

}
