package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * Helper class to support the Patient Bill domain
 * 
 * The parent class in this domain is PatientBill, and the child classes are
 * BillPayment, and PatientServiceBill.
 * 
 * 
 * @author dthomas
 * 
 */
public class PatientBillUtil {

	/**
	 * @param insurance
	 * @param date
	 * @param amount
	 * @return
	 */
	/*
	 * public static BigDecimal applyInsuranceRate(Insurance insurance, Date
	 * date, BigDecimal amount) {
	 * 
	 * MathContext mc = new MathContext(BigDecimal.ROUND_DOWN); BigDecimal rate
	 * = BigDecimal.valueOf(insurance.getRateOnDate(date) .getRate());
	 * 
	 * //System.out.println(
	 * "fdddddddddddddddddddddddddddfffffffffffffffffffddddddddddddddddddddddddddddd"
	 * +rate+"  fggg "+amount.multiply(rate, mc));
	 * 
	 * return (insurance == null) ? amount : amount.multiply(rate, mc); }
	 */

	/**
	 * @param insurance
	 * @param date
	 * @param unitPrice
	 * @param quantity
	 * @return
	 */
	public static BigDecimal calculateTotal(Insurance insurance, Date date,
			BigDecimal unitPrice, Integer quantity) {

		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);
		BigDecimal rate = BigDecimal.valueOf(insurance.getRateOnDate(date)
				.getRate());

		BigDecimal qty = BigDecimal.valueOf(quantity);
		BigDecimal totalAmount = unitPrice.multiply(qty, mc);

