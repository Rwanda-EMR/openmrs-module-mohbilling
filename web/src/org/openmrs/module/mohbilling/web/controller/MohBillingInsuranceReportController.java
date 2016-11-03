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
			 Float insuranceRate=insurance.getCurrentRate().getRate();
			 
         	BigDecimal totalConsult = new BigDecimal(0);
         	BigDecimal totalLabo = new BigDecimal(0);
         	BigDecimal totalImaging = new BigDecimal(0);
         	BigDecimal totalHosp = new BigDecimal(0);
         	BigDecimal totalProcAndMater = new BigDecimal(0);
         	BigDecimal totalOtherCons = new BigDecimal(0);
         	BigDecimal totalMedic = new BigDecimal(0);
         	BigDecimal total100 = new BigDecimal(0);

			// get all consommation with globalbill closed
				List<GlobalBill> globalBills = ReportsUtil.getGlobalBills(startDate, endDate);
				List<Consommation> cons = null;
				if(globalBills.size()!=0)
				 cons = ReportsUtil.getConsommationByGlobalBills(globalBills);
				
				List<AllServicesRevenue> listOfAllServicesRevenue = new ArrayList<AllServicesRevenue>();
				
					AllServicesRevenue servicesRevenu = null;
						
					try {
		            	
						//ambulance?????		
						for (Consommation c : cons) {
							if(c.getBeneficiary().getInsurancePolicy().getInsurance().getInsuranceId()==insurance.getInsuranceId()){
							
							 ServiceRevenue consultRevenue =  ReportsUtil.getServiceRevenue(c, "mohbilling.CONSULTATION");
							 if(consultRevenue==null){
								 consultRevenue = new ServiceRevenue("mohbilling.CONSULTATION", new BigDecimal(0));
							 }
							 totalConsult=totalConsult.add(consultRevenue.getDueAmount());
							 
							 ServiceRevenue laboRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.LABORATOIRE");
							 if(laboRevenue==null){
								 laboRevenue = new ServiceRevenue("mohbilling.LABORATOIRE", new BigDecimal(0));
							 }
							 totalLabo=totalLabo.add(laboRevenue.getDueAmount());
							 
							 ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.IMAGING");
							 if(imagingRevenue==null){
								 imagingRevenue = new ServiceRevenue("mohbilling.IMAGING", new BigDecimal(0));
							 }
							 totalImaging=totalImaging.add(imagingRevenue.getDueAmount());
							 
							 ServiceRevenue hospRevenue= ReportsUtil.getServiceRevenue(c, "mohbilling.HOSPITALISATION");
							 if(hospRevenue==null){
								 hospRevenue = new ServiceRevenue("mohbilling.HOSPITALISATION", new BigDecimal(0));
							 }
							 totalHosp=totalHosp.add(hospRevenue.getDueAmount());
							 
							 ServiceRevenue proceduresAndMater = ReportsUtil.getServiceRevenue(c, "mohbilling.procAndMaterials");
							 if(proceduresAndMater==null){
								 proceduresAndMater = new ServiceRevenue("mohbilling.procAndMaterials", new BigDecimal(0));
							 }
							 totalProcAndMater=totalProcAndMater.add(proceduresAndMater.getDueAmount());
							 
							 ServiceRevenue otherConsummables = ReportsUtil.getServiceRevenue(c, "mohbilling.otherConsummables");
							 if(otherConsummables==null){
								 otherConsummables = new ServiceRevenue("mohbilling.otherConsummables", new BigDecimal(0));
							 }
							 totalOtherCons=totalOtherCons.add(otherConsummables.getDueAmount());
							 
							 ServiceRevenue medicRevenue = ReportsUtil.getServiceRevenue(c, "mohbilling.MEDICAMENTS");
							 if(medicRevenue==null){
								 medicRevenue = new ServiceRevenue("mohbilling.MEDICAMENTS", new BigDecimal(0));
							 }
							 totalMedic=totalMedic.add(medicRevenue.getDueAmount());
							 
							if(startDate!=null && endDate!=null){
								//allGlobalAmount = servicesRevenu.getAllDueAmounts();
								List<ServiceRevenue> revenueList = new ArrayList<ServiceRevenue>();
								 revenueList.add(consultRevenue);
								 revenueList.add(laboRevenue);
								 revenueList.add(imagingRevenue);
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
		    		mav.addObject("insuranceRate", insuranceRate);
		    		mav.addObject("totalConsult", totalConsult);
		    		mav.addObject("totalLabo", totalLabo);
		    		mav.addObject("totalImaging", totalImaging);
		    		mav.addObject("totalHosp", totalHosp);
		    		mav.addObject("totalProcAndMater", totalProcAndMater);
		    		mav.addObject("totalOtherCons", totalOtherCons);
		    		mav.addObject("totalMedic", totalMedic);
		    		total100=total100.add(totalConsult).add(totalLabo).add(totalImaging).add(totalHosp).add(totalProcAndMater).add(totalOtherCons).add(totalMedic);
		    		mav.addObject("total100", total100);
	}
		 mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		return mav;
	}

}
