package org.openmrs.module.mohbilling.irembo.util;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

/**
 * Maps low-level network exceptions to actionable Irembo Pay messages.
 */
public final class IremboPayNetworkUtil {

	private IremboPayNetworkUtil() {
	}

	public static boolean isNetworkFailure(Throwable throwable) {
		return resolveNetworkFailure(throwable) != null;
	}

	public static boolean isNetworkFailureMessage(String message) {
		if (message == null || message.trim().isEmpty()) {
			return false;
		}
		String lower = message.toLowerCase();
		return lower.contains("cannot resolve irembo pay host")
				|| lower.contains("cannot connect to irembo pay")
				|| lower.contains("timed out connecting to irembo pay")
				|| lower.contains("timed out waiting for irembo pay")
				|| lower.contains("network_error");
	}

	public static String describeFailure(Throwable throwable, String target) {
		Throwable root = resolveNetworkFailure(throwable);
		if (root == null) {
			root = throwable;
		}
		String endpoint = target == null || target.trim().isEmpty() ? "Irembo Pay API" : target.trim();
		if (root instanceof ConnectTimeoutException || isConnectTimeout(root)) {
			return "Timed out connecting to Irembo Pay (" + endpoint + "). Check server network/firewall.";
		}
		if (root instanceof SocketTimeoutException) {
			return "Timed out waiting for Irembo Pay response (" + endpoint + ").";
		}
		if (root instanceof UnknownHostException) {
			return "Cannot resolve Irembo Pay host (" + endpoint
					+ "). The OpenMRS server DNS or outbound internet access is unavailable.";
		}
		if (root instanceof HttpHostConnectException || root instanceof ConnectException
				|| root instanceof NoRouteToHostException) {
			return "Cannot connect to Irembo Pay (" + endpoint + "). Connection refused or blocked by firewall.";
		}
		if (root instanceof SSLException) {
			return "SSL/TLS error when connecting to Irembo Pay (" + endpoint + ").";
		}
		String message = root.getMessage();
		if (message == null || message.trim().isEmpty()) {
			return "Network error calling Irembo Pay (" + endpoint + "): " + root.getClass().getSimpleName();
		}
		return "Network error calling Irembo Pay (" + endpoint + "): " + message.trim();
	}

	public static String hostFromBaseUrl(String baseUrl) {
		if (baseUrl == null || baseUrl.trim().isEmpty()) {
			return "Irembo Pay API";
		}
		try {
			java.net.URI uri = java.net.URI.create(baseUrl.trim());
			if (uri.getHost() != null) {
				return uri.getHost();
			}
		} catch (IllegalArgumentException ignored) {
			// fall through
		}
		return baseUrl.trim();
	}

	private static Throwable resolveNetworkFailure(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof UnknownHostException
					|| current instanceof ConnectTimeoutException
					|| current instanceof SocketTimeoutException
					|| current instanceof HttpHostConnectException
					|| current instanceof ConnectException
					|| current instanceof NoRouteToHostException
					|| current instanceof SSLException
					|| isConnectTimeout(current)) {
				return current;
			}
			Throwable cause = current.getCause();
			if (cause == current) {
				break;
			}
			current = cause;
		}
		return null;
	}

	private static boolean isConnectTimeout(Throwable throwable) {
		return throwable instanceof ConnectTimeoutException
				|| (throwable != null && "ConnectTimeoutException".equals(throwable.getClass().getSimpleName()));
	}
}
