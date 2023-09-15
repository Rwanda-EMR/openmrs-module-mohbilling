package org.openmrs.module.mohbilling.model;

import org.openmrs.User;

import java.math.BigDecimal;
import java.util.List;

public class AllServiceRevenueCons {
    private Consommation consommation;
    private List<ServiceRevenue> revenues;
    private BigDecimal allDueAmounts;
    private BigDecimal bigTotal;
    private BigDecimal patientTotal;
    private BigDecimal allpaidAmount;
    private String reportingPeriod;
    private User collector;

    public AllServiceRevenueCons(BigDecimal allDueAmounts, BigDecimal allPaidAmount,String reportingPeriod) {
        this.allDueAmounts =allDueAmounts;
        this.allpaidAmount =allPaidAmount;
        this.reportingPeriod = reportingPeriod;
    }

    public Consommation getConsommation() {
        return consommation;
    }

    public void setConsommation(Consommation consommation) {
        this.consommation = consommation;
    }

    public List<ServiceRevenue> getRevenues() {
        return revenues;
    }

    public void setRevenues(List<ServiceRevenue> revenues) {
        this.revenues = revenues;
    }

    public BigDecimal getAllDueAmounts() {
        return allDueAmounts;
    }

    public void setAllDueAmounts(BigDecimal allDueAmounts) {
        this.allDueAmounts = allDueAmounts;
    }

    public BigDecimal getBigTotal() {
        return bigTotal;
    }

    public void setBigTotal(BigDecimal bigTotal) {
        this.bigTotal = bigTotal;
    }

    public BigDecimal getPatientTotal() {
        return patientTotal;
    }

    public void setPatientTotal(BigDecimal patientTotal) {
        this.patientTotal = patientTotal;
    }

    public BigDecimal getAllpaidAmount() {
        return allpaidAmount;
    }

    public void setAllpaidAmount(BigDecimal allpaidAmount) {
        this.allpaidAmount = allpaidAmount;
    }

    public String getReportingPeriod() {
        return reportingPeriod;
    }

    public void setReportingPeriod(String reportingPeriod) {
        this.reportingPeriod = reportingPeriod;
    }

    public User getCollector() {
        return collector;
    }

    public void setCollector(User collector) {
        this.collector = collector;
    }
}