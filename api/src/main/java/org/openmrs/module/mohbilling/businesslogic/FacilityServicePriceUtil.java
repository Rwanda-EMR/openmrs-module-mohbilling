package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * Class to contain all of the business logic helper classes for the creation of
 * facility services, and billable service items.
 * 
 * The parent class is FacilityServicePrice, and BillableServices are child
 * objects.
 * 
 * @author EMR-RBC
 * 
 */
public class FacilityServicePriceUtil {
	
	private static Log log = LogFactory.getLog(FacilityServicePriceUtil.class);

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

		List<FacilityServicePrice> fspList = new ArrayList<FacilityServicePrice>();

		for (FacilityServicePrice fs : getService()
				.getAllFacilityServicePrices()) {
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

		for (FacilityServicePrice fsp : getService()
				.getAllFacilityServicePrices())
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

		List<BillableService> services = getService()
				.getBillableServicesByInsurance(insurance);

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

		return getService().getBillableServicesByFacilityService(fsp);
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
	// @param facilityServicePrice
	//  the facility service to be updated
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

		getService().saveFacilityServicePrice(facilityServicePrice);
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

		return getService().getFacilityServiceByConcept(concept);
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

			BillableService service = new BillableService();

			// Adding the Billable Service automatically for non insured
			// patients.
//			service.setCreatedDate(fsp.getCreatedDate());
//			service.setCreator(fsp.getCreator());
//			service.setFacilityServicePrice(fsp);
//			service.setStartDate(fsp.getStartDate());
//			service.setMaximaToPay(fsp.getFullPrice());
//			service.setRetired(fsp.isRetired());

			// Adding the Billable service to the FSP.
//			fsp.addBillableService(service);

			getService().saveFacilityServicePrice(fsp);

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

			BillableService service = new BillableService();

			// Editing the Billable Service automatically for non insured
			// patients.
//			service.setCreatedDate(fsp.getCreatedDate());
//			service.setCreator(fsp.getCreator());
//			service.setFacilityServicePrice(fsp);
//			service.setStartDate(fsp.getStartDate());
//			service.setMaximaToPay(fsp.getFullPrice());
//			service.setRetired(false);
			
			

			// Retiring the existing Billable service
			for (BillableService serv : fsp.getBillableServices()) {
				if (serv.isRetired()) {
					retireBillableService(serv, fsp.getStartDate(),
							"The facility service has changed");
					
					
				}
			}
			// Editing the Billable service to the FSP.
//			fsp.addBillableService(service);

			getService().saveFacilityServicePrice(fsp);
			


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

		List<FacilityServicePrice> fspList = getService()
				.getAllFacilityServicePrices();

		// Sorting Facility Service Price by Name
		Collections.sort(fspList, FACILITY_SERVICE_PRICE_COMPARATOR);

		return fspList;
	}

	/**
	 * Gets a BillableService by selecting those having the provided Concept
	 * (passing by FacilityServicePrice, as it is the one having Concept) and
	 * the Insurance.
	 * 
	 * @param concept
	 *            the Concept to retrieve the corresponding FacilityServicePrice
	 * @param insurance
	 *            the Insurance
	 * @return BillableService that matches both FacilityServicePrice and
	 *         Insurance
	 */
	public static BillableService getBillableServiceByConcept(Concept concept,
			Insurance insurance) {

		return getService().getBillableServiceByConcept(
				getFacilityServiceByConcept(concept), insurance);
	}

	/**
	 * Adds Category to All Facility Services that do not have it...
	 * 
	 * @param category
	 *            the one to be added
	 */
	public static Boolean addCategoryToAllFacilityServices(String category) {

		Insurance insurance = null;
		for (Insurance ins : InsuranceUtil.getAllInsurances()) {
			if (ins.getCategory().equalsIgnoreCase(category)) {
				insurance = ins;
				break;
			}
		}

		if (insurance != null) {

			for (BillableService billable : getBillableServicesByInsurance(
					insurance, null)) {

				if (billable.getServiceCategory() != null) {

					FacilityServicePrice fsp = billable
							.getFacilityServicePrice();
					if (fsp.getCategory() == null)
						fsp.setCategory(billable.getServiceCategory().getName());

					getService().saveFacilityServicePrice(fsp);
				}
			}
			return true;

		} else
			return false;
	}

	/**
	 * Gets FacilityServicePrice by facilityId
	 * 
	 * @param facilityId
	 *            the ID to match
	 * @return facilityServicePrice that matches the ID
	 */
	public static FacilityServicePrice getFacilityServicePrice(
			Integer facilityId) {

		return getService().getFacilityServicePrice(facilityId);
	}

	/**
	 * Adds Category to Facility Service Price where it misses
	 * 
	 * @param facilityServicePrice
	 *            the one to be updated
	 */
	public static void addCategoryToFacilityService(
			FacilityServicePrice facilityServicePrice) {

		for (BillableService bill : getBillableServices(facilityServicePrice,
				null, null)) {

			if (bill.getServiceCategory() != null) {

				facilityServicePrice.setCategory(bill.getServiceCategory()
						.getName());
				getService().saveFacilityServicePrice(facilityServicePrice);
				break;
			}
		}
	}
	
