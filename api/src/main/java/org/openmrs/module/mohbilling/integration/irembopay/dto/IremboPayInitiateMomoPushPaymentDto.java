/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.integration.irembopay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IremboPayInitiateMomoPushPaymentDto {
    /**
     * Identifier of the user account to be charged. Phone numbers should be valid Rwanda phone numbers in the format 07********
     */
    private String accountIdentifier;

    /**
     * Possible values: MTN, AIRTEL.
     */
    private String paymentProvider;

    /**
     * Invoice Number to be charged to the user.
     */
    private String invoiceNumber;

    public IremboPayInitiateMomoPushPaymentDto() {
        // no-arg constructor for Jackson
    }

    public IremboPayInitiateMomoPushPaymentDto(String accountIdentifier, String paymentProvider, String invoiceNumber) {
        this.accountIdentifier = accountIdentifier;
        this.paymentProvider = paymentProvider;
        this.invoiceNumber = invoiceNumber;
    }

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(String accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
}
