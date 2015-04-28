package org.openmrs.module.mohbilling.model;

import java.util.Date;

public class Consommation {
	Date recordDate;
	String libelle;
	Double unitCost;
	Integer quantity;
	Double cost;
	Double patientCost;
	Double insuranceCost;
	/**
	 * @return the libelle
	 */
	public String getLibelle() {
		return libelle;
	}
	/**
	 * @param libelle the libelle to set
	 */
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	/**
	 * @return the unitCost
	 */
	public Double getUnitCost() {
		return unitCost;
	}
	/**
	 * @param unitCost the unitCost to set
	 */
	public void setUnitCost(Double unitCost) {
		this.unitCost = unitCost;
	}
	/**
	 * @return the quantity
	 */
	public Integer getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	/**
	 * @return the cost
	 */
	public Double getCost() {
		return cost;
	}
	/**
	 * @param cost the cost to set
	 */
	public void setCost(Double cost) {
		this.cost = cost;
	}
	/**
	 * @return the patientCost
	 */
	public Double getPatientCost() {
		return patientCost;
	}
	/**
	 * @param patientCost the patientCost to set
	 */
	public void setPatientCost(Double patientCost) {
		this.patientCost = patientCost;
	}
	/**
	 * @return the insuranceCost
	 */
	public Double getInsuranceCost() {
		return insuranceCost;
	}
	/**
	 * @param insuranceCost the insuranceCost to set
	 */
	public void setInsuranceCost(Double insuranceCost) {
		this.insuranceCost = insuranceCost;
	}
	
	public Date getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

}
