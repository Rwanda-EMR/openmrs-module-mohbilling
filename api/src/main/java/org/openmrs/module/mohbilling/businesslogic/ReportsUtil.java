/**
 * 
 */
package org.openmrs.module.mohbilling.businesslogic;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.ParametersConversion;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * @author EMR-RBC
 * 
 */
public class ReportsUtil {

	protected final Log log = LogFactory.getLog(getClass());
	
	public static BillingService getService(){
		
		return Context.getService(BillingService.class);
	}

	/**
	 * The patient Bill (Listing all the received services and their detailed
	 * information: Insurance rate, Patient rate, total amount, paid amount,
	 * rest,...) >> going to the Patient
	 * 
	 * @param beneficiary
	 *            the beneficiary to be checked
	 * 
	 * @return bill the bill matching the beneficiary, null otherwise
	 */
	public static PatientBill getPatientBillByBeneficiary(
			Beneficiary beneficiary, Date date) {

		

		return null;
	}


	public static List<BillableService> getPaidServices(Date startDate,
			Date endDate, Boolean isPaid) {

		List<BillableService> services = new ArrayList<BillableService>();
/*
		if (getService().getAllPatientBills() != null)
			for (PatientBill pb : getService().getAllPatientBills())
				if (!pb.isVoided())
					if (pb != null && pb.getIsPaid() == isPaid) {

						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								services.add(psb.getService());
					} else {
						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								services.add(psb.getService());
					}*/

		return services;
	}

	/**
	 * The monthly report on all bills (specifying the patients details: the
	 * amount they paid and their respective insurances) >> going to the Chief
	 * Accountant
	 * 
	 * @param startDate
	 *            the start date of the period
	 * @param endDate
	 *            the end date of the period
	 * @param isPaid
	 *            the condition of payment
	 * @return bills the list of matched PatientBill
	 */
	public static List<PatientBill> getMonthlyGeneralBills(Date startDate,
			Date endDate, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		/*for (PatientBill pb : getService().getAllPatientBills())
			if (isPaid != null) {
				if (!pb.isVoided() && pb.getIsPaid() == isPaid)
					for (PatientServiceBill psb : pb.getBillItems())
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0
								&& psb.getServiceDate().compareTo(endDate) <= 0)
							bills.add(pb);
			} else {
				if (!pb.isVoided())
					for (PatientServiceBill psb : pb.getBillItems())
						if (!psb.isVoided()
								&& psb.getServiceDate().compareTo(startDate) >= 0
								&& psb.getServiceDate().compareTo(endDate) <= 0)
							bills.add(pb);
			}*/

		return bills;
	}

	/**
	 * The monthly report on all patients of same insurance and the amount
	 * un/paid >> going to the Data manager/ Accountant
	 * 
	 * @param insurance
	 *            the insurance of the patients
	 * @param startDate
	 *            the start date of the period
	 * @param endDate
	 *            the end date of the period
	 * @param isPaid
	 *            the condition of payment
	 * @return bills the list of matched PatientBill
	 */
	public static List<PatientBill> getMonthlyBillsByInsurance(
			Insurance insurance, Date startDate, Date endDate, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills())
			;

