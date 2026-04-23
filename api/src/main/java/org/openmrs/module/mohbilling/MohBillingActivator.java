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

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
public class MohBillingActivator extends BaseModuleActivator {

	private static final String IREMBO_TASK_NAME = "mohbilling.irembopay.reconciliation.task";

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see BaseModuleActivator#started()
	 */
	public void started() {

		scheduleIremboReconciliationTask();
		log.info("MoH-Billing Module started");

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
