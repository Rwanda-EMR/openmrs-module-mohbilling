/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mohbilling.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.db.BillingDAO;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author EMR@RBC
 *
 */
@SuppressWarnings("unchecked")
public class HibernateBillingDAO implements BillingDAO {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    private SessionFactory sessionFactory;

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory
     *            the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * (non-Javadoc)
     *
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getInsurance(org.openmrs .
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
     * @see org.openmrs.module.mohbilling.db.BillingDAO
     * #getInsurancePolicy(org.openmrs.module.mohbilling.model.InsurancePolicy)
     */
    @Override
    public InsurancePolicy getInsurancePolicy(Integer cardId) {

        return (InsurancePolicy) sessionFactory.getCurrentSession().get(
                InsurancePolicy.class, cardId);
    }

    /**
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getInsurancePolicyByCardNo(String)
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
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getFacilityServicePrice(Integer)
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
                .add(Restrictions.eq("retired", false))
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

        //if (getInsurancePolicyByCardNo(card.getInsuranceCardNo()) == null)
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
     *      Date, Date, Integer, String)
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


        List<BillPayment> billPayments = session
                .createSQLQuery(combinedSearch.toString())
                .addEntity("pay", BillPayment.class).list();


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
            psBill.setQuantity((BigDecimal) obj[3]);
            psBill.setServiceOther((String) obj[4]);
            psBill.setServiceOtherDescription((String) obj[5]);
            psBill.setCreatedDate((Date) obj[6]);
            psBill.setVoided(false);
            psBill.setVoidedDate(null);
            psBill.setVoidReason(null);
            psBill.setService(getBillableService((Integer) obj[10]));
            //psBill.setPatientBill(getPatientBill((Integer) obj[11]));
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
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getBeneficiaryByPolicyNumber(String)
     */
    @Override
    public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber) {

        return (Beneficiary) sessionFactory.getCurrentSession()
                .createCriteria(Beneficiary.class)
                .add(Restrictions.eq("policyIdNumber", policyIdNumber))
                .uniqueResult();
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
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getServiceCategory(Integer)
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
                .add(Restrictions.eq("retired", false))
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
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getPolicyIdByPatient(Integer)
     */
    @Override
    public List<String[]> getPolicyIdByPatient(Integer patientId) {

        return (List<String[]>) sessionFactory
                .getCurrentSession()
                .createSQLQuery(
                        "SELECT ins.name, b.policy_id_number,b.insurance_policy_id"
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

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillPaymentsByDateAndCollector(java.util.Date, java.util.Date, org.openmrs.User)
     */
    public List<BillPayment> getBillPaymentsByDateAndCollector(Date startDate, Date endDate, User collector) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(BillPayment.class);
        crit.add(Restrictions.between("dateReceived", startDate, endDate));
        crit.add(Restrictions.eq("voided", false));
        if (collector != null)
            crit.add(Restrictions.eq("collector", collector));

        return crit.list();

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
                " where date_received between '" + sdf.format(startDate) + "' and '" + sdf.format(endDate) + "' ");
        List<Date> dateList = query.list();
        return dateList;

    }


    @Override
    public Object[] getBills(Date startDate, Date endDate, User collector) {

        Criteria crit = sessionFactory.getCurrentSession().createCriteria(BillPayment.class).add(Restrictions.between("createdDate", startDate, endDate));
        //Criteria crit1 = sessionFactory.getCurrentSession().createCriteria(PatientBill.class).add(Restrictions.between("createdDate", startDate, endDate));

        if (collector != null && collector.getUserId() != null) {
            crit.add(Expression.eq("collector", collector));
        }

        List<BillPayment> payments = crit.list();
        List<PatientBill> bills = crit.list();

        Set<PatientBill> patientBillsFullPaids = new HashSet<PatientBill>();
        Double fullReceived = 0.0;
        Double allPartiallypaid = 0.0;

        for (BillPayment pay : payments) {
            //full paid patient bill

            PatientBill pb = pay.getPatientBill();
            if (pb.getStatus().equals("FULLY PAID")) {
                patientBillsFullPaids.add(pay.getPatientBill());

                fullReceived = fullReceived + pay.getAmountPaid().doubleValue();
            }
            if (pb.getStatus().equals("PARTLY PAID")) {
                allPartiallypaid = allPartiallypaid + pay.getAmountPaid().doubleValue();

            }

        }


        //facture object compiled for all facture and set all paid bills at
        //index 0 and received amount .for
        Object[] factureCompiled = new Object[]{patientBillsFullPaids, fullReceived, allPartiallypaid};

        return factureCompiled;
    }


    @Override
    public Map<String, Double> getRevenueByService(Date receivedDate, String[] serviceCategory, String collector, Insurance insurance) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Double amountSum = 0.0;

        LinkedHashMap<String, Double> mappedReport = new LinkedHashMap();
        //Map<String, Double> mappedReport = new HashMap<String, Double>();

        Session session = getSessionFactory().getCurrentSession();

        for (String svceCatgory : serviceCategory) {
            StringBuilder strb = new StringBuilder("");
//		   log.info(">>>>>>>>>start >sql query "+strb.toString());

            strb.append("SELECT fsp.category,SUM(((m.unit_price * m.quantity)*(100-ir.rate)/100)) "
                    + " FROM moh_bill_patient_service_bill m "
                    + " inner join moh_bill_billable_service bs on bs.billable_service_id =m.billable_service_id "
                    + " inner join moh_bill_facility_service_price fsp on fsp.facility_service_price_id =bs.facility_service_price_id "
                    + " inner join  moh_bill_patient_bill pb on pb.patient_bill_id=m.patient_bill_id "
                    + " inner join moh_bill_beneficiary bn on pb.beneficiary_id =bn.beneficiary_id "
                    + " inner join  moh_bill_insurance_rate ir on ir.insurance_id=bs.insurance_id "
                    + " inner join moh_bill_insurance_policy ip on ip.insurance_policy_id =bn.insurance_policy_id"
                    + " inner  join  moh_bill_payment pay on pay.patient_bill_id=pb.patient_bill_id "
                    + " where fsp.category like  '%" + svceCatgory + "%' and pay.date_received = '" + formatter.format(receivedDate) + "' ");

            if (insurance != null)
                strb.append(" and ip.insurance_id =  "
                        + insurance.getInsuranceId().intValue());

            if (collector != null && !collector.equals(""))
                strb.append(" AND pay.collector =  " + collector);

            SQLQuery query = session.createSQLQuery(strb.toString());

            List<Object[]> categoryReports = query.list();

//		   DecimalFormat decimalFormat = new DecimalFormat("###.##");

            for (Object[] object : categoryReports) {
                String catg = (String) object[0];
                Double amount = (Double) object[1];

                if (catg != null) {
                    mappedReport.put(catg, ReportsUtil.roundTwoDecimals(amount));
                    amountSum = amountSum + amount;
                }
                if (catg == null) {

                    mappedReport.put(svceCatgory, 0.0);
                    amountSum = amountSum + 0.0;
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
                " and pb.created_date between '" + df.format(startDate) + " 00:00:00' and '" + df.format(endDate) + " 23:59:00' and b.patient_id=" + patient.getPatientId();

        SQLQuery query = session.createSQLQuery(str);
        List<Object> ob = query.list();
        PatientBill pb = null;
        if (ob != null)
            pb = getPatientBill((Integer) ob.get(0));

        return pb;
    }

    @Override
    public InsuranceRate getInsuranceRateByInsurance(Insurance insurance) {
        // TODO Auto-generated method stub
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(InsuranceRate.class)
                .add(Restrictions.eq("insurance", insurance));
        crit.add(Expression.eq("retired", false));

        InsuranceRate insuranceRate = (InsuranceRate) crit.uniqueResult();
        return insuranceRate;
    }

    @Override
    public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo) {
        // TODO Auto-generated method stub

        Criteria crit = sessionFactory.getCurrentSession().createCriteria(Beneficiary.class)
                .add(Restrictions.eq("policyIdNumber", cardNo));

        return crit.list();
    }

    @Override
    public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(
            Date startDate, Date endDate) {
        // TODO Auto-generated method stub

        Criteria crit = sessionFactory.getCurrentSession().createCriteria(InsurancePolicy.class).add(Restrictions.between("createdDate", startDate, endDate));

        return crit.list();

    }

    public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary, Date startDate, Date endDate) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientBill.class).add(Restrictions.between("createdDate", startDate, endDate));

