package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.util.*;

/**
 * Helper class to support the InsurancePolicy domain. (in other words, the
 * insurance card)
 * 
 * Parent class is InsurancePolicy, child class is Beneficiary
 * 
 * @author EMR-RBC
 * 
 */
public class InsurancePolicyUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {
		return Context.getService(BillingService.class);
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param beneficiary
	 * @param date
	 * @return
	 */
	public static List<InsurancePolicy> getValidInsurancePolicyOnDate(
			Beneficiary beneficiary, Date date) {

		List<InsurancePolicy> insurancePolicies = new ArrayList<InsurancePolicy>();
		for (InsurancePolicy insurancePolicy : getService()
				.getAllInsurancePolicies()) {
			for (Beneficiary ben : insurancePolicy.getBeneficiaries()) {

				if (ben.equals(beneficiary)
						&& (insurancePolicy.getCoverageStartDate().compareTo(
								date) < 0)
						&& (insurancePolicy.getExpirationDate().compareTo(date) > 0)) {
					insurancePolicies.add(insurancePolicy);
				}
			}

		}
		return insurancePolicies;

	}

	/**
	 * Auto generated method comment
	 * 
	 * @param p
	 * @param date
	 * @return
	 */
	public static List<InsurancePolicy> getValidInsurancePolicyOnDate(
			Person owner, Date date) {

		List<InsurancePolicy> insurancePolicies = new ArrayList<InsurancePolicy>();
		for (InsurancePolicy insurancePolicy : getService()
				.getAllInsurancePolicies()) {
			for (Beneficiary ben : insurancePolicy.getBeneficiaries()) {

				if (ben.equals(owner)
						&& (insurancePolicy.getCoverageStartDate().compareTo(
								date) < 0)
						&& (insurancePolicy.getExpirationDate().compareTo(date) > 0)) {
					insurancePolicies.add(insurancePolicy);
				}
			}

		}
		return insurancePolicies;
	}

	/**
	 * Creates an new Beneficiary and saves it into the DB
	 * 
	 * @param beneficiary
	 *            the Beneficiary object to be saved.
	 * 
	 * @return beneficiary that was added, and null otherwise
	 */
	public static Beneficiary createBeneficiary(Beneficiary beneficiary) {

		if (beneficiary != null) {
			beneficiary.setRetired(false);
			beneficiary.setCreatedDate(new Date());
			beneficiary.setCreator(Context.getAuthenticatedUser());

			beneficiary.getInsurancePolicy().addBeneficiary(beneficiary);
			getService().saveInsurancePolicy(beneficiary.getInsurancePolicy());			
			return beneficiary;
		}
		return null;
	}

	/**
	 * Creates the insurance policy and at the same time sets the Owner as the
	 * very first beneficiary of the card
	 * 
	 * @param card
	 *            the Insurance Policy to be added into the DB
	 * @return card if the card is not null, and null otherwise
	 */
	public static InsurancePolicy createInsurancePolicy(InsurancePolicy card) {

		if (card != null) {

			if (card.getInsurancePolicyId() != null)
				getService().saveInsurancePolicy(card);

			else {

				card.setCreatedDate(new Date());
				card.setCreator(Context.getAuthenticatedUser());
				card.setRetired(false);

				if (card.getInsurance().getCategory().toString()
						.equalsIgnoreCase(InsuranceCategory.NONE.toString())
						&& getPrimaryPatientIdentiferType()
								.equals(Context
										.getPatientService()
										.getPatientIdentifierType(
												Integer.valueOf(Context
														.getAdministrationService()
														.getGlobalProperty(
																BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE))))) {

					/** Getting the Patient Identifier from the system **/
					PatientIdentifier pi = InsurancePolicyUtil
							.getPrimaryPatientIdentifierForLocation(
									card.getOwner(),
									InsurancePolicyUtil.getLocationLoggedIn());

					card.setInsuranceCardNo(pi.getIdentifier().toString());
					card.setCoverageStartDate(new Date());
				}

				getService().saveInsurancePolicy(card);

				/**
				 * Creating the owner who is at the same time the very first
				 * beneficiary of this insurance policy (In this case
				 * <code>insuranceCardNo == policyIdNumber</code>)
				 */
				Beneficiary beneficiary = new Beneficiary();

				beneficiary.setInsurancePolicy(card);

				/* Check how it should be working for NONE insurance */
				beneficiary.setPolicyIdNumber(card.getInsuranceCardNo());
				/* End of checking */

				beneficiary.setCreatedDate(card.getCreatedDate());
				beneficiary.setCreator(card.getCreator());
				beneficiary.setPatient(card.getOwner());
				beneficiary.setRetired(card.isRetired());
				beneficiary.setRetiredBy(card.getRetiredBy());
				beneficiary.setRetiredDate(card.getRetiredDate());
				beneficiary.setRetireReason(card.getRetireReason());

				card.addBeneficiary(beneficiary);

				getService().saveInsurancePolicy(card);
			}

			return card;
		}

		return null;
	}

