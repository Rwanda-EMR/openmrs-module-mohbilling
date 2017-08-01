package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.module.mohbilling.businesslogic.PaymentRefundUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

public class MohBillingRefundReportController extends ParameterizableViewController {

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
			 User collector = (User) params[2];
			 
             List<PaymentRefund> confirmedRefunds = PaymentRefundUtil.getRefundsBetweenDatesAndByCollector(startDate, endDate, collector);

      		mav.addObject("confirmedRefunds", confirmedRefunds);
			 
	}

		return mav;
	}
}
