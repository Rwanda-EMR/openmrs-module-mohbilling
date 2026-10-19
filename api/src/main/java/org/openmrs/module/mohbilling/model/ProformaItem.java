package org.openmrs.module.mohbilling.model;

import java.math.BigDecimal;

/**
 * A priced line on a {@link Proforma}. Name and unit price are snapshots taken when the
 * proforma is issued so reprints stay identical even after tariffs change.
 */
public class ProformaItem {

	private Integer proformaItemId;
	private Proforma proforma;
	private BillableService billableService;
	private String serviceName;
	private String categoryName;
	private BigDecimal unitPrice;
	private BigDecimal quantity;
	private BigDecimal lineTotal;
	private String uuid;

	public Integer getProformaItemId() {
		return proformaItemId;
	}

	public void setProformaItemId(Integer proformaItemId) {
		this.proformaItemId = proformaItemId;
	}

	public Proforma getProforma() {
		return proforma;
	}

	public void setProforma(Proforma proforma) {
		this.proforma = proforma;
	}

	public BillableService getBillableService() {
		return billableService;
	}

	public void setBillableService(BillableService billableService) {
		this.billableService = billableService;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

	public void setLineTotal(BigDecimal lineTotal) {
		this.lineTotal = lineTotal;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
