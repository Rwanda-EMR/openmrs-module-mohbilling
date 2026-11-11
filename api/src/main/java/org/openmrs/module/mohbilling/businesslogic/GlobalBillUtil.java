/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author emr
 *
 */
public class GlobalBillUtil {	
	
	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	/**
	 * Save global to DB
	 * @param globalBill
	 * @return 
	 */
	public static GlobalBill saveGlobalBill(GlobalBill globalBill){
		syncAdmissionDischargeFields(globalBill);
		return getService().saveGlobalBill(globalBill);
	}

	/**
	 * When a global bill is closed/discharged, keep {@code moh_bill_admission.discharged_by}
	 * and {@code discharging_date} aligned with {@code closed_by} / {@code closing_date}.
	 */
	static void syncAdmissionDischargeFields(GlobalBill globalBill) {
		if (globalBill == null || globalBill.getAdmission() == null) {
			return;
		}
		if (!Boolean.TRUE.equals(globalBill.getClosed()) && globalBill.getClosingDate() == null) {
			return;
		}
		Admission admission = globalBill.getAdmission();
		User discharger = globalBill.getClosedBy();
		if (discharger == null) {
			discharger = Context.getAuthenticatedUser();
		}
		Date dischargeDate = globalBill.getClosingDate() != null ? globalBill.getClosingDate() : new Date();
		boolean dirty = false;
		Integer currentDischargerId = admission.getDischargedBy() != null
				? admission.getDischargedBy().getUserId() : null;
		Integer newDischargerId = discharger != null ? discharger.getUserId() : null;
		if (discharger != null && (currentDischargerId == null || !currentDischargerId.equals(newDischargerId))) {
			admission.setDischargedBy(discharger);
			dirty = true;
		}
		if (admission.getDischargingDate() == null
				|| (globalBill.getClosingDate() != null
						&& !globalBill.getClosingDate().equals(admission.getDischargingDate()))) {
			admission.setDischargingDate(dischargeDate);
			dirty = true;
		}
		if (dirty) {
			AdmissionUtil.savePatientAdmission(admission);
		}
	}
	public static GlobalBill getGlobalBillByAdmission(Admission admission){
		
		return getService().getGlobalBillByAdmission(admission);
		
	}
	public static List<GlobalBill> getGlobalBillsByInsurancePolicy(InsurancePolicy ip){
	
		List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
	    List<Admission> admissions = AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);
	    System.out.println("Admissions size"+admissions.size());
	   for (Admission admission : admissions) {
		   GlobalBill globalBill = GlobalBillUtil.getGlobalBillByAdmission(admission);
		   if(globalBill !=null){
			   globalBills.add(globalBill);
		   }
		  
	}
	return globalBills;
		
	}

	public static GlobalBill getGlobalBill(Integer globalBillId) {
		
	return 	getService().GetGlobalBill(globalBillId);
		
	}

	/**
	 * Get GlobalBill by bill identifier
	 * @param billIdentifier matching with globalBill
	 * @return GlobalBill
	 */
	public static GlobalBill getGlobalBillByBillIdentifier(String billIdentifier) {
		
		return getService().getGlobalBillByBillIdentifier(billIdentifier);
	}

	public static String getDiagnosisFromAdmissionToDischarge(String primaryAndSecondaryDiagnosis, Date startDate, Date endDate, Integer patientid) {
		return getService().getDiagnosisFromAdmissionToDischarge(primaryAndSecondaryDiagnosis, startDate, endDate, patientid);
	}


	// 🔹 NEW: Paginated fetch
	public static List<GlobalBill> getGlobalBillsByInsurancePolicy(InsurancePolicy ip, int page, int size) {
		if (ip == null || page < 1 || size < 1) {
			return Collections.emptyList();
		}

		// Fetch all admissions for the insurance policy
		List<Admission> admissions = AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);

		// Pagination math
		int fromIndex = (page - 1) * size;
		if (fromIndex >= admissions.size()) {
			return Collections.emptyList(); // no more results
		}
		int toIndex = Math.min(fromIndex + size, admissions.size());

		List<Admission> subList = admissions.subList(fromIndex, toIndex);

		// Collect GlobalBills for only this slice
		List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
		for (Admission admission : subList) {
			GlobalBill globalBill = GlobalBillUtil.getGlobalBillByAdmission(admission);
			if (globalBill != null) {
				globalBills.add(globalBill);
			}
		}
		return globalBills;
	}

	// 🔹 NEW: Count total number of global bills for pagination
	public static int countGlobalBillsByInsurancePolicy(InsurancePolicy ip) {
		if (ip == null) {
			return 0;
		}
		List<Admission> admissions = AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);
		int count = 0;
		for (Admission admission : admissions) {
			GlobalBill gb = GlobalBillUtil.getGlobalBillByAdmission(admission);
			if (gb != null) {
				count++;
			}
		}
		return count;
	}
}
