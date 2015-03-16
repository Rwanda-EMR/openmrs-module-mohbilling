/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;



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
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * @author Kamonyo
 * 
 */
public class ReportsUtil {

	protected final Log log = LogFactory.getLog(getClass());
	
	public static BillingService getService(){
		
		return Context.getService(BillingService.class);
	}

	/**
	 * The patient Bill (Listing all the received services and their detailed
	 * information: Insurance rate, Patient rate, total amount, paid amount,
	 * rest,...) >> going to the Patient
	 * 
	 * @param beneficiary
	 *            the beneficiary to be checked
	 * 
	 * @return bill the bill matching the beneficiary, null otherwise
	 */
	public static PatientBill getPatientBillByBeneficiary(
			Beneficiary beneficiary, Date date) {

		if (beneficiary != null)
			// if(date!=null)
			for (PatientBill bill : getService().getAllPatientBills())
				if (!bill.isVoided()
						&& bill.getBeneficiary().getBeneficiaryId().intValue() == beneficiary
								.getBeneficiaryId().intValue())
					return bill;

		return null;
	}

	// add patient bill by insurance and period

	/**
	 * The list of all Un/paid bills on a certain date/period >> to the
	 * Accountant
	 * 
	 * @param date
	 *            the service Date to be matched
	 * @param isPaid
	 *            the one to determine whether the Bill is paid or not
	 * @return bills the list of un/paid bills on the specified date
	 */
	public static List<PatientBill> getPaidBills(Date date, Boolean isPaid) {

		List<PatientBill> bills;
		Set<PatientServiceBill> itemBills;

		if (date != null && isPaid != null) {
			bills = new ArrayList<PatientBill>();
			for (PatientBill pb : getService().getAllPatientBills())
				if (!pb.isVoided() && pb.getIsPaid() == isPaid) {
					itemBills = new TreeSet<PatientServiceBill>();

					if (pb.getBillItems() != null)
						for (PatientServiceBill psb : pb.getBillItems()) {
							if (!psb.isVoided()
									&& psb.getServiceDate().compareTo(date) == 0) {
								itemBills.add(psb);
							}
							bills.add(pb);
						}
					pb.setBillItems(itemBills);
				}

			return bills;
		}

		return null;
	}

	/**
	 * The list of services that a patient received and paid during a certain
	 * period >> going to the Data manager/ Accountant/ HC Head
	 * 
	 * @param startDate
	 *            the Start date to be considered as the min boundary
	 * @param endDate
	 *            the End date to be considered as the max boundary
	 * @param isPaid
	 *            the Value that determines whether the services are un/paid, if
	 *            null (no Value is provided) it will match without considering
	 *            this Value <code>isPaid</code>
	 * @return services the lis of un/paid services on a certain period
	 */
	public static List<BillableService> getPaidServices(Date startDate,
			Date endDate, Boolean isPaid) {

		List<BillableService> services = new ArrayList<BillableService>();

		if (getService().getAllPatientBills() != null)
			for (PatientBill pb : getService().getAllPatientBills())
				if (!pb.isVoided())
					if (pb != null && pb.getIsPaid() == isPaid) {

						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								services.add(psb.getService());
					} else if (pb != null) {
						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								services.add(psb.getService());
					} else
						services = null;

		return services;
	}

	/**
	 * The monthly report on all bills (specifying the patients details: the
	 * amount they paid and their respective insurances) >> going to the Chief
	 * Accountant
	 * 
	 * @param startDate
	 *            the start date of the period
	 * @param endDate
	 *            the end date of the period
	 * @param isPaid
	 *            the condition of payment
	 * @return bills the list of matched PatientBill
	 */
	public static List<PatientBill> getMonthlyGeneralBills(Date startDate,
			Date endDate, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills())
			if (isPaid != null) {
				if (!pb.isVoided() && pb.getIsPaid() == isPaid)
					for (PatientServiceBill psb : pb.getBillItems())
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0
								&& psb.getServiceDate().compareTo(endDate) <= 0)
							bills.add(pb);
			} else {
				if (!pb.isVoided())
					for (PatientServiceBill psb : pb.getBillItems())
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0
								&& psb.getServiceDate().compareTo(endDate) <= 0)
							bills.add(pb);
			}

		return bills;
	}

	/**
	 * The monthly report on all patients of same insurance and the amount
	 * un/paid >> going to the Data manager/ Accountant
	 * 
	 * @param insurance
	 *            the insurance of the patients
	 * @param startDate
	 *            the start date of the period
	 * @param endDate
	 *            the end date of the period
	 * @param isPaid
	 *            the condition of payment
	 * @return bills the list of matched PatientBill
	 */
	public static List<PatientBill> getMonthlyBillsByInsurance(
			Insurance insurance, Date startDate, Date endDate, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills())
			if (!pb.isVoided()
					&& pb.getBeneficiary().getInsurancePolicy().getInsurance()
							.getInsuranceId().intValue() == insurance
							.getInsuranceId().intValue()
					&& pb.getIsPaid() == isPaid)
				for (PatientServiceBill psb : pb.getBillItems())
					if (!psb.isVoided()
							&& psb.getServiceDate().compareTo(startDate) >= 0
							& psb.getServiceDate().compareTo(endDate) <= 0)
						bills.add(pb);

		return bills;
	}

	/**
	 * 
	 * gets all bill services' names and keep each once >>>>>>>Map<String,
	 * String>
	 * 
	 * @param patientsBills
	 * @return item names
	 */
	public static Map<String, String> getAllBillItems(
			List<PatientBill> patientsBills) {

		Set<PatientServiceBill> patientServiceBill = new HashSet<PatientServiceBill>();
		for (PatientBill bill : patientsBills) {

			patientServiceBill.addAll(bill.getBillItems());

		}

		Map<String, String> names = new HashMap<String, String>();

		for (PatientServiceBill psb : patientServiceBill) {
			if (psb.getBillableServiceCategory().getName() != null
					&& !psb.getBillableServiceCategory().getName().equals("")) {

				names.put(psb.getBillableServiceCategory().getName(), psb
						.getBillableServiceCategory().getName());
			}
		}

		return names;
	}

	/**
	 * The daily report on all amount paid (including the collectors and the
	 * patients as well as the respective amount) >> going to the Chief Cashier
	 * 
	 * @param day
	 *            the Day to report
	 * @return payments all daily the payments
	 */
	public static List<BillPayment> getDailyPayments(Date day) {

		List<BillPayment> payments = new ArrayList<BillPayment>();

		for (PatientBill pb : getService().getAllPatientBills())
			if (!pb.isVoided() && pb.getPayments() != null)
				for (BillPayment bp : pb.getPayments())
					if (!bp.isVoided()
							&& bp.getDateReceived().compareTo(day) == 0)
						payments.add(bp);

		return payments;
	}

	// <<<<<<<<<< B. External:

	/**
	 * The monthly report on all patients of same insurance and the amount to be
	 * paid by the Patient and Insurance >> going to the Insurance company
	 * 
	 * @param insurance
	 * @param startDate
	 *            the start date of the period
	 * @param endDate
	 *            the end date of the period
	 * @return bills the list of matched PatientBill
	 */
	public static List<PatientBill> getMonthlyReportByInsurance(
			Insurance insurance, Date startDate, Date endDate, Integer patientId) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills())
			if (!pb.isVoided()
					&& pb.getBeneficiary().getInsurancePolicy().getInsurance()
							.getInsuranceId().intValue() == insurance
							.getInsuranceId().intValue())

				/*
				 * for (PatientServiceBill psb : pb.getBillItems()){
				 * if(startDate!=null && endDate!=null) if (!psb.isVoided()&&
				 * psb.getServiceDate().compareTo(startDate) >= 0 &&
				 * psb.getServiceDate().compareTo(endDate) <= 0) bills.add(pb);
				 * if(startDate!=null && endDate==null) if (!psb.isVoided()&&
				 * psb.getServiceDate().compareTo(startDate) >= 0)
				 * bills.add(pb); if(startDate==null && endDate!=null) if
				 * (!psb.isVoided()&& psb.getServiceDate().compareTo(endDate) <=
				 * 0) bills.add(pb); if(startDate==null && endDate==null)
				 */
				for (PatientServiceBill psb : pb.getBillItems()) {
					if (startDate != null && endDate != null
							&& patientId != null) {
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0
								&& psb.getServiceDate().compareTo(endDate) <= 0) {
							if (!bills.contains(getService().getPatientBill(pb
									.getPatientBillId()))) {
								if (psb.getPatientBill().getBeneficiary()
										.getPatient().getPatientId()
										.compareTo(patientId) == 0) {
									// pb.getBeneficiary().getPatient().getPatientId();
									// System.out.println("sjdbfhjdfsjdbfhkjavjads 1");
									bills.add(pb);
									// System.out.println("sjdbfhjdfsjdbfhkjavjads 2");
								}
							}
						}
					}
					if (startDate != null && endDate != null
							&& patientId == null) {
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0
								&& psb.getServiceDate().compareTo(endDate) <= 0) {
							if (!bills.contains(getService().getPatientBill(pb
									.getPatientBillId()))) {
								bills.add(pb);
							}
						}
					}
					if (startDate != null && endDate == null
							&& patientId == null)
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0)
							if (!bills.contains(getService().getPatientBill(pb
									.getPatientBillId())))
								bills.add(pb);
					if (startDate == null && endDate != null
							&& patientId == null)
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(endDate) <= 0)
							if (!bills.contains(getService().getPatientBill(pb
									.getPatientBillId())))
								bills.add(pb);
					if (startDate == null && endDate == null
							&& patientId == null)
						if (!bills.contains(getService().getPatientBill(pb
								.getPatientBillId())))

							bills.add(pb);
				}
		return bills;
	}

	public static List<PatientBill> billCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCreator) {

		List<PatientBill> bills = getService().billCohortBuilder(insurance, startDate,
				endDate, patientId, serviceName, billStatus, billCreator);
		return bills;
	}
	
	public static List<BillPayment> paymentsCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {

		List<BillPayment> bills = getService().paymentsCohortBuilder(insurance, startDate,
				endDate, patientId, serviceName, billStatus, billCollector);
		return bills;
	}

	/**
	 * The list of all patients that received billable services of same
	 * insurance for a certain period >> going to the Insurance company/
	 * District (local governance)
	 */
	// Wonder if it is not the same as above
	public static Float getMonthlyInsuranceDueAmount(Insurance insurance,
			Date startDate, Date endDate, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {

			if (!pb.isVoided()
					&& pb.getBeneficiary().getInsurancePolicy().getInsurance()
							.getInsuranceId().intValue() == insurance
							.getInsuranceId().intValue()
					&& pb.getIsPaid() == isPaid) {

				for (PatientServiceBill psb : pb.getBillItems()) {
					if (!psb.isVoided()
							&& psb.getServiceDate().compareTo(startDate) >= 0
							& psb.getServiceDate().compareTo(endDate) <= 0)

						bills.add(pb);
				}
			}
		}
		float amountToBePaid = 0;
		for (PatientBill patientBills : bills) {

			float amountPerBillByInsurance = (patientBills.getAmount()
					.intValue() * insurance.getRateOnDate(endDate).getRate()) / 100;
			amountToBePaid = amountToBePaid + amountPerBillByInsurance;

		}

		return amountToBePaid;
	}

	/**
	 * 
	 * gets all patients' bills by facility service
	 * 
	 * @param sc
	 * @param startDate
	 * @param endDate
	 * @param patient
	 * @param insurance
	 * @return bills the list of matched PatientBill
	 */
	public static List<PatientBill> getBillsByServiceCategory(
			FacilityServicePrice sc, Date startDate, Date endDate,
			Patient patient, Insurance insurance) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills())
			for (PatientServiceBill psb : pb.getBillItems()) {
				if (sc != null && startDate == null && endDate == null
						&& patient == null && insurance == null)
					if (psb.getService().getFacilityServicePrice()
							.getFacilityServicePriceId().intValue() == sc
							.getFacilityServicePriceId().intValue()) {
						bills.add(pb);
					}
				if (sc != null && startDate != null && endDate == null
						&& patient == null && insurance == null)
					if (psb.getService().getFacilityServicePrice()
							.getFacilityServicePriceId().intValue() == sc
							.getFacilityServicePriceId().intValue()
							&& psb.getService().getStartDate()
									.compareTo(startDate) >= 0) {
						bills.add(pb);
					}
				if (sc != null && startDate == null && endDate != null
						&& patient == null && insurance == null)
					if (psb.getService().getFacilityServicePrice()
							.getFacilityServicePriceId().intValue() == sc
							.getFacilityServicePriceId().intValue()
							&& psb.getService().getStartDate()
									.compareTo(endDate) <= 0) {
						bills.add(pb);
					}
				if (sc != null && startDate != null && endDate != null
						&& patient == null && insurance == null)
					if (psb.getService().getFacilityServicePrice()
							.getFacilityServicePriceId().intValue() == sc
							.getFacilityServicePriceId().intValue()
							&& psb.getService().getStartDate()
									.compareTo(startDate) >= 0
							&& psb.getService().getStartDate()
									.compareTo(endDate) <= 0) {
						bills.add(pb);
					}
				if (sc != null && startDate != null && endDate != null
						&& patient != null && insurance == null)
					if (psb.getService().getFacilityServicePrice()
							.getFacilityServicePriceId().intValue() == sc
							.getFacilityServicePriceId().intValue()
							&& psb.getService().getStartDate()
									.compareTo(startDate) >= 0
							&& psb.getService().getStartDate()
									.compareTo(endDate) <= 0
							&& pb.getBeneficiary().getPatient().getPatientId()
									.intValue() == patient.getPatientId()) {
						bills.add(pb);
					}
				if (sc != null && startDate != null && endDate != null
						&& patient != null && insurance != null)
					if (psb.getService().getFacilityServicePrice()
							.getFacilityServicePriceId().intValue() == sc
							.getFacilityServicePriceId().intValue()
							&& psb.getService().getStartDate()
									.compareTo(startDate) >= 0
							&& psb.getService().getStartDate()
									.compareTo(endDate) <= 0
							&& psb.getService().getInsurance().getInsuranceId() == insurance
									.getInsuranceId()) {
						bills.add(pb);
					}
			}

		return bills;
	}
	
	static public double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	

	
	static public void printPatientBillToPDF(HttpServletRequest request,
			HttpServletResponse response, List<PatientBill> reportedPatientBills)
			throws Exception {
		Document document = new Document();

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "Report"); // file name

		PdfWriter writer = PdfWriter.getInstance(document,
				response.getOutputStream());
		writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));
		writer.setBoxSize("art", PageSize.A4);

		HeaderFooter event = new HeaderFooter();
		writer.setPageEvent(event);

		document.open();
		Image image1 = Image.getInstance("C:/image1.jpg");
		Image image2 = Image.getInstance("C:/image2.jpg");

		image1.setAbsolutePosition(0, 0);
		image2.setAbsolutePosition(0, 0);

		/** Adding an image (logo) to the file */
		PdfContentByte byte1 = writer.getDirectContent();
		PdfTemplate tp1 = byte1.createTemplate(600, 150);
		tp1.addImage(image2);
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
			Image image = Image.getInstance("../../images/police.jpg");
			image.setAlignment(Image.ALIGN_LEFT);
			image.setBorder(4900);
			document.add(image);
		} catch (Exception e) {
			System.out.println("error loading image...... " + e.getMessage());
		}

		document.add(fontTitle.process("POLICE NATIONALE\n"));
		document.add(fontTitle.process("KACYIRU POLICE HOSPITAL\n"));
		document.add(fontTitle.process("B.P. 6183 KIGALI\n"));
		document.add(fontTitle.process("T�l : 584897\n"));
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

		PdfPCell cell = new PdfPCell(
				fontTitleSelector.process("Compagnie d'Assurance : " + 543));
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

		// log.info("reportedPatientBills   new reportedPatientBills reportedPatientBills reportedPatientBills reportedPatientBills  : "+reportedPatientBills.size());
		document.close();

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
			footer.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, footer,
					(rect.getLeft() + rect.getRight()) / 2,
					rect.getBottom() - 40, 0);

		}
	}
	
	
}