        if (beneficiary != null) {
            crit.add(Expression.eq("beneficiary", beneficiary));
        }
        return crit.list();
    }

    @Override
    public void loadBillables(Insurance insurance) {

        boolean alreadyExecuted = false;
//		if(!alreadyExecuted){
        Insurance rama = Context.getService(BillingService.class).getInsurance(2);
        //Criteria crit = sessionFactory.getCurrentSession().createCriteria(ServiceCategory.class).add(Restrictions.eq("insurance", rama));

        //List<ServiceCategory> ramaSC = crit.list();
        List<Object[]> ramaSC = sessionFactory.getCurrentSession().createSQLQuery("select distinct name,description from moh_bill_hop_service").list();
        // map service category to insurance
        System.out.println("HOP Servises size: " + ramaSC.size());
        List<ServiceCategory> serviceCategoryCheckList = Context.getService(BillingService.class).getAllServiceCategories();
        for (Object[] sc : ramaSC) {
            ServiceCategory scToMapToInsurance = new ServiceCategory();
            //scToMapToInsurance.setName(sc.getName());
            scToMapToInsurance.setName(sc[0].toString());
            //scToMapToInsurance.setDescription(sc.getDescription());
            scToMapToInsurance.setDescription(sc[1].toString());
            scToMapToInsurance.setCreatedDate(new Date());
            scToMapToInsurance.setRetired(false);
            scToMapToInsurance.setInsurance(insurance);
            scToMapToInsurance.setCreator(Context.getAuthenticatedUser());
            for (ServiceCategory scExisting : serviceCategoryCheckList) {
                if (!(scExisting.getName().toString().equals(sc[0].toString()) && scExisting.getInsurance().getInsuranceId() == insurance.getInsuranceId())) {
                    insurance.addServiceCategory(scToMapToInsurance);
                }
            }
            //System.out.println("Before Saving an Insurance :"+sc[0].toString());
            //insurance.addServiceCategory(scToMapToInsurance);
            Context.getService(BillingService.class).saveInsurance(insurance);
        }
        List<Object[]> baseBillableServices = getBaseBillableServices(insurance);
        List<Object[]> basePharmacyItems = getPharmacyBaseBillableServices(insurance);
        //retrieve billables(acts) from RAMA and add them on new insurance
        BillableService newBS = null;
        for (Object[] b : baseBillableServices) {
            newBS = new BillableService();
            newBS.setInsurance(Context.getService(BillingService.class).getInsurance((Integer) b[0]));
            newBS.setMaximaToPay((BigDecimal) b[1]);
            newBS.setStartDate((Date) b[2]);
            Integer fspId = (Integer) b[3];
            FacilityServicePrice fsp = FacilityServicePriceUtil.getFacilityServicePrice(fspId);
            newBS.setFacilityServicePrice(fsp);
            ServiceCategory sc = getServiceCategory((Integer) b[4]);
            newBS.setServiceCategory(sc);
            newBS.setCreatedDate(new Date());
            newBS.setRetired(false);
            newBS.setCreator(Context.getAuthenticatedUser());
            InsuranceUtil.saveBillableService(newBS);
        }

        //retrieve billables(Pharmacy items) from RAMA and add them on new insurance
        //BillableService newBS = null;
        for (Object[] b : basePharmacyItems) {
            newBS = new BillableService();
            newBS.setInsurance(Context.getService(BillingService.class).getInsurance((Integer) b[0]));
            newBS.setMaximaToPay((BigDecimal) b[1]);
            newBS.setStartDate((Date) b[2]);
            Integer fspId = (Integer) b[3];
            FacilityServicePrice fsp = FacilityServicePriceUtil.getFacilityServicePrice(fspId);
            newBS.setFacilityServicePrice(fsp);
            ServiceCategory sc = getServiceCategory((Integer) b[4]);
            newBS.setServiceCategory(sc);
            newBS.setCreatedDate(new Date());
            newBS.setRetired(false);
            newBS.setCreator(Context.getAuthenticatedUser());
            InsuranceUtil.saveBillableService(newBS);
        }

    }

    @Override
    public List<Object[]> getBaseBillableServices(Insurance i) {
        Session session = getSessionFactory().getCurrentSession();

        StringBuilder bui = new StringBuilder();
        bui.append("select i.insurance_id,");
        bui.append(" CASE ");
        bui.append("");
		/*bui.append(" WHEN i.category = 'MUTUELLE' THEN (full_price/2)");
		bui.append(" WHEN i.category = 'PRIVATE' THEN (full_price*1.25)");
		bui.append(" WHEN i.category = 'NONE' THEN (full_price*1.5)");*/
        bui.append(" WHEN i.category = 'MUTUELLE' THEN (CEIL(full_price/2))");
        bui.append(" WHEN i.category = 'RSSB' THEN (CEIL(full_price*1.25))");
        bui.append(" WHEN i.category = 'MMI_UR' THEN (CEIL(full_price*1.15))");
        bui.append(" WHEN i.category = 'PRIVATE' THEN (CEIL(full_price*1.4375))");
        bui.append(" WHEN i.category = 'NONE' THEN (CEIL(full_price*1.725))");
        bui.append(" ELSE full_price END as maxima_to_pay,");
        bui.append(" fsp.start_date, fsp.facility_service_price_id, sc.service_category_id, fsp.created_date, fsp.retired, fsp.creator ");
        bui.append(" FROM moh_bill_facility_service_price fsp ");
        bui.append(" inner join moh_bill_service_category sc on fsp.category = sc.name ");
        bui.append(" inner join moh_bill_insurance i on sc.insurance_id = i.insurance_id");
        bui.append(" WHERE fsp.category not in ('MEDICAMENTS', 'CONSOMMABLES') and fsp.retired = '0' and i.insurance_id in(" + i.getInsuranceId() + ")");


//		log.info("ssssssssssssssssssssssss "+bui.toString());

        SQLQuery query = session.createSQLQuery(bui.toString());
        List<Object[]> ob = query.list();

        return ob;
    }

    @Override
    public List<Object[]> getPharmacyBaseBillableServices(Insurance i) {
        Session session = getSessionFactory().getCurrentSession();

        StringBuilder bui = new StringBuilder();
        bui.append("select i.insurance_id,full_price,");
        bui.append(" fsp.start_date, fsp.facility_service_price_id, sc.service_category_id, fsp.created_date, fsp.retired, fsp.creator ");
        bui.append(" FROM moh_bill_facility_service_price fsp ");
        bui.append(" inner join moh_bill_service_category sc on fsp.category = sc.name ");
        bui.append(" inner join moh_bill_insurance i on sc.insurance_id = i.insurance_id");
        bui.append(" WHERE fsp.category in ('MEDICAMENTS', 'CONSOMMABLES') and fsp.retired = '0' and i.insurance_id in(" + i.getInsuranceId() + ")");

//		log.info("ssssssssssssssssssssssss "+bui.toString());

        SQLQuery query = session.createSQLQuery(bui.toString());
        List<Object[]> ob = query.list();

        return ob;
    }


    @Override
    public Set<PatientBill> getRefundedBills(Date startDate, Date endDate, User collector) {

        Criteria crit = sessionFactory.getCurrentSession().createCriteria(BillPayment.class).add(Restrictions.between("createdDate", startDate, endDate));
        //Criteria crit1 = sessionFactory.getCurrentSession().createCriteria(PatientBill.class).add(Restrictions.between("createdDate", startDate, endDate));

        if (collector != null && collector.getUserId() != null) {
            crit.add(Expression.eq("collector", collector));
        }

        List<BillPayment> payments = crit.list();

        Set<PatientBill> refundedBills = new HashSet<PatientBill>();


        for (BillPayment pay : payments) {

            PatientBill pb = pay.getPatientBill();
            //if(pb.getBillItems().size()==0)
            refundedBills.add(pb);

        }

        return refundedBills;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#savesaveDepartement(org.openmrs.module.mohbilling.model.Department)
     */
    @Override
    public Department saveDepartement(Department departement) {

        sessionFactory.getCurrentSession().saveOrUpdate(departement);
        return departement;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getDepartement(java.lang.Integer)
     */
    @Override
    public Department getDepartement(Integer departementId) {
        return (Department) sessionFactory.getCurrentSession().get(Department.class, departementId);
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllDepartements()
     */
    @Override
    public List<Department> getAllDepartements() {
        return sessionFactory.getCurrentSession().createCriteria(Department.class).list();
    }

    @Override
    public HopService saveHopService(HopService service) {
        // TODO Auto-generated method stub
        sessionFactory.getCurrentSession().saveOrUpdate(service);
        return service;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllHopServicesByDepartement(org.openmrs.module.mohbilling.model.Department)
     */
    @Override
    public List<HopService> getAllHopService() {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(HopService.class);
        return criteria.list();
    }

    @Override
    public HopService getHopService(Integer serviceId) {
        return (HopService) sessionFactory.getCurrentSession().get(HopService.class, serviceId);
    }

    @Override
    public HopService getHopService(String name) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(HopService.class)
                .add(Restrictions.eq("name", name));

        HopService hopService = (HopService) crit.uniqueResult();
        return hopService;

    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#saveAdmission(org.openmrs.module.mohbilling.model.Admission)
     */
    @Override
    public Admission saveAdmission(Admission admission) {

        sessionFactory.getCurrentSession().saveOrUpdate(admission);
        return admission;
    }

    public Admission getPatientAdmission(Integer admissionid) {
        return (Admission) sessionFactory.getCurrentSession().get(Admission.class, admissionid);
    }

    @Override
    public GlobalBill saveGlobalBill(GlobalBill globalBill) {
        sessionFactory.getCurrentSession().saveOrUpdate(globalBill);
        return globalBill;
    }

    @Override
    public GlobalBill GetGlobalBill(Integer globalBillId) {
        return (GlobalBill) sessionFactory.getCurrentSession().get(GlobalBill.class, globalBillId);
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getGlobalBillByAdmission(org.openmrs.module.mohbilling.model.Admission)
     */
    @Override
    public GlobalBill getGlobalBillByAdmission(Admission admission) {

        Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class)
                .add(Restrictions.eq("admission", admission));

        GlobalBill globalBill = (GlobalBill) crit.uniqueResult();
        return globalBill;
    }

    @Override
    public List<Admission> getAdmissionsListByInsurancePolicy(InsurancePolicy ip) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(Admission.class)
                .add(Expression.eq("insurancePolicy", ip));
        return crit.list();
    }

    @Override
    public void saveConsommation(Consommation consommation) {
        sessionFactory.getCurrentSession().saveOrUpdate(consommation);
    }

    @Override
    public void saveInsuranceBill(InsuranceBill ib) {
        sessionFactory.getCurrentSession().saveOrUpdate(ib);

    }

    @Override
    public void saveThirdPartyBill(ThirdPartyBill thirdBill) {
        sessionFactory.getCurrentSession().saveOrUpdate(thirdBill);
    }

    @Override
    public Consommation getConsommation(Integer consommationId) {
        return (Consommation) sessionFactory.getCurrentSession().get(Consommation.class, consommationId);
    }

    @Override
    public CashPayment saveCashPayment(CashPayment cashPayment) {
        sessionFactory.getCurrentSession().saveOrUpdate(cashPayment);
        return cashPayment;
    }

    @Override
    public PatientServiceBill saveBilledItem(PatientServiceBill psb) {
        sessionFactory.getCurrentSession().saveOrUpdate(psb);
        return psb;
    }

    @Override
    public PatientServiceBill getPatientServiceBill(Integer patientServiceBillId) {
        return (PatientServiceBill) sessionFactory.getCurrentSession().get(PatientServiceBill.class, patientServiceBillId);
    }

    @Override
    public void getPatientServiceBill(BillPayment bp) {
        sessionFactory.getCurrentSession().saveOrUpdate(bp);

    }

    @Override
    public void savePaidServiceBill(PaidServiceBill paidSb) {
        sessionFactory.getCurrentSession().saveOrUpdate(paidSb);

    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllConsommationByGlobalBill(org.openmrs.module.mohbilling.model.GlobalBill)
     */
    @Override
    public List<Consommation> getAllConsommationByGlobalBill(GlobalBill globalBill) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(Consommation.class);
        crit.add(Restrictions.eq("globalBill", globalBill));
        // log.info("jjjjjjjjjjJJJJJJJJJJJJJJJJJJJJJJJJJJ "+crit.list());
        return crit.list();
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getGlobalBillByBillIdentifier(java.lang.String)
     */
    @Override
    public GlobalBill getGlobalBillByBillIdentifier(String billIdentifier) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class)
                .add(Restrictions.eq("billIdentifier", billIdentifier));

        GlobalBill globalBill = (GlobalBill) crit.uniqueResult();
        return globalBill;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getConsommationsByBeneficiary(org.openmrs.module.mohbilling.model.Beneficiary)
     */
    @Override
    public List<Consommation> getConsommationsByBeneficiary(Beneficiary beneficiary) {
        return sessionFactory.getCurrentSession().createCriteria(Consommation.class)
                .add(Restrictions.eq("beneficiary", beneficiary)).list();
    }


    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#savePatientAccount(org.openmrs.module.mohbilling.model.PatientAccount)
     */
    @Override
    public void savePatientAccount(PatientAccount account) {
        sessionFactory.getCurrentSession().saveOrUpdate(account);
    }

    @Override
    public PatientAccount getPatientAccount(Integer accountId) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientAccount.class)
                .add(Restrictions.eq("patientAccountId", accountId));

        PatientAccount account = (PatientAccount) crit.uniqueResult();
        return account;
    }

    @Override
    public PatientAccount getPatientAccount(Patient patient) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientAccount.class)
                .add(Restrictions.eq("patient", patient));

        PatientAccount account = (PatientAccount) crit.uniqueResult();
        return account;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getBillPayment(java.lang.Integer)
     */
    @Override
    public BillPayment getBillPayment(Integer paymentId) {
        return (BillPayment) sessionFactory.getCurrentSession().get(BillPayment.class, paymentId);
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getPaidServices(org.openmrs.module.mohbilling.model.BillPayment)
     */
    @Override
    public List<PaidServiceBill> getPaidServices(BillPayment payment) {
        return sessionFactory.getCurrentSession().createCriteria(PaidServiceBill.class)
                .add(Restrictions.eq("billPayment", payment))
                .add(Restrictions.eq("voided", false))
                .list();
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getConsommationByPatientBill(org.openmrs.module.mohbilling.model.PatientBill)
     */
    @Override
    public Consommation getConsommationByPatientBill(PatientBill patientBill) {

        return (Consommation) sessionFactory.getCurrentSession()
                .createCriteria(Consommation.class)
                .add(Restrictions.eq("patientBill", patientBill))
                .uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#savePaymentRefund(org.openmrs.module.mohbilling.model.PaymentRefund)
     */
    @Override
    public PaymentRefund savePaymentRefund(PaymentRefund refund) {
        sessionFactory.getCurrentSession().saveOrUpdate(refund);
        return refund;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getPaidServiceBill(java.lang.Integer)
     */
    @Override
    public PaidServiceBill getPaidServiceBill(Integer paidSviceBillid) {
        return (PaidServiceBill) sessionFactory.getCurrentSession().get(PaidServiceBill.class, paidSviceBillid);
    }

    @Override
    public DepositPayment saveDepositPayment(DepositPayment depositPayment) {
        sessionFactory.getCurrentSession().saveOrUpdate(depositPayment);
        return depositPayment;
    }

    @Override
    public List<HopService> getHospitalServicesByDepartment(
            Department department) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(ServiceCategory.class)
                .add(Expression.eq("department", department));
        crit.setProjection(Projections.distinct(Projections.property("hopService")));
        return crit.list();
    }

    public Set<Transaction> getTransactions(PatientAccount acc,
                                            Date startDate, Date endDate, String reason) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(Transaction.class);

        if (acc != null) {
            crit.add(Expression.eq("patientAccount", acc));
        }
        if (reason != null) {
            crit.add(Expression.eq("reason", reason));
        }
        if (startDate != null) {
            crit.add(Expression.ge("transactionDate", startDate));
        }
        if (endDate != null) {
            crit.add(Expression.le("transactionDate", endDate));
        }
        crit.addOrder(Order.desc("transactionDate"));
        //crit.list() is a set, the following codes serve to convert the set to the list
        //a set cannot be ordered on display
        //List<Transaction> list = new ArrayList<Transaction>(crit.list());
        return (Set<Transaction>) crit.list();

    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getTransactionById(java.lang.Integer)
     */
    @Override
    public Transaction getTransactionById(Integer id) {
        return (Transaction) sessionFactory.getCurrentSession().get(Transaction.class, id);
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getServiceByName(java.lang.String)
     */
    public HopService getServiceByName(String name) {
        return (HopService) sessionFactory.getCurrentSession()
                .createCriteria(HopService.class)
                .add(Restrictions.eq("name", name))
                .uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getPaidItemsByBillPayments(java.util.List)
     */
    @Override
    public List<PaidServiceBill> getPaidItemsByBillPayments(List<BillPayment> payments) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PaidServiceBill.class);
        criteria.add(Restrictions.in("billPayment", payments));
        criteria.add(Restrictions.eq("voided", false));
        List<PaidServiceBill> paidItems = new ArrayList<PaidServiceBill>();
        paidItems = criteria.list();

        Consommation c = ConsommationUtil.getConsommationByPatientBill(payments.get(0).getPatientBill());
        if (c.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            paidItems = BillPaymentUtil.getOldPaidItems(payments);
        }
        //List<PaidServiceBill> oldItems = BillPaymentUtil.getOldPaidItems(payments);
//	      		if(oldItems!=null){
//	      				for (PaidServiceBill ps : oldItems) {
//	      					Consommation consommation = ps.getBillItem().getConsommation();
//	    	      			if(consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
//	    	      				paidItems.add(ps);
//						     }
//	      			     }
//		         }
        return paidItems;

    }

    @Override
    public List<PatientServiceBill> getBillItemsByCategory(
            Consommation consommation, HopService service) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientServiceBill.class);
        if (consommation != null)
            crit.add(Expression.eq("consommation", consommation));
        if (service != null)
            crit.add(Expression.eq("hopService", service));

        return crit.list();
    }

    @Override
    public List<PatientServiceBill> getBillItemsByGroupedCategories(
            Consommation consommation, List<HopService> services) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientServiceBill.class);
        criteria.add(Restrictions.eq("consommation", consommation));
        criteria.add(Restrictions.in("hopService", services));
        return criteria.list();
    }

    @Override
    public List<GlobalBill> getGlobalBills(Date date1, Date date2, Insurance insurance) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class)
                .add(Restrictions.between("createdDate", date1, date2))
                .add(Restrictions.eq("closed", true))
                .add(Restrictions.eq("insurance", insurance))
                .addOrder(Order.asc("closingDate"));
        return crit.list();
    }

    @Override
    public InsuranceReport getBillItemsByCategoryFromMamba(Integer insuranceIdentifier, Date startDate, Date endDate) {

        System.out.println("parameters for sp insurance : " + insuranceIdentifier);
        System.out.println("parameters for sp start_date: " + startDate);
        System.out.println("parameters for sp end_date  : " + endDate);

        System.out.println("Starting.. to Fetch items from MambaETL tables");

        InsuranceReport report = new InsuranceReport();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        long startTime = System.nanoTime();
        SQLQuery billingReportQuery = sessionFactory.getCurrentSession().createSQLQuery(
                "CALL sp_mamba_fact_insurance_report_query(:insurance_id, :start_date, :end_date)");

        long endTime = System.nanoTime();
        double elapsedTimeInSeconds = (endTime - startTime) / 1e9; // Convert nanoseconds to seconds

        billingReportQuery.setParameter("insurance_id", insuranceIdentifier);
        billingReportQuery.setParameter("start_date", startDate);
        billingReportQuery.setParameter("end_date", endDate);
        //TODO: Create a hibernate Object for this result-set type
        List<Object[]> resultSet = billingReportQuery.list();

        System.out.println("It took MambaETL: " + elapsedTimeInSeconds + " seconds to retrieve: " + resultSet.size() + " items");


        Insurance insurance = InsuranceUtil.getInsurance(insuranceIdentifier);
        InsuranceRate insuranceRate = insurance.getCurrentRate();
        Float insuranceFirmRate = insuranceRate.getRate();
        Float insurancePatientRate = 100 - insuranceRate.getRate();

        //Double totalInsuranceFirm = 0.9 * total;

        for (Object[] objects : resultSet) {

            Integer id = (objects[0] != null) ? Integer.parseInt(objects[0].toString()) : null;
            Date admissionDate = null;
            try {
                admissionDate = (objects[1] != null) ? dateFormat.parse(objects[1].toString().substring(0, 10)) : null;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date closingDate = null;
            try {
                closingDate = (objects[2] != null) ? dateFormat.parse(objects[2].toString().substring(0, 10)) : null;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String beneficiaryName = (objects[3] != null) ? objects[3].toString() : null;
            String houseHoldHeadName = (objects[4] != null) ? objects[4].toString() : null;
            String familyCode = (objects[5] != null) ? objects[5].toString() : null;
            Integer beneficiaryLevel = (objects[6] != null) ? Integer.parseInt(objects[6].toString()) : null;
            String cardNumber = (objects[7] != null) ? objects[7].toString() : null;
            String companyName = (objects[8] != null) ? objects[8].toString() : null;
            Integer age = (objects[9] != null) ? Integer.parseInt(objects[9].toString()) : null;
            Date birthDate = null;
            try {
                birthDate = (objects[10] != null) ? dateFormat.parse(objects[10].toString().substring(0, 10)) : null;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String gender = (objects[11] != null) ? objects[11].toString() : null;
            String doctorName = (objects[12] != null) ? objects[12].toString() : null;
            Integer insuranceId = (objects[13] != null) ? Integer.parseInt(objects[13].toString()) : null;
            Integer globalBillId = (objects[14] != null) ? Integer.parseInt(objects[14].toString()) : null;
            String globalBillIdentifier = (objects[15] != null) ? objects[15].toString() : null;

            //services
            Double medicament = (objects[16] != null) ? Double.parseDouble(objects[16].toString()) : 0;
            Double consultation = (objects[17] != null) ? Double.parseDouble(objects[17].toString()) : 0;
            Double hospitalisation = (objects[18] != null) ? Double.parseDouble(objects[18].toString()) : 0;
            Double laboratoire = (objects[19] != null) ? Double.parseDouble(objects[19].toString()) : 0;
            Double formaliteAdministratives = (objects[20] != null) ? Double.parseDouble(objects[20].toString()) : 0;
            Double ambulance = (objects[21] != null) ? Double.parseDouble(objects[21].toString()) : 0;
            Double consommables = (objects[22] != null) ? Double.parseDouble(objects[22].toString()) : 0;
            Double oxygenotherapie = (objects[23] != null) ? Double.parseDouble(objects[23].toString()) : 0;
            Double imaging = (objects[24] != null) ? Double.parseDouble(objects[24].toString()) : 0;
            Double proced = (objects[25] != null) ? Double.parseDouble(objects[25].toString()) : 0;

            InsuranceReportItem reportItem = new InsuranceReportItem();
            reportItem.setId(id);
            reportItem.setAdmissionDate(admissionDate);
            reportItem.setClosingDate(closingDate);
            reportItem.setBeneficiaryName(beneficiaryName);
            reportItem.setHouseholdHeadName(houseHoldHeadName);
            reportItem.setFamilyCode(familyCode);
            reportItem.setBeneficiaryLevel(beneficiaryLevel);
            reportItem.setCardNumber(cardNumber);
            reportItem.setCompanyName(companyName);
            reportItem.setAge(age);
            reportItem.setBirthDate(birthDate);
            reportItem.setGender(gender);
            reportItem.setDoctorName(doctorName);
            reportItem.setInsuranceId(insuranceId);
            reportItem.setGlobalBillId(globalBillId);
            reportItem.setGlobalBillIdentifier(globalBillIdentifier);

            reportItem.setMedicament(medicament);
            reportItem.setConsultation(consultation);
            reportItem.setHospitalisation(hospitalisation);
            reportItem.setLaboratoire(laboratoire);
            reportItem.setFormaliteAdministratives(formaliteAdministratives);
            reportItem.setAmbulance(ambulance);
            reportItem.setConsommables(consommables);
            reportItem.setOxygenotherapie(oxygenotherapie);
            reportItem.setImaging(imaging);
            reportItem.setProced(proced);

            Double total = medicament + consultation + hospitalisation + laboratoire + formaliteAdministratives + ambulance + consommables + oxygenotherapie + imaging + proced;
            Double totalInsuranceFirm = (insuranceFirmRate/100) * total;

            reportItem.setTotal100(total);
            reportItem.setTotalInsurance(totalInsuranceFirm);
            reportItem.setTotalPatient(total - totalInsuranceFirm);

            report.addReportItem(reportItem);

            report.addServiceRevenue("MEDICAMENTS", BigDecimal.valueOf(medicament));
            report.addServiceRevenue("CONSULTATION", BigDecimal.valueOf(consultation));
            report.addServiceRevenue("HOSPITALISATION", BigDecimal.valueOf(hospitalisation));
            report.addServiceRevenue("LABORATOIRE", BigDecimal.valueOf(laboratoire));
            report.addServiceRevenue("FORMALITES ADMINISTRATIVES", BigDecimal.valueOf(formaliteAdministratives));
            report.addServiceRevenue("AMBULANCE", BigDecimal.valueOf(ambulance));
            report.addServiceRevenue("CONSOMMABLES", BigDecimal.valueOf(consommables));
            report.addServiceRevenue("OXYGENOTHERAPIE", BigDecimal.valueOf(oxygenotherapie));
            report.addServiceRevenue("IMAGING", BigDecimal.valueOf(imaging));
            report.addServiceRevenue("PROCED.", BigDecimal.valueOf(proced));

            report.addServiceRevenue("100%", BigDecimal.valueOf(reportItem.getTotal100()));
            report.addServiceRevenue("Insurance (" + insuranceFirmRate + "%)", BigDecimal.valueOf(reportItem.getTotalInsurance()));
            report.addServiceRevenue("Patient (" + insurancePatientRate + "%)", BigDecimal.valueOf(reportItem.getTotalPatient()));
        }
        System.out.println("Done Fetching Insurance Report of size: " + report.getReportItems().size() + ", from MambaETL tables");
        return report;
    }

    @Override
    public List<GlobalBill> getGlobalBills() {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class);
        return crit.list();
    }

    @Override
    public List<GlobalBill> getGlobalBills(Date date1, Date date2) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class)
                .add(Restrictions.between("createdDate", date1, date2))
                .add(Restrictions.eq("closed", true));
        return crit.list();
    }

    @Override
    public List<GlobalBill> getGlobalBillsWithNullInsurance() {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class)
                .add(Restrictions.eq("insurance", null));
        return crit.list();
    }

    @Override
    public List<Consommation> getConsommationByGlobalBills(
            List<GlobalBill> globalBills) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Consommation.class);
        criteria.add(Restrictions.in("globalBill", globalBills));
        return criteria.list();
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getAllSubmittedPaymentRefunds()
     */
    @Override
    public List<PaymentRefund> getAllSubmittedPaymentRefunds() {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PaymentRefund.class);
        return criteria.list();
    }

    @Override
    public PaymentRefund getRefundById(Integer id) {
        return (PaymentRefund) sessionFactory.getCurrentSession().get(PaymentRefund.class, id);
    }

    @Override
    public PaidServiceBillRefund getPaidServiceBillRefund(
            Integer paidSviceBillRefundid) {
        return (PaidServiceBillRefund) sessionFactory.getCurrentSession().get(PaidServiceBillRefund.class, paidSviceBillRefundid);
    }

    @Override
    public List<PaymentRefund> getRefundsByBillPayment(BillPayment payment) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientServiceBill.class);
        crit.add(Expression.eq("consommation", payment));
        crit.add(Restrictions.eq("voided", false));
        return crit.list();
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getRefundsBetweenDatesAndByCollector(java.util.Date, java.util.Date, org.openmrs.User)
     */
    @Override
    public List<PaymentRefund> getRefundsBetweenDatesAndByCollector(
            Date startDate, Date endDate, User collector) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(PaymentRefund.class)
                .add(Restrictions.between("createdDate", startDate, endDate));
        if (collector != null)
            crit.add(Restrictions.eq("creator", collector));
        return crit.list();
    }

    @Override
    public InsurancePolicy getInsurancePolicyByThirdParty(ThirdParty t) {
        System.out.print(" am getting in getinsurancepolicybythird party in hibernate " + t.getThirdPartyId());


        InsurancePolicy insurancePolicy = (InsurancePolicy) sessionFactory.getCurrentSession().createCriteria(InsurancePolicy.class)
                .add(Restrictions.eq("thirdParty", t)).list().get(0);
        //.uniqueResult();

        System.out.print(" insurancePolicyinsurancePolicyinsurancePolicyinsurancePolicy ");
        return insurancePolicy;
    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getConsommations(java.util.Date, java.util.Date, org.openmrs.module.mohbilling.model.Insurance, org.openmrs.module.mohbilling.model.ThirdParty, org.openmrs.User)
     */
    @Override
    public List<Consommation> getConsommations(Date startDate,
                                               Date endDate, Insurance insurance, ThirdParty tp,
                                               User billCreator, Department department) {
        Session session = sessionFactory.getCurrentSession();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder combinedSearch = new StringBuilder("");

        combinedSearch.append("SELECT c.* FROM moh_bill_consommation c "
                + " inner join moh_bill_patient_bill pb on pb.patient_bill_id=c.patient_bill_id"
                + " and c.created_date between '" + df.format(startDate) + " 00:00:00 " + "' AND '" + df.format(endDate) + " 23:59:59'");

        if (insurance != null || tp != null) {
            combinedSearch
                    .append(" inner join moh_bill_beneficiary b on b.beneficiary_id=c.beneficiary_id "
                            + " inner join moh_bill_insurance_policy ip on ip.insurance_policy_id=b.insurance_policy_id "
                            + " inner join moh_bill_insurance i on i.insurance_id = ip.insurance_id "
                    );

            if (insurance != null)
                combinedSearch.append(" and i.insurance_id ='" + insurance.getInsuranceId() + "'");

            if (tp != null)
                combinedSearch.append(" and ip.third_party_id ='" + tp.getThirdPartyId() + "'");
        }

        if (billCreator != null)
            combinedSearch.append(" and c.creator ='" + billCreator.getUserId() + "'");

        if (department != null)
            combinedSearch.append(" and c.department_id ='" + department.getDepartmentId() + "'");

        List<Consommation> consommations = session
                .createSQLQuery(combinedSearch.toString())
                .addEntity("c", Consommation.class).list();

        return consommations;
    }

    @Override
    public List<Consommation> getConsommationsWithPatientNotConfirmed(Date startDate,
                                                                      Date endDate) {
        Session session = sessionFactory.getCurrentSession();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder combinedSearch = new StringBuilder("");

        combinedSearch.append("SELECT c.* FROM moh_bill_consommation c "
                + " inner join moh_bill_patient_bill pb on pb.patient_bill_id=c.patient_bill_id and paymentConfirmed=0"
                + " and c.created_date between '" + df.format(startDate) + " 00:00:00 " + "' AND '" + df.format(endDate) + " 23:59:59'");

        List<Consommation> consommations = session
                .createSQLQuery(combinedSearch.toString())
                .addEntity("c", Consommation.class).list();

        return consommations;
    }

    @Override
    public List<Consommation> getDCPConsommations(Date startDate, Date endDate, User billCreator) {
        Session session = sessionFactory.getCurrentSession();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder combinedSearch = new StringBuilder("");

        combinedSearch.append("SELECT c.* FROM moh_bill_consommation c "
                //+" inner join moh_bill_patient_bill pb on pb.patient_bill_id=c.patient_bill_id"
                //+ " inner join moh_bill_beneficiary b on b.beneficiary_id=c.beneficiary_id "
                //+ " inner join moh_bill_insurance_policy ip on ip.insurance_policy_id=b.insurance_policy_id "
                //+ " inner join moh_bill_insurance i on i.insurance_id = ip.insurance_id "
                + " inner join moh_bill_patient_service_bill psb on c.consommation_id=psb.consommation_id "
                + " and psb.is_paid='1' and psb.item_type='2' and psb.voided='0' "
                + " and c.created_date between '" + df.format(startDate) + " 00:00:00 " + "' AND '" + df.format(endDate) + " 23:59:59'");
			/*if (insurance != null)
				combinedSearch.append(" and i.insurance_id ='"+insurance.getInsuranceId()+"'");
			if (tp != null)
				combinedSearch.append(" and ip.third_party_id ='"+tp.getThirdPartyId()+"'");*/

        if (billCreator != null)
            combinedSearch.append(" and c.creator ='" + billCreator.getUserId() + "'");
        combinedSearch.append(" GROUP BY c.consommation_id ");

		/*if (department != null)
			combinedSearch.append(" and c.department_id ='"+department.getDepartmentId()+"'");*/

        List<Consommation> consommations = session
                .createSQLQuery(combinedSearch.toString())
                .addEntity("c", Consommation.class).list();
        return consommations;
    }

    @Override
    public void updateOtherInsurances(ServiceCategory sc) {
        Session session = sessionFactory.getCurrentSession();

        try {
            for (Insurance ins : InsuranceUtil.getAllInsurances()) {
                StringBuilder queryStr = new StringBuilder("");
                if (ins.getInsuranceId() != 1) {
                    ServiceCategory serviceCategory = getServiceCategoryByName(sc.getName(), ins);
                    if (serviceCategory == null) {
                        queryStr.append("insert ignore into moh_bill_service_category(name,description,created_date,retired,insurance_id,creator)");
                        queryStr.append(" SELECT name,description,created_date,retired," + ins.getInsuranceId() + " as insurance_id,creator FROM moh_bill_service_category "
                                + " where service_category_id=" + sc.getServiceCategoryId() + ";");
                        session.createSQLQuery(queryStr.toString()).executeUpdate();
                    }

                }

            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    /* (non-Javadoc)
     * @see org.openmrs.module.mohbilling.db.BillingDAO#getTransactions(java.util.Date, java.util.Date, org.openmrs.User, java.lang.String)
     */
    @Override
    public List<Transaction> getTransactions(Date startDate,
                                             Date endDate, User collector, String type) {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(Transaction.class)
                .add(Restrictions.between("createdDate", startDate, endDate));
        if (collector != null)
            crit.add(Restrictions.eq("collector", collector));
        if (type != null)
            crit.add(Restrictions.eq("reason", type));
        return crit.list();
    }

    @Override
    public GlobalBill getOpenGlobalBillByInsuranceCardNo(String insuranceCardNo) {
        try {
            Criteria crit = sessionFactory.getCurrentSession().createCriteria(GlobalBill.class)
                    .add(Restrictions.like("billIdentifier", insuranceCardNo + "%")).add(Restrictions.eq("closed", false));

            System.out.println("Find GBBBBBBBBBBBBBBBBBBBBBBBBBB: " + crit.list().size());

            GlobalBill globalBill = (GlobalBill) crit.uniqueResult();


            return globalBill;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<InsurancePolicy> getAllInsurancePoliciesByPatient(Patient patient) throws DAOException {

        return sessionFactory
                .getCurrentSession()
                .createCriteria(InsurancePolicy.class)
                .add(Restrictions.eq("owner", patient)).list();
    }

    @Override
    public FacilityServicePrice getFacilityServiceByName(String name) {

        return (FacilityServicePrice) sessionFactory.getCurrentSession()
                .createCriteria(FacilityServicePrice.class)
                .add(Restrictions.eq("name", name)).add(Restrictions.eq("retired", false)).uniqueResult();
    }

}