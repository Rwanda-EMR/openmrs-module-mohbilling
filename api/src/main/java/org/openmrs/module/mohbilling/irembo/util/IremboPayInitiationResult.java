package org.openmrs.module.mohbilling.irembo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Structured outcome of an Irembo Pay initiation flow (single or batch).
 */
public class IremboPayInitiationResult {

	public enum FailedStep {
		CREATE_INVOICE,
		CREATE_BATCH,
		INITIATE_PAYMENT
	}

	private final boolean success;
	private final FailedStep failedStep;
	private final String message;
	private final String iremboMessage;
	private final boolean rolledBack;
	private final String rollbackDetail;
	private final List<String> invoiceNumbers;
	private final String batchNumber;

	private IremboPayInitiationResult(boolean success, FailedStep failedStep, String message, String iremboMessage,
			boolean rolledBack, String rollbackDetail, List<String> invoiceNumbers, String batchNumber) {
		this.success = success;
		this.failedStep = failedStep;
		this.message = message;
		this.iremboMessage = iremboMessage;
		this.rolledBack = rolledBack;
		this.rollbackDetail = rollbackDetail;
		this.invoiceNumbers = invoiceNumbers == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(invoiceNumbers));
		this.batchNumber = batchNumber;
	}

	public static IremboPayInitiationResult success(List<String> invoiceNumbers, String batchNumber) {
		return new IremboPayInitiationResult(true, null,
				"Irembo Pay initiated successfully", null, false, null, invoiceNumbers, batchNumber);
	}

	public static IremboPayInitiationResult failure(FailedStep failedStep, String message, String iremboMessage,
			boolean rolledBack, String rollbackDetail, List<String> invoiceNumbers, String batchNumber) {
		return new IremboPayInitiationResult(false, failedStep, message, iremboMessage, rolledBack, rollbackDetail,
				invoiceNumbers, batchNumber);
	}

	public boolean isSuccess() {
		return success;
	}

	public FailedStep getFailedStep() {
		return failedStep;
	}

	public String getMessage() {
		return message;
	}

	public String getIremboMessage() {
		return iremboMessage;
	}

	public boolean isRolledBack() {
		return rolledBack;
	}

	public String getRollbackDetail() {
		return rollbackDetail;
	}

	public List<String> getInvoiceNumbers() {
		return invoiceNumbers;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public String getUserFacingMessage() {
		StringBuilder sb = new StringBuilder();
		if (message != null) {
			sb.append(message);
		}
		if (failedStep != null) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append("(failed step: ").append(failedStep.name()).append(")");
		}
		if (iremboMessage != null && !iremboMessage.trim().isEmpty()) {
			if (sb.length() > 0) {
				sb.append(" — ");
			}
			sb.append(iremboMessage);
		}
		if (rolledBack && rollbackDetail != null && !rollbackDetail.trim().isEmpty()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append("[").append(rollbackDetail).append("]");
		}
		return sb.toString();
	}
}
