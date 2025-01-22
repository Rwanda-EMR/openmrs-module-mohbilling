package org.openmrs.module.mohbilling.rest.dto;

import java.math.BigDecimal;

public class BillResponseDTO {
    private String patientBillId;
    private String insuranceBillId;
    private String thirdPartyBillId;
    private String globalBillId;
    private String obsId;
    private BigDecimal amount;

    public BillResponseDTO(String patientBillId, String insuranceBillId,
                           String thirdPartyBillId, String globalBillId,
                           String obsId, BigDecimal amount) {
        this.patientBillId = patientBillId;
        this.insuranceBillId = insuranceBillId;
        this.thirdPartyBillId = thirdPartyBillId;
        this.globalBillId = globalBillId;
        this.obsId = obsId;
        this.amount = amount;
    }

    public String getPatientBillId() {
        return patientBillId;
    }

    public void setPatientBillId(String patientBillId) {
        this.patientBillId = patientBillId;
    }

    public String getInsuranceBillId() {
        return insuranceBillId;
    }

    public void setInsuranceBillId(String insuranceBillId) {
        this.insuranceBillId = insuranceBillId;
    }

    public String getThirdPartyBillId() {
        return thirdPartyBillId;
    }

    public void setThirdPartyBillId(String thirdPartyBillId) {
        this.thirdPartyBillId = thirdPartyBillId;
    }

    public String getGlobalBillId() {
        return globalBillId;
    }

    public void setGlobalBillId(String globalBillId) {
        this.globalBillId = globalBillId;
    }

    public String getObsId() {
        return obsId;
    }

    public void setObsId(String obsId) {
        this.obsId = obsId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
