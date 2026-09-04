package org.openmrs.module.mohbilling;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.Date;

public class MohBillingActivatorTest {

	@Test
	public void nextStartTime_shouldUseConfiguredTimeTodayWhenStillUpcoming() {
		Date now = dateAt(2026, Calendar.SEPTEMBER, 1, 0, 30);
		Calendar next = calendar(MohBillingActivator.nextStartTime("01:15", now));

		Assert.assertEquals(1, next.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(1, next.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(15, next.get(Calendar.MINUTE));
	}

	@Test
	public void nextStartTime_shouldRollToTomorrowWhenConfiguredTimePassed() {
		Date now = dateAt(2026, Calendar.SEPTEMBER, 1, 13, 0);
		Calendar next = calendar(MohBillingActivator.nextStartTime("01:00", now));

		Assert.assertEquals(2, next.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(1, next.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(0, next.get(Calendar.MINUTE));
	}

	@Test
	public void nextStartTime_shouldUseDefaultForInvalidConfiguration() {
		Date now = dateAt(2026, Calendar.SEPTEMBER, 1, 0, 30);
		Calendar next = calendar(MohBillingActivator.nextStartTime("25:99", now));

		Assert.assertEquals(1, next.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(0, next.get(Calendar.MINUTE));
	}

	@Test
	public void validTimeOrDefault_shouldKeepCashierAtThreeAmWhenConfigurationIsInvalid() {
		Assert.assertEquals("03:00", MohBillingActivator.validTimeOrDefault("not-a-time", "03:00"));
		Assert.assertEquals("04:30", MohBillingActivator.validTimeOrDefault("04:30", "03:00"));
	}

	@Test
	public void saveAndReloadTaskDefinition_shouldScheduleUsingPersistedTask() throws Exception {
		TaskDefinition submittedTask = new TaskDefinition();
		submittedTask.setName("test.task");
		TaskDefinition persistedTask = new TaskDefinition();
		persistedTask.setId(41);
		persistedTask.setName("test.task");
		boolean[] saved = { false };
		SchedulerService schedulerService = schedulerService((proxy, method, args) -> {
			if ("saveTaskDefinition".equals(method.getName())) {
				saved[0] = args[0] == submittedTask;
				return null;
			}
			if ("getTaskByName".equals(method.getName())) {
				return persistedTask;
			}
			throw new UnsupportedOperationException(method.getName());
		});

		TaskDefinition result = MohBillingActivator.saveAndReloadTaskDefinition(schedulerService, submittedTask);

		Assert.assertTrue(saved[0]);
		Assert.assertSame(persistedTask, result);
	}

	@Test(expected = SchedulerException.class)
	public void saveAndReloadTaskDefinition_shouldRejectTaskWithoutDatabaseId() throws Exception {
		TaskDefinition submittedTask = new TaskDefinition();
		submittedTask.setName("test.task");
		SchedulerService schedulerService = schedulerService((proxy, method, args) -> {
			if ("saveTaskDefinition".equals(method.getName())) {
				return null;
			}
			if ("getTaskByName".equals(method.getName())) {
				return submittedTask;
			}
			throw new UnsupportedOperationException(method.getName());
		});

		MohBillingActivator.saveAndReloadTaskDefinition(schedulerService, submittedTask);
	}

	private static Date dateAt(int year, int month, int day, int hour, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month, day, hour, minute);
		return calendar.getTime();
	}

	private static Calendar calendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	private static SchedulerService schedulerService(java.lang.reflect.InvocationHandler handler) {
		return (SchedulerService) Proxy.newProxyInstance(SchedulerService.class.getClassLoader(),
				new Class<?>[] { SchedulerService.class }, handler);
	}
}
