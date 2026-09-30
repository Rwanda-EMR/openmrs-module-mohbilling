package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.CashPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Auto-creates and confirms cash payments for unpaid patient bills whose due
 * amount is zero and that are older than {@code mohbilling.autopaydelays}.
 */
public class AutoPayZeroBillTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(AutoPayZeroBillTask.class);
	private static final String MISSING_USER_MESSAGE = "Configured user for auto pay zero bills not found";

	@Override
	public void execute() {
		try {
			boolean enabled = Boolean.parseBoolean(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_AUTOPAY_ZERO_BILL, "false"));
			if (!enabled) {
				log.debug("Auto-pay zero bill task skipped: mohbilling.autopayzerobill is false.");
				return;
			}

			long delaySeconds = parsePositiveLong(
					Context.getAdministrationService().getGlobalProperty(
							BillingConstants.GLOBAL_PROPERTY_AUTOPAY_DELAYS,
							String.valueOf(BillingConstants.DEFAULT_AUTOPAY_DELAYS_SECONDS)),
					BillingConstants.DEFAULT_AUTOPAY_DELAYS_SECONDS);

			Date cutoff = new Date(System.currentTimeMillis() - (delaySeconds * 1000L));
			BillingService billingService = Context.getService(BillingService.class);
			List<PatientBill> bills = billingService.getUnpaidZeroDueBillsCreatedOnOrBefore(cutoff);
			if (bills == null || bills.isEmpty()) {
				log.debug("Auto-pay zero bill task found no eligible bills older than " + delaySeconds + "s.");
				return;
			}

			User autoPayUser = resolveAutoPayUser();
			if (autoPayUser == null) {
				log.warn(MISSING_USER_MESSAGE);
			}

			int paid = 0;
			int skipped = 0;
			for (PatientBill bill : bills) {
				if (bill == null || bill.getPatientBillId() == null) {
					skipped++;
					continue;
				}
				try {
					if (payZeroDueBill(billingService, bill, autoPayUser)) {
						paid++;
					} else {
						skipped++;
					}
				} catch (Exception perBillError) {
					skipped++;
					log.error("Auto-pay zero bill failed for patientBillId=" + bill.getPatientBillId(), perBillError);
				}
			}
			log.info("Auto-pay zero bill task completed: candidates=" + bills.size()
					+ ", paid=" + paid + ", skipped=" + skipped
					+ ", delaySeconds=" + delaySeconds);
		} catch (Exception e) {
			log.error("Auto-pay zero bill task failed", e);
		}
	}

	private static User resolveAutoPayUser() {
		String username = Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_AUTOPAY_USER, "");
		if (username == null || username.trim().isEmpty()) {
			return null;
		}
		User user = Context.getUserService().getUserByUsername(username.trim());
		if (user == null || Boolean.TRUE.equals(user.getRetired())) {
			return null;
		}
		if (user.getPerson() != null && Boolean.TRUE.equals(user.getPerson().getVoided())) {
			return null;
		}
		return user;
	}

	/**
	 * Creates a zero cash payment and confirms the bill when eligible.
	 *
	 * @return true when the bill was marked paid/confirmed
	 */
	private static boolean payZeroDueBill(BillingService billingService, PatientBill bill, User autoPayUser) {
		PatientBill current = billingService.getPatientBill(bill.getPatientBillId());
		if (current == null || Boolean.TRUE.equals(current.getVoided())) {
			return false;
		}
		if (Boolean.TRUE.equals(current.getIsPaid()) && current.isPaymentConfirmed()) {
			return false;
		}
		BigDecimal amount = current.getAmount() != null ? current.getAmount() : BigDecimal.ZERO;
		if (amount.compareTo(BigDecimal.ZERO) != 0) {
			return false;
		}

		boolean hasNonVoidedPayment = false;
		Set<BillPayment> billPayments = current.getPayments();
		if (billPayments != null) {
			for (BillPayment payment : billPayments) {
				if (payment != null && !Boolean.TRUE.equals(payment.getVoided())) {
					hasNonVoidedPayment = true;
					break;
				}
			}
		}

		Date now = new Date();
		CashPayment cashPayment = null;
		if (!hasNonVoidedPayment) {
			CashPayment cp = new CashPayment();
			cp.setAmountPaid(BigDecimal.ZERO);
			cp.setDateReceived(now);
			cp.setPatientBill(current);
			cp.setCollector(autoPayUser);
			cp.setCreator(autoPayUser);
			cp.setCreatedDate(now);
			cp.setVoided(false);
			cashPayment = PatientBillUtil.createCashPayment(cp);

			Consommation consommation = billingService.getConsommationByPatientBill(current);
			if (consommation != null && consommation.getBillItems() != null) {
				for (PatientServiceBill psb : consommation.getBillItems()) {
					if (psb == null || Boolean.TRUE.equals(psb.getVoided())) {
						continue;
					}
					PaidServiceBill paidSb = new PaidServiceBill();
					paidSb.setBillItem(psb);
					BigDecimal paidQuantity = psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO;
					paidSb.setPaidQty(paidQuantity);
					paidSb.setBillPayment(cashPayment);
					paidSb.setCreator(autoPayUser);
					paidSb.setCreatedDate(now);
					paidSb.setVoided(false);
					BillPaymentUtil.createPaidServiceBill(paidSb);

					psb.setPaid(true);
					psb.setPaidQuantity(paidQuantity);
					ConsommationUtil.createPatientServiceBill(psb);
				}
			}
		}

		current.setIsPaid(true);
		current.setPaymentConfirmed(true);
		current.setPaymentConfirmedDate(now);
		current.setPaymentConfirmedBy(autoPayUser);
		if (current.getPaidAt() == null) {
			current.setPaidAt(now);
		}
		PatientBillUtil.savePatientBill(current);

		log.info("Auto-paid zero-due patientBillId=" + current.getPatientBillId()
				+ (cashPayment != null
						? ", cashPaymentId=" + cashPayment.getBillPaymentId()
						: ", existingPaymentReused=true")
				+ ", collector=" + (autoPayUser != null ? autoPayUser.getUsername() : "none"));
		return true;
	}

	private static long parsePositiveLong(String configured, long defaultValue) {
		try {
			long value = Long.parseLong(configured != null ? configured.trim() : "");
			return value > 0 ? value : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
