package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingThirdPartyReportController extends
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

			System.out.print("fffffffffffffffff   a, reaching if in the controller  fffffffffffffffff");

			String endDateStr = request.getParameter("endDate");
			String endHourStr = request.getParameter("endHour");
			String endMinuteStr = request.getParameter("endMinute");
			
			String collectorStr = null;
			String insuranceStr = null;
			String thirdPartyStr = null;

			System.out.print("ffffffffffffffffffffffffffffffffffi endDatetestr"+endDateStr+" and end hourstr equals "+endHourStr+" and end end minutes equals "+endMinuteStr);
			
			 // parameters
			 Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);
			
			 Date startDate = (Date) params[0];
			 Date endDate = (Date) params[1];
			 
			 ThirdParty thirdParty = Context.getService(BillingService.class).getThirdParty(Integer.valueOf(request.getParameter("thirdPartyId")));
			// get all consommation with globalbill closed
			 
		
				List<AllServicesRevenue> listOfAllServicesRevenue = new ArrayList<AllServicesRevenue>();
				BigDecimal total100 = new BigDecimal(0);
				
				System.out.print("  thirdParty  object here  "+thirdParty.getName()+" the iddddddddddddddddddddddddddddddddddd of this "+thirdParty.getThirdPartyId());
				Insurance insurance = InsurancePolicyUtil.getInsurancePolicyByThirdParty(thirdParty).getInsurance();
			    System.out.print(" insurance insurance "+insurance);


				Float insuranceRate=insurance.getCurrentRate().getRate();
				
				List<String> columns = new ArrayList<String>();	
				Consommation initialConsom=null;
				List<BigDecimal> totals = new ArrayList<BigDecimal>();
		    		
		    		if(thirdParty!=null){			    		
			    		
//						try {
							List<GlobalBill> globalBills = ReportsUtil.getGlobalBills(startDate, endDate);
							
							List<GlobalBill> globalBillsByInsuranceAndThirdParty = new ArrayList<GlobalBill>();
							for (GlobalBill gb : globalBills) {
								if(gb.getAdmission().getInsurancePolicy().getThirdParty()!=null)
								if(gb.getAdmission().getInsurancePolicy().getThirdParty().getThirdPartyId()==thirdParty.getThirdPartyId())
									if(gb.isClosed())
										globalBillsByInsuranceAndThirdParty.add(gb);
							}
							if(startDate!=null && endDate!=null){
						System.out.print(" globalBillsByInsuranceAndThirdParty globalBillsByInsuranceAndThirdParty "+globalBillsByInsuranceAndThirdParty.size());
								for (GlobalBill gb : globalBillsByInsuranceAndThirdParty) {
									BigDecimal globalBillAmount = new BigDecimal(0);
									if(ReportsUtil.getConsommationByGlobalBill(gb)!=null)
									initialConsom = ReportsUtil.getConsommationByGlobalBill(gb);
									
									List<ServiceRevenue> insuranceColumnsRevenues=new ArrayList<ServiceRevenue>();
									if(gb.isClosed()){
									List<PatientServiceBill> gbItems = ReportsUtil.getAllItemsByGlobalBill(gb);

										 List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.thirdPartyReportColumns");
										 for (HopService hopService : reportColumns) {
											 if(!columns.contains(hopService.getName()))
											 columns.add(hopService.getName());
											 
											 insuranceColumnsRevenues.add(ReportsUtil.getServiceRevenues(gbItems, hopService));
											 
										}
										 
										 ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(gbItems, "mohbilling.IMAGING");
										 insuranceColumnsRevenues.add(imagingRevenue);
										 
										 
										 ServiceRevenue proceduresRevenue = ReportsUtil.getServiceRevenue(gbItems, "mohbilling.PROCEDURES");
										 insuranceColumnsRevenues.add(proceduresRevenue);
										 
										 globalBillAmount=globalBillAmount.add(ReportsUtil.getTotalByItems(gbItems));
										 
									 //populate asr
									 AllServicesRevenue servicesRevenu = new AllServicesRevenue(new BigDecimal(0), new BigDecimal(0), "2016-09-11");
									 servicesRevenu.setRevenues(insuranceColumnsRevenues);
									 servicesRevenu.setAllDueAmounts(globalBillAmount);
									 servicesRevenu.setConsommation(initialConsom);
									 listOfAllServicesRevenue.add(servicesRevenu); 
							}
									
						  }
						}		
								List<PatientServiceBill> allItems = ReportsUtil.getBillItemsByAllGlobalBills(globalBillsByInsuranceAndThirdParty);
								for (String category : columns) {
									totals.add(ReportsUtil.getTotalByCategorizedItems(allItems, category));
									total100=total100.add(ReportsUtil.getTotalByCategorizedItems(allItems, category));
								}
								totals.add(ReportsUtil.getTotalByCategorizedItems(allItems, GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.IMAGING")));
								totals.add(ReportsUtil.getTotalByCategorizedItems(allItems, GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.PROCEDURES")));
								total100=total100.add(ReportsUtil.getTotalByCategorizedItems(allItems, GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.IMAGING")));
								total100=total100.add(ReportsUtil.getTotalByCategorizedItems(allItems, GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.PROCEDURES")));
							
						/*} catch (Exception e) {
							request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
									"No patient bill found or service categories are not set properly. Contact System Admin... !");
							log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+e.getMessage());
						}*/
		    		}
		    		//requests sessions.
			request.getSession().setAttribute("columns" , columns);
			request.getSession().setAttribute("listOfAllServicesRevenue" , listOfAllServicesRevenue);
			request.getSession().setAttribute("insurance" , insurance);

					if(!columns.contains("IMAGING"))
						 columns.add("IMAGING");
					if(!columns.contains("PROCEDURES"))
						 columns.add("PROCED.");
			
					mav.addObject("columns", columns);
					mav.addObject("totals", totals);

   
		    		mav.addObject("listOfAllServicesRevenue", listOfAllServicesRevenue);
		    		mav.addObject("resultMsg", "["+thirdParty.getName()+"] Bill from "+startDateStr +" To "+ endDateStr);
		    		mav.addObject("total100", total100);
		    		
		    		
		    		
		    		mav.addObject("thirdPartyRate", thirdParty.getRate());
		    		mav.addObject("patientRate", (new BigDecimal(100).subtract(new BigDecimal(insuranceRate)).subtract(new BigDecimal(thirdParty.getRate()))));
		    		mav.addObject("insuranceRate", insuranceRate);
		    		log.info("hhghghghdhfdfgfgfgfgdfgdfgdfgdgggsggsfsfdsffafafafafdf the insurance rate is"+insuranceRate);
	}
		 mav.addObject("thirdParties", Context.getService(BillingService.class).getAllThirdParties());

		System.out.print("ffffffffffffffffffffffffffffffffff my last value of third parties is :  "+Context.getService(BillingService.class).getAllThirdParties());
		return mav;
	}
}
