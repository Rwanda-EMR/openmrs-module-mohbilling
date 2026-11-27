package org.openmrs.module.mohbilling.integration.insurance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * How MMI would settle a bill, from /voucher-closure with preview true: nothing is billed. Shown before the bill is
 * closed, which cannot be undone.
 */
public class MmiClosurePreview {

	private boolean success;

	private String message;

	private String voucherCode;

	private String billStatus;

	private BigDecimal totalAmount;

	private BigDecimal insuranceAmount;

	private BigDecimal patientAmount;

	private BigDecimal otherAmount;

	private BigDecimal insurancePercentage;

	private BigDecimal patientPercentage;

	private BigDecimal otherPercentage;

	private final List<Line> lines = new ArrayList<Line>();

	private int rejectedLineCount;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getVoucherCode() {
		return voucherCode;
	}

	public void setVoucherCode(String voucherCode) {
		this.voucherCode = voucherCode;
	}

	public String getBillStatus() {
		return billStatus;
	}

	public void setBillStatus(String billStatus) {
		this.billStatus = billStatus;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getInsuranceAmount() {
		return insuranceAmount;
	}

	public void setInsuranceAmount(BigDecimal insuranceAmount) {
		this.insuranceAmount = insuranceAmount;
	}

	public BigDecimal getPatientAmount() {
		return patientAmount;
	}

	public void setPatientAmount(BigDecimal patientAmount) {
		this.patientAmount = patientAmount;
	}

	public BigDecimal getOtherAmount() {
		return otherAmount;
	}

	public void setOtherAmount(BigDecimal otherAmount) {
		this.otherAmount = otherAmount;
	}

	public BigDecimal getInsurancePercentage() {
		return insurancePercentage;
	}

	public void setInsurancePercentage(BigDecimal insurancePercentage) {
		this.insurancePercentage = insurancePercentage;
	}

	public BigDecimal getPatientPercentage() {
		return patientPercentage;
	}

	public void setPatientPercentage(BigDecimal patientPercentage) {
		this.patientPercentage = patientPercentage;
	}

	public BigDecimal getOtherPercentage() {
		return otherPercentage;
	}

	public void setOtherPercentage(BigDecimal otherPercentage) {
		this.otherPercentage = otherPercentage;
	}

	/**
	 * @return the lines that would be settled, including approved items MMI adds when the bill closes
	 */
	public List<Line> getLines() {
		return lines;
	}

	/**
	 * @return how many lines of the global bill MMI did not record on the voucher; they will not be on the MMI bill
	 */
	public int getRejectedLineCount() {
		return rejectedLineCount;
	}

	public void setRejectedLineCount(int rejectedLineCount) {
		this.rejectedLineCount = rejectedLineCount;
	}

	public static class Line {

		private final String code;

		private final String name;

		private final BigDecimal lineTotal;

		private final String priceSource;

		private final boolean fromApproval;

		public Line(String code, String name, BigDecimal lineTotal, String priceSource, boolean fromApproval) {
			this.code = code;
			this.name = name;
			this.lineTotal = lineTotal;
			this.priceSource = priceSource;
			this.fromApproval = fromApproval;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public BigDecimal getLineTotal() {
			return lineTotal;
		}

		public String getPriceSource() {
			return priceSource;
		}

		/**
		 * @return true for an approved item that is not on the bill yet and will be added when it closes
		 */
		public boolean isFromApproval() {
			return fromApproval;
		}
	}
}
