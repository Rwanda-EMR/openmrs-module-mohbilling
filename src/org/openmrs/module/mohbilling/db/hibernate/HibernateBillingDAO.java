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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
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

	private Beneficiary getBeneficiary(Integer beneficiaryId) {
		return (Beneficiary) sessionFactory.getCurrentSession().get(
				Beneficiary.class, beneficiaryId);
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
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getInsurancePolicyByCardNo(java.lang.String)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo) {

		return (InsurancePolicy) sessionFactory.getCurrentSession()
				.createCriteria(InsurancePolicy.class)
				.add(Restrictions.eq("insuranceCardNo", insuranceCardNo))
				.uniqueResult();
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
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillableServiceByConcept(Concept
	 *      concept, Insurance insurance)
	 */
	@Override
	public BillableService getBillableServiceByConcept(
			FacilityServicePrice price, Insurance insurance) {
		return (BillableService) sessionFactory
				.getCurrentSession()
				.createCriteria(BillableService.class)
				.add(Restrictions.eq("facility_service_price_id",
						price.getFacilityServicePriceId()))
				.add(Restrictions.eq("insurance_id", insurance.getInsuranceId()))
				.uniqueResult();
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

		if (getInsurancePolicyByCardNo(card.getInsuranceCardNo()) == null)
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

		return sessionFactory.getCurrentSession()
				.createCriteria(InsurancePolicy.class).list();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllInsurances()
	 */
	@Override
	public List<Insurance> getAllInsurances() throws DAOException {

		return sessionFactory.getCurrentSession()
				.createCriteria(Insurance.class)
				.addOrder(Order.asc("category")).list();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllPatientBills()
	 */
	@Override
	public List<PatientBill> getAllPatientBills() throws DAOException {

		return sessionFactory.getCurrentSession()
				.createCriteria(PatientBill.class).list();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllFacilityServicePrices()
	 */
	@Override
	public List<ServiceCategory> getAllServiceCategories() throws DAOException {

		return sessionFactory.getCurrentSession()
				.createCriteria(ServiceCategory.class).list();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllFacilityServicePrices()
	 */
	@Override
	public List<FacilityServicePrice> getAllFacilityServicePrices()
			throws DAOException {

		return sessionFactory.getCurrentSession()
				.createCriteria(FacilityServicePrice.class).list();
	}

	@Override
	public List<BillableService> getAllBillableServices() {
		return sessionFactory.getCurrentSession()
				.createCriteria(BillableService.class).list();
	}

	@Override
	public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance,
			Date startDate, Date endDate) {

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
			recovery.setInsuranceId(getInsurance(insuranceId));

			recovery.setStartPeriod((Date) ob[2]);
			recovery.setEndPeriod((Date) ob[3]);
			String paidAmountStr = ob[4].toString();
			recovery.setPaidAmount(Float.parseFloat(paidAmountStr));
			recovery.setPayementDate((Date) ob[5]);

			recoveries.add(recovery);
		}

		return recoveries;

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#buildCohort(org.openmrs.module.mohbilling.model.Insurance,
	 *      java.util.Date, java.util.Date, java.lang.Integer, java.lang.String)
	 */
	@Override
	public List<PatientBill> buildCohort(Insurance insurance, Date startDate,
			Date endDate, Integer patientId, String serviceName) {

		List<PatientBill> bills = new ArrayList<PatientBill>();
		Session session = sessionFactory.getCurrentSession();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder combinedSearch = new StringBuilder("");

		combinedSearch
				.append("SELECT DISTINCT pb.patient_bill_id,pb.description,"
						+ "pb.amount,pb.printed,pb.is_paid,pb.created_date,pb.voided,"
						+ "pb.voided_date,pb.void_reason,pb.beneficiary_id,"
						+ "pb.voided_by,pb.creator FROM moh_bill_patient_bill pb ");
		combinedSearch
				.append("INNER JOIN moh_bill_patient_service_bill psb "
						+ "ON psb.patient_bill_id = pb.patient_bill_id "
						+ "INNER JOIN moh_bill_beneficiary b ON b.beneficiary_id = pb.beneficiary_id "
						+ "INNER JOIN moh_bill_billable_service bs ON bs.billable_service_id = psb.billable_service_id "
						+ "INNER JOIN moh_bill_service_category sc ON sc.service_category_id = bs.service_category_id "
						+ "INNER JOIN moh_bill_insurance i ON i.insurance_id = sc.insurance_id WHERE pb.voided = 0");

		if (insurance != null)
			combinedSearch.append(" AND i.insurance_id = "
					+ insurance.getInsuranceId().intValue());

		if (startDate != null && endDate != null)
			combinedSearch.append(" AND psb.service_date BETWEEN '"
					+ formatter.format(startDate) + "'" + " AND '"
					+ formatter.format(endDate) + "'");

		if (startDate != null && endDate == null)
			combinedSearch.append(" AND psb.service_date = '"
					+ formatter.format(startDate) + "'");

		if (startDate == null && endDate != null)
			combinedSearch.append(" AND psb.service_date = '"
					+ formatter.format(endDate) + "'");

		if (patientId != null)
			combinedSearch
					.append(" AND b.patient_id = " + patientId.intValue());

		if (serviceName != null && !serviceName.equals(""))
			combinedSearch.append(" AND sc.name LIKE '" + serviceName + "'");

		combinedSearch.append(";");

		List<Object[]> patientBills = session.createSQLQuery(
				combinedSearch.toString()).list();

		for (Object[] object : patientBills) {

			PatientBill bill = new PatientBill();

			bill.setPatientBillId((Integer) object[0]);
			bill.setDescription((String) object[1]);
			bill.setAmount((BigDecimal) object[2]);

			Short printed = null, isPaid = null;
			if (object[3] != null) {
				printed = (Short) object[3];
				if (printed.intValue() == 0)
					bill.setPrinted(false);
				else
					bill.setPrinted(true);
			}
			if (object[4] != null) {
				isPaid = (Short) object[4];
				if (isPaid.intValue() == 0)
					bill.setIsPaid(false);
				else
					bill.setIsPaid(true);
			}
			bill.setCreatedDate((Date) object[5]);
			bill.setVoided(false);
			bill.setVoidedDate(null);
			bill.setVoidReason(null);
			bill.setBeneficiary(getBeneficiary((Integer) object[9]));
			bill.setVoidedBy(null);
			bill.setCreator(Context.getUserService().getUser(
					(Integer) object[11]));

			bill.setBillItems(getBillItems(bill, serviceName));

			bills.add(bill);

		}
		return bills;
	}

	/**
	 * Gets all corresponding patient service bills matching the parameters
	 * 
	 * @param patientBill
	 * @param serviceName
	 * @return
	 */
	private Set<PatientServiceBill> getBillItems(PatientBill patientBill,
			String serviceName) {

		List<PatientServiceBill> services = new ArrayList<PatientServiceBill>();
		Session session = sessionFactory.getCurrentSession();
		StringBuilder combinedSearch = new StringBuilder("");

		combinedSearch
				.append("SELECT DISTINCT psb.patient_service_bill_id,psb.service_date,"
						+ "psb.unit_price,psb.quantity,psb.service_other,"
						+ "psb.service_other_description,psb.created_date,"
						+ "psb.voided,psb.voided_date,psb.void_reason,"
						+ "psb.billable_service_id,psb.patient_bill_id, "
						+ "psb.voided_by,psb.creator FROM moh_bill_patient_service_bill psb ");
		combinedSearch
				.append(" INNER JOIN moh_bill_patient_bill pb ON pb.patient_bill_id = psb.patient_bill_id"
						+ " INNER JOIN moh_bill_billable_service bs ON bs.billable_service_id = psb.billable_service_id "
						+ " INNER JOIN moh_bill_service_category sc ON sc.service_category_id = bs.service_category_id "
						+ " WHERE psb.voided = 0");

		if (serviceName != null && !serviceName.equals(""))
			combinedSearch.append(" AND sc.name LIKE '" + serviceName + "'");

		if (patientBill != null)
			combinedSearch.append(" AND psb.patient_bill_id = "
					+ patientBill.getPatientBillId());

		combinedSearch.append(";");

		List<Object[]> billItems = session.createSQLQuery(
				combinedSearch.toString()).list();

		for (Object[] obj : billItems) {
			PatientServiceBill psBill = new PatientServiceBill();

			psBill.setPatientServiceBillId((Integer) obj[0]);
			psBill.setServiceDate((Date) obj[1]);
			psBill.setUnitPrice((BigDecimal) obj[2]);
			psBill.setQuantity((Integer) obj[3]);
			psBill.setServiceOther((String) obj[4]);
			psBill.setServiceOtherDescription((String) obj[5]);
			psBill.setCreatedDate((Date) obj[6]);
			psBill.setVoided(false);
			psBill.setVoidedDate(null);
			psBill.setVoidReason(null);
			psBill.setService(getBillableService((Integer) obj[10]));
			psBill.setPatientBill(getPatientBill((Integer) obj[11]));
			psBill.setVoidedBy(null);
			psBill.setCreator(Context.getUserService().getUser(
					(Integer) obj[13]));

			services.add(psBill);
		}

		Set<PatientServiceBill> items = new HashSet<PatientServiceBill>(
				services);

		return items;
	}

	private BillableService getBillableService(Integer billableServiceId) {
		return (BillableService) sessionFactory.getCurrentSession().get(
				BillableService.class, billableServiceId);
	}
}
