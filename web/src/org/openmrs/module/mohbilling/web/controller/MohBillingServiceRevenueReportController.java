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
import org.openmrs.User;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.DepartmentRevenues;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

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

			
			 // marameters
			 Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);
			
			 Date startDate = (Date) params[0];
			 Date endDate = (Date) params[1];
			 User collector =  (User) params[2];

			
			 List<BillPayment> payments = BillPaymentUtil.getAllPaymentByDatesAndCollector(startDate, endDate,collector);
			 BigDecimal totalRevenueAmount = new BigDecimal(0);
			 for (BillPayment bp : payments) {
				 totalRevenueAmount=totalRevenueAmount.add(bp.getAmountPaid());
			}

			 try {
					 List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);
					 
					 List<Department> allDepartments = DepartementUtil.getAllHospitalDepartements();
					 List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.servicesReportColumn");
					 List<String> columns = new ArrayList<String>();
					 for (HopService hopService : reportColumns) {
						 columns.add(hopService.getName());
					}
					 
					 List<DepartmentRevenues> departRevenues =  new ArrayList<DepartmentRevenues>();
					 for (Department depart : allDepartments) {
						 if(ReportsUtil.getRevenuesByDepartment(paidItems, depart,columns)!=null){
						     departRevenues.add(ReportsUtil.getRevenuesByDepartment(paidItems, depart,columns));
						 }
					}

					 mav.addObject("departmentsRevenues", departRevenues);	 
					 mav.addObject("services", departRevenues.get(0).getPaidServiceRevenues());
					 mav.addObject("totalRevenueAmount",totalRevenueAmount);
					 mav.addObject("resultMsg", "Revenue Amount From "+startDateStr+" To "+ endDateStr);
					
			} catch (Exception e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"No payment found !");
				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+e.getMessage());
			}

	}	
			
		return mav;
	}

	
}
