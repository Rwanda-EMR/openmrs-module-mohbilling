package org.openmrs.module.mohbilling.model;

/**
 * Facility / geographic fields written once in the Bill Report Excel header
 * (omitted from repeating data rows).
 */
public class BillReportFacilityHeader {

	private String hfCode;
	private String facilityName;
	private String facilityType;
	private String facilityCategory;
	private String facilityLevelOfCare;
	private String facilityOwnershipType;
	private String provinceName;
	private String districtName;
	private String sectorName;

	public String getHfCode() {
		return hfCode;
	}

	public void setHfCode(String hfCode) {
		this.hfCode = hfCode;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(String facilityType) {
		this.facilityType = facilityType;
	}

	public String getFacilityCategory() {
		return facilityCategory;
	}

	public void setFacilityCategory(String facilityCategory) {
		this.facilityCategory = facilityCategory;
	}

	public String getFacilityLevelOfCare() {
		return facilityLevelOfCare;
	}

	public void setFacilityLevelOfCare(String facilityLevelOfCare) {
		this.facilityLevelOfCare = facilityLevelOfCare;
	}

	public String getFacilityOwnershipType() {
		return facilityOwnershipType;
	}

	public void setFacilityOwnershipType(String facilityOwnershipType) {
		this.facilityOwnershipType = facilityOwnershipType;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}

	public String getSectorName() {
		return sectorName;
	}

	public void setSectorName(String sectorName) {
		this.sectorName = sectorName;
	}
}
