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
package org.openmrs.module.mohbilling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherService;
import org.openmrs.module.mohbilling.metadata.RhipPractitionerConceptMetadata;

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
public class MohBillingActivator extends BaseModuleActivator implements DaemonTokenAware {

	protected Log log = LogFactory.getLog(this.getClass());
	private DaemonToken daemonToken;

	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}

	/**
	 * @see BaseModuleActivator#started()
	 */
	public void started() {

		log.info("MoH-Billing Module started");
		boolean openedSession = false;
		try {
			if (!Context.isSessionOpen()) {
				Context.openSession();
				openedSession = true;
			}
			Context.addProxyPrivilege("Manage Concepts");
			RhipPractitionerConceptMetadata.ensureInstalled();
		}
		catch (Exception e) {
			log.warn("Unable to install RHIP practitioner concept metadata", e);
		}
		finally {
			try {
				Context.removeProxyPrivilege("Manage Concepts");
			}
			catch (Exception ignored) {
			}
			if (openedSession) {
				try {
					Context.closeSession();
				}
				catch (Exception ignored) {
				}
			}
		}

		// One-time best-effort background sync of RHIP practitioner sub-categories into concept answers + local cache table.
		if (daemonToken == null) {
			log.warn("Unable to sync RHIP practitioner sub-categories on startup: daemonToken is not available");
			return;
		}
		Daemon.runInDaemonThread(() -> {
			boolean daemonSessionOpened = false;
			try {
				if (!Context.isSessionOpen()) {
					Context.openSession();
					daemonSessionOpened = true;
				}
				Context.addProxyPrivilege("Manage Concepts");
				RhipVoucherService voucherService =
						Context.getRegisteredComponent("rhipVoucherService", RhipVoucherService.class);
				if (voucherService == null) {
					log.warn("Unable to sync RHIP practitioner sub-categories: rhipVoucherService bean not found");
					return;
				}
				try {
					if (voucherService.hasPractitionerSubCategoryConceptAnswers()) {
						log.debug("Skipping RHIP practitioner sub-category sync on startup: concept answers already exist");
						return;
					}
				}
				catch (Exception e) {
					log.debug("Unable to check whether RHIP practitioner sub-category answers already exist; proceeding with sync", e);
				}
				// Per RHIP docs, categoryId can be an empty string (no filter).
				log.info("Syncing RHIP practitioner sub-categories on startup (first run)");
				voucherService.syncPractitionerSubCategoryConceptAnswers("cbhi", "");
			}
			catch (Exception e) {
				log.warn("Unable to sync RHIP practitioner sub-categories on startup", e);
			}
			finally {
				try {
					Context.removeProxyPrivilege("Manage Concepts");
				}
				catch (Exception ignored) {
				}
				if (daemonSessionOpened) {
					try {
						Context.closeSession();
					}
					catch (Exception ignored) {
					}
				}
			}
		}, daemonToken);
	}

	/**
	 * @see BaseModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("MoH-Billing Module stopped");
	}

}