	/**
	 * Updates the Insurance policy that has been changed
	 * 
	 * @param card
	 *            the Insurance policy to be updated
	 * @return card the Insurance policy that has been changed, null otherwise
	 */
	public static InsurancePolicy editInsurancePolicy(InsurancePolicy card) {

		if (card != null) {
			getService().saveInsurancePolicy(card);
			for (Beneficiary beneficiary : card.getBeneficiaries())
				// Checking also the matching owner beneficiary
				if (beneficiary.getPolicyIdNumber().equals(
						card.getInsuranceCardNo())) {

					beneficiary.setInsurancePolicy(card);
					beneficiary.setPolicyIdNumber(card.getInsuranceCardNo());
					beneficiary.setCreatedDate(card.getCreatedDate());
					beneficiary.setCreator(card.getCreator());
					beneficiary.setPatient(card.getOwner());
					beneficiary.setRetired(card.isRetired());
					beneficiary.setRetiredBy(card.getRetiredBy());
					beneficiary.setRetiredDate(card.getRetiredDate());
					beneficiary.setRetireReason(card.getRetireReason());
				}

			return card;
		}

		return null;
	}

	/**
	 * Retires (deletes) the provided Insurance policy as well as its related
	 * beneficiaries
	 * 
	 * @param card
	 *            the Insurance policy to be retired
	 * @param retireReason
	 */
	public static void retireInsurancePolicy(InsurancePolicy card,
			String retireReason) {

		card.setRetired(true);
		card.setRetireReason(retireReason);
		card.setRetiredDate(new Date());
		card.setRetiredBy(Context.getAuthenticatedUser());

		/**
		 * Retiring all related Beneficiaries as well
		 */
		if (card.getBeneficiaries() != null
				&& card.getBeneficiaries().size() > 0)
			for (Beneficiary ben : card.getBeneficiaries()) {
				if (!ben.isRetired())
					retireBeneficiary(ben, retireReason);
			}

		getService().saveInsurancePolicy(card);

	}

	/**
	 * Retires the beneficiary from the valid beneficiaries of an Insurance
	 * policy
	 * 
	 * @param beneficiary
	 *            the beneficiary to be retired
	 * @param retiredReason
	 *            the retire reason
	 * @return beneficary the Beneficiary that has been retired, null otherwise
	 */
	public static Beneficiary retireBeneficiary(Beneficiary beneficiary,
			String retiredReason) {

		if (beneficiary != null) {
			for (Beneficiary ben : beneficiary.getInsurancePolicy()
					.getBeneficiaries())

				if (beneficiary.equals(ben)) {
					beneficiary.setRetired(true);
					beneficiary.setRetiredBy(Context.getAuthenticatedUser());
					beneficiary.setRetiredDate(new Date());

					if (retiredReason != null)
						if (!retiredReason.equals(""))
							beneficiary.setRetireReason(retiredReason);
						else
							beneficiary
									.setRetireReason("The beneficiary has been "
											+ "retired and no reason was provided");

					getService().saveInsurancePolicy(
							beneficiary.getInsurancePolicy());
				}
			return beneficiary;
		}
		return null;
	}

	/**
	 * Gets the Insurance policy that has the provided beneficiary
	 * 
	 * @param beneficiary
	 *            the beneficiary to be matched
	 * @return card if matched, null otherwise
	 */
	public static InsurancePolicy getInsurancePolicyByBeneficiary(
			Beneficiary beneficiary) {

		return getService().getInsurancePolicyByBeneficiary(beneficiary);
	}

