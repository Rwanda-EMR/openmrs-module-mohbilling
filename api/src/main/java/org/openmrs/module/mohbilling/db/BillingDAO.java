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
package org.openmrs.module.mohbilling.db;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author EMR@RBC
 * 
 */

/**
 * @author emr
 *
 */
public interface BillingDAO {

	/**
	 * Gets the Patient Bill from the DB by specifying the Object/ID
	 * 
	 * @param bill
	 *            , the bill to be matched
	 * @return patientBill, the bill that was matched by the entered object.
	 *         Otherwise it returns Null Object
	 * 
	 * @throws DAOException
	 */
	public PatientBill getPatientBill(Integer billId) throws DAOException;

	/**
	 * Gets all existing PatientBills
	 * 
	 * @return <code>List<PatientBill></code> a list of PatientBill
	 * 
	 * @throws DAOException
	 */
	public List<PatientBill> getAllPatientBills() throws DAOException;

	/**
	 * Gets the insurance from the DB by specifying the Object/ID
	 * 
	 * @param insurance
	 *            , the insurance to be matched
	 * @return insurance, the insurance that was matched by the entered object.
	 *         Otherwise it returns Null Object
	 * 
	 * @throws DAOException
	 */
	public Insurance getInsurance(Integer insuranceId) throws DAOException;

	/**
	 * Gets all existing Insurances
	 * 
	 * @return <code>List<Insurance></code> a list of Insurance
	 * 
	 * @throws DAOException
	 */
	public List<Insurance> getAllInsurances() throws DAOException;

	/**
	 * Gets Insurance Policy from the DB by specifying the Object/ID
	 * 
	 * @param card
	 *            , the insurance policy to be matched
	 * @return card, the card that was matched by the entered object. Otherwise
	 *         it returns Null Object
	 * 
	 * @throws DAOException
	 */
	public InsurancePolicy getInsurancePolicy(Integer insurancePolicyId)
			throws DAOException;

	/**
	 * Gets all existing InsurancePolicies
	 * 
	 * @return <code>List<InsurancePolicy></code> a list of InsurancePolicy
	 * 
	 * @throws DAOException
	 */
	public List<InsurancePolicy> getAllInsurancePolicies() throws DAOException;

	/**
	 * Saves the Patient Bill to the DB by passing the Object to be saved
	 * 
	 * @param bill
	 *            , the bill to be saved
	 * 
	 * @throws DAOException
	 */
	public void savePatientBill(PatientBill bill) throws DAOException;

	/**
	 * Saves the Insurance to the DB by passing the Object to be saved
	 * 
	 * @param insurance
	 *            , the insurance to be saved
	 * 
	 * @throws DAOException
	 */
	public void saveInsurance(Insurance insurance) throws DAOException;

	/**
	 * Saves the Insurance Policy to the DB by passing the Object to be saved
	 * 
	 * @param card
	 *            , the card to be saved
	 * 
	 * @param card
	 * @throws DAOException
	 */
	public void saveInsurancePolicy(InsurancePolicy card) throws DAOException;

	/**
	 * Gets a Facility Service Price to the DB
	 * 
	 * @param id
	 *            , The Facility Service Price ID to be matched
	 * @return the matched Facility Service Price
	 * 
	 * @throws DAOException
	 */
	public FacilityServicePrice getFacilityServicePrice(Integer id)
			throws DAOException;

	/**
	 * Gets all existing Service Categorie from the DB
	 * 
	 * @return list of Service Categorie Prices
	 * @throws DAOException
	 */
	public List<ServiceCategory> getAllServiceCategories() throws DAOException;

	/**
	 * Gets all existing Facility Service Prices from the DB
	 * 
	 * @return list of Facility Service Prices
	 * @throws DAOException
	 */
	public List<FacilityServicePrice> getAllFacilityServicePrices()
			throws DAOException;

	/**
	 * Adds a Facility Service Price to the DB
	 * 
	 * @param fsp
	 *            , the facility service price to save
	 * 
	 * @throws DAOException
	 */
	public void saveFacilityServicePrice(FacilityServicePrice fsp)
			throws DAOException;

	/**
	 * get all billable services
	 * 
	 * @return
	 */

	public List<BillableService> getAllBillableServices();

	/**
	 * get all billable services
	 * 
	 * @return
	 */
	public Float getPaidAmountPerInsuranceAndPeriod(Insurance insurance,
													Date startDate, Date endDate);



	/**
	 * Gets the InsurancePolicyCard by unique InsuranceCardNo
	 * 
	 * @param insuranceCardNo
	 *            the unique insurance card number
	 * @return insurancePolicyCard that matches the unique card number
	 */
	public InsurancePolicy getInsurancePolicyByCardNo(String insuranceCardNo);

