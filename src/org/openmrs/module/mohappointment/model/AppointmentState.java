/**
 * 
 */
package org.openmrs.module.mohappointment.model;

/**
 * @author Kamonyo
 * 
 */
public class AppointmentState {

	private Integer appointmentStateId;
	private String description;

	public AppointmentState() {

	}

	public AppointmentState(Integer appointmentStateId, String description) {

		this.appointmentStateId = appointmentStateId;
		this.description = description;
	}

	/**
	 * @return the appointmentStateId
	 */
	public Integer getAppointmentStateId() {
		return appointmentStateId;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param appointmentStateId
	 *            the appointmentStateId to set
	 */
	public void setAppointmentStateId(Integer appointmentStateId) {
		this.appointmentStateId = appointmentStateId;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* -------------- JAVA Overriden methods -------------- */

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		AppointmentState appointmentState = (AppointmentState) obj;
		if (appointmentState != null)
			if (appointmentState.getAppointmentStateId() == this.appointmentStateId
					&& appointmentState.hashCode() == this.hashCode()) {
				return true;
			}

		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.appointmentStateId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "\n - Appointment State Id : " + this.appointmentStateId
				+ "\n - State Description : " + this.description;
	}

}
