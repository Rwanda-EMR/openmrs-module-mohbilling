/**
 * 
 */
package org.openmrs.module.mohappointment.extension.html;

import org.openmrs.module.web.extension.PatientDashboardTabExt;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentDashboardTab extends PatientDashboardTabExt {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.web.extension.PatientDashboardTabExt#getPortletUrl()
	 */
	@Override
	public String getPortletUrl() {
		return "appointment";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.web.extension.PatientDashboardTabExt#getRequiredPrivilege
	 * ()
	 */
	@Override
	public String getRequiredPrivilege() {
		return "Patient Dashboard - View Appointments Section";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getTabId()
	 */
	@Override
	public String getTabId() {
		return "appointment";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getTabName()
	 */
	@Override
	public String getTabName() {
		return "mohappointment.appointment";
	}

}
