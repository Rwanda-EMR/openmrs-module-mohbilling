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
import org.openmrs.module.mohbilling.tasks.InsuranceReportEtlTask;
import org.openmrs.module.mohbilling.tasks.IremboReconciliationTask;
import org.openmrs.module.mohbilling.irembo.util.IremboPayLogUtil;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

import java.util.Calendar;
import java.util.Date;

/**
 * This class contains the logic that is run every time this module is either
 * started or shutdown
 */
public class MohBillingActivator extends BaseModuleActivator {

	private static final String IREMBO_TASK_NAME = "mohbilling.irembopay.reconciliation.task";
	private static final String INSURANCE_REPORT_ETL_TASK_NAME = "mohbilling.insurance.report.etl.task";
	private static final long DEFAULT_INSURANCE_REPORT_ETL_INTERVAL_SECONDS = 86400L;
	private static final String DEFAULT_INSURANCE_REPORT_ETL_START_TIME = "01:00";

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see BaseModuleActivator#started()
	 */
	public void started() {

		scheduleIremboReconciliationTask();
		scheduleInsuranceReportEtlTask();
		log.info("MoH-Billing Module started");
	}

	/**
	 * @see BaseModuleActivator#stopped()
	 */
	public void stopped() {
		shutdownIremboReconciliationTask();
		shutdownInsuranceReportEtlTask();
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

			task = saveAndReloadTaskDefinition(schedulerService, task);
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
			IremboPayLogUtil.logFailure(log, "RECONCILIATION",
					"failed to schedule Irembo reconciliation task", e);
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
			IremboPayLogUtil.logFailure(log, "RECONCILIATION",
					"failed to stop Irembo reconciliation task cleanly", e);
		}
	}

	private void scheduleInsuranceReportEtlTask() {
		try {
			boolean enabled = Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_SCHEDULER_ENABLED, "true"));
			long intervalSeconds = parsePositiveLong(Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_SCHEDULER_INTERVAL_SECONDS,
					String.valueOf(DEFAULT_INSURANCE_REPORT_ETL_INTERVAL_SECONDS)),
					DEFAULT_INSURANCE_REPORT_ETL_INTERVAL_SECONDS);
			String configuredStartTime = Context.getAdministrationService().getGlobalProperty(
					BillingConstants.GLOBAL_PROPERTY_INSURANCE_REPORT_ETL_SCHEDULER_START_TIME,
					DEFAULT_INSURANCE_REPORT_ETL_START_TIME);

			SchedulerService schedulerService = Context.getSchedulerService();
			TaskDefinition task = schedulerService.getTaskByName(INSURANCE_REPORT_ETL_TASK_NAME);
			if (task == null) {
				task = new TaskDefinition();
				task.setName(INSURANCE_REPORT_ETL_TASK_NAME);
			}
			task.setDescription("Materialize the MoH Billing insurance report from local billing tables.");
			task.setTaskClass(InsuranceReportEtlTask.class.getName());
			task.setStartTime(nextStartTime(configuredStartTime, new Date()));
			task.setRepeatInterval(intervalSeconds);
			task.setStartOnStartup(false);
			task.setStarted(enabled);
			task = saveAndReloadTaskDefinition(schedulerService, task);

			if (enabled) {
				schedulerService.scheduleIfNotRunning(task);
				log.info("Scheduled insurance report ETL task every " + intervalSeconds
						+ " seconds, starting at " + configuredStartTime + ".");
			} else {
				try {
					schedulerService.shutdownTask(task);
				} catch (SchedulerException ignored) {
					// Task may be registered without having been started.
				}
				log.info("Insurance report ETL task is registered but disabled.");
			}
		} catch (Exception e) {
			log.error("Failed to register insurance report ETL task", e);
		}
	}

	private void shutdownInsuranceReportEtlTask() {
		try {
			TaskDefinition task = Context.getSchedulerService().getTaskByName(INSURANCE_REPORT_ETL_TASK_NAME);
			if (task != null) {
				Context.getSchedulerService().shutdownTask(task);
				log.info("Stopped insurance report ETL task.");
			}
		} catch (SchedulerException e) {
			log.error("Failed to stop insurance report ETL task cleanly", e);
		}
	}

	static Date nextStartTime(String configuredTime, Date now) {
		int hour = 1;
		int minute = 0;
		if (configuredTime != null && configuredTime.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) {
			String[] parts = configuredTime.split(":");
			hour = Integer.parseInt(parts[0]);
			minute = Integer.parseInt(parts[1]);
		}
		Calendar next = Calendar.getInstance();
		next.setTime(now);
		next.set(Calendar.HOUR_OF_DAY, hour);
		next.set(Calendar.MINUTE, minute);
		next.set(Calendar.SECOND, 0);
		next.set(Calendar.MILLISECOND, 0);
		if (!next.getTime().after(now)) {
			next.add(Calendar.DAY_OF_MONTH, 1);
		}
		return next.getTime();
	}

	static TaskDefinition saveAndReloadTaskDefinition(SchedulerService schedulerService, TaskDefinition task)
			throws SchedulerException {
		String taskName = task.getName();
		schedulerService.saveTaskDefinition(task);
		TaskDefinition persistedTask = schedulerService.getTaskByName(taskName);
		if (persistedTask == null || persistedTask.getId() == null) {
			throw new SchedulerException("Scheduler task was not persisted with an id: " + taskName);
		}
		return persistedTask;
	}

	private long parsePositiveLong(String configured, long defaultValue) {
		try {
			long value = Long.parseLong(configured);
			return value > 0 ? value : defaultValue;
		} catch (NumberFormatException e) {
			log.warn("Invalid scheduler interval: " + configured + ". Using " + defaultValue + ".");
			return defaultValue;
		}
	}

}
