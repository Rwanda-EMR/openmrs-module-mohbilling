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
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.tasks.IremboReconciliationTask;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

import java.util.Date;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherService;
import org.openmrs.module.mohbilling.metadata.RhipPractitionerConceptMetadata;

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
public class MohBillingActivator extends BaseModuleActivator implements DaemonTokenAware {

	private static final String IREMBO_TASK_NAME = "mohbilling.irembopay.reconciliation.task";

	protected Log log = LogFactory.getLog(this.getClass());
	private DaemonToken daemonToken;

	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}

	/**
	 * @see BaseModuleActivator#started()
	 */
	public void started() {

		scheduleIremboReconciliationTask();
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
		shutdownIremboReconciliationTask();
		log.info("MoH-Billing Module stopped");
	}

	private void scheduleIremboReconciliationTask() {
		try {
			boolean enabled = Boolean.parseBoolean(Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_IREMBO_SCHEDULER_ENABLED, "false"));

			String intervalGp = Context.getAdministrationService()
					.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_IREMBO_SCHEDULER_INTERVAL_SECONDS, "120");
			long intervalSeconds = 120L;
			try {
				intervalSeconds = Long.parseLong(intervalGp);
			} catch (NumberFormatException nfe) {
				log.warn("Invalid interval value for " + BillingConstants.GLOBAL_PROPERTY_IREMBO_SCHEDULER_INTERVAL_SECONDS
						+ ": " + intervalGp + ". Using default 120 seconds.");
			}
			if (intervalSeconds <= 0) {
				intervalSeconds = 120L;
			}

			SchedulerService schedulerService = Context.getSchedulerService();
			TaskDefinition task = schedulerService.getTaskByName(IREMBO_TASK_NAME);
			if (task == null) {
				task = new TaskDefinition();
				task.setName(IREMBO_TASK_NAME);
			}
			task.setDescription("Periodic Irembo reconciliation task for mohbilling.");
			task.setTaskClass(IremboReconciliationTask.class.getName());
			task.setStartTime(new Date(System.currentTimeMillis() + 10_000L));
			task.setRepeatInterval(intervalSeconds);
			task.setStartOnStartup(enabled);
			task.setStarted(enabled);

			schedulerService.saveTaskDefinition(task);
			if (enabled) {
				schedulerService.scheduleTask(task);
				log.info("Scheduled Irembo reconciliation task to run every " + intervalSeconds + " seconds.");
			} else {
				try {
					schedulerService.shutdownTask(task);
				} catch (SchedulerException ignored) {
					// no-op; task may not be running yet
				}
				log.info("Irembo reconciliation task is registered and visible in scheduler, but not auto-started.");
			}
		} catch (SchedulerException e) {
			log.error("Failed to schedule Irembo reconciliation task", e);
		}
	}

	private void shutdownIremboReconciliationTask() {
		try {
			SchedulerService schedulerService = Context.getSchedulerService();
			TaskDefinition task = schedulerService.getTaskByName(IREMBO_TASK_NAME);
			if (task != null) {
				schedulerService.shutdownTask(task);
				log.info("Stopped Irembo reconciliation task.");
			}
		} catch (SchedulerException e) {
			log.warn("Failed to stop Irembo reconciliation task cleanly", e);
		}
	}

}
