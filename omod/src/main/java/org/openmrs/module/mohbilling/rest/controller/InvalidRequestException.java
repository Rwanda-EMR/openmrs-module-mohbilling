package org.openmrs.module.mohbilling.rest.controller;

import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * Exception for invalid request validation errors (HTTP 400 Bad Request).
 * Extends ResponseException so the REST framework handles it properly.
 */
public class InvalidRequestException extends ResponseException {

    private static final long serialVersionUID = 1L;

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
