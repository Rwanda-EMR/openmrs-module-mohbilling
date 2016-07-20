/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingCohortBuilderFormController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List<String> categories = InsuranceUtil.getAllServiceCategories();
		List<PatientBill> reportedPatientBills = new ArrayList<PatientBill>();

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", InsuranceUtil.getAllInsurances());
		mav.addObject("categories", categories);

		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = null;
		String patientIdStr = null, startDateStr = null, endDateStr = null, billStatus = null, billCreator = null;
		if (request.getParameter("startDate") != null || request.getParameter("endDate") != null) {
			startDateStr = request.getParameter("startDate");
			endDateStr = request.getParameter("endDate");
			
			Integer insuranceIdInt = null;
			Integer patientId = null;
			Date endDate = null;
			Insurance insurance = null;
			String patientNames = null;
			
			
			if(request.getParameter("patientId") != null)
				patientIdStr = request.getParameter("patientId");
		
			if(request.getParameter("insurance") != null && !request.getParameter("insurance").equals("")) {
				insuranceIdInt = Integer.parseInt(request.getParameter("insurance"));
				insurance = InsuranceUtil.getInsurance(insuranceIdInt);
			}
			
			if(request.getParameter("billStatus") != null && !request.getParameter("billStatus").equals(""))
				billStatus = request.getParameter("billStatus");
			
			if(request.getParameter("billCreator") != null && !request.getParameter("billCreator").equals(""))
				billCreator = request.getParameter("billCreator");

			

			
			
			User user = Context.getAuthenticatedUser();
			
			String cashierNames = (user.getPersonName().getFamilyName() != null ? user
					.getPersonName().getFamilyName() : "")
					+ " "
					+ (user.getPersonName().getMiddleName() != null ? user
							.getPersonName().getMiddleName() : "")
					+ " "
					+ (user.getPersonName().getGivenName() != null ? user
							.getPersonName().getGivenName() : "");

			if (startDateStr != null) {
				startDate = (Date) formatter.parse(startDateStr);
			}

			if (endDateStr != null) {
				endDate = (Date) formatter.parse(endDateStr);
			}

			if (!request.getParameter("patientId").equals("")) {

				patientIdStr = request.getParameter("patientId");
				patientId = Integer.parseInt(patientIdStr);
				Patient patient = Context.getPatientService().getPatient(patientId);
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
				
			}

			reportedPatientBills = ReportsUtil.billCohortBuilder(insurance,	startDate, endDate, patientId, null, billStatus, billCreator);
			
//			log.info("chubbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb "+reportedPatientBills.size());

			mav.addObject("startDateStr", startDateStr);
			mav.addObject("endDateStr", endDateStr);
//			mav.addObject("serviceId", serviceId);
			mav.addObject("insurance", insurance);
			mav.addObject("patientId", patientId);
			mav.addObject("billStatus", billStatus);
			mav.addObject("billCreator", billCreator);
			mav.addObject("healthFacilityName",Context.getAdministrationService().getGlobalProperty("billing.healthFacilityName"));
			mav.addObject("address", Context.getAdministrationService().getGlobalProperty("billing.healthFacilityPhysicalAddress"));
			mav.addObject("healthFacilityShortCode",Context.getAdministrationService().getGlobalProperty("billing.healthFacilityShortCode"));
			mav.addObject("healthFacilityLogo", Context.getAdministrationService().getGlobalProperty("billing.healthFacilityLogo"));
			mav.addObject("healthFacilityEmail", Context.getAdministrationService().getGlobalProperty("billing.healthFacilityEmail"));
			mav.addObject("today", new Date());
			mav.addObject("patientNames", patientNames);
			mav.addObject("cashierNames", cashierNames);

			double totalAmount = 0, totalPatientDueAmount = 0, totalInsuranceDueAmount = 0, totalAmountReceived = 0;
			List<Object[]> billObj = new ArrayList<Object[]>();

			for (PatientBill bill : reportedPatientBills) {

				/** Updating status for unmarked ones */
				if(bill.getStatus() == null)
					//PatientBillUtil.markBillAsPaid(bill);
			
				
			mav.addObject("totalAmountReceived",
					ReportsUtil.roundTwoDecimals(totalAmountReceived));
			mav.addObject("insuranceDueAmount",
					ReportsUtil.roundTwoDecimals(totalInsuranceDueAmount));
			mav.addObject("patientDueAmount",
					ReportsUtil.roundTwoDecimals(totalPatientDueAmount));
			mav.addObject("totalAmount", ReportsUtil.roundTwoDecimals(totalAmount));
			mav.addObject("billObj", billObj);
			mav.addObject("reportedPatientBills", reportedPatientBills);
			mav.addObject("startDate", request.getParameter("startDate"));
			mav.addObject("endDate", request.getParameter("endDate"));
			mav.addObject("cashier", Context.getAuthenticatedUser().getPersonName().getGivenName());

			List<String> serviceNames = new ArrayList<String>();
			// new ReportsUtil();
			Map<String, String> billServiceNames = ReportsUtil
					.getAllBillItems(reportedPatientBills);

			for (String key : billServiceNames.keySet()) {
				serviceNames.add(billServiceNames.get(key));
			}

			mav.addObject("serviceNames", serviceNames);
			request.getSession().setAttribute("mariam" , reportedPatientBills);

			if (request.getParameter("print") != null)
				if (request.getParameter("print").equals("true")) {

//					log.info("00000000000000000000 Reaching there!!");
					if (request.getParameter("reportedPatientBills") != null) {
						// reportedPatientBills =
						// request.getParameter("reportedPatientBills");
						ReportsUtil.printPatientBillToPDF(request, response,
								reportedPatientBills);
					}

				}
		}
		
		String patientBillIdStr=null;
		
		if (request.getParameter("print")!=null) {
			//HttpSession session = request.getSession(true);
			String checked_bill[] = null;
			if(request.getParameterValues("checked_bill")!=null){
				checked_bill=request.getParameterValues("checked_bill");
				log.info("mamakakakakakaaaaaaaaaaaaaaaaaaaaa "+checked_bill.length);
			}
			for (int i = 0; i < checked_bill.length; i++) {
				FileExporter fexp = new FileExporter();
				log.info("myarrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrray "+checked_bill[i]);
				//print all these bills
				PatientBill patientBill =  Context.getService(BillingService.class).getPatientBill(Integer.parseInt(checked_bill[i]));				
				//String invoiceOwner = "facNo"+patientBill.getPatientBillId()+"_"+patientBill.getBeneficiary().getPatient().getFamilyName()+" "+patientBill.getBeneficiary().getPatient().getGivenName()+"On"+patientBill.getCreatedDate()+".pdf";
				
			}
			
		}
		mav.setViewName(getViewName());
		



	}
		return mav;
		}

}



