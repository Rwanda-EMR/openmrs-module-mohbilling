package org.openmrs.module.mohbilling.model;

import java.util.Date;

/**
 * @author smallGod
 * date: 20/07/2023
 */
public class PatientServiceBillReport {

    private Integer id;
    private Date admissionDate;
    private Date closingDate;
    private String beneficiaryName;
    private String householdHeadName;
    private String familyCode;
    private Integer beneficiaryLevel;
    private String cardNumber;
    private String companyName;
    private Integer age;
    private Date birthDate;
    private String gender;
    private String doctorName;
    private Double serviceBillQuantity;
    private Double serviceBillUnitPrice;
    private Integer insuranceId;
    private Integer hopServiceId;
    private Integer globalBillId;
    private Float currentInsuranceRate;
    private Double currentInsuranceRateFlatFee;
    private String hopServiceName;
    private String globalBillIdentifier;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(Date admissionDate) {
        this.admissionDate = admissionDate;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getHouseholdHeadName() {
        return householdHeadName;
    }

    public void setHouseholdHeadName(String householdHeadName) {
        this.householdHeadName = householdHeadName;
    }

    public String getFamilyCode() {
        return familyCode;
    }

    public void setFamilyCode(String familyCode) {
        this.familyCode = familyCode;
    }

    public Integer getBeneficiaryLevel() {
        return beneficiaryLevel;
    }

    public void setBeneficiaryLevel(Integer beneficiaryLevel) {
        this.beneficiaryLevel = beneficiaryLevel;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Integer getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(Integer insuranceId) {
        this.insuranceId = insuranceId;
    }

    public Integer getHopServiceId() {
        return hopServiceId;
    }

    public void setHopServiceId(Integer hopServiceId) {
        this.hopServiceId = hopServiceId;
    }

    public Double getServiceBillQuantity() {
        return serviceBillQuantity;
    }

    public void setServiceBillQuantity(Double serviceBillQuantity) {
        this.serviceBillQuantity = serviceBillQuantity;
    }

    public Double getServiceBillUnitPrice() {
        return serviceBillUnitPrice;
    }

    public void setServiceBillUnitPrice(Double serviceBillUnitPrice) {
        this.serviceBillUnitPrice = serviceBillUnitPrice;
    }

    public Integer getGlobalBillId() {
        return globalBillId;
    }

    public void setGlobalBillId(Integer globalBillId) {
        this.globalBillId = globalBillId;
    }

    public Float getCurrentInsuranceRate() {
        return currentInsuranceRate;
    }

    public void setCurrentInsuranceRate(Float currentInsuranceRate) {
        this.currentInsuranceRate = currentInsuranceRate;
    }

    public Double getCurrentInsuranceRateFlatFee() {
        return currentInsuranceRateFlatFee;
    }

    public void setCurrentInsuranceRateFlatFee(Double currentInsuranceRateFlatFee) {
        this.currentInsuranceRateFlatFee = currentInsuranceRateFlatFee;
    }

    public String getHopServiceName() {
        return hopServiceName;
    }

    public void setHopServiceName(String hopServiceName) {
        this.hopServiceName = hopServiceName;
    }

    public String getGlobalBillIdentifier() {
        return globalBillIdentifier;
    }

    public void setGlobalBillIdentifier(String globalBillIdentifier) {
        this.globalBillIdentifier = globalBillIdentifier;
    }

    @Override
    public String toString() {
        return "PatientServiceBillReport{" +
                "id=" + id +
                ", admissionDate=" + admissionDate +
                ", closingDate=" + closingDate +
                ", beneficiaryName='" + beneficiaryName + '\'' +
                ", householdHeadName='" + householdHeadName + '\'' +
                ", familyCode='" + familyCode + '\'' +
                ", beneficiaryLevel=" + beneficiaryLevel +
                ", cardNumber='" + cardNumber + '\'' +
                ", companyName='" + companyName + '\'' +
                ", age=" + age +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", serviceBillQuantity=" + serviceBillQuantity +
                ", serviceBillUnitPrice=" + serviceBillUnitPrice +
                ", insuranceId=" + insuranceId +
                ", hopServiceId=" + hopServiceId +
                ", globalBillId=" + globalBillId +
                ", currentInsuranceRate=" + currentInsuranceRate +
                ", currentInsuranceRateFlatFee=" + currentInsuranceRateFlatFee +
                ", hopServiceName='" + hopServiceName + '\'' +
                '}';
    }
}
