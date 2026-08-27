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
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ThirdPartyBillUtil;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.irembo.Invoice;
import org.openmrs.module.mohbilling.irembo.IremboPay;
import org.openmrs.module.mohbilling.irembo.models.Customer;
import org.openmrs.module.mohbilling.irembo.models.PaymentItem;
import org.openmrs.module.mohbilling.irembo.util.Environment;
import org.openmrs.module.mohbilling.irembo.util.IremboInvoiceResult;
import org.openmrs.module.mohbilling.irembo.util.IremboPayInitiationResult;
import org.openmrs.module.mohbilling.irembo.util.IremboPayLogUtil;
import org.openmrs.module.mohbilling.irembo.util.IremboPayNetworkUtil;
import org.openmrs.module.mohbilling.irembo.util.IremboPayResponse;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.util.ConfigUtil;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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
        initIremboPayWithResult(patient, patientBill, phoneNumber);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public IremboPayInitiationResult initIremboPayWithResult(Patient patient, PatientBill patientBill,
            String phoneNumber) throws DAOException {
        patientBill = refreshPatientBill(patientBill);
        boolean invoiceCreatedInThisCall = !hasText(patientBill.getInvoiceNumber());

        Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT)
                .equalsIgnoreCase("production");
        String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);
        String iremboPayProductCode = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_PRODUCT_CODE);

        String transactionId = UUID.randomUUID().toString();
        Customer customer = new Customer();
        customer.setFullName(patient.getPersonName().getFullName());
        customer.setPhoneNumber(phoneNumber);

        List<PaymentItem> paymentItems = new ArrayList<>();
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCode(iremboPayProductCode);
        paymentItem.setQuantity(1);
        paymentItem.setUnitAmount(toIremboPayUnitAmount(patientBill.getAmount()));
        paymentItems.add(paymentItem);

        Department myDepartment = billingDAO.getConsommationByPatientBill(patientBill).getDepartment();
        String invoiceDescription = myDepartment.getName();
        String iremboPayAccountIdentifier = myDepartment.getAccountIdentifier();
        if (iremboPayAccountIdentifier == null) {
            iremboPayAccountIdentifier = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_ACCOUNT_IDENTIFIER);
        }

        Environment iremboEnv = isProduction ? Environment.PRODUCTION : Environment.SANDBOX;
        IremboPay iremboPay = new IremboPay(iremboPaySecretKey, iremboEnv);
        Invoice invoice = null;

        if (invoiceCreatedInThisCall) {
            log.info("Irembo single init createInvoice start: patientBillId=" + patientBill.getPatientBillId()
                    + ", transactionId=" + transactionId
                    + ", amount=" + patientBill.getAmount()
                    + ", phoneNumber=" + phoneNumber
                    + ", accountIdentifier=" + iremboPayAccountIdentifier);
            long createStartedAt = System.currentTimeMillis();
            IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.createInvoice(transactionId,
                    iremboPayAccountIdentifier, customer, paymentItems, invoiceDescription, null, "EN");
            logIremboApiCall("createInvoice(single-init)", createStartedAt, iremboPayResponse);
            if (!isSuccessfulIremboResponse(iremboPayResponse)) {
                String iremboMessage = formatIremboErrorMessage(iremboPayResponse);
                IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                        "single-init createInvoice failed, patientBillId=" + patientBill.getPatientBillId()
                                + ", transactionId=" + transactionId + ", iremboMessage=" + iremboMessage);
                return IremboPayInitiationResult.failure(
                        IremboPayInitiationResult.FailedStep.CREATE_INVOICE,
                        "Failed to create Irembo invoice",
                        iremboMessage,
                        false,
                        null,
                        Collections.emptyList(),
                        null);
            }
            invoice = iremboPayResponse.getData();
            patientBill.setReferenceId(transactionId);
            patientBill.setInvoiceNumber(invoice.getInvoiceNumber());
            patientBill.setPaymentLinkUrl(invoice.getPaymentLinkUrl());
            patientBill.setInitiatedAt(new Date());
            patientBill.setPhoneNumber(phoneNumber);
            patientBill.setTransactionStatus("Pending");
            patientBill.setRetryCount(0);
            billingDAO.savePatientBill(patientBill);
            if (!hasText(invoice.getPaymentLinkUrl())) {
                IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                        "Irembo createInvoice succeeded but paymentLinkUrl is missing/empty, patientBillId="
                                + patientBill.getPatientBillId() + ", invoiceNumber=" + invoice.getInvoiceNumber());
            }
            log.info("Irembo single init createInvoice saved: patientBillId=" + patientBill.getPatientBillId()
                    + ", invoiceNumber=" + invoice.getInvoiceNumber()
                    + ", paymentLinkUrl=" + invoice.getPaymentLinkUrl()
                    + ", paymentStatus=" + invoice.getPaymentStatus());
        } else {
            log.info("Irembo init skipped createInvoice: PatientBill id=" + patientBill.getPatientBillId()
                    + " already has invoiceNumber=" + patientBill.getInvoiceNumber());
            invoice = new Invoice(iremboPaySecretKey, iremboEnv);
            invoice.setInvoiceNumber(patientBill.getInvoiceNumber());
        }

        String company = detectTelco(phoneNumber);
        String paymentPhoneNumber = resolveSandboxPhoneNumber(phoneNumber, company, isProduction);
        if (company.equalsIgnoreCase("Unknown operator")) {
            IremboPayLogUtil.logFailure(log, "INITIATE_PAYMENT",
                    "unknown mobile money operator, phoneNumber=" + phoneNumber + ", invoiceNumber="
                            + invoice.getInvoiceNumber() + ", patientBillId=" + patientBill.getPatientBillId());
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.INITIATE_PAYMENT,
                    "Unable to detect mobile money operator for phone number",
                    null,
                    false,
                    null,
                    Collections.singletonList(invoice.getInvoiceNumber()),
                    null);
        }

        String paymentUuid = UUID.randomUUID().toString();
        log.info("Irembo single init payment start: invoiceNumber=" + invoice.getInvoiceNumber()
                + ", provider=" + company
                + ", phoneNumber=" + paymentPhoneNumber
                + ", paymentUuid=" + paymentUuid);
        long initiateStartedAt = System.currentTimeMillis();
        IremboPayResponse<?> initiateResponse;
        try {
            initiateResponse = iremboPay.mobileMoney.initiate(paymentPhoneNumber, company,
                    invoice.getInvoiceNumber(), paymentUuid);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - initiateStartedAt;
            IremboPayLogUtil.logFailure(log, "INITIATE_PAYMENT",
                    "single-init MoMo exception after " + durationMs + "ms, invoiceNumber="
                            + invoice.getInvoiceNumber() + ", patientBillId=" + patientBill.getPatientBillId()
                            + ", paymentUuid=" + paymentUuid,
                    e);
            String rollbackDetail = null;
            if (invoiceCreatedInThisCall) {
                rollbackDetail = rollbackInvoiceMappings(Collections.singletonList(patientBill.getPatientBillId()));
            }
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.INITIATE_PAYMENT,
                    "Failed to initiate mobile money payment",
                    e.getMessage(),
                    invoiceCreatedInThisCall,
                    rollbackDetail,
                    Collections.singletonList(invoice.getInvoiceNumber()),
                    null);
        }
        logIremboApiCall("initiatePayment(single)", initiateStartedAt, initiateResponse);
        if (!isSuccessfulIremboResponseWithoutData(initiateResponse)) {
            String iremboMessage = formatIremboErrorMessage(initiateResponse);
            String rollbackDetail = null;
            if (invoiceCreatedInThisCall) {
                rollbackDetail = rollbackInvoiceMappings(Collections.singletonList(patientBill.getPatientBillId()));
            }
            IremboPayLogUtil.logFailure(log, "INITIATE_PAYMENT",
                    "single-init MoMo rejected, invoiceNumber=" + invoice.getInvoiceNumber()
                            + ", patientBillId=" + patientBill.getPatientBillId() + ", provider=" + company
                            + ", iremboMessage=" + iremboMessage
                            + (rollbackDetail != null ? ", rollbackDetail=" + rollbackDetail : ""));
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.INITIATE_PAYMENT,
                    "Irembo rejected mobile money payment initiation",
                    iremboMessage,
                    invoiceCreatedInThisCall,
                    rollbackDetail,
                    Collections.singletonList(invoice.getInvoiceNumber()),
                    null);
        }

        return IremboPayInitiationResult.success(Collections.singletonList(invoice.getInvoiceNumber()), null);
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
        if (iremboPayResponse == null) {
            IremboPayLogUtil.logFailure(log, "STATUS_CHECK",
                    "null response from getInvoice, invoiceId=" + invoiceId);
            return billingDAO.getPatientBillStatus(invoiceId);
        }
        Invoice invoice = iremboPayResponse.getData();
        if (invoice == null) {
            IremboPayLogUtil.logFailure(log, "STATUS_CHECK",
                    "null invoice data from getInvoice, invoiceId=" + invoiceId
                            + ", iremboMessage=" + formatIremboErrorMessage(iremboPayResponse));
            return billingDAO.getPatientBillStatus(invoiceId);
        }
        log.info("Irembo status check response: invoiceId=" + invoiceId
                + ", status=" + invoice.getPaymentStatus()
                + ", type=" + invoice.getType()
                + ", invoiceNumber=" + invoice.getInvoiceNumber()
                + ", batchNumber=" + invoice.getBatchNumber()
                + ", amount=" + invoice.getAmount()
                + ", paymentReference=" + invoice.getPaymentReference()
                + ", childInvoices=" + (invoice.getChildInvoices() == null ? 0 : invoice.getChildInvoices().size()));
        BillingService billingService = Context.getService(BillingService.class);
        if (invoice.getPaymentStatus() != null && invoice.getPaymentStatus().equalsIgnoreCase("PAID")) {
            if (isIremboBatchInvoice(invoice)) {
                String batchNumber = resolveStatusCheckBatchNumber(invoice, invoiceId);
                log.info("Irembo status check treating invoice as BATCH: invoiceId=" + invoiceId
                        + ", batchNumber=" + batchNumber
                        + ", childInvoices=" + invoice.getChildInvoices());
                processIrembopayBatchCallback(
                        batchNumber,
                        invoice.getChildInvoices(),
                        true,
                        invoice.getPaymentReference(),
                        formatPaidAt(invoice.getPaidAt()),
                        invoice.getPaymentStatus());
                List<PatientBill> paidBatchBills = billingDAO.getPatientBillsByBatchNumber(batchNumber);
                if (paidBatchBills != null && !paidBatchBills.isEmpty()) {
                    return paidBatchBills.get(0);
                }
                return billingDAO.getPatientBillStatus(invoiceId);
            }

            //Here we need to make sure update the patient bill if it was not yet marked as paid

            User iremboUser = resolveConfiguredIremboUser();

            PatientBill billToConfirm = billingDAO.getPatientBillStatusForUpdate(invoiceId);
            if (billToConfirm == null) {
                IremboPayLogUtil.logFailure(log, "STATUS_CHECK",
                        "no PatientBill found for PAID invoice, invoiceId=" + invoiceId);
                return null;
            }
            log.info("Irembo PAID pre-check: invoiceId=" + invoiceId
                    + ", patientBillId=" + billToConfirm.getPatientBillId()
                    + ", currentBillIsPaid=" + billToConfirm.getIsPaid()
                    + ", currentPaymentConfirmed=" + billToConfirm.isPaymentConfirmed()
                    + ", currentTransactionStatus=" + billToConfirm.getTransactionStatus()
                    + ", currentBillPaymentReference=" + billToConfirm.getPaymentReference());

            //Get the invoice amount
            BigDecimal invoiceAmount = toIremboPayComparableAmount(invoice.getAmount());
            BigDecimal billAmount = toIremboPayComparableAmount(billToConfirm.getAmount());
            log.info("Irembo PAID amount compare: invoiceId=" + invoiceId
                    + ", iremboAmount=" + invoiceAmount
                    + ", billAmount=" + billAmount);

            if (iremboPaidAmountCoversBill(invoiceAmount, billAmount)) {
                String resolvedPaymentReference = resolveIremboPaymentReference(invoice.getPaymentReference(),
                        billToConfirm.getPaymentReference(), invoiceId);
                if (resolvedPaymentReference == null) {
                    IremboPayLogUtil.logFailure(log, "STATUS_CHECK",
                            "missing payment reference for PAID invoice, invoiceId=" + invoiceId
                                    + ", patientBillId=" + billToConfirm.getPatientBillId()
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

                                if (!Boolean.TRUE.equals(psb.getVoided())) {
                                    psb.setPaid(true);
                                    psb.setPaidQuantity(psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO);
                                    ConsommationUtil.createPatientServiceBill(psb);
                                    paidItemsCreated++;
                                }
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
                IremboPayLogUtil.logFailure(log, "STATUS_CHECK",
                        "PAID invoice amount mismatch, invoiceId=" + invoiceId
                                + ", patientBillId=" + billToConfirm.getPatientBillId()
                                + ", iremboAmount=" + invoiceAmount + ", billAmount=" + billAmount
                                + " (paid amount must be >= bill amount)"
                                + ", iremboPaymentReference=" + invoice.getPaymentReference()
                                + ", billPaymentReference=" + billToConfirm.getPaymentReference());
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
            IremboPayLogUtil.logFailure(log, "CALLBACK",
                    "no PatientBill found for callback invoiceNumber=" + invoiceNumber);
            throw new IllegalArgumentException("No PatientBill found for invoice number " + invoiceNumber);
        }
        if (!Boolean.TRUE.equals(success)) {
            IremboPayLogUtil.logFailure(log, "CALLBACK",
                    "callback reported success=false, invoiceNumber=" + invoiceNumber
                            + ", patientBillId=" + billToConfirm.getPatientBillId()
                            + ", paymentStatus=" + paymentStatus);
            return;
        }
        if (callbackAmount == null) {
            IremboPayLogUtil.logFailure(log, "CALLBACK",
                    "callback amount missing, invoiceNumber=" + invoiceNumber
                            + ", patientBillId=" + billToConfirm.getPatientBillId());
            throw new IllegalArgumentException("Callback amount is missing for invoice " + invoiceNumber);
        }
        if (!iremboPaidAmountCoversBill(callbackAmount, billToConfirm.getAmount())) {
            IremboPayLogUtil.logFailure(log, "CALLBACK",
                    "callback amount mismatch, invoiceNumber=" + invoiceNumber
                            + ", patientBillId=" + billToConfirm.getPatientBillId()
                            + ", callbackAmount=" + callbackAmount + ", billAmount=" + billToConfirm.getAmount()
                            + " (paid amount must be >= bill amount)");
            throw new IllegalArgumentException(String.format(
                "Amount mismatch: callback amount=%s is less than bill amount=%s for invoice %s",
                callbackAmount, billToConfirm.getAmount(), invoiceNumber));
        }
        String resolvedPaymentReference = resolveIremboPaymentReference(paymentReference,
                billToConfirm.getPaymentReference(), invoiceNumber);
        if (resolvedPaymentReference == null) {
            IremboPayLogUtil.logFailure(log, "CALLBACK",
                    "payment reference missing, invoiceNumber=" + invoiceNumber
                            + ", patientBillId=" + billToConfirm.getPatientBillId()
                            + ", incomingPaymentReference=" + paymentReference);
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
            IremboPayLogUtil.logFailure(log, "BATCH_CALLBACK",
                    "batch callback reported success=false, batchNumber=" + batchNumber
                            + ", paymentStatus=" + paymentStatus
                            + ", childInvoicesFromPayload=" + (childInvoices == null ? 0 : childInvoices.size()));
            return;
        }
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            IremboPayLogUtil.logFailure(log, "BATCH_CALLBACK", "batch number missing in callback payload");
            throw new IllegalArgumentException("Batch number is missing for batch callback");
        }
        String resolvedPaymentReference = resolveIremboPaymentReference(paymentReference, null, batchNumber);
        if (resolvedPaymentReference == null) {
            IremboPayLogUtil.logFailure(log, "BATCH_CALLBACK",
                    "payment reference missing, batchNumber=" + batchNumber
                            + ", incomingPaymentReference=" + paymentReference);
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
            IremboPayLogUtil.logFailure(log, "BATCH_CALLBACK",
                    "no child invoices resolved for batch callback, batchNumber=" + batchNumber);
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
                IremboPayLogUtil.logFailure(log, "BATCH_CALLBACK",
                        "no PatientBill for child invoice, batchNumber=" + batchNumber
                                + ", childInvoice=" + childInvoice);
                skipped++;
                continue;
            }
            if (billToConfirm.getBatchNumber() != null && !batchNumber.equals(billToConfirm.getBatchNumber())) {
                IremboPayLogUtil.logFailure(log, "BATCH_CALLBACK",
                        "child invoice belongs to different batch, batchNumber=" + batchNumber
                                + ", childInvoice=" + childInvoice
                                + ", billBatchNumber=" + billToConfirm.getBatchNumber());
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

            // Always attribute Irembo Pay confirmations to the configured Irembo user,
            // not the authenticated session user (callback/daemon/cashier context).
            User iremboUser = resolveConfiguredIremboUser();
            billToConfirm.setIsPaid(true);
            billToConfirm.setPaymentReference(resolvedPaymentReference);
            billToConfirm.setPaymentConfirmed(true);
            billToConfirm.setPaymentConfirmedBy(iremboUser);
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
            billPayment.setCollector(iremboUser);
            billPayment.setCreator(iremboUser);
            billPayment.setCreatedDate(new Date());
            billPayment.setVoided(false);
            billPayment.setPaymentReference(resolvedPaymentReference);
            billPayment.setInvoiceNumber(billToConfirm.getInvoiceNumber());
            billingDAO.getPatientServiceBill(billPayment);

            Consommation affectedConsommation = billingDAO.getConsommationByPatientBill(billToConfirm);
            if (affectedConsommation != null && affectedConsommation.getBillItems() != null) {
                Set<PatientServiceBill> billItems = affectedConsommation.getBillItems();
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
                    billingDAO.savePaidServiceBill(paidSb);

                    if (!Boolean.TRUE.equals(psb.getVoided())) {
                        psb.setPaid(true);
                        psb.setPaidQuantity(psb.getQuantity() != null ? psb.getQuantity() : BigDecimal.ZERO);
                        ConsommationUtil.createPatientServiceBill(psb);
                    }
                }
            }
            return true;
        }
    }

    private static boolean isIremboBatchInvoice(Invoice invoice) {
        if (invoice == null) {
            return false;
        }
        if ("BATCH".equalsIgnoreCase(invoice.getType())) {
            return true;
        }
        return invoice.getChildInvoices() != null && !invoice.getChildInvoices().isEmpty();
    }

    private static String resolveStatusCheckBatchNumber(Invoice invoice, String invoiceId) {
        if (hasText(invoice.getBatchNumber())) {
            return invoice.getBatchNumber().trim();
        }
        if (hasText(invoice.getInvoiceNumber()) && "BATCH".equalsIgnoreCase(invoice.getType())) {
            return invoice.getInvoiceNumber().trim();
        }
        return invoiceId;
    }

    private static String formatPaidAt(Date paidAt) {
        if (paidAt == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(paidAt);
    }

    /**
     * Resolves the OpenMRS user configured for Irembo Pay attribution
     * ({@link BillingConstants#BLOBAL_PROPERTY_IREMBO_USER}).
     */
    private User resolveConfiguredIremboUser() {
        String username = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_USER);
        if (!hasText(username)) {
            IremboPayLogUtil.logFailure(log, "IREMBO_USER",
                    "global property " + BillingConstants.BLOBAL_PROPERTY_IREMBO_USER
                            + " is missing/empty; Irembo confirmation may not attribute payments correctly");
            return null;
        }
        User iremboUser = Context.getService(UserService.class).getUserByUsername(username.trim());
        if (iremboUser == null) {
            IremboPayLogUtil.logFailure(log, "IREMBO_USER",
                    "configured Irembo user not found: username=" + username.trim());
        }
        return iremboUser;
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
        IremboInvoiceResult result = ensureIremboInvoice(patient, patientBill, phoneNumber);
        if (!result.isSuccess()) {
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                    "createIremboInvoice failed, patientBillId=" + patientBill.getPatientBillId()
                            + ", message=" + result.getUserFacingMessage());
            throw new DAOException(result.getUserFacingMessage());
        }
        return result.getInvoiceNumber();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public IremboInvoiceResult ensureIremboInvoice(Patient patient, PatientBill patientBill, String phoneNumber)
            throws DAOException {
        patientBill = refreshPatientBill(patientBill);
        if (hasText(patientBill.getInvoiceNumber())) {
            log.info("Irembo createInvoice skipped: PatientBill id=" + patientBill.getPatientBillId()
                    + " already has invoiceNumber=" + patientBill.getInvoiceNumber());
            return IremboInvoiceResult.success(patientBill.getInvoiceNumber().trim(), false,
                    patientBill.getPatientBillId());
        }

        Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT)
                .equalsIgnoreCase("production");
        String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);
        String iremboPayProductCode = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_PRODUCT_CODE);

        String transactionId = UUID.randomUUID().toString();
        Customer customer = new Customer();
        customer.setFullName(patient.getPersonName().getFullName());
        customer.setPhoneNumber(phoneNumber);

        List<PaymentItem> paymentItems = new ArrayList<>();
        PaymentItem paymentItem = new PaymentItem();
        paymentItem.setCode(iremboPayProductCode);
        paymentItem.setQuantity(1);
        paymentItem.setUnitAmount(toIremboPayUnitAmount(patientBill.getAmount()));
        paymentItems.add(paymentItem);

        Department myDepartment = billingDAO.getConsommationByPatientBill(patientBill).getDepartment();
        String invoiceDescription = myDepartment.getName();
        String iremboPayAccountIdentifier = myDepartment.getAccountIdentifier();
        if (iremboPayAccountIdentifier == null) {
            iremboPayAccountIdentifier = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_ACCOUNT_IDENTIFIER);
        }

        Environment iremboEnv = isProduction ? Environment.PRODUCTION : Environment.SANDBOX;
        IremboPay iremboPay = new IremboPay(iremboPaySecretKey, iremboEnv);

        log.info("Irembo createInvoice start: patientBillId=" + patientBill.getPatientBillId()
                + ", transactionId=" + transactionId
                + ", amount=" + patientBill.getAmount()
                + ", phoneNumber=" + phoneNumber
                + ", accountIdentifier=" + iremboPayAccountIdentifier);
        long createStartedAt = System.currentTimeMillis();
        IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.createInvoice(transactionId,
                iremboPayAccountIdentifier, customer, paymentItems, invoiceDescription, null, "EN");
        logIremboApiCall("createInvoice(child)", createStartedAt, iremboPayResponse);
        if (!isSuccessfulIremboResponse(iremboPayResponse)) {
            String iremboMessage = formatIremboErrorMessage(iremboPayResponse);
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                    "child createInvoice failed, patientBillId=" + patientBill.getPatientBillId()
                            + ", transactionId=" + transactionId + ", iremboMessage=" + iremboMessage);
            return IremboInvoiceResult.failure(patientBill.getPatientBillId(),
                    "Failed to create Irembo invoice for bill " + patientBill.getPatientBillId(), iremboMessage);
        }

        Invoice invoice = iremboPayResponse.getData();
        patientBill.setReferenceId(transactionId);
        patientBill.setInvoiceNumber(invoice.getInvoiceNumber());
        patientBill.setPaymentLinkUrl(invoice.getPaymentLinkUrl());
        patientBill.setInitiatedAt(new Date());
        patientBill.setPhoneNumber(phoneNumber);
        patientBill.setTransactionStatus("Pending");
        patientBill.setRetryCount(0);
        billingDAO.savePatientBill(patientBill);
        if (!hasText(invoice.getPaymentLinkUrl())) {
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                    "Irembo createInvoice(child) succeeded but paymentLinkUrl is missing/empty, patientBillId="
                            + patientBill.getPatientBillId() + ", invoiceNumber=" + invoice.getInvoiceNumber());
        }
        log.info("Irembo createInvoice saved: patientBillId=" + patientBill.getPatientBillId()
                + ", invoiceNumber=" + invoice.getInvoiceNumber()
                + ", paymentLinkUrl=" + invoice.getPaymentLinkUrl()
                + ", paymentStatus=" + invoice.getPaymentStatus());
        return IremboInvoiceResult.success(invoice.getInvoiceNumber(), true, patientBill.getPatientBillId());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static double toIremboPayUnitAmount(BigDecimal amount) {
        if (amount == null) {
            return 0.0;
        }
        return amount.doubleValue();
    }

    private static BigDecimal toIremboPayComparableAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private static BigDecimal toIremboPayComparableAmount(double amount) {
        return BigDecimal.valueOf(amount);
    }

    /**
     * Accepts Irembo paid amount when it is greater than or equal to the local bill amount.
     * Exact equality still passes; amounts that were previously ceiling-rounded on Irembo
     * (paid slightly more than the decimal bill) also pass so old invoices can confirm.
     */
    private static boolean iremboPaidAmountCoversBill(BigDecimal paidAmount, BigDecimal billAmount) {
        if (paidAmount == null || billAmount == null) {
            return false;
        }
        return toIremboPayComparableAmount(paidAmount)
                .compareTo(toIremboPayComparableAmount(billAmount)) >= 0;
    }

    /**
     * Compact log line for an Irembo API call: operation, duration, success/message, and response body.
     */
    private void logIremboApiCall(String operation, long startedAtMs, Object response) {
        long durationMs = System.currentTimeMillis() - startedAtMs;
        if (response == null) {
            IremboPayLogUtil.logFailure(log, "API_" + operation,
                    "completed in " + durationMs + "ms with null response");
            return;
        }
        if (response instanceof IremboPayResponse) {
            IremboPayResponse<?> iremboResponse = (IremboPayResponse<?>) response;
            String message = "completed in " + durationMs + "ms"
                    + ", success=" + iremboResponse.isSuccess()
                    + ", message=" + iremboResponse.getMessage()
                    + ", response=" + iremboResponse;
            if (Boolean.TRUE.equals(iremboResponse.isSuccess())) {
                log.info("Irembo " + operation + " " + message);
            } else {
                IremboPayLogUtil.logFailure(log, "API_" + operation, message);
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
                IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                        "invoices reference different batch numbers (" + resolvedBatchNumber + " vs "
                                + trimmedBatch + ") for invoice " + invoiceNumber.trim());
            }
        }
        return resolvedBatchNumber;
    }

    /**
     * Sets the given batchNumber on every PatientBill found for the supplied invoice numbers,
     * skipping bills that are missing. When paymentLinkUrl is provided, it overwrites any
     * existing individual invoice payment link with the batch checkout link.
     * Returns the number of bills that were updated.
     */
    private int applyBatchNumberToBills(String batchNumber, List<String> invoiceNumbers, String paymentLinkUrl)
            throws DAOException {
        if (!hasText(batchNumber) || invoiceNumbers == null || invoiceNumbers.isEmpty()) {
            return 0;
        }
        String trimmedBatch = batchNumber.trim();
        String trimmedPaymentLink = hasText(paymentLinkUrl) ? paymentLinkUrl.trim() : null;
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
                IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                        "batch mapping skipped: no PatientBill found for invoice " + key
                                + ", batchNumber=" + trimmedBatch);
                continue;
            }
            boolean needsSave = false;
            if (!trimmedBatch.equals(patientBill.getBatchNumber())) {
                patientBill.setBatchNumber(trimmedBatch);
                needsSave = true;
            }
            if (trimmedPaymentLink != null && !trimmedPaymentLink.equals(patientBill.getPaymentLinkUrl())) {
                // Batch payment link replaces any individual invoice payment link.
                patientBill.setPaymentLinkUrl(trimmedPaymentLink);
                needsSave = true;
            }
            if (!needsSave) {
                continue;
            }
            billingDAO.savePatientBill(patientBill);
            updated++;
        }
        return updated;
    }

    private int applyBatchNumberToBills(String batchNumber, List<String> invoiceNumbers) throws DAOException {
        return applyBatchNumberToBills(batchNumber, invoiceNumbers, null);
    }

    public boolean canWeInitiateBatch(List<String> invoices) throws DAOException {
        if (invoices == null || invoices.isEmpty()) {
            IremboPayLogUtil.logFailure(log, "CREATE_BATCH", "batch eligibility check: no invoices provided");
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
    public void initIremboPayBatch(Patient patient, List<String> invoices, String phoneNumber) throws DAOException {
        initIremboPayBatchWithResult(patient, invoices, phoneNumber, Collections.emptyList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public IremboPayInitiationResult initIremboPayBatchWithResult(Patient patient, List<String> invoices,
            String phoneNumber, List<Integer> newlyInvoicedBillIds) throws DAOException {
        String batchDescription = patient.getPersonName().getFullName() + " medical service payments";
        String transactionId = UUID.randomUUID().toString();

        Boolean isProduction = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_ENVIRONMENT)
                .equalsIgnoreCase("production");
        String iremboPaySecretKey = ConfigUtil.getGlobalProperty(BillingConstants.BLOBAL_PROPERTY_IREMBO_PAY_SECRET);
        Environment iremboEnv = isProduction ? Environment.PRODUCTION : Environment.SANDBOX;
        IremboPay iremboPay = new IremboPay(iremboPaySecretKey, iremboEnv);

        List<Integer> billIdsToRollback = newlyInvoicedBillIds == null
                ? Collections.emptyList() : newlyInvoicedBillIds;
        String batchNumber = null;
        boolean batchCreatedInThisCall = false;
        List<String> invoicesMappedToBatch = new ArrayList<>();

        if (canWeInitiateBatch(invoices)) {
            log.info("Irembo createBatchInvoice start: transactionId=" + transactionId
                    + ", requestedInvoiceCount=" + (invoices == null ? 0 : invoices.size())
                    + ", invoices=" + invoices
                    + ", phoneNumber=" + phoneNumber);
            long batchCreateStartedAt = System.currentTimeMillis();
            IremboPayResponse<Invoice> iremboPayResponse = iremboPay.invoice.createBatchInvoice(invoices, transactionId,
                    batchDescription);
            logIremboApiCall("createBatchInvoice", batchCreateStartedAt, iremboPayResponse);
            if (!isSuccessfulIremboResponse(iremboPayResponse)) {
                String iremboMessage = formatIremboErrorMessage(iremboPayResponse);
                String rollbackDetail = rollbackInvoiceMappings(billIdsToRollback);
                IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                        "createBatchInvoice failed, transactionId=" + transactionId
                                + ", invoiceCount=" + (invoices == null ? 0 : invoices.size())
                                + ", iremboMessage=" + iremboMessage
                                + (rollbackDetail != null ? ", rollbackDetail=" + rollbackDetail : ""));
                return IremboPayInitiationResult.failure(
                        IremboPayInitiationResult.FailedStep.CREATE_BATCH,
                        "Failed to create Irembo batch invoice",
                        iremboMessage,
                        !billIdsToRollback.isEmpty(),
                        rollbackDetail,
                        invoices,
                        null);
            }

            Invoice batchInvoice = iremboPayResponse.getData();
            batchNumber = batchInvoice.getBatchNumber();
            if (!hasText(batchNumber) && "BATCH".equalsIgnoreCase(batchInvoice.getType())) {
                batchNumber = batchInvoice.getInvoiceNumber();
                log.warn("Irembo batch response missing batchNumber; falling back to invoiceNumber="
                        + batchNumber + ", transactionId=" + transactionId);
            }
            if (!hasText(batchNumber)) {
                String rollbackDetail = rollbackInvoiceMappings(billIdsToRollback);
                IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                        "batch response missing batchNumber, transactionId=" + transactionId
                                + ", responseInvoiceNumber=" + batchInvoice.getInvoiceNumber()
                                + (rollbackDetail != null ? ", rollbackDetail=" + rollbackDetail : ""));
                return IremboPayInitiationResult.failure(
                        IremboPayInitiationResult.FailedStep.CREATE_BATCH,
                        "Irembo batch response did not include a batch number",
                        formatIremboErrorMessage(iremboPayResponse),
                        !billIdsToRollback.isEmpty(),
                        rollbackDetail,
                        invoices,
                        null);
            }

            batchCreatedInThisCall = true;
            List<String> childInvoices = batchInvoice.getChildInvoices();
            if (childInvoices == null || childInvoices.isEmpty()) {
                log.warn("Irembo batch invoice response has no childInvoices; mapping requested invoices instead: transactionId="
                        + transactionId + ", batchNumber=" + batchNumber);
            }

            List<String> invoicesToMap = new ArrayList<>();
            if (invoices != null) {
                invoicesToMap.addAll(invoices);
            }
            if (childInvoices != null) {
                invoicesToMap.addAll(childInvoices);
            }
            invoicesMappedToBatch.addAll(invoicesToMap);
            if (!hasText(batchInvoice.getPaymentLinkUrl())) {
                IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                        "Irembo createBatchInvoice succeeded but paymentLinkUrl is missing/empty, batchNumber="
                                + batchNumber + ", transactionId=" + transactionId);
            }
            int updatedBills = applyBatchNumberToBills(batchNumber, invoicesToMap, batchInvoice.getPaymentLinkUrl());
            log.info("Irembo batch mapping complete: transactionId=" + transactionId
                    + ", batchNumber=" + batchNumber
                    + ", paymentLinkUrl=" + batchInvoice.getPaymentLinkUrl()
                    + ", type=" + batchInvoice.getType()
                    + ", paymentStatus=" + batchInvoice.getPaymentStatus()
                    + ", amount=" + batchInvoice.getAmount()
                    + ", childInvoicesCount=" + (childInvoices == null ? 0 : childInvoices.size())
                    + ", requestedInvoiceCount=" + (invoices == null ? 0 : invoices.size())
                    + ", updatedBills=" + updatedBills);
        } else {
            batchNumber = resolveExistingBatchNumber(invoices);
            if (!hasText(batchNumber)) {
                IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                        "cannot create or reuse batch, invoices=" + invoices);
                return IremboPayInitiationResult.failure(
                        IremboPayInitiationResult.FailedStep.CREATE_BATCH,
                        "Batch invoice already exists or cannot be created for the selected bills",
                        null,
                        false,
                        null,
                        invoices,
                        null);
            }
            if (invoices != null) {
                invoicesMappedToBatch.addAll(invoices);
            }
            int backfilledBills = applyBatchNumberToBills(batchNumber, invoices);
            log.info("Irembo batch creation skipped; reusing existing batchNumber=" + batchNumber
                    + ", backfilledBills=" + backfilledBills);
        }

        if (!hasText(batchNumber)) {
            IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                    "batchNumber missing before payment initiation, transactionId=" + transactionId
                            + ", invoices=" + invoices);
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.CREATE_BATCH,
                    "Batch number is missing",
                    null,
                    false,
                    null,
                    invoices,
                    null);
        }

        List<PatientBill> billsForBatch = billingDAO.getPatientBillsByBatchNumber(batchNumber);
        if (billsForBatch == null || billsForBatch.isEmpty()) {
            String rollbackDetail = batchCreatedInThisCall
                    ? rollbackBatchMappings(batchNumber, invoicesMappedToBatch) : null;
            if (batchCreatedInThisCall) {
                rollbackDetail = appendRollbackDetail(rollbackDetail, rollbackInvoiceMappings(billIdsToRollback));
            }
            IremboPayLogUtil.logFailure(log, "CREATE_BATCH",
                    "no PatientBill mapped to batchNumber=" + batchNumber + ", transactionId=" + transactionId
                            + (rollbackDetail != null ? ", rollbackDetail=" + rollbackDetail : ""));
            throw new DAOException("Irembo batch aborted: no PatientBill mapped to batchNumber " + batchNumber
                    + " for transactionId=" + transactionId + "; rollback=" + rollbackDetail);
        }

        String company = detectTelco(phoneNumber);
        String paymentPhoneNumber = resolveSandboxPhoneNumber(phoneNumber, company, isProduction);
        if (company.equalsIgnoreCase("Unknown operator")) {
            IremboPayLogUtil.logFailure(log, "INITIATE_PAYMENT",
                    "unknown mobile money operator for batch, phoneNumber=" + phoneNumber
                            + ", batchNumber=" + batchNumber + ", transactionId=" + transactionId);
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.INITIATE_PAYMENT,
                    "Unable to detect mobile money operator for phone number",
                    null,
                    false,
                    null,
                    invoices,
                    batchNumber);
        }

        String paymentUuid = UUID.randomUUID().toString();
        log.info("Irembo batch payment start: batchNumber=" + batchNumber
                + ", provider=" + company
                + ", phoneNumber=" + paymentPhoneNumber
                + ", paymentUuid=" + paymentUuid
                + ", mappedBillCount=" + billsForBatch.size());
        long initiateStartedAt = System.currentTimeMillis();
        IremboPayResponse<?> initiateResponse;
        try {
            initiateResponse = iremboPay.mobileMoney.initiate(paymentPhoneNumber, company, batchNumber, paymentUuid);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - initiateStartedAt;
            IremboPayLogUtil.logFailure(log, "INITIATE_PAYMENT",
                    "batch MoMo exception after " + durationMs + "ms, batchNumber=" + batchNumber
                            + ", transactionId=" + transactionId + ", paymentUuid=" + paymentUuid
                            + " (batch mapping preserved for retry)",
                    e);
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.INITIATE_PAYMENT,
                    "Failed to initiate mobile money payment for batch",
                    e.getMessage(),
                    false,
                    null,
                    invoices,
                    batchNumber);
        }
        logIremboApiCall("initiatePayment(batch)", initiateStartedAt, initiateResponse);
        if (!isSuccessfulIremboResponseWithoutData(initiateResponse)) {
            String iremboMessage = formatIremboErrorMessage(initiateResponse);
            IremboPayLogUtil.logFailure(log, "INITIATE_PAYMENT",
                    "batch MoMo rejected, batchNumber=" + batchNumber + ", transactionId=" + transactionId
                            + ", iremboMessage=" + iremboMessage + " (batch mapping preserved for retry)");
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.INITIATE_PAYMENT,
                    "Irembo rejected mobile money payment initiation for batch",
                    iremboMessage,
                    false,
                    null,
                    invoices,
                    batchNumber);
        }

        return IremboPayInitiationResult.success(invoices, batchNumber);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public IremboPayInitiationResult initIremboPayBatchForBillIds(List<Integer> billIds, String phoneNumber)
            throws DAOException {
        if (billIds == null || billIds.isEmpty()) {
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE", "batch-for-bill-ids called with no bill IDs");
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.CREATE_INVOICE,
                    "No bill IDs were provided",
                    null,
                    false,
                    null,
                    Collections.emptyList(),
                    null);
        }
        if (!hasText(phoneNumber)) {
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                    "batch-for-bill-ids called without phone number, billIds=" + billIds);
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.CREATE_INVOICE,
                    "Phone number is required",
                    null,
                    false,
                    null,
                    Collections.emptyList(),
                    null);
        }

        List<String> invoices = new ArrayList<>();
        List<Integer> newlyInvoicedBillIds = new ArrayList<>();
        Patient patient = null;
        List<Integer> failedBillIds = new ArrayList<>();
        String lastInvoiceFailureMessage = null;
        String lastInvoiceIremboMessage = null;

        for (Integer billId : billIds) {
            if (billId == null) {
                continue;
            }
            PatientBill patientBill = billingDAO.getPatientBill(billId);
            if (patientBill == null || Boolean.TRUE.equals(patientBill.getIsPaid()) || patientBill.isPaymentConfirmed()) {
                IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                        "bill skipped for batch invoicing, billId=" + billId
                                + ", found=" + (patientBill != null)
                                + ", paid=" + (patientBill != null && Boolean.TRUE.equals(patientBill.getIsPaid()))
                                + ", paymentConfirmed=" + (patientBill != null && patientBill.isPaymentConfirmed()));
                failedBillIds.add(billId);
                continue;
            }
            Consommation consommation = billingDAO.getConsommationByPatientBill(patientBill);
            if (consommation == null || consommation.getBeneficiary() == null
                    || consommation.getBeneficiary().getPatient() == null) {
                IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                        "bill skipped for batch invoicing due to missing patient context, billId=" + billId);
                failedBillIds.add(billId);
                continue;
            }
            if (patient == null) {
                patient = consommation.getBeneficiary().getPatient();
            }
            IremboInvoiceResult invoiceResult = ensureIremboInvoice(patient, patientBill, phoneNumber.trim());
            if (!invoiceResult.isSuccess() || !hasText(invoiceResult.getInvoiceNumber())) {
                IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                        "child invoice creation failed during batch-for-bill-ids, billId=" + billId
                                + ", message=" + invoiceResult.getMessage()
                                + ", iremboMessage=" + invoiceResult.getIremboMessage());
                failedBillIds.add(billId);
                lastInvoiceFailureMessage = invoiceResult.getMessage();
                lastInvoiceIremboMessage = invoiceResult.getIremboMessage();
                if (IremboPayNetworkUtil.isNetworkFailureMessage(invoiceResult.getIremboMessage())) {
                    IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                            "stopping batch invoicing after network failure; remaining bills will not be attempted");
                    break;
                }
                continue;
            }
            invoices.add(invoiceResult.getInvoiceNumber());
            if (invoiceResult.isNewlyCreated() && invoiceResult.getPatientBillId() != null) {
                newlyInvoicedBillIds.add(invoiceResult.getPatientBillId());
            }
        }

        if (invoices.isEmpty()) {
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                    "no bills could be invoiced for batch payment, requestedBillIds=" + billIds
                            + ", failedBillIds=" + failedBillIds
                            + ", lastIremboMessage="
                            + (lastInvoiceIremboMessage != null ? lastInvoiceIremboMessage : lastInvoiceFailureMessage));
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.CREATE_INVOICE,
                    "No bills could be submitted for batch payment",
                    lastInvoiceIremboMessage != null ? lastInvoiceIremboMessage : lastInvoiceFailureMessage,
                    false,
                    null,
                    Collections.emptyList(),
                    null);
        }

        if (patient == null) {
            String rollbackDetail = rollbackInvoiceMappings(newlyInvoicedBillIds);
            IremboPayLogUtil.logFailure(log, "CREATE_INVOICE",
                    "patient context missing after batch invoicing, invoices=" + invoices
                            + (rollbackDetail != null ? ", rollbackDetail=" + rollbackDetail : ""));
            return IremboPayInitiationResult.failure(
                    IremboPayInitiationResult.FailedStep.CREATE_INVOICE,
                    "Patient context missing for batch payment",
                    null,
                    rollbackDetail != null,
                    rollbackDetail,
                    invoices,
                    null);
        }

        return initIremboPayBatchWithResult(patient, invoices, phoneNumber.trim(), newlyInvoicedBillIds);
    }

    private static boolean isSuccessfulIremboResponse(IremboPayResponse<?> response) {
        return response != null && Boolean.TRUE.equals(response.isSuccess()) && response.getData() != null;
    }

    private static boolean isSuccessfulIremboResponseWithoutData(IremboPayResponse<?> response) {
        return response != null && Boolean.TRUE.equals(response.isSuccess());
    }

    private static String formatIremboErrorMessage(IremboPayResponse<?> response) {
        if (response == null) {
            return "No response from Irembo Pay";
        }
        if (response.getMessage() != null && !response.getMessage().trim().isEmpty()) {
            return response.getMessage().trim();
        }
        StringBuilder sb = new StringBuilder();
        if (response.getErrors() != null) {
            for (IremboPayResponse.Error error : response.getErrors()) {
                if (error == null) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                if (error.code != null) {
                    sb.append(error.code);
                }
                if (error.detail != null) {
                    if (error.code != null) {
                        sb.append(": ");
                    }
                    sb.append(error.detail);
                }
            }
        }
        if (sb.length() == 0) {
            return "Irembo Pay request failed";
        }
        return sb.toString();
    }

    private String resolveSandboxPhoneNumber(String phoneNumber, String company, boolean isProduction) {
        if (isProduction) {
            return phoneNumber;
        }
        if (company.equalsIgnoreCase("AIRTEL")) {
            return "0731234567";
        }
        if (company.equalsIgnoreCase("MTN")) {
            return "0781234567";
        }
        return phoneNumber;
    }

    private String rollbackInvoiceMappings(List<Integer> patientBillIds) throws DAOException {
        if (patientBillIds == null || patientBillIds.isEmpty()) {
            return null;
        }
        int rolledBack = 0;
        for (Integer patientBillId : patientBillIds) {
            if (patientBillId == null) {
                continue;
            }
            PatientBill bill = billingDAO.getPatientBill(patientBillId);
            if (bill == null || Boolean.TRUE.equals(bill.getIsPaid()) || bill.isPaymentConfirmed()) {
                continue;
            }
            if (!hasText(bill.getInvoiceNumber())) {
                continue;
            }
            clearIremboInvoiceFields(bill);
            billingDAO.savePatientBill(bill);
            rolledBack++;
        }
        return rolledBack > 0 ? ("rolled back invoice mapping on " + rolledBack + " bill(s)") : null;
    }

    private String rollbackBatchMappings(String batchNumber, List<String> invoiceNumbers) throws DAOException {
        if (!hasText(batchNumber) || invoiceNumbers == null || invoiceNumbers.isEmpty()) {
            return null;
        }
        int rolledBack = 0;
        Set<String> processed = new LinkedHashSet<>();
        for (String invoiceNumber : invoiceNumbers) {
            if (!hasText(invoiceNumber) || !processed.add(invoiceNumber.trim())) {
                continue;
            }
            PatientBill bill = billingDAO.getPatientBillStatus(invoiceNumber.trim());
            if (bill == null || Boolean.TRUE.equals(bill.getIsPaid()) || bill.isPaymentConfirmed()) {
                continue;
            }
            if (!batchNumber.equals(bill.getBatchNumber())) {
                continue;
            }
            bill.setBatchNumber(null);
            billingDAO.savePatientBill(bill);
            rolledBack++;
        }
        return rolledBack > 0 ? ("rolled back batch mapping on " + rolledBack + " bill(s)") : null;
    }

    private static String appendRollbackDetail(String first, String second) {
        if (first == null || first.trim().isEmpty()) {
            return second;
        }
        if (second == null || second.trim().isEmpty()) {
            return first;
        }
        return first + "; " + second;
    }

    private static void clearIremboInvoiceFields(PatientBill bill) {
        bill.setInvoiceNumber(null);
        bill.setReferenceId(null);
        bill.setInitiatedAt(null);
        bill.setTransactionStatus(null);
        bill.setRetryCount(null);
        bill.setBatchNumber(null);
        bill.setPaymentLinkUrl(null);
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

	@Override
	@Transactional
	public Consommation createAmbulanceBill(String insurancePolicyNumber, int kilometers, String description) {
		if (insurancePolicyNumber == null || insurancePolicyNumber.trim().isEmpty()) {
			throw new APIException("Insurance policy number is required");
		}
		if (kilometers <= 0) {
			throw new APIException("Number of kilometers must be greater than zero");
		}
		if (description == null || description.trim().isEmpty()) {
			throw new APIException("Ambulance bill description is required");
		}

		String policyNumber = insurancePolicyNumber.trim();
		String billDescription = description.trim();

		InsurancePolicy insurancePolicy = getInsurancePolicyByCardNo(policyNumber);
		if (insurancePolicy == null) {
			throw new APIException("Insurance policy not found for card number: " + policyNumber);
		}

		Beneficiary beneficiary = getBeneficiaryByPolicyNumber(policyNumber);
		if (beneficiary == null) {
			throw new APIException("Beneficiary not found for policy number: " + policyNumber);
		}

		GlobalBill globalBill = getOpenGlobalBillByInsuranceCardNo(insurancePolicy.getInsuranceCardNo());
		if (globalBill == null) {
			throw new APIException("No open global bill found for insurance policy: " + policyNumber);
		}

		Department department = resolveAmbulanceDepartment();
		Insurance insurance = insurancePolicy.getInsurance();
		BillableService billableService = resolveAmbulanceBillableService(insurance);
		HopService hopService = HopServiceUtil.getServiceByName(billableService.getServiceCategory().getName());

		BigDecimal quantity = BigDecimal.valueOf(kilometers);
		BigDecimal unitPrice = billableService.getMaximaToPay();
		if (unitPrice == null) {
			throw new APIException("Ambulance billable service has no unit price configured for insurance: "
					+ insurance.getName());
		}

		BigDecimal totalAmount = quantity.multiply(unitPrice);
		User creator = Context.getAuthenticatedUser();
		Date now = new Date();

		Integer itemType = null;
		if (billableService.getFacilityServicePrice() != null
				&& billableService.getFacilityServicePrice().getItemType() != null) {
			itemType = billableService.getFacilityServicePrice().getItemType().intValue();
		}

		PatientServiceBill patientServiceBill = new PatientServiceBill(
				billableService, hopService, now, unitPrice, quantity, creator, now, null, itemType);
		patientServiceBill.setServiceOtherDescription(billDescription);

		PatientBill patientBill = PatientBillUtil.createPatientBill(totalAmount, insurancePolicy);
		InsuranceBill insuranceBill = InsuranceBillUtil.createInsuranceBill(insurance, totalAmount);
		ThirdPartyBill thirdPartyBill = ThirdPartyBillUtil.createThirdPartyBill(insurancePolicy, totalAmount);

		Consommation consommation = new Consommation(globalBill, beneficiary, now, creator, false);
		consommation.setDepartment(department);
		consommation.setPatientBill(patientBill);
		consommation.setInsuranceBill(insuranceBill);
		consommation.setThirdPartyBill(thirdPartyBill);
		consommation.addBillItem(patientServiceBill);

		saveConsommation(consommation);

		BigDecimal globalAmount = globalBill.getGlobalAmount() != null ? globalBill.getGlobalAmount() : BigDecimal.ZERO;
		globalBill.setGlobalAmount(globalAmount.add(totalAmount));
		saveGlobalBill(globalBill);

		log.info("Created ambulance bill consommationId=" + consommation.getConsommationId()
				+ " policyNumber=" + policyNumber + " km=" + kilometers + " total=" + totalAmount);

		return consommation;
	}

	@Override
	@Transactional
	public Consommation deleteAmbulanceBill(Integer consommationId, String voidReason) {
		if (consommationId == null) {
			throw new APIException("Ambulance consommation id is required");
		}
		Consommation existing = getConsommation(consommationId);
		if (existing != null && Boolean.TRUE.equals(existing.getVoided())) {
			log.info("Ambulance bill already voided: consommationId=" + consommationId);
			return existing;
		}

		Consommation consommation = loadEditableAmbulanceConsommation(consommationId, "delete");

		User voidedBy = Context.getAuthenticatedUser();
		Date now = new Date();
		String reason = hasText(voidReason) ? voidReason.trim() : "Ambulance bill deleted";

		if (consommation.getBillItems() != null) {
			for (PatientServiceBill item : consommation.getBillItems()) {
				if (item == null || Boolean.TRUE.equals(item.getVoided())) {
					continue;
				}
				item.setVoided(true);
				item.setVoidedBy(voidedBy);
				item.setVoidedDate(now);
				item.setVoidReason(reason);
				saveBilledItem(item);
			}
		}

		voidBillEntity(consommation.getPatientBill(), voidedBy, now, reason);
		voidBillEntity(consommation.getInsuranceBill(), voidedBy, now, reason);
		voidBillEntity(consommation.getThirdPartyBill(), voidedBy, now, reason);

		consommation.setVoided(true);
		consommation.setVoidedBy(voidedBy);
		consommation.setVoidedDate(now);
		consommation.setVoidReason(reason);
		saveConsommation(consommation);

		refreshAmbulanceRelatedAmounts(consommation);

		log.info("Deleted (voided) ambulance bill consommationId=" + consommationId
				+ ", voidedBy=" + (voidedBy != null ? voidedBy.getUsername() : null)
				+ ", reason=" + reason);
		return consommation;
	}

	@Override
	@Transactional
	public Consommation updateAmbulanceBill(Integer consommationId, int kilometers, String description) {
		if (kilometers <= 0) {
			throw new APIException("Number of kilometers must be greater than zero");
		}
		if (description == null || description.trim().isEmpty()) {
			throw new APIException("Ambulance bill description is required");
		}

		Consommation consommation = loadEditableAmbulanceConsommation(consommationId, "update");
		String billDescription = description.trim();
		BigDecimal quantity = BigDecimal.valueOf(kilometers);

		InsurancePolicy insurancePolicy = consommation.getBeneficiary() != null
				? consommation.getBeneficiary().getInsurancePolicy() : null;
		if (insurancePolicy == null || insurancePolicy.getInsurance() == null) {
			throw new APIException("Ambulance bill " + consommationId
					+ " has no insurance policy linked for recalculation");
		}

		BillableService billableService = resolveAmbulanceBillableService(insurancePolicy.getInsurance());
		BigDecimal unitPrice = billableService.getMaximaToPay();
		if (unitPrice == null) {
			throw new APIException("Ambulance billable service has no unit price configured for insurance: "
					+ insurancePolicy.getInsurance().getName());
		}

		int updatedItems = 0;
		if (consommation.getBillItems() != null) {
			for (PatientServiceBill item : consommation.getBillItems()) {
				if (item == null || Boolean.TRUE.equals(item.getVoided())) {
					continue;
				}
				if (!isAmbulanceBillItem(item)) {
					throw new APIException("Ambulance bill " + consommationId
							+ " contains non-ambulance items and cannot be updated via updateAmbulanceBill");
				}
				item.setQuantity(quantity);
				item.setUnitPrice(unitPrice);
				item.setServiceOtherDescription(billDescription);
				if (item.getService() == null) {
					item.setService(billableService);
				}
				saveBilledItem(item);
				updatedItems++;
			}
		}
		if (updatedItems == 0) {
			throw new APIException("Ambulance bill " + consommationId
					+ " has no active ambulance items to update");
		}

		refreshAmbulanceRelatedAmounts(consommation);

		log.info("Updated ambulance bill consommationId=" + consommationId
				+ ", km=" + kilometers
				+ ", description=" + billDescription
				+ ", unitPrice=" + unitPrice
				+ ", updatedItems=" + updatedItems);
		return getConsommation(consommationId);
	}

	private Consommation loadEditableAmbulanceConsommation(Integer consommationId, String operation) {
		if (consommationId == null) {
			throw new APIException("Ambulance consommation id is required");
		}

		Consommation consommation = getConsommation(consommationId);
		if (consommation == null) {
			throw new APIException("Ambulance consommation not found for id: " + consommationId);
		}
		if (Boolean.TRUE.equals(consommation.getVoided())) {
			throw new APIException("Cannot " + operation + " ambulance bill " + consommationId
					+ ": consommation is already voided");
		}
		if (!isAmbulanceConsommation(consommation)) {
			throw new APIException("Consommation " + consommationId + " is not an ambulance bill");
		}

		PatientBill patientBill = consommation.getPatientBill();
		if (patientBill != null) {
			if (Boolean.TRUE.equals(patientBill.getIsPaid()) || patientBill.isPaymentConfirmed()) {
				throw new APIException("Cannot " + operation + " ambulance bill " + consommationId
						+ ": patient bill is already paid/confirmed");
			}
			if (hasText(patientBill.getInvoiceNumber()) || hasText(patientBill.getBatchNumber())) {
				throw new APIException("Cannot " + operation + " ambulance bill " + consommationId
						+ ": Irembo invoice/batch is already linked (invoiceNumber="
						+ patientBill.getInvoiceNumber() + ", batchNumber=" + patientBill.getBatchNumber() + ")");
			}
			List<BillPayment> payments = billingDAO.getBillPaymentsByPatientBill(patientBill);
			boolean hasNonVoidedPayment = payments != null && payments.stream()
					.anyMatch(p -> p != null && (p.getVoided() == null || !Boolean.TRUE.equals(p.getVoided())));
			if (hasNonVoidedPayment) {
				throw new APIException("Cannot " + operation + " ambulance bill " + consommationId
						+ ": payment records already exist");
			}
		}
		return consommation;
	}

	private void refreshAmbulanceRelatedAmounts(Consommation consommation) {
		if (consommation.getBeneficiary() != null
				&& consommation.getBeneficiary().getInsurancePolicy() != null) {
			ConsommationUtil.refreshBillAmountsFromNonVoidedItems(consommation,
					consommation.getBeneficiary().getInsurancePolicy());
			return;
		}
		if (consommation.getGlobalBill() == null) {
			return;
		}
		GlobalBill globalBill = consommation.getGlobalBill();
		BigDecimal remaining = BigDecimal.ZERO;
		List<Consommation> siblings = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
		if (siblings != null) {
			for (Consommation sibling : siblings) {
				if (sibling == null || Boolean.TRUE.equals(sibling.getVoided())) {
					continue;
				}
				if (sibling.getBillItems() == null) {
					continue;
				}
				for (PatientServiceBill item : sibling.getBillItems()) {
					if (item == null || Boolean.TRUE.equals(item.getVoided())) {
						continue;
					}
					BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
					BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
					remaining = remaining.add(qty.multiply(price));
				}
			}
		}
		globalBill.setGlobalAmount(remaining);
		saveGlobalBill(globalBill);
	}

	private boolean isAmbulanceConsommation(Consommation consommation) {
		if (consommation == null) {
			return false;
		}
		Department ambulanceDepartment = null;
		try {
			ambulanceDepartment = resolveAmbulanceDepartment();
		} catch (APIException ignored) {
			// Fall back to item-category checks when department GP is missing.
		}
		if (ambulanceDepartment != null && consommation.getDepartment() != null
				&& ambulanceDepartment.getDepartmentId() != null
				&& ambulanceDepartment.getDepartmentId().equals(consommation.getDepartment().getDepartmentId())) {
			return true;
		}
		if (consommation.getBillItems() == null || consommation.getBillItems().isEmpty()) {
			return false;
		}
		boolean hasActiveAmbulanceItem = false;
		for (PatientServiceBill item : consommation.getBillItems()) {
			if (item == null || Boolean.TRUE.equals(item.getVoided())) {
				continue;
			}
			if (!isAmbulanceBillItem(item)) {
				return false;
			}
			hasActiveAmbulanceItem = true;
		}
		return hasActiveAmbulanceItem;
	}

	private boolean isAmbulanceBillItem(PatientServiceBill item) {
		if (item == null || item.getService() == null) {
			return false;
		}
		String configuredFacilityServiceName = resolveConfiguredAmbulanceFacilityServicePriceNameOrNull();
		if (configuredFacilityServiceName != null
				&& item.getService().getFacilityServicePrice() != null
				&& item.getService().getFacilityServicePrice().getName() != null
				&& configuredFacilityServiceName.equalsIgnoreCase(
						item.getService().getFacilityServicePrice().getName().trim())) {
			return true;
		}
		// Fallback for historical ambulance bills created before the facility-service GP existed.
		if (item.getService().getServiceCategory() == null
				|| item.getService().getServiceCategory().getName() == null) {
			return false;
		}
		return Category.AMBULANCE.getDescription()
				.equalsIgnoreCase(item.getService().getServiceCategory().getName().trim());
	}

	private void voidBillEntity(Object billEntity, User voidedBy, Date voidedDate, String voidReason) {
		if (billEntity instanceof PatientBill) {
			PatientBill bill = (PatientBill) billEntity;
			if (Boolean.TRUE.equals(bill.getVoided())) {
				return;
			}
			bill.setVoided(true);
			bill.setVoidedBy(voidedBy);
			bill.setVoidedDate(voidedDate);
			bill.setVoidReason(voidReason);
			bill.setAmount(BigDecimal.ZERO);
			PatientBillUtil.savePatientBill(bill);
			return;
		}
		if (billEntity instanceof InsuranceBill) {
			InsuranceBill bill = (InsuranceBill) billEntity;
			if (bill.isVoided()) {
				return;
			}
			bill.setVoided(true);
			bill.setVoidedBy(voidedBy);
			bill.setVoidedDate(voidedDate);
			bill.setVoidReason(voidReason);
			bill.setAmount(BigDecimal.ZERO);
			InsuranceBillUtil.saveInsuranceBill(bill);
			return;
		}
		if (billEntity instanceof ThirdPartyBill) {
			ThirdPartyBill bill = (ThirdPartyBill) billEntity;
			if (bill.isVoided()) {
				return;
			}
			bill.setVoided(true);
			bill.setVoidedBy(voidedBy);
			bill.setVoidedDate(voidedDate);
			bill.setVoidReason(voidReason);
			bill.setAmount(BigDecimal.ZERO);
			ThirdPartyBillUtil.saveThirdPartyBill(bill);
		}
	}

	private Department resolveAmbulanceDepartment() {
		String departmentIdValue = Context.getAdministrationService()
				.getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_AMBULANCE_DEPARTMENT_ID);
		if (departmentIdValue == null || departmentIdValue.trim().isEmpty()) {
			throw new APIException("Global property " + BillingConstants.GLOBAL_PROPERTY_AMBULANCE_DEPARTMENT_ID
					+ " is not configured");
		}
		try {
			Integer departmentId = Integer.valueOf(departmentIdValue.trim());
			Department department = getDepartement(departmentId);
			if (department == null) {
				throw new APIException("Ambulance billing department not found for id: " + departmentId);
			}
			return department;
		}
		catch (NumberFormatException e) {
			throw new APIException("Invalid ambulance department id in global property: " + departmentIdValue);
		}
	}

	private BillableService resolveAmbulanceBillableService(Insurance insurance) {
		if (insurance == null) {
			throw new APIException("Insurance is required to resolve the ambulance billable service");
		}
		String facilityServicePriceName = resolveConfiguredAmbulanceFacilityServicePriceName();
		FacilityServicePrice facilityServicePrice = getFacilityServiceByName(facilityServicePriceName);
		if (facilityServicePrice == null) {
			throw new APIException("Ambulance facility service price not found for name: \""
					+ facilityServicePriceName + "\" (global property "
					+ BillingConstants.GLOBAL_PROPERTY_AMBULANCE_FACILITY_SERVICE_PRICE_NAME + ")");
		}
		if (Boolean.TRUE.equals(facilityServicePrice.isRetired())) {
			throw new APIException("Configured ambulance facility service price is retired: name=\""
					+ facilityServicePriceName + "\"");
		}

		BillableService billableService = getBillableServiceByConcept(facilityServicePrice, insurance);
		if (billableService == null || Boolean.TRUE.equals(billableService.getRetired())) {
			throw new APIException("No active billable service found for facility service name=\""
					+ facilityServicePriceName + "\" and insurance: " + insurance.getName()
					+ ". Configure the insurance billable service for that facility service.");
		}
		return billableService;
	}

	private String resolveConfiguredAmbulanceFacilityServicePriceName() {
		String facilityServicePriceName = resolveConfiguredAmbulanceFacilityServicePriceNameOrNull();
		if (facilityServicePriceName == null) {
			throw new APIException("Global property "
					+ BillingConstants.GLOBAL_PROPERTY_AMBULANCE_FACILITY_SERVICE_PRICE_NAME
					+ " is not configured");
		}
		return facilityServicePriceName;
	}

	private String resolveConfiguredAmbulanceFacilityServicePriceNameOrNull() {
		String facilityServicePriceName = Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_AMBULANCE_FACILITY_SERVICE_PRICE_NAME,
				BillingConstants.DEFAULT_AMBULANCE_FACILITY_SERVICE_PRICE_NAME);
		if (facilityServicePriceName == null || facilityServicePriceName.trim().isEmpty()) {
			return null;
		}
		return facilityServicePriceName.trim();
	}

	@Override
	public RhipVoucherItemRecord saveRhipVoucherItemRecord(RhipVoucherItemRecord record) {
		return billingDAO.saveRhipVoucherItemRecord(record);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RhipVoucherItemRecord> getRhipVoucherItemRecordsByGlobalBill(GlobalBill globalBill) {
		return billingDAO.getRhipVoucherItemRecordsByGlobalBill(globalBill);
	}

	@Override
	@Transactional(readOnly = true)
	public RhipVoucherItemRecord getLatestRhipVoucherItemRecord(PatientServiceBill patientServiceBill) {
		return billingDAO.getLatestRhipVoucherItemRecord(patientServiceBill);
	}

	@Override
	public RhipVoucherSubmission saveRhipVoucherSubmission(RhipVoucherSubmission submission) {
		return billingDAO.saveRhipVoucherSubmission(submission);
	}

	@Override
	@Transactional(readOnly = true)
	public List<RhipVoucherSubmission> getRhipVoucherSubmissionsByGlobalBill(GlobalBill globalBill) {
		return billingDAO.getRhipVoucherSubmissionsByGlobalBill(globalBill);
	}

	@Override
	@Transactional(readOnly = true)
	public RhipVoucherSubmission getLatestRhipVoucherSubmission(GlobalBill globalBill) {
		return billingDAO.getLatestRhipVoucherSubmission(globalBill);
	}

	@Override
	@Transactional(readOnly = true)
	public RhipVoucherSubmission getSuccessfulRhipVoucherSubmission(GlobalBill globalBill) {
		return billingDAO.getSuccessfulRhipVoucherSubmission(globalBill);
	}

	@Override
	@Transactional(readOnly = true)
	public List<GlobalBill> getRhipVoucherSubmissionGlobalBills(RhipVoucherSubmissionSearchCriteria criteria,
			Integer firstResult, Integer maxResults) {
		return billingDAO.getRhipVoucherSubmissionGlobalBills(criteria, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly = true)
	public Integer countRhipVoucherSubmissionGlobalBills(RhipVoucherSubmissionSearchCriteria criteria) {
		return billingDAO.countRhipVoucherSubmissionGlobalBills(criteria);
	}

}
