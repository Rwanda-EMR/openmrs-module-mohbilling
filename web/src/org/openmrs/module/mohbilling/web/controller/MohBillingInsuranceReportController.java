package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.AllServicesRevenue;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.ServiceRevenue;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingInsuranceReportController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());


		if (request.getParameter("formStatus") != null
				&& !request.getParameter("formStatus").equals("")) {
			String startDateStr = request.getParameter("startDate");
			String startHourStr = request.getParameter("startHour");
			String startMinStr = request.getParameter("startMinute");
			
			String endDateStr = request.getParameter("endDate");
			String endHourStr = request.getParameter("endHour");
			String endMinuteStr = request.getParameter("endMinute");
			
			String collectorStr = null;
			String insuranceStr = null;
			String thirdPartyStr = null;
			
			
			 // marameters
			 Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);
			
			 Date startDate = (Date) params[0];
			 Date endDate = (Date) params[1];
			 
			 Insurance insurance = InsuranceUtil.getInsurance(Integer.valueOf(request.getParameter("insuranceId")));
			 
			// get all consommation with globalbill closed
				List<GlobalBill> globalBills = ReportsUtil.getGlobalBills(startDate, endDate);
				List<Consommation> cons = null;
				if(globalBills.size()!=0)
				 cons = ReportsUtil.getConsommationByGlobalBills(globalBills);
				
				List<AllServicesRevenue> listOfAllServicesRevenue = new ArrayList<AllServicesRevenue>();
				
					AllServicesRevenue servicesRevenu = null;
					BigDecimal globalBillAmount = new BigDecimal(0);
						
					try {
						// revenueList
			            if(cons!=null)
						for (Consommation c : cons) {
							globalBillAmount=globalBillAmount.add(c.getInsuranceBill().getAmount());
							
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
							 
							 ServiceRevenue echoRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.ECHOGRAPHY");
							 if(echoRevenue==null){
								 echoRevenue = new ServiceRevenue("mohbilling.ECHOGRAPHY", new BigDecimal(0));
							 }
							 
							 ServiceRevenue radioRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.RADIOLOGY");
							 if(radioRevenue==null){
								 radioRevenue = new ServiceRevenue("mohbilling.RADIOLOGY", new BigDecimal(0));
							 }
							 
							 ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.ACTS");
							 if(actsRevenue==null){
								 actsRevenue = new ServiceRevenue("mohbilling.ACTS", new BigDecimal(0));
							 }
							 ServiceRevenue nursingCareRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.NURSINGCARE");
							 if(nursingCareRevenue==null){
								 nursingCareRevenue = new ServiceRevenue("mohbilling.NURSINGCARE", new BigDecimal(0));
							 }
							 
							 ServiceRevenue pharmacyRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.PHARMACY");
							 if(pharmacyRevenue==null){
								 pharmacyRevenue = new ServiceRevenue("mohbilling.PHARMACY", new BigDecimal(0));
							 }
							if(startDate!=null && endDate!=null){
							if(c.getBeneficiary().getInsurancePolicy().getInsurance().getInsuranceId()==insurance.getInsuranceId()){
								//allGlobalAmount = servicesRevenu.getAllDueAmounts();
								List<ServiceRevenue> revenueList = new ArrayList<ServiceRevenue>();
								 
								 revenueList.add(consultRevenue);
								 revenueList.add(laboRevenue);
								 revenueList.add(radioRevenue);
								 revenueList.add(hospRevenue);
								 revenueList.add(proceduresAndMater);
								 revenueList.add(otherConsummables);
								 revenueList.add(medicRevenue);

								 //populate asr
								 servicesRevenu = new AllServicesRevenue(new BigDecimal(20000), new BigDecimal(21000), "2016-09-11");
								 servicesRevenu.setRevenues(revenueList);
								 servicesRevenu.setAllDueAmounts(c.getInsuranceBill().getAmount());
								 servicesRevenu.setConsommation(c);
								 listOfAllServicesRevenue.add(servicesRevenu); 
							}
							}
							}
					} catch (Exception e) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"No patient bill found or service categories are not set properly. Contact System Admin... !");
						log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+e.getMessage());
					}
   
		    		mav.addObject("listOfAllServicesRevenue", listOfAllServicesRevenue);
		    		mav.addObject("resultMsg", "["+insurance.getName()+"] Bill from "+startDateStr +" To "+ endDateStr);
		    		mav.addObject("globalBillAmount", globalBillAmount);
	}
		 mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		return mav;
	}

}
