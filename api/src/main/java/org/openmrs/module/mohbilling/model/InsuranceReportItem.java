package org.openmrs.module.mohbilling.model;

import java.util.Date;

/**
 * @author smallGod
 * date: 20/07/2023
 */
public class InsuranceReportItem {

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
    private Integer insuranceId;
    private Integer globalBillId;
    private Float currentInsuranceRate;
    private Double currentInsuranceRateFlatFee;
    private String globalBillIdentifier;

    private Double medicament;
    private Double consultation;
    private Double hospitalisation;
    private Double laboratoire;
    private Double formaliteAdministratives;
    private Double ambulance;
    private Double consommables;
    private Double oxygenotherapie;
    private Double imaging;
    private Double proced;
    private Double total100;
    private Double totalPatient;
    private Double totalInsurance;

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

    public String getGlobalBillIdentifier() {
        return globalBillIdentifier;
    }

    public void setGlobalBillIdentifier(String globalBillIdentifier) {
        this.globalBillIdentifier = globalBillIdentifier;
    }

    public Double getMedicament() {
        return medicament;
    }

    public void setMedicament(Double medicament) {
        this.medicament = medicament;
    }

    public Double getConsultation() {
        return consultation;
    }

    public void setConsultation(Double consultation) {
        this.consultation = consultation;
    }

    public Double getHospitalisation() {
        return hospitalisation;
    }

    public void setHospitalisation(Double hospitalisation) {
        this.hospitalisation = hospitalisation;
    }

    public Double getLaboratoire() {
        return laboratoire;
    }

    public void setLaboratoire(Double laboratoire) {
        this.laboratoire = laboratoire;
    }

    public Double getFormaliteAdministratives() {
        return formaliteAdministratives;
    }

    public void setFormaliteAdministratives(Double formaliteAdministratives) {
        this.formaliteAdministratives = formaliteAdministratives;
    }

    public Double getAmbulance() {
        return ambulance;
    }

    public void setAmbulance(Double ambulance) {
        this.ambulance = ambulance;
    }

    public Double getConsommables() {
        return consommables;
    }

    public void setConsommables(Double consommables) {
        this.consommables = consommables;
    }

    public Double getOxygenotherapie() {
        return oxygenotherapie;
    }

    public void setOxygenotherapie(Double oxygenotherapie) {
        this.oxygenotherapie = oxygenotherapie;
    }

    public Double getImaging() {
        return imaging;
    }

    public void setImaging(Double imaging) {
        this.imaging = imaging;
    }

    public Double getProced() {
        return proced;
    }

    public void setProced(Double proced) {
        this.proced = proced;
    }

    public Double getTotal100() {
        return total100;
    }

    public void setTotal100(Double total100) {
        this.total100 = total100;
    }

    public Double getTotalPatient() {
        return totalPatient;
    }

    public void setTotalPatient(Double totalPatient) {
        this.totalPatient = totalPatient;
    }

    public Double getTotalInsurance() {
        return totalInsurance;
    }

    public void setTotalInsurance(Double totalInsurance) {
        this.totalInsurance = totalInsurance;
    }
}