	/**
	 * Gets the Insurance policy that matches the provided Owner
	 * 
	 * @param patient
	 *            the Owner to be matched
	 * @return card if matched, null otherwise
	 */
	public static InsurancePolicy getInsurancePolicyByOwner(Patient patient) {

		if (patient != null)
			for (InsurancePolicy card : getService().getAllInsurancePolicies())

				if (card.getOwner().equals(patient))
					return card;

		return null;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param insurance
	 * @return
	 */
	public static List<InsurancePolicy> getInsurancePoliciesByInsurance(
			Insurance insurance) {
		List<InsurancePolicy> CardsList = new ArrayList<InsurancePolicy>();

		List<InsurancePolicy> allCards = getService().getAllInsurancePolicies();
		for (InsurancePolicy oneCard : allCards) {
			if (oneCard.getInsurance().equals(insurance)) {
				CardsList.add(oneCard);

			}

		}

		return CardsList;
	}

	public static Set<Beneficiary> getInsurancyPolicyBeneficiary(
			InsurancePolicy card) {

		return card.getBeneficiaries();

	}

	/**
	 * @param patient
	 * @return
	 */
	public static List<InsurancePolicy> getInsurancePolicyByPatient(
			Patient patient) {
		// TODO: Verifies that the Patient is insured and has an Insurance card
		// or more
		// than 1
		// (InsurancePolicy), then after it returns the InsurancePolicy or NULL
		List<InsurancePolicy> patientCards = new ArrayList<InsurancePolicy>();

		List<InsurancePolicy> insurancePolicies = getService()
				.getAllInsurancePolicies();
		for (InsurancePolicy insurancePolicy : insurancePolicies) {
			if (insurancePolicy.getOwner().equals(patient)) {
				patientCards.add(insurancePolicy);

			}

		}
		return patientCards;
	}

	/**
	 * @param insurance
	 * @param valid
	 * @return
	 */
	public static List<InsurancePolicy> getInsurancePoliciesByInsurance(
			Insurance insurance, Boolean valid) {
		// TODO: This is to facilitate in getting all Insurance Policies by the
		// given Insurance, it may be valid or not depending on the entered
		// condition: valid.

		List<InsurancePolicy> CardsList = new ArrayList<InsurancePolicy>();

		List<InsurancePolicy> allCards = getService().getAllInsurancePolicies();
		for (InsurancePolicy oneCard : allCards) {
			if (oneCard.getInsurance().equals(insurance)) {
				CardsList.add(oneCard);

			}

		}

		return CardsList;

	}

	/**
	 * {-- This should be done in the Hibernate Services in case we have many
	 * records of InsurancePolicy --} This will help when reading the Card no
	 * where Barcode is applicable
	 * 
	 * @param insuranceCardNo
	 * @return
	 */
	public static InsurancePolicy getInsurancePolicyByCardNo(
			String insuranceCardNo) {

		InsurancePolicy card = null;
		List<InsurancePolicy> allCards = getService().getAllInsurancePolicies();
		for (InsurancePolicy insurancePolicy : allCards) {
			if (insurancePolicy.getInsuranceCardNo().equals(insuranceCardNo)) {
				return card;
			}
		}
		return null;
	}

	/**
	 * Gets the InsurancePolicy matching the Beneficiary policyIdNumber and
	 * returning the one matching the status "isRetired"
	 * 
	 * @param policyIdNumber
	 * @return insurancePolicy that 1. is retired || 2. is not retired
	 */
	public static InsurancePolicy getInsurancePolicyByPolicyIdNo(
			String policyIdNumber, Boolean isRetired) {

		for (InsurancePolicy card : getService().getAllInsurancePolicies())
			if (card.isRetired() == isRetired)
				for (Beneficiary ben : card.getBeneficiaries())
					if (ben.getPolicyIdNumber().equals(policyIdNumber))
						return card;

		return null;
	}

	/**
	 * Gets the InsurancePolicy where the given Insurance and insuranceCardNo is
	 * matched
	 * 
	 * @param insuranceCardNo
	 * @return
	 */
	public static InsurancePolicy getBeneficiaryByCardNo(Insurance insurance,
			String insuranceCardNo) {

		for (InsurancePolicy card : getService().getAllInsurancePolicies()) {
			if (card.getInsuranceCardNo().equals(insuranceCardNo))
				return card;
		}
		return null;
	}

	/**
	 * Gets the Beneficiary where it matches its policyIdNumber, you can pass
	 * the InsuranceCardNo as well, given the fact that this will be matching
	 * also the Beneficiary PolicyIdNo for the very first beneficiary of the
	 * InsurancePolicy
	 * 
	 * @param policyIdNumber
	 * @return beneficiary whether matched the policyIdNumber, null otherwise
	 */
	public static Beneficiary getBeneficiaryByPolicyIdNo(String policyIdNumber) {

		return getService().getBeneficiaryByPolicyNumber(policyIdNumber);
	}

	/**
	 * Gets the list of Beneficiaries that are not retired yet.
	 * 
	 * @param card
	 * @return
	 */
	public static List<Beneficiary> getBeneficiaryByInsurancePolicy(
			InsurancePolicy card) {

		List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		if (card != null) {
			for (Beneficiary ben : card.getBeneficiaries()) {
				if (ben.isRetired() == false)
					beneficiaries.add(ben);
			}
		}
		return beneficiaries;
	}

	/**
	 * Gets the list of Beneficiaries that match the given Patient.
	 * 
	 * @param patient
	 *            , the patient to match the beneficiaries
	 * @return list of matched Beneficiaries
	 */
	public static List<Beneficiary> getBeneficiaryByPatient(Patient patient) {

		List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		if (patient != null)
			for (InsurancePolicy ip : getService().getAllInsurancePolicies())
				for (Beneficiary ben : ip.getBeneficiaries())
					if (!ben.isRetired() && ben.getPatient() == patient)
						beneficiaries.add(ben);

		return beneficiaries;
	}

	/**
	 * Gets the list of Beneficiaries that match the given Owner.
	 * 
	 * @param owner
	 *            , the owner to match the beneficiaries
	 * @return list of matched Beneficiaries
	 */
	public static List<Beneficiary> getBeneficiaryByOwner(Patient owner) {

		List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		if (owner != null)
			for (InsurancePolicy ip : getService().getAllInsurancePolicies())
				if (ip.getOwner() == owner)
					for (Beneficiary ben : ip.getBeneficiaries())
						if (!ben.isRetired())
							beneficiaries.add(ben);

		return beneficiaries;
	}

	/**
	 * Gets the Beneficiary corresponding to the provided InsurancePolicy
	 * 
	 * @param card
	 *            the InsurancePolicy to match
	 * @return beneficiary when found, null otherwise
	 */
	public static Beneficiary getInsurancePolicyOwner(InsurancePolicy card) {

		if (card != null) {
			for (Beneficiary beneficiary : card.getBeneficiaries()) {
				if (beneficiary.getPatient().getPatientId().intValue() == card
						.getOwner().getPatientId().intValue())
					return beneficiary;
			}
		}
		return null;
	}

	public static PatientIdentifierType getPrimaryPatientIdentiferType() {
		PatientIdentifierType pit = null;
		try {
			pit = Context
					.getPatientService()
					.getPatientIdentifierType(
							Integer.valueOf(Context
									.getAdministrationService()
									.getGlobalProperty(
											BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE)));
		} catch (Exception ex) {
			pit = Context
					.getPatientService()
					.getPatientIdentifierTypeByName(
							Context.getAdministrationService()
									.getGlobalProperty(
											BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE));
		}
		if (pit == null) {
			throw new RuntimeException(
					"Cannot find patient identifier type specified by global property "
							+ BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE);
		}
		return pit;
	}

	/**
	 * Gets all patient identifier types that should be used in this module.
	 * This includes the primary type and the other types specified in the two
	 * global properties.
	 * 
	 * The first element of the returned list is the primary type. This method
	 * ensures that the returned list contains no duplicates.
	 * 
	 * @return
	 */
	public static List<PatientIdentifierType> getPatientIdentifierTypesToUse() {
		List<PatientIdentifierType> ret = new ArrayList<PatientIdentifierType>();
		ret.add(getPrimaryPatientIdentiferType());

		String s = Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_OTHER_IDENTIFIER_TYPES);
		if (s != null) {
			String[] ids = s.split(",");
			for (String idAsString : ids) {
				try {
					idAsString = idAsString.trim();
					if (idAsString.length() == 0)
						continue;
					PatientIdentifierType idType = null;
					try {
						Integer id = Integer.valueOf(idAsString);
						idType = Context.getPatientService()
								.getPatientIdentifierType(id);
					} catch (Exception ex) {
						idType = Context.getPatientService()
								.getPatientIdentifierTypeByName(idAsString);
					}
					if (idType == null) {
						throw new IllegalArgumentException(
								"Cannot find patient identifier type "
										+ idAsString
										+ " specified in global property "
										+ BillingConstants.GLOBAL_PROPERTY_OTHER_IDENTIFIER_TYPES);
					}
					if (!ret.contains(idType)) {
						ret.add(idType);
					}
				} catch (Exception ex) {
					throw new IllegalArgumentException(
							"Error in global property "
									+ BillingConstants.GLOBAL_PROPERTY_OTHER_IDENTIFIER_TYPES
									+ " near '" + idAsString + "'");
				}
			}
		}
		return ret;
	}

