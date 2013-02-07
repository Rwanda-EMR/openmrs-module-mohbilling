package org.openmrs.module.mohbilling.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.RecoveryUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingManageRecoveryController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());
		BillingService billingService = Context.getService(BillingService.class);
		//RecoveryUtil recoveryUtil = new RecoveryUtil();
		ReportsUtil reportUtil= new ReportsUtil(); 
       // ReportsUtil reportUtil = Context.getService(ReportsUtil.class);
         		
		mav.addObject("allInsurances",billingService.getAllInsurances());
		// ReportsUtil reportUtil = Context.getService(ReportsUtil.class);

		mav.addObject("allInsurances", billingService.getAllInsurances());
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = null;
		Date endDate = null;
		Insurance insurance = null;
		String insuranceStr = request.getParameter("insurance"), 
		startDateStr = request.getParameter("startDate"),
		endDateStr = request.getParameter("endDate");
		Integer insuranceIdInt = null;
		if (startDateStr != null && !startDateStr.equals("")) {
			startDate = (Date) formatter.parse(startDateStr);
		}

		if (endDateStr != null && !endDateStr.equals("")) {
			endDate = (Date) formatter.parse(endDateStr);

		}

		if (insuranceStr != null && !insuranceStr.equals("")) {
			insuranceIdInt = Integer.parseInt(insuranceStr);
			insurance = billingService.getInsurance(insuranceIdInt);
		}

		// getting parameters after payment is done

		String amountPaid = request.getParameter("amountPaid");
		String datePayment = request.getParameter("datePayment");

		Date datePaymentFormatted = null;
		if (datePayment != null && !datePayment.equals("")) {
			datePaymentFormatted = (Date) formatter.parse(datePayment);
		}
		Date startDateNewFormatted = null;
		String startDateNew = request.getParameter("startDateNew");
		if (startDateNew != null && !startDateNew.equals("")) {
			startDateNewFormatted = (Date) formatter.parse(startDateNew);
		}
		Date endDateNewFormatted = null;
		String endDateNew = request.getParameter("endDateNew");
		if (endDateNew != null && !endDateNew.equals("")) {
			endDateNewFormatted = (Date) formatter.parse(endDateNew);
		}
		String insuranceId = request.getParameter("insuranceId");
		if (amountPaid != null) {

			Insurance insuranceToSave = new Insurance();
			insuranceToSave.setInsuranceId(Integer.parseInt(insuranceId));

			Recovery recoveryObj = new Recovery();

			recoveryObj.setInsuranceId(insuranceToSave);

			recoveryObj.setStartPeriod(startDateNewFormatted);
			recoveryObj.setEndPeriod(endDateNewFormatted);
			if(amountPaid != null && !amountPaid.equals(""))
			recoveryObj.setPaidAmount(Float.parseFloat(amountPaid));
			recoveryObj.setPayementDate(datePaymentFormatted);
            recoveryObj.setCreator(Context.getAuthenticatedUser());
		    recoveryObj.setCreatedDate(new Date());
		    recoveryObj.setRetired(false);
		 if (recoveryObj != null && amountPaid != null && !amountPaid.equals("")) {
			 BillingService service = Context.getService(BillingService.class);
				
				service.saveRecovery(recoveryObj);
		 
				mav.addObject("amountPaid", Float.parseFloat(amountPaid));
				mav.addObject("startDateNew", startDateNew);
				mav.addObject("endDateNew", endDateNew);

				mav.addObject("msg", "Saved!!!");
			}

		}

		if (insurance != null)
			mav.addObject("insuranceId", insurance.getInsuranceId());
		mav.addObject("insurance", insurance);
		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);
		
		// if(insurance != null && startDateNewFormatted != null &&
		// endDateNewFormatted != null){
		// log.info(" wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww insurance "+insurance+" aaaaaaaaaaaaaaaaaaa  the first aaaaaaaaaaaaaa startDate"+startDateNewFormatted+" tttttttttttttttttttttttttttt tttttttttendDate"+endDateNewFormatted);

		// billingService.getPaidAmountPerInsuranceAndPeriod(insurance,
		// startDateNewFormatted, endDateNewFormatted));
		// }
		if (insuranceStr != null && !insuranceStr.equals(""))
			mav.addObject("insuranceName", insurance.getName());
		if (insurance != null && startDate != null && endDate != null) {
			mav.addObject("amount", ReportsUtil.getMonthlyInsuranceDueAmount(
					insurance, startDate, endDate, false));
			mav.addObject("AlreadyPaidAmount", billingService
					.getPaidAmountPerInsuranceAndPeriod(insurance, startDate,
							endDate));
		}
		
		mav.addObject("amountPaid",amountPaid);
		mav.addObject("datePayment",datePayment);
		return mav;
	}

}
