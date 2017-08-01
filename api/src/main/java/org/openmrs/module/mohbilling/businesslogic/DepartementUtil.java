/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.List;

/**
 * @author emr
 *
 */
public class DepartementUtil {	
	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}
	
	/**
	 * creates Departement object and save it to the DB
	 * @param departement to be saved
	 * @return the departement that has  been saved,if departement object is null it will return  null
	 */
	public static Department saveDepartement(Department departement){
		if(departement!=null){
			getService().saveDepartement(departement);			
			return departement;
		}
		return null;
	}
	/**
	 * Gets departement that matches the identifier departementId
	 * @param departmentId
	 * @return the departement
	 */
	public static Department getDepartement(Integer departmentId){
		 return getService().getDepartement(departmentId);
	}
	/**
	 * Gets the list of departements in the Hospital
	 * 
	 * @return List<Department> departments
	 */
	public static List<Department> getAllHospitalDepartements(){		
	    return	getService().getAllDepartements();	
		
	}
	
	

}
