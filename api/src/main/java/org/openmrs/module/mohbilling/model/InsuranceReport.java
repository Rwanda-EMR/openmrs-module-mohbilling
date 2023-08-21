package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.*;

public class InsuranceReport {

    private Map<String, BigDecimal> serviceTotalRevenues;

    private List<InsuranceReportItem> reportItems;

    public InsuranceReport() {
        serviceTotalRevenues = new HashMap<>();
        reportItems = new ArrayList<>();
    }

    public Map<String, BigDecimal> getServiceTotalRevenues() {
        return serviceTotalRevenues;
    }

    public void setServiceTotalRevenues(Map<String, BigDecimal> serviceTotalRevenues) {
        this.serviceTotalRevenues = serviceTotalRevenues;
    }

    public List<InsuranceReportItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(List<InsuranceReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    public void addServiceRevenue(String serviceName, BigDecimal serviceRevenue) {
        getServiceTotalRevenues().computeIfAbsent(serviceName, k -> BigDecimal.ZERO).add(serviceRevenue);
    }

    public void addReportItem(InsuranceReportItem reportItem) {
        getReportItems().add(reportItem);
    }
}
