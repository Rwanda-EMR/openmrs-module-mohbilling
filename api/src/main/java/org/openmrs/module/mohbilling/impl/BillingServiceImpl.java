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
 * @author rbcemr
 * 
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
	 * @param billingDAO
	 *            the billingDAO to set
	 */
	public void setBillingDAO(BillingDAO billingDAO) {
		this.billingDAO = billingDAO;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.service.BillingService#getInsurance(java
	 *      .lang.Integer)
	 */
	@Override
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
	 * @see org.openmrs.module.mohbilling.service.BillingService#savePatientBill(org.openmrs.module.mohbilling.model.PatientBill)
	 */
	@Override
	public void savePatientBill(PatientBill bill) {

		billingDAO.savePatientBill(bill);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.module.mohbilling.service.BillingService#getFacilityServicePrice(Integer)
	 */
	@Override
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
	public List<InsurancePolicy> getAllInsurancePolicies() {

		return billingDAO.getAllInsurancePolicies();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllInsurances()
	 */
	@Override
	public List<Insurance> getAllInsurances() throws DAOException {

		return billingDAO.getAllInsurances();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllPatientBills()
	 */
	@Override
	public List<PatientBill> getAllPatientBills() throws DAOException {

		return billingDAO.getAllPatientBills();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllFacilityServicePrices()
	 */
	@Override
	public List<FacilityServicePrice> getAllFacilityServicePrices()
			throws DAOException {

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

	public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance,
			Date startDate, Date endDate) {
		return billingDAO.getPaidAmountPerInsuranceAndPeriod(insurance,
				startDate, endDate);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicyByCardNo(String)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo) {
		return billingDAO.getInsurancePolicyByCardNo(insuranceCardNo);
	}

	@Override
	public List<PatientBill> billCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {
		return billingDAO.billCohortBuilder(insurance, startDate, endDate,
				patientId, serviceName, billStatus, billCollector);
	}

	@Override
	public BillableService getBillableServiceByConcept(
			FacilityServicePrice price, Insurance insurance) {
		return billingDAO.getBillableServiceByConcept(price, insurance);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getThirdParty(Integer)
	 */
	@Override
	public ThirdParty getThirdParty(Integer thirdPartyId) throws DAOException {

		return billingDAO.getThirdParty(thirdPartyId);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllThirdParties()
	 */
	@Override
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
	public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber)
			throws DAOException {

		return billingDAO.getBeneficiaryByPolicyNumber(policyIdNumber);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicyByBeneficiary(org.openmrs.module.mohbilling.model.Beneficiary)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByBeneficiary(
			Beneficiary beneficiary) {

		return billingDAO.getInsurancePolicyByBeneficiary(beneficiary);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getValidBillableService(Integer)
	 */
	@Override
	public BillableService getBillableService(Integer id) {

		return billingDAO.getBillableService(id);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getServiceCategory(Integer)
	 */
	@Override
	public ServiceCategory getServiceCategory(Integer id) {

		return billingDAO.getServiceCategory(id);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getBillableServiceByCategory(org.openmrs.module.mohbilling.model.ServiceCategory)
	 */
	@Override
	public List<BillableService> getBillableServiceByCategory(ServiceCategory sc) {

		return billingDAO.getBillableServiceByCategory(sc);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getFacilityServiceByConcept(org.openmrs.Concept)
	 */
	@Override
	public FacilityServicePrice getFacilityServiceByConcept(Concept concept) {

		return billingDAO.getFacilityServiceByConcept(concept);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getBillableServicesByFacilityService(org.openmrs.module.mohbilling.model.FacilityServicePrice)
	 */
	@Override
	public List<BillableService> getBillableServicesByFacilityService(
			FacilityServicePrice fsp) {

		return billingDAO.getBillableServicesByFacilityService(fsp);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getBillableServicesByInsurance(org.openmrs.module.mohbilling.model.Insurance)
	 */
	@Override
	public List<BillableService> getBillableServicesByInsurance(
			Insurance insurance) {

		return billingDAO.getBillableServicesByInsurance(insurance);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getPolicyIdByPatient(Integer)
	 */
	@Override
	public List<String[]> getPolicyIdByPatient(Integer patientId) {

		return billingDAO.getPolicyIdByPatient(patientId);
	}

	@Override
	public List<BillPayment> getAllBillPayments() {
		return billingDAO.getAllBillPayments();
	}

	@Override
	public List<BillPayment> getBillPaymentsByDateAndCollector(
			Date createdDate, Date endDate, User collector) {

		return billingDAO.getBillPaymentsByDateAndCollector(createdDate,
				endDate, collector);
	}

	@Override
	public List<BillPayment> paymentsCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {
		return billingDAO.paymentsCohortBuilder(insurance, startDate, endDate,
				patientId, serviceName, billStatus, billCollector);
	}

	@Override
	public ServiceCategory getServiceCategoryByName(String name,
			Insurance insurance) {
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
	public Map<String, Double> getRevenueByService(Date receivedDate,
			String[] serviceCategory, String collector, Insurance insurance) {
		return billingDAO.getRevenueByService(receivedDate, serviceCategory,
				collector, insurance);
	}

	@Override
	public List<PatientBill> getPatientBillsByCollector(Date receivedDate,
			User collector) {
		// TODO Auto-generated method stub
		return billingDAO.getPatientBillsByCollector(receivedDate, collector);
	}

	@Override
	public PatientBill getBills(Patient patient, Date startDate, Date endDate) {
		return billingDAO.getBills(patient, startDate, endDate);
	}

	@Override
	public InsuranceRate getInsuranceRateByInsurance(Insurance insurance) {
		// TODO Auto-generated method stub
		return billingDAO.getInsuranceRateByInsurance(insurance);
	}

	@Override
	public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo) {
		// TODO Auto-generated method stub
		return billingDAO.getBeneficiaryByCardNumber(cardNo);
	}

	@Override
	public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(
			Date startDate, Date endDate) {
		// TODO Auto-generated method stub
		return billingDAO.getInsurancePoliciesBetweenTwodates(startDate,
				endDate);
	}

	@Override
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
	public List<Object[]> getBaseBillableServices(Insurance i) {
		return billingDAO.getBaseBillableServices(i);
	}

	@Override
	public List<Object[]> getPharmacyBaseBillableServices(Insurance i) {
		return billingDAO.getPharmacyBaseBillableServices(i);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getRefundedBills(Date,
	 *      Date, org.openmrs.User)
	 */
	@Override
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
	public List<HopService> getAllHopService() {
		return billingDAO.getAllHopService();
	}

	@Override
	public HopService getHopService(Integer serviceId) {
		// TODO Auto-generated method stub
		return billingDAO.getHopService(serviceId);
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
	public PatientAccount getPatientAccount(Integer accountId) {
		return billingDAO.getPatientAccount(accountId);
	}

	@Override
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
	public PaidServiceBill getPaidServiceBill(Integer paidSviceBillid) {
		return billingDAO.getPaidServiceBill(paidSviceBillid);
	}

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
	public List<HopService> getHospitalServicesByDepartment(
			Department department) {
		return billingDAO.getHospitalServicesByDepartment(department);
	}
	@Override
	public Transaction getTransactionById(Integer id) {
		return billingDAO.getTransactionById(id);
	}
	/* (non-Javadoc)
	 * @see org.openmrs.module.mohbilling.service.BillingService#getServiceByName(java.lang.String)
	 */
	@Override
	public HopService getServiceByName(String name) {
		
		return billingDAO.getServiceByName(name);
	}

	@Override
	public List<PaidServiceBill> getPaidItemsByBillPayments(
			List<BillPayment> payments) {
		// TODO Auto-generated method stub
		return billingDAO.getPaidItemsByBillPayments(payments);
	}

	@Override
	public List<PatientServiceBill> getBillItemsByCategory(
			Consommation consommation, HopService service) {
		return billingDAO.getBillItemsByCategory(consommation, service);
	}

	@Override
	public List<PatientServiceBill> getBillItemsByGroupedCategories(
			Consommation consommation, List<HopService> services) {
		return billingDAO.getBillItemsByGroupedCategories(consommation, services);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.mohbilling.service.BillingService#getGlobalBills(java.util.Date, java.util.Date)
	 */
	@Override
	public List<GlobalBill> getGlobalBills(Date date1, Date date2) {
		// TODO Auto-generated method stub
		return billingDAO.getGlobalBills(date1, date2);
	}

	@Override
	public List<Consommation> getConsommationByGlobalBills(
			List<GlobalBill> globalBills) {
		return billingDAO.getConsommationByGlobalBills(globalBills);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllSubmittedPaymentRefunds()
	 */
	@Override
	public List<PaymentRefund> getAllSubmittedPaymentRefunds() {
		return billingDAO.getAllSubmittedPaymentRefunds();
	}

	@Override
	public PaymentRefund getRefundById(Integer id) {
		return billingDAO.getRefundById(id);
	}

	@Override
	public PaidServiceBillRefund getPaidServiceBillRefund(
			Integer paidSviceBillRefundid) {
		return billingDAO.getPaidServiceBillRefund(paidSviceBillRefundid);
	}


	@Override
	public List<PaymentRefund> getRefundsByBillPayment(BillPayment payment) {
		return billingDAO.getRefundsByBillPayment(payment);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.mohbilling.service.BillingService#getRefundsBetweenDatesAndByCollector(java.util.Date, java.util.Date, org.openmrs.User)
	 */
	@Override
	public List<PaymentRefund> getRefundsBetweenDatesAndByCollector(
			Date startDate, Date endDate, User collector) {
		return billingDAO.getRefundsBetweenDatesAndByCollector(startDate, endDate, collector);
	}

	@Override
	public InsurancePolicy getInsurancePolicyByThirdParty(ThirdParty t) {
		System.out.print(" am getting in getinsurancepolicybythird party in billingserviceImplement "+t);
		return billingDAO.getInsurancePolicyByThirdParty(t);
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.mohbilling.service.BillingService#getConsommations(java.util.Date, java.util.Date, org.openmrs.module.mohbilling.model.Insurance, org.openmrs.module.mohbilling.model.ThirdParty, org.openmrs.User)
	 */
	@Override
	public List<Consommation> getConsommations(Date startDate, Date endDate,
			Insurance insurance, ThirdParty tp, User billCreator,Department department) {
		return billingDAO.getConsommations(startDate, endDate, insurance, tp, billCreator,department);
	}

	@Override
	public void updateOtherInsurances( ServiceCategory sc) {
		billingDAO.updateOtherInsurances(sc);
		
	}

	/* (non-Javadoc)
	 * @see org.openmrs.module.mohbilling.service.BillingService#getTransactions(java.util.Date, java.util.Date, org.openmrs.User, java.lang.String)
	 */
	@Override
	public List<Transaction> getTransactions(Date startDate, Date endDate,
			User collector, String type) {
		return billingDAO.getTransactions(startDate, endDate, collector, type);
	}


}