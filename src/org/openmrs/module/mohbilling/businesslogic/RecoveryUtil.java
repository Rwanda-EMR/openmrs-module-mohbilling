package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.RecoveryStatus;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.service.BillingService;

public class RecoveryUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	/**
	 * This si a Comparator to help to be able to sort recovery list in a DESC
	 * order
	 */

	private static Comparator<Recovery> RECOVERY_COMPARATOR = new Comparator<Recovery>() {

		// This is where the sorting happens.
		public int compare(Recovery recovery1, Recovery recovery2) {
			int periodDifference = recovery2.getStartPeriod().compareTo(
					recovery1.getStartPeriod());

			if (periodDifference == 0)
				return recovery2.getEndPeriod().compareTo(
						recovery1.getEndPeriod());
			else
				return periodDifference;
		}
	};

	/**
	 * Saves the Recovery Process information to the DB Conditions are as
	 * follows: The required fields are "StartPeriod" and "EndPeriod"
	 * 
	 * @param recovery
	 *            the Recovery information to be saved
	 * 
	 * @return true if saved successfully, false otherwise
	 */
	public static Recovery createRecovery(Recovery recovery) {

		if (recovery != null) {

			if (recovery.getStartPeriod() != null
					&& recovery.getEndPeriod() != null) {

				recovery.setCreator(Context.getAuthenticatedUser());
				recovery.setCreatedDate(new Date());
				recovery.setRetired(false);

				if (recovery.getRecoveryId() == null)
					recovery.setStatus(RecoveryStatus.UNPAID.getDescription());

				getService().saveRecovery(recovery);
			}

			return recovery;
		}

		return null;
	}

	/**
	 * Gets all recoveries sorting them by DESC order
	 * 
	 * @return
	 */
	public static List<Recovery> getAllRecoveries() {

		List<Recovery> recoveryList = getService().getAllRecoveries();

		Collections.sort(recoveryList, RECOVERY_COMPARATOR);

		return recoveryList;
	}

	/**
	 * Gets the Amount due by a given Third Party
	 * 
	 * @param startPeriod
	 *            starting period of recovery
	 * @param endPeriod
	 *            ending period of recovery
	 * @param thirdParty
	 *            third party to be matched
	 * @return null if nothing to pay, the amount otherwise
	 */
	public static BigDecimal getThirdPartyDueAmount(Date startPeriod,
			Date endPeriod, ThirdParty thirdParty) {

		BigDecimal allDueAmount = null;
		BigDecimal thirdPartyDueAmount = new BigDecimal(0);

		if (startPeriod != null && endPeriod != null && thirdParty != null) {

			Float thirdPartyRate = thirdParty.getRate().floatValue();
			allDueAmount = new BigDecimal(0);

			for (PatientBill bill : PatientBillUtil.getBillsByPeriod(
					startPeriod, endPeriod)) {

				if (bill.getBeneficiary().getInsurancePolicy().getThirdParty() != null) {
					if (bill.getBeneficiary().getInsurancePolicy()
							.getThirdParty().getThirdPartyId().intValue() == thirdParty
							.getThirdPartyId().intValue())

						allDueAmount = allDueAmount.add(bill.getAmount());
				}
			}

			if (thirdPartyRate != null && thirdPartyRate > 0f
					&& allDueAmount.compareTo(new BigDecimal(0)) > 0)

				thirdPartyDueAmount = new BigDecimal(thirdPartyRate).multiply(
						allDueAmount).divide(new BigDecimal(100));

		}

		return thirdPartyDueAmount;
	}

	/**
	 * Gets the Amount due by a given Insurance
	 * 
	 * @param startPeriod
	 *            starting period of recovery
	 * @param endPeriod
	 *            ending period of recovery
	 * @param thirdParty
	 *            third party to be matched
	 * @return null if nothing to pay, the amount otherwise
	 */
	public static BigDecimal getInsuranceDueAmount(Date startPeriod,
			Date endPeriod, Insurance insurance) {

		BigDecimal allDueAmount = null;
		BigDecimal thirdPartyDueAmount = new BigDecimal(0);

		if (startPeriod != null && endPeriod != null && insurance != null) {

			Float insuranceRate = insurance.getRateOnDate(startPeriod)
					.getRate();
			allDueAmount = new BigDecimal(0);

			for (PatientBill bill : PatientBillUtil.getBillsByPeriod(
					startPeriod, endPeriod)) {

				if (bill.getBeneficiary().getInsurancePolicy().getInsurance()
						.getInsuranceId().intValue() == insurance
						.getInsuranceId().intValue()) {
					allDueAmount = allDueAmount.add(bill.getAmount());
				}
			}

			if (insuranceRate != null && insuranceRate > 0f
					&& allDueAmount.compareTo(new BigDecimal(0)) > 0)

				thirdPartyDueAmount = new BigDecimal(insuranceRate).multiply(
						allDueAmount).divide(new BigDecimal(100));

		}

		return thirdPartyDueAmount;
	}

	/**
	 * Gets Recovery by recoveryId
	 * 
	 * @param recoveryId
	 *            the ID to match
	 * @return
	 */
	public static Recovery getRecovery(Integer recoveryId) {

		return getService().getRecovery(recoveryId);
	}

	/**
	 * Retires the Recovery that is selected: Sets the Changer and the Retirer
	 * 
	 * @param recovery
	 *            the Recovery object to be retired
	 */
	public static void retireRecovery(Recovery recovery) {

		recovery.setChangedBy(Context.getAuthenticatedUser());
		recovery.setRetired(true);
		recovery.setRetireReason("The recovery has been retired by : "
				+ recovery.getChangedBy().getPerson().getFamilyName()
				+ " UserID: " + recovery.getChangedBy().getUserId());
		recovery.setRetiredDate(new Date());
		recovery.setRetiredBy(Context.getAuthenticatedUser());

		getService().saveRecovery(recovery);
	}

	/**
	 * Sets Payment Status to what it should mean, depending on the Paid Amount
	 * by the Insurer.
	 * 
	 * @param recovery
	 *            the recovery to be paid
	 */
	public static void setPaymentStatus(Recovery recovery) {

		if (recovery.getDueAmount().floatValue() == recovery.getPaidAmount()
				.floatValue())
			recovery.setStatus(RecoveryStatus.FULLYPAID.getDescription());

		if (recovery.getPaidAmount().floatValue() == 0f)
			recovery.setStatus(RecoveryStatus.REFUSED.getDescription());

		if (recovery.getDueAmount().floatValue() > recovery.getPaidAmount()
				.floatValue())
			recovery.setStatus(RecoveryStatus.PARTLYPAID.getDescription());

		recovery.setChangedBy(Context.getAuthenticatedUser());
	}
}
