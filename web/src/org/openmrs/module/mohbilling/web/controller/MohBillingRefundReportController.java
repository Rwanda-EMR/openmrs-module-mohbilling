package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.User;
import org.openmrs.api.context.Context;

import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingRefundReportController extends 	ParameterizableViewController {

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String insuranceStr = null, startDateStr = null, endDateStr = null, serviceId = null, cashCollector = null, startHourStr = null, startMinute = null, endHourStr = null, endMinuteStr = null;
	
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

				if (request.getParameter("cashCollector") != null
						&& !request.getParameter("cashCollector").equals("")) {
					cashCollector = request.getParameter("cashCollector");
					collector = Context.getUserService().getUser(Integer.parseInt(cashCollector));
				}
				
				if (startDate != null && endDate != null) {
			     
					Set<PatientBill> refundedBills = PatientBillUtil.getRefundedBill(startDate, endDate, collector);
					
					
					mav.addObject("collector", collector);
					mav.addObject("refundedBills", refundedBills);	
		

				}

				

		

	
		mav.setViewName(getViewName());

		return mav;
	}
}
