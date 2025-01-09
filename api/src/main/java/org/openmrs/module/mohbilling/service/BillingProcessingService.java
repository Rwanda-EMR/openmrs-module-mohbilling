package org.openmrs.module.mohbilling.service;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.module.mohbilling.model.PatientBill;

import java.util.Date;
import java.util.List;

public interface BillingProcessingService {
    PatientBill createBill(Patient patient, List<Obs> observations, String insuranceCardNumber);
    PatientBill getPatientBillById(int id);
    void voidPatientBill(PatientBill patientBill, User voidedBy, Date voidedDate, String voidReason);
    PatientBill savePatientBill(PatientBill patientBill);
    List<PatientBill> getAllPatientBills();
    /**
     * Gets a paginated list of patient bills
     * 
     * @param startIndex starting index for pagination
     * @param pageSize number of records per page
     * @return List<PatientBill> paginated list of bills
     */
    List<PatientBill> getPatientBillsByPagination(Integer startIndex, Integer pageSize,
        String orderBy, String orderDirection);
}