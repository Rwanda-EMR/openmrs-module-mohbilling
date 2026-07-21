/**
 *
 */
package org.openmrs.module.mohbilling.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.irembo.Invoice;
import org.openmrs.module.mohbilling.irembo.IremboPay;
import org.openmrs.module.mohbilling.irembo.models.Customer;
import org.openmrs.module.mohbilling.irembo.models.PaymentItem;
import org.openmrs.module.mohbilling.irembo.util.Environment;
import org.openmrs.module.mohbilling.irembo.util.IremboPayResponse;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.util.ConfigUtil;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author rbcemr
 *
 */

@Transactional
public class BillingServiceImpl implements BillingService {

    private static final Log log = LogFactory.getLog(BillingServiceImpl.class);
    private BillingDAO billingDAO;
    private final Object iremboPaymentCreationLock = new Object();

    /**
     * @return the billingDAO
     */
    public BillingDAO getBillingDAO() {
        return billingDAO;
    }

    /**
     * @param billingDAO
     *                   the billingDAO to set
     */
    public void setBillingDAO(BillingDAO billingDAO) {
        this.billingDAO = billingDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> getPatientBillsByPagination(Integer startIndex, Integer pageSize, String orderBy,
            String orderDirection) throws DAOException {
        return billingDAO.getPatientBillsByPagination(startIndex, pageSize, orderBy, orderDirection);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getInsurance(java
     *      .lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public Insurance getInsurance(Integer insuranceId) throws DAOException {

        return billingDAO.getInsurance(insuranceId);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicy
     *      (java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public InsurancePolicy getInsurancePolicy(Integer insurancePolicyId)
            throws DAOException {

        return billingDAO.getInsurancePolicy(insurancePolicyId);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getPatientBill(java
     *      .lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public PatientBill getPatientBill(Integer billId) throws DAOException {

        return billingDAO.getPatientBill(billId);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#saveInsurance(org
     *      .openmrs.module.mohbilling.model.Insurance)
     */
    @Override
    public void saveInsurance(Insurance insurance) {
        if (insurance.getName() == null) {
            throw new APIException("Insurance name is required");
        }
        billingDAO.saveInsurance(insurance);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#saveInsurancePolicy
     *      (org.openmrs.module.mohbilling.model.InsurancePolicy)
     */
    @Override
    public void saveInsurancePolicy(InsurancePolicy card) {
        if (card.getInsuranceCardNo() == null
                && !card.getInsurance().getCategory()
                        .equals(InsuranceCategory.NONE.toString())) {
            throw new APIException("Insurance Card Number is required");
        } else
            billingDAO.saveInsurancePolicy(card);
    }

    /**
     * (non-Javadoc)
     *
     * @return
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#savePatientBill(org.openmrs.module.mohbilling.model.PatientBill)
     */
    @Override
    public PatientBill savePatientBill(PatientBill bill) {

        billingDAO.savePatientBill(bill);
        return bill;
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getFacilityServicePrice(Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public FacilityServicePrice getFacilityServicePrice(Integer id) {
        return billingDAO.getFacilityServicePrice(id);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#saveFacilityServicePrice(org.openmrs.module.mohbilling.model.FacilityServicePrice)
     */
    @Override
    public void saveFacilityServicePrice(FacilityServicePrice fsp) {
        if (fsp.getName() == null) {
            throw new APIException("Facility Service name is required");
        }
        billingDAO.saveFacilityServicePrice(fsp);
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getAllInsurancePolicies()
     */
    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicy> getAllInsurancePolicies() {

        return billingDAO.getAllInsurancePolicies();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getAllInsurances()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Insurance> getAllInsurances() throws DAOException {

        return billingDAO.getAllInsurances();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getAllPatientBills()
     */
    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> getAllPatientBills() throws DAOException {

        return billingDAO.getAllPatientBills();
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#getAllFacilityServicePrices()
     */
    @Override
    @Transactional(readOnly = true)
    public List<FacilityServicePrice> getAllFacilityServicePrices()
            throws DAOException {

        return billingDAO.getAllFacilityServicePrices();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceCategory> getAllServiceCategories() throws DAOException {

        return billingDAO.getAllServiceCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillableService> getAllBillableServices() {
        return billingDAO.getAllBillableServices();
    }

    public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance,
            Date startDate, Date endDate) {
        return billingDAO.getPaidAmountPerInsuranceAndPeriod(insurance,
                startDate, endDate);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicyByCardNo(String)
     */
    @Override
    @Transactional(readOnly = true)
    public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo) {
        return billingDAO.getInsurancePolicyByCardNo(insuranceCardNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> billCohortBuilder(Insurance insurance,
            Date startDate, Date endDate, Integer patientId,
            String serviceName, String billStatus, String billCollector) {
        return billingDAO.billCohortBuilder(insurance, startDate, endDate,
                patientId, serviceName, billStatus, billCollector);
    }

    @Override
    @Transactional(readOnly = true)
    public BillableService getBillableServiceByConcept(
            FacilityServicePrice price, Insurance insurance) {
        return billingDAO.getBillableServiceByConcept(price, insurance);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getThirdParty(Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public ThirdParty getThirdParty(Integer thirdPartyId) throws DAOException {

        return billingDAO.getThirdParty(thirdPartyId);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getAllThirdParties()
     */
    @Override
    @Transactional(readOnly = true)
    public List<ThirdParty> getAllThirdParties() {

        return billingDAO.getAllThirdParties();
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#saveThirdParty(org.openmrs.module.mohbilling.model.ThirdParty)
     */
    @Override
    public void saveThirdParty(ThirdParty thirdParty) throws DAOException {

        billingDAO.saveThirdParty(thirdParty);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getBeneficiaryByPolicyNumber(String)
     */
    @Override
    @Transactional(readOnly = true)
    public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber)
            throws DAOException {

        return billingDAO.getBeneficiaryByPolicyNumber(policyIdNumber);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicyByBeneficiary(org.openmrs.module.mohbilling.model.Beneficiary)
     */
    @Override
    @Transactional(readOnly = true)
    public InsurancePolicy getInsurancePolicyByBeneficiary(
            Beneficiary beneficiary) {

        return billingDAO.getInsurancePolicyByBeneficiary(beneficiary);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getValidBillableService(Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public BillableService getBillableService(Integer id) {

        return billingDAO.getBillableService(id);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getServiceCategory(Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public ServiceCategory getServiceCategory(Integer id) {

        return billingDAO.getServiceCategory(id);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getBillableServiceByCategory(org.openmrs.module.mohbilling.model.ServiceCategory)
     */
    @Override
    @Transactional(readOnly = true)
    public List<BillableService> getBillableServiceByCategory(ServiceCategory sc) {

        return billingDAO.getBillableServiceByCategory(sc);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getFacilityServiceByConcept(Concept)
     */
    @Override
    @Transactional(readOnly = true)
    public FacilityServicePrice getFacilityServiceByConcept(Concept concept) {

        return billingDAO.getFacilityServiceByConcept(concept);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getBillableServicesByFacilityService(org.openmrs.module.mohbilling.model.FacilityServicePrice)
     */
    @Override
    @Transactional(readOnly = true)
    public List<BillableService> getBillableServicesByFacilityService(
            FacilityServicePrice fsp) {

        return billingDAO.getBillableServicesByFacilityService(fsp);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getBillableServicesByInsurance(org.openmrs.module.mohbilling.model.Insurance)
     */
    @Override
    @Transactional(readOnly = true)
    public List<BillableService> getBillableServicesByInsurance(
            Insurance insurance) {

        return billingDAO.getBillableServicesByInsurance(insurance);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getPolicyIdByPatient(Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public List<String[]> getPolicyIdByPatient(Integer patientId) {

        return billingDAO.getPolicyIdByPatient(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillPayment> getAllBillPayments() {
        return billingDAO.getAllBillPayments();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillPayment> getBillPaymentsByDateAndCollector(
            Date createdDate, Date endDate, User collector) {

        return billingDAO.getBillPaymentsByDateAndCollector(createdDate,
                endDate, collector);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillPayment> paymentsCohortBuilder(Insurance insurance,
            Date startDate, Date endDate, Integer patientId,
            String serviceName, String billStatus, String billCollector) {
        return billingDAO.paymentsCohortBuilder(insurance, startDate, endDate,
                patientId, serviceName, billStatus, billCollector);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceCategory getServiceCategoryByName(String name,
            Insurance insurance) {
        return billingDAO.getServiceCategoryByName(name, insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate) {
        return billingDAO.getRevenueDatesBetweenDates(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getBills(Date startDate, Date endDate, User collector) {
        return billingDAO.getBills(startDate, endDate, collector);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getRevenueByService(Date receivedDate,
            String[] serviceCategory, String collector, Insurance insurance) {
        return billingDAO.getRevenueByService(receivedDate, serviceCategory,
                collector, insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> getPatientBillsByCollector(Date receivedDate,
            User collector) {
        // TODO Auto-generated method stub
        return billingDAO.getPatientBillsByCollector(receivedDate, collector);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientBill getBills(Patient patient, Date startDate, Date endDate) {
        return billingDAO.getBills(patient, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceRate getInsuranceRateByInsurance(Insurance insurance) {
        // TODO Auto-generated method stub
        return billingDAO.getInsuranceRateByInsurance(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo) {
        // TODO Auto-generated method stub
        return billingDAO.getBeneficiaryByCardNumber(cardNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(
            Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return billingDAO.getInsurancePoliciesBetweenTwodates(startDate,
                endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary,
            Date startDate, Date endDate) {
        // TODO Auto-generated method stub
        return billingDAO
                .getBillsByBeneficiary(beneficiary, startDate, endDate);
    }

    @Override
    public void loadBillables(Insurance insurance) {
        billingDAO.loadBillables(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getBaseBillableServices(Insurance i) {
        return billingDAO.getBaseBillableServices(i);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPharmacyBaseBillableServices(Insurance i) {
        return billingDAO.getPharmacyBaseBillableServices(i);
    }

    /**
     * @see org.openmrs.module.mohbilling.service.BillingService#getRefundedBills(Date,
     *      Date, User)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<PatientBill> getRefundedBills(Date startDate, Date endDate,
            User collector) {
        // TODO Auto-generated method stub
        return billingDAO.getRefundedBills(startDate, endDate, collector);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveDepartement(
     * org.openmrs.module.mohbilling.model.Department)
     */
    @Override
    public Department saveDepartement(Department departement) {
        return billingDAO.saveDepartement(departement);
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getDepartement(java
     * .lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public Department getDepartement(Integer departementId) {
        // TODO Auto-generated method stub
        return billingDAO.getDepartement(departementId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getAllDepartements()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartements() {
        // TODO Auto-generated method stub
        return billingDAO.getAllDepartements();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveHopService(org
     * .openmrs.module.mohbilling.model.HopService)
     */
    @Override
    public HopService saveHopService(HopService service) {
        // TODO Auto-generated method stub
        return billingDAO.saveHopService(service);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getAllHopServicesByDepartement
     * (org.openmrs.module.mohbilling.model.Department)
     */
    @Override
    @Transactional(readOnly = true)
    public List<HopService> getAllHopService() {
        return billingDAO.getAllHopService();
    }

    @Override
    @Transactional(readOnly = true)
    public HopService getHopService(Integer serviceId) {
        // TODO Auto-generated method stub
        return billingDAO.getHopService(serviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public HopService getHopService(String name) {
        // TODO Auto-generated method stub
        return billingDAO.getHopService(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveAdmission(org
     * .openmrs.module.mohbilling.model.Admission)
     */
    @Override
    public Admission saveAdmission(Admission admission) {

        // TODO Auto-generated method stub
        return billingDAO.saveAdmission(admission);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getPatientAdmission
     * (java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public Admission getPatientAdmission(Integer admissionid) {
        // TODO Auto-generated method stub
        return billingDAO.getPatientAdmission(admissionid);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveGlobalBill(org
     * .openmrs.module.mohbilling.model.GlobalBill)
     */
    @Override
    public GlobalBill saveGlobalBill(GlobalBill globalBill) {
        // TODO Auto-generated method stub
        return billingDAO.saveGlobalBill(globalBill);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#GetGlobalBill(java
     * .lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public GlobalBill GetGlobalBill(Integer globalBillId) {

        return billingDAO.GetGlobalBill(globalBillId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getGlobalBillByAdmission
     * (org.openmrs.module.mohbilling.model.Admission)
     */
    @Override
    @Transactional(readOnly = true)
    public GlobalBill getGlobalBillByAdmission(Admission admission) {

        return billingDAO.getGlobalBillByAdmission(admission);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getAdmissionsListByInsurancePolicy
     * (org.openmrs.module.mohbilling.model.InsurancePolicy)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Admission> getAdmissionsListByInsurancePolicy(InsurancePolicy ip) {

        return billingDAO.getAdmissionsListByInsurancePolicy(ip);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveConsommation
     * (org.openmrs.module.mohbilling.model.Consommation)
     */
    @Override
    public void saveConsommation(Consommation consommation) {

        billingDAO.saveConsommation(consommation);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveInsuranceBill
     * (org.openmrs.module.mohbilling.model.InsuranceBill)
     */
    @Override
    public void saveInsuranceBill(InsuranceBill ib) {
        billingDAO.saveInsuranceBill(ib);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#saveThirdPartyBill
     * (org.openmrs.module.mohbilling.model.ThirdPartyBill)
     */
    @Override
    public void saveThirdPartyBill(ThirdPartyBill thirdBill) {
        billingDAO.saveThirdPartyBill(thirdBill);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getConsommation(
     * java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public Consommation getConsommation(Integer consommationId) {
        return billingDAO.getConsommation(consommationId);
    }

    @Override
    public CashPayment saveCashPayment(CashPayment cashPayment) {
        return billingDAO.saveCashPayment(cashPayment);

    }

    @Override
    public PatientServiceBill saveBilledItem(PatientServiceBill psb) {
        // TODO Auto-generated method stub
        return billingDAO.saveBilledItem(psb);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientServiceBill getPatientServiceBill(Integer patientServiceBillId) {
        // TODO Auto-generated method stub
        return billingDAO.getPatientServiceBill(patientServiceBillId);
    }

    @Override
    public void saveBillPayment(BillPayment bp) {
        // TODO Auto-generated method stub
        billingDAO.getPatientServiceBill(bp);
    }

    @Override
    public void savePaidServiceBill(PaidServiceBill paidSb) {
        billingDAO.savePaidServiceBill(paidSb);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getAllConsommationByGlobalBill(java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Consommation> getAllConsommationByGlobalBill(
            GlobalBill globalBill) {
        return billingDAO.getAllConsommationByGlobalBill(globalBill);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getGlobalBillByBillIdentifier(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public GlobalBill getGlobalBillByBillIdentifier(String billIdentifier) {

        return billingDAO.getGlobalBillByBillIdentifier(billIdentifier);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getConsommationsByBeneficiary
     * (org.openmrs.module.mohbilling.model.Beneficiary)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Consommation> getConsommationsByBeneficiary(
            Beneficiary beneficiary) {
        // TODO Auto-generated method stub
        return billingDAO.getConsommationsByBeneficiary(beneficiary);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#savePatientAccount
     * (org.openmrs.module.mohbilling.model.PatientAccount)
     */
    @Override
    public void savePatientAccount(PatientAccount account) {
        billingDAO.savePatientAccount(account);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getPatientAccount
     * (java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public PatientAccount getPatientAccount(Integer accountId) {
        return billingDAO.getPatientAccount(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientAccount getPatientAccount(Patient patient) {
        return billingDAO.getPatientAccount(patient);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getBillPayment(java
     * .lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public BillPayment getBillPayment(Integer paymentId) {

        return billingDAO.getBillPayment(paymentId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getPaidServices(
     * org.openmrs.module.mohbilling.model.BillPayment)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaidServiceBill> getPaidServices(BillPayment payment) {

        return billingDAO.getPaidServices(payment);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getConsommationByPatientBill
     * (org.openmrs.module.mohbilling.model.PatientBill)
     */
    @Override
    @Transactional(readOnly = true)
    public Consommation getConsommationByPatientBill(PatientBill patientBill) {

        return billingDAO.getConsommationByPatientBill(patientBill);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#savePaymentRefund
     * (org.openmrs.module.mohbilling.model.PaymentRefund)
     */
    @Override
    public PaymentRefund savePaymentRefund(PaymentRefund refund) {
        return billingDAO.savePaymentRefund(refund);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getPaidServiceBill
     * (java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public PaidServiceBill getPaidServiceBill(Integer paidSviceBillid) {
        return billingDAO.getPaidServiceBill(paidSviceBillid);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Transaction> getTransactions(PatientAccount acc,
            Date startDate, Date endDate, String reason) {
        return billingDAO.getTransactions(acc, startDate, endDate, reason);
    }

    @Override
    public DepositPayment saveDepositPayment(DepositPayment depositPayment) {
        return billingDAO.saveDepositPayment(depositPayment);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getHospitalServicesByDepartment
     * (org.openmrs.module.mohbilling.model.Department)
     */
    @Override
    @Transactional(readOnly = true)
    public List<HopService> getHospitalServicesByDepartment(
            Department department) {
        return billingDAO.getHospitalServicesByDepartment(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Integer id) {
        return billingDAO.getTransactionById(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getServiceByName(java.
     * lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public HopService getServiceByName(String name) {

        return billingDAO.getServiceByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaidServiceBill> getPaidItemsByBillPayments(
            List<BillPayment> payments) {
        // TODO Auto-generated method stub
        return billingDAO.getPaidItemsByBillPayments(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientServiceBill> getBillItemsByCategory(
            Consommation consommation, HopService service) {
        return billingDAO.getBillItemsByCategory(consommation, service);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientServiceBill> getBillItemsByGroupedCategories(
            Consommation consommation, List<HopService> services) {
        return billingDAO.getBillItemsByGroupedCategories(consommation, services);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getGlobalBills(java.util
     * .Date, java.util.Date)
     */
    @Override
    @Transactional(readOnly = true)
    public List<GlobalBill> getGlobalBills(Date date1, Date date2) {
        // TODO Auto-generated method stub
        return billingDAO.getGlobalBills(date1, date2);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GlobalBill> getGlobalBills() {
        // TODO Auto-generated method stub
        return billingDAO.getGlobalBills();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GlobalBill> getGlobalBills(Date date1, Date date2, Insurance insurance) {
        // TODO Auto-generated method stub
        return billingDAO.getGlobalBills(date1, date2, insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GlobalBill> getGlobalBillsWithNullInsurance() {
        return billingDAO.getGlobalBillsWithNullInsurance();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consommation> getConsommationByGlobalBills(
            List<GlobalBill> globalBills) {
        return billingDAO.getConsommationByGlobalBills(globalBills);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getAllSubmittedPaymentRefunds()
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefund> getAllSubmittedPaymentRefunds() {
        return billingDAO.getAllSubmittedPaymentRefunds();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentRefund getRefundById(Integer id) {
        return billingDAO.getRefundById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaidServiceBillRefund getPaidServiceBillRefund(
            Integer paidSviceBillRefundid) {
        return billingDAO.getPaidServiceBillRefund(paidSviceBillRefundid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefund> getRefundsByBillPayment(BillPayment payment) {
        return billingDAO.getRefundsByBillPayment(payment);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.service.BillingService#
     * getRefundsBetweenDatesAndByCollector(java.util.Date,
     * java.util.Date, org.openmrs.User)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentRefund> getRefundsBetweenDatesAndByCollector(
            Date startDate, Date endDate, User collector) {
        return billingDAO.getRefundsBetweenDatesAndByCollector(startDate, endDate, collector);
    }

    @Override
    @Transactional(readOnly = true)
    public InsurancePolicy getInsurancePolicyByThirdParty(ThirdParty t) {
        System.out.print(" am getting in getinsurancepolicybythird party in billingserviceImplement " + t);
        return billingDAO.getInsurancePolicyByThirdParty(t);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getConsommations(java.
     * util.Date, java.util.Date, org
     * .openmrs.module.mohbilling.model.Insurance,
     * org.openmrs.module.mohbilling.model.ThirdParty, org.openmrs.User)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Consommation> getConsommations(Date startDate, Date endDate,
            Insurance insurance, ThirdParty tp, User billCreator,
            Department department, int limit, int offSet) {
        return billingDAO.getConsommations(startDate, endDate, insurance, tp, billCreator, department, limit, offSet);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalConsommations(Date startDate, Date endDate, Insurance insurance, ThirdParty tp,
            User billCreator, Department department) {
        return billingDAO.getTotalConsommations(startDate, endDate, insurance, tp, billCreator, department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consommation> getConsommationsWithPatientNotConfirmed(Date startDate, Date endDate) {
        return billingDAO.getConsommationsWithPatientNotConfirmed(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consommation> getDCPConsommations(Date startDate, Date endDate, User billCreator) {
        return billingDAO.getDCPConsommations(startDate, endDate, billCreator);
    }

    @Override
    public void updateOtherInsurances(ServiceCategory sc) {
        billingDAO.updateOtherInsurances(sc);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openmrs.module.mohbilling.service.BillingService#getTransactions(java.
     * util.Date, java.util.Date, org
     * .openmrs.User, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactions(Date startDate, Date endDate,
            User collector, String type) {
        return billingDAO.getTransactions(startDate, endDate, collector, type);
    }

    @Override
    @Transactional(readOnly = true)
    public GlobalBill getOpenGlobalBillByInsuranceCardNo(String insuranceCardNo) {
        return billingDAO.getOpenGlobalBillByInsuranceCardNo(insuranceCardNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicy> getAllInsurancePoliciesByPatient(Patient patient) throws DAOException {
        return billingDAO.getAllInsurancePoliciesByPatient(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public FacilityServicePrice getFacilityServiceByName(String name) {
        return billingDAO.getFacilityServiceByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceReport getBillItemsReportByCategory(Integer insuranceId, Date startDate, Date endDate) {
        return billingDAO.getBillItemsByCategoryFromMamba(insuranceId, startDate, endDate);
    }

	public String getDiagnosisFromAdmissionToDischarge(String primaryAndSecondaryDiagnosis, Date startDate, Date endDate, Integer patientid){
		return billingDAO.getDiagnosisFromAdmissionToDischarge(primaryAndSecondaryDiagnosis,startDate,endDate,patientid);
	};

    @Override
    @Transactional(readOnly = true)
    public List<PatientBillIrembo> getUnpaidBills(Patient patient) throws DAOException {
        //billingDAO.getUnpaidBills(patient).getFirst().
        return billingDAO.getUnpaidBills(patient);
    }

    @Override
    public List<PatientBill> getUnpaidBillsWithInvoiceNumber() throws DAOException {
        return billingDAO.getUnpaidBillsWithInvoiceNumber();
    }

    public static String detectTelco(String phoneNumber) {

        // Normalize number
        String normalized = phoneNumber.replaceAll("\\s+", "");

        if (normalized.startsWith("+250")) {
            normalized = "0" + normalized.substring(4);
        }

        if (!normalized.matches("^07\\d{8}$")) {
            return "Unknown operator";
        }

        String prefix = normalized.substring(0, 3);

        switch (prefix) {
            case "078":
            case "079":
                return "MTN";

            case "072":
            case "073":
                return "AIRTEL";

            default:
                return "Unknown operator";
        }
    }

    @Override
	public void initIremboPay(Patient patient, PatientBill patientBill, String phoneNumber) throws DAOException {

        Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT).equalsIgnoreCase("production")?true:false;
        String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);

        String iremboPayProductCode = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_PRODUCT_CODE);

        String transactionId = UUID.randomUUID().toString();
        //Create Customer information
        Customer customer = new Customer();
        customer.setFullName(patient.getPersonName().getFullName());
        customer.setPhoneNumber(phoneNumber);

        List<PaymentItem> paymentItems = new ArrayList<>();
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCode(iremboPayProductCode);
        paymentItem.setQuantity(1);
        paymentItem.setUnitAmount(patientBill.getAmount().setScale(0, RoundingMode.CEILING).doubleValue());

        //Add the Item to the list
        paymentItems.add(paymentItem);
        Department myDepartment = billingDAO.getConsommationByPatientBill(patientBill).getDepartment();
        
        String invoiceDescription = myDepartment.getName();

        //Check if the account hold a specific account identifier and make sure to use that once please
        String iremboPayAccountIdentifier = myDepartment.getAccountIdentifier();
        if(iremboPayAccountIdentifier == null){
            //Once configuration does not support specific fallback to default one
            iremboPayAccountIdentifier = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_ACCOUNT_IDENTIFIER);
        }

        String language = "EN";

        Environment iremboEnv = null;
        if(!isProduction) {
            iremboEnv = Environment.SANDBOX;
        } else {
            iremboEnv = Environment.PRODUCTION;
        }

        Invoice invoice = null;
        IremboPay iremboPay = new IremboPay(iremboPaySecretKey,iremboEnv);

        //Here Make sure not to recreate the billId while the patient bill already hold one
        patientBill = refreshPatientBill(patientBill);
        if (!hasText(patientBill.getInvoiceNumber())) {
            log.info("Irembo single init createInvoice start: patientBillId=" + patientBill.getPatientBillId()
                    + ", transactionId=" + transactionId
                    + ", amount=" + patientBill.getAmount()
                    + ", phoneNumber=" + phoneNumber
                    + ", accountIdentifier=" + iremboPayAccountIdentifier);
            long createStartedAt = System.currentTimeMillis();
            IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.createInvoice(transactionId, iremboPayAccountIdentifier, customer, paymentItems, invoiceDescription, null, language);
            logIremboApiCall("createInvoice(single-init)", createStartedAt, iremboPayResponse);
            if (iremboPayResponse == null || !Boolean.TRUE.equals(iremboPayResponse.isSuccess())
                    || iremboPayResponse.getData() == null) {
                log.warn("Irembo single init aborted: createInvoice failed for patientBillId="
                        + patientBill.getPatientBillId() + ", transactionId=" + transactionId);
                return;
            }
            invoice = iremboPayResponse.getData();
            //After the invoice create operation make sure to save comming returned information into database for later reference
            patientBill.setReferenceId(transactionId);
            patientBill.setInvoiceNumber(invoice.getInvoiceNumber());
            patientBill.setInitiatedAt(new Date());
            patientBill.setPhoneNumber(phoneNumber);
            patientBill.setTransactionStatus("Pending");
            patientBill.setRetryCount(0);

            //Save the patient into the database
            billingDAO.savePatientBill(patientBill);
            log.info("Irembo single init createInvoice saved: patientBillId=" + patientBill.getPatientBillId()
                    + ", invoiceNumber=" + invoice.getInvoiceNumber()
                    + ", paymentStatus=" + invoice.getPaymentStatus());
        } else {
            log.info("Irembo init skipped createInvoice: PatientBill id=" + patientBill.getPatientBillId()
                    + " already has invoiceNumber=" + patientBill.getInvoiceNumber());
            //From Here make sure to build the invoice
            invoice = new Invoice(iremboPaySecretKey, iremboEnv);

            invoice.setInvoiceNumber(patientBill.getInvoiceNumber());
        }
        //Detect if we are in sandbox environment
        String company = detectTelco(phoneNumber);
        if(!isProduction){
            //rename the phone number to be used for testing purpose
            if(company.equalsIgnoreCase("AIRTEL")){
                phoneNumber = "0731234567";
            } else if(company.equalsIgnoreCase("MTN")){
                phoneNumber = "0781234567";
            }
        }

        //Now initiate the Payment
        if(!company.equalsIgnoreCase("Unknown operator")){
            String paymentUuid = UUID.randomUUID().toString();
            log.info("Irembo single init payment start: invoiceNumber=" + invoice.getInvoiceNumber()
                    + ", provider=" + company
                    + ", phoneNumber=" + phoneNumber
                    + ", paymentUuid=" + paymentUuid);
            long initiateStartedAt = System.currentTimeMillis();
            try {
                IremboPayResponse<?> initiateResponse = iremboPay.mobileMoney
                        .initiate(phoneNumber, company, invoice.getInvoiceNumber(), paymentUuid);
                logIremboApiCall("initiatePayment(single)", initiateStartedAt, initiateResponse);
            } catch (Exception e) {
                long durationMs = System.currentTimeMillis() - initiateStartedAt;
                log.error("Irembo initiatePayment(single) failed after " + durationMs
                        + "ms for invoiceNumber=" + invoice.getInvoiceNumber(), e);
            }
        } else {
            log.warn("Irembo single init payment skipped: unknown operator for phoneNumber=" + phoneNumber
                    + ", invoiceNumber=" + (invoice != null ? invoice.getInvoiceNumber() : null));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PatientBill getInvoiceStatus(String invoiceId) throws DAOException {
        log.info("Irembo status check start: invoiceId=" + invoiceId);
        Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT).equalsIgnoreCase("production")?true:false;
        String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);
        
        IremboPay iremboPay = null;
        if(!isProduction) {
            iremboPay = new IremboPay(iremboPaySecretKey, Environment.SANDBOX);
        } else {
            iremboPay = new IremboPay(iremboPaySecretKey, Environment.PRODUCTION);
        }
        IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.getInvoice(invoiceId);
        System.out.println(iremboPayResponse);
        if (iremboPayResponse == null) {
            log.warn("Irembo status check: null response object returned for invoiceId=" + invoiceId);
            return billingDAO.getPatientBillStatus(invoiceId);
        }
        Invoice invoice = iremboPayResponse.getData();
        if (invoice == null) {
            log.warn("Irembo status check: null invoice data returned for invoiceId=" + invoiceId);
            return billingDAO.getPatientBillStatus(invoiceId);
        }
        log.info("Irembo status check response: invoiceId=" + invoiceId
                + ", status=" + invoice.getPaymentStatus()
                + ", amount=" + invoice.getAmount()
                + ", paymentReference=" + invoice.getPaymentReference());
        BillingService billingService = Context.getService(BillingService.class);
        if (invoice.getPaymentStatus() != null && invoice.getPaymentStatus().equalsIgnoreCase("PAID")) {
            //Here we need to make sure update the patient bill if it was not yet marked as paid

            User iremboUser = Context.getService(UserService.class).getUserByUsername(ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_USER));

            PatientBill billToConfirm = billingDAO.getPatientBillStatusForUpdate(invoiceId);
            if (billToConfirm == null) {
                log.warn("Irembo status check: no PatientBill found for invoiceId=" + invoiceId);
                return null;
            }
            log.info("Irembo PAID pre-check: invoiceId=" + invoiceId
                    + ", patientBillId=" + billToConfirm.getPatientBillId()
                    + ", currentBillIsPaid=" + billToConfirm.getIsPaid()
                    + ", currentPaymentConfirmed=" + billToConfirm.isPaymentConfirmed()
                    + ", currentTransactionStatus=" + billToConfirm.getTransactionStatus()
                    + ", currentBillPaymentReference=" + billToConfirm.getPaymentReference());

            //Get the invoice amount
            BigDecimal invoiceAmount = BigDecimal.valueOf(invoice.getAmount()).setScale(0, RoundingMode.CEILING);
            BigDecimal billAmount = billToConfirm.getAmount().setScale(0, RoundingMode.CEILING);
            log.info("Irembo PAID amount compare: invoiceId=" + invoiceId
                    + ", iremboAmountRaw=" + invoice.getAmount()
                    + ", iremboAmountScaled=" + invoiceAmount
                    + ", billAmountScaled=" + billAmount);

            if (invoiceAmount.compareTo(billAmount) == 0) {
                String resolvedPaymentReference = resolveIremboPaymentReference(invoice.getPaymentReference(),
                        billToConfirm.getPaymentReference(), invoiceId);
                if (resolvedPaymentReference == null) {
                    log.warn("Skipping PAID invoice update because payment reference is missing for invoice " + invoiceId
                            + ", iremboPaymentReference=" + invoice.getPaymentReference()
                            + ", existingBillPaymentReference=" + billToConfirm.getPaymentReference());
                    return billToConfirm;
                }
                log.info("Irembo PAID invoice matched: invoiceId=" + invoiceId
                        + ", patientBillId=" + billToConfirm.getPatientBillId()
                        + ", resolvedPaymentReference=" + resolvedPaymentReference
                        + ", iremboPaidAt=" + invoice.getPaidAt());
                // Always update bill-level payment status fields when invoice is PAID.
                billToConfirm.setIsPaid(true);
                billToConfirm.setPaymentReference(resolvedPaymentReference);
                billToConfirm.setPaymentConfirmed(true);
                billToConfirm.setPaymentConfirmedBy(iremboUser);
                billToConfirm.setPaymentConfirmedDate(new Date());
                billToConfirm.setTransactionStatus(invoice.getPaymentStatus());

                Date paidAt = invoice.getPaidAt();
                if (paidAt != null) {
                    billToConfirm.setPaidAt(paidAt);
                } else if (billToConfirm.getPaidAt() == null) {
                    billToConfirm.setPaidAt(new Date());
                }
                billingService.savePatientBill(billToConfirm);

                synchronized (iremboPaymentCreationLock) {
                    // Create payment records only if none exists yet.
                    List<BillPayment> billPayments = billingDAO.getBillPaymentsByPatientBill(billToConfirm);
                    boolean hasNonVoidedPayment = billPayments != null && billPayments.stream()
                            .anyMatch(p -> p.getVoided() == null || !Boolean.TRUE.equals(p.getVoided()));
                    log.info("Irembo PAID payment record check: invoiceId=" + invoiceId
                            + ", patientBillId=" + billToConfirm.getPatientBillId()
                            + ", billPaymentsFound=" + (billPayments == null ? 0 : billPayments.size())
                            + ", hasNonVoidedPayment=" + hasNonVoidedPayment);
                    if (!hasNonVoidedPayment) {
                        log.info("Irembo PAID invoice creating payment records: invoiceId=" + invoiceId
                                + ", patientBillId=" + billToConfirm.getPatientBillId());
                        Date paymentDate = billToConfirm.getPaidAt() != null ? billToConfirm.getPaidAt() : new Date();

                        BillPayment billPayment = new BillPayment();
                        billPayment.setAmountPaid(billAmount);
                        billPayment.setDateReceived(paymentDate);
                        billPayment.setPatientBill(billToConfirm);
                        billPayment.setCollector(iremboUser);
                        billPayment.setCreator(iremboUser);
                        billPayment.setCreatedDate(new Date());
                        billPayment.setVoided(false);
                        billPayment.setPaymentReference(resolvedPaymentReference);
                        billPayment.setInvoiceNumber(billToConfirm.getInvoiceNumber());
                        billingService.saveBillPayment(billPayment);

                        Consommation affectedConsommation = billingService.getConsommationByPatientBill(billToConfirm);
                        if (affectedConsommation != null && affectedConsommation.getBillItems() != null) {
                            Set<PatientServiceBill> billItems = affectedConsommation.getBillItems();
                            int paidItemsCreated = 0;
                            for (PatientServiceBill psb : billItems) {
                                if (Boolean.TRUE.equals(psb.getVoided())) {
                                    continue;
                                }
                                PaidServiceBill paidSb = new PaidServiceBill();
                                paidSb.setBillItem(psb);
                                BigDecimal paidQuantity = psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO;
                                paidSb.setPaidQty(paidQuantity);
                                paidSb.setBillPayment(billPayment);
                                paidSb.setCreator(iremboUser);
                                paidSb.setCreatedDate(new Date());
                                paidSb.setVoided(false);
                                BillPaymentUtil.createPaidServiceBill(paidSb);

                                psb.setPaid(true);
                                psb.setPaidQuantity(psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO);
                                ConsommationUtil.createPatientServiceBill(psb);
                                paidItemsCreated++;
                            }
                            log.info("Irembo PAID invoice paid item updates complete: invoiceId=" + invoiceId
                                    + ", patientBillId=" + billToConfirm.getPatientBillId()
                                    + ", paidItemsCreated=" + paidItemsCreated);
                        }
                    } else {
                        log.info("Irembo PAID invoice skipped BillPayment creation (already exists): invoiceId=" + invoiceId
                                + ", patientBillId=" + billToConfirm.getPatientBillId());
                    }
                }
                System.out.println("Irembopay invoice check: confirmed payment for PatientBill id=" + billToConfirm.getPatientBillId());
                log.info("Irembo status check complete (PAID): invoiceId=" + invoiceId
                        + ", patientBillId=" + billToConfirm.getPatientBillId()
                        + ", isPaid=" + billToConfirm.getIsPaid()
                        + ", paymentConfirmed=" + billToConfirm.isPaymentConfirmed()
                        + ", paymentReference=" + billToConfirm.getPaymentReference()
                        + ", transactionStatus=" + billToConfirm.getTransactionStatus());
            } else {
                // Here make sure to log some error to checked on later
                log.warn("Skipping PAID invoice update due amount mismatch for invoice " + invoiceId
                        + ": iremboAmount=" + invoiceAmount + ", billAmount=" + billAmount
                        + ", patientBillId=" + billToConfirm.getPatientBillId()
                        + ", iremboPaymentReference=" + invoice.getPaymentReference()
                        + ", billPaymentReference=" + billToConfirm.getPaymentReference());
                System.out.println("Paid amount from irembo pay " + invoiceAmount  + " is different from the expected amount " + billAmount);
            }
        } else {
            log.info("Irembo status check non-PAID: invoiceId=" + invoiceId + ", status=" + invoice.getPaymentStatus());
        }
        
        return billingService.getPatientBillStatus(invoiceId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processIrembopayCallback(String invoiceNumber, Boolean success, BigDecimal callbackAmount,
            String paymentReference, String paidAtStr, String paymentStatus) {
        PatientBill billToConfirm = billingDAO.getPatientBillStatusForUpdate(invoiceNumber);
        if (billToConfirm == null) {
            throw new IllegalArgumentException("No PatientBill found for invoice number " + invoiceNumber);
        }
        if (!Boolean.TRUE.equals(success)) {
            return;
        }
        if (callbackAmount == null) {
            throw new IllegalArgumentException("Callback amount is missing for invoice " + invoiceNumber);
        }
        BigDecimal callbackAmountScaled = callbackAmount.setScale(0, RoundingMode.CEILING);
        BigDecimal billAmount = billToConfirm.getAmount().setScale(0, RoundingMode.CEILING);
        if (callbackAmountScaled.compareTo(billAmount) != 0) {
            throw new IllegalArgumentException(String.format(
                "Amount mismatch: callback amount=%.2f, bill amount=%.2f for invoice %s",
                callbackAmountScaled.doubleValue(), billAmount.doubleValue(), invoiceNumber));
        }
        String resolvedPaymentReference = resolveIremboPaymentReference(paymentReference,
                billToConfirm.getPaymentReference(), invoiceNumber);
        if (resolvedPaymentReference == null) {
            throw new IllegalArgumentException(
                    "Payment reference is missing for invoice " + invoiceNumber + "; callback processing aborted");
        }
        confirmPatientBillPayment(billToConfirm, callbackAmount, resolvedPaymentReference, paidAtStr, paymentStatus);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processIrembopayBatchCallback(String batchNumber, List<String> childInvoices, Boolean success,
            String paymentReference, String paidAtStr, String paymentStatus) {
        log.info("Irembo batch callback start: batchNumber=" + batchNumber
                + ", success=" + success
                + ", paymentStatus=" + paymentStatus
                + ", childInvoicesFromPayload=" + (childInvoices == null ? 0 : childInvoices.size()));
        if (!Boolean.TRUE.equals(success)) {
            log.warn("Irembo batch callback skipped because success=false: batchNumber=" + batchNumber);
            return;
        }
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Batch number is missing for batch callback");
        }
        String resolvedPaymentReference = resolveIremboPaymentReference(paymentReference, null, batchNumber);
        if (resolvedPaymentReference == null) {
            throw new IllegalArgumentException(
                    "Payment reference is missing for batch " + batchNumber + "; callback processing aborted");
        }

        List<String> invoicesToProcess = new ArrayList<>();
        if (childInvoices != null) {
            for (String childInvoice : childInvoices) {
                if (childInvoice != null && !childInvoice.trim().isEmpty()) {
                    invoicesToProcess.add(childInvoice.trim());
                }
            }
        }
        if (invoicesToProcess.isEmpty()) {
            List<PatientBill> billsByBatch = billingDAO.getPatientBillsByBatchNumber(batchNumber);
            if (billsByBatch != null) {
                for (PatientBill bill : billsByBatch) {
                    if (bill != null && bill.getInvoiceNumber() != null && !bill.getInvoiceNumber().trim().isEmpty()) {
                        invoicesToProcess.add(bill.getInvoiceNumber().trim());
                    }
                }
            }
            log.info("Irembo batch callback child invoice fallback from DB: batchNumber=" + batchNumber
                    + ", invoicesResolvedFromDb=" + invoicesToProcess.size());
        }
        if (invoicesToProcess.isEmpty()) {
            throw new IllegalArgumentException(
                    "No child invoices found to process for batch " + batchNumber);
        }

        int processed = 0;
        int skipped = 0;
        for (String childInvoice : invoicesToProcess) {
            log.info("Irembo batch callback invoice processing start: batchNumber=" + batchNumber
                    + ", childInvoice=" + childInvoice);
            PatientBill billToConfirm = billingDAO.getPatientBillStatusForUpdate(childInvoice);
            if (billToConfirm == null) {
                log.warn("Irembo batch callback skipped: no PatientBill found for child invoice " + childInvoice
                        + ", batchNumber=" + batchNumber);
                skipped++;
                continue;
            }
            if (billToConfirm.getBatchNumber() != null && !batchNumber.equals(billToConfirm.getBatchNumber())) {
                log.warn("Irembo batch callback skipped: child invoice " + childInvoice
                        + " belongs to different batch " + billToConfirm.getBatchNumber()
                        + ", callback batchNumber=" + batchNumber);
                skipped++;
                continue;
            }
            log.info("Irembo batch callback invoice pre-check: batchNumber=" + batchNumber
                    + ", childInvoice=" + childInvoice
                    + ", patientBillId=" + billToConfirm.getPatientBillId()
                    + ", billAmount=" + billToConfirm.getAmount()
                    + ", isPaid=" + billToConfirm.getIsPaid()
                    + ", paymentConfirmed=" + billToConfirm.isPaymentConfirmed()
                    + ", existingPaymentReference=" + billToConfirm.getPaymentReference());
            boolean paymentCreated = confirmPatientBillPayment(billToConfirm, billToConfirm.getAmount(),
                    resolvedPaymentReference, paidAtStr, paymentStatus);
            if (paymentCreated) {
                log.info("Irembo batch callback invoice processed successfully: batchNumber=" + batchNumber
                        + ", childInvoice=" + childInvoice
                        + ", patientBillId=" + billToConfirm.getPatientBillId()
                        + ", paymentReference=" + resolvedPaymentReference);
            } else {
                log.info("Irembo batch callback invoice skipped by idempotency: batchNumber=" + batchNumber
                        + ", childInvoice=" + childInvoice
                        + ", patientBillId=" + billToConfirm.getPatientBillId());
            }
            processed++;
        }
        log.info("Irembo batch callback processed: batchNumber=" + batchNumber
                + ", totalInvoices=" + invoicesToProcess.size()
                + ", processed=" + processed
                + ", skipped=" + skipped);
    }

    private boolean confirmPatientBillPayment(PatientBill billToConfirm, BigDecimal amountPaid, String resolvedPaymentReference,
            String paidAtStr, String paymentStatus) {
        synchronized (iremboPaymentCreationLock) {
            // Idempotency: if this bill already has a payment (e.g. from scheduler/status check or duplicate callback),
            // do not create another BillPayment nor repeat PaidServiceBill updates.
            List<BillPayment> existingPayments = billingDAO.getBillPaymentsByPatientBill(billToConfirm);
            boolean hasNonVoidedPayment = existingPayments != null && existingPayments.stream()
                .anyMatch(p -> p.getVoided() == null || !Boolean.TRUE.equals(p.getVoided()));
            if (hasNonVoidedPayment) {
                return false;
            }

            User currentUser = Context.getAuthenticatedUser();
            billToConfirm.setIsPaid(true);
            billToConfirm.setPaymentReference(resolvedPaymentReference);
            billToConfirm.setPaymentConfirmed(true);
            billToConfirm.setPaymentConfirmedBy(currentUser);
            billToConfirm.setPaymentConfirmedDate(new Date());
            billToConfirm.setTransactionStatus(paymentStatus);
            Date paidAt = parsePaidAt(paidAtStr);
            billToConfirm.setPaidAt(paidAt != null ? paidAt : new Date());
            billingDAO.savePatientBill(billToConfirm);

            Date paymentDate = billToConfirm.getPaidAt();
            BillPayment billPayment = new BillPayment();
            billPayment.setAmountPaid(amountPaid);
            billPayment.setDateReceived(paymentDate);
            billPayment.setPatientBill(billToConfirm);
            billPayment.setCollector(currentUser);
            billPayment.setCreator(currentUser);
            billPayment.setCreatedDate(new Date());
            billPayment.setVoided(false);
            billPayment.setPaymentReference(resolvedPaymentReference);
            billPayment.setInvoiceNumber(billToConfirm.getInvoiceNumber());
            billingDAO.getPatientServiceBill(billPayment);

            Consommation affectedConsommation = billingDAO.getConsommationByPatientBill(billToConfirm);
            if (affectedConsommation != null && affectedConsommation.getBillItems() != null) {
                Set<PatientServiceBill> billItems = affectedConsommation.getBillItems();
                for (PatientServiceBill psb : billItems) {
                    PaidServiceBill paidSb = new PaidServiceBill();
                    paidSb.setBillItem(psb);
                    BigDecimal paidQuantity = psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO;
                    paidSb.setPaidQty(paidQuantity);
                    paidSb.setBillPayment(billPayment);
                    paidSb.setCreator(currentUser);
                    paidSb.setCreatedDate(new Date());
                    paidSb.setVoided(false);
                    billingDAO.savePaidServiceBill(paidSb);

                    psb.setPaid(true);
                    psb.setPaidQuantity(psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO);
                    ConsommationUtil.createPatientServiceBill(psb);
                }
            }
            return true;
        }
    }

    private static Date parsePaidAt(String paidAtStr) {
        if (paidAtStr == null || paidAtStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(paidAtStr.trim());
        } catch (Exception e1) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(paidAtStr.trim());
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static String resolveIremboPaymentReference(String incomingReference, String existingBillReference,
            String invoiceNumber) {
        if (incomingReference != null && !incomingReference.trim().isEmpty()) {
            String incomingTrimmed = incomingReference.trim();
            if (!incomingTrimmed.equals(invoiceNumber)) {
                return incomingTrimmed;
            }
        }
        if (existingBillReference != null && !existingBillReference.trim().isEmpty()) {
            String existingTrimmed = existingBillReference.trim();
            if (!existingTrimmed.equals(invoiceNumber)) {
                return existingTrimmed;
            }
        }
        return null;
    }

    public PatientBill getPatientBillByInvoiceNumber(String invoiceId) throws DAOException {
        return billingDAO.getPatientBillByInvoiceNumber(invoiceId);
    }

    public List<PatientServiceBill> getPatientServiceBillByConsomation(Integer consommationId) throws DAOException {
        return billingDAO.getPatientServiceBillByConsomation(consommationId);
    }

    public PatientBill getPatientBillStatus(String invoiceId) throws DAOException {
        return billingDAO.getPatientBillStatus(invoiceId);
    }

    @Override
	public List<Consommation> getConsommationsOld(Date startDate, Date endDate,
			Insurance insurance, ThirdParty tp, User billCreator,Department department) {
		return billingDAO.getConsommationsOld(startDate, endDate, insurance, tp, billCreator,department);
	}

    @Override
	public String createIremboInvoice(Patient patient, PatientBill patientBill, String phoneNumber) throws DAOException {
        patientBill = refreshPatientBill(patientBill);
        if (hasText(patientBill.getInvoiceNumber())) {
            log.info("Irembo createInvoice skipped: PatientBill id=" + patientBill.getPatientBillId()
                    + " already has invoiceNumber=" + patientBill.getInvoiceNumber());
            return patientBill.getInvoiceNumber().trim();
        }

            Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT).equalsIgnoreCase("production")?true:false;
            String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);

            String iremboPayProductCode = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_PRODUCT_CODE);

            String transactionId = UUID.randomUUID().toString();
            //Create Customer information
            Customer customer = new Customer();
            customer.setFullName(patient.getPersonName().getFullName());
            customer.setPhoneNumber(phoneNumber);

            List<PaymentItem> paymentItems = new ArrayList<>();
            PaymentItem paymentItem = new PaymentItem();
            paymentItem.setCode(iremboPayProductCode);
            paymentItem.setQuantity(1);
            paymentItem.setUnitAmount(patientBill.getAmount().setScale(0, RoundingMode.CEILING).doubleValue());

            //Add the Item to the list
            paymentItems.add(paymentItem);
            Department myDepartment = billingDAO.getConsommationByPatientBill(patientBill).getDepartment();
            String invoiceDescription = myDepartment.getName();

            //Check if the account hold a specific account identifier and make sure to use that once please
            String iremboPayAccountIdentifier = myDepartment.getAccountIdentifier();
            if(iremboPayAccountIdentifier == null){
                //Once configuration does not support specific fallback to default one
                iremboPayAccountIdentifier = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_ACCOUNT_IDENTIFIER);
            }

            //Create a the expiration Date to be 24Hours
            // Date expiryDate = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));

            String language = "EN";

            IremboPay iremboPay = null;
            if(!isProduction) {
                iremboPay = new IremboPay(iremboPaySecretKey, Environment.SANDBOX);
            } else {
                iremboPay = new IremboPay(iremboPaySecretKey, Environment.PRODUCTION);
            }
            //Start by creating invoice
            log.info("Irembo createInvoice start: patientBillId=" + patientBill.getPatientBillId()
                    + ", transactionId=" + transactionId
                    + ", amount=" + patientBill.getAmount()
                    + ", phoneNumber=" + phoneNumber
                    + ", accountIdentifier=" + iremboPayAccountIdentifier);
            long createStartedAt = System.currentTimeMillis();
            IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.createInvoice(transactionId, iremboPayAccountIdentifier, customer, paymentItems, invoiceDescription, null, language);
            logIremboApiCall("createInvoice(child)", createStartedAt, iremboPayResponse);
            if (iremboPayResponse == null || !Boolean.TRUE.equals(iremboPayResponse.isSuccess())
                    || iremboPayResponse.getData() == null) {
                throw new DAOException("Irembo createInvoice failed for patientBillId="
                        + patientBill.getPatientBillId() + ", transactionId=" + transactionId
                        + ", response=" + iremboPayResponse);
            }
            Invoice invoice = iremboPayResponse.getData();
            //After the invoice create operation make sure to save comming returned information into database for later reference
            patientBill.setReferenceId(transactionId);
            patientBill.setInvoiceNumber(invoice.getInvoiceNumber());
            patientBill.setInitiatedAt(new Date());
            patientBill.setPhoneNumber(phoneNumber);
            patientBill.setTransactionStatus("Pending");
            patientBill.setRetryCount(0);

            //Save the patient into the database
            billingDAO.savePatientBill(patientBill);
            log.info("Irembo createInvoice saved: patientBillId=" + patientBill.getPatientBillId()
                    + ", invoiceNumber=" + invoice.getInvoiceNumber()
                    + ", paymentStatus=" + invoice.getPaymentStatus());

            return invoice.getInvoiceNumber();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Compact log line for an Irembo API call: operation, duration, success/message, and response body.
     */
    private void logIremboApiCall(String operation, long startedAtMs, Object response) {
        long durationMs = System.currentTimeMillis() - startedAtMs;
        if (response == null) {
            log.warn("Irembo " + operation + " completed in " + durationMs + "ms with null response");
            return;
        }
        if (response instanceof IremboPayResponse) {
            IremboPayResponse<?> iremboResponse = (IremboPayResponse<?>) response;
            String level = Boolean.TRUE.equals(iremboResponse.isSuccess()) ? "info" : "warn";
            String message = "Irembo " + operation + " completed in " + durationMs + "ms"
                    + ", success=" + iremboResponse.isSuccess()
                    + ", message=" + iremboResponse.getMessage()
                    + ", response=" + iremboResponse;
            if ("warn".equals(level)) {
                log.warn(message);
            } else {
                log.info(message);
            }
            return;
        }
        log.info("Irembo " + operation + " completed in " + durationMs + "ms, response=" + response);
    }

    private PatientBill refreshPatientBill(PatientBill patientBill) throws DAOException {
        if (patientBill == null || patientBill.getPatientBillId() == null) {
            return patientBill;
        }
        PatientBill latest = billingDAO.getPatientBill(patientBill.getPatientBillId());
        return latest != null ? latest : patientBill;
    }

    private String resolveExistingBatchNumber(List<String> invoices) throws DAOException {
        if (invoices == null || invoices.isEmpty()) {
            return null;
        }
        String resolvedBatchNumber = null;
        for (String invoiceNumber : invoices) {
            if (!hasText(invoiceNumber)) {
                continue;
            }
            PatientBill patientBill = billingDAO.getPatientBillStatus(invoiceNumber.trim());
            if (patientBill == null) {
                continue;
            }
            String batchNumber = patientBill.getBatchNumber();
            if (!hasText(batchNumber)) {
                continue;
            }
            String trimmedBatch = batchNumber.trim();
            if (resolvedBatchNumber == null) {
                resolvedBatchNumber = trimmedBatch;
            } else if (!resolvedBatchNumber.equals(trimmedBatch)) {
                log.warn("Irembo batch resolve: invoices reference different batch numbers ("
                        + resolvedBatchNumber + " vs " + trimmedBatch + ") for invoice " + invoiceNumber.trim());
            }
        }
        return resolvedBatchNumber;
    }

    /**
     * Sets the given batchNumber on every PatientBill found for the supplied invoice numbers,
     * skipping bills that are missing or already mapped to this batch. Returns the number of
     * bills that were updated (newly mapped).
     */
    private int applyBatchNumberToBills(String batchNumber, List<String> invoiceNumbers) throws DAOException {
        if (!hasText(batchNumber) || invoiceNumbers == null || invoiceNumbers.isEmpty()) {
            return 0;
        }
        String trimmedBatch = batchNumber.trim();
        int updated = 0;
        Set<String> processed = new LinkedHashSet<>();
        for (String invoiceNumber : invoiceNumbers) {
            if (!hasText(invoiceNumber)) {
                continue;
            }
            String key = invoiceNumber.trim();
            if (!processed.add(key)) {
                continue;
            }
            PatientBill patientBill = billingDAO.getPatientBillStatus(key);
            if (patientBill == null) {
                log.warn("Irembo batch mapping skipped: no PatientBill found for invoice " + key
                        + ", batchNumber=" + trimmedBatch);
                continue;
            }
            if (trimmedBatch.equals(patientBill.getBatchNumber())) {
                continue;
            }
            patientBill.setBatchNumber(trimmedBatch);
            billingDAO.savePatientBill(patientBill);
            updated++;
        }
        return updated;
    }

    public boolean canWeInitiateBatch(List<String> invoices) throws DAOException {
        if (invoices == null || invoices.isEmpty()) {
            log.warn("Irembo batch eligibility check: no invoices provided");
            return false;
        }
        String existingBatchNumber = resolveExistingBatchNumber(invoices);
        if (existingBatchNumber != null) {
            log.info("Irembo batch eligibility check: existing batch " + existingBatchNumber
                    + " found; createBatchInvoice will be skipped");
            return false;
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void initIremboPayBatch(Patient patient,List<String> invoices, String phoneNumber) throws DAOException {
        String batchDescription = patient.getPersonName().getFullName() + " medical service payments";

        String transactionId = UUID.randomUUID().toString();

        Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT).equalsIgnoreCase("production")?true:false;
        String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);

        IremboPay iremboPay = null;
        if(!isProduction) {
            iremboPay = new IremboPay(iremboPaySecretKey, Environment.SANDBOX);
        } else {
            iremboPay = new IremboPay(iremboPaySecretKey, Environment.PRODUCTION);
        }

        String batchNumber = null;
        if (canWeInitiateBatch(invoices)) {
            log.info("Irembo createBatchInvoice start: transactionId=" + transactionId
                    + ", requestedInvoiceCount=" + (invoices == null ? 0 : invoices.size())
                    + ", invoices=" + invoices
                    + ", phoneNumber=" + phoneNumber);
            long batchCreateStartedAt = System.currentTimeMillis();
            IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.createBatchInvoice(invoices,transactionId,batchDescription);
            logIremboApiCall("createBatchInvoice", batchCreateStartedAt, iremboPayResponse);
            if (iremboPayResponse == null || !Boolean.TRUE.equals(iremboPayResponse.isSuccess())
                    || iremboPayResponse.getData() == null) {
                log.warn("Irembo batch invoice creation failed: transactionId=" + transactionId
                        + ", requestedInvoiceCount=" + (invoices == null ? 0 : invoices.size())
                        + ", response=" + iremboPayResponse);
                return;
            }

            Invoice batchInvoice = iremboPayResponse.getData();
            batchNumber = batchInvoice.getBatchNumber();
            // Fallback: some batch responses carry the batch id in invoiceNumber when type == BATCH.
            if (!hasText(batchNumber) && "BATCH".equalsIgnoreCase(batchInvoice.getType())) {
                batchNumber = batchInvoice.getInvoiceNumber();
                log.warn("Irembo batch response missing batchNumber; falling back to invoiceNumber="
                        + batchNumber + ", transactionId=" + transactionId);
            }
            if (!hasText(batchNumber)) {
                log.warn("Irembo batch invoice response missing batchNumber: transactionId=" + transactionId
                        + ", responseInvoiceNumber=" + batchInvoice.getInvoiceNumber());
                return;
            }

            List<String> childInvoices = batchInvoice.getChildInvoices();
            if (childInvoices == null || childInvoices.isEmpty()) {
                log.warn("Irembo batch invoice response has no childInvoices; mapping requested invoices instead: transactionId="
                        + transactionId + ", batchNumber=" + batchNumber);
            }

            // Map using both the requested invoices and any child invoices returned by Irembo,
            // so bills always get their batch_number even if childInvoices is empty or mismatched.
            List<String> invoicesToMap = new ArrayList<>();
            if (invoices != null) {
                invoicesToMap.addAll(invoices);
            }
            if (childInvoices != null) {
                invoicesToMap.addAll(childInvoices);
            }
            int updatedBills = applyBatchNumberToBills(batchNumber, invoicesToMap);
            log.info("Irembo batch mapping complete: transactionId=" + transactionId
                    + ", batchNumber=" + batchNumber
                    + ", type=" + batchInvoice.getType()
                    + ", paymentStatus=" + batchInvoice.getPaymentStatus()
                    + ", amount=" + batchInvoice.getAmount()
                    + ", childInvoicesCount=" + (childInvoices == null ? 0 : childInvoices.size())
                    + ", requestedInvoiceCount=" + (invoices == null ? 0 : invoices.size())
                    + ", updatedBills=" + updatedBills);
        } else {
            batchNumber = resolveExistingBatchNumber(invoices);
            if (!hasText(batchNumber)) {
                log.warn("Irembo batch skipped: createBatchInvoice not allowed and no existing batch number found for invoices="
                        + invoices);
                return;
            }
            // Backfill batch_number on any requested bill that has invoice_number but is still missing batch_number.
            int backfilledBills = applyBatchNumberToBills(batchNumber, invoices);
            log.info("Irembo batch creation skipped; reusing existing batchNumber=" + batchNumber
                    + ", backfilledBills=" + backfilledBills);
        }

        if (!hasText(batchNumber)) {
            log.warn("Irembo batch payment skipped: batchNumber is missing");
            return;
        }

        // Fail hard if no bill ended up mapped to this batch; do not initiate MoMo for an unmapped batch.
        List<PatientBill> billsForBatch = billingDAO.getPatientBillsByBatchNumber(batchNumber);
        if (billsForBatch == null || billsForBatch.isEmpty()) {
            throw new DAOException("Irembo batch aborted: no PatientBill mapped to batchNumber " + batchNumber
                    + " for transactionId=" + transactionId);
        }
        //Detect if we are in sandbox environment
        String company = detectTelco(phoneNumber);
        if(!isProduction){
            //rename the phone number to be used for testing purpose
            if(company.equalsIgnoreCase("AIRTEL")){
                phoneNumber = "0731234567";
            } else if(company.equalsIgnoreCase("MTN")){
                phoneNumber = "0781234567";
            } else {
                log.error("Unable to detect phonenumber and company " + phoneNumber);
            }
        }

        //Now initiate the Payment
        // Guard the MoMo initiate call: a payment-initiation failure must NOT roll back the
        // batch_number mapping we already persisted above.
        if(!company.equalsIgnoreCase("Unknown operator")){
            String paymentUuid = UUID.randomUUID().toString();
            log.info("Irembo batch payment start: batchNumber=" + batchNumber
                    + ", provider=" + company
                    + ", phoneNumber=" + phoneNumber
                    + ", paymentUuid=" + paymentUuid
                    + ", mappedBillCount=" + billsForBatch.size());
            long initiateStartedAt = System.currentTimeMillis();
            try {
                IremboPayResponse<?> initiateResponse = iremboPay.mobileMoney
                        .initiate(phoneNumber, company, batchNumber, paymentUuid);
                logIremboApiCall("initiatePayment(batch)", initiateStartedAt, initiateResponse);
            } catch (Exception e) {
                long durationMs = System.currentTimeMillis() - initiateStartedAt;
                log.error("Irembo initiatePayment(batch) failed after " + durationMs
                        + "ms for batchNumber=" + batchNumber
                        + "; batch mapping is already saved", e);
            }
        } else {
            log.warn("Irembo batch payment skipped: unknown operator for phoneNumber=" + phoneNumber
                    + ", batchNumber=" + batchNumber);
        }
    }

	@Override
	public RhipIntegrationLog saveRhipIntegrationLog(RhipIntegrationLog logEntry) {
		return billingDAO.saveRhipIntegrationLog(logEntry);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RhipIntegrationLog> getRecentRhipIntegrationLogs(Integer limit) {
		return billingDAO.getRecentRhipIntegrationLogs(limit);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RhipIntegrationLog> getRhipIntegrationLogs(RhipIntegrationLogSearchCriteria criteria, Integer firstResult,
	                                                       Integer maxResults) {
		return billingDAO.getRhipIntegrationLogs(criteria, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly = true)
	public Integer countRhipIntegrationLogs(RhipIntegrationLogSearchCriteria criteria) {
		return billingDAO.countRhipIntegrationLogs(criteria);
	}

}