	public static boolean isBillableCreated(FacilityServicePrice facilityService, Insurance insurance) {
		BillableService billableService = getService().getBillableServiceByConcept(facilityService, insurance);
		if(billableService != null)
			return true;		
		return false;
	}
	
	public static String saveBillableServiceByInsurance(HttpServletRequest request) {
		List<Insurance> insurances = getService().getAllInsurances();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		FacilityServicePrice fsp = null;
		BillableService bs = new BillableService();
		
		BillableService bscopy = new BillableService();
		
		String startDateStr = null, msg = null;
		Date startDate = null;
		
		if(request.getParameter("startDate") != null && !request.getParameter("startDate").equals("")
				&& request.getParameter("facilityServiceId") != null && !request.getParameter("facilityServiceId").equals("")) {
			System.out.println("Facility Service Id: " + request.getParameter("facilityServiceId"));
			
			startDateStr = request.getParameter("startDate");
			
			try {
				startDate = sdf.parse(startDateStr.split("/")[2] + "-" + startDateStr.split("/")[1] + "-" + startDateStr.split("/")[0]);
			} catch (ParseException e) {
				e.printStackTrace();
			}			
			
			fsp = getService().getFacilityServicePrice(Integer.valueOf(request.getParameter("facilityServiceId")));
		}
		
		BigDecimal quarter = new BigDecimal(25).divide(new BigDecimal(100));
		BigDecimal fifth = new BigDecimal(20).divide(new BigDecimal(100));
		
	
		
		for(Insurance insurance : insurances) {
			if(!insurance.isVoided())			
				try {
					if(!fsp.getCategory().toLowerCase().equals("medicaments") && !fsp.getCategory().toLowerCase().equals("consommables")&&!fsp.getCategory().equals("AUTRES")) {
									
						if(FacilityServicePriceUtil.isBillableCreated(fsp, insurance)) {
							System.out.println("The bill exist already");
							
							bs = getService().getBillableServiceByConcept(fsp, insurance);
							bs.setStartDate(startDate);
							bs.setInsurance(insurance);
							bs.setServiceCategory(getService().getServiceCategoryByName(fsp.getCategory(), insurance));
							bs.setCreatedDate(new Date());
							bs.setRetired(false);
							bs.setCreator(Context.getAuthenticatedUser());
							bs.setFacilityServicePrice(fsp);
							
							/*if(insurance.getCategory().toLowerCase().equals("base")) {
								bs.setMaximaToPay(fsp.getFullPrice());
							}*/
							if(insurance.getCategory().toLowerCase().equals("mmi_ur")) {
								bs.setMaximaToPay((fsp.getFullPrice().multiply(new BigDecimal(1.15))).setScale(0, RoundingMode.CEILING));
							}
							else if(insurance.getCategory().toLowerCase().equals("rssb")) {
								bs.setMaximaToPay((fsp.getFullPrice().multiply(new BigDecimal(1.25))).setScale(0, RoundingMode.CEILING));
							}
							else if(insurance.getCategory().toLowerCase().equals("mutuelle")) {
								bs.setMaximaToPay((fsp.getFullPrice().divide(new BigDecimal(2))).setScale(0, RoundingMode.CEILING));
							} /*else if(insurance.getCategory().toLowerCase().equals("private")) {
								bs.setMaximaToPay(fsp.getFullPrice().add(fsp.getFullPrice().multiply(quarter)));
							}*/
							else if(insurance.getCategory().toLowerCase().equals("private")) {
								bs.setMaximaToPay((fsp.getFullPrice().multiply(new BigDecimal(1.4375))).setScale(0, RoundingMode.CEILING));
							}else if(insurance.getCategory().toLowerCase().equals("none")) {
								BigDecimal initial = fsp.getFullPrice().multiply(new BigDecimal(1.725));
								bs.setMaximaToPay(initial.setScale(0, RoundingMode.CEILING));
							}

						} else {
							bs = new BillableService();
							bs.setStartDate(startDate);
							bs.setInsurance(insurance);
							bs.setServiceCategory(getService().getServiceCategoryByName(fsp.getCategory(), insurance));
							bs.setCreatedDate(new Date());
							bs.setRetired(false);
							bs.setCreator(Context.getAuthenticatedUser());
							bs.setFacilityServicePrice(fsp);
							/*if(insurance.getCategory().toLowerCase().equals("base")) {
								bs.setMaximaToPay(fsp.getFullPrice());
							}*/
							if(insurance.getCategory().toLowerCase().equals("mmi_ur")) {
								bs.setMaximaToPay((fsp.getFullPrice().multiply(new BigDecimal(1.15))).setScale(0, RoundingMode.CEILING));
							}
							else if(insurance.getCategory().toLowerCase().equals("rssb")) {
								bs.setMaximaToPay((fsp.getFullPrice().multiply(new BigDecimal(1.25))).setScale(0, RoundingMode.CEILING));
							}
							else if(insurance.getCategory().toLowerCase().equals("mutuelle")) {
								bs.setMaximaToPay((fsp.getFullPrice().divide(new BigDecimal(2))).setScale(0, RoundingMode.CEILING));
							} else if(insurance.getCategory().toLowerCase().equals("private")) {
								bs.setMaximaToPay((fsp.getFullPrice().multiply(new BigDecimal(1.4375))).setScale(0, RoundingMode.CEILING));
							} else if(insurance.getCategory().toLowerCase().equals("none")) {
								BigDecimal initial = fsp.getFullPrice().multiply(new BigDecimal(1.725));
								bs.setMaximaToPay(initial.setScale(0, RoundingMode.CEILING));
							}
						}
					} 
					else {
						if(FacilityServicePriceUtil.isBillableCreated(fsp, insurance)) {
							System.out.println("Existing tarrif item");
							bs = getService().getBillableServiceByConcept(fsp, insurance);
							bs.setStartDate(startDate);
							bs.setInsurance(insurance);
							bs.setServiceCategory(getService().getServiceCategoryByName(fsp.getCategory(), insurance));
							bs.setCreatedDate(new Date());
							bs.setRetired(false);
							bs.setCreator(Context.getAuthenticatedUser());
							bs.setFacilityServicePrice(fsp);
							bs.setMaximaToPay(fsp.getFullPrice());
						} else {
							System.out.println("New Tarrif item");
							bs = new BillableService();
							bs.setStartDate(startDate);
							bs.setInsurance(insurance);
							bs.setServiceCategory(getService().getServiceCategoryByName(fsp.getCategory(), insurance));
							bs.setCreatedDate(new Date());
							bs.setRetired(false);
							bs.setCreator(Context.getAuthenticatedUser());
							bs.setFacilityServicePrice(fsp);
							bs.setMaximaToPay(fsp.getFullPrice());
						}
					}
					
					fsp.addBillableService(bs);
					getService().saveFacilityServicePrice(fsp);

					msg = "Updated Successfully";
				} catch(Exception e) {
					log.error(">>>MOH>>BILLING>>BULK UPDATE>> " + e.getMessage());
					e.printStackTrace();
			}
		}
		return msg;
	}

