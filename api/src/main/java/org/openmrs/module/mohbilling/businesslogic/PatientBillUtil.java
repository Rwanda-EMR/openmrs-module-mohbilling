package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
			BigDecimal unitPrice, BigDecimal quantity) {

		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);
		BigDecimal rate = BigDecimal.valueOf(insurance.getRateOnDate(date)
				.getRate());

//		BigDecimal qty = (BigDecimal.valueOf(quantity));
		BigDecimal totalAmount = unitPrice.multiply(quantity, mc);

		return (insurance == null) ? totalAmount : totalAmount.multiply(rate,
				mc);
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
	 * this calculates the total amount of a Patient Bill (taking into
	 * consideration the List of PatientServiceBill and "unitPrice", "quantity"
	 * and "rate" corresponding to each)
	 * 
	 * @param bill
	 * @param insurance
	 */
	public static void calculateBill(PatientBill bill, Insurance insurance) {

		//bill.setAmount(calculateTotalBill(insurance, bill));

		getService().savePatientBill(bill);
	}

	/**
	 * this returns the Total amount due (same as above)
	 * 
	 * @param insurance
	 * @param bill
	 * @return
	 */
	public static BigDecimal calculateTotalBill(Insurance insurance, Consommation consom) {

		BigDecimal amount = new BigDecimal(0);
		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);

		// The valid Rate for the entered Insurance company
		InsuranceRate validRate = insurance.getRateOnDate(new Date());

		for (PatientServiceBill psb : consom.getBillItems()) {

			amount.add(
					psb.getUnitPrice().multiply((psb.getQuantity()), mc), mc);
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
	 * create insurance bill and then save it to the DB
	 * @param totalAmount for all bill items before appling flat fees
	 * @param ip that holds the insurance upon which the amount will be calculated
	 * @return PatientBill
	 */
	public static PatientBill createPatientBill(BigDecimal totalAmount,InsurancePolicy ip){
		
		 InsuranceRate validRate = ip.getInsurance().getRateOnDate(new Date());
		 ThirdParty thirdParty =ip.getThirdParty();
		 
		 Float rateToPay=null;		//rate based on which the amount is calculated	 
		
		if(thirdParty == null)
			  rateToPay = 100-validRate.getRate();			
		else			
			rateToPay = (100-validRate.getRate())-thirdParty.getRate();			
		
	    BigDecimal  pbAmount = totalAmount.multiply(BigDecimal.valueOf((rateToPay)/100));
		
	    PatientBill pb = new PatientBill();
			 pb.setAmount(pbAmount);
			 pb.setCreatedDate(new Date());
			 pb.setCreator(Context.getAuthenticatedUser());
			 pb.setVoided(false);
			 
			pb =savePatientBill(pb);
			
		return pb;
		
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
			//bill.removeBillItem(psb);
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
	public static CashPayment createCashPayment(CashPayment cashPayment){
		
		getService().saveCashPayment(cashPayment);		
		
		return cashPayment;
		
	}
	public static DepositPayment createDepositPayment(DepositPayment depositPayment){
		getService().saveDepositPayment(depositPayment);
		return depositPayment;
	}

}
