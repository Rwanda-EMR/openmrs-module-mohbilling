package org.openmrs.module.mohbilling.rest.controller;

import org.openmrs.module.webservices.rest.SimpleObject;

/**
 * Builds callback error payloads without stack trace details.
 */
public final class IrembopayCallbackErrorResponse {

    private IrembopayCallbackErrorResponse() {
    }

    public static SimpleObject build(String code, String message) {
        SimpleObject response = new SimpleObject();
        response.put("success", false);
        SimpleObject error = new SimpleObject();
        error.put("code", code);
        error.put("message", message);
        response.put("error", error);
        return response;
    }

    public static SimpleObject fromThrowable(Throwable throwable) {
        String code = throwable instanceof IllegalArgumentException ? "VALIDATION_ERROR" : "PROCESSING_ERROR";
        return build(code, extractMessage(throwable));
    }

    private static String extractMessage(Throwable throwable) {
        if (throwable == null) {
            return "Callback processing failed";
        }
        String message = throwable.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            return extractMessage(throwable.getCause());
        }
        return "Callback processing failed";
    }
}
