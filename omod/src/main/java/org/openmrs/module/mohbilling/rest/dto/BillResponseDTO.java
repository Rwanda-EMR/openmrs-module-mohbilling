package org.openmrs.module.mohbilling.rest.dto;

import java.math.BigDecimal;

public class BillResponseDTO {
    private Integer patientBillId;
    private Integer insuranceBillId;
    private Integer thirdPartyBillId;
    private Integer globalBillId;
    private BigDecimal amount;

    public BillResponseDTO(Integer patientBillId, Integer insuranceBillId,
                           Integer thirdPartyBillId, Integer globalBillId,
                           BigDecimal amount) {
        this.patientBillId = patientBillId;
        this.insuranceBillId = insuranceBillId;
        this.thirdPartyBillId = thirdPartyBillId;
        this.globalBillId = globalBillId;
        this.amount = amount;
    }

    public Integer getPatientBillId() {
        return patientBillId;
    }

    public void setPatientBillId(Integer patientBillId) {
        this.patientBillId = patientBillId;
    }

    public Integer getInsuranceBillId() {
        return insuranceBillId;
    }

    public void setInsuranceBillId(Integer insuranceBillId) {
        this.insuranceBillId = insuranceBillId;
    }

    public Integer getThirdPartyBillId() {
        return thirdPartyBillId;
    }

    public void setThirdPartyBillId(Integer thirdPartyBillId) {
        this.thirdPartyBillId = thirdPartyBillId;
    }

    public Integer getGlobalBillId() {
        return globalBillId;
    }

    public void setGlobalBillId(Integer globalBillId) {
        this.globalBillId = globalBillId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}