/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.CashPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author rbcemr 
 */
public class MohBillingPatientBillPaymentFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null ){			
			handleSavePatientBillPayment(request);
		}

		 try{
			Consommation  consommation = null;
		
			/*if (request.getParameter("consommationId") == null)
				return new ModelAndView(new RedirectView(
						"patientSearchBill.form"));*/

			consommation = Context.getService(BillingService.class).getConsommation(
					Integer.parseInt(request.getParameter("consommationId")));
			
			//patientBills = PatientBillUtil.getBillsByBeneficiary(pb.getBeneficiary());
					

			mav.addObject("consommation", consommation);
			//mav.addObject("patientBills", patientBills);
			mav.addObject("beneficiary", consommation.getBeneficiary());

			InsurancePolicy ip = consommation.getBeneficiary().getInsurancePolicy();
		    mav.addObject("insurancePolicy", ip);

			// check the validity of the insurancePolicy for today
			Date today = new Date();
			mav.addObject(
					"valid",
					((ip.getCoverageStartDate().getTime() <= today.getTime()) && (today
							.getTime() <= ip.getExpirationDate().getTime())));
			mav.addObject("todayDate", today);
			mav.addObject("authUser", Context.getAuthenticatedUser());

		} catch (Exception e) {
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();
			//return new ModelAndView(new RedirectView("patientSearchBill.form"));
		}

		return mav;
	}

	/**
	 * @param request
	 * @return
	 */
	private BillPayment handleSavePatientBillPayment(HttpServletRequest request) {
	
		BillPayment billPayment = null;
		// Float rate = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {			
			Consommation consommation =ConsommationUtil.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
			//PatientBill pb = PatientBillUtil.getPatientBillById(Integer
					//.parseInt(request.getParameter("patientBillId")));
			PatientBill pb =consommation.getPatientBill();

			// BigDecimal amountPaidByThirdPart = new BigDecimal(0);			
			
			// get all selected items and updated them as paid
			
			
			

			if (null != request.getParameter("receivedCash")) {
				BillPayment bp = new BillPayment();
				/**
				 * We need to add both Patient Due amount and amount paid by
				 * third part
				 */

			
				bp.setAmountPaid(BigDecimal.valueOf(Double.parseDouble(request
						.getParameter("receivedCash"))));
				bp.setCollector(Context.getAuthenticatedUser());
				bp.setDateReceived(Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));				
				bp.setPatientBill(pb);
				bp.setCreatedDate(new Date());
				bp.setCreator(Context.getAuthenticatedUser());					
				//bp = PatientBillUtil.createBillPayment(bp);
				
				//create cashPayment
				
				CashPayment cp =new CashPayment(bp);
				cp.setCreator(Context.getAuthenticatedUser());
				cp.setVoided(false);
				cp.setCreatedDate(new Date());			
				//mark as paid all  selected items for payment purpose
			
				
				cp =PatientBillUtil.createCashPayment(cp);
				
				createPaidServiceBill(request, consommation, cp);		
				

				/** Marking a Bill as PAID */
				//PatientBillUtil.markBillAsPaid(pb);

				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR,
						"The Bill Payment has been saved successfully !");

				return billPayment;

			} else {
				request.getSession()
						.setAttribute(
								WebConstants.OPENMRS_MSG_ATTR,
								"The Bill Payment cannot be saved when the 'Received Amount' is BLANK or is < 0 !");
				return null;
			}

		} catch (Exception e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The Bill Payment has not been saved !");
			log.error("" + e.getMessage());
			e.printStackTrace();

			return null;
		}

	

	}
	
	
	public void createPaidServiceBill(HttpServletRequest request,Consommation consommation, BillPayment bp){
  
		Map<String, String[]> parameterMap = request.getParameterMap();	
		for (String  parameterName : parameterMap.keySet()) {
			
			if (!parameterName.startsWith("item-")) {
				continue;
			}
			PaidServiceBill paidSb = new PaidServiceBill();
			
			
			String psbIdStr = request.getParameter(parameterName);
			Integer  patientServiceBillId = Integer.parseInt(psbIdStr);	
			PatientServiceBill psb  =ConsommationUtil.getPatientServiceBill(patientServiceBillId);	
			
			paidSb.setPaidItem(psb);
			paidSb.setBillPayment(bp);
			paidSb.setCreator(Context.getAuthenticatedUser());
			paidSb.setCreatedDate(new Date());			
			paidSb.setVoided(false);
			BillPaymentUtil.createPaidServiceBill(paidSb);
			
			//if paid,then update patientservicebill as paid
			psb.setPaid(true);
			ConsommationUtil.createPatientServiceBill(psb);
			
			
			
		}
		
		
	}


}
