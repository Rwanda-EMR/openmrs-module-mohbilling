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

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IremboPayCreateInvoiceResponseDataDto {
    /**
     * Amount of the invoice. Calculation method : Sum of (Product unit amount x quantity)
     */
    private int amount;

    /**
     * Identifier of the invoice that the end-user will use to do the payment.
     */
    private String invoiceNumber;

    /**
     * Transaction Identifier provided during the creation of the invoice.
     */
    private String transactionId;

    /**
     * n Internet date and time format. eg.2022-11-27T16:24:51.000+02:00
     */
    private String createdAt;

    /**
     * In Internet date and time format. eg.2022-11-27T16:24:51.000+02:00
     */
    private String updatedAt;

    /**
     * In Internet date and time format. eg.2022-11-27T16:24:51.000+02:00
     */
    private String expiryAt;

    /**
     * Identifier of the Payment Account where the money will be deposited.
     */
    private String paymentAccountIdentifier;

    /**
     * Products for which this invoice has been created.
     */
    private List<PaymentItemDto> paymentItems;

    /**
     * Optional description provided at invoice creation.
     */
    private String description;

    /**
     * SINGLE or BATCH
     */
    private String type;

    /**
     * Payment status of the invoice:
     * NEW: invoice created and not paid yet.
     * PAID: paid invoice.
     */
    private String paymentStatus;

    /**
     * Currency of the invoice which is the same as the currency of the payment account identifier. Possible values: "RWF", "EUR", "GBP", "USD".
     */
    private String currency;

    /**
     * An optional customer object who will be notified about this invoice.
     */
    private CustomerDto customer;

    /**
     * Identifier of the user who created the invoice.
     */
    private String createdBy;

    /**
     * Language provided during the creation of the invoice.
     * Possible values: FR, EN, RW.
     */
    private String language;

    /**
     * A URL that directs the merchants' end-user to a checkout page where they can make a payment.
     */
    private String paymentLinkUrl;

    public IremboPayCreateInvoiceResponseDataDto() {
        // no-arg constructor for Jackson
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getExpiryAt() {
        return expiryAt;
    }

    public void setExpiryAt(String expiryAt) {
        this.expiryAt = expiryAt;
    }

    public String getPaymentAccountIdentifier() {
        return paymentAccountIdentifier;
    }

    public void setPaymentAccountIdentifier(String paymentAccountIdentifier) {
        this.paymentAccountIdentifier = paymentAccountIdentifier;
    }

    public List<PaymentItemDto> getPaymentItems() {
        return paymentItems;
    }

    public void setPaymentItems(List<PaymentItemDto> paymentItems) {
        this.paymentItems = paymentItems;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDto customer) {
        this.customer = customer;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPaymentLinkUrl() {
        return paymentLinkUrl;
    }

    public void setPaymentLinkUrl(String paymentLinkUrl) {
        this.paymentLinkUrl = paymentLinkUrl;
    }
}
