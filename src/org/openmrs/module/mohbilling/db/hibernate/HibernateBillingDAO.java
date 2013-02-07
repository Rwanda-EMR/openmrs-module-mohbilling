/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohbilling.db.hibernate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * @author Kamonyo
 * 
 */
@SuppressWarnings("unchecked")
public class HibernateBillingDAO implements BillingDAO {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getInsurance(org.openmrs.
	 *      module.mohbilling.model.Insurance)
	 */
	@Override
	public Insurance getInsurance(Integer insuranceId) {

		return (Insurance) sessionFactory.getCurrentSession().get(
				Insurance.class, insuranceId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getInsurancePolicy(org.openmrs
	 *      .module.mohbilling.model.InsurancePolicy)
	 */
	@Override
	public InsurancePolicy getInsurancePolicy(Integer cardId) {

		return (InsurancePolicy) sessionFactory.getCurrentSession().get(
				InsurancePolicy.class, cardId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getFacilityServicePrice(java.lang.Integer)
	 */
	@Override
	public FacilityServicePrice getFacilityServicePrice(Integer id) {
		return (FacilityServicePrice) sessionFactory.getCurrentSession().get(
				FacilityServicePrice.class, id);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#saveFacilityServicePrice(org.openmrs.module.mohbilling.model.FacilityServicePrice)
	 */
	@Override
	public void saveFacilityServicePrice(FacilityServicePrice fsp) {
		sessionFactory.getCurrentSession().saveOrUpdate(fsp);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getPatientBill(org.openmrs
	 *      .module.mohbilling.model.PatientBill)
	 */
	@Override
	public PatientBill getPatientBill(Integer billId) {

		return (PatientBill) sessionFactory.getCurrentSession().get(
				PatientBill.class, billId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#saveInsurance(org.openmrs
	 *      .module.mohbilling.model.Insurance)
	 */
	@Override
	public void saveInsurance(Insurance insurance) {

		sessionFactory.getCurrentSession().saveOrUpdate(insurance);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#saveInsurancePolicy(org.openmrs
	 *      .module.mohbilling.model.InsurancePolicy)
	 */
	@Override
	public void saveInsurancePolicy(InsurancePolicy card) {

		sessionFactory.getCurrentSession().saveOrUpdate(card);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#savePatientBill(org.openmrs
	 *      .module.mohbilling.model.PatientBill)
	 */
	@Override
	public void savePatientBill(PatientBill bill) {

		sessionFactory.getCurrentSession().saveOrUpdate(bill);
	}

	@Override
	public void saveRecovery(Recovery recovery) {
		sessionFactory.getCurrentSession().saveOrUpdate(recovery);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllInsurancePolicies()
	 */
	@Override
	public List<InsurancePolicy> getAllInsurancePolicies() {

		return sessionFactory.getCurrentSession().createCriteria(
				InsurancePolicy.class).list();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllInsurances()
	 */
	@Override
	public List<Insurance> getAllInsurances() throws DAOException {

		return sessionFactory.getCurrentSession().createCriteria(
				Insurance.class).addOrder(Order.asc("category")).list();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllPatientBills()
	 */
	@Override
	public List<PatientBill> getAllPatientBills() throws DAOException {

		return sessionFactory.getCurrentSession().createCriteria(
				PatientBill.class).list();
	}
	
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllFacilityServicePrices()
	 */
	@Override
	public List<ServiceCategory> getAllServiceCategories()
			throws DAOException {

		return sessionFactory.getCurrentSession().createCriteria(
				ServiceCategory.class).list();
	}

	
	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllFacilityServicePrices()
	 */
	@Override
	public List<FacilityServicePrice> getAllFacilityServicePrices()
			throws DAOException {

		return sessionFactory.getCurrentSession().createCriteria(
				FacilityServicePrice.class).list();
	}

	@Override
	public List<BillableService> getAllBillableServices() {
		return sessionFactory.getCurrentSession().createCriteria(
				BillableService.class).list();
	}

	@Override
	public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance,
			Date startDate, Date endDate) {

		/*log.info(" wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww insurance "
				+ insurance + " aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa startDate"
				+ startDate + " tttttttttttttttttttttttttttt tttttttttendDate"
				+ endDate);*/

		Session session = sessionFactory.getCurrentSession();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		SQLQuery amount = session
				.createSQLQuery("SELECT sum(paid_amount) FROM moh_bill_recovery  where insurance_id = "
						+ insurance.getInsuranceId()
						+ " and start_period = '"
						+ formatter.format(startDate)
						+ "' and end_period = '"
						+ formatter.format(endDate) + "';");
		if (amount.list().size() > 0 && amount.list().get(0) != null) {
			return Float.valueOf(amount.list().get(0) + "");
		}
		return Float.valueOf(0 + "");
	}

	public List<Recovery> getAllPaidAmountPerInsuranceAndPeriod(
			Insurance insurance, Date startDate, Date endDate) {

		BillingService billingService = Context
				.getService(BillingService.class);
		Session session = sessionFactory.getCurrentSession();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		SQLQuery amount = session
				.createSQLQuery("SELECT recovery_id,insurance_id,start_period,end_period,paid_amount,payement_date FROM moh_bill_recovery  where insurance_id =  "
						+ insurance.getInsuranceId()
						+ " and start_period >= '"
						+ formatter.format(startDate)
						+ "' and end_period <= '"
						+ formatter.format(endDate) + "';");

		List<Object[]> amounts = amount.list();
		List<Recovery> recoveries = new ArrayList<Recovery>();

		for (Object[] ob : amounts) {
			Recovery recovery = new Recovery();

			recovery.setRecoveryId((Integer) ob[0]);
			int insuranceId = (Integer) ob[1];
			recovery.setInsuranceId(billingService.getInsurance(insuranceId));

			recovery.setStartPeriod((Date) ob[2]);
			recovery.setEndPeriod((Date) ob[3]);
			String paidAmountStr = ob[4].toString();
			recovery.setPaidAmount(Float.parseFloat(paidAmountStr));
			recovery.setPayementDate((Date) ob[5]);
			
			
			recoveries.add(recovery);
		}

		return recoveries;

	}
}