	public static void cascadeUpdateFacilityService(FacilityServicePrice fsp) {
		
		List<Insurance> insurances = getService().getAllInsurances();
		Set<BillableService> billableServices = fsp.getBillableServices();
		
		BigDecimal quarter = new BigDecimal(25).divide(new BigDecimal(100));
		BigDecimal fifth = new BigDecimal(20).divide(new BigDecimal(100));
		
		for(Insurance insurance : insurances) {
		 if(!insurance.isVoided())	
			 try{
			  
			for(BillableService bs:billableServices) {
				bs = getService().getBillableServiceByConcept(fsp, insurance);
				bs.setStartDate(new Date());
				bs.setInsurance(insurance);
				bs.setServiceCategory(getService().getServiceCategoryByName(fsp.getCategory(), insurance));
				bs.setCreatedDate(new Date());
				bs.setRetired(false);
				bs.setCreator(Context.getAuthenticatedUser());
				bs.setFacilityServicePrice(fsp);
				
				if(!fsp.getCategory().toLowerCase().equals("medicaments") && !fsp.getCategory().toLowerCase().equals("consommables")) {
				/*if(insurance.getCategory().toLowerCase().equals("base")) {
					bs.setMaximaToPay(fsp.getFullPrice());
				}*/
				if(insurance.getCategory().toLowerCase().equals("mmi_ur")) {
						bs.setMaximaToPay(fsp.getFullPrice().multiply(new BigDecimal(1.15)));
					}
				else if(insurance.getCategory().toLowerCase().equals("rssb")) {
						bs.setMaximaToPay(fsp.getFullPrice().multiply(new BigDecimal(1.25)));
					}
				else if(insurance.getCategory().toLowerCase().equals("mutuelle")) {
					bs.setMaximaToPay(fsp.getFullPrice().divide(new BigDecimal(2)));
				} else if (insurance.getCategory().toLowerCase().equals("private")) {
					bs.setMaximaToPay(fsp.getFullPrice().multiply(new BigDecimal(1.4375)));
				} else if(insurance.getCategory().toLowerCase().equals("none")) {
					BigDecimal initial = fsp.getFullPrice().multiply(new BigDecimal(1.725));
					bs.setMaximaToPay(initial);
				}
				fsp.addBillableService(bs);
			} 
				else
					bs.setMaximaToPay(fsp.getFullPrice());  
				  
		}
		  
		}
		 catch (Exception e) {
			// TODO: handle exception
		}
	
			getService().saveFacilityServicePrice(fsp);
			log.info("wouuuuuuuuuuuuuuuuuuuuuuu Updated Successfully");
		}

	}
}
