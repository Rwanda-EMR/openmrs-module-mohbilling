/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.ParametersConversion;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

			if (request.getParameter("cashCollector") != null && !request.getParameter("cashCollector").equals("")) {
				collector = Context.getUserService()
						.getUser(Integer.parseInt(request.getParameter("cashCollector")));
			}


			// get all consommation with globalbill closed
			List<GlobalBill> globalBills = ReportsUtil.getGlobalBills(startDate, endDate);
			List<Consommation> cons = null;
			if(globalBills.size()!=0)
			 cons = ReportsUtil.getConsommationByGlobalBills(globalBills);
			
			List<AllServicesRevenue> listOfAllServicesRevenue = new ArrayList<AllServicesRevenue>();
			
			List<AllServicesRevenue> listOfAllPrivServicesRevenue = new ArrayList<AllServicesRevenue>();
				
				AllServicesRevenue servicesRevenu = null;
				BigDecimal allGlobalAmount = null;
				
				AllServicesRevenue privAllServicesRevenu = null;
	            List<PaidServiceRevenue> paidServiceRevenues = new ArrayList<PaidServiceRevenue>();
					
				// revenueList
	            if(cons!=null)
				for (Consommation c : cons) {
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
					if(c.getBeneficiary().getInsurancePolicy().getInsurance().getInsuranceId()==1){
						//allGlobalAmount = servicesRevenu.getAllDueAmounts();
						List<ServiceRevenue> revenueList = new ArrayList<ServiceRevenue>();
						 
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
					else if(c.getBeneficiary().getInsurancePolicy().getInsurance().getCategory().equals("PRIVATE")){
						List<ServiceRevenue> privRevenueList = new ArrayList<ServiceRevenue>();
						
						 privRevenueList.add(hospRevenue);
						 privRevenueList.add(consultRevenue);
						 privRevenueList.add(laboRevenue);
						 privRevenueList.add(echoRevenue);
						 privRevenueList.add(radioRevenue);
						 privRevenueList.add(actsRevenue);
						 privRevenueList.add(nursingCareRevenue);
						 privRevenueList.add(otherConsummables);
						 privRevenueList.add(pharmacyRevenue);
						 
						 
						 privAllServicesRevenu = new AllServicesRevenue(new BigDecimal(20000), new BigDecimal(21000), "2016-09-11");
						 privAllServicesRevenu.setRevenues(privRevenueList);
						 privAllServicesRevenu.setAllDueAmounts(c.getPatientBill().getAmount());
						 privAllServicesRevenu.setConsommation(c);
						 listOfAllPrivServicesRevenue.add(privAllServicesRevenu); 
					}
					}
					} 
				
			// cashier report
			if(request.getParameter("reportType")!=null && !request.getParameter("reportType").equals("")){
					List<BillPayment> payments = BillPaymentUtil.getAllPaymentByDatesAndCollector(startDate, endDate,
							collector);
					 List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);
					 if(request.getParameter("reportType").equals("cashierReport")){
							BigDecimal totalReceivedAmount = new BigDecimal(0);
							List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.cashierReportColumns");
							for (HopService hs : reportColumns) {
								if(ReportsUtil.getPaidServiceRevenue(paidItems, hs.getName())!=null)
								paidServiceRevenues.add(ReportsUtil.getPaidServiceRevenue(paidItems, hs.getName()));
							}

							mav.addObject("totalReceivedAmount", totalReceivedAmount);
							mav.addObject("paidServiceRevenues", paidServiceRevenues);
					}
			}

			//revenues by department
			if(request.getParameter("reportType")!=null && !request.getParameter("reportType").equals("")){
				if(request.getParameter("reportType").equals("serviceRevenue")){
						
						List<BillPayment> payments = BillPaymentUtil.getAllPaymentByDatesAndCollector(startDate, endDate,
								collector);
						 List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);
						 
						 List<Department> allDepartments = DepartementUtil.getAllHospitalDepartements();
						 List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.servicesReportColumn");
						 List<String> columns = new ArrayList<String>();
						 for (HopService hopService : reportColumns) {
							 columns.add(hopService.getName());
						}
						 
						 List<DepartmentRevenues> departRevenues =  new ArrayList<DepartmentRevenues>();
						 for (Department depart : allDepartments) {
							 if(ReportsUtil.getRevenuesByDepartment(paidItems, depart, columns)!=null)
							     departRevenues.add(ReportsUtil.getRevenuesByDepartment(paidItems, depart, columns));
						 }
						 mav.addObject("departmentsRevenues", departRevenues);	 
						 mav.addObject("services", departRevenues.get(0).getPaidServiceRevenues());
			   }
					
			}
					
		}
		mav.setViewName(getViewName());

		return mav;
	}

}