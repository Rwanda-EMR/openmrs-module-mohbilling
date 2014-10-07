/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.runtime.directive.Parse;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MohBIllingrReceivedAmountController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		String startDateStr = null;
		String endDateStr = null;
		String billCollector = null;
		Date startDate = null;
		Date endDate = null ;
		User collector = null;
		BillPaymentUtil billPaymentUtil = null;
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		if (request.getParameter("startDate") != null && request.getParameter("endDate") != null
				&& request.getParameter("billCollector") != null) {
			
			
			
			startDateStr = request.getParameter("startDate");
			endDateStr = request.getParameter("endDate");

			billCollector = request.getParameter("billCollector");

			if (!startDateStr.equals("")) {
				startDate = (Date) formatter.parse(startDateStr);
			}
			if (!endDateStr.equals("")) {
				endDate = (Date) formatter.parse(endDateStr);
			}
			
			Integer billCollectorId = null;
             
			if(billCollector != null && billCollector != ""){
			billCollectorId = Integer.parseInt(billCollector);
			collector = Context.getUserService().getUser(billCollectorId);
             }

			List<BillPayment> billPaymentsByDateAndCollector = billPaymentUtil
					.getBillPaymentsByDateAndCollector(startDate,endDate, collector);

			mav.addObject("billPaymentsByDateAndCollector",
					billPaymentsByDateAndCollector);

			Double TotalReceivedAmount = 0.0;

			for (BillPayment billPayment : billPaymentsByDateAndCollector) {

				TotalReceivedAmount = TotalReceivedAmount
						+ billPayment.getAmountPaid().doubleValue();

			}
            
			mav.addObject("TotalReceivedAmount", TotalReceivedAmount); 
		}

		mav.setViewName(getViewName());

		return mav;

	}

}
