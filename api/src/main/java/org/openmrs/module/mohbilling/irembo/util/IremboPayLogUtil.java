package org.openmrs.module.mohbilling.irembo.util;

import org.apache.commons.logging.Log;

/**
 * Consistent log lines for Irembo Pay failure points.
 */
public final class IremboPayLogUtil {

	private IremboPayLogUtil() {
	}

	public static void logFailure(Log log, String step, String details) {
		log.warn("Irembo Pay FAILED [step=" + step + "]: " + details);
	}

	public static void logFailure(Log log, String step, String details, Throwable throwable) {
		log.error("Irembo Pay FAILED [step=" + step + "]: " + details, throwable);
	}
}
