package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillStatus;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
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
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

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

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
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

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
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

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
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

		return getService().getBillsByBeneficiary(beneficiary);
	}

	/**
	 * This should return all paid Bills on a given date (isPaid == true),
	 * otherwise it returns unpaid (isPaid==false)
	 * 
	 * @param isPaid
	 * @return
	 */
	public static List<PatientBill> getPaidBills(Boolean isPaid, Date date) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
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

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
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

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
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

		bill.setPrinted(true);
		getService().savePatientBill(bill);
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

		bill.setAmount(calculateTotalBill(insurance, bill));

		getService().savePatientBill(bill);
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

		if (bill != null) {
			getService().savePatientBill(bill);
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

		PatientBill bill = new PatientBill();

		if (psb != null) {
			bill = psb.getPatientBill();
			bill.addBillItem(psb);
			getService().savePatientBill(bill);
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

		PatientBill bill = new PatientBill();

		if (payment != null) {
			payment.setVoided(false);
			payment.setCreatedDate(new Date());
			payment.setCreator(Context.getAuthenticatedUser());
			bill = payment.getPatientBill();
			bill.addBillPayment(payment);
			getService().savePatientBill(bill);
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

		List<PatientBill> bills = new ArrayList<PatientBill>();

		if (getService().getAllPatientBills() != null)
			for (PatientBill pb : getService().getAllPatientBills())
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
	 * @param billId
	 *            the one to be matched
	 * @return the PatientBill that matches the provided billId
	 */
	public static PatientBill getPatientBillById(Integer billId) {

		return getService().getPatientBill(billId);
	}

	/**
	 * Gets Bills by a given Period
	 * 
	 * @param startDate
	 *            the starting period
	 * @param endDate
	 *            the ending period
	 * @return bills as a list of all matched bills
	 */
	public static List<PatientBill> getBillsByPeriod(Date startDate,
			Date endDate) {

		List<PatientBill> bills = null;

		if (startDate != null && endDate != null) {

			bills = new ArrayList<PatientBill>();

			for (PatientBill bill : getService().getAllPatientBills()) {
				if (bill.getCreatedDate().compareTo(startDate) >= 0
						&& bill.getCreatedDate().compareTo(endDate) <= 0) {
					bills.add(bill);
				}
			}
		}

		return bills;
	}
	
	public static void markBillAsPaid(PatientBill bill) {

		PatientBill pb = getService().getPatientBill(bill.getPatientBillId());
		double amountNotPaid = 0d;
		double amountPaid = pb.getAmountPaid().doubleValue();
		double insuranceRate = pb.getBeneficiary().getInsurancePolicy()
				.getInsurance().getCurrentRate().getRate();
		double patientRate = (100f - insuranceRate) / 100f;
		double amountDueByPatient = (pb.getAmount().doubleValue() * patientRate);

		if (pb.getBeneficiary().getInsurancePolicy().getThirdParty() == null)
			
			amountNotPaid = amountDueByPatient - amountPaid;
		else {

			double thirdPartRate = pb.getBeneficiary().getInsurancePolicy()
					.getThirdParty().getRate().doubleValue();

			double amountPaidByThirdPart = pb.getAmount().doubleValue()
					* (thirdPartRate / 100);

			amountNotPaid = amountDueByPatient
					- (amountPaidByThirdPart + amountPaid);
			amountDueByPatient -= amountPaidByThirdPart;

		}

		/** Marking the BILL as FULLY PAID */
		if (amountPaid >= amountDueByPatient) {
			pb.setIsPaid(true);
			pb.setStatus(BillStatus.FULLY_PAID.getDescription());
		}
		/** Marking the BILL as NOT PAID at all */
		if (amountNotPaid == amountDueByPatient){
			pb.setStatus(BillStatus.UNPAID.getDescription());
		}
		/** Marking the BILL as PARTLY PAID */
		if (amountNotPaid > 0d && amountNotPaid < amountDueByPatient)
			pb.setStatus(BillStatus.PARTLY_PAID.getDescription());
		
		getService().savePatientBill(pb);
	}
	
	public static PatientInvoice getPatientInvoice(PatientBill pb, String[] serviceCategories){
		
		Set<PatientServiceBill> billItems =pb.getBillItems();		
		
		PatientInvoice invoice = new PatientInvoice();
		LinkedHashMap<String, Double> amountByCategory =new LinkedHashMap<String, Double>();
		Double currentRate =pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();
		invoice.setPatientBill(pb);
		
		for (String svcecategory : serviceCategories) {
			Double amount =0.0;
			for (PatientServiceBill item : billItems) {		
				String category =item.getService().getFacilityServicePrice().getCategory();
				if (category.equals(svcecategory)) {
					
		    // Double  quantity = (Double)item.getQuantity();
					Double unitPrice=item.getUnitPrice().doubleValue();
					Double cost =item.getQuantity()*unitPrice*currentRate/100;
					amount+=cost;				
				}
				
			}
			amountByCategory.put(svcecategory, amount);	
			invoice.setCategoryAmount(amountByCategory);	
		}	
		
		return  invoice;
		
	}
	
}
