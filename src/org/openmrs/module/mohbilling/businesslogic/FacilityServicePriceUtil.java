package org.openmrs.module.mohbilling.businesslogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;

/**
 * 
 * Class to contain all of the business logic helper classes for the creation of
 * facility services, and billable service items.
 * 
 * The parent class is FacilityServicePrice, and BillableServices are child
 * objects.
 * 
 * @author Kamonyo
 * 
 */
public class FacilityServicePriceUtil {

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

	private static Comparator<FacilityServicePrice> FACILITY_SERVICE_PRICE_COMPARATOR = new Comparator<FacilityServicePrice>() {
		// This is where the sorting happens.
		public int compare(FacilityServicePrice fsp1, FacilityServicePrice fsp2) {

			return fsp1.getName().toLowerCase()
					.compareTo(fsp2.getName().toLowerCase());
		}
	};

	/**
	 * Gets all Facility Services at a given location and on a given date (they
	 * may be retired or not)
	 * 
	 * @param location
	 *            the location to be matched
	 * @param date
	 *            the date to test the validity
	 * @param isRetired
	 *            the parameter that specifies whether the facility service is
	 *            retired or not
	 * @return list of valid FacilityServicePrices if
	 *         <code>isRetired==false</code>, and retired otherwise
	 */
	public static List<FacilityServicePrice> getFacilityServicesOnDate(
			Location location, Date date, Boolean isRetired) {

		BillingService bs = Context.getService(BillingService.class);
		List<FacilityServicePrice> fspList = new ArrayList<FacilityServicePrice>();

		for (FacilityServicePrice fs : bs.getAllFacilityServicePrices()) {
			if (fs.getEndDate() != null)
				if (fs.isRetired() == isRetired
						&& fs.getLocation().equals(location)
						&& fs.getStartDate().compareTo(date) <= 0
						&& fs.getEndDate().compareTo(date) >= 0)
					fspList.add(fs);
			if (!fs.isRetired() && fs.getLocation().equals(location)
					&& fs.getStartDate().compareTo(date) <= 0)
				fspList.add(fs);
		}

		return fspList;
	}

