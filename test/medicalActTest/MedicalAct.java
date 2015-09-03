package medicalActTest;

import java.math.BigDecimal;
import java.util.Date;

import org.openmrs.Location;
import org.openmrs.User;

public class MedicalAct {
	
	private String name;	
	private String category;
	private String fullPrice;
	private String startDate;	
	private String createdDate;
	private String retired ;
	private String location;	
	private String creator;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	/**
	 * @return the fullPrice
	 */
	public String getFullPrice() {
		return fullPrice;
	}
	/**
	 * @param fullPrice the fullPrice to set
	 */
	public void setFullPrice(String fullPrice) {
		this.fullPrice = fullPrice;
	}
	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the createdDate
	 */
	public String getCreatedDate() {
		return createdDate;
	}
	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	/**
	 * @return the retired
	 */
	public String getRetired() {
		return retired;
	}
	/**
	 * @param retired the retired to set
	 */
	public void setRetired(String retired) {
		this.retired = retired;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	

}
