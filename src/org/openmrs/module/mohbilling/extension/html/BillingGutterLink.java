/**
 * 
 */
package org.openmrs.module.mohbilling.extension.html;

import org.openmrs.module.web.extension.LinkExt;

/**
 * @author Yves GAKUBA
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
		return "Check Patient Bill Payment";
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.web.extension.LinkExt#getUrl()
	 */
	@Override
	public String getUrl() {
		return "module/mohbilling/checkPatientBillPayment.form";
	}

}
