package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.Insurance;
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

		String insuranceStr = null, 
				startDateStr = null, endDateStr = null, serviceId = null, 
				cashCollector = null, startHourStr = null, startMinute = null, 
				endHourStr = null, endMinuteStr = null;

		if (request.getParameter("formStatus") != null && !request.getParameter("formStatus").equals("")) {
			
			startHourStr = request.getParameter("startHour");
			startMinute = request.getParameter("startMinute"); 
			endHourStr = request.getParameter("endHour");
			endMinuteStr = request.getParameter("endMinute");

			String startTimeStr = startHourStr + ":" + startMinute + ":00";
			String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
			Date startDate = null, endDate = null;
			if(request.getParameter("startDate") != null && !request.getParameter("startDate").equals("")) {
				startDateStr = request.getParameter("startDate");
				startDate = sdf.parse(startDateStr.split("/")[2] + "-"
						+ startDateStr.split("/")[1] + "-"
						+ startDateStr.split("/")[0] + " " + startTimeStr);
			}
			
			if(request.getParameter("endDate") != null && !request.getParameter("endDate").equals("")) {
				endDateStr = request.getParameter("endDate");
				endDate = sdf.parse(endDateStr.split("/")[2] + "-"
						+ endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0]
						+ " " + endTimeStr);
			}
				
			if(request.getParameter("cashCollector") != null)
				cashCollector = request.getParameter("cashCollector");
			
			
			if(request.getParameter("insurance") != null)
				insuranceStr = request.getParameter("insurance");			

			Integer insuranceIdInt = null;
			Insurance insurance = null;

			User user = Context.getAuthenticatedUser();

			String cashierNames = (user.getPersonName().getFamilyName() != null ? user
					.getPersonName().getFamilyName() : "")
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

//			List<Date> dates = Context.getService(BillingService.class).getRevenueDatesBetweenDates(startDate, endDate);
			
			  if(startDate!=null && endDate!=null){
				   
				   BillingService bs= Context.getService(BillingService.class);
				  
				   
				   String[] serviceCategories = {"FORMAL","CONSULT","LABO","RADIO","ECHOG","OPHTAL","CHIRUR","MEDEC","CONSOM","KINES","STOMAT","MATERN","AMBULANCE","SOINS INF","MEDICA","HOSPIT"};
				         
				   String pattern = "yyyy-MM-dd";
				   SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				   List<Date>dateList =bs.getRevenueDatesBetweenDates(startDate, endDate);
				
				   LinkedHashMap<String, Map<String, Double>> basedDateReport =new LinkedHashMap<String, Map<String, Double>>();          
				          
				   for (Date receivedDate : dateList) {
				    Map<String, Double> mappedReport =bs.getRevenueByService(receivedDate, serviceCategories, cashCollector,insurance);
				    basedDateReport.put(simpleDateFormat.format(receivedDate), mappedReport);   
				   }
				   System.out.println("the size of daily report map size"+basedDateReport.size());
				        
				  // Map<String, Double> mappedReport =bs.getCashierReceiptReport(startDate, endDate, serviceCategories, null);
				   //mav.addObject("mappedReport", mappedReport);
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
	private void rebuildParameters(HttpServletRequest request,
			ModelAndView mav)throws Exception {		
		
		String param = (request.getParameter("startDate") != null) ? "&startDate="
				+ request.getParameter("startDate") : "";
		param += (request.getParameter("endDate") != null) ? "&endDate="
				+ request.getParameter("endDate") : "";
		param += (request.getParameter("cashCollector") != null) ? "&cashCollector="
				+ request.getParameter("cashCollector") : "";
				param += (request.getParameter("insurance") != null) ? "&insurance="
						+ request.getParameter("insurance") : "";
		mav.addObject("prmtrs", param);
	}

	
}
