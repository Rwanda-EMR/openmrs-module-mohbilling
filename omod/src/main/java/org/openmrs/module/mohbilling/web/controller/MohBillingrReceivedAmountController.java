/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingrReceivedAmountController extends
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

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String patientIdStr = null, insuranceStr = null, 
				startDateStr = null, endDateStr = null, serviceId = null, 
				cashCollector = null, startHourStr = null, startMinute = null, 
				endHourStr = null, endMinuteStr = null;

		if (request.getParameter("formStatus") != null && !request.getParameter("formStatus").equals("")) {
			
			startHourStr = request.getParameter("startHour");
			startMinute = request.getParameter("startMinute"); 
			endHourStr = request.getParameter("endHour");
			endMinuteStr = request.getParameter("endMinute");

			String startTimeStr = startHourStr + ":" + startMinute + ":00";
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
			
			if(request.getParameter("patientId") != null)
				patientIdStr = request.getParameter("patientId");
				
			if(request.getParameter("cashCollector") != null)
				cashCollector = request.getParameter("cashCollector");
			
			
			if(request.getParameter("insurance") != null)
				insuranceStr = request.getParameter("insurance");			

			Integer insuranceIdInt = null;
			Integer patientId = null;
			// Date endDate = null;
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

//			Double TotalReceivedAmount = (double) 0;
			BigDecimal TotalReceivedAmount = new BigDecimal(0);

			for (BillPayment billPayment : reportedPayments) {
//				TotalReceivedAmount = TotalReceivedAmount
//						+ billPayment.getAmountPaid().doubleValue();
				TotalReceivedAmount = TotalReceivedAmount.add(billPayment.getAmountPaid());
			}
			request.getSession().setAttribute("payments" , reportedPayments);
			
			mav.addObject("TotalReceivedAmount", TotalReceivedAmount);
			

		}
		if (request.getParameter("printed")!=null) {
			HttpSession session = request.getSession(true);

			List<BillPayment> payments = (List<BillPayment>) session.getAttribute("payments");
			
			FileExporter fexp = new FileExporter();
			String fileName = "Deposit Report.pdf";

			//fexp.pdfPrintPaymentsReport(request, response, payments, fileName, "Deposit");	
		}
		mav.setViewName(getViewName());

		return mav;

	}

}
