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

public class MohBillingServiceRevenueReportController extends ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		try {
			if (request.getParameter("formStatus") != null
					&& !request.getParameter("formStatus").isEmpty()) {

				// Get date/time params
				String startDateStr = request.getParameter("startDate");
				String startHourStr = request.getParameter("startHour");
				String startMinStr = request.getParameter("startMinute");
				String endDateStr = request.getParameter("endDate");
				String endHourStr = request.getParameter("endHour");
				String endMinuteStr = request.getParameter("endMinute");

				// Other filters
				String collectorStr = request.getParameter("cashCollector");
				if (collectorStr != null && collectorStr.isEmpty()) {
					collectorStr = null;
				}

				// Parameters
				Object[] params = ReportsUtil.getReportParameters(
						request, startDateStr, startHourStr, startMinStr,
						endDateStr, endHourStr, endMinuteStr,
						collectorStr, null, null);

				Date startDate = (Date) params[0];
				Date endDate = (Date) params[1];
				User collector = (User) params[2];

				// Payments
				List<BillPayment> payments = BillPaymentUtil.getAllPaymentByDatesAndCollector(startDate, endDate, collector);
				if (payments == null || payments.isEmpty()) {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"No payments found for the given criteria.");
					return mav;
				}

				// Paid items
				List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);

				// Extra check for "old" bills
				Consommation c = ConsommationUtil.getConsommationByPatientBill(payments.get(0).getPatientBill());
				if (c != null && c.getGlobalBill() != null
						&& c.getGlobalBill().getBillIdentifier() != null
						&& c.getGlobalBill().getBillIdentifier().startsWith("bill")) {
					paidItems = BillPaymentUtil.getOldPaidItems(payments);
				}

				// Departments & services
				List<Department> allDepartments = DepartementUtil.getAllHospitalDepartements();
				List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.servicesReportColumns");
				if (reportColumns == null) {
					reportColumns = new ArrayList<HopService>();
				}

				List<String> columns = new ArrayList<String>();
				for (HopService hopService : reportColumns) {
					if (hopService != null && hopService.getName() != null) {
						columns.add(hopService.getName());
					}
				}

				// Calculate revenues
				List<DepartmentRevenues> departRevenues = new ArrayList<DepartmentRevenues>();
				for (Department depart : allDepartments) {
					DepartmentRevenues dr = ReportsUtil.getRevenuesByDepartment(paidItems, depart, columns);
					if (dr != null) {
						departRevenues.add(dr);
					}
				}

				// Save in session for re-use
				request.getSession().setAttribute("departRevenues", departRevenues);

				// Add to model
				mav.addObject("departmentsRevenues", departRevenues);
				if (!departRevenues.isEmpty()) {
					mav.addObject("services", departRevenues.get(0).getPaidServiceRevenues());
				} else {
					mav.addObject("services", new ArrayList<PaidServiceRevenue>());
				}
				mav.addObject("totalRevenueAmount", BillPaymentUtil.getTotalPaid(payments));
				mav.addObject("resultMsg", "Revenue Amount From " + startDateStr + " To " + endDateStr);
			}

			// Printing
			if (request.getParameter("print") != null) {
				List<DepartmentRevenues> departRevenues = (List<DepartmentRevenues>)
						request.getSession().getAttribute("departRevenues");
				if (departRevenues != null) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					FileExporter fexp = new FileExporter();
					String fileName = "ServiceRep" + df.format(new Date()) + ".pdf";
					fexp.printServicesRevenuesReport(request, response, departRevenues, fileName);
				} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"No data available to print.");
				}
			}

		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Error while generating report.");
			log.error("Error generating Service Revenue Report", e);
		}

		return mav;
	}
}
