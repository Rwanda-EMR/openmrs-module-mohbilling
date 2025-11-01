package org.openmrs.module.mohbilling.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;

import java.util.List;
import java.util.Set;

public class CreateBillOnSaveLabAndPharmacyOrders{

    protected static final Log log = LogFactory.getLog(CreateBillOnSaveLabAndPharmacyOrders.class);

    public static void createBillOnSaveLabOrders(Set<Concept> labOrdersConceptsList, Patient patient) {
        log.debug("createBillOnSaveLabOrders: no operation as this will be handled by the interceptor");
    }

    public static void checkBilling(){
        System.out.println("Billing is checked");
    }

    public static void createBillOnSavePharmacyOrders(List<DrugOrderedAndQuantinty> phamacyDrugOrderList, Patient patient) {
        log.debug("createBillOnSavePharmacyOrders: no operation as this will be handled by the interceptor");
    }
}