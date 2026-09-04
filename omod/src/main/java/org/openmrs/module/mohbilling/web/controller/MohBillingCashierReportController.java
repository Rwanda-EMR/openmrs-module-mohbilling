package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingCashierReportController extends
		ParameterizableViewController {
	private static final String CASHIER_REPORT_PRIVILEGE = "Billing Report - View Cashier Report";
	
	protected final Log log = LogFactory.getLog(getClass());

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if (Boolean.parseBoolean(request.getParameter("export"))) {
			if (!Context.hasPrivilege(CASHIER_REPORT_PRIVILEGE)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}
			exportCashierReport(request, response);
			return null;
		}
		
		
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		
		String startDateStr = null, endDateStr = null, startHourStr = null, startMinuteStr = null, 
				endHourStr = null, endMinuteStr = null;
		User cashier=null;
		
		if (request.getParameter("formStatus") != null && !request.getParameter("formStatus").equals("")) {

			startHourStr = request.getParameter("startHour");
			startMinuteStr = request.getParameter("startMinute");
			
			endHourStr = request.getParameter("endHour");
			endMinuteStr = request.getParameter("endMinute");
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String startTimeStr = startHourStr + ":" + startMinuteStr + ":00";
			String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
			Date startDate = null, endDate = null;
			
			
			if(request.getParameter("startDate") != null && !request.getParameter("startDate").equals("")) {
				startDateStr = request.getParameter("startDate");
				startDate = sdf.parse(startDateStr.split("/")[2] + "-"
						+ startDateStr.split("/")[1] + "-"
						+ startDateStr.split("/")[0] + " " + startTimeStr);
			}
			
			if(request.getParameter("endDate") != null && !request.getParameter("endDate").equals("")) {
				endDateStr = request.getParameter("endDate");
				endDate = sdf.parse(endDateStr.split("/")[2] + "-"
						+ endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0]
						+ " " + endTimeStr);
			}
			
			
			String collectorStr = null;
			String insuranceStr = null;
			String thirdPartyStr = null;
			
			if(request.getParameter("cashCollector")!=null && !request.getParameter("cashCollector").equals(""))
				collectorStr= request.getParameter("cashCollector");

			if(request.getParameter("insuranceId")!=null && request.getParameter("insuranceId").equals(""))
				insuranceStr = request.getParameter("insuranceId");

			if(request.getParameter("thirdPartyId")!=null && !request.getParameter("thirdPartyId").equals(""))
			 thirdPartyStr = request.getParameter("thirdPartyId");
			
			 Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinuteStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);

			// Date startDate = (Date) params[0];
			// Date endDate = (Date) params[1];
			 User collector =  (User) params[2];
			 cashier=collector;
