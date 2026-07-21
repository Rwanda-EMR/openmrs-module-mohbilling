package org.openmrs.module.mohbilling.rest.controller;

import java.sql.SQLIntegrityConstraintViolationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackRequest;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackResource;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles POST /openmrs/ws/rest/v1/mohbilling/irembopay/callback with explicit HTTP status codes.
 * This avoids the generic REST update handler, which always returns 200 via RestUtil.updated().
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/mohbilling/irembopay")
public class IrembopayCallbackRestController {

    private static final Log log = LogFactory.getLog(IrembopayCallbackRestController.class);

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<SimpleObject> handleCallback(@RequestBody SimpleObject body) {
        log.info("Irembopay callback: dedicated REST controller received request");
        try {
            IrembopayCallbackResource callbackResource = (IrembopayCallbackResource) Context.getService(RestService.class)
                .getResourceByName(RestConstants.VERSION_1 + "/mohbilling/irembopay/callback");
            IrembopayCallbackRequest delegate = callbackResource.convert(body);
            IrembopayCallbackProcessor.process(delegate);
            delegate.setSuccess(true);

            SimpleObject ok = new SimpleObject();
            ok.put("success", true);
            ok.put("data", delegate);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            // A duplicate payment unique-key violation means this callback was already processed
            // (payment already exists). Treat it as idempotent success so Irembo stops retrying.
            if (isDuplicatePaymentViolation(e)) {
                log.warn("Irembopay callback: duplicate payment detected; treating as already completed");
                SimpleObject alreadyCompleted = new SimpleObject();
                alreadyCompleted.put("success", true);
                alreadyCompleted.put("message", "Transaction already completed");
                return ResponseEntity.ok(alreadyCompleted);
            }

            String missingBillMessage = extractMissingPatientBillMessage(e);
            if (missingBillMessage != null) {
                log.warn("Irembopay callback: " + missingBillMessage);
                SimpleObject notFound = new SimpleObject();
                notFound.put("success", false);
                notFound.put("message", missingBillMessage);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(notFound);
            }

            log.error("Irembopay callback: processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(IrembopayCallbackErrorResponse.fromThrowable(e));
        }
    }

    /**
     * Detects a duplicate payment unique-constraint violation (uk_moh_bill_payment_invoice_reference)
     * anywhere in the exception cause chain. Matches by exception type and by message content so it
     * works whether the SQL exception is wrapped by Hibernate/Spring or not.
     */
    private boolean isDuplicatePaymentViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            String className = current.getClass().getName();
            if (className.equals("org.hibernate.exception.ConstraintViolationException")) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("duplicate entry")
                        || lower.contains("uk_moh_bill_payment_invoice_reference")) {
                    return true;
                }
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Returns the "No PatientBill found for invoice number ..." message if present in the cause chain,
     * otherwise null.
     */
    private String extractMissingPatientBillMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.startsWith("No PatientBill found for invoice number ")) {
                return message;
            }
            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        return null;
    }
}