		return bills;
	}

	/**
	 * 
	 * gets all bill services' names and keep each once >>>>>>>Map<String,
	 * String>
	 * 
	 * @param patientsBills
	 * @return item names
	 */
	public static Map<String, String> getAllBillItems(
			List<PatientBill> patientsBills) {

		Set<PatientServiceBill> patientServiceBill = new HashSet<PatientServiceBill>();
		for (PatientBill bill : patientsBills) {

			//patientServiceBill.addAll(bill.getBillItems());

		}

		Map<String, String> names = new HashMap<String, String>();

		for (PatientServiceBill psb : patientServiceBill) {
			if (psb.getBillableServiceCategory().getName() != null
					&& !psb.getBillableServiceCategory().getName().equals("")) {

				names.put(psb.getBillableServiceCategory().getName(), psb
						.getBillableServiceCategory().getName());
			}
		}

		return names;
	}

	/**
	 * The daily report on all amount paid (including the collectors and the
	 * patients as well as the respective amount) >> going to the Chief Cashier
	 * 
	 * @param day
	 *            the Day to report
	 * @return payments all daily the payments
	 */
	public static List<BillPayment> getDailyPayments(Date day) {

		List<BillPayment> payments = new ArrayList<BillPayment>();

		for (PatientBill pb : getService().getAllPatientBills())
			if (!pb.isVoided() && pb.getPayments() != null)
				for (BillPayment bp : pb.getPayments())
					if (!bp.isVoided()
							&& bp.getDateReceived().compareTo(day) == 0)
						payments.add(bp);

		return payments;
	}

	
	public static List<PatientBill> billCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCreator) {

		List<PatientBill> bills = getService().billCohortBuilder(insurance, startDate,
				endDate, patientId, serviceName, billStatus, billCreator);
		return bills;
	}
	
	public static List<BillPayment> paymentsCohortBuilder(Insurance insurance,
			Date startDate, Date endDate, Integer patientId,
			String serviceName, String billStatus, String billCollector) {

		List<BillPayment> bills = getService().paymentsCohortBuilder(insurance, startDate,
				endDate, patientId, serviceName, billStatus, billCollector);
		return bills;
	}

	/**
	 * The list of all patients that received billable services of same
	 * insurance for a certain period >> going to the Insurance company/
	 * District (local governance)
	 */
	// Wonder if it is not the same as above


	/**
	 * 
	 * gets all patients' bills by facility service
	 * 
	 * @param sc
	 * @param startDate
	 * @param endDate
	 * @param patient
	 * @param insurance
	 * @return bills the list of matched PatientBill
	 */

	
	static public double roundTwoDecimals(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
	

	
	static public void printPatientBillToPDF(HttpServletRequest request,
			HttpServletResponse response, List<PatientBill> reportedPatientBills)
			throws Exception {
	

	}

	static class HeaderFooter extends PdfPageEventHelper {
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getBoxSize("art");

			Phrase header = new Phrase(String.format("- %d -",
					writer.getPageNumber()));
			header.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			if (document.getPageNumber() > 1) {
				ColumnText.showTextAligned(writer.getDirectContent(),
						Element.ALIGN_CENTER, header,
						(rect.getLeft() + rect.getRight()) / 2,
						rect.getTop() + 40, 0);
			}

			Phrase footer = new Phrase(String.format("- %d -",
					writer.getPageNumber()));
			footer.setFont(new Font(FontFamily.COURIER, 4, Font.NORMAL));

			ColumnText.showTextAligned(writer.getDirectContent(),
					Element.ALIGN_CENTER, footer,
					(rect.getLeft() + rect.getRight()) / 2,
					rect.getBottom() - 40, 0);

		}
	}

	/**
	 * gets revenue of a specific category (eg: MEDICAMENTS, CONSULTATION,...)
	 * @param billItems
	 * @param hopService
	 * @return
	 */
       public static ServiceRevenue  getServiceRevenues(List<PatientServiceBill> billItems,HopService hopService){
		
		BigDecimal dueAmount = new BigDecimal(0);
		ServiceRevenue revenue=null;
		//due Amount  by Service
		List<PatientServiceBill> serviceItems = new ArrayList<PatientServiceBill>();
		float pRate = 0f;
		for (PatientServiceBill psb : billItems) {
			
			if(psb.getHopService()==hopService){
				Float insuranceRate = psb.getConsommation().getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
				pRate = (100f - insuranceRate) / 100f;
				BigDecimal patientRte = new BigDecimal(""+pRate);
				
				BigDecimal reqQty = psb.getQuantity();
				BigDecimal unitPrice =psb.getUnitPrice();
				dueAmount =dueAmount.add(reqQty.multiply(unitPrice).multiply(patientRte));	
				serviceItems.add(psb);
			}
			
		}
		// ||pRate==0 to fix mutuelle indigent which was not allowing the printout to display some items
		   if(dueAmount.compareTo(BigDecimal.ZERO)>0 || pRate==0.0){
		revenue = new ServiceRevenue(hopService.getName(), dueAmount);
	    revenue.setBillItems(serviceItems);
		}
		return revenue;
	}

	/**
	 * Get all paid amount after summing up all paid amount from each billpayment
	 * @param payments
	 * @return All paidAmount
	 */
	public static BigDecimal  getAllPaidAmount(List<BillPayment> payments) {
		BigDecimal allpaidAmount = new BigDecimal(0);
		for (BillPayment billPayment : payments) {
			allpaidAmount = allpaidAmount.add(billPayment.getAmountPaid());			
		}
		
		return allpaidAmount;
	}

	/**
	 * get revenue from all categories on the given consommable
	 * @param cons
	 * @param category
	 * @return AllServicesRevenue
	 */
	public static AllServicesRevenue getAllServicesRevenue(Consommation cons, String category){
	
		BigDecimal allDueAmounts = new BigDecimal(0);
		List<PatientServiceBill> billItems = new ArrayList<PatientServiceBill>();
		//due to casting error, the following is about changing the set to list
		for (PatientServiceBill item : cons.getBillItems()) {
			billItems.add(item);
		}
		
		List<HopService> services =GlobalPropertyConfig.getHospitalServiceByCategory(category);
		//get All services revenues
		List<ServiceRevenue> allRevenues = new ArrayList<ServiceRevenue>();
		
		 for (HopService hopService : services) {
			 //  System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV333 "+getService().getBillItemsByCategory(cons, hopService));
			 ServiceRevenue revenue = null;
			   if(getServiceRevenues(billItems, hopService)!=null){
				   revenue = ReportsUtil.getServiceRevenues(billItems, hopService);
				   allDueAmounts = allDueAmounts.add(revenue.getDueAmount());
				   allRevenues.add(revenue);
			   }	
			}
			AllServicesRevenue allServicesRevenue = new AllServicesRevenue(allDueAmounts, new BigDecimal(0), null);
			                   allServicesRevenue.setRevenues(allRevenues);
			                   allServicesRevenue.setConsommation(cons);
			
			return allServicesRevenue;      
	}
	/**
	 * gets revenue from grouped categories (eg: imagerie=Echographie+radiographie)
	 * @param consommation
	 * @param categ
	 * @return Service Revenue
	 */
	public static ServiceRevenue getServiceRevenue(Consommation consommation,String categ){
		
		BigDecimal amount = new BigDecimal(0);
		ServiceRevenue revenue=null;
		
		List<HopService> services =GlobalPropertyConfig.getHospitalServiceByCategory(categ);
		List<PatientServiceBill> billItems = getService().getBillItemsByGroupedCategories(consommation, services);
		//due Amount  by Service
		for (PatientServiceBill psb : billItems) {
			
			if(services.contains(psb.getHopService())){
				Float insuranceRate = psb.getConsommation().getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
				float pRate = (100f - insuranceRate) / 100f;
				BigDecimal patientRte = new BigDecimal(""+pRate);
				
				BigDecimal reqQty = psb.getQuantity();
				BigDecimal unitPrice =psb.getUnitPrice();
				/*dueAmount =dueAmount.add(reqQty.multiply(unitPrice).multiply(patientRte));	*/
				amount =amount.add(reqQty.multiply(unitPrice));	
			}
		}
		
		if(amount.compareTo(BigDecimal.ZERO)>0){	
			String[]	parts =  categ.split("\\.");
			categ = parts[1]; 
	        revenue = new ServiceRevenue(categ.toUpperCase(), amount);
	        revenue.setBillItems(billItems);
		}
		return revenue;
	}
	

	public static List<GlobalBill> getGlobalBills(Date date1, Date date2){
		return getService().getGlobalBills(date1,date2);
	}
	/**
	 * gets a list of consommations matching with a given global bill list
	 * @param gb
	 * @return list of Consommations
	 */
	public static List<Consommation> getConsommationByGlobalBills(List<GlobalBill> globalBills){
		return getService().getConsommationByGlobalBills(globalBills);
				
	}

	/**
	 * takes a list of paiditems and returns PaidServiceRevenue by a given category
	 * @param paidItems
	 * @param categ
	 * @return PaidServiceRevenue
	 */
	public static PaidServiceRevenue getPaidServiceRevenue(List<PaidServiceBill> paidItems, String categ){
		BigDecimal paidAmountOnThisCategory = new BigDecimal(0);
		for (PaidServiceBill pi : paidItems) {
			if(!pi.getVoided())
			if(categ.equals(pi.getBillItem().getHopService().getName())){
				Float insuranceRate = pi.getBillItem().getConsommation().getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
				float pRate = (100f - insuranceRate) / 100f;
				BigDecimal patientRte = new BigDecimal(""+pRate);
				
				BigDecimal paidQty = pi.getPaidQty();
				BigDecimal unitPrice =pi.getBillItem().getUnitPrice();
				paidAmountOnThisCategory =paidAmountOnThisCategory.add(paidQty.multiply(unitPrice).multiply(patientRte));	
			}
		}
		PaidServiceRevenue paidRevenue = null;

		if(paidAmountOnThisCategory.compareTo(BigDecimal.ZERO)>0){	
			paidRevenue = new PaidServiceRevenue();
			paidRevenue.setService(categ);
			paidRevenue.setPaidItems(paidItems);
			paidRevenue.setPaidAmount(paidAmountOnThisCategory);
		}
		else{
			paidRevenue = new PaidServiceRevenue();
			paidRevenue.setService(categ);
			paidRevenue.setPaidItems(null);
			paidRevenue.setPaidAmount(new BigDecimal(0));
		}
		return paidRevenue;
		
	}
	/**
	 * get revenues by categories and by each department
	 * @param paidItems
	 * @param d
	 * @param columns
	 * @return DepartmentRevenues
	 */
	public static DepartmentRevenues getRevenuesByDepartment(List<PaidServiceBill> paidItems,Department d,List<String> columns) {
		List<PaidServiceBill> itemsByDepart = new ArrayList<PaidServiceBill>();
	//	List<HopService> services =GlobalPropertyConfig.getHospitalServiceByCategory(columns);
		for (PaidServiceBill pi : paidItems) {
			if(pi.getBillItem().getConsommation().getDepartment().getName().equals(d.getName())){
		       itemsByDepart.add(pi);
			}
		}
		List<PaidServiceRevenue> paidSr = new ArrayList<PaidServiceRevenue>();
//		List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory(columns);
		BigDecimal totalByDepartment = new BigDecimal(0);
		for (String col : columns) {
			if(getPaidServiceRevenue(itemsByDepart, col)!=null){
			paidSr.add(getPaidServiceRevenue(itemsByDepart, col));
			totalByDepartment=totalByDepartment.add(getPaidServiceRevenue(itemsByDepart, col).getPaidAmount());
			}
	    }
		
		DepartmentRevenues departRevenues = null;
		if(totalByDepartment.compareTo(BigDecimal.ZERO)>0){	
		departRevenues = new DepartmentRevenues();
		departRevenues.setAmount(totalByDepartment);
		departRevenues.setDepartment(d);
		departRevenues.setPaidServiceRevenues(paidSr);
		}
		return departRevenues;
	}


	/**
	 * get all parameters 
	 * @param request
	 * @param startDateStr
	 * @param startHrStr
	 * @param startMinStr
	 * @param endDateStr
	 * @param endHrStr
	 * @param endMinStr
	 * @param collectorStr
	 * @param insuranceStr
	 * @param thirdPartyStr
	 * @return array of all given parameters
	 */
	public static Object[] getReportParameters(HttpServletRequest request,String startDateStr,String startHrStr,String startMinStr,
			String endDateStr,String endHrStr,String endMinStr,String collectorStr,String insuranceStr,String thirdPartyStr){
		Date startDate = null;
		Date endDate = null;
		User collector = null;
		Insurance insurance=null;
		ThirdParty thirdParty=null;
		
		Object[] params = new Object[5]; 
		
		if (startDateStr != null && !startDateStr.equals("")) {
			startDate = ParametersConversion.getStartDate(startDateStr,
					startHrStr, startMinStr);
			params[0]=startDate;
		}

		if (endDateStr != null && !endDateStr.equals("")) {
			endDate = ParametersConversion.getEndDate(endDateStr,
					endHrStr, endMinStr);
			params[1]=endDate;
		}

		if (collectorStr != null && !collectorStr.equals("")) {
			collector = Context.getUserService()
					.getUser(Integer.valueOf(collectorStr));
			params[2]=collector;
		}
		
		if(insuranceStr!=null && !insuranceStr.equals("")){
			insurance = InsuranceUtil.getInsurance(Integer.valueOf(insuranceStr));
			params[3]=insurance;
		}
		
		if(thirdPartyStr!=null && !thirdPartyStr.equals("")){
			thirdParty = Context.getService(BillingService.class).getThirdParty(Integer.valueOf(thirdPartyStr));
			params[4]=thirdParty;
		}
		
		return params;
	}

	public static List<PatientServiceBill> getAllItemsByGlobalBill(GlobalBill gb){
		List<PatientServiceBill> items = new ArrayList<PatientServiceBill>();
		for (Consommation c : ConsommationUtil.getConsommationsByGlobalBill(gb)) {
			for (PatientServiceBill item : c.getBillItems()) {
				if (!item.getVoided()) {
					items.add(item);
				}
			}
		}
		return items;
	}

	public static Consommation getConsommationByGlobalBill(GlobalBill gb){
		Consommation c=null;
		try {
			if(ConsommationUtil.getConsommationsByGlobalBill(gb)!=null)
			c= ConsommationUtil.getConsommationsByGlobalBill(gb).get(0);
		} catch (Exception e) {
			// TODO: handle exception
		}
				return c;
			}
	public static ServiceRevenue getServiceRevenue(List<PatientServiceBill> billItems,String groupedCategories){
		
		BigDecimal amount = new BigDecimal(0);
		ServiceRevenue revenue=null;

		List<HopService> services =GlobalPropertyConfig.getHospitalServiceByCategory(groupedCategories);
		List<PatientServiceBill> matchingItems = new ArrayList<PatientServiceBill>();
		//due Amount  by Service
		float pRate=0;
		for (HopService hs : services) {
			for (PatientServiceBill psb : billItems) {
				if(hs.equals(psb.getHopService())){
					Float insuranceRate = psb.getConsommation().getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
					pRate = (100f - insuranceRate) / 100f;
					BigDecimal patientRte = new BigDecimal(""+pRate);
					
					BigDecimal reqQty = psb.getQuantity();
					BigDecimal unitPrice =psb.getUnitPrice();
					amount =amount.add(reqQty.multiply(unitPrice).multiply(patientRte));	
					matchingItems.add(psb);
				}
			}
		}
		String category="";
		if(amount.compareTo(BigDecimal.ZERO)>0 || pRate==0.0){
			String[]	parts =  groupedCategories.split("\\.");
			category = parts[1]; 
	        revenue = new ServiceRevenue(category.toUpperCase(), amount);
	        revenue.setBillItems(matchingItems);
		}
		return revenue;
	}
	
	public static BigDecimal getTotalByItems(List<PatientServiceBill> billItems){
		BigDecimal total = new BigDecimal(0);
		for (PatientServiceBill psb : billItems) {
				BigDecimal reqQty = psb.getQuantity();
				BigDecimal unitPrice =psb.getUnitPrice();
				total =total.add(reqQty.multiply(unitPrice));	
		}
		return total;
	}
	/**
	 * gets total of items matching one category
	 * @param billItems
	 * @param category
	 * @return
	 */
	public static BigDecimal getTotalByCategorizedItems(List<PatientServiceBill> billItems,String category){
		BigDecimal totalByCategory = new BigDecimal(0);
		for (PatientServiceBill psb : billItems) {
			if(psb.getHopService().getName().equals(category)){
				BigDecimal reqQty = psb.getQuantity();
				BigDecimal unitPrice =psb.getUnitPrice();
				totalByCategory =totalByCategory.add(reqQty.multiply(unitPrice));	
			}
		}
		return totalByCategory;
	}
	/**
	 * gets total of items matching any of grouped category
	 * @param billItems
	 * @param category
	 * @return
	 */
	public static BigDecimal getTotalByCategorizedItems(List<PatientServiceBill> billItems,List<HopService> hopServices){
		BigDecimal totalByCategory = new BigDecimal(0);
		for (PatientServiceBill psb : billItems) {
			for (HopService hs : hopServices) {
				if(psb.getHopService().getName().equals(hs.getName())){
					BigDecimal reqQty = psb.getQuantity();
					BigDecimal unitPrice =psb.getUnitPrice();
					totalByCategory =totalByCategory.add(reqQty.multiply(unitPrice));	
				}
			}
		}
		return totalByCategory;
	}
	/**
	 * gets a list of items from multiple global bills
	 * @param globalBills
	 * @return
	 */
	public static List<PatientServiceBill> getBillItemsByAllGlobalBills(List<GlobalBill> globalBills){
		List<PatientServiceBill> allItems = new ArrayList<PatientServiceBill>();
		for (GlobalBill gb : globalBills) {	
			for (Consommation c : ConsommationUtil.getConsommationsByGlobalBill(gb)) {
				for (PatientServiceBill psb : c.getBillItems()) {
					if(!psb.isVoided())
					allItems.add(psb);
				}
			}
		}
		return allItems;
	}
}