//			 try {
				  
						 String paymentType = request.getParameter("paymentType");
						 String reportMsg;
						 if ("cashPayment".equals(paymentType)) {
							 reportMsg = "Cash Payments From "+startDateStr+" To "+endDateStr;
						 } else if ("depositPayment".equals(paymentType)) {
							 reportMsg = "Deposit Payments From "+startDateStr+" To "+endDateStr;
						 } else {
							 reportMsg = "Total Received Amount From "+startDateStr+" To "+endDateStr;
						 }
						 mav.addObject("reportMsg", reportMsg);
					 mav.addObject("reportMsg1", "Total Received Amount From "+startDateStr+" To "+endDateStr);
					 
					 List<HopService> reportColumns=null;
					 String reportType = request.getParameter("reportType");
					 if (reportType != null && !reportType.isEmpty()){
						 if(reportType.equals("NO_DCP_Report")){
							 reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.cashierReportColumns");
						 }else if(reportType.equals("DCP_Report")){
							 reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.cashierReportColumnsDcp");
					 }
					 else {
						 reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.cashierReportColumnsAll");
					 }
				 }
				 else {
					 request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"Please!!!! Dear " +Context.getAuthenticatedUser()+ " Select Report Type");
					 return new ModelAndView(new RedirectView("cashierReport.form"));
				 }
			
						 BillingService billingService = Context.getService(BillingService.class);
						 Integer collectorId = collector != null ? collector.getUserId() : null;
						 List<PaymentRevenue> paymentRevenues = billingService.getCashierReportFromEtl(
								 startDate, endDate, collectorId, paymentType, reportType, reportColumns);
					 
					List<PaidServiceRevenue> services=null;
					if (paymentRevenues.size()<=0){
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"No data in the selected interval of time");
					}
					else{
						services = paymentRevenues.get(0).getPaidServiceRevenues();
					}
					 // Footer TOT(Due) must equal the sum of row TOTAL Due values.
					 // Previously bigTotal used getTotalByCategorizedPaidItems (full qty, no third-party,
					 // no void/DCP filters), which did not match per-row paidQty-based amounts.
					 List<BigDecimal> subTotals = new ArrayList<BigDecimal>();
					 BigDecimal bigTotal = new BigDecimal(0);
					 if (services != null && !services.isEmpty()) {
						 for (int colIdx = 0; colIdx < services.size(); colIdx++) {
							 BigDecimal columnTotal = new BigDecimal(0);
							 for (PaymentRevenue pr : paymentRevenues) {
								 List<PaidServiceRevenue> rowServices = pr.getPaidServiceRevenues();
								 if (rowServices != null && colIdx < rowServices.size()
										 && rowServices.get(colIdx).getPaidAmount() != null) {
									 columnTotal = columnTotal.add(rowServices.get(colIdx).getPaidAmount());
								 }
							 }
							 subTotals.add(columnTotal);
							 bigTotal = bigTotal.add(columnTotal);
						 }
					 }
						 BigDecimal totalPaid = billingService.getCashierReportTotalPaidFromEtl(
								 startDate, endDate, collectorId, paymentType);
						
					 mav.addObject("paymentRevenues", paymentRevenues);	 

					 mav.addObject("services", services);
					 mav.addObject("totalRevenueAmount", totalPaid);
					 String resultMsg = "Revenue Amount From "+startDateStr+" To "+ endDateStr;
					 mav.addObject("resultMsg", resultMsg);
					 mav.addObject("subTotals", subTotals);
					 mav.addObject("bigTotal", bigTotal);
					 mav.addObject("collector", cashier);

					 request.getSession().setAttribute("paymentRevenues" , paymentRevenues);
					 request.getSession().setAttribute("services" , services);
					 request.getSession().setAttribute("subTotals" , subTotals); 
					 request.getSession().setAttribute("bigTotal" , bigTotal); 
					 request.getSession().setAttribute("totalRevenueAmount" , totalPaid);
			         request.getSession().setAttribute("collector" , cashier);
					 request.getSession().setAttribute("cashierReportTitle", reportMsg);

		/*	} catch (Exception e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"No payment found !");
				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+e.getMessage());
			}
				
				
			} catch (Exception e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"No payment found !");
			}*/

	}

		mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		mav.addObject("thirdParties", Context.getService(BillingService.class).getAllThirdParties());
		
		if(request.getParameter("print")!=null){
		//	HttpSession session = request.getSession(true);
			 List<PaymentRevenue> paymentRevenues  = (List<PaymentRevenue>) request.getSession().getAttribute("paymentRevenues" );
			 List<BigDecimal> subTotals = (List<BigDecimal>) request.getSession().getAttribute("subTotals");
			 BigDecimal bigTotal = (BigDecimal) request.getSession().getAttribute("bigTotal");
			 BigDecimal totalPaid = (BigDecimal) request.getSession().getAttribute("totalRevenueAmount");
				
				 BigDecimal amount = (BigDecimal)request.getSession().getAttribute("totalReceivedAmount");
				 FileExporter fexp = new FileExporter();
				 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			User cashierUser=(User) request.getSession().getAttribute("collector");
					String fileName = "cashierReport-"+df.format(new Date())+".pdf";
			//System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC:"+cashierUser.getPersonName().getFullName());
				    fexp.printCashierReport(request, response, amount,paymentRevenues,subTotals,bigTotal,totalPaid,fileName,cashierUser);
			
		}
		return mav;
}

	@SuppressWarnings("unchecked")
	private void exportCashierReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<PaymentRevenue> paymentRevenues =
				(List<PaymentRevenue>) request.getSession().getAttribute("paymentRevenues");
		if (paymentRevenues == null || paymentRevenues.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing cashier report export data.");
			return;
		}
		FileExporter.exportCashierReportData(response, paymentRevenues,
				(List<BigDecimal>) request.getSession().getAttribute("subTotals"),
				(BigDecimal) request.getSession().getAttribute("bigTotal"),
				(BigDecimal) request.getSession().getAttribute("totalRevenueAmount"),
				(String) request.getSession().getAttribute("cashierReportTitle"),
				(User) request.getSession().getAttribute("collector"));
	}
}