	public static PatientIdentifier getPrimaryPatientIdentifierForLocation(
			Patient patient, Location location) {

		List<PatientIdentifier> piList = patient.getActiveIdentifiers();
		for (PatientIdentifier piTmp : piList) {

			if (piTmp
					.getIdentifierType()
					.getPatientIdentifierTypeId()
					.equals(getPrimaryPatientIdentiferType()
							.getPatientIdentifierTypeId())
					&& piTmp.getLocation().getLocationId()
							.equals(location.getLocationId())) {
				return piTmp;
			}
		}

		return null;

	}

	public static Location getLocationLoggedIn() {

		/**
		 * This location is hard coded because this is the quick way of getting
		 * this location by ID
		 */
		String locationId = Context.getAdministrationService()
				.getGlobalProperty(
						BillingConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION);
		return Context.getLocationService().getLocation(
				Integer.valueOf(locationId));
	}

	/**
	 * Checks whether the matching patient has an insurance already that matches
	 * its PatientIdentifier
	 * 
	 * @param patient
	 *            the patient to be matched
	 * @return true if the patient does not have any, false otherwise
	 */
	public static boolean insuranceDoesNotExist(Patient patient) {

		/** Getting the Patient Identifier from the system **/
		String insuranceCardNo = "";

		if (InsurancePolicyUtil.getPrimaryPatientIdentifierForLocation(patient,
				InsurancePolicyUtil.getLocationLoggedIn()) != null) {
			InsurancePolicyUtil.getPrimaryPatientIdentifierForLocation(patient,
					InsurancePolicyUtil.getLocationLoggedIn()).getIdentifier();
		}

		
		if (getService().getInsurancePolicyByCardNo(insuranceCardNo) == null)
			return true;
		else
			return false;
	}

