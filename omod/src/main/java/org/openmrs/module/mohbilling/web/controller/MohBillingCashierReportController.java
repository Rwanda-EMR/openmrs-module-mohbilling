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
	
	protected final Log log = LogFactory.getLog(getClass());

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		
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

			 User collector =  (User) params[2];
			 cashier=collector;

					 List<BillPayment> payments = BillPaymentUtil.getAllPaymentByDatesAndCollector(startDate, endDate, collector);
					 
					 List<BillPayment> cashPayments = new ArrayList<BillPayment>();
					 List<BillPayment> depositPayments= new ArrayList<BillPayment>();
					 List<PaidServiceBill> paidItems = null;
					 if(request.getParameter("paymentType").equals("cashPayment")){
					 for (BillPayment bp : payments) {
						if(bp instanceof CashPayment)
							cashPayments.add(bp);
					 }
					 paidItems = BillPaymentUtil.getPaidItemsByBillPayments(cashPayments);
						mav.addObject("reportMsg", "Cash Payments From "+startDateStr+" To "+endDateStr);
					 }
					 else if(request.getParameter("paymentType").equals("depositPayment")){
						 for (BillPayment bp : payments) {
								if(bp instanceof DepositPayment)
									depositPayments.add(bp);
							 }
					  paidItems = BillPaymentUtil.getPaidItemsByBillPayments(depositPayments);
					  mav.addObject("reportMsg", "Deposit Payments From "+startDateStr+" To "+endDateStr);
					 }
					 else{
						 paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);
						 mav.addObject("reportMsg", "Total Received Amount From "+startDateStr+" To "+endDateStr);
					 }
				 mav.addObject("reportMsg1", "Total Received Amount From "+startDateStr+" To "+endDateStr);
			List<HopService> reportColumns=null;
			if (request.getParameter("reportType")!=null && request.getParameter("reportType")!=""){
				if(request.getParameter("reportType").equals("NO_DCP_Report")){
					reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.cashierReportColumns");
				}else if(request.getParameter("reportType").equals("DCP_Report")){
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
					 Consommation c = ConsommationUtil.getConsommationByPatientBill(payments.get(0).getPatientBill());
			      		if(c!=null && c.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")){
			      			paidItems = BillPaymentUtil.getOldPaidItems(payments);
			      		}
					 
					 List<String> columns = new ArrayList<String>();
					 for (HopService hopService : reportColumns) {
						 columns.add(hopService.getName());
					}
					 
					 List<PaymentRevenue> paymentRevenues =  new ArrayList<PaymentRevenue>();
					 for (BillPayment pay : payments) {
						 PaymentRevenue br =null;
						 if(request.getParameter("reportType")!=null && request.getParameter("reportType").equals("NO_DCP_Report")) {
							 br = ReportsUtil.getRevenuesByPayment(pay, columns);
						 }
						 else if(request.getParameter("reportType")!=null && request.getParameter("reportType").equals("DCP_Report")){
							 br = ReportsUtil.getRevenuesByPaymentDCP(pay, columns);
							 }
						 else {
						 	br = ReportsUtil.getRevenuesByPaymentAll(pay,columns);
						 }
						 if(br!=null){
							 paymentRevenues.add(br);
						 }
					}
					 List<PaidServiceRevenue> services=null;
					 if (paymentRevenues.size()<=0){
					 	request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"No data in the selected interval of time");
					 }
					 else{
					 	services = paymentRevenues.get(0).getPaidServiceRevenues();
					 }
					 List<BigDecimal> subTotals = new ArrayList<BigDecimal>();
					 BigDecimal bigTotal = new BigDecimal(0);
					 for (String s : columns) {
							 subTotals.add(ReportsUtil.getTotalByCategorizedPaidItems(paidItems, s));
							 bigTotal=bigTotal.add(ReportsUtil.getTotalByCategorizedPaidItems(paidItems, s));
					}
						
					 mav.addObject("paymentRevenues", paymentRevenues);	 

					 mav.addObject("services", services);
					 mav.addObject("totalRevenueAmount", BillPaymentUtil.getTotalPaid(payments));
					 mav.addObject("resultMsg", "Revenue Amount From "+startDateStr+" To "+ endDateStr);
					 mav.addObject("subTotals", subTotals);
					 mav.addObject("bigTotal", bigTotal);
					 mav.addObject("collector", cashier);

					 request.getSession().setAttribute("paymentRevenues" , paymentRevenues);
					 request.getSession().setAttribute("services" , services);
					 request.getSession().setAttribute("subTotals" , subTotals); 
					 request.getSession().setAttribute("bigTotal" , bigTotal); 
					 request.getSession().setAttribute("totalRevenueAmount" , BillPaymentUtil.getTotalPaid(payments));
			         request.getSession().setAttribute("collector" , cashier);
	}
		mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		mav.addObject("thirdParties", Context.getService(BillingService.class).getAllThirdParties());
		
		if(request.getParameter("print")!=null){
			 List<PaymentRevenue> paymentRevenues  = (List<PaymentRevenue>) request.getSession().getAttribute("paymentRevenues" );
			 List<BigDecimal> subTotals = (List<BigDecimal>) request.getSession().getAttribute("subTotals");
			 BigDecimal bigTotal = (BigDecimal) request.getSession().getAttribute("bigTotal");
			 BigDecimal totalPaid = (BigDecimal) request.getSession().getAttribute("totalRevenueAmount");
				
				 BigDecimal amount = (BigDecimal)request.getSession().getAttribute("totalReceivedAmount");
				 FileExporter fexp = new FileExporter();
				 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			User cashierUser=(User) request.getSession().getAttribute("collector");
					String fileName = "cashierReport-"+df.format(new Date())+".pdf";
				    fexp.printCashierReport(request, response, amount,paymentRevenues,subTotals,bigTotal,totalPaid,fileName,cashierUser);
			
		}
		return mav;
}
}