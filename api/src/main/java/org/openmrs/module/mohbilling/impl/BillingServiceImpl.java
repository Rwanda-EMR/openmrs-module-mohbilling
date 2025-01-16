/**
 *
 */
package org.openmrs.module.mohbilling.impl;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BillingService Implementation.
 */
@Transactional
public class BillingServiceImpl implements BillingService {

    private BillingDAO billingDAO;

    /**
     * @return the billingDAO
     */
    public BillingDAO getBillingDAO() {
        return billingDAO;
    }

    /**
     * @param billingDAO the billingDAO to set
     */
    public void setBillingDAO(BillingDAO billingDAO) {
        this.billingDAO = billingDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientBill> getPatientBillsByPagination(Integer startIndex, Integer pageSize) throws DAOException {
        return billingDAO.getPatientBillsByPagination(startIndex, pageSize);
    }

    @Override
    public Insurance getInsurance(Integer insuranceId) throws DAOException {
        return billingDAO.getInsurance(insuranceId);
    }

    @Override
    public InsurancePolicy getInsurancePolicy(Integer insurancePolicyId) throws DAOException {
        return billingDAO.getInsurancePolicy(insurancePolicyId);
    }

    @Override
    public PatientBill getPatientBill(Integer billId) throws DAOException {
        return billingDAO.getPatientBill(billId);
    }

    @Override
    public void saveInsurance(Insurance insurance) {
        if (insurance.getName() == null) {
            throw new APIException("Insurance name is required");
        }
        billingDAO.saveInsurance(insurance);
    }

    @Override
    public void saveInsurancePolicy(InsurancePolicy card) {
        if (card.getInsuranceCardNo() == null && !card.getInsurance().getCategory().equals(InsuranceCategory.NONE.toString())) {
            throw new APIException("Insurance Card Number is required");
        }
        billingDAO.saveInsurancePolicy(card);
    }

    @Override
    public PatientBill savePatientBill(PatientBill bill) {
        if (bill == null) {
            throw new IllegalArgumentException("Cannot save a null PatientBill.");
        }
        billingDAO.savePatientBill(bill);
        return bill;
    }

    @Override
    public FacilityServicePrice getFacilityServicePrice(Integer id) {
        return billingDAO.getFacilityServicePrice(id);
    }

    @Override
    public void saveFacilityServicePrice(FacilityServicePrice fsp) {
        if (fsp.getName() == null) {
            throw new APIException("Facility Service name is required");
        }
        billingDAO.saveFacilityServicePrice(fsp);
    }

    @Override
    public List<InsurancePolicy> getAllInsurancePolicies() {
        return billingDAO.getAllInsurancePolicies();
    }

    @Override
    public List<Insurance> getAllInsurances() throws DAOException {
        return billingDAO.getAllInsurances();
    }

    @Override
    public List<PatientBill> getAllPatientBills() throws DAOException {
        return billingDAO.getAllPatientBills();
    }

    @Override
    public List<FacilityServicePrice> getAllFacilityServicePrices() throws DAOException {
        return billingDAO.getAllFacilityServicePrices();
    }

    @Override
    public List<ServiceCategory> getAllServiceCategories() throws DAOException {
        return billingDAO.getAllServiceCategories();
    }

    @Override
    public List<BillableService> getAllBillableServices() {
        return billingDAO.getAllBillableServices();
    }

    @Override
    public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance, Date startDate, Date endDate) {
        return billingDAO.getPaidAmountPerInsuranceAndPeriod(insurance, startDate, endDate);
    }

