/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.model;

/**
 * @author Kamonyo
 */
public enum RecoveryStatus {

	SUBMITTED("SUBMITTED"), VERIFIED("VERIFIED"), FULLYPAID("FULLYPAID"), 
	PARTLYPAID("PARTLYPAID"), REFUSED("REFUSED"), UNPAID("UNPAID");

	private final String description;

	private RecoveryStatus(String name) {
		this.description = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}
}
