/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohbilling.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * This class defines the links that will appear on the administration page
 * under the "basicmodule.title" heading. This extension is enabled by defining
 * (uncommenting) it in the /metadata/config.xml file.
 */
public class AdminList extends AdministrationSectionExt {

	
	@Override
	public String getRequiredPrivilege() {
	    // TODO Auto-generated method stub
	    return "View Billing";
	}
	/**
	 * @see org.openmrs.module.web.extension.AdministrationSectionExt#getMediaType()
	 */
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}

	/**
	 * @see org.openmrs.module.web.extension.AdministrationSectionExt#getTitle()
	 */
	public String getTitle() {
		return "mohbilling.title";
	}

	/**
	 * @see org.openmrs.module.web.extension.AdministrationSectionExt#getLinks()
	 */
	public Map<String, String> getLinks() {

		Map<String, String> map = new HashMap<String, String>();
		if (Context.getAuthenticatedUser().hasPrivilege("Add Insurance"))
		map.put("module/mohbilling/insurance.list", "mohbilling.insurance.manage");
		if (Context.getAuthenticatedUser().hasPrivilege("Add Facility service"))
		map.put("module/mohbilling/facilityService.list", "mohbilling.facility.service.manage");
		if (Context.getAuthenticatedUser().hasPrivilege("Create Insurance Policy"))
		map.put("module/mohbilling/insurancePolicySearch.form", "mohbilling.insurance.policy.manage");
		if (Context.getAuthenticatedUser().hasPrivilege("Manage Patient Bill Calculations"))
		map.put("module/mohbilling/patientSearchBill.form", "mohbilling.billing.manage");
		if (Context.getAuthenticatedUser().hasPrivilege("Manage Reports"))
		map.put("module/mohbilling/cohort.form", "mohbilling.billing.report");
        
		return map;
	}

}