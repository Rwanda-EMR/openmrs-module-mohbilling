package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.module.mohbilling.businesslogic.DepositUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Transaction;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MohBillingDepositReportController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {


		ModelAndView mav = new ModelAndView();

		String startDateStr = null, endDateStr = null, startHourStr = null, startMinuteStr = null,
				endHourStr = null, endMinuteStr = null;


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

			String transactionType = request.getParameter("transactionType");

			String reason="";
			if(transactionType.equals("deposit"))
				reason = "Deposit";
			else if(transactionType.equals("withdrawal"))
				reason = "Withdrawal";
			else if(transactionType.equals("billPayment"))
				reason="Bill Payment";

			List<Transaction> transactions = DepositUtil.getTransactions(startDate, endDate, collector, reason);
			request.getSession().setAttribute("transactions" , transactions);
			BigDecimal total = new BigDecimal(0);
			for (Transaction trans : transactions) {
				total=total.add(trans.getAmount());
			}
			mav.addObject("transactions", transactions);
			mav.addObject("total", total);
			mav.addObject("reason", reason);

		}
		mav.setViewName(getViewName());
		if(request.getParameter("printPdf")!=null){
			List<Transaction> transactions =  (List<Transaction>) request.getSession().getAttribute("transactions" );
			FileExporter fexp = new FileExporter();
			String fileName = "cashier_daily_report.pdf";
			fexp.printDepositReport(request, response, transactions, fileName);
		}
		return mav;
	}

}