/**
 * 
 */
package org.openmrs.module.mohbilling.service;

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
public interface BillingService {

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
	 * Gets all existing Bill Payments
	 * 
	 * @return <code>List<BillPayment></code> a list of BillPayment
	 * 
	 * @throws DAOException
	 */
	public List<BillPayment> getAllBillPayments() throws DAOException;

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
	 * Gets all existing Service Categories from the DB
	 * 
	 * @return list of Service Categories
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

	public List<BillableService> getAllBillableServices();

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
	public List<PatientBill> billCohortBuilder(Insurance insurance, Date startDate,
											   Date endDate, Integer patientId, String serviceName,
											   String billStatus, String billCollector);
	
	public List<BillPayment> paymentsCohortBuilder(Insurance insurance, Date startDate,
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
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public List<ThirdParty> getAllThirdParties() throws DAOException;

	/**
	 * Adds a Third Party to the DB
	 * 
	 * @param thirdParty
	 *            , the Third Party to save
	 * 
	 * @throws DAOException
	 */
	public void saveThirdParty(ThirdParty thirdParty) throws DAOException;

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
	 * @param insurance 
	 * 
	 * @param sc
	 *            the Service Category to match
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
	 * 
	 * @param Gets billPayments by date and collector
	 * @param collector
	 * @return
	 */
	public List<BillPayment> getBillPaymentsByDateAndCollector(Date createdDate, Date endDate, User collector);
	
	/**
	 * 
	 * @param Gets service category by name
	 * @param name
	 * @return
	 */
	public ServiceCategory getServiceCategoryByName(String name, Insurance insurance);
	
	/**
	 * gets a list of payments' dates
	 * @param insurance
	 * @param startDate
	 * @param endDate
	 * @param patientId
	 * @param serviceName
	 * @param billStatus
	 * @param billCollector
	 * @return List<Date>
	 */
	public List<Date> getRevenueDatesBetweenDates(Date startDate, Date endDate);;
	
	public Map<String,Double> getRevenueByService(Date receivedDate, String[] serviceCategory, String collector, Insurance insurance);
	
	public  Object[]  getBills(Date startDate, Date endDate, User collector);
	
	public List<PatientBill> getPatientBillsByCollector(Date receivedDate, User collector);
	
	public PatientBill getBills(Patient patient, Date startDate, Date endDate);
	
	public InsuranceRate getInsuranceRateByInsurance(Insurance insurance);
	
	public List<Beneficiary> getBeneficiaryByCardNumber(String cardNo);
	
	public List<InsurancePolicy> getInsurancePoliciesBetweenTwodates(Date startDate, Date endDate);
	
	public List<PatientBill> getBillsByBeneficiary(Beneficiary beneficiary, Date startDate, Date endDate);
	
	public void loadBillables(Insurance insurance);
	
	public List<Object[]> getBaseBillableServices(Insurance i);

	public List<Object[]> getPharmacyBaseBillableServices(Insurance i);
	
	
	/**
	 * gets refunded bills 
	 * 
	 * @param startDate
	 * @param endDate
	 * @param collector
	 * @return
	 */
	public Set<PatientBill> getRefundedBills(Date startDate, Date endDate, User collector);

	/**
	 * saves the departement Object to the DB
	 * @param departement to be saved	
	 * @return Departement 
	 */
	public Department saveDepartement(Department departement);
	
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
	 * Gets all Hospital services(LABO,Pharmacy) that can be served by any Departement
	 * If Departement is null,it returns  all service
	 * @return List<HopServices> services
	 */
	public List<HopService>getAllHopService();
	

	/**
	 * Get Hop Service by id
	 * @param serviceId
	 * @return Hop Service
	 */
	public HopService getHopService(Integer serviceId);
	
	/**
	 * saves Admission to the DB
	 * @param admission the admission to be saved
	 * @return admission
	 */
	public Admission saveAdmission(Admission admission);
    
	/**
	 * Get patient admission
	 * @param admissionid matching with admission
	 * @return admission
	 */
	public Admission getPatientAdmission(Integer admissionid);
	
	/**
	 * Get GlobalBill
	 * @param globalBill 
	 * @return GlobalBill
	 */
	public GlobalBill saveGlobalBill(GlobalBill globalBill);
	
	/**
	 * @param globalBillId
	 * @return
	 */
	public GlobalBill GetGlobalBill(Integer globalBillId);
	
	/**
	 * Get GlobalBill by patient admission
	 * @param admission
	 * @return GlobalBill
	 */
	public GlobalBill getGlobalBillByAdmission(Admission admission);
	
	/**
	 * Get admissions list by insurance
	 * @param ip
	 * @return List<Admission>
	 */
	public List<Admission> getAdmissionsListByInsurancePolicy(InsurancePolicy ip);

	/**
	 * Save consommation to the DB
	 * @param consommation consommat to be saved
	 */
	public void saveConsommation(Consommation consommation);

	/**
	 * save InsuranceBill to the DB
	 * @param ib
	 */
	public void saveInsuranceBill(InsuranceBill ib);

	/**
	 * Save the ThirdPartyBill to the DB
	 * @param thirdBill to be saved
	 */
	public void saveThirdPartyBill(ThirdPartyBill thirdBill);

	/**
	 * Get consommation by id
	 * @param consommationId
	 * @return consommation that  matches with consommationId
	 */
	public Consommation getConsommation(Integer consommationId);

	public CashPayment saveCashPayment(CashPayment cashPayment);

	public PatientServiceBill saveBilledItem(PatientServiceBill psb);

	public PatientServiceBill getPatientServiceBill(Integer patientServiceBillId);

	public void saveBillPayment(BillPayment bp);

	public void savePaidServiceBill(PaidServiceBill paidSb);

	/**
	 * Gets all consommation  by globalBill
	 * @param globalBill
	 * @return List<Consommation>
	 */
	public List<Consommation> getAllConsommationByGlobalBill(GlobalBill globalBill);

	/**
	 * Gets Global bill  matching with a given bill identifier
	 * @param billIdentifier
	 * @return GlobalBill
	 */
	public GlobalBill getGlobalBillByBillIdentifier(String billIdentifier);
	/**
	 * Gets List of consommations matching with a given beneficiary
	 * @param beneficiary
	 * @return List<Consommation>
	 */
	public List<Consommation> getConsommationsByBeneficiary(Beneficiary beneficiary);
	/**
	 * Gets Patient Account with a given accountId
	 * @param accountId
	 * @return PatientAccount
	 */
	public PatientAccount getPatientAccount(Integer accountId);
	/**
	 * Get patient account by patient
	 * @param account
	 * @return
	 */
	public PatientAccount getPatientAccount(Patient patient);
	/**
	 * saves patient account for cash deposit purpose
	 * @param account
	 */
	public void savePatientAccount(PatientAccount account);

	/**
	 * Gets BillPayment by a given PaymentId
	 * @param paymentId
	 * @return Billpayment
	 */
	public BillPayment getBillPayment(Integer paymentId);

	/**
	 * Gets all paid bill services by BillPayment
	 * @param payment
	 * @return List<PaidServiceBill>  paidItems
	 */
	public List<PaidServiceBill> getPaidServices(BillPayment payment);

	/**
	 * Gets consommation by patientBill
	 * @param patientBill
	 * @return consommation
	 */
	public Consommation getConsommationByPatientBill(PatientBill patientBill);
	/**
	 * saves PaymentRefund
	 * @param refund
	 * @return PaymentRefund
	 */
	public PaymentRefund savePaymentRefund(PaymentRefund refund);

	/**
	 * gets paidSericeBill matching with agiven a paidServiceBillid
	 * @param paidSviceBillid
	 * @return paidItem
	 */
	public PaidServiceBill getPaidServiceBill(Integer paidSviceBillid);

	public Set<Transaction> getTransactions(PatientAccount acc, Date startDate, Date endDate, String reason);
	

	public DepositPayment saveDepositPayment(DepositPayment depositPayment);
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
	 * gets hopservice by name
	 * @param name
	 * @return HopService
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