		return (insurance == null) ? totalAmount : totalAmount.multiply(rate,
				mc);
	}

	/**
	 * This should return all Bills corresponding to a given Patient on a given
	 * date
	 * 
	 * @param patient
	 * @param date
	 * @return
	 */
	public static List<PatientBill> getBillsByPatientOnDate(Patient patient,
			Date date) {

		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (pb.getBeneficiary().getPatient().getPatientId().intValue() == patient
					.getPatientId().intValue()) {
				for (PatientServiceBill psb : pb.getBillItems()) {
					if (psb.getServiceDate().compareTo(date) == 0) {
						bills.add(pb);
						break;
					}
				}
			}
		}
		return bills;
	}

	/**
	 * This should return all Bills corresponding to a given Beneficiary on a
	 * given date
	 * 
	 * @param beneficiary
	 * @param date
	 *            the date on which the Bill
	 * @return bill the list of all matching PatientBills
	 */
	public static List<PatientBill> getBillsByBeneficiaryOnDate(
			Beneficiary beneficiary, Date date) {

		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (pb.getBeneficiary().getBeneficiaryId().intValue() == beneficiary
					.getBeneficiaryId().intValue()) {
				for (PatientServiceBill psb : pb.getBillItems()) {
					if (psb.getServiceDate().compareTo(date) == 0) {
						bills.add(pb);
					}
				}
			}
		}
		return bills;
	}

	/**
	 * This should return all Bills corresponding to a given Patient
	 * 
	 * @param patient
	 * @return
	 */
	public static List<PatientBill> getBillsByPatient(Patient patient) {

		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (pb.getBeneficiary().getPatient() == patient) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * This should return all Bills corresponding to a given Beneficiary
	 * 
	 * @param beneficiary
	 * @return
	 */
	public static List<PatientBill> getBillsByBeneficiary(
			Beneficiary beneficiary) {

		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (pb.getBeneficiary() == beneficiary) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * This should return all paid Bills on a given date (isPaid == true),
	 * otherwise it returns unpaid (isPaid==false)
	 * 
	 * @param isPaid
	 * @return
	 */
	public static List<PatientBill> getPaidBills(Boolean isPaid, Date date) {

		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (pb.getIsPaid() == true
					&& pb.getCreatedDate().compareTo(date) == 0) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * 
	 * This should return all paid Bills on,before,before a given date or
	 * between two dates (isPaid == true), otherwise it returns unpaid
	 * (isPaid==false)
	 * 
	 * @param isPaid
	 * @param date
	 * @return
	 */
	public static List<PatientBill> getPatientBillsInDates(Date startDate,
			Date endDate) {
		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (startDate != null && endDate == null)
				if (pb.getCreatedDate().compareTo(startDate) >= 0)
					bills.add(pb);
			if (startDate == null && endDate != null)
				if (pb.getCreatedDate().compareTo(endDate) <= 0)
					bills.add(pb);
			if (startDate != null && endDate != null)
				if (pb.getCreatedDate().compareTo(startDate) >= 0
						&& pb.getCreatedDate().compareTo(endDate) <= 0)
					bills.add(pb);
		}
		return bills;
	}

	/**
	 * This should return all paid Bills corresponding to a given Beneficiary
	 * (isPaid == true), otherwise it returns unpaid (isPaid==false)
	 * 
	 * @param beneficiary
	 * @param isPaid
	 * @return
	 */
	public static List<PatientBill> getPaidBillsByBeneficiary(
			Beneficiary beneficiary, Boolean isPaid) {

		BillingService bs = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : bs.getAllPatientBills()) {
			if (pb.getIsPaid() == isPaid
					&& pb.getBeneficiary().equals(beneficiary)) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * This should change the PatientBill to printed status (printed == true)
	 * 
	 * @param bill
	 */
	public static void printBill(PatientBill bill) {

		BillingService bs = Context.getService(BillingService.class);
		bill.setPrinted(true);
		bs.savePatientBill(bill);
	}

	/**
	 * this calculates the total amount of a Patient Bill (taking into
	 * consideration the List of PatientServiceBill and "unitPrice", "quantity"
	 * and "rate" corresponding to each)
	 * 
	 * @param bill
	 * @param insurance
	 */
	public static void calculateBill(PatientBill bill, Insurance insurance) {

		BillingService bs = Context.getService(BillingService.class);

		bill.setAmount(calculateTotalBill(insurance, bill));

		bs.savePatientBill(bill);
	}

	/**
	 * this returns the Total amount due (same as above)
	 * 
	 * @param insurance
	 * @param bill
	 * @return
	 */
	public static BigDecimal calculateTotalBill(Insurance insurance,
			PatientBill bill) {

		BigDecimal amount = new BigDecimal(0);
		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);

		// The valid Rate for the entered Insurance company
		InsuranceRate validRate = insurance.getRateOnDate(new Date());

		for (PatientServiceBill psb : bill.getBillItems()) {

			amount.add(
					psb.getUnitPrice().multiply(
							BigDecimal.valueOf(psb.getQuantity()), mc), mc);
		}

		// This returned amount is the one the patient pays (It may change to
		// the total amount without applying the Insurance rate)
		return amount.multiply(BigDecimal.valueOf(validRate.getRate()), mc);
	}

	/**
	 * Creates a PatientBill object and saves it in the DB
	 * 
	 * @param bill
	 *            the PatientBill to be saved
	 * @return bill the PatientBill that has been saved
	 */
	public static PatientBill savePatientBill(PatientBill bill) {

		BillingService service = Context.getService(BillingService.class);

		if (bill != null) {
			service.savePatientBill(bill);
			return bill;
		}

		return null;
	}

	/**
	 * Creates a PatientServiceBill object and saves it in the DB through
	 * PatientBill which is its parent
	 * 
	 * @param psb
	 *            the PatientServiceBill to be saved
	 * @return psb the PatientServiceBill that has been saved
	 */
	public static PatientServiceBill createPatientServiceBill(
			PatientServiceBill psb) {

		BillingService service = Context.getService(BillingService.class);
		PatientBill bill = new PatientBill();

		if (psb != null) {
			bill = psb.getPatientBill();
			bill.addBillItem(psb);
			service.savePatientBill(bill);
			return psb;
		}

		return null;
	}

	/**
	 * Creates a BillPayment object and saves it in the DB through PatientBill
	 * which is its parent
	 * 
	 * @param payment
	 *            the BillPayment to be saved
	 * @return payment the BillPayment that has been saved
	 */
	public static BillPayment createBillPayment(BillPayment payment) {

		BillingService service = Context.getService(BillingService.class);
		PatientBill bill = new PatientBill();

		if (payment != null) {
			payment.setVoided(false);
			payment.setCreatedDate(new Date());
			payment.setCreator(Context.getAuthenticatedUser());
			bill = payment.getPatientBill();
			bill.addBillPayment(payment);
			service.savePatientBill(bill);
			return payment;
		}

		return null;
	}

	/**
	 * The list of services that a patient received and paid during a certain
	 * period >> going to the Data manager/ Accountant/ HC Head
	 * 
	 * @param patient
	 *            the patient to be matched as the one who received care
	 * @param startDate
	 *            the Start date to be considered as the min boundary
	 * @param endDate
	 *            the End date to be considered as the max boundary
	 * @param isPaid
	 *            the Value that determines whether the services are un/paid, if
	 *            null (no Value is provided) it will match without considering
	 *            this Value <code>isPaid</code>
	 * @return bills the lis of un/paid bills on a certain period
	 */
	public static List<PatientBill> getBillsByPatient(Patient patient,
			Date startDate, Date endDate, Boolean isPaid) {

		BillingService service = Context.getService(BillingService.class);
		List<PatientBill> bills = new ArrayList<PatientBill>();

		if (service.getAllPatientBills() != null)
			for (PatientBill pb : service.getAllPatientBills())
				if (!pb.isVoided())
					if (pb.getBeneficiary().getPatient().getPatientId()
							.intValue() == patient.getPatientId().intValue()
							&& pb != null && pb.getIsPaid() == isPaid) {

						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								bills.add(pb);
					} else if (pb.getBeneficiary().getPatient().getPatientId()
							.intValue() == patient.getPatientId().intValue()
							&& pb != null) {
						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0) {
								System.out
										.println(" i m getting here rwertwertewrt wertwert ewrtwert wertwert");
								bills.add(pb);
							}

					} else
						bills = null;

		return bills;
	}

	/**
	 * Refunds Patient Bill: When the patient pays for some service, it may
	 * happen that the service is not available at the moment, so this requires
	 * that s/he comes back to the billing desk and ask for refund. This will be
	 * processed by removing those Billable Service from the list of the
	 * services s/he got, and the corresponding amount will be deducted from the
	 * general Total Bill.
	 * 
	 * @param bill
	 *            the one to be refunded
	 * @param services
	 *            the ones to be removed
	 * @return the refunded/updated bill
	 */
	public static PatientBill refundPatientBill(PatientBill bill,
			List<PatientServiceBill> services) {

		for (PatientServiceBill psb : services) {
			bill.removeBillItem(psb);
		}

		return bill;
	}
	
	/**
	 * Gets the PatientBill by billId
	 * 
	 * @param billId the one to be matched
	 * @return the PatientBill that matches the provided billId
	 */
	public static PatientBill getPatientBillById(Integer billId){

		BillingService service = Context.getService(BillingService.class);
		
		return service.getPatientBill(billId);
	}
}
