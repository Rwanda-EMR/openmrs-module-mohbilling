/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.model;

/**
 *
 */
public enum BillStatus {

	FULLY_PAID("FULLY_PAID", "FULLY PAID"), PARTLY_PAID("PARTLY_PAID",
			"PARTLY PAID"), UNPAID("UNPAID", "UNPAID");

	private final String name;
	private final String description;

	private BillStatus(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
