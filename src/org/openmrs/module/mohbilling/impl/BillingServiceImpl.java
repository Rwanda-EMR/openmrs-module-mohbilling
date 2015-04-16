/**
 * 
 */
package org.openmrs.module.mohbilling.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kamonyo
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
	 * @see org.openmrs.module.mohbilling.service.BillingService#getFacilityServicePrice(java.lang.Integer)
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

	@Override
	public void saveRecovery(Recovery recovery) {
		billingDAO.saveRecovery(recovery);
	}

	public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance,
			Date startDate, Date endDate) {
		return billingDAO.getPaidAmountPerInsuranceAndPeriod(insurance,
				startDate, endDate);
	}

	public List<Recovery> getAllPaidAmountPerInsuranceAndPeriod(
			Insurance insurance, Date startDate, Date endDate) {
		return billingDAO.getAllPaidAmountPerInsuranceAndPeriod(insurance,
				startDate, endDate);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getInsurancePolicyByCardNo(java.lang.String)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo) {
		return billingDAO.getInsurancePolicyByCardNo(insuranceCardNo);
	}

	@Override
	public List<PatientBill> billCohortBuilder(Insurance insurance, Date startDate,
			Date endDate, Integer patientId, String serviceName,
			String billStatus, String billCollector) {
		return billingDAO.billCohortBuilder(insurance, startDate, endDate, patientId,
				serviceName, billStatus, billCollector);
	}

	@Override
	public BillableService getBillableServiceByConcept(
			FacilityServicePrice price, Insurance insurance) {
		return billingDAO.getBillableServiceByConcept(price, insurance);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getThirdParty(java.lang.Integer)
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
	 * @see org.openmrs.module.mohbilling.service.BillingService#getAllRecoveries()
	 */
	@Override
	public List<Recovery> getAllRecoveries() throws DAOException {

		return billingDAO.getAllRecoveries();
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getRecovery(java.lang.Integer)
	 */
	@Override
	public Recovery getRecovery(Integer recoveryId) throws DAOException {

		return billingDAO.getRecovery(recoveryId);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getBeneficiaryByPolicyNumber(java.lang.String)
	 */
	@Override
	public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber)
			throws DAOException {

		return billingDAO.getBeneficiaryByPolicyNumber(policyIdNumber);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getBillsByBeneficiary(org.openmrs.module.mohbilling.model.Beneficiary)
	 */
	@Override
	public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary) {

		return billingDAO.getBillsByBeneficiary(beneficiary);
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
	 * @see org.openmrs.module.mohbilling.service.BillingService#getValidBillableService(java.lang.Integer)
	 */
	@Override
	public BillableService getBillableService(Integer id) {

		return billingDAO.getBillableService(id);
	}

	/**
	 * @see org.openmrs.module.mohbilling.service.BillingService#getServiceCategory(java.lang.Integer)
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
	 * @see org.openmrs.module.mohbilling.service.BillingService#getPolicyIdByPatient(java.lang.Integer)
	 */
	@Override
	public List<String[]> getPolicyIdByPatient(Integer patientId) {

		return billingDAO.getPolicyIdByPatient(patientId);
	}

	@Override
	public List<BillPayment> getAllBillPayments(){
		return billingDAO.getAllBillPayments();
	}

	@Override
	public List<BillPayment> getBillPaymentsByDateAndCollector(
			Date createdDate,Date endDate, User collector) {
	
		return billingDAO.getBillPaymentsByDateAndCollector(createdDate,endDate,collector);
	}

	@Override
	public List<BillPayment> paymentsCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {
		return billingDAO.paymentsCohortBuilder(insurance, startDate, endDate, patientId, serviceName, billStatus, billCollector);
	}

	@Override
	public ServiceCategory getServiceCategoryByName(String name, Insurance insurance) {
		return billingDAO.getServiceCategoryByName(name, insurance);
	}

	@Override
	public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate){
		return billingDAO.getRevenueDatesBetweenDates(startDate,endDate);
	}


	@Override
	public List<PatientBill> getBills(Date startDate,Date endDate) {
		return billingDAO.getBills(startDate,endDate);
	}

	@Override
	public Map<String,Double> getRevenueByService(Date receivedDate,
			String[] serviceCategory, String collector, Insurance insurance) {
		return billingDAO.getRevenueByService(receivedDate, serviceCategory, collector, insurance);
	}

	@Override
	public PatientBill getBills(Patient patient, Date startDate, Date endDate) {
		return billingDAO.getBills(patient, startDate, endDate);
	}


	
	
}
