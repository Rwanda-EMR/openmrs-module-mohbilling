/**
 * Auto generated file comment
 */
package org.openmrs.module.mohbilling.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class MohBillingInsuranceInvoiceController extends
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
		
		rebuildExistingParameters(request,mav);


		if (request.getParameter("startDate") != null && !request.getParameter("startDate").equals("")) {	
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
			Patient patient=null;
			

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
			
			 Object[] allfactureCompiled =Context.getService(BillingService.class).getBills(startDate, endDate,null);
			 Set<PatientBill> bills = (Set<PatientBill>) allfactureCompiled[0];
			 Double receivedAmount =(Double) allfactureCompiled[1];
			
			
			String[] serviceCategories = {"FORMALITES ADMINISTRATIVES","CONSULTATION","LABORATOIRE","RADIOLOGIE","ECHOGRAPHIE","OPHTALMOLOGIE","CHIRURGIE","MEDEC","CONSOMMABLES","KINESITHERAPIE","STOMATOLOGIE","MATERNITE","AMBULANCE","SOINS INFIRMIERS","MEDICAMENTS","HOSPITALISATION"};  
			
			//Map<String,List<PatientServiceBill>> groupMap = new HashMap<String, List<PatientServiceBill>>();
			List<PatientInvoice> patientInvoiceList = new ArrayList<PatientInvoice>();
			
				Insurance pbInsurance = null;
				User pbCreator =null;
				for (PatientBill pb : bills) {					
					    pbCreator=pb.getCreator();
						pbInsurance=pb.getBeneficiary().getInsurancePolicy().getInsurance();
					if(patient==pb.getBeneficiary().getPatient()||insurance==pbInsurance||cashCollector==pbCreator.getUsername()){
						patientInvoiceList.add(PatientBillUtil.getPatientInvoice(pb, insurance));
					}
				}
			mav.addObject("patientInvoiceList", patientInvoiceList);
			
//
//			mav.addObject("startDate", startDate);
//			mav.addObject("endDate", endDate);
//			mav.addObject("insurance", insurance);
//			mav.addObject("cashCollector", cashCollector);
//			mav.addObject("patientId", patientId);
			
		}
		// print.....
					String billIdStr = null;
					if(request.getParameter("billId")!=null){
						billIdStr=request.getParameter("billId");
						Integer billId = Integer.parseInt(billIdStr);
						PatientBill patientBill =  Context.getService(BillingService.class).getPatientBill(billId); 
						PatientInvoice patientInvoice = PatientBillUtil.getPatientInvoice(patientBill, null);
						mav.addObject("patientInvoice", patientInvoice);			
					
					}					
					// export
					String patientBillIdStr=null;
					FileExporter fexp = new FileExporter();
					if (request.getParameter("patientBillId") != null) {
						patientBillIdStr=request.getParameter("patientBillId");
						Integer patientBillId=Integer.parseInt(patientBillIdStr);
						PatientBill patientBill =  Context.getService(BillingService.class).getPatientBill(patientBillId); 
						PatientInvoice patientInvoice = PatientBillUtil.getPatientInvoice(patientBill, null);
						String invoiceOwner = "facNo"+patientBill.getPatientBillId()+"On"+patientBill.getCreatedDate()+".pdf";
						fexp.exportToPDF(request, response,patientInvoice,invoiceOwner,"Details des soins recus");
				    }
		mav.setViewName(getViewName());

		return mav;

	}	
	/**
	 * @param request
	 * @param mav
	 */
	private void rebuildExistingParameters(HttpServletRequest request,ModelAndView mav) {
		String param = (request.getParameter("startDate") != null) ? "&startDate="
				+ request.getParameter("startDate") : "";
		param += (request.getParameter("startHour") != null) ? "&startHour="
			  + request.getParameter("startHour") : "";
		param += (request.getParameter("startMinute") != null) ? "&startMinute="
			  + request.getParameter("startMinute") : "";
		param += (request.getParameter("endDate") != null) ? "&endDate="
				+ request.getParameter("endDate") : "";
		param += (request.getParameter("endHour") != null) ? "&endHour="
			    + request.getParameter("endHour") : "";
		param += (request.getParameter("endMinute") != null) ? "&endMinute="
			   + request.getParameter("endMinute") : "";
		param += (request.getParameter("patientId") != null) ? "&patientId="
				+ request.getParameter("patientId") : "";
		param += (request.getParameter("cashCollector") != null) ? "&cashCollector="
			  + request.getParameter("cashCollector") : "";
		param += (request.getParameter("insurance") != null) ? "&insurance="
			  + request.getParameter("insurance") : "";

		mav.addObject("prmtrs", param);
	}

}