    @Override
    public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo) {
        return billingDAO.getInsurancePolicyByCardNo(insuranceCardNo);
    }

    @Override
    public List<PatientBill> billCohortBuilder(Insurance insurance, Date startDate, Date endDate, Integer patientId,
                                               String serviceName, String billStatus, String billCollector) {
        return billingDAO.billCohortBuilder(insurance, startDate, endDate, patientId, serviceName, billStatus, billCollector);
    }

    @Override
    public BillableService getBillableServiceByConcept(FacilityServicePrice price, Insurance insurance) {
        return billingDAO.getBillableServiceByConcept(price, insurance);
    }

    @Override
    public ThirdParty getThirdParty(Integer thirdPartyId) throws DAOException {
        return billingDAO.getThirdParty(thirdPartyId);
    }

    @Override
    public List<ThirdParty> getAllThirdParties() {
        return billingDAO.getAllThirdParties();
    }

    @Override
    public void saveThirdParty(ThirdParty thirdParty) throws DAOException {
        billingDAO.saveThirdParty(thirdParty);
    }

    @Override
    public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber) throws DAOException {
        return billingDAO.getBeneficiaryByPolicyNumber(policyIdNumber);
    }

    @Override
    public InsurancePolicy getInsurancePolicyByBeneficiary(Beneficiary beneficiary) {
        return billingDAO.getInsurancePolicyByBeneficiary(beneficiary);
    }

    @Override
    public BillableService getBillableService(Integer id) {
        return billingDAO.getBillableService(id);
    }

    @Override
    public ServiceCategory getServiceCategory(Integer id) {
        return billingDAO.getServiceCategory(id);
    }

    @Override
    public List<BillableService> getBillableServiceByCategory(ServiceCategory sc) {
        return billingDAO.getBillableServiceByCategory(sc);
    }

    @Override
    public FacilityServicePrice getFacilityServiceByConcept(Concept concept) {
        return billingDAO.getFacilityServiceByConcept(concept);
    }

    @Override
    public List<BillableService> getBillableServicesByFacilityService(FacilityServicePrice fsp) {
        return billingDAO.getBillableServicesByFacilityService(fsp);
    }

    @Override
    public List<BillableService> getBillableServicesByInsurance(Insurance insurance) {
        return billingDAO.getBillableServicesByInsurance(insurance);
    }

    @Override
    public List<String[]> getPolicyIdByPatient(Integer patientId) {
        return billingDAO.getPolicyIdByPatient(patientId);
    }

    @Override
    public List<BillPayment> getAllBillPayments() {
        return billingDAO.getAllBillPayments();
    }

    @Override
    public List<BillPayment> getBillPaymentsByDateAndCollector(Date createdDate, Date endDate, User collector) {
        return billingDAO.getBillPaymentsByDateAndCollector(createdDate, endDate, collector);
    }

    @Override
    public List<BillPayment> paymentsCohortBuilder(Insurance insurance, Date startDate, Date endDate, Integer patientId,
                                                    String serviceName, String billStatus, String billCollector) {
        return billingDAO.paymentsCohortBuilder(insurance, startDate, endDate, patientId, serviceName, billStatus, billCollector);
    }

    @Override
    public ServiceCategory getServiceCategoryByName(String name, Insurance insurance) {
        return billingDAO.getServiceCategoryByName(name, insurance);
    }

    @Override
    public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate) {
        return billingDAO.getRevenueDatesBetweenDates(startDate, endDate);
    }

    @Override
    public Object[] getBills(Date startDate, Date endDate, User collector) {
        return billingDAO.getBills(startDate, endDate, collector);
    }

    @Override
    public Map<String, Double> getRevenueByService(Date receivedDate, String[] serviceCategory, String collector, Insurance insurance) {
        return billingDAO.getRevenueByService(receivedDate, serviceCategory, collector, insurance);
    }

    @Override
    public List<PatientBill> getPatientBillsByCollector(Date receivedDate, User collector) {
        return billingDAO.getPatientBillsByCollector(receivedDate, collector);
    }

    @Override
    public PatientBill getBills(Patient patient, Date startDate, Date endDate) {
        return billingDAO.getBills(patient, startDate, endDate);
    }

    @Override
    public InsuranceRate getInsuranceRateByInsurance(Insurance insurance) {
        return billingDAO.getInsuranceRateByInsurance(insurance);
    }

    @Override
    public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo) {
        return billingDAO.getBeneficiaryByCardNumber(cardNo);
    }

    @Override
    public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(Date startDate, Date endDate) {
        return billingDAO.getInsurancePoliciesBetweenTwodates(startDate, endDate);
    }

    @Override
    public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary, Date startDate, Date endDate) {
        return billingDAO.getBillsByBeneficiary(beneficiary, startDate, endDate);
    }

    @Override
    public void loadBillables(Insurance insurance) {
        billingDAO.loadBillables(insurance);
    }

    @Override
    public List<Object[]> getBaseBillableServices(Insurance i) {
        return billingDAO.getBaseBillableServices(i);
    }

    @Override
    public List<Object[]> getPharmacyBaseBillableServices(Insurance i) {
        return billingDAO.getPharmacyBaseBillableServices(i);
    }

    @Override
    public Set<PatientBill> getRefundedBills(Date startDate, Date endDate, User collector) {
        return billingDAO.getRefundedBills(startDate, endDate, collector);
    }

    @Override
    public Department saveDepartement(Department departement) {
        return billingDAO.saveDepartement(departement);
    }

    @Override
    public Department getDepartement(Integer departementId) {
        return billingDAO.getDepartement(departementId);
    }

    @Override
    public List<Department> getAllDepartements() {
        return billingDAO.getAllDepartements();
    }

    @Override
    public HopService saveHopService(HopService service) {
        return billingDAO.saveHopService(service);
    }

    @Override
    public List<HopService> getAllHopService() {
        return billingDAO.getAllHopService();
    }

    @Override
    public HopService getHopService(Integer serviceId) {
        return billingDAO.getHopService(serviceId);
    }

    @Override
    public HopService getHopService(String name) {
        return billingDAO.getHopService(name);
    }

    @Override
    public Admission saveAdmission(Admission admission) {
        return billingDAO.saveAdmission(admission);
    }

    @Override
    public Admission getPatientAdmission(Integer admissionId) {
        return billingDAO.getPatientAdmission(admissionId);
    }

    @Override
    public GlobalBill saveGlobalBill(GlobalBill globalBill) {
        return billingDAO.saveGlobalBill(globalBill);
    }

    @Override
    public GlobalBill GetGlobalBill(Integer globalBillId) {
        return billingDAO.GetGlobalBill(globalBillId);
    }

    @Override
    public GlobalBill getGlobalBillByAdmission(Admission admission) {
        return billingDAO.getGlobalBillByAdmission(admission);
    }

    @Override
    public List<Admission> getAdmissionsListByInsurancePolicy(InsurancePolicy ip) {
        return billingDAO.getAdmissionsListByInsurancePolicy(ip);
    }

    @Override
    public void saveConsommation(Consommation consommation) {
        billingDAO.saveConsommation(consommation);
    }

    @Override
    public void saveInsuranceBill(InsuranceBill ib) {
        billingDAO.saveInsuranceBill(ib);
    }

    @Override
    public void saveThirdPartyBill(ThirdPartyBill thirdBill) {
        billingDAO.saveThirdPartyBill(thirdBill);
    }

    @Override
    public Consommation getConsommation(Integer consommationId) {
        return billingDAO.getConsommation(consommationId);
    }

    @Override
    public CashPayment saveCashPayment(CashPayment cashPayment) {
        return billingDAO.saveCashPayment(cashPayment);
    }

    @Override
    public PatientServiceBill saveBilledItem(PatientServiceBill psb) {
        return billingDAO.saveBilledItem(psb);
    }

    @Override
    public PatientServiceBill getPatientServiceBill(Integer patientServiceBillId) {
        return billingDAO.getPatientServiceBill(patientServiceBillId);
    }

    @Override
    public void saveBillPayment(BillPayment bp) {
        billingDAO.getPatientServiceBill(bp);
    }

    @Override
    public void savePaidServiceBill(PaidServiceBill paidSb) {
        billingDAO.savePaidServiceBill(paidSb);
    }

    @Override
    public List<Consommation> getAllConsommationByGlobalBill(GlobalBill globalBill) {
        return billingDAO.getAllConsommationByGlobalBill(globalBill);
    }

    @Override
    public GlobalBill getGlobalBillByBillIdentifier(String billIdentifier) {
        return billingDAO.getGlobalBillByBillIdentifier(billIdentifier);
    }

    @Override
    public List<Consommation> getConsommationsByBeneficiary(Beneficiary beneficiary) {
        return billingDAO.getConsommationsByBeneficiary(beneficiary);
    }

    @Override
    public void savePatientAccount(PatientAccount account) {
        billingDAO.savePatientAccount(account);
    }

    @Override
    public PatientAccount getPatientAccount(Integer accountId) {
        return billingDAO.getPatientAccount(accountId);
    }

    @Override
    public PatientAccount getPatientAccount(Patient patient) {
        return billingDAO.getPatientAccount(patient);
    }

    @Override
    public BillPayment getBillPayment(Integer paymentId) {
        return billingDAO.getBillPayment(paymentId);
    }

    @Override
    public List<PaidServiceBill> getPaidServices(BillPayment payment) {
        return billingDAO.getPaidServices(payment);
    }

    @Override
    public Consommation getConsommationByPatientBill(PatientBill patientBill) {
        return billingDAO.getConsommationByPatientBill(patientBill);
    }

    @Override
    public PaymentRefund savePaymentRefund(PaymentRefund refund) {
        return billingDAO.savePaymentRefund(refund);
    }

    @Override
    public PaidServiceBill getPaidServiceBill(Integer paidSviceBillId) {
        return billingDAO.getPaidServiceBill(paidSviceBillId);
    }

    @Override
    public Set<Transaction> getTransactions(PatientAccount acc, Date startDate, Date endDate, String reason) {
        return billingDAO.getTransactions(acc, startDate, endDate, reason);
    }

    @Override
    public DepositPayment saveDepositPayment(DepositPayment depositPayment) {
        return billingDAO.saveDepositPayment(depositPayment);
    }

    @Override
    public List<HopService> getHospitalServicesByDepartment(Department department) {
        return billingDAO.getHospitalServicesByDepartment(department);
    }

    @Override
    public Transaction getTransactionById(Integer id) {
        return billingDAO.getTransactionById(id);
    }

    @Override
    public HopService getServiceByName(String name) {
        return billingDAO.getServiceByName(name);
    }

    @Override
    public List<PaidServiceBill> getPaidItemsByBillPayments(List<BillPayment> payments) {
        return billingDAO.getPaidItemsByBillPayments(payments);
    }

    @Override
    public List<PatientServiceBill> getBillItemsByCategory(Consommation consommation, HopService service) {
        return billingDAO.getBillItemsByCategory(consommation, service);
    }

    @Override
    public List<PatientServiceBill> getBillItemsByGroupedCategories(Consommation consommation, List<HopService> services) {
        return billingDAO.getBillItemsByGroupedCategories(consommation, services);
    }

    @Override
    public List<GlobalBill> getGlobalBills(Date date1, Date date2) {
        return billingDAO.getGlobalBills(date1, date2);
    }

    @Override
    public List<GlobalBill> getGlobalBills() {
        return billingDAO.getGlobalBills();
    }

    @Override
    public List<GlobalBill> getGlobalBills(Date date1, Date date2, Insurance insurance) {
        return billingDAO.getGlobalBills(date1, date2, insurance);
    }

    @Override
    public List<GlobalBill> getGlobalBillsWithNullInsurance() {
        return billingDAO.getGlobalBillsWithNullInsurance();
    }

    @Override
    public List<Consommation> getConsommationByGlobalBills(List<GlobalBill> globalBills)
