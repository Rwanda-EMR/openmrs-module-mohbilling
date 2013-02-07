/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

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

		return "" + amountPaid;
	}

	public static String getTotalAmountNotPaidByPatientBill(
			Integer patientBillId) {
		Double amountNotPaid = 0d;
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

			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		return "" + amountNotPaid;
	}

}
