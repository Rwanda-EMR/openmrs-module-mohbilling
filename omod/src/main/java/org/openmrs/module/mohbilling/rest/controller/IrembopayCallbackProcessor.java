package org.openmrs.module.mohbilling.rest.controller;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackRequest;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * Shared logic for processing Irembo Pay callbacks. Delegates to
 * {@link BillingService#processIrembopayCallback} so the whole process runs in a single
 * transaction (any failure rolls back all DB changes).
 */
public class IrembopayCallbackProcessor {

    private static final Log log = LogFactory.getLog(IrembopayCallbackProcessor.class);

    /**
     * Process a successful callback in a single transaction: validate, update PatientBill,
     * create BillPayment, update PatientServiceBills and create PaidServiceBills.
     * Safe to call when delegate.getData() or invoice number is null; logs and returns.
     * Throws IllegalArgumentException on validation failure; any other exception rolls back the transaction.
     */
    public static void process(IrembopayCallbackRequest delegate) {
        if (delegate == null || delegate.getData() == null || delegate.getData().getInvoiceNumber() == null) {
            log.warn("Irembopay callback: skipped (missing data or invoice number)");
            return;
        }
        String invoiceNumber = delegate.getData().getInvoiceNumber();
        log.info("Irembopay callback: invoiceNumber=" + invoiceNumber + ", transactionId=" + delegate.getData().getTransactionId());

        BigDecimal callbackAmount = delegate.getData().getAmount() == null
            ? null
            : BigDecimal.valueOf(delegate.getData().getAmount());

        BillingService billingService = Context.getService(BillingService.class);
        if ("BATCH".equalsIgnoreCase(delegate.getData().getType())) {
            String batchNumber = delegate.getData().getBatchNumber();
            if (batchNumber == null || batchNumber.trim().isEmpty()) {
                batchNumber = invoiceNumber;
            }
            billingService.processIrembopayBatchCallback(
                batchNumber,
                delegate.getData().getChildInvoices(),
                delegate.getSuccess(),
                delegate.getData().getPaymentReference(),
                delegate.getData().getPaidAt(),
                delegate.getData().getPaymentStatus());
            log.info("Irembopay callback: confirmed batch payment for batch " + batchNumber);
            return;
        }

        billingService.processIrembopayCallback(
            invoiceNumber,
            delegate.getSuccess(),
            callbackAmount,
            delegate.getData().getPaymentReference(),
            delegate.getData().getPaidAt(),
            delegate.getData().getPaymentStatus());

        log.info("Irembopay callback: confirmed payment for invoice " + invoiceNumber);
    }
}
