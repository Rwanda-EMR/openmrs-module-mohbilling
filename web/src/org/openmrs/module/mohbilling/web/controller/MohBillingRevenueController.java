package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.advice.MohBillingUsageStatsUtils;
import org.openmrs.module.mohbilling.businesslogic.FacilityServicePriceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.Invoice;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingRevenueController extends ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", InsuranceUtil.getAllInsurances());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String insuranceStr = null, startDateStr = null, endDateStr = null, serviceId = null, cashCollector = null, startHourStr = null, startMinute = null, endHourStr = null, endMinuteStr = null;

		if (request.getParameter("formStatus") != null
				&& !request.getParameter("formStatus").equals("")) {

			startHourStr = request.getParameter("startHour");
			startMinute = request.getParameter("startMinute");
			endHourStr = request.getParameter("endHour");
			endMinuteStr = request.getParameter("endMinute");

			String startTimeStr = startHourStr + ":" + startMinute + ":00";
			String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
			Date startDate = null, endDate = null;
			if (request.getParameter("startDate") != null
					&& !request.getParameter("startDate").equals("")) {
				startDateStr = request.getParameter("startDate");
				startDate = sdf.parse(startDateStr.split("/")[2] + "-"
						+ startDateStr.split("/")[1] + "-"
						+ startDateStr.split("/")[0] + " " + startTimeStr);
			}

			if (request.getParameter("endDate") != null
					&& !request.getParameter("endDate").equals("")) {
				endDateStr = request.getParameter("endDate");
				endDate = sdf.parse(endDateStr.split("/")[2] + "-"
						+ endDateStr.split("/")[1] + "-"
						+ endDateStr.split("/")[0] + " " + endTimeStr);
			}

			User collector = null;			

			if (request.getParameter("cashCollector") != null && !request.getParameter("cashCollector").equals("")) {
				cashCollector = request.getParameter("cashCollector");
				collector = Context.getUserService().getUser(Integer.parseInt(cashCollector));
			}

			if (request.getParameter("insurance") != null)
				insuranceStr = request.getParameter("insurance");

			Integer insuranceIdInt = null;
			Insurance insurance = null;

			User user = Context.getAuthenticatedUser();

			String cashierNames = (user.getPersonName().getFamilyName() != null ? user
					.getPersonName().getFamilyName()
					: "")
					+ " "
					+ (user.getPersonName().getMiddleName() != null ? user
							.getPersonName().getMiddleName() : "")
					+ " "
					+ (user.getPersonName().getGivenName() != null ? user
							.getPersonName().getGivenName() : "");

			if (!request.getParameter("insurance").equals("")) {
				insuranceIdInt = Integer.parseInt(insuranceStr);
				insurance = InsuranceUtil.getInsurance(insuranceIdInt);
			}

			// List<Date> dates =
			// Context.getService(BillingService.class).getRevenueDatesBetweenDates(startDate,
			// endDate);

			if (startDate != null && endDate != null) {

				BillingService bs = Context.getService(BillingService.class);

				String[] serviceCategories = {"CONSULT","LABO","CHIRUR","CONSOMM","DERMAT", "ECHOG", "FORMAL", "HOSPIT", "KINE","MATER","MEDECI", "MEDICAM","OPHTAL", "ORL", "OXGYNOT","PEDIAT","RADIO","SOINS INF","SOINS INT","STOMAT","GYNECO","POLYC" };
				String pattern = "yyyy-MM-dd";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);				
				
		
				
				System.out.println(">>>>>>>>>is this end Date end Date >"+endDate);
				
				LinkedHashMap<String, Map<String, Double>> basedDateReport = new LinkedHashMap<String, Map<String, Double>>();
				
 
					//List<PatientBill> bills = bs.getPatientBillsByCollector(receivedDate, collector);
			
				List<Date> datesBetweenTwoDates =MohBillingUsageStatsUtils.getDaysBetweenDates(startDate, endDate);
			    	
				for (Date date : datesBetweenTwoDates) {					
					
					Date startOfDay =sdf.parse(MohBillingUsageStatsUtils.getStartOfDay(date)) ;
		        	Date endOfDay =sdf.parse(MohBillingUsageStatsUtils.getEndOfDay(date));	
		        	System.out.println("start of day:"+startOfDay+"and  end of day "+endOfDay);
		        	
		        	List<PatientBill>patientBills = bs.getBills(startOfDay, endOfDay,collector);
		             
		        	if (patientBills.size()>0) {
		        		
		        		Map<String, Double> mappedReport = getAllBillsByCollector(patientBills, serviceCategories);		        	
			        	
			        	basedDateReport.put(simpleDateFormat.format(date),	mappedReport);						
					}		
					
				} 

				//	Map<String, Double> mappedReport = getAllBillsByCollector(patientBills, serviceCategories);

					//Map<String, Double> mappedReport = bs.getRevenueByService(	receivedDate, serviceCategories, cashCollector,	insurance);

					//basedDateReport.put(simpleDateFormat.format(startDate),	mappedReport);
	
				// Map<String, Double> mappedReport
				// =bs.getCashierReceiptReport(startDate, endDate,
				// serviceCategories, null);
				// mav.addObject("mappedReport", mappedReport);
				mav.addObject("serviceCategories", serviceCategories);
				mav.addObject("basedDateReport", basedDateReport);
			}

		}
		mav.setViewName(getViewName());

		return mav;
	}

	/**
	 * @param request
	 * @param mav
	 */
	private void rebuildParameters(HttpServletRequest request, ModelAndView mav)
			throws Exception {

		String param = (request.getParameter("startDate") != null) ? "&startDate="
				+ request.getParameter("startDate")
				: "";
		param += (request.getParameter("endDate") != null) ? "&endDate="
				+ request.getParameter("endDate") : "";
		param += (request.getParameter("cashCollector") != null) ? "&cashCollector="
				+ request.getParameter("cashCollector")
				: "";
		param += (request.getParameter("insurance") != null) ? "&insurance="
				+ request.getParameter("insurance") : "";
		mav.addObject("prmtrs", param);
	}

	public static LinkedHashMap<String,Double> getAllBillsByCollector(List<PatientBill> bills,String[] serviceCategories){	
		
		
		LinkedHashMap< String, Double> invoiceMap = new LinkedHashMap<String,Double>();	
		Double total =0.0;	
		 for (String svceCateg : serviceCategories) {
			 Double subTotal =0.0;
			//starting all patient bills
				for (PatientBill pb : bills) {					
					Double currentRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();					 
					//scan each item  amount
					for (PatientServiceBill item : pb.getBillItems()) {						
						String  category = item.getService().getFacilityServicePrice().getCategory();						
						if (category.startsWith(svceCateg)) {							
						double patientCost=	item.getQuantity()*item.getUnitPrice().doubleValue()*(100-currentRate)/100;
						
						subTotal =subTotal+patientCost;							
						}			
					}
				}
				//end of all patient bills
				invoiceMap.put(svceCateg, ReportsUtil.roundTwoDecimals(subTotal));			
			 total=total+subTotal;
				
		}
		
		//add each invoice linked to catehory service to list of invoice		
		invoiceMap.put("Total", ReportsUtil.roundTwoDecimals(total));		
		return  invoiceMap;
	}
}
