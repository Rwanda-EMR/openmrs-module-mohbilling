/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.List;

/**
 * @author emr
 *
 */
public class AdmissionUtil {

	public static final String NO_ACTIVE_VISIT_MESSAGE =
			"No active visit found for this patient. Please open a visit before creating an admission.";

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	
	/**
	 * save the patient admission to DB
	 * @param admission patient admission to be saved
	 * @return the admission saved
	 */
	public static Admission savePatientAdmission(Admission admission){
		
		return getService().saveAdmission(admission);
		
	}
	public static List<Admission> getPatientAdmissionsByInsurancePolicy(InsurancePolicy ip){
		
		return getService().getAdmissionsListByInsurancePolicy(ip);
		
		
	}

	/**
	 * Resolves the OpenMRS visit to hard-link on a new admission.
	 * Prefers the encounter's visit when present; otherwise requires an active visit.
	 *
	 * @throws APIException when no visit can be resolved
	 */
	public static Visit requireVisitForAdmission(Patient patient, Encounter encounter) {
		if (encounter != null && encounter.getVisit() != null) {
			return encounter.getVisit();
		}
		return requireActiveVisit(patient);
	}

	/**
	 * Returns one active visit for the patient, or fails if none exist.
	 *
	 * @throws APIException when the patient has no active visit
	 */
	public static Visit requireActiveVisit(Patient patient) {
		if (patient == null) {
			throw new APIException(NO_ACTIVE_VISIT_MESSAGE);
		}
		List<Visit> activeVisits = Context.getVisitService().getActiveVisitsByPatient(patient);
		if (activeVisits == null || activeVisits.isEmpty()) {
			throw new APIException(NO_ACTIVE_VISIT_MESSAGE);
		}
		// Prefer the most recently started active visit when several are open
		Visit chosen = activeVisits.get(0);
		for (Visit candidate : activeVisits) {
			if (candidate == null || candidate.getStartDatetime() == null) {
				continue;
			}
			if (chosen.getStartDatetime() == null
					|| candidate.getStartDatetime().after(chosen.getStartDatetime())) {
				chosen = candidate;
			}
		}
		return chosen;
	}
	
}
