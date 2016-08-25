/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.ParametersConversion;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
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

			List<BillPayment> payments = BillPaymentUtil
					.getAllPaymentByDatesAndCollector(startDate, endDate,
							collector);

			String reportingPeriod = "" + startDate + "-" + endDate;
			if (payments.size()>0) {
				mav.addObject("allServicesRevenue", ReportsUtil.getAllServicesRevenue(payments, reportingPeriod));

			}

		}

		mav.setViewName(getViewName());

		return mav;
	}

}
