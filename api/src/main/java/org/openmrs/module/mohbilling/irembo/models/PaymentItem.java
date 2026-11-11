package org.openmrs.module.mohbilling.irembo.models;

public class PaymentItem {
    private String code;
    private int quantity;
    private double unitAmount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(double unitAmount) {
        this.unitAmount = unitAmount;
    }

    @Override
    public String toString() {
        return "PaymentItem{" +
                "code='" + code + '\'' +
                ", quantity=" + quantity +
                ", unitAmount=" + unitAmount +
                '}';
    }
}
