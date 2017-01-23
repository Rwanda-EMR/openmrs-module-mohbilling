package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.util.*;

/**
 * This is a helper class for the Insurance domain, to contain all business
 * logic.
 * 
 * Parent Class is Insurance, child classes are ServiceCategory, and
 * InsuranceRate
 * 
 * @author dthomas
 * 
 */
public class InsuranceUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	private static Comparator<BillableService> BILLABLE_SERVICE_COMPARATOR = new Comparator<BillableService>() {
		// This is where the sorting happens.
		public int compare(BillableService bs1, BillableService bs2) {
			int categoryDifference = bs1
					.getServiceCategory()
					.getName()
					.toLowerCase()
					.compareTo(bs2.getServiceCategory().getName().toLowerCase());

			if (categoryDifference == 0)
				return bs1
						.getFacilityServicePrice()
						.getName()
						.toLowerCase()
						.compareTo(
								bs2.getFacilityServicePrice().getName()
										.toLowerCase());
			else
				return categoryDifference;
		}
	};

	/**
	 * Auto generated method comment
	 * 
	 * @param insurance
	 * @param date
	 * @return
	 */
	public static List<ServiceCategory> getServiceCategories(
			Insurance insurance, Date date) {

		List<ServiceCategory> serviceCategories = new ArrayList<ServiceCategory>();
		for (ServiceCategory serviceCategory : insurance.getCategories()) {

			if (serviceCategory.getRetiredDate() != null) {
				if (serviceCategory.getRetiredDate().compareTo(date) > 0) {
					serviceCategories.add(serviceCategory);
				}
			} else {
				serviceCategories.add(serviceCategory);
			}
		}

		return serviceCategories;

	}

	/**
	 * @param insurance
	 * @param name
	 * @param address
	 * @param phone
	 */
	public static void updateInsuranceInfomations(Insurance insurance,
			String name, String address, String phone) {

		if (name != null) {
			insurance.setName(name);
		}
		if (address != null) {
			insurance.setAddress(address);
		}
		if (phone != null) {
			insurance.setPhone(phone);
		}

		getService().saveInsurance(insurance);
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param insurance
	 * @param creator
	 * @param createdDate
	 * @param concept
	 */
	public static void updateInsuranceCreationInfos(Insurance insurance,
			User creator, Date createdDate, Concept concept) {

		if (creator != null) {
			insurance.setCreator(creator);
		}
		if (createdDate != null) {
			insurance.setCreatedDate(createdDate);
		}
		if (concept != null) {
			insurance.setConcept(concept);
		}

		getService().saveInsurance(insurance);
	}

	/**
	 * @param name
	 * @param address
	 * @param phone
	 * @param concept
	 */
	public static void createInsurance(String name, String address,
			String phone, Concept concept) {

		Insurance insurance = new Insurance();

		insurance.setName(name);
		insurance.setAddress(address);
		insurance.setPhone(phone);
		insurance.setConcept(concept);
		insurance.setCreator(Context.getAuthenticatedUser());
		insurance.setCreatedDate(new Date());

		getService().saveInsurance(insurance);
	}

	/**
	 * Creates an new or edit an existing one. It just offers the option of
	 * adding only and only one rate to the given insurance. If InsuranceRate is
	 * not provided, it will just save the insurance.
	 * 
	 * @param insurance
	 *            the insurance to be added/edited
	 * @param rate
	 *            the rate to be added
	 * @return the saved insurance
	 */
	public static Insurance createInsurance(Insurance insurance,
			InsuranceRate rate) {

		if (insurance != null) {
			if (rate != null) {
				if (insurance.getRates() != null && rate.isRetired() != null)
					for (InsuranceRate ir : insurance.getRates()) {
						if (!ir.isRetired() && ir.getRetiredDate() == null) {
							ir.setRetiredDate(rate.getStartDate());
							retireInsuranceRate(
									insurance,
									ir,
									"A New Insurance Rate of : '"
											+ rate.getRate()
											+ "%' and Flat fee of : '"
											+ rate.getFlatFee()
											+ "' is created.");
							break;
						}
					}

				insurance.addInsuranceRate(rate);
			}

			getService().saveInsurance(insurance);
			return insurance;
		} else
			return null;
	}

	/**
	 * At the same time we retire the existing ones or the current (valid) one :
	 * see Edit link in insurance form. Look through the Rates and retire all
	 * existing Rates. Also check the StartDate is not ranged in the previous
	 * Rate validity dates range (this has to be implemented in the business
	 * logic)
	 * 
	 * @param insurance
	 * @param Rate
	 * @param flatFee
	 * @param startDate
	 */
	public static void createInsuranceRate(Insurance insurance, Float rate,
			BigDecimal flatFee, Date startDate) {
		// Create a new insurance rate will be the Retire Reason for the current
		// one to be retired
		InsuranceRate insuranceRate = new InsuranceRate();

		insuranceRate.setFlatFee(flatFee);
		insuranceRate.setRate(rate);
		insuranceRate.setStartDate(startDate);
		insuranceRate.setCreator(Context.getAuthenticatedUser());
		insuranceRate.setCreatedDate(new Date());

		if (insurance.getRates() != null)
			for (InsuranceRate ir : insurance.getRates()) {
				if (!ir.isRetired() && ir.getRetiredDate() == null) {
					ir.setRetiredDate(startDate);
					retireInsuranceRate(
							insurance,
							ir,
							"A New Insurance Rate of : '"
									+ insuranceRate.getRate()
									+ "%' and Flat fee of : '"
									+ insuranceRate.getFlatFee()
									+ "' is created.");
					break;
				}
			}

		insurance.addInsuranceRate(insuranceRate);
		getService().saveInsurance(insurance);
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param insurance
	 * @param rate
	 * @param reason
	 */
	public static void retireInsuranceRate(Insurance insurance,
			InsuranceRate rate, String reason) {

		// Retire the previous rate.
		for (InsuranceRate ir : insurance.getRates()) {
			if (ir.equals(rate)) {
				ir.setRetiredBy(Context.getAuthenticatedUser());
				ir.setRetiredDate(new Date());
				ir.setEndDate(new Date());
				ir.setRetireReason(reason);
				ir.setRetired(true);
				break;
			}
		}

		getService().saveInsurance(insurance);
	}

	/**
	 * Adds a new Insurance Rate instead of editing the existing that has to be
	 * retired in this case.
	 * 
	 * @param insurance
	 * @param rate
	 */
	public static void editInsuranceRate(Insurance insurance, InsuranceRate rate) {

		for (InsuranceRate ir : insurance.getRates()) {
			if (ir.equals(rate)) {
				ir.setRetireReason("The previous Insurance Rate of ID: '"
						+ ir.getInsuranceRateId()
						+ "' was misspelled and mistaken");
				ir.setRetired(true);
				break;
			}
		}

		getService().saveInsurance(insurance);

	}

	/**
	 * This is the same as Saving an insurance: addNewInsurance();
	 * 
	 * @param insurance
	 */
	public static void editInsurance(Insurance insurance) {

		if (insurance != null)
			getService().saveInsurance(insurance);
	}

	/**
	 * This involves retiring current (not retired) InsuranceRate
	 * 
	 * @param insurance
	 * @param voidReason
	 */
	public static void voidInsurance(Insurance insurance, String voidReason) {

		if (!insurance.isVoided()) {
			insurance.setVoided(true);
			insurance.setVoidedBy(Context.getAuthenticatedUser());
			insurance.setVoidedDate(new Date());
			insurance.setVoidReason(voidReason);

			for (InsuranceRate ir : insurance.getRates()) {
				if (!ir.isRetired()) {
					ir.setRetired(true);
					ir.setRetiredBy(Context.getAuthenticatedUser());
					ir.setRetiredDate(new Date());
					ir.setRetireReason("The Insurance " + insurance.getName()
							+ " has been voided");
				}
			}
			getService().saveInsurance(insurance);
		}
	}

	/**
	 * This has to be on the Insurance Object. This will use the
	 * "getRateOnDate(Date date)" where date is the "new Date()" i.e. today's
	 * date
	 * 
	 * @return
	 */
	public static InsuranceRate getCurrentRate(Insurance insurance) {

		return insurance.getRateOnDate(new Date());
	}

	/**
	 * Looking through all existing Rates for the given Insurance and display
	 * (return) String msg saying that the StartDate entered by the user is in
	 * Dates Ranges of previous Insurance Rates: considering the Rates related
	 * to the given Insurance.}
	 * 
	 * @param insurance
	 * @param startDate
	 * @return
	 */
	public static String checkRateStartDateConflict(Insurance insurance,
			Date startDate) {

		String message = "";

		for (InsuranceRate ir : insurance.getRates()) {
			if (ir.getEndDate() != null)
				if (ir.getEndDate().compareTo(startDate) > 0
						|| ir.getStartDate().compareTo(startDate) > 0) {
					message = "The entered Start Date ("
							+ startDate.toString()
							+ ") is in the range of an existing Insurance Rate (i.e. From '"
							+ ir.getStartDate() + "' To '" + ir.getEndDate()
							+ "')";
					break;
				}
		}
		return message;
	}

	/**
	 * Gets insurances considering the provided validity: i.e. if isValid ==
	 * true it will return the non voided Insurance, and voided ones otherwise.
	 * 
	 * @param isValid
	 *            the validity condition
	 * @return voided Insurances if isValid == false, unvoided otherwise.
	 */
	public static List<Insurance> getInsurances(Boolean isValid) {

		List<Insurance> insurances = new ArrayList<Insurance>();

		for (Insurance insurance : getService().getAllInsurances())
			if (insurance.isVoided() != isValid)
				insurances.add(insurance);

		return insurances;
	}

	/**
	 * Gets all Valid/Invalid service categories depending on the provided
	 * validity value
	 * 
	 * @param insurance
	 *            the insurance company to be matched
	 * @param isValid
	 *            the conditioning value
	 * @return voided Service Categories by Insurance if isValid == false,
	 *         unvoided otherwise
	 */
	public static List<ServiceCategory> getServiceCategoriesByInsurance(
			Insurance insurance, Boolean isValid) {

		List<ServiceCategory> categories = new ArrayList<ServiceCategory>();

		for (ServiceCategory category : getService().getInsurance(
				insurance.getInsuranceId()).getCategories()) {
			if (category.isRetired() != isValid) {
				categories.add(category);
			}
		}
		return categories;
	}

	/**
	 * Gets the Valid (not yet retired) Billable services at a Facility and by
	 * specified insurance company on a given date
	 * 
	 * @param insurance
	 * @param date
	 * @param facilityServicePrice
	 * @return
	 */
	public static List<BillableService> getBillableServicesByInsurance(
			Insurance insurance, Date date, Boolean isRetired) {

		List<BillableService> services = new ArrayList<BillableService>();

		for (FacilityServicePrice fsp : getService()
				.getAllFacilityServicePrices())
			if (!fsp.isRetired())
				for (BillableService service : fsp.getBillableServices()) {

					if (service.isRetired() == isRetired
							&& service.getInsurance() != null
							&& service.getStartDate().compareTo(date) <= 0)
						services.add(service);
					if (service.getEndDate() != null)
						if (service.getInsurance() != null
								&& service.getEndDate().compareTo(date) > 0
								&& service.getStartDate().compareTo(date) <= 0)
							services.add(service);
				}

		// Sorting by Service Category
		Collections.sort(services, BILLABLE_SERVICE_COMPARATOR);

		return services;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param sc
	 * @param date
	 * @param isRetired
	 * @return
	 */
	public static List<BillableService> getBillableServicesByServiceCategory(
			ServiceCategory sc, Date date, Boolean isRetired) {

		List<BillableService> bsByServiceCategory = getService().getBillableServiceByCategory(sc);
		
		// Sorting by Service Category
		Collections.sort(bsByServiceCategory, BILLABLE_SERVICE_COMPARATOR);
		return bsByServiceCategory;
	}

	/**
	 * Saves the Billable service into the DB
	 * 
	 * @param service
	 *            the Billable service to be saved
	 * @return the saved Billable service
	 */

	// We will have to remove the "Maxima to pay" label from the Billable
	// Service form on the GUI. Or just find a way of handling it.
	public static BillableService saveBillableService(BillableService service) {

		FacilityServicePrice fsp = service.getFacilityServicePrice();
		BigDecimal quarter = new BigDecimal(25).divide(new BigDecimal(100));
		BigDecimal fifth = new BigDecimal(20).divide(new BigDecimal(100));

		if (service != null && fsp != null) {

			BigDecimal amount = fsp.getFullPrice();

			// This means the Maxima to Pay is not set in the service
			if (service.getMaximaToPay() == null) {
				/*if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.BASE.toString())) {
					service.setMaximaToPay(amount);
				}*/
				if (service
						.getInsurance()
						.getCategory()
						.equalsIgnoreCase(InsuranceCategory.MUTUELLE.toString())) {

					if (!service.getServiceCategory().getName()
							.equalsIgnoreCase("MEDICAMENTS")
							&& !service.getServiceCategory().getName()
							.equalsIgnoreCase("CONSOMABLE")) {
						service.setMaximaToPay(amount.divide(new BigDecimal(2)));

					} else {
						service.setMaximaToPay(amount);
					}
				}
				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.RSSB.toString())) {
					service.setMaximaToPay(amount.multiply(new BigDecimal(1.25)));
				}
				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.MMI_UR.toString())) {
					service.setMaximaToPay(amount.multiply(new BigDecimal(1.15)));
				}
				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.PRIVATE.toString())) {
					service.setMaximaToPay(amount.multiply(new BigDecimal(1.4375)));
				}
				if (service.getInsurance().getCategory()
						.equalsIgnoreCase(InsuranceCategory.NONE.toString())) {
					BigDecimal initial = amount.multiply(new BigDecimal(1.725));
					service.setMaximaToPay(initial);
				}
			} else
				//This happens when Maxima to Pay is set in the service
				service.setMaximaToPay(service.getMaximaToPay());

			fsp.addBillableService(service);
			getService().saveFacilityServicePrice(fsp);

			return service;
		} else
			return null;
	}
	
	public static BillableService saveBillableServiceForPharmacyItems(BillableService service) {
		
		FacilityServicePrice fsp = service.getFacilityServicePrice();

		if (service != null && fsp != null) {

			BigDecimal amount = fsp.getFullPrice();

			// This means the Maxima to Pay is not set in the service
			if (service.getMaximaToPay() == null) {
				service.setMaximaToPay(amount);
			} else
				// This happens when Maxima to Pay is set in the service
				service.setMaximaToPay(service.getMaximaToPay());

			fsp.addBillableService(service);
			getService().saveFacilityServicePrice(fsp);

			return service;
		}
		
		return null;
	}

	public static String getCategoryName(ServiceCategory category) {
		if (category != null)
			return category.getName();
		else
			return null;
	}

	/**
	 * Updates the Billable service into the DB
	 * 
	 * @param service
	 *            the Billable service to be updated
	 * @return the updated Billable service
	 */
	public static BillableService editBillableService(BillableService service) {

		return saveBillableService(service);
	}

	/**
	 * Gets a Billable service by the provided ID
	 * 
	 * @param id
	 *            the ID to be found
	 * @return service Billable if ID matches, and null otherwise
	 */
	public static BillableService getValidBillableService(Integer id) {

		return getService().getBillableService(id);
	}

	/**
	 * Gets all Billable services on a given date and not retired if specified
	 * 
	 * @param date
	 *            the date to check the validity
	 * @param isRetired
	 *            true means "is retired" and false means "is not retired"
	 * @return the list of Billable service that matched the conditions
	 */
	public static List<BillableService> getAllBillableServices(Date date,
			Boolean isRetired) {

		List<BillableService> services = new ArrayList<BillableService>();

		for (FacilityServicePrice fsp : getService()
				.getAllFacilityServicePrices())
			if (!fsp.isRetired())
				for (BillableService service : fsp.getBillableServices()) {

					if (service.isRetired() == isRetired
							&& service.getInsurance() != null
							&& service.getStartDate().compareTo(date) <= 0)
						services.add(service);
					if (service.getEndDate() != null)
						if (service.getInsurance() != null
								&& service.getEndDate().compareTo(date) > 0
								&& service.getStartDate().compareTo(date) <= 0)
							services.add(service);
				}

		return services;
	}

	/**
	 * Gets a Service category by the provided ID
	 * 
	 * @param id
	 *            the ID to be found
	 * @return category Service if ID matches, and null otherwise
	 */
	public static ServiceCategory getValidServiceCategory(Integer id) {

		return getService().getServiceCategory(id);
	}

	/**
	 * Retires a Billable service for a given Facility Service
	 * 
	 * @param billableService
	 *            the billable service to be retired
	 * @param retiredDate
	 *            the date we retire the service
	 * @param retireReason
	 *            the reason for retirement
	 * @param facilityServicePrice
	 *            the facility service to be updated
	 */
	public static void retireBillableService(BillableService billableService,
			Date retiredDate, String retireReason) {

		FacilityServicePrice fsp = billableService.getFacilityServicePrice();

		billableService.setRetired(true);
		billableService.setRetiredBy(Context.getAuthenticatedUser());
		billableService.setRetiredDate((retiredDate != null) ? retiredDate
				: new Date());
		billableService.setRetireReason(retireReason != null
				|| retireReason != "" ? retireReason : "No reason provided");

		getService().saveFacilityServicePrice(fsp);
	}

	public static List<String> getAllServiceCategories() {

		List<String> categories = new ArrayList<String>();

		categories.add(Category.CHIRURGIE.getDescription());
		categories.add(Category.CONSOMMABLES.getDescription());
		categories.add(Category.CONSULTATION.getDescription());
		categories.add(Category.DERMATOLOGIE.getDescription());
		categories.add(Category.ECHOGRAPHIE.getDescription());
		categories.add(Category.FORMALITES_ADMINISTRATIVES.getDescription());
		categories.add(Category.HOSPITALISATION.getDescription());
		categories.add(Category.KINESITHERAPIE.getDescription());
		categories.add(Category.LABORATOIRE.getDescription());
		categories.add(Category.MATERNITE.getDescription());
		categories.add(Category.MEDECINE_INTERNE.getDescription());
		categories.add(Category.MEDICAMENTS.getDescription());
		categories.add(Category.OPHTALMOLOGIE.getDescription());
		categories.add(Category.ORL.getDescription());
		categories.add(Category.OXYGENOTHERAPIE.getDescription());
		categories.add(Category.PEDIATRIE.getDescription());
		categories.add(Category.RADIOLOGIE.getDescription());
		categories.add(Category.SOINS_INFIRMIERS.getDescription());
		categories.add(Category.SOINS_INTENSIFS.getDescription());
		categories.add(Category.STOMATOLOGIE.getDescription());
		categories.add(Category.NEUROLOGIE.getDescription());
		categories.add(Category.AUTRES.getDescription());
		categories.add(Category.AMBULANCE.getDescription());

		return categories;
	}

	public static Insurance getInsuranceByConcept(Concept concept) {

		for (Insurance insurance : getInsurances(true)) {
			if (insurance.getConcept().equals(concept))
				return insurance;
		}

		return null;
	}

	public static Insurance getInsurance(Integer insuranceId) {

		return getService().getInsurance(insuranceId);
	}

	public static List<Insurance> getAllInsurances() {
		
		return getService().getAllInsurances();
	}
	
	public static ServiceCategory getServiceCategoryById(Integer id) {

		return getService().getServiceCategory(id);
	}

}
