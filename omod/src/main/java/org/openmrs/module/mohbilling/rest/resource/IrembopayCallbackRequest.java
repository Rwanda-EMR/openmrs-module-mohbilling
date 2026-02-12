package org.openmrs.module.mohbilling.rest.resource;

import java.util.List;

/**
 * DTO for Irembo Pay callback POST body at
 * POST /openmrs/ws/rest/v1/mohbilling/irembopay/callback
 */
public class IrembopayCallbackRequest {

    private Boolean success;
    private IrembopayCallbackData data;
    private String error;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public IrembopayCallbackData getData() {
        return data;
    }

    public void setData(IrembopayCallbackData data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /** Nested data object in the callback payload. */
    public static class IrembopayCallbackData {
        private Double amount;
        private String currency;
        private String invoiceNumber;
        private String transactionId;
        private String createdAt;
        private String updatedAt;
        private String paidAt;
        private String expiryAt;
        private String paymentStatus;
        private String type;
        private String paymentMethod;
        private String paymentReference;
        private IrembopayCallbackCustomer customer;
        private List<IrembopayPaymentItem> paymentItems;
        private String paymentAccountIdentifier;

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public String getPaidAt() { return paidAt; }
        public void setPaidAt(String paidAt) { this.paidAt = paidAt; }
        public String getExpiryAt() { return expiryAt; }
        public void setExpiryAt(String expiryAt) { this.expiryAt = expiryAt; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
        public IrembopayCallbackCustomer getCustomer() { return customer; }
        public void setCustomer(IrembopayCallbackCustomer customer) { this.customer = customer; }
        public List<IrembopayPaymentItem> getPaymentItems() { return paymentItems; }
        public void setPaymentItems(List<IrembopayPaymentItem> paymentItems) { this.paymentItems = paymentItems; }
        public String getPaymentAccountIdentifier() { return paymentAccountIdentifier; }
        public void setPaymentAccountIdentifier(String paymentAccountIdentifier) { this.paymentAccountIdentifier = paymentAccountIdentifier; }
    }

    /** Customer info in the callback. */
    public static class IrembopayCallbackCustomer {
        private String email;
        private String phoneNumber;
        private String name;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /** Single payment item. */
    public static class IrembopayPaymentItem {
        private String code;
        private Integer quantity;
        private Double unitAmount;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getUnitAmount() { return unitAmount; }
        public void setUnitAmount(Double unitAmount) { this.unitAmount = unitAmount; }
    }
}
