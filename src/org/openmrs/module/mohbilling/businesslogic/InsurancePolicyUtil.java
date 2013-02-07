package org.openmrs.module.mohbilling.businesslogic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceCategory;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * Helper class to support the InsurancePolicy domain. (in other words, the
 * insurance card)
 * 
 * Parent class is InsurancePolicy, child class is Beneficiary
 * 
 * @author dthomas
 * 
 */
public class InsurancePolicyUtil {

	public static List<InsurancePolicy> getValidInsurancePolicyOnDate(
			Beneficiary beneficiary, Date date) {
		BillingService service = Context.getService(BillingService.class);
		List<InsurancePolicy> insurancePolicies = new ArrayList<InsurancePolicy>();
		for (InsurancePolicy insurancePolicy : service
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
		BillingService service = Context.getService(BillingService.class);
		List<InsurancePolicy> insurancePolicies = new ArrayList<InsurancePolicy>();
		for (InsurancePolicy insurancePolicy : service
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

		BillingService bs = Context.getService(BillingService.class);

		if (beneficiary != null) {
			beneficiary.setRetired(false);
			beneficiary.setCreatedDate(new Date());
			beneficiary.setCreator(Context.getAuthenticatedUser());

			beneficiary.getInsurancePolicy().addBeneficiary(beneficiary);
			bs.saveInsurancePolicy(beneficiary.getInsurancePolicy());

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
		BillingService billingService = Context
				.getService(BillingService.class);

		if (card != null) {

			card.setCreatedDate(new Date());
			card.setCreator(Context.getAuthenticatedUser());
			card.setRetired(false);
			if (card.getInsurance().getCategory()
					.equals(InsuranceCategory.NONE.toString())) {
				card.setInsuranceCardNo(card.getOwner().getId() + "");
				card.setCoverageStartDate(new Date());
			}

			billingService.saveInsurancePolicy(card);

			/**
			 * Creating the owner who is at the same time the very first
			 * beneficiary of this insurance policy (In this case
			 * <code>insuranceCardNo == policyIdNumber</code>)
			 */
			Beneficiary beneficiary = new Beneficiary();

			beneficiary.setInsurancePolicy(card);
			beneficiary.setPolicyIdNumber(card.getInsuranceCardNo());
			beneficiary.setCreatedDate(card.getCreatedDate());
			beneficiary.setCreator(card.getCreator());
			beneficiary.setPatient(card.getOwner());
			beneficiary.setRetired(card.isRetired());
			beneficiary.setRetiredBy(card.getRetiredBy());
			beneficiary.setRetiredDate(card.getRetiredDate());
			beneficiary.setRetireReason(card.getRetireReason());

			card.addBeneficiary(beneficiary);

			billingService.saveInsurancePolicy(card);

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
		BillingService billingService = Context
				.getService(BillingService.class);

		if (card != null) {
			billingService.saveInsurancePolicy(card);
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
		BillingService billingService = Context
				.getService(BillingService.class);
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

		billingService.saveInsurancePolicy(card);

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
		BillingService billingService = Context
				.getService(BillingService.class);

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

					billingService.saveInsurancePolicy(beneficiary
							.getInsurancePolicy());
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

		BillingService billingService = Context
				.getService(BillingService.class);
		if (beneficiary != null)
			for (InsurancePolicy card : billingService
					.getAllInsurancePolicies())

				for (Beneficiary ben : card.getBeneficiaries())
					if (ben.equals(beneficiary))
						return card;

		return null;
	}

	/**
	 * Gets the Insurance policy that matches the provided Owner
	 * 
	 * @param patient
	 *            the Owner to be matched
	 * @return card if matched, null otherwise
	 */
	public static InsurancePolicy getInsurancePolicyByOwner(Patient patient) {

		BillingService billingService = Context
				.getService(BillingService.class);
		if (patient != null)
			for (InsurancePolicy card : billingService
					.getAllInsurancePolicies())

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
		BillingService billingService = Context
				.getService(BillingService.class);
		List<InsurancePolicy> allCards = billingService
				.getAllInsurancePolicies();
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
		BillingService billingService = Context
				.getService(BillingService.class);
		List<InsurancePolicy> insurancePolicies = billingService
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
		BillingService billingService = Context
				.getService(BillingService.class);
		List<InsurancePolicy> allCards = billingService
				.getAllInsurancePolicies();
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

		BillingService service = Context.getService(BillingService.class);
		InsurancePolicy card = null;
		List<InsurancePolicy> allCards = service.getAllInsurancePolicies();
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

		BillingService service = Context.getService(BillingService.class);

		for (InsurancePolicy card : service.getAllInsurancePolicies())
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

		BillingService service = Context.getService(BillingService.class);
		for (InsurancePolicy card : service.getAllInsurancePolicies()) {
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

		BillingService service = Context.getService(BillingService.class);
		for (InsurancePolicy card : service.getAllInsurancePolicies()) {
			for (Beneficiary ben : card.getBeneficiaries()) {
				if (ben.getPolicyIdNumber().equals(policyIdNumber))
					return ben;
			}
		}
		return null;
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

		BillingService service = Context.getService(BillingService.class);
		List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		if (patient != null)
			for (InsurancePolicy ip : service.getAllInsurancePolicies())
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

		BillingService service = Context.getService(BillingService.class);
		List<Beneficiary> beneficiaries = new ArrayList<Beneficiary>();

		if (owner != null)
			for (InsurancePolicy ip : service.getAllInsurancePolicies())
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

}
