package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.scheduler.tasks.AbstractTask;

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
            for (PatientBill bill : unpaidBills) {
                if (bill == null || bill.getInvoiceNumber() == null || bill.getInvoiceNumber().trim().isEmpty()) {
                    continue;
                }
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
                    }
                } catch (Exception perBillError) {
                    log.error("Failed invoice status check for invoice " + bill.getInvoiceNumber(), perBillError);
                } finally {
                    try {
                        Thread.sleep(REQUEST_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Irembo reconciliation task delay interrupted; stopping pacing delay.", ie);
                    }
                }
            }
            log.info("Irembo reconciliation task completed: checked=" + checked + ", paidUpdated=" + updated);
        } catch (Exception e) {
            log.error("Error while running Irembo reconciliation task", e);
        }
    }
}
