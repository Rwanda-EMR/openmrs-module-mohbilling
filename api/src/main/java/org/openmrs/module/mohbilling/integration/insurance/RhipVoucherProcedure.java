package org.openmrs.module.mohbilling.integration.insurance;

import java.math.BigDecimal;

public class RhipVoucherProcedure {
	private String code;
	private BigDecimal quantity;
	private String prescribedAt;
	private BigDecimal price;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public String getPrescribedAt() {
		return prescribedAt;
	}

	public void setPrescribedAt(String prescribedAt) {
		this.prescribedAt = prescribedAt;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
