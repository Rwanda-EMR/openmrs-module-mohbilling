package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.Invoice;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingFactureRecovery extends ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;
	
	ModelAndView mav = new ModelAndView();

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List<String> categories = InsuranceUtil.getAllServiceCategories();

		ModelAndView mav = new ModelAndView();
		mav.addObject("allInsurances", InsuranceUtil.getAllInsurances());
		mav.addObject("categories", categories);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date startDate = null;

		String patientIdStr = null, insuranceStr = null, 
				startDateStr = null, endDateStr = null, serviceId = null, 
				cashCollector = null, startHourStr = null, startMinute = null, 
				endHourStr = null, endMinuteStr = null;
		

		LinkedHashMap<PatientBill, PatientInvoice> billMap = new LinkedHashMap<PatientBill, PatientInvoice>();
		
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
			Patient patient = null;
			BillingService service=Context.getService(BillingService.class);

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
				patient = Context.getPatientService().getPatient(
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
		
			
			List<PatientBill>patientBills = service.getBills(startDate, endDate,null);
			InsuranceRate insuranceRate =service.getInsuranceRateByInsurance(insurance);			
			Float rate =insuranceRate.getRate();
		
			//String[] serviceCategories = {"CHIRUR","CONSOMM", "CONSULT","DERMAT", "ECHOG", "FORMAL", "HOSPIT", "KINE","LABO","MAT","MEDECI", "MEDICAM","OPHTAL", "ORL", "OXGYNOT","PEDIAT","RADIO","SOINS INF","SOINS INT","STOMAT","GYNECO","POLYC"};
			
			String[] serviceCategories ={"CONSULT","LABO","IMAGERIE","ACTS","MEDICAMENTS","CONSOMM","AMBULANCE","AUTRE","HOSPITAL"};
			

			 for (PatientBill patientBill : patientBills) {
				 Insurance pbinsurance =patientBill.getBeneficiary().getInsurancePolicy().getInsurance();
				 if (pbinsurance==insurance) {
		
					PatientInvoice patientInvoice = PatientBillUtil.getPatientInvoice(patientBill, insurance);					 
					 
					 billMap.put(patientBill, patientInvoice);
				}					
				
			}
		     				
			mav.addObject("patientBillMap", billMap);
			mav.addObject("serviceCategories", serviceCategories);
		
			mav.addObject("rate", ""+rate+"%");
			mav.addObject("tcketModel",""+(100-rate)+"%" );	
		}
		
		FileExporter fexp = new FileExporter();
		if(request.getParameter("excel")!=null){
			String filename = "facture"+new Date();
			fexp.exportToCSVFile(request, response, billMap, filename, "Recovery");
		}

		mav.setViewName(getViewName());

		return mav;

	}
	

}
