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
import org.openmrs.module.mohbilling.businesslogic.MohBillingTagUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
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
					PatientBillUtil.markBillAsPaid(bill);
				
				Date serviceDate = null;
				double patDueAmt = 0, insDueAmt = 0, totalDueAmt = 0, payments = 0;
//				double cost = 0;
				for (PatientServiceBill item : bill.getBillItems()) {
					serviceDate = item.getServiceDate();
					double qty = item.getQuantity().doubleValue();
					double rate = bill.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
					
					//act and pharmacy unit price
					double unitPrice = item.getUnitPrice().doubleValue();
				    
					double cost= unitPrice*qty;

					patDueAmt += ReportsUtil.roundTwoDecimals(cost*(100-rate)/100);

					insDueAmt += ReportsUtil.roundTwoDecimals(cost*rate/100);
					
					totalDueAmt = patDueAmt + insDueAmt;

				}
				
				//TODO: must check this method called right here from Tag...
				payments = ReportsUtil.roundTwoDecimals(Long.parseLong(MohBillingTagUtil.getTotalAmountPaidByPatientBill(bill.getPatientBillId())));
				billObj.add(new Object[] {
						Context.getDateFormat().format(serviceDate),
						bill.getBeneficiary().getPolicyIdNumber(),
						bill.getBeneficiary().getPatient().getGivenName() + " " + bill.getBeneficiary().getPatient().getFamilyName(),
						bill.getBillItems(),
						bill.getBeneficiary().getInsurancePolicy().getInsurance().getName(),
						ReportsUtil.roundTwoDecimals(insDueAmt),
						ReportsUtil.roundTwoDecimals(patDueAmt),
						ReportsUtil.roundTwoDecimals(payments),
						ReportsUtil.roundTwoDecimals(totalDueAmt),
						bill.getStatus(),
						bill.getPatientBillId()});

				totalAmount += totalDueAmt;
				totalPatientDueAmount += patDueAmt;
				totalInsuranceDueAmount += insDueAmt;
				totalAmountReceived += payments;

			}
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
				PatientInvoice patientInvoice = PatientBillUtil.getPatientInvoice(patientBill, null);
				String invoiceOwner = "facNo"+patientBill.getPatientBillId()+"_"+patientBill.getBeneficiary().getPatient().getFamilyName()+" "+patientBill.getBeneficiary().getPatient().getGivenName()+"On"+patientBill.getCreatedDate()+".pdf";
				fexp.exportPatientBillToPDF(request, response,patientInvoice,invoiceOwner,"Details des soins recus");
			}
			
		}
		
		// display all bills to think about on
//		List<PatientBill> pendingBills = null;
//		pendingBills = Context.getService(BillingService.class).getPendingBill();
//		int alertSize  = (pendingBills == null) ? 0 : pendingBills.size();
//
//		mav.addObject("pendingBills", pendingBills);
//		mav.addObject("alertSize", alertSize);
//		
//		//calculate the due
//	    // minus the paid amount
//		// the rest will the total unpaid
//
//		Double totalUnpaid = 0.0;
//		Double totalDueAmount = 0.0;
//		Double totalPaid = 0.0;
//		for (PatientBill pb : pendingBills) {
//			double patRate = 100-(pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate());
//			
//			totalDueAmount+=(pb.getAmount().doubleValue())*patRate;
////			for (BillPayment pay : pb.getPayments()) {
////				totalPaid+=pay.getAmountPaid().doubleValue();
////			}
//			totalPaid = pb.getAmountPaid().doubleValue();
//			log.info("patidddddddddddiiiiiiiiiiiiiiiii "+pb.getPatientBillId()+"due---"+pb.getAmount()+"paid----"+pb.getAmountPaid());
//			totalUnpaid+=totalDueAmount-pb.getAmountPaid().doubleValue();
//		}
//		log.info("totalDueeeeeeeeeeeeeeeeee "+totalDueAmount);
//		log.info("totalPaiddddddddddddddddddd "+totalPaid);
////		totalUnpaid = totalDueAmount - totalPaid;
//		mav.addObject("totalUnpaid", totalUnpaid);
		mav.setViewName(getViewName());
		

		return mav;

	}

	


}
