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
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
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

		

		return null;
	}


	public static List<BillableService> getPaidServices(Date startDate,
			Date endDate, Boolean isPaid) {

		List<BillableService> services = new ArrayList<BillableService>();
/*
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
					} else {
						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								services.add(psb.getService());
					}*/

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

		/*for (PatientBill pb : getService().getAllPatientBills())
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
			}*/

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
			;

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

			//patientServiceBill.addAll(bill.getBillItems());

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

	
	static public double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	

	
	static public void printPatientBillToPDF(HttpServletRequest request,
			HttpServletResponse response, List<PatientBill> reportedPatientBills)
			throws Exception {
	

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
	public static Double getTotalRefundedAmount(Set<PatientBill> pb){
		 Double total = 0.0;
		 for (PatientBill b : pb) {
			for (BillPayment pay : b.getPayments()) {
				if(pay.getAmountPaid().doubleValue()<0)
				total+=pay.getAmountPaid().doubleValue();
			}
		}
		return total;
		 
	 }
}
