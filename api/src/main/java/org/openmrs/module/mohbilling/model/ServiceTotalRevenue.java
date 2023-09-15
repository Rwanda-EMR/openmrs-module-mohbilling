package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;

public class ServiceTotalRevenue {

    private String serviceName;
    private BigDecimal serviceTotalAmount;

    public ServiceTotalRevenue(String serviceName, BigDecimal serviceTotalAmount) {
        this.serviceName = serviceName;
        this.serviceTotalAmount = serviceTotalAmount;
    }

    /**
     * @return the service
     */
    public String getService() {
        return serviceName;
    }


    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.serviceName = service;
    }

    /**
     * @return the dueAmount
     */
    public BigDecimal getDueAmount() {
        return serviceTotalAmount;
    }


    /**
     * @param dueAmount the dueAmount to set
     */
    public void setDueAmount(BigDecimal dueAmount) {
        this.serviceTotalAmount = dueAmount;
    }
}
