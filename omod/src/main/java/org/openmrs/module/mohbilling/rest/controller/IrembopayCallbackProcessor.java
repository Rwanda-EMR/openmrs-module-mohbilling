package org.openmrs.module.mohbilling.rest.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackRequest;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * Shared logic for processing Irembo Pay callbacks: update PatientBill, create BillPayment,
 * update PatientServiceBills and create PaidServiceBills. Used by both the REST resource
 * (which actually receives the callback in OpenMRS) and the custom controller.
 */
public class IrembopayCallbackProcessor {

    private static final Log log = LogFactory.getLog(IrembopayCallbackProcessor.class);

    /**
     * Process a successful callback: persist payment and update related records.
     * Safe to call when delegate.getData() or invoice number is null; logs and returns.
     */
    public static void process(IrembopayCallbackRequest delegate) {
        if (delegate == null || delegate.getData() == null || delegate.getData().getInvoiceNumber() == null) {
            log.warn("Irembopay callback: skipped (missing data or invoice number)");
            return;
        }
        String invoiceNumber = delegate.getData().getInvoiceNumber();
        log.info("Irembopay callback: invoiceNumber=" + invoiceNumber + ", transactionId=" + delegate.getData().getTransactionId());

        BillingService billingService = Context.getService(BillingService.class);
        PatientBill billToConfirm = billingService.getPatientBillByInvoiceNumber(invoiceNumber);

        if (billToConfirm == null) {
            log.warn("Irembopay callback: no PatientBill found for invoice number " + invoiceNumber);
            throw new IllegalArgumentException("No PatientBill found for invoice number " + invoiceNumber);
        }
        if (!Boolean.TRUE.equals(delegate.getSuccess())) {
            log.info("Irembopay callback: success=false, not updating bill " + billToConfirm.getPatientBillId());
            return;
        }

        // Validate that callback amount matches bill amount
        if (delegate.getData().getAmount() == null) {
            String errorMsg = "Callback amount is missing for invoice " + invoiceNumber;
            log.error("Irembopay callback: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        BigDecimal callbackAmount = BigDecimal.valueOf(delegate.getData().getAmount()).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal billAmount = billToConfirm.getAmount().setScale(2, java.math.RoundingMode.HALF_UP);
        if (callbackAmount.compareTo(billAmount) != 0) {
            String errorMsg = String.format("Amount mismatch: callback amount=%.2f, bill amount=%.2f for invoice %s",
                callbackAmount.doubleValue(), billAmount.doubleValue(), invoiceNumber);
            log.error("Irembopay callback: " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        billToConfirm.setIsPaid(true);
        billToConfirm.setPaymentReference(delegate.getData().getPaymentReference());
        billToConfirm.setPaymentConfirmed(true);
        billToConfirm.setPaymentConfirmedBy(Context.getAuthenticatedUser());
        billToConfirm.setPaymentConfirmedDate(new Date());
        Date paidAt = parsePaidAt(delegate.getData().getPaidAt());
        if (paidAt != null) {
            billToConfirm.setPaidAt(paidAt);
        } else {
            billToConfirm.setPaidAt(new Date());
        }
        billingService.savePatientBill(billToConfirm);

        Date paymentDate = billToConfirm.getPaidAt() != null ? billToConfirm.getPaidAt() : new Date();
        BigDecimal amountPaid = billToConfirm.getAmount();
        if (delegate.getData().getAmount() != null) {
            amountPaid = BigDecimal.valueOf(delegate.getData().getAmount());
        }

        BillPayment billPayment = new BillPayment();
        billPayment.setAmountPaid(amountPaid);
        billPayment.setDateReceived(paymentDate);
        billPayment.setPatientBill(billToConfirm);
        billPayment.setCollector(Context.getAuthenticatedUser());
        billPayment.setCreator(Context.getAuthenticatedUser());
        billPayment.setCreatedDate(new Date());
        billPayment.setVoided(false);
        billingService.saveBillPayment(billPayment);

        Consommation affectedConsommation = billingService.getConsommationByPatientBill(billToConfirm);
        if (affectedConsommation != null && affectedConsommation.getBillItems() != null) {
            Set<PatientServiceBill> billItems = affectedConsommation.getBillItems();
            for (PatientServiceBill psb : billItems) {
                PaidServiceBill paidSb = new PaidServiceBill();
                paidSb.setBillItem(psb);
                BigDecimal paidQuantity = psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO;
                paidSb.setPaidQty(paidQuantity);
                paidSb.setBillPayment(billPayment);
                paidSb.setCreator(Context.getAuthenticatedUser());
                paidSb.setCreatedDate(new Date());
                paidSb.setVoided(false);
                BillPaymentUtil.createPaidServiceBill(paidSb);

                psb.setPaid(true);
                psb.setPaidQuantity(psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO);
                ConsommationUtil.createPatientServiceBill(psb);
            }
        }

        log.info("Irembopay callback: confirmed payment for PatientBill id=" + billToConfirm.getPatientBillId());
    }

    static Date parsePaidAt(String paidAtStr) {
        if (paidAtStr == null || paidAtStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(paidAtStr.trim());
        } catch (Exception e1) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(paidAtStr.trim());
            } catch (Exception e2) {
                log.warn("Irembopay callback: could not parse paidAt " + paidAtStr, e2);
                return null;
            }
        }
    }
}