	/**
	 * Gets all patient bills by entered parameters
	 * 
	 * @param insurance
	 * @param startDate
	 * @param endDate
	 * @param patientId
	 * @param serviceName
	 * @return
	 */
	public List<BillPayment> paymentsCohortBuilder(Insurance insurance, Date startDate,
												   Date endDate, Integer patientId, String serviceName,
												   String billStatus, String billCollector);
	/**
	 * Gets all bill payments by entered parameters
	 * 
	 * @param insurance
	 * @param startDate
	 * @param endDate
	 * @param patientId
	 * @param serviceName
	 * @return
	 */
	public List<PatientBill> billCohortBuilder(Insurance insurance, Date startDate,
											   Date endDate, Integer patientId, String serviceName,
											   String billStatus, String billCollector);

	/**
	 * Gets a BillableService by selecting those having the provided
	 * FacilityServicePrice and the Insurance.
	 * 
	 * @param price
	 *            the FacilityServicePrice
	 * @param insurance
	 *            the Insurance
	 * @return BillableService that matches both FacilityServicePrice and
	 *         Insurance
	 */
	public BillableService getBillableServiceByConcept(
			FacilityServicePrice price, Insurance insurance);

	/**
	 * Gets a Third Party from the DB
	 * 
	 * @param id
	 *            , The Third Party ID to be matched
	 * @return the matched Third Party
	 * 
	 * @throws DAOException
	 */
	public ThirdParty getThirdParty(Integer thirdPartyId) throws DAOException;

	/**
	 * Adds a Third Party from the DB
	 * 
	 * @param thirdParty
	 *            , the Third Party to save
	 * 
	 * @throws DAOException
	 */
	public void saveThirdParty(ThirdParty thirdParty) throws DAOException;

	/**
	 * get all Third Parties
	 * 
	 * @return
	 */
	public List<ThirdParty> getAllThirdParties() throws DAOException;


	/**
	 * Gets a Beneficiary by passing its PolicyNumber
	 * 
	 * @param policyIdNumber
	 *            the Number of the policy card to be matched
	 * @return beneficiary that matches
	 * @throws DAOException
	 */
	public Beneficiary getBeneficiaryByPolicyNumber(String policyIdNumber)
			throws DAOException;
	/**
	 * Gets the InsurancePolicy for a given Beneficiary
	 * 
	 * @param beneficiary
	 *            the one to be
	 * @return InsurancePolicy that matches
	 */
	public InsurancePolicy getInsurancePolicyByBeneficiary(
			Beneficiary beneficiary);

	/**
	 * Gets a Billable Service by matching a given Id
	 * 
	 * @param id
	 *            the Id for Billable Service
	 * @return BillableService that matches
	 */
	public BillableService getBillableService(Integer id);

	/**
	 * Gets a Service Category by its ID
	 * 
	 * @param id
	 *            of a service to be matched
	 * @return serviceCategory that matches
	 */
	public ServiceCategory getServiceCategory(Integer id);

	/**
	 * Gets all BillableServices that matches the provided Service Category
	 * 
	 * @param sc
	 *            the Service Category to match
	 * @param insurance 
	 * @return billableServices a list of all Billable Services that are in a
	 *         category
	 */
	public List<BillableService> getBillableServiceByCategory(ServiceCategory sc);

	/**
	 * Gets a Facility Service Price by providing a Concept
	 * 
	 * @param concept
	 *            the concept to be matched
	 * @return facilityServicePrice that matches the concept
	 */
	public FacilityServicePrice getFacilityServiceByConcept(Concept concept);

	/**
	 * Gets all Billable Services for a given Facility Service Price
	 * 
	 * @param fsp
	 *            the FacilityServicePrice to match
	 * @return billableServices that matches the fsp
	 */
	public List<BillableService> getBillableServicesByFacilityService(
			FacilityServicePrice fsp);

	/**
	 * Gets all Billable Services that correspond to a given Insurance
	 * 
	 * @param insurance
	 *            the Company to be matched
	 * @return billableServices that matches the Insurance
	 */
	public List<BillableService> getBillableServicesByInsurance(
			Insurance insurance);

	/**
	 * Gets all PolicyIds that are associated to the given patient
	 * 
	 * @param patientId
	 *            the patient ID to match
	 * @return list of String[] : {INSURANCE NAME, POLICY ID}
	 */
	public List<String[]> getPolicyIdByPatient(Integer patientId);

	
	/**
	 * Get all BIllPayments 
	 * 
	 * 
	 * @return
	 */
	
	
	public List<BillPayment> getAllBillPayments();
	
