package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.irembo.util.IremboPayLogUtil;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Scheduled background task for Irembo reconciliation.
 *
 * Runs on an interval managed by OpenMRS Scheduler. The task is intentionally
 * lightweight and safe; extend this class with reconciliation logic as needed.
 */
public class IremboReconciliationTask extends AbstractTask {

    private static final Log log = LogFactory.getLog(IremboReconciliationTask.class);
    private static final int MAX_RETRY_ATTEMPTS = 20;
    private static final long REQUEST_DELAY_MS = 1000L;
    private static final long INVOICE_EXPIRY_HOURS = 24L;

    @Override
    public void execute() {
        try {
            BillingService billingService = Context.getService(BillingService.class);
            List<PatientBill> unpaidBills = billingService.getUnpaidBillsWithInvoiceNumber();
            if (unpaidBills == null || unpaidBills.isEmpty()) {
                log.debug("Irembo reconciliation task found no unpaid bills with invoice numbers.");
                return;
            }

            int checked = 0;
            int updated = 0;
            int expiredCleared = 0;
            for (PatientBill bill : unpaidBills) {
                if (bill == null || bill.getInvoiceNumber() == null || bill.getInvoiceNumber().trim().isEmpty()) {
                    continue;
                }
                boolean expired = isInvoiceExpired(bill);
                int currentRetryCount = bill.getRetryCount() != null ? bill.getRetryCount() : 0;
                if (currentRetryCount >= MAX_RETRY_ATTEMPTS) {
                    continue;
                }
                checked++;
                try {
                    bill.setRetryCount(currentRetryCount + 1);
                    billingService.savePatientBill(bill);
                    PatientBill latest = billingService.getInvoiceStatus(bill.getInvoiceNumber());
                    if (latest != null && Boolean.TRUE.equals(latest.getIsPaid())) {
                        updated++;
                    } else if (expired && isPendingStatus(latest)) {
                        // Invoice expired (>24h) and still pending on Irembo: clear invoice linkage so a new invoice can be initiated.
                        String oldInvoiceNumber = latest.getInvoiceNumber();
                        java.util.Date oldInitiatedAt = latest.getInitiatedAt();
                        Integer oldRetryCount = latest.getRetryCount();
                        latest.setInvoiceNumber(null);
                        latest.setInitiatedAt(null);
                        latest.setRetryCount(0);
                        latest.setReferenceId(null);
                        latest.setPaymentLinkUrl(null);
                        latest.setTransactionStatus("EXPIRED");
                        billingService.savePatientBill(latest);
                        log.info("Cleared expired Irembo invoice from PatientBill id=" + latest.getPatientBillId()
                                + ", oldInvoiceNumber=" + oldInvoiceNumber
                                + ", oldInitiatedAt=" + oldInitiatedAt
                                + ", oldRetryCount=" + oldRetryCount);
                        expiredCleared++;
                    }
                } catch (Exception perBillError) {
                    IremboPayLogUtil.logFailure(log, "RECONCILIATION",
                            "invoice status check failed for invoice " + bill.getInvoiceNumber(), perBillError);
                } finally {
                    try {
                        Thread.sleep(REQUEST_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Irembo reconciliation task delay interrupted; stopping pacing delay.", ie);
                    }
                }
            }
            log.info("Irembo reconciliation task completed: checked=" + checked + ", paidUpdated=" + updated
                    + ", expiredCleared=" + expiredCleared);
        } catch (Exception e) {
            IremboPayLogUtil.logFailure(log, "RECONCILIATION", "reconciliation task failed", e);
        }
    }

    private static boolean isInvoiceExpired(PatientBill bill) {
        if (bill.getInitiatedAt() == null) {
            return false;
        }
        Instant initiatedAt = bill.getInitiatedAt().toInstant();
        return Duration.between(initiatedAt, Instant.now()).toHours() >= INVOICE_EXPIRY_HOURS;
    }

    private static boolean isPendingStatus(PatientBill bill) {
        if (bill == null || bill.getTransactionStatus() == null) {
            return true;
        }
        String status = bill.getTransactionStatus().trim().toUpperCase();
        return status.isEmpty() || status.contains("PENDING");
    }
}
