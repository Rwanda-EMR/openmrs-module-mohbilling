package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InsuranceReport {

    private Map<String, BigDecimal> serviceTotalRevenues;

    private List<InsuranceReportItem> reportItems;

    public InsuranceReport() {
        serviceTotalRevenues = new LinkedHashMap<>();
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
        BigDecimal revenue = getServiceTotalRevenues().getOrDefault(serviceName, BigDecimal.ZERO);
        BigDecimal newRevenue = revenue.add(serviceRevenue);
        getServiceTotalRevenues().put(serviceName, newRevenue);
    }

    public void addReportItem(InsuranceReportItem reportItem) {
        getReportItems().add(reportItem);
    }
}
