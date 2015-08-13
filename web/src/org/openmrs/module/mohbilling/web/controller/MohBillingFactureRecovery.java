package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Consommation;
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

		/*	if (!request.getParameter("patientId").equals("")) {

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
			}*/

			if (!request.getParameter("insurance").equals("")) {
				insuranceIdInt = Integer.parseInt(insuranceStr);
				insurance = InsuranceUtil.getInsurance(insuranceIdInt);
			
			}
		
			 Object[] allfactureCompiled =service.getBills(startDate, endDate,null);
				
			 Double receivedAmount =(Double) allfactureCompiled[1];        	
        	 Set<PatientBill> patientBills = (Set<PatientBill>) allfactureCompiled[0];
			
			InsuranceRate insuranceRate =service.getInsuranceRateByInsurance(insurance);			
			Float rate =insuranceRate.getRate();
		
			//String[] serviceCategories = {"CHIRUR","CONSOMM", "CONSULT","DERMAT", "ECHOG", "FORMAL", "HOSPIT", "KINE","LABO","MAT","MEDECI", "MEDICAM","OPHTAL", "ORL", "OXGYNOT","PEDIAT","RADIO","SOINS INF","SOINS INT","STOMAT","GYNECO","POLYC"};
			
			String[] serviceCategories ={"CONSULT","LABO","IMAGERIE","ACTS","MEDICAMENTS","CONSOMM","AMBULANCE","AUTRE","HOSPITAL"};
			Double totalConsult = 0.0;
			Double totalLabo = 0.0;
			Double totalImagery = 0.0;
			Double totalActs = 0.0;
			Double totalMedica = 0.0;
			Double totalConsomm = 0.0;
			Double totalAmbul = 0.0;
			Double totalAutres = 0.0;
			Double totalHosp = 0.0;
			Double total100 = 0.0;
			Double totalTickMod = 0.0;
			Double totalRate = 0.0;
			
			
			for (PatientBill patientBill : patientBills) {
				 Insurance pbinsurance =patientBill.getBeneficiary().getInsurancePolicy().getInsurance();
				 if (pbinsurance==insurance) {
		
					PatientInvoice patientInvoice = PatientBillUtil.getPatientInvoice(patientBill, insurance);	

					for (String ip : patientInvoice.getInvoiceMap().keySet()) {
//						log.info("ttttttttttttttttttttttttttttttttttttttttttttkey "+ip);
//						log.info("ttttttttttttttttttttttttttttttttttttttttttttSubTotal "+patientInvoice.getInvoiceMap().get(ip).getSubTotal());
						
						total100+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();;
						
						if(ip.equals("CONSULTATION"))
						totalConsult+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("LABORATOIRE"))
						totalLabo+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("IMAGERIE"))
						totalImagery+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("ACTS"))
						totalActs+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("MEDICAMENTS"))
						totalMedica+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("CONSOMMABLES"))
						totalConsomm+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("AMBULANCE"))
						totalAmbul+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("AUTRES"))
						totalAutres+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
						if(ip.equals("HOSPITALISATION"))
						totalHosp+=patientInvoice.getInvoiceMap().get(ip).getSubTotal();
					}
					
					 billMap.put(patientBill, patientInvoice);
				}					
				
			}
			totalRate=(total100*rate)/100;
			totalTickMod=(total100*((100-rate)/100));
			
			Double[] totals ={ReportsUtil.roundTwoDecimals(totalConsult),ReportsUtil.roundTwoDecimals(totalLabo),
					ReportsUtil.roundTwoDecimals(totalImagery),ReportsUtil.roundTwoDecimals(totalActs),
					ReportsUtil.roundTwoDecimals(totalMedica),ReportsUtil.roundTwoDecimals(totalConsomm),
					ReportsUtil.roundTwoDecimals(totalAmbul),ReportsUtil.roundTwoDecimals(totalAutres),
					ReportsUtil.roundTwoDecimals(totalHosp),ReportsUtil.roundTwoDecimals(total100),
					ReportsUtil.roundTwoDecimals(totalTickMod),ReportsUtil.roundTwoDecimals(totalRate)};
			

							
			mav.addObject("patientBillMap", billMap);
			mav.addObject("serviceCategories", serviceCategories);
			mav.addObject("totalByServices", totals);
		
			mav.addObject("rate", ""+rate+"%");
			mav.addObject("tcketModel",""+(100-rate)+"%" );	
			
			LinkedHashMap<PatientBill, PatientInvoice> billMapExport = new LinkedHashMap<PatientBill, PatientInvoice>();
				
			if (patientBills.size() > 0) {

//				Map<String, Double> mappedReport = getAllBillsByCollector(patientBills, serviceCategories,fullyreceivedAmount, partialypaids);
//				basedDateReport.put(simpleDateFormat.format(date),	mappedReport);	
				request.getSession().setAttribute("patientBillsInSession" , billMap);
				request.getSession().setAttribute("serviceCategories" , serviceCategories);
			//	log.info("WWWWWWWWWWWWWWWWWWWWWWWWWWW "+patientBills);
			}	
			
			mav.addObject("patientBills", patientBills);
			
		}
		
		
		
//		Set<PatientBill> patsBill = billMap.keySet();
//		FileExporter fexp = new FileExporter();
//		if(request.getParameter("excel")!=null && request.getParameter("excel").equals("true")){
//			log.info("TTTTTTTTTTTTTTTTTTTTT");
//			String filename = "facture"+new Date();
//			fexp.exportToCSVFile(request, response, patientBills, filename, "Recovery");
//		}
		
		if (request.getParameter("printed")!=null) {				
//			LinkedHashMap<PatientBill, PatientInvoice> patientBillMap =  (LinkedHashMap<PatientBill, PatientInvoice>) request.getSession().getAttribute("patientBillMap" );
			  
			HttpSession session = request.getSession(true);	
			//log.info("JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ "+session.getAttribute("patientBillsInSession"));
			
			String[] serviceCategories = (String[]) session.getAttribute("serviceCategories");
			LinkedHashMap<PatientBill, PatientInvoice> basedDateReport =  (LinkedHashMap<PatientBill, PatientInvoice>) session.getAttribute("patientBillsInSession");
			
			FileExporter fexp = new FileExporter();
			String fileName = "daily_report.pdf";
//		    fexp.printCashierReport(request, response,basedDateReport,fileName,"Daily Cashier report");	
			
			fexp.exportToCSVFile(request, response, basedDateReport, fileName, "Recovery");
			
//			for (PatientBill pb : basedDateReport.keySet()) {
//				log.info("Keyyyyyyy "+pb.getPatientBillId()+" Iddddddddddddd"+basedDateReport.get(pb).getInvoiceMap().keySet());
//				for (String st : basedDateReport.get(pb).getInvoiceMap().keySet()) {
//					log.info("fffffffffffffffffffffffffff "+basedDateReport.get(pb).getInvoiceMap().get(st).getSubTotal());
//				}
//			}
			
		}


		mav.setViewName(getViewName());

		return mav;

	}
	
	

	

}
