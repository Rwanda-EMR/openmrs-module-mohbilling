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
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.CashPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.DepositPayment;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.Transaction;
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
		BillPayment payment = null;
		
		if (request.getParameter("save") != null ){			
			payment = handleSavePatientBillPayment(request);
			mav.addObject("payment",payment);
		}

		 try{
			Consommation  consommation = null;
			List<Consommation> consommations = null;

			consommation = Context.getService(BillingService.class).getConsommation(
					Integer.parseInt(request.getParameter("consommationId")));
			
			consommations = ConsommationUtil.getConsommationsByBeneficiary(consommation.getBeneficiary());			

			mav.addObject("consommation", consommation);
			mav.addObject("consommations", consommations);
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
			mav.addObject("patientAccount", PatientAccountUtil.getPatientAccountByPatient(consommation.getBeneficiary().getPatient()));

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
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {			
			Consommation consommation =ConsommationUtil.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
			PatientBill pb =consommation.getPatientBill();
				BillPayment bp = new BillPayment();
				/**
				 * We need to add both Patient Due amount and amount paid by
				 * third part
				 */
				
				bp.setCollector(Context.getAuthenticatedUser());
				bp.setDateReceived(Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));				
				bp.setPatientBill(pb);
				bp.setCreatedDate(new Date());
				bp.setCreator(Context.getAuthenticatedUser());					
				//bp = PatientBillUtil.createBillPayment(bp);
				
				//mark as paid all  selected items for payment purpose
				
				if(request.getParameter("cashPayment")!=null){
					bp.setAmountPaid(BigDecimal.valueOf(Double.parseDouble(request
							.getParameter("receivedCash"))));
					//create cashPayment
					CashPayment cp =new CashPayment(bp);	
					setParams(cp, Context.getAuthenticatedUser(), false, new Date());
					
					cp =PatientBillUtil.createCashPayment(cp);
					billPayment =cp;
					createPaidServiceBill(request, consommation, cp);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
							"The Bill Payment with cash has been saved successfully !");
				}
				
				if(request.getParameter("depositPayment")!=null){
					BigDecimal deductedAmount = BigDecimal.valueOf(Double.parseDouble(request.getParameter("deductedAmount")));
					
					//update the patient account balance
					PatientAccount patientAccount = PatientAccountUtil.getPatientAccountByPatient(consommation.getBeneficiary().getPatient());
					patientAccount.setBalance(patientAccount.getBalance().subtract(deductedAmount));
					
					//create transaction
					Transaction transaction = PatientAccountUtil.createTransaction(deductedAmount.negate(), new Date(), new Date(), Context.getAuthenticatedUser(), patientAccount, "bill payment", Context.getAuthenticatedUser());
					
					bp.setAmountPaid(deductedAmount);
					DepositPayment dp = new DepositPayment(bp);

					setParams(dp, Context.getAuthenticatedUser(), false, new Date());
					dp.setTransaction(transaction);
					
					//create deposit payment
					dp =PatientBillUtil.createDepositPayment(dp);
					createPaidServiceBill(request, consommation, dp);
					request.getSession().setAttribute(
							WebConstants.OPENMRS_MSG_ATTR,
							"The Bill Payment with deposit has been saved successfully !");
					billPayment = dp;
				}

				return billPayment;
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
			String[] splittedParameterName = parameterName.split("-");
			String psbIdStr = splittedParameterName[2];			
			//String psbIdStr = request.getParameter(parameterName);
			Integer  patientServiceBillId = Integer.parseInt(psbIdStr);	
			PatientServiceBill psb  =ConsommationUtil.getPatientServiceBill(patientServiceBillId);	
			
			paidSb.setBillItem(psb);
			paidSb.setPaidQty(psb.getQuantity());
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
	public void setParams(BillPayment payment,User creator,Boolean voided,Date createdDate){
		payment.setCreator(Context.getAuthenticatedUser());
		payment.setVoided(false);
		payment.setCreatedDate(new Date());
	}

}
