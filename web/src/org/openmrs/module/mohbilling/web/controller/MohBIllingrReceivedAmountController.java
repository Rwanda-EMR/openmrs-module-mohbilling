/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBIllingrReceivedAmountController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List<String> categories = InsuranceUtil.getAllServiceCategories();
		List<BillPayment> reportedPayments = new ArrayList<BillPayment>();

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", InsuranceUtil.getAllInsurances());
		mav.addObject("categories", categories);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	//	Date startDate = null;
		
		log.info("testtestetetetetttttttttttttt--today "+new Date());
		log.info("testtestetetetetttttttttttttt--startHour "+request.getParameter("startHour"));
		log.info("testtestetetetetttttttttttttt--startMinute "+request.getParameter("startMinute"));
		log.info("testtestetetetetttttttttttttt--endHour "+request.getParameter("endHour"));
		log.info("testtestetetetetttttttttttttt--endMinute "+request.getParameter("endMinute"));

		if (request.getParameter("patientId") != null
				&& request.getParameter("insurance") != null
				&& request.getParameter("startDate") != null
				&& request.getParameter("endDate") != null
				&& request.getParameter("serviceId") != null
				&& request.getParameter("cashCollector") != null) {

			String patientIdStr = request.getParameter("patientId"), insuranceStr = request
					.getParameter("insurance"), startDateStr = request
					.getParameter("startDate"), endDateStr = request
					.getParameter("endDate"), serviceId = request
					.getParameter("serviceId"), cashCollector = request
					.getParameter("cashCollector"),startHourStr=request.getParameter("startHour"),startMinute=request.getParameter("startMinute"),
					endHourStr=request.getParameter("endHour"),endMinuteStr=request.getParameter("endMinute")
					;
			
			Date startDate = sdf.parse(startDateStr.split("/")[2] + "-"
					+ startDateStr.split("/")[1] + "-"
					+ startDateStr.split("/")[0]);
			
			Date endDate = sdf.parse(endDateStr.split("/")[2] + "-"
					+ endDateStr.split("/")[1] + "-"
					+ endDateStr.split("/")[0]);

			Integer insuranceIdInt = null;
			Integer patientId = null;
	//		Date endDate = null;
			Insurance insurance = null;
			String patientNames = null;

			User user = Context.getAuthenticatedUser();

			String cashierNames = (user.getPersonName().getFamilyName() != null ? user
					.getPersonName().getFamilyName() : "")
					+ " "
					+ (user.getPersonName().getMiddleName() != null ? user
							.getPersonName().getMiddleName() : "")
					+ " "
					+ (user.getPersonName().getGivenName() != null ? user
							.getPersonName().getGivenName() : "");

//			if (!startDateStr.equals("")) {
//				startDate = (Date) formatter.parse(startDateStr);
//			}
//
//			if (!endDateStr.equals("")) {
//				endDate = (Date) formatter.parse(endDateStr);
//			}

			if (!request.getParameter("patientId").equals("")) {

				patientIdStr = request.getParameter("patientId");
				patientId = Integer.parseInt(patientIdStr);
				Patient patient = Context.getPatientService().getPatient(
						patientId);
				patientNames = (patient.getFamilyName() != null ? patient
						.getFamilyName() : "")
						+ " "
						+ (patient.getMiddleName() != null ? patient
								.getMiddleName() : "")
						+ " "
						+ (patient.getGivenName() != null ? patient
								.getGivenName() : "");
			}

			if (!request.getParameter("insurance").equals("")) {
				insuranceIdInt = Integer.parseInt(insuranceStr);
				insurance = InsuranceUtil.getInsurance(insuranceIdInt);
			}

			reportedPayments = ReportsUtil.paymentsCohortBuilder(insurance,
					startDate, endDate, patientId, serviceId, null,
					cashCollector);
			
			
			
			mav.addObject("reportedPayments", reportedPayments);
			mav.addObject("startDateStr", startDateStr);
			mav.addObject("endDateStr", endDateStr);
			mav.addObject("serviceId", serviceId);
			mav.addObject("insurance", insurance);
			mav.addObject("patientId", patientId);
			mav.addObject("cashCollector", cashCollector);
			mav.addObject(
					"healthFacilityName",
					Context.getAdministrationService().getGlobalProperty(
							"billing.healthFacilityName"));
			mav.addObject("address", Context.getAdministrationService()
					.getGlobalProperty("billing.healthFacilityPhysicalAddress"));
			mav.addObject(
					"healthFacilityShortCode",
					Context.getAdministrationService().getGlobalProperty(
							"billing.healthFacilityShortCode"));
			mav.addObject(
					"healthFacilityLogo",
					Context.getAdministrationService().getGlobalProperty(
							"billing.healthFacilityLogo"));
			mav.addObject(
					"healthFacilityEmail",
					Context.getAdministrationService().getGlobalProperty(
							"billing.healthFacilityEmail"));
			mav.addObject("today", new Date());
			mav.addObject("patientNames", patientNames);
			mav.addObject("cashierNames", cashierNames);
			
			Double TotalReceivedAmount = (double) 0;

			for (BillPayment billPayment : reportedPayments) {
				TotalReceivedAmount = TotalReceivedAmount
						+ billPayment.getAmountPaid().doubleValue();

			}
			
            
			mav.addObject("TotalReceivedAmount", TotalReceivedAmount); 
		
		}

		mav.setViewName(getViewName());

		return mav;

	}

}