	/**
	 * Checks whether the matching patient has an insurance already that matches
	 * its PatientIdentifier
	 * 
	 * @param patient
	 *            the patient to be matched
	 * @return true if the patient does not have any, false otherwise
	 */
	public static boolean insuranceDoesNotExist(Patient patient,
			String insuranceCardNo) {

		/** Getting the Patient Identifier from the system **/
		InsurancePolicy card = getService().getInsurancePolicyByCardNo(
				insuranceCardNo);

		if (card != null && !card.getOwner().equals(patient))
			return true;
		else if (card == null)
			return true;
		else
			return false;
	}

	/**
	 * Adds years to the given date
	 * 
	 * @param date
	 *            the given date
	 * @param years
	 *            to be added
	 * @return the date with Years added
	 */
	public static Date addYears(Date date, int years) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, years); // minus number would decrement the days
		return cal.getTime();
	}

	/**
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public static List<ThirdParty> getAllThirdParties() {

		List<ThirdParty> parts = getService().getAllThirdParties();

		if (parts != null)
			return parts;
		else
			return new ArrayList<ThirdParty>();
	}

	/**
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public static ThirdParty getThirdParty(Integer thirdPartyId) {

		return getService().getThirdParty(thirdPartyId);
	}

	/**
	 * Gets all Third Parties
	 * 
	 * @return
	 */
	public static void saveThirdParty(ThirdParty thirdParty) {

		thirdParty.setVoided(false);
		thirdParty.setCreator(Context.getAuthenticatedUser());
		thirdParty.setCreatedDate(new Date());

		getService().saveThirdParty(thirdParty);
	}

	/**
	 * Voids Third Party that is provided
	 * 
	 * @param thirdParty
	 *            the ThirdParty to void
	 */
	public static void voidThirdParty(ThirdParty thirdParty) {

		thirdParty.setVoided(true);
		thirdParty.setVoidedBy(Context.getAuthenticatedUser());
		thirdParty.setVoidedDate(new Date());
		thirdParty.setVoidReason("This Third Party : "
				+ thirdParty.getName().toUpperCase()
				+ " is no longer in use...");

		getService().saveThirdParty(thirdParty);
	}

	/**
	 * Gets all PolicyIds that are associated to the given patient
	 * 
	 * @param patientId
	 *            the patient ID to match
	 * @return list of String[] : {INSURANCE NAME, POLICY ID}
	 */
	public static List<String[]> getPolicyIdByPatient(Integer patientId) {

		return getService().getPolicyIdByPatient(patientId);
	}
	/**
	 * Checks whether there is an insurance policy associated insurance card number
	 * @param insuranceCardNo
	 * @return true if the insurance exists,
	 * else return false if the insurance policy does not exist
	 */
	public  static boolean isInsurancePolicyExists(String insuranceCardNo){
		
		InsurancePolicy insurancePolicy = getService().getInsurancePolicyByCardNo(insuranceCardNo);
		if (insurancePolicy !=null) {
			return true;			
		}
		else return false;		
	}
	
	public static InsurancePolicy getInsurancePolicyByThirdParty(ThirdParty t){

	   	System.out.print(" am getting in getinsurancepolicybythird party ttttttttttttttttttttt "+t);

		return getService().getInsurancePolicyByThirdParty(t);
	}

}
