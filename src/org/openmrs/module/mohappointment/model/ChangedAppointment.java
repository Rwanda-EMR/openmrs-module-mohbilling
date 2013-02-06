/**
 * 
 */
package org.openmrs.module.mohappointment.model;

import java.util.Date;

/**
 * @author Kamonyo
 * 
 */
public class ChangedAppointment {

	private Integer changedAppointId;
	private Appointment appointment;
	private String reason;
	private Date newDateOfAppointment;

	/**
	 * @return the changedAppointId
	 */

	public Integer getChangedAppointId() {
		return changedAppointId;
	}

	/**
	 * @param changedAppointId
	 *            the changedAppointId to set
	 */

	public void setChangedAppointId(Integer changedAppointId) {
		this.changedAppointId = changedAppointId;
	}

	/**
	 * @return the appointment
	 */

	public Appointment getAppointment() {
		return appointment;
	}

	/**
	 * @param appointment
	 *            the appointment to set
	 */

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	/**
	 * @return the reason
	 */

	public String getReason() {
		return reason;
	}

	/**
	 * @param reason
	 *            the reason to set
	 */

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the newDateOfAppointment
	 */

	public Date getNewDateOfAppointment() {
		return newDateOfAppointment;
	}

	/**
	 * @param newDateOfAppointment
	 *            the newDateOfAppointment to set
	 */

	public void setNewDateOfAppointment(Date newDateOfAppointment) {
		this.newDateOfAppointment = newDateOfAppointment;
	}
}