	/**
	 * 
	 * @param Gets billPayments by date and collector
	 * @param collector
	 * @return
	 */

	public List<BillPayment> getBillPaymentsByDateAndCollector(
			Date createdDate, Date endDate, User collector);
	
	/**
	 * 
	 * @param Gets service category by name
	 * @param name
	 * @return
	 */
	public ServiceCategory getServiceCategoryByName(String name, Insurance insurance);
	
	/**
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate);
	
	public Map<String,Double> getRevenueByService(Date receivedDate, String[] serviceCategory, String collector, Insurance insurance);
	

	public Object[] getBills(Date startDate, Date endDate, User collector);

	public List<PatientBill> getPatientBillsByCollector(Date receivedDate,
														User collector);

	public PatientBill getBills(Patient patient, Date startDate, Date endDate);

	public InsuranceRate getInsuranceRateByInsurance(Insurance insurance);
	
	public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo);

	public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(
			Date startDate, Date endDate);

	public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary,
												   Date startDate, Date endDate);
	
	public void loadBillables(Insurance insurance);
	
	public List<Object[]> getBaseBillableServices(Insurance i);
	
	/**
	 * @param 
	 * @return
	 */
	public List<Object[]> getPharmacyBaseBillableServices(Insurance i);
	

	public Set<PatientBill> getRefundedBills(Date startDate, Date endDate, User collector);

	/**
	 * saves Departement object in the DB
	 * @param departement to be saved
	 */
	public Department  saveDepartement(Department departement);
	
	/**
	 * Get encounter by idententifier departementid
	 * @param departementId
	 * @return departement with given identifier departementId
	 */
	public Department getDepartement(Integer departementId);

	/**
	 * Get all departements in the Hospital
	 * @return List<Department> departements including the voided ones
	 */
	public List<Department> getAllDepartements();	

	/**
	 * Saves the the hop services to the DB
	 * @param service  the  hopital services to be saved
	 * @return hopService
	 */

	public HopService saveHopService(HopService service);
	/**
	 * Gets all Hospital services(LABO,Pharmacy) that can be served by Departement
	 * If Departement is null,it returns  all service
	 * if departement is not null,it retuns list of services served by this department
	 * @return List<HopServices> services
	 */
	public List<HopService> getAllHopService();

	public HopService getHopService(Integer serviceId);

	/**
	 * Saves admission to DB
	 * Save admission to DB
	 * @param admission admission to be saved
	 * @return admission saved
	 */
	public Admission saveAdmission(Admission admission);

	/**
	 * @param admissionid
	 * @return
	 */
	public Admission getPatientAdmission(Integer admissionid);

	/**
	 * @param globalBill
	 * @return
	 */
	public GlobalBill saveGlobalBill(GlobalBill globalBill);

	/**
	 * Get GlobalBillBy id 
	 * @param globalBillId
	 * @return GlobalBill 
	 */
	public GlobalBill GetGlobalBill(Integer globalBillId);

	/**
	 * Get global bill by admission
	 * @param admission
	 * @return Admission
	 */
	public GlobalBill getGlobalBillByAdmission(Admission admission);

	/**
	 * Get a List of admission by insurance policy
	 * @param ip
	 * @return List<Admission>
	 */
	public List<Admission> getAdmissionsListByInsurancePolicy(InsurancePolicy ip);


	public void saveConsommation(Consommation consommation);

	public void saveInsuranceBill(InsuranceBill ib);

	public void saveThirdPartyBill(ThirdPartyBill thirdBill);

	public Consommation getConsommation(Integer consommationId);

	public CashPayment saveCashPayment(CashPayment cashPayment);

	public PatientServiceBill saveBilledItem(PatientServiceBill psb);

	public PatientServiceBill getPatientServiceBill(Integer patientServiceBillId);

	public void getPatientServiceBill(BillPayment bp);

	public void savePaidServiceBill(PaidServiceBill paidSb);

	/**
	 * Gets all consommations by globalBill
	 * @param globalBillid
	 * 
	 */
	public List<Consommation> getAllConsommationByGlobalBill(GlobalBill globalBill);

	/**
	 * Gets GlobalBill matching with a given billIdentifier
	 * @param billIdentifier
	 * @return GlobalBill
	 */
	public GlobalBill getGlobalBillByBillIdentifier(String billIdentifier);
	
