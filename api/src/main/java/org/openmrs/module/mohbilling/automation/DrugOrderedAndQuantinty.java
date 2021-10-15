package org.openmrs.module.mohbilling.automation;

import org.openmrs.Drug;

import java.math.BigDecimal;

public class DrugOrderedAndQuantinty {
    public Drug drug;
    public BigDecimal quantity;

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
