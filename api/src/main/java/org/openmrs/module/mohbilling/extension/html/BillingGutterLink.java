/**
 * 
 */
package org.openmrs.module.mohbilling.extension.html;

import org.openmrs.module.web.extension.LinkExt;

/**
 * @author rbcemr
 *
 */
public class BillingGutterLink extends LinkExt {

	/* (non-Javadoc)
	 * @see org.openmrs.module.web.extension.LinkExt#getLabel()
	 */
	@Override
	public String getLabel() {
		return "mohbilling.billing";
		//return null;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.web.extension.LinkExt#getRequiredPrivilege()
	 */
	@Override
	public String getRequiredPrivilege() {
		//return "Check Patient Bill Payment";
		return "Search Insurance Policy";
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.web.extension.LinkExt#getUrl()
	 */
	@Override
	public String getUrl() {
		//return "module/mohbilling/patientSearchBill.form";
		return "module/mohbilling/insurancePolicySearch.form";
	}

}
