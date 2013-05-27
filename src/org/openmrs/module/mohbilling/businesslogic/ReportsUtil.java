/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

/**
 * @author Kamonyo
 * 
 */
public class ReportsUtil {

	protected final Log log = LogFactory.getLog(getClass());

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

		BillingService service = Context.getService(BillingService.class);

		if (beneficiary != null)
			// if(date!=null)
			for (PatientBill bill : service.getAllPatientBills())
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

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills;
		Set<PatientServiceBill> itemBills;

		if (date != null && isPaid != null) {
			bills = new ArrayList<PatientBill>();
			for (PatientBill pb : service.getAllPatientBills())
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

		BillingService service = Context.getService(BillingService.class);
		List<BillableService> services = new ArrayList<BillableService>();

		if (service.getAllPatientBills() != null)
			for (PatientBill pb : service.getAllPatientBills())
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

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : service.getAllPatientBills())
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

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : service.getAllPatientBills())
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

		BillingService service = Context.getService(BillingService.class);
		List<BillPayment> payments = new ArrayList<BillPayment>();

		for (PatientBill pb : service.getAllPatientBills())
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

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : service.getAllPatientBills())
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
							if (!bills.contains(service.getPatientBill(pb
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
							if (!bills.contains(service.getPatientBill(pb
									.getPatientBillId()))) {
								bills.add(pb);
							}
						}
					}
					if (startDate != null && endDate == null
							&& patientId == null)
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0)
							if (!bills.contains(service.getPatientBill(pb
									.getPatientBillId())))
								bills.add(pb);
					if (startDate == null && endDate != null
							&& patientId == null)
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(endDate) <= 0)
							if (!bills.contains(service.getPatientBill(pb
									.getPatientBillId())))
								bills.add(pb);
					if (startDate == null && endDate == null
							&& patientId == null)
						if (!bills.contains(service.getPatientBill(pb
								.getPatientBillId())))

							bills.add(pb);
				}
		return bills;
	}

	public static List<PatientBill> buildCohort(Insurance insurance,
			Date startDate, Date endDate, Integer patientId, String serviceName) {

		BillingService service = Context.getService(BillingService.class);

		List<PatientBill> bills = service.buildCohort(insurance, startDate,
				endDate, patientId, serviceName);
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

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : service.getAllPatientBills()) {

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

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : service.getAllPatientBills())
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
}
