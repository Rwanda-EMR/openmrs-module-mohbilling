/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingTagUtil {

	public static String getTotalAmountPaidByPatientBill(Integer patientBillId) {

		Long amountPaid = 0l;
//		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);

		if (null == patientBillId)
			return "";
		else {
			try {
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);

				if(pb.getAmountPaid() != null)
					amountPaid = pb.getAmountPaid().longValue();
//				for (BillPayment bp : pb.getPayments()) {
//					amountPaid = amountPaid + bp.getAmountPaid().longValue();
//				}

			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		return "" + amountPaid;
	}

	/**
	 * Gets the REST of the whole Patient Bill
	 * 
	 * @param patientBillId
	 *            the patient bill ID
	 * @return the REST that is in String
	 */
	public static String getTotalAmountNotPaidByPatientBill(
			Integer patientBillId) {

		double amountNotPaid = 0d;
//		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);

		if (null == patientBillId)
			return "";
		else {
			try {
				double amountPaid = 0d;
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);
				Float insuranceRate = pb.getBeneficiary().getInsurancePolicy()
						.getInsurance().getCurrentRate().getRate();
				Float patientRate = (100f - insuranceRate) / 100f;
				
				// mariam codes to make one print out (one bill with 1.Ticket Moderateur applied on services and 2.private things)
				// in order to reduce number of papers and users switching pages
				double amountDueByPatient = 0.0;
				for (PatientServiceBill psb : pb.getBillItems()) {
					Double cost = psb.getUnitPrice().doubleValue()*psb.getQuantity();
					amountDueByPatient+=psb.getService().getFacilityServicePrice().getCategory().equals("AUTRES")?cost:cost*patientRate.doubleValue();
				}
//				double amountDueByPatient = (pb.getAmount().doubleValue() * patientRate
//						.doubleValue());

				for (BillPayment bp : pb.getPayments()) {
					amountPaid = amountPaid + bp.getAmountPaid().doubleValue();
				}

				if (pb.getBeneficiary().getInsurancePolicy().getThirdParty() == null) {
					amountNotPaid = amountDueByPatient - amountPaid;

					/** Marking the BILL as PAID */
					if (amountNotPaid <= 0) {
						pb.setIsPaid(true);
						Context.getService(BillingService.class)
								.savePatientBill(pb);
					}

					/** END of PAID part... */
				} else {

					double thirdPartRate = pb.getBeneficiary()
							.getInsurancePolicy().getThirdParty().getRate()
							.doubleValue();

					double amountPaidByThirdPart = pb.getAmount().doubleValue()
							* (thirdPartRate / 100);

					amountNotPaid = amountDueByPatient - (amountPaidByThirdPart + amountPaid);

					/** Marking the BILL as PAID */
					if (amountNotPaid <= 0) {
						pb.setIsPaid(true);
						
						Context.getService(BillingService.class)
								.savePatientBill(pb);
					}

					/** END of PAID part... */
				}

			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		/** Rounding the value to 2 decimals */
		double roundedAmountNotPaid = Math.round(amountNotPaid * 100);
		roundedAmountNotPaid = roundedAmountNotPaid / 100;

		return "" + roundedAmountNotPaid;
	}

	public static String getAmountPaidByThirdPart(Integer patientBillId) {

		Double amountPaidByThirdPart = 0d;
		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);

		if (null == patientBillId)
			return "";
		else {
			try {
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);

				Float thirdPartRate = pb.getBeneficiary().getInsurancePolicy()
						.getThirdParty().getRate();

				if (thirdPartRate != null)
					amountPaidByThirdPart = (pb.getAmount().doubleValue()
							* thirdPartRate / 100);
				else
					;

			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		return ""
				+ new BigDecimal(1).multiply(
						BigDecimal.valueOf(amountPaidByThirdPart), mc)
						.longValue();
	}
	
	public static List<Double> getDetailsPaymentsByPatientBill(
			Integer patientBillId) {

		double amountNotPaid = 0d;
		List<Double> payments = new ArrayList<Double>();

			try {
				double totalAmountPaid = 0d;
				
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);
				Float insuranceRate = pb.getBeneficiary().getInsurancePolicy()
						.getInsurance().getCurrentRate().getRate();
				Float patientRate = (100f - insuranceRate) / 100f;
				
				double amountDueByPatient = (pb.getAmount().doubleValue() * patientRate
						.doubleValue());

				for (BillPayment bp : pb.getPayments()) {
					totalAmountPaid = totalAmountPaid + bp.getAmountPaid().doubleValue();
					payments.add(bp.getAmountPaid().doubleValue());
				}

				if (pb.getBeneficiary().getInsurancePolicy().getThirdParty() == null) {
					amountNotPaid = amountDueByPatient - totalAmountPaid;

					/** Marking the BILL as PAID */
					if (amountNotPaid <= 0) {
						pb.setIsPaid(true);
						Context.getService(BillingService.class)
								.savePatientBill(pb);
					}

					/** END of PAID part... */
				} else {

					double thirdPartRate = pb.getBeneficiary()
							.getInsurancePolicy().getThirdParty().getRate()
							.doubleValue();

					double amountPaidByThirdPart = pb.getAmount().doubleValue()
							* (thirdPartRate / 100);

					amountNotPaid = amountDueByPatient - (amountPaidByThirdPart + totalAmountPaid);

					/** Marking the BILL as PAID */
					if (amountNotPaid <= 0) {
						pb.setIsPaid(true);
						
						Context.getService(BillingService.class)
								.savePatientBill(pb);
					}

					/** END of PAID part... */
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		/** Rounding the value to 2 decimals */
		double roundedAmountNotPaid = Math.round(amountNotPaid * 100);
		roundedAmountNotPaid = roundedAmountNotPaid / 100;

		return payments;
	}

}
