package org.openmrs.module.mohbilling.rest.dto;

import java.util.Date;

/**
 * Simple response DTO for GlobalBill revert operation
 */
public class RevertGlobalBillResponse {
    
    private Integer globalBillId;
    private Boolean closed;
    private Date closingDate;
    private String closedBy;
    private String editingReason;
    private String editedBy;
    private String message;
    
    public RevertGlobalBillResponse() {}
    
    public RevertGlobalBillResponse(Integer globalBillId, String editingReason, String editedBy) {
        this.globalBillId = globalBillId;
        this.closed = false;
        this.closingDate = null;
        this.closedBy = null;
        this.editingReason = editingReason;
        this.editedBy = editedBy;
        this.message = "Global bill successfully reverted to unpaid status";
    }
    
    // Getters and Setters
    public Integer getGlobalBillId() {
        return globalBillId;
    }
    
    public void setGlobalBillId(Integer globalBillId) {
        this.globalBillId = globalBillId;
    }
    
    public Boolean getClosed() {
        return closed;
    }
    
    public void setClosed(Boolean closed) {
        this.closed = closed;
    }
    
    public Date getClosingDate() {
        return closingDate;
    }
    
    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }
    
    public String getClosedBy() {
        return closedBy;
    }
    
    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }
    
    public String getEditingReason() {
        return editingReason;
    }
    
    public void setEditingReason(String editingReason) {
        this.editingReason = editingReason;
    }
    
    public String getEditedBy() {
        return editedBy;
    }
    
    public void setEditedBy(String editedBy) {
        this.editedBy = editedBy;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
