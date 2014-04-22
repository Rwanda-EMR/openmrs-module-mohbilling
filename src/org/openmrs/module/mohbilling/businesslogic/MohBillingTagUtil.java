/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingTagUtil {

	public static String getTotalAmountPaidByPatientBill(Integer patientBillId) {

		Long amountPaid = 0l;
		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);

		if (null == patientBillId)
			return "";
		else {
			try {
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);
				for (BillPayment bp : pb.getPayments()) {
					amountPaid = amountPaid + bp.getAmountPaid().longValue();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		return ""
				+ new BigDecimal(1)
						.multiply(BigDecimal.valueOf(amountPaid), mc)
						.longValue();
	}

	public static String getTotalAmountNotPaidByPatientBill(
			Integer patientBillId) {

		Double amountNotPaid = 0d;
		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);

		if (null == patientBillId)
			return "";
		else {
			try {
				Double amountPaid = 0d;
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);
				for (BillPayment bp : pb.getPayments()) {
					amountPaid = amountPaid + bp.getAmountPaid().doubleValue();
				}

				amountNotPaid = ((pb.getAmount().doubleValue() * (100 - (pb
						.getBeneficiary().getInsurancePolicy().getInsurance()
						.getCurrentRate().getRate()))) / 100)
						- amountPaid;

				/** Marking the BILL as PAID */
				if (new BigDecimal(1).multiply(
						BigDecimal.valueOf(amountNotPaid), mc).doubleValue() <= 0.0) {
					pb.setIsPaid(true);
					Context.getService(BillingService.class)
							.savePatientBill(pb);
				}
				
				/** END of PAID part... */

			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		return ""
				+ new BigDecimal(1).multiply(BigDecimal.valueOf(amountNotPaid),
						mc).doubleValue();
	}

	public static String getAmountPaidByThirdPart(Integer patientBillId) {

		Double amountPaidByThirdPart = 0d;
		MathContext mc = new MathContext(BigDecimal.ROUND_HALF_DOWN);

		if (null == patientBillId)
			return "";
		else {
			try {
				Double amountPaid = 0d;
				PatientBill pb = Context.getService(BillingService.class)
						.getPatientBill(patientBillId);

				Float rate = pb.getBeneficiary().getInsurancePolicy()
						.getThirdParty().getRate();
				for (BillPayment bp : pb.getPayments()) {
					amountPaid = amountPaid + bp.getAmountPaid().doubleValue();
				}

				if (rate != null)
					amountPaidByThirdPart = ((pb.getAmount().doubleValue() * (rate)) / 100)
							- amountPaid;
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

}
