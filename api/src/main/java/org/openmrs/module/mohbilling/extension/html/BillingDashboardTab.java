/**
 * 
 */
package org.openmrs.module.mohbilling.extension.html;

import org.openmrs.module.web.extension.PatientDashboardTabExt;

/**
 * @author rbcemr
 * 
 */
public class BillingDashboardTab extends PatientDashboardTabExt {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.web.extension.PatientDashboardTabExt#getPortletUrl()
	 */
	@Override
	public String getPortletUrl() {
		return "billingPortlet";
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
		return "Patient Dashboard - View Billing Section";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getTabId()
	 */
	@Override
	public String getTabId() {
		return "billing";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.web.extension.PatientDashboardTabExt#getTabName()
	 */
	@Override
	public String getTabName() {
		return "mohbilling.billtab";
	}

}
