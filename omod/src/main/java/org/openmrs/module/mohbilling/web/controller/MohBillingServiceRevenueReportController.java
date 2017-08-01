package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingServiceRevenueReportController extends
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
			
			if(request.getParameter("cashCollector")!=null && !request.getParameter("cashCollector").equals(""))
				collectorStr= request.getParameter("cashCollector");

			
			 // parameters
			 Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);
			
			 Date startDate = (Date) params[0];
			 Date endDate = (Date) params[1];
			 User collector =  (User) params[2];

			
			 List<BillPayment> payments = BillPaymentUtil.getAllPaymentByDatesAndCollector(startDate, endDate, collector);
			 BigDecimal totalRevenueAmount = new BigDecimal(0);
			 
			 try {
					 List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);
					 
					 Consommation c = ConsommationUtil.getConsommationByPatientBill(payments.get(0).getPatientBill());
			      		if(c.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
			      			paidItems = BillPaymentUtil.getOldPaidItems(payments);
			      		}
					 
					 List<Department> allDepartments = DepartementUtil.getAllHospitalDepartements();
					 List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.servicesReportColumns");
					 List<String> columns = new ArrayList<String>();
					 for (HopService hopService : reportColumns) {
						 columns.add(hopService.getName());
					}
					 
					 List<DepartmentRevenues> departRevenues =  new ArrayList<DepartmentRevenues>();
					 for (Department depart : allDepartments) {
						 DepartmentRevenues dr= ReportsUtil.getRevenuesByDepartment(paidItems, depart, columns);
						 if(dr!=null){
						     departRevenues.add(dr);
						     //totalRevenueAmount=totalRevenueAmount.add(dr.getAmount());
						 }
					}
					 request.getSession().setAttribute("departRevenues" , departRevenues);
						
					 mav.addObject("departmentsRevenues", departRevenues);	 
					 mav.addObject("services", departRevenues.get(0).getPaidServiceRevenues());
					 mav.addObject("totalRevenueAmount", BillPaymentUtil.getTotalPaid(payments));
					 mav.addObject("resultMsg", "Revenue Amount From "+startDateStr+" To "+ endDateStr);
					 
					
			} catch (Exception e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"No payment found !");
				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+e.getMessage());
			}

	}	
		if(request.getParameter("print")!=null){
			List<DepartmentRevenues> departRevenues  = (List<DepartmentRevenues>) request.getSession().getAttribute("departRevenues" );
			 //String msg = (String) request.getSession().getAttribute("msg" );
			 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			 FileExporter fexp = new FileExporter();
				String fileName = "ServiceRep"+df.format(new Date())+".pdf";
			    fexp.printServicesRevenuesReport(request, response, departRevenues, fileName);
		}	
		return mav;
	}

	
}
