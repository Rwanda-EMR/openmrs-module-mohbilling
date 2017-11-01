/**
 * 
 */
package org.openmrs.module.mohbilling.model;

/**
 * @author Kamonyo
 * 
 */
public enum InsuranceCategory {

	MUTUELLE,RSSB,MMI_UR,PRIVATE,NONE,BASE;

	/**
	 * (non-Javadoc)
	 * 
	 * @see Enum#toString()
	 */
	@Override
	public String toString() {
		// only capitalize the first letter
		String s = super.toString();
		return s.substring(0, 1) + s.substring(1).toLowerCase();
	}

}
