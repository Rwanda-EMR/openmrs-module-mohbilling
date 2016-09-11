/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.ParametersConversion;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.BillingConstants;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.AllServicesRevenue;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.ServiceRevenue;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingReportFormController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();

		if (request.getParameter("formStatus") != null
				&& !request.getParameter("formStatus").equals("")) {
			String startHourStr = request.getParameter("startHour");
			String startMinute = request.getParameter("startMinute");
			String endHourStr = request.getParameter("endHour");
			String endMinuteStr = request.getParameter("endMinute");

			Date startDate = null;
			Date endDate = null;
			User collector = null;

			if (request.getParameter("startDate") != null
					&& !request.getParameter("startDate").equals("")) {
				String startDateStr = request.getParameter("startDate");

				startDate = ParametersConversion.getStartDate(startDateStr,
						startHourStr, startMinute);

			}

			if (request.getParameter("endDate") != null
					&& !request.getParameter("endDate").equals("")) {
				String endDateStr = request.getParameter("endDate");
				endDate = ParametersConversion.getEndDate(endDateStr,
						endHourStr, endMinuteStr);
			}

			if (request.getParameter("cashCollector") != null) {
				collector = Context.getUserService()
						.getUser(
								Integer.parseInt(request
										.getParameter("cashCollector")));
			}
			

			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			// get all consommation with globalbill closed
			List<GlobalBill> globalBills = ReportsUtil.getGlobalBills(startDate, endDate);
			
			List<Consommation> cons = ReportsUtil.getConsommationByGlobalBills(globalBills);
			
			List<AllServicesRevenue> listOfAllServicesRevenue = new ArrayList<AllServicesRevenue>();
				
				AllServicesRevenue servicesRevenu = null;
				BigDecimal allGlobalAmount = null;
				
				// revenueList
				for (Consommation c : cons) {
					//allGlobalAmount = servicesRevenu.getAllDueAmounts();
					List<ServiceRevenue> revenueList = new ArrayList<ServiceRevenue>();
					 
					 
					 ServiceRevenue consultRevenue =  ReportsUtil.getServiceRevenue(c, "mohbilling.CONSULTATION");
					 if(consultRevenue==null){
						 consultRevenue = new ServiceRevenue("mohbilling.CONSULTATION", new BigDecimal(0));
					 }
					 ServiceRevenue laboRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.LABORATOIRE");
					 if(laboRevenue==null){
						 laboRevenue = new ServiceRevenue("mohbilling.LABORATOIRE", new BigDecimal(0));
					 }
					 ServiceRevenue hospRevenue= ReportsUtil.getServiceRevenue(c, "mohbilling.HOSPITALISATION");
					 if(hospRevenue==null){
						 hospRevenue = new ServiceRevenue("mohbilling.HOSPITALISATION", new BigDecimal(0));
					 }
					
						 
					 ServiceRevenue proceduresAndMater = ReportsUtil.getServiceRevenue(c, "mohbilling.procAndMaterials");
					 if(proceduresAndMater==null){
						 proceduresAndMater = new ServiceRevenue("mohbilling.procAndMaterials", new BigDecimal(0));
					 }
					 
					 ServiceRevenue otherConsummables = ReportsUtil.getServiceRevenue(c, "mohbilling.otherConsummables");
					 if(otherConsummables==null){
						 otherConsummables = new ServiceRevenue("mohbilling.otherConsummables", new BigDecimal(0));
					 }
					 
					 ServiceRevenue medicRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.MEDICAMENTS");
					 if(medicRevenue==null){
						 medicRevenue = new ServiceRevenue("mohbilling.MEDICAMENTS", new BigDecimal(0));
					 }
					 
					 revenueList.add(consultRevenue);
					 revenueList.add(laboRevenue);
					 revenueList.add(hospRevenue);
					 revenueList.add(proceduresAndMater);
					 revenueList.add(otherConsummables);
					 revenueList.add(medicRevenue);

					 //populate asr
					 servicesRevenu = new AllServicesRevenue(new BigDecimal(20000), new BigDecimal(21000), "2016-09-11");
					 servicesRevenu.setRevenues(revenueList);
					 servicesRevenu.setAllDueAmounts(c.getPatientBill().getAmount());
					 servicesRevenu.setConsommation(c);
					 listOfAllServicesRevenue.add(servicesRevenu); 
				}				
				// servicesRevenu.setRevenues(revenueList); 
			
			mav.addObject("listOfAllServicesRevenue", listOfAllServicesRevenue);
			
			/*for (AllServicesRevenue asr : listOfAllServicesRevenue) {
				log.info("VCCCCCCCCCCCCCCCCCCCCCCCCVVVVVVVVVV "+asr.getRevenues());
			}*/
		}

		mav.setViewName(getViewName());

		return mav;
	}

}