	public void savePatientAccount(PatientAccount account);
	/**
	 * Gets Patient Account with a given accountId
	 * @param accountId
	 * @return PatientAccount
	 */
	public PatientAccount getPatientAccount(Integer accountId);
	/**
	 * gets transactions set by given parameters among PatientAccount, startDate, endDate and/or reason
	 * @param acc
	 * @param startDate
	 * @param endDate
	 * @param reason
	 * @return transactions set
	 */
	public Set<Transaction> getTransactions(PatientAccount acc, Date startDate, Date endDate, String reason);
	
	public PatientAccount getPatientAccount(Patient patient);
	
	public DepositPayment saveDepositPayment(DepositPayment depositPayment);

	/**
	 * Gets all consommations matching with a given Beneficiary
	 * @param beneficiary
	 * @return consommations list
	 */
	public List<Consommation> getConsommationsByBeneficiary(Beneficiary beneficiary);

	/**
<<<<<<< HEAD
	 * Gets BillPayment by a given paymentId
	 * @param paymentId
	 * @return billPayment
	 */
	public BillPayment getBillPayment(Integer paymentId);

	/**
	 * Gets all paid service bills by BillPayment
	 * @param payment
	 * @return List<PaidServiceBill> paidItems
	 */
	public List<PaidServiceBill> getPaidServices(BillPayment payment);

	/**
	 * Gets Consommation  by patientBill
	 * @param patientBill
	 * @return Consommation
	 */
	public Consommation getConsommationByPatientBill(PatientBill patientBill);
      
	/**
	 * saves payment refund to the DB
	 * @param refund
	 * @return PaymentRefund
	 */
	public PaymentRefund savePaymentRefund(PaymentRefund refund);
	/**
	 *gets paidServiceBill matching with a given paidServiceBillid
	 * @param paidSviceBillid
	 * @return paidItem
	 */
	public PaidServiceBill getPaidServiceBill(Integer paidSviceBillid);
	/**
	 * Gets hospital services by department
	 * @param department
	 * @return List<HopService>
	 */
	public List<HopService> getHospitalServicesByDepartment(Department department);
	/**
	 * gets Transaction with a given id
	 * @param id
	 * @return Transaction
	 */
	public Transaction getTransactionById(Integer id);

	/**
	 * Gets hospital service by name
	 * @param name
	 * @return HospitalService
	 */
	public HopService getServiceByName(String name);

	public List<PaidServiceBill> getPaidItemsByBillPayments(
			List<BillPayment> payments);
	
	public List<PatientServiceBill> getBillItemsByCategory(Consommation consommation, HopService service);
	public List<PatientServiceBill> getBillItemsByGroupedCategories(Consommation consommation, List<HopService> services);
	/**
	 * gets Global Bills between 2 dates
	 * @param date1
	 * @param date2
	 * @return list of Global bills
	 */
	public List<GlobalBill> getGlobalBills(Date date1, Date date2);
	
	/**
	 * gets a list of consommations matching with a given global bill list
	 * @param gb
	 * @return list of Consommations
	 */
	public List<Consommation> getConsommationByGlobalBills(
			List<GlobalBill> globalBills);
	/**
	 * gets all submitted payment refunds
	 * @return
	 */
	public List<PaymentRefund> getAllSubmittedPaymentRefunds();
	
	/**
	 * get refund by id
	 * @param id
	 * @return
	 */
	public PaymentRefund getRefundById(Integer id);
	
	public PaidServiceBillRefund getPaidServiceBillRefund(Integer paidSviceBillRefundid);
	
	public List<PaymentRefund> getRefundsByBillPayment(BillPayment payment);
	
	/**
	 * get all confirmed refunds between two dates and by a given collector
	 * @param startDate
	 * @param endDate
	 * @param collector
	 * @return list of PaymentRefund
	 */
	public List<PaymentRefund> getRefundsBetweenDatesAndByCollector(Date startDate, Date endDate, User collector);
	/**
	 * get InsurancePolicy by a given third party
	 * @param t
	 * @return InsurancePolicy
	 */
	public InsurancePolicy getInsurancePolicyByThirdParty(ThirdParty t);
	/**
	 * get all consommations by provided parameters
	 * @param startDate
	 * @param endDate
	 * @param insurance
	 * @param tp
	 * @param billCreator
	 * @return consommation list
	 */
	public List<Consommation> getConsommations(Date startDate, Date endDate, Insurance insurance, ThirdParty tp, User billCreator, Department department);
	
	public void updateOtherInsurances(ServiceCategory sc);
	/**
	 * gets transactions by type,in a period and by cashier
	 * @param startDate
	 * @param endDate
	 * @param collector
	 * @param type
	 * @return List<DepositPayment>
	 */
	public List<Transaction> getTransactions(Date startDate, Date endDate, User collector, String type);
}