	/**
	 * Gets all Valid Billable services in a given service category
	 * 
	 * @param serviceCategory
	 *            the service category to be considered
	 * @param date
	 *            the date of validity
	 * @return list of BillableService
	 */
	public static List<BillableService> getBillableServicesByCategory(
			ServiceCategory serviceCategory, Date date, Boolean isValid) {

		List<BillableService> services = new ArrayList<BillableService>();
		BillingService bs = Context.getService(BillingService.class);

		for (FacilityServicePrice fsp : bs.getAllFacilityServicePrices())
			if (!fsp.isRetired())
				for (BillableService service : fsp.getBillableServices()) {

					if (service.isRetired() != isValid
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
	 * Gets the Valid (not yet retired) Billable services at a Facility and by
	 * specified insurance company on a given date
	 * 
	 * @param insurance
	 * @param date
	 * @param facilityServicePrice
	 * @return
	 */
	public static List<BillableService> getBillableServicesByInsurance(
			Insurance insurance, Date date) {

		List<BillableService> services = new ArrayList<BillableService>();
		BillingService bs = Context.getService(BillingService.class);

		for (FacilityServicePrice fsp : bs.getAllFacilityServicePrices())
			if (!fsp.isRetired())
				for (BillableService service : fsp.getBillableServices()) {

					if (service.getInsurance() != null
							&& service.getInsurance().getInsuranceId()
									.intValue() == insurance.getInsuranceId()
									.intValue()) {
						if (!service.isRetired()
								&& service.getInsurance() != null
								&& service.getStartDate().compareTo(date) <= 0)
							services.add(service);
						if (service.getEndDate() != null)
							if (service.getInsurance() != null
									&& service.getEndDate().compareTo(date) > 0
									&& service.getStartDate().compareTo(date) <= 0)
								services.add(service);
					}
				}

		// Sorting by Service Category
		Collections.sort(services, BILLABLE_SERVICE_COMPARATOR);

		return services;

	}

	/**
	 * Gets all Billable services on a given date and not retired if specified
	 * 
	 * @param fsp
	 *            the Facility service to be matched
	 * @param date
	 *            the date to check the validity
	 * @param isRetired
	 *            true means "is retired" and false means "is not retired"
	 * @return the list of Billable service that matched the conditions
	 */
	public static List<BillableService> getBillableServices(
			FacilityServicePrice fsp, Date date, Boolean isRetired) {

		List<BillableService> services = new ArrayList<BillableService>();

		if (fsp != null)
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
		BillingService bs = Context.getService(BillingService.class);

		for (FacilityServicePrice fsp : bs.getAllFacilityServicePrices())
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

		BillingService service = Context.getService(BillingService.class);
		FacilityServicePrice fsp = billableService.getFacilityServicePrice();

		billableService.setRetired(true);
		billableService.setRetiredBy(Context.getAuthenticatedUser());
		billableService.setRetiredDate((retiredDate != null) ? retiredDate
				: new Date());
		billableService.setRetireReason(retireReason != null
				|| retireReason != "" ? retireReason : "No reason provided");

		service.saveFacilityServicePrice(fsp);
	}

	/**
	 * Retires the Facility Service at same time it has to retire its
	 * BillableServices where they are not
	 * 
	 * @param facilityServicePrice
	 *            the facility service to be retired
	 * @param retiredDate
	 *            the date of retirement
	 * @param retireReason
	 *            the reason of retirement
	 */
	public static void retireFacilityServicePrice(
			FacilityServicePrice facilityServicePrice, Date retiredDate,
			String retireReason) {

		BillingService service = Context.getService(BillingService.class);

		for (BillableService bs : facilityServicePrice.getBillableServices()) {
			if (!bs.isRetired()) {
				bs.setRetired(true);
				bs.setRetiredBy(Context.getAuthenticatedUser());
				bs.setRetiredDate(retiredDate != null ? retiredDate
						: new Date());
				bs.setRetireReason("The parent Facility Service (No :"
						+ facilityServicePrice.getFacilityServicePriceId()
						+ ") was retired as well because '" + retireReason
						+ "'");
			}
		}

		facilityServicePrice.setRetired(true);
		facilityServicePrice.setRetiredBy(Context.getAuthenticatedUser());
		facilityServicePrice.setRetiredDate(retiredDate != null ? retiredDate
				: new Date());
		facilityServicePrice.setRetireReason(retireReason != null
				|| retireReason != "" ? retireReason : "No reason provided");

		service.saveFacilityServicePrice(facilityServicePrice);
	}

	/**
	 * Gets the Facility Service that mathces the given OpenMRS Concept
	 * 
	 * @param concept
	 *            the concept to be compared to the Facility service
	 * @return the FacilityServicePrice when it matches the concept, null
	 *         otherwise
	 */
	public static FacilityServicePrice getFacilityServiceByConcept(
			Concept concept) {

		BillingService bs = Context.getService(BillingService.class);

		for (FacilityServicePrice fsp : bs.getAllFacilityServicePrices()) {
			if (fsp.getConcept().equals(concept) && !fsp.isRetired())
				return fsp;
		}
		return null;
	}

	/**
	 * Creates a Facility service and at the same time create the Billable
	 * service for non-insured patients (Billable service StartDate and EndDate
	 * are same as the Facility Service ones, and the MaximaToPay is the same
	 * amount as the FullPrice of the Facility Service)
	 * 
	 * @param fsp
	 *            the Facility service to be added to the DB
	 */
	public static FacilityServicePrice createFacilityService(
			FacilityServicePrice fsp) {

		if (fsp != null) {
			BillingService bs = Context.getService(BillingService.class);
			BillableService service = new BillableService();

			// Adding the Billable Service automatically for non insured
			// patients.
			service.setCreatedDate(fsp.getCreatedDate());
			service.setCreator(fsp.getCreator());
			service.setFacilityServicePrice(fsp);
			service.setStartDate(fsp.getStartDate());
			service.setMaximaToPay(fsp.getFullPrice());
			service.setRetired(fsp.isRetired());

			// Adding the Billable service to the FSP.
			fsp.addBillableService(service);

			bs.saveFacilityServicePrice(fsp);

			return fsp;
		}

		return null;
	}

	/**
	 * Edits a Facility service and at the same time create the Billable service
	 * for non-insured patients (Billable service StartDate and EndDate are same
	 * as the Facility Service ones, and the MaximaToPay is the same amount as
	 * the FullPrice of the Facility Service)
	 * 
	 * @param fsp
	 *            the Facility service to be added to the DB
	 */
	public static FacilityServicePrice editFacilityService(
			FacilityServicePrice fsp) {

		if (fsp != null) {
			BillingService bs = Context.getService(BillingService.class);
			BillableService service = new BillableService();

			// Editing the Billable Service automatically for non insured
			// patients.
			service.setCreatedDate(fsp.getCreatedDate());
			service.setCreator(fsp.getCreator());
			service.setFacilityServicePrice(fsp);
			service.setStartDate(fsp.getStartDate());
			service.setMaximaToPay(fsp.getFullPrice());
			service.setRetired(false);

			// Retiring the existing Billable service
			for (BillableService serv : fsp.getBillableServices()) {
				if (serv.isRetired()) {
					retireBillableService(serv, fsp.getStartDate(),
							"The facility service has changed");
				}
			}
			// Editing the Billable service to the FSP.
			fsp.addBillableService(service);

			bs.saveFacilityServicePrice(fsp);

			return fsp;
		}

		return null;
	}

	/**
	 * Gets Facility Services considering the provided validity: i.e. if isValid
	 * == true it will return the non retired Facility Services, and retired
	 * ones otherwise.
	 * 
	 * @param isValid
	 *            the validity condition
	 * @return retired Facility Services if isValid == false, unvoided
	 *         otherwise.
	 */
	public static List<FacilityServicePrice> getFacilityServices(Boolean isValid) {

		BillingService bs = Context.getService(BillingService.class);
		List<FacilityServicePrice> fspList = new ArrayList<FacilityServicePrice>();

		for (FacilityServicePrice fs : bs.getAllFacilityServicePrices())
			if (fs.isRetired() != isValid)
				fspList.add(fs);

		// Sorting Facility Service Price by Name
		Collections.sort(fspList, FACILITY_SERVICE_PRICE_COMPARATOR);

		return fspList;
	}

}
