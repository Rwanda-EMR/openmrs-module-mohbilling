package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
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

			if(request.getParameter("insuranceId")!=null && request.getParameter("insuranceId").equals(""))
				insuranceStr = request.getParameter("insuranceId");

			if(request.getParameter("thirdPartyId")!=null && !request.getParameter("thirdPartyId").equals(""))
			 thirdPartyStr = request.getParameter("thirdPartyId");
			
			 Object[] params = ReportsUtil.getReportParameters(request, startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, collectorStr, insuranceStr, thirdPartyStr);
			
			 List<PaidServiceRevenue> paidServiceRevenues = new ArrayList<PaidServiceRevenue>();

			 Date startDate = (Date) params[0];
			 Date endDate = (Date) params[1];
			 User collector =  (User) params[2];

			 
			//BigDecimal totalReceivedAmount = new BigDecimal(0);

			 //try {
				  
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

				 paidItems = BillPaymentUtil.getPaidItemsByBillPayments(payments);
				 mav.addObject("reportMsg1", "Total Received Amount From "+startDateStr+" To "+endDateStr);
				 
				 List<HopService> reportColumns = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.cashierReportColumns");
	     		 for (HopService hs : reportColumns) {
	     			 PaidServiceRevenue psr= ReportsUtil.getPaidServiceRevenue(paidItems, hs.getName());
					if(psr!=null){
						paidServiceRevenues.add(psr);
						//totalReceivedAmount=totalReceivedAmount.add(psr.getPaidAmount());
					}
				}
	 			mav.addObject("totalReceivedAmount", BillPaymentUtil.getTotalPaid(payments));
				mav.addObject("paidServiceRevenues", paidServiceRevenues);
				
				request.getSession().setAttribute("paidServiceRevenues" , paidServiceRevenues);
				request.getSession().setAttribute("totalReceivedAmount", BillPaymentUtil.getTotalPaid(payments));
				
			/*} catch (Exception e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"No payment found !");
				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+e.getMessage());
			}
*/
	}

		mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		mav.addObject("thirdParties", Context.getService(BillingService.class).getAllThirdParties());
		mav.setViewName(getViewName());
		
		if(request.getParameter("print")!=null){
			 List<PaidServiceRevenue> paidServiceRevenues = (List<PaidServiceRevenue>) request.getSession().getAttribute("paidServiceRevenues" );
			 BigDecimal amount = (BigDecimal)request.getSession().getAttribute("totalReceivedAmount");
			 FileExporter fexp = new FileExporter();
			 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String fileName = "cashier_daily_report_on_"+df.format(new Date())+".pdf";
			    fexp.printCashierReport(request, response, amount,paidServiceRevenues, fileName);
		}
		return mav;
}
}
