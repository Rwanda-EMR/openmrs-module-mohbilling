package org.openmrs.module.mohbilling.irembo.util;

/**
 * Outcome of ensuring a child Irembo invoice exists for a PatientBill.
 */
public class IremboInvoiceResult {

	private final String invoiceNumber;
	private final boolean newlyCreated;
	private final Integer patientBillId;
	private final boolean success;
	private final String message;
	private final String iremboMessage;

	private IremboInvoiceResult(String invoiceNumber, boolean newlyCreated, Integer patientBillId,
			boolean success, String message, String iremboMessage) {
		this.invoiceNumber = invoiceNumber;
		this.newlyCreated = newlyCreated;
		this.patientBillId = patientBillId;
		this.success = success;
		this.message = message;
		this.iremboMessage = iremboMessage;
	}

	public static IremboInvoiceResult success(String invoiceNumber, boolean newlyCreated, Integer patientBillId) {
		return new IremboInvoiceResult(invoiceNumber, newlyCreated, patientBillId, true, null, null);
	}

	public static IremboInvoiceResult failure(Integer patientBillId, String message, String iremboMessage) {
		return new IremboInvoiceResult(null, false, patientBillId, false, message, iremboMessage);
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public boolean isNewlyCreated() {
		return newlyCreated;
	}

	public Integer getPatientBillId() {
		return patientBillId;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getIremboMessage() {
		return iremboMessage;
	}

	public String getUserFacingMessage() {
		StringBuilder sb = new StringBuilder();
		if (message != null) {
			sb.append(message);
		}
		if (iremboMessage != null && !iremboMessage.trim().isEmpty()) {
			if (sb.length() > 0) {
				sb.append(" — ");
			}
			sb.append(iremboMessage);
		}
		return sb.toString();
	}
}
