package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openmrs.module.mohbilling.businesslogic.PaymentRefundUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.CashPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.DepositPayment;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PaidServiceBillRefund;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.PaymentRefund;
import org.openmrs.module.mohbilling.model.Transaction;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

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

/*		if(request.getParameter("refundId")!=null){
			PaymentRefund refund = PaymentRefundUtil.getRefundById(Integer.valueOf(request.getParameter("refundId")));
			//BillPaymentUtil.updatePaymentAfterRefund(request, refund);
		}*/


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

			//edit existing payment
			if(request.getParameter("newAmount")!=null && !request.getParameter("newAmount").equals("")){

				billPayment = BillPaymentUtil.getBillPaymentById(Integer.valueOf(request.getParameter("paymentId")));
				BigDecimal newAmount  = new BigDecimal(Double.valueOf(request.getParameter("newAmount")));
				billPayment.setVoided(true);
				billPayment.setVoidedBy(Context.getAuthenticatedUser());
				billPayment.setVoidedDate(new Date());
				billPayment.setVoidReason("Typing Error");
				Context.getService(BillingService.class).saveBillPayment(billPayment);

				BillPayment cpyPay = new BillPayment();
				cpyPay.setCollector(billPayment.getCollector());
				cpyPay.setDateReceived(billPayment.getDateReceived());
				cpyPay.setPatientBill(billPayment.getPatientBill());
				cpyPay.setCreatedDate(billPayment.getCreatedDate());
				cpyPay.setCreator(billPayment.getCreator());
				cpyPay.setAmountPaid(newAmount);
				Context.getService(BillingService.class).saveBillPayment(cpyPay);

				List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(billPayment);

				//recreate services which has been paid
				for (PaidServiceBill oldPaidSb : paidItems) {
					PaidServiceBill newPaidService = new PaidServiceBill();
					newPaidService.setBillItem(oldPaidSb.getBillItem());
					newPaidService.setPaidQty(oldPaidSb.getPaidQty());
					newPaidService.setBillPayment(cpyPay);
					newPaidService.setCreator(oldPaidSb.getCreator());
					newPaidService.setCreatedDate(oldPaidSb.getCreatedDate());
					newPaidService.setVoided(false);
					BillPaymentUtil.createPaidServiceBill(newPaidService);
				}

				//void services which has been paid before
				for (PaidServiceBill oldPaidSb : paidItems) {
					oldPaidSb.setVoided(true);
					oldPaidSb.setVoidedBy(Context.getAuthenticatedUser());
					oldPaidSb.setVoidedDate(new Date());
					oldPaidSb.setVoidReason("Typing Error");
					BillPaymentUtil.createPaidServiceBill(oldPaidSb);
				}
			}


			/**
			 * We need to add both Patient Due amount and amount paid by
			 * third part
			 */

			else{
				billPayment=new BillPayment();
				billPayment.setCollector(Context.getAuthenticatedUser());
				billPayment.setDateReceived(Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));
				billPayment.setPatientBill(pb);
				billPayment.setCreatedDate(new Date());
				billPayment.setCreator(Context.getAuthenticatedUser());
				//bp = PatientBillUtil.createBillPayment(bp);

				//mark as paid all  selected items for payment purpose

				if(request.getParameter("cashPayment")!=null){
					billPayment.setAmountPaid(BigDecimal.valueOf(Double.parseDouble(request
							.getParameter("receivedCash"))));
					//create cashPayment
					CashPayment cp =new CashPayment(billPayment);
					setParams(cp, Context.getAuthenticatedUser(), false, new Date());

					cp =PatientBillUtil.createCashPayment(cp);
					billPayment =cp;
					createPaidServiceBill(request, consommation, cp);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
							"The Bill Payment with cash has been saved successfully !");
				}

				if(request.getParameter("depositPayment")!=null){
					BigDecimal deductedAmount = BigDecimal.valueOf(Double.parseDouble(request.getParameter("deductedAmount")));
					PatientAccount patientAccount = PatientAccountUtil.getPatientAccountByPatient(consommation.getBeneficiary().getPatient());
					//create deposit payment
					if(deductedAmount.compareTo(patientAccount.getBalance())==-1){
						//update the patient account balance
						patientAccount.setBalance(patientAccount.getBalance().subtract(deductedAmount));

						//create transaction
						Transaction transaction = PatientAccountUtil.createTransaction(deductedAmount.negate(), new Date(), new Date(), Context.getAuthenticatedUser(), patientAccount, "Bill Payment", Context.getAuthenticatedUser());

						billPayment.setAmountPaid(deductedAmount);
						DepositPayment dp = new DepositPayment(billPayment);

						setParams(dp, Context.getAuthenticatedUser(), false, new Date());
						dp.setTransaction(transaction);

						dp =PatientBillUtil.createDepositPayment(dp);
						createPaidServiceBill(request, consommation, dp);

						billPayment = dp;

						request.getSession().setAttribute(
								WebConstants.OPENMRS_MSG_ATTR,
								"The Bill Payment with deposit has been saved successfully !");
					}
					else
						request.getSession().setAttribute(
								WebConstants.OPENMRS_ERROR_ATTR,
								"The Bill Payment is not saved. No sufficient balance !");
				}
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

			BigDecimal paidQuantity = new BigDecimal(Double.valueOf(request.getParameter("paidQty_"+psb.getPatientServiceBillId())));
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
	public void setParams(BillPayment payment,User creator,Boolean voided,Date createdDate){
		payment.setCreator(Context.getAuthenticatedUser());
		payment.setVoided(false);
		payment.setCreatedDate(new Date());
	}

}