package org.openmrs.module.mohbilling.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The national RHIC (procedures) or RLTC (laboratory) code of a billing item. Older catalogs keep it in the item's
 * name, after a "|" ("Cataract surgery|RHIC-OPTH-072") or a "-" ("Cataract surgery - RHIC-OPTH-072"); it now has its
 * own column, moh_bill_facility_service_price.rhic_code.
 */
public final class RhicCode {

	private static final Pattern CODE = Pattern.compile("(RHIC|RLTC)(-[A-Z0-9]+)+");

	/**
	 * The code at the end of a name, after "|", "-" or a space
	 */
	private static final Pattern CODE_AT_END = Pattern.compile("(?:^|[|\\s-])((?:RHIC|RLTC)(?:-[A-Z0-9]+)+)\\s*$");

	private RhicCode() {
	}

	/**
	 * @return the RHIC/RLTC code at the end of a billing item's name, upper case, or null when it has none
	 */
	public static String fromName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return null;
		}
		Matcher matcher = CODE_AT_END.matcher(name.trim().toUpperCase());
		return matcher.find() ? matcher.group(1) : null;
	}

	/**
	 * @return the code trimmed and upper case, or null when it is blank or not an RHIC/RLTC code
	 */
	public static String normalize(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}
		String normalized = code.trim().toUpperCase();
		return CODE.matcher(normalized).matches() ? normalized : null;
	}

	public static boolean isValid(String code) {
		return normalize(code) != null;
	}
}
