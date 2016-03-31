/**
 * 
 */
package org.openmrs.module.mohbilling.model;

/**
 * @author Kamonyo
 * 
 */
public enum InsuranceCategory {

	MUTUELLE, BASE, PRIVATE, NONE;

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		// only capitalize the first letter
		String s = super.toString();
		return s.substring(0, 1) + s.substring(1).toLowerCase();
	}

}
