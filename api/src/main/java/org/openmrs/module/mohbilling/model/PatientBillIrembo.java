package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.Date;

public class PatientBillIrembo {
    private Integer patientBillId;
    private Date billDate;
    private BigDecimal amount = new BigDecimal(0);
    private String phoneNumber;
    private String invoiceNumber;

    private String department;

    public Integer getPatientBillId() {
        return patientBillId;
    }

    public void setPatientBillId(Integer patientBillId) {
        this.patientBillId = patientBillId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }
}
