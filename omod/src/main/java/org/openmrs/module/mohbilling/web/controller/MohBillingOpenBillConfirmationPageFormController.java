package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.automation.MTNMomoApiIntegrationRequestToPay;
import org.openmrs.module.mohbilling.businesslogic.*;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class MohBillingOpenBillConfirmationPageFormController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	//@RequestMapping(value = "/redirect", method = RequestMethod.GET)
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		/*if (request.getParameter("formStatus") != null
				&& !request.getParameter("formStatus").equals("")) {*/

		/*request.getSession().setAttribute("startDateStr" , "");
		request.getSession().setAttribute("endDateStr" , "");
*/



		String startDateStr = request.getParameter("startDate");
			String startHourStr = "00";
			String startMinStr = "00";


			System.out.println("Starttttttttttttddd:"+startDateStr);
			String endDateStr = request.getParameter("endDate");
			String endHourStr = "23";
			String endMinuteStr = "59";


		System.out.println("Endddddddddddddddddd:"+endDateStr);

		/*if (!request.getSession().getAttribute("startDateStr").equals("") && request.getSession().getAttribute("startDateStr")!=null && (startDateStr.equalsIgnoreCase("") || startDateStr==null))
			startDateStr= request.getSession().getAttribute("startDateStr").toString();
		if (!request.getSession().getAttribute("endDateStr").equals("") && request.getSession().getAttribute("endDateStr")!=null && (endDateStr.equalsIgnoreCase("") || endDateStr==null))
			endDateStr= request.getSession().getAttribute("endDateStr").toString();
*/

			// parameters
			Object[] params = ReportsUtil.getReportParameters(startDateStr, startHourStr, startMinStr, endDateStr, endHourStr, endMinuteStr, null, null, null);

			Date startDate = (Date) params[0];
			Date endDate = (Date) params[1];


			List<Consommation> cons = ConsommationUtil.getConsommationsWithPatientNotConfirmed(startDate, endDate);


			mav.addObject("consommations", cons);

			if(request.getParameter("consommationId")!=null && !request.getParameter("consommationId").equals("")) {
				BillingService service = Context.getService(BillingService.class);
				Consommation consommation=service.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
				PatientBill pb=consommation.getPatientBill();
				if (!pb.isPaymentConfirmed()) {
					BillPayment billPayment = new BillPayment();
					billPayment.setCollector(Context.getAuthenticatedUser());
					billPayment.setDateReceived(new Date());
					billPayment.setPatientBill(pb);
					billPayment.setCreatedDate(new Date());
					billPayment.setCreator(Context.getAuthenticatedUser());

					//mark as paid all  selected items for payment purpose


						billPayment.setAmountPaid(pb.getAmount());
						//create cashPayment
						CashPayment cp = new CashPayment(billPayment);
						cp.setCreator(Context.getAuthenticatedUser());
						cp.setVoided(false);
						cp.setCreatedDate(new Date());

						cp = PatientBillUtil.createCashPayment(cp);
						billPayment = cp;

						createPaidServiceBill(consommation, cp);

						pb.setPaymentConfirmed(true);
						pb.setPaymentConfirmedBy(Context.getAuthenticatedUser());
						pb.setPaymentConfirmedDate(new Date());

						service.savePatientBill(pb);

						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
								"The Bill Payment with MoMo been saved successfully !");
				}
			}

		if(request.getParameter("patientBillId")!=null && !request.getParameter("patientBillId").equals("")) {
			BillingService service = Context.getService(BillingService.class);
			//Consommation consommation=service.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
			PatientBill pb=service.getPatientBill(Integer.parseInt(request.getParameter("patientBillId")));
			if ((pb.getTransactionStatus()==null || !pb.getTransactionStatus().equals("SUCCESSFUL")) && request.getParameter("requesttopay")!=null) {
				String phoneNumber=request.getParameter("phoneNumber");
				pb.setPhoneNumber(phoneNumber);
				String referenceId = (UUID.randomUUID()).toString();
				pb.setReferenceId(referenceId);
				MTNMomoApiIntegrationRequestToPay momo=new MTNMomoApiIntegrationRequestToPay();


				momo.requesttopay(referenceId,pb.getAmount().setScale(0, RoundingMode.UP).toString(),phoneNumber);
				pb.setTransactionStatus(momo.getransactionStatus(referenceId));
				pb =PatientBillUtil.savePatientBill(pb);

				//service.savePatientBill(pb);

				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						"Request to pay has been sent to patient on phone number "+phoneNumber+" !");
			}
		}

		//}
		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);
		mav.addObject("consommationId", "");
		mav.addObject("patientBillId", "");
		mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		mav.addObject("insurances", InsuranceUtil.getAllInsurances());
		mav.addObject("thirdParties", InsurancePolicyUtil.getAllThirdParties());
		mav.addObject("departments", DepartementUtil.getAllHospitalDepartements());

		return mav;
		/*String projectUrl= "openConfirmationPage.form?startDate="+startDateStr+"&endDate="+endDateStr;
		return new ModelAndView(projectUrl);*/
	}


	public void createPaidServiceBill(Consommation consommation, BillPayment bp){

		Set<PatientServiceBill> billedItems= consommation.getBillItems();

		for (PatientServiceBill  psb : billedItems) {


			PaidServiceBill paidSb = new PaidServiceBill();
			/*String[] splittedParameterName = parameterName.split("-");
			String psbIdStr = splittedParameterName[2];
			//String psbIdStr = request.getParameter(parameterName);
			Integer  patientServiceBillId = Integer.parseInt(psbIdStr);
			PatientServiceBill psb  = ConsommationUtil.getPatientServiceBill(patientServiceBillId);*/


			paidSb.setBillItem(psb);

			BigDecimal paidQuantity = psb.getQuantity();

					//new BigDecimal(Double.valueOf(request.getParameter("paidQty_"+psb.getPatientServiceBillId())));
			paidSb.setPaidQty(paidQuantity);
			paidSb.setBillPayment(bp);
			paidSb.setCreator(Context.getAuthenticatedUser());
			paidSb.setCreatedDate(new Date());
			paidSb.setVoided(false);
			BillPaymentUtil.createPaidServiceBill(paidSb);

			BigDecimal totalQtyPaid=paidQuantity;
			if(psb.getPaidQuantity()!=null)
				totalQtyPaid = psb.getPaidQuantity().add(paidQuantity);

			//if paid,then update patientservicebill as paid
			psb.setPaid(true);
			psb.setPaidQuantity(totalQtyPaid);
			ConsommationUtil.createPatientServiceBill(psb);
		}


	}


}