/**
 * 
 */
package org.openmrs.module.mohappointment.extension.html;

import org.openmrs.module.web.extension.LinkExt;

/**
 * @author Kamonyo
 * 
 */
public class AppointmentGutterLink extends LinkExt {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.LinkExt#getLabel()
	 */
	@Override
	public String getLabel() {
		return "mohappointment.appointments";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.LinkExt#getRequiredPrivilege()
	 */
	@Override
	public String getRequiredPrivilege() {
		return "View Provider Appointments";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.LinkExt#getUrl()
	 */
	@Override
	public String getUrl() {
		return "module/mohappointment/providerDashboard.form";
	}

}
