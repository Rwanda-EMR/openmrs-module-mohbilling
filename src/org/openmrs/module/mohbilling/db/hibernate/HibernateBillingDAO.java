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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.model.ThirdParty;
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
		return (BillableService) sessionFactory.getCurrentSession()
				.createCriteria(BillableService.class)
				.add(Restrictions.eq("facilityServicePrice", price))
				.add(Restrictions.eq("insurance", insurance)).uniqueResult();
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
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getPatientBill(org.openmrs
	 *      .module.mohbilling.model.PatientBill)
	 */
	@Override
	public ThirdParty getThirdParty(Integer thirdPartyId) {

		return (ThirdParty) sessionFactory.getCurrentSession().get(
				ThirdParty.class, thirdPartyId);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#saveThirdParty(org.openmrs
	 *      .module.mohbilling.model.ThirdParty)
	 */
	@Override
	public void saveThirdParty(ThirdParty thirdParty) {

		sessionFactory.getCurrentSession().saveOrUpdate(thirdParty);
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
				.createCriteria(FacilityServicePrice.class)
				.add(Restrictions.eq("retired", false)).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllBillableServices()
	 */
	@Override
	public List<BillableService> getAllBillableServices() {
		return sessionFactory.getCurrentSession()
				.createCriteria(BillableService.class).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllThirdParties()
	 */
	@Override
	public List<ThirdParty> getAllThirdParties() {

		return sessionFactory.getCurrentSession()
				.createCriteria(ThirdParty.class)
				.add(Restrictions.eq("voided", false)).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#saveRecovery(org.openmrs.module.mohbilling.model.Recovery)
	 */
	@Override
	public void saveRecovery(Recovery recovery) {
		sessionFactory.getCurrentSession().saveOrUpdate(recovery);
	}

	@Override
	public Recovery getRecovery(Integer recoveryId) {

		return (Recovery) sessionFactory.getCurrentSession().get(
				Recovery.class, recoveryId);
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllRecoveries()
	 */
	@Override
	public List<Recovery> getAllRecoveries() {

		return sessionFactory.getCurrentSession()
				.createCriteria(Recovery.class)
				.add(Restrictions.eq("retired", false))
				.addOrder(Order.asc("startPeriod")).list();
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
				.createSQLQuery("SELECT recovery_id,insurance_id,start_period,"
						+ "end_period,paid_amount,payement_date "
						+ "FROM moh_bill_recovery  WHERE insurance_id =  "
						+ insurance.getInsuranceId() + " AND start_period >= '"
						+ formatter.format(startDate) + "' AND end_period <= '"
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
			recovery.setPaidAmount(new BigDecimal(Float
					.parseFloat(paidAmountStr)));
			recovery.setPaymentDate((Date) ob[5]);

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
	public List<PatientBill> billCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {

		Session session = sessionFactory.getCurrentSession();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder combinedSearch = new StringBuilder("");

		combinedSearch
				.append("SELECT pb.* FROM moh_bill_patient_bill pb "
						+ "inner join moh_bill_beneficiary b on pb.beneficiary_id = b.beneficiary_id ");

		if (patientId != null)
			combinedSearch
					.append(" and b.patient_id = " + patientId.intValue());

		combinedSearch
				.append(" inner join moh_bill_patient_service_bill psb on pb.patient_bill_id = psb.patient_bill_id and psb.voided = 0 "
						+ " inner join moh_bill_billable_service bs on psb.billable_service_id = bs.billable_service_id "
						+ " inner join moh_bill_insurance i on bs.insurance_id = i.insurance_id ");

		if (insurance != null)
			combinedSearch.append(" and i.insurance_id =  "
					+ insurance.getInsuranceId().intValue());

		combinedSearch.append(" WHERE 1 = 1 ");

		if (billCollector != null && !billCollector.equals(""))
			combinedSearch.append(" and pb.creator =  " + billCollector);

		if (startDate != null && endDate != null)
			combinedSearch.append(" and pb.created_date >= '"
					+ formatter.format(startDate)
					+ " 00:00:00' and pb.created_date <= '"
					+ formatter.format(endDate) + " 23:59:59' ");

		if (startDate != null && endDate == null)
			combinedSearch.append(" and pb.created_date >= '"
					+ formatter.format(startDate) + " 00:00:00' ");

		if (startDate == null && endDate != null)
			combinedSearch.append(" and pb.created_date <= '"
					+ formatter.format(endDate) + " 23:59:59' ");

		if (billStatus != null && !billStatus.equals(""))
			combinedSearch.append(" AND pb.status = '" + billStatus + "' ");

		combinedSearch.append(" GROUP BY pb.patient_bill_id;");

		log.info("tttttttttttttttttttttttttttttttbills "
				+ combinedSearch.toString());

		List<PatientBill> patientBills = session
				.createSQLQuery(combinedSearch.toString())
				.addEntity("pb", PatientBill.class).list();

		System.out.println("_____________________ BILL QUERY __________\n"
				+ combinedSearch.toString());
		return patientBills;
	}

	@Override
	public List<BillPayment> paymentsCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {
		Session session = sessionFactory.getCurrentSession();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder combinedSearch = new StringBuilder("");

		combinedSearch
				.append(" SELECT pay.* FROM moh_bill_payment pay "
						+ "inner join moh_bill_patient_bill pb on pay.patient_bill_id = pb.patient_bill_id "
						+ " INNER JOIN moh_bill_beneficiary b ON pb.beneficiary_id = b.beneficiary_id ");
		if (patientId != null)
			combinedSearch
					.append(" AND b.patient_id = " + patientId.intValue());

		combinedSearch
				.append(" inner join moh_bill_patient_service_bill psb on pb.patient_bill_id = psb.patient_bill_id "
						+ " inner join moh_bill_billable_service bs on psb.billable_service_id = bs.billable_service_id "
						+ " inner join moh_bill_insurance i on bs.insurance_id = i.insurance_id ");

		if (insurance != null)
			combinedSearch.append(" and i.insurance_id =  "
					+ insurance.getInsuranceId().intValue());

		combinedSearch
				.append(" inner join moh_bill_service_category sc on bs.service_category_id = sc.service_category_id WHERE 1 = 1");

		if (billCollector != null && !billCollector.equals(""))
			combinedSearch.append(" AND pay.creator =  " + billCollector);

		if (startDate != null && endDate != null)
			combinedSearch.append(" and pay.created_date >= '"
					+ formatter.format(startDate)
					+ "' and pay.created_date <= '" + formatter.format(endDate)
					+ "' ");

		if (startDate != null && endDate == null)
			combinedSearch.append(" and pay.created_date >= '"
					+ formatter.format(startDate) + "' ");

		if (startDate == null && endDate != null)
			combinedSearch.append(" and pay.created_date <= '"
					+ formatter.format(endDate) + "' ");

		// payment status
		if (billStatus != null && !billStatus.equals(""))
			if (!billStatus.equals("0"))// when it's "0" it does affect the
										// query...
				combinedSearch.append(" AND pb.status = '" + billStatus + "'");

		combinedSearch.append(" GROUP BY bill_payment_id");

		log.info("ttttttttttttttttttttttttttttttt " + combinedSearch.toString());

		List<BillPayment> billPayments = session
				.createSQLQuery(combinedSearch.toString())
				.addEntity("pay", BillPayment.class).list();

		System.out.println("_____________________ BILL QUERY __________\n"
				+ combinedSearch.toString());
		return billPayments;
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

	public BillableService getBillableService(Integer billableServiceId) {

		return (BillableService) sessionFactory.getCurrentSession().get(
				BillableService.class, billableServiceId);
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getBeneficiaryByPolicyNumber(java.lang.String)
	 */
	@Override
	public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber) {

		return (Beneficiary) sessionFactory.getCurrentSession()
				.createCriteria(Beneficiary.class)
				.add(Restrictions.eq("policyIdNumber", policyIdNumber))
				.uniqueResult();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillsByBeneficiary(org.openmrs.module.mohbilling.model.Beneficiary)
	 */
	@Override
	public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary)
			throws DAOException {

		return sessionFactory.getCurrentSession()
				.createCriteria(PatientBill.class)
				.add(Restrictions.eq("beneficiary", beneficiary)).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getInsurancePolicyByBeneficiary(org.openmrs.module.mohbilling.model.Beneficiary)
	 */
	@Override
	public InsurancePolicy getInsurancePolicyByBeneficiary(
			Beneficiary beneficiary) {

		return (InsurancePolicy) sessionFactory
				.getCurrentSession()
				.createCriteria(InsurancePolicy.class)
				.add(Restrictions.eq("insurancePolicyId", beneficiary
						.getInsurancePolicy().getInsurancePolicyId()))
				.uniqueResult();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getServiceCategory(java.lang.Integer)
	 */
	@Override
	public ServiceCategory getServiceCategory(Integer id) {

		return (ServiceCategory) sessionFactory.getCurrentSession().get(
				ServiceCategory.class, id);
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillableServiceByCategory(org.openmrs.module.mohbilling.model.ServiceCategory)
	 */
	@Override
	public List<BillableService> getBillableServiceByCategory(ServiceCategory sc) {

		return sessionFactory.getCurrentSession()
				.createCriteria(BillableService.class)
				.add(Restrictions.eq("serviceCategory", sc)).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getFacilityServiceByConcept(org.openmrs.Concept)
	 */
	@Override
	public FacilityServicePrice getFacilityServiceByConcept(Concept concept) {

		return (FacilityServicePrice) sessionFactory.getCurrentSession()
				.createCriteria(FacilityServicePrice.class)
				.add(Restrictions.eq("concept", concept)).uniqueResult();
	}

	@Override
	public List<BillableService> getBillableServicesByFacilityService(
			FacilityServicePrice fsp) {

		return sessionFactory.getCurrentSession()
				.createCriteria(BillableService.class)
				.add(Restrictions.eq("facilityServicePrice", fsp)).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillableServicesByInsurance(org.openmrs.module.mohbilling.model.Insurance)
	 */
	@Override
	public List<BillableService> getBillableServicesByInsurance(
			Insurance insurance) {

		return sessionFactory.getCurrentSession()
				.createCriteria(BillableService.class)
				.add(Restrictions.eq("insurance", insurance)).list();
	}

	/**
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getPolicyIdByPatient(java.lang.Integer)
	 */
	@Override
	public List<String[]> getPolicyIdByPatient(Integer patientId) {

		return (List<String[]>) sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"SELECT ins.name, b.policy_id_number"
								+ " FROM moh_bill_beneficiary b INNER JOIN moh_bill_insurance_policy ip"
								+ " ON b.insurance_policy_id = ip.insurance_policy_id INNER JOIN moh_bill_insurance ins"
								+ " ON ip.insurance_id = ins.insurance_id WHERE b.patient_id ="
								+ patientId
								+ " AND ins.voided = 0 AND ip.retired = 0 AND b.retired = 0;")
				.list();
	}

	public List<BillPayment> getAllBillPayments() {

		return sessionFactory.getCurrentSession()
				.createCriteria(BillPayment.class).list();

	}

	public List<BillPayment> getBillPaymentsByDateAndCollector(Date startDate,
			Date endDate, User collector) {

		List<BillPayment> paymentItems = new ArrayList<BillPayment>();
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		// DateFormat formatterTime = new SimpleDateFormat("HH:mm");
		Session session = sessionFactory.getCurrentSession();
		StringBuilder combinedSearch = new StringBuilder("");

		// works
		combinedSearch
				.append(" select amount_paid,date_received,collector from moh_bill_payment ");
		// works
		if (startDate != null && endDate != null && collector == null) {

			combinedSearch.append(" where date_received >='"
					+ formatter.format(startDate) + "' and date_received <='"
					+ formatter.format(endDate) + "'");

		}

		if (startDate != null && endDate == null && collector == null) {

			combinedSearch.append(" where date_received >='"
					+ formatter.format(startDate) + "'");

		}
		if (startDate == null && endDate != null && collector == null) {
			combinedSearch.append(" where date_received <='"
					+ formatter.format(endDate) + "'");

		}

		if (startDate == null && endDate == null && collector != null) {

			combinedSearch.append(" where collector =" + collector.getUserId()
					+ " ");

		}

		if (startDate != null && endDate != null && collector != null) {

			combinedSearch.append(" where date_received >='"
					+ formatter.format(startDate) + "' and date_received <='"
					+ formatter.format(endDate) + "' and collector ="
					+ collector.getUserId() + " ");

		}

		if (startDate != null && endDate == null && collector != null) {

			combinedSearch.append(" where date_received >='"
					+ formatter.format(startDate) + "' and collector ="
					+ collector.getUserId() + " ");

		}
		if (startDate == null && endDate != null && collector != null) {

			combinedSearch.append(" where date_received <='"
					+ formatter.format(endDate) + "' and collector ="
					+ collector.getUserId() + " ");

		}

		combinedSearch.append(" ; ");
		List<Object[]> paymentItem = session.createSQLQuery(
				combinedSearch.toString()).list();

		for (Object[] object : paymentItem) {

			BillPayment billPayment = new BillPayment();

			billPayment.setAmountPaid((BigDecimal) object[0]);

			billPayment.setCreatedDate((Date) object[1]);
			User user = Context.getUserService().getUser((Integer) object[2]);

			billPayment.setCollector(user);
			paymentItems.add(billPayment);

		}

		return paymentItems;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillableServiceByConcept(Concept
	 *      concept, Insurance insurance)
	 */
	@Override
	public ServiceCategory getServiceCategoryByName(String name,
			Insurance insurance) {
		return (ServiceCategory) sessionFactory.getCurrentSession()
				.createCriteria(ServiceCategory.class)
				.add(Restrictions.eq("name", name))
				.add(Restrictions.eq("insurance", insurance)).uniqueResult();
	}

	@Override
	public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate) {
		// TODO Auto-generated method stub
		  DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  Session session = getSessionFactory().getCurrentSession();  
		  SQLQuery query = session.createSQLQuery("SELECT date_received FROM moh_bill_payment" +
		    " where date_received between '" +sdf.format(startDate)+"' and '" +sdf.format(endDate)+"' ");
		  List<Date> dateList = query.list();  
		  return dateList;
			
	}

	

	@Override
	public List<PatientBill> getBills(Date startDate,Date endDate,User collector) {

		Criteria crit = sessionFactory.getCurrentSession().createCriteria(BillPayment.class).add(Restrictions.between("createdDate", startDate, endDate));
	
		if (collector != null && collector.getUserId() != null) {
			crit.add(Expression.eq("collector", collector));
		}
		
		
		
		
		List<BillPayment> payments = crit.list();
		
		List<PatientBill> bills = new ArrayList<PatientBill>();
		
		for (BillPayment pay : payments) {
			bills.add(pay.getPatientBill());
		}
	return bills;
	}
	

	@Override
	public Map<String,Double> getRevenueByService(Date receivedDate,String[] serviceCategory, String collector, Insurance insurance) {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		  Double amountSum =0.0;

		   LinkedHashMap<String, Double> mappedReport= new LinkedHashMap();
		  //Map<String, Double> mappedReport = new HashMap<String, Double>();
		   
		  Session session = getSessionFactory().getCurrentSession();
		  
		  for (String svceCatgory : serviceCategory) {
		   StringBuilder strb = new StringBuilder("");
//		   log.info(">>>>>>>>>start >sql query "+strb.toString());
		   
		   strb.append("SELECT fsp.category,SUM(((m.unit_price * m.quantity)*(100-ir.rate)/100)) " 
		     +" FROM moh_bill_patient_service_bill m " 
		     +" inner join moh_bill_billable_service bs on bs.billable_service_id =m.billable_service_id " 
		     +" inner join moh_bill_facility_service_price fsp on fsp.facility_service_price_id =bs.facility_service_price_id " 
		     +" inner join  moh_bill_patient_bill pb on pb.patient_bill_id=m.patient_bill_id " 
		     +" inner join moh_bill_beneficiary bn on pb.beneficiary_id =bn.beneficiary_id "
		     +" inner join  moh_bill_insurance_rate ir on ir.insurance_id=bs.insurance_id "      
		     +" inner join moh_bill_insurance_policy ip on ip.insurance_policy_id =bn.insurance_policy_id"
		     +" inner  join  moh_bill_payment pay on pay.patient_bill_id=pb.patient_bill_id " 
		     +" where fsp.category like  '%" +svceCatgory+"%' and pay.date_received = '" +formatter.format(receivedDate)+"' "); 
		
		   if (insurance != null)
				strb.append(" and ip.insurance_id =  "
						+ insurance.getInsuranceId().intValue());

			if (collector != null && !collector.equals(""))
				strb.append(" AND pay.collector =  " + collector);
		  
		   SQLQuery query = session.createSQLQuery(strb.toString());  
		 
		   List<Object[]> categoryReports = query.list(); 
		   
//		   DecimalFormat decimalFormat = new DecimalFormat("###.##");
		  
		   for (Object[] object : categoryReports) {		   
			   String catg =(String)object[0];     
			   Double amount =(Double )object[1];   
		   
		     if(catg!=null){             
		             mappedReport.put(catg, ReportsUtil.roundTwoDecimals(amount));
		             amountSum=amountSum+amount; 
		    }
		           if(catg==null){
		            
		            mappedReport.put(svceCatgory, 0.0); 
		            amountSum=amountSum+0.0; 
		   }
		                
		  }
		}
		  mappedReport.put("Total", ReportsUtil.roundTwoDecimals(amountSum));
		  
		  return mappedReport;
	}


	@Override
	public List<PatientBill> getPatientBillsByCollector(Date dateReceived, User collector) {
		// TODO Auto-generated method stub
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(BillPayment.class)
		         .add(Restrictions.eq("collector", collector))
				 .add(Restrictions.eq("dateReceived", dateReceived));
		
		//Criteria crit = sessionFactory.getCurrentSession().createCriteria(BillPayment.class).add(Restrictions.between("createdDate", startDate, endDate));
		
		List<BillPayment> payments = crit.list();		
		List<PatientBill> bills = new ArrayList<PatientBill>();		
		for (BillPayment pay : payments) {
			bills.add(pay.getPatientBill());
		}
	return bills;		
		
	}


	@Override
	public PatientBill getBills(Patient patient, Date startDate, Date endDate) {
		Session session = getSessionFactory().getCurrentSession();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String str = "SELECT pb.patient_bill_id FROM moh_bill_patient_bill pb " +
				" inner join moh_bill_beneficiary b on b.beneficiary_id=pb.beneficiary_id " +
				" and pb.created_date between '"+df.format(startDate)+" 00:00:00' and '"+df.format(endDate)+" 23:59:00' and b.patient_id="+patient.getPatientId();
		log.info("ssssssssssssssssssssssss "+str);
		SQLQuery query = session.createSQLQuery(str);
		List<Object> ob = query.list();
		PatientBill pb = null;
		if(ob!=null)
			pb=getPatientBill((Integer) ob.get(0));
		
		return pb;
	}

	@Override
	public InsuranceRate getInsuranceRateByInsurance(Insurance insurance) {
		// TODO Auto-generated method stub
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(InsuranceRate.class)
                 .add(Restrictions.eq("insurance",insurance));
		InsuranceRate insuranceRate = (InsuranceRate) crit.uniqueResult();		
		return insuranceRate;
	}


}
