/**
 * 
 */
package org.openmrs.module.mohbilling.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.ServiceCategory;
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
	public List<PatientBill> buildCohort(Insurance insurance, Date startDate,
			Date endDate, Integer patientId, String serviceName) {
		return billingDAO.buildCohort(insurance, startDate, endDate, patientId,
				serviceName);
	}

	@Override
	public BillableService getBillableServiceByConcept(
			FacilityServicePrice price, Insurance insurance) {
		return billingDAO.getBillableServiceByConcept(price, insurance);
	}
}
