package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.BillPaymentUtil;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceBillUtil;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientAccountUtil;
import org.openmrs.module.mohbilling.businesslogic.PatientBillUtil;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.CashPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.DepositPayment;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsuranceBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientAccount;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.model.Transaction;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rbcemr
 */
public class MohBillingPatientBillPaymentFormController extends
		ParameterizableViewController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	@RequestMapping(value = "module/mohbilling/patientBillPayment.form", method = RequestMethod.POST)
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
			if (request.getParameter("billTransferItems") != null ){
				payment = handleBillTransferItems(request);
				mav.addObject("payment",payment);
			}else {
				payment = handleSavePatientBillPayment(request);
				mav.addObject("payment", payment);
			}
		}
		if (request.getParameter("billTransfer") != null ){
			payment = handlebillTransfer(request);
			mav.addObject("payment",payment);
		}

		try{
			Consommation consommation = null;
			List<Consommation> consommations = null;

			consommation = Context.getService(BillingService.class).getConsommation(
					Integer.parseInt(request.getParameter("consommationId")));
			System.out.println("Allllllllll Itemsssssssssssss sizeeeeeeeeeeeee:"+consommation.getBillItems().size());

			consommations = ConsommationUtil.getConsommationsByBeneficiary(consommation.getBeneficiary());

			mav.addObject("consommation", consommation);
			mav.addObject("consommations", consommations);
			mav.addObject("beneficiary", consommation.getBeneficiary());


			InsurancePolicy ip = consommation.getBeneficiary().getInsurancePolicy();
			mav.addObject("insurancePolicy", ip);

			// check the validity of the insurancePolicy for today
			Date today = new Date();
			boolean startValid = ip.getCoverageStartDate().getTime() <= today.getTime();
			boolean expireValid = ip.getExpirationDate() == null || ip.getExpirationDate().getTime() <= today.getTime();
			mav.addObject("valid", startValid && expireValid);
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
			Consommation consommation = ConsommationUtil.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
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
	/*			billPayment.setDateReceived(Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));*/
				billPayment.setDateReceived(new Date());
				/*log.info("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH "+Context.getDateFormat().parse(
						request.getParameter("dateBillReceived")));*/
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

					cp = PatientBillUtil.createCashPayment(cp);
					billPayment =cp;
					createPaidServiceBill(request, consommation, cp);

	                System.out.println("From Paymenttttttttttttttttt1:"+pb.getAmountPaid());
					System.out.println("From patient billlllllllllll1:"+pb.getAmount());


					if (pb.getAmountPaid().doubleValue() == pb.getAmount().doubleValue()){
						pb.setPaymentConfirmed(true);
						pb.setPaymentConfirmedDate(new Date());
						pb.setPaymentConfirmedBy(Context.getAuthenticatedUser());
						System.out.println("Inside1");
						pb=PatientBillUtil.savePatientBill(pb);
					}



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

						dp = PatientBillUtil.createDepositPayment(dp);
						createPaidServiceBill(request, consommation, dp);

						billPayment = dp;

						System.out.println("From Paymenttttttttttttttttt2:"+consommation.getPatientBill().getAmountPaid());
						System.out.println("From patient billlllllllllll2:"+consommation.getPatientBill().getAmount());

						if (consommation.getPatientBill().getAmountPaid().doubleValue()==consommation.getPatientBill().getAmount().doubleValue()){
							pb.setPaymentConfirmed(true);
							pb.setPaymentConfirmedDate(new Date());
							pb.setPaymentConfirmedBy(Context.getAuthenticatedUser());
							pb=PatientBillUtil.savePatientBill(pb);
						}
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
			PatientServiceBill psb  = ConsommationUtil.getPatientServiceBill(patientServiceBillId);


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


	private BillPayment handlebillTransfer(HttpServletRequest request) {
		Consommation consommation = ConsommationUtil.getConsommation(Integer.parseInt(request.getParameter("consommationId")));
		PatientBill currentPb =consommation.getPatientBill();
		InsuranceBill currentIb=consommation.getInsuranceBill();
		Set<PatientServiceBill> servicesBill=consommation.getBillItems();

		BigDecimal totalAmount = new BigDecimal(0);
		BigDecimal totalMaximaTopay=new BigDecimal(0);

		for (PatientServiceBill  serviceBill : servicesBill) {

			/*BigDecimal qty = serviceBill.getQuantity();
			PatientServiceBill cpyPsb = new PatientServiceBill();
			cpyPsb.setUnitPrice(psb.getUnitPrice());
			qty = psb.getQuantity();
			cpyPsb.setQuantity(qty);
			cpyPsb.setCreator(Context.getAuthenticatedUser());
			cpyPsb.setCreatedDate(new Date());
			cpyPsb.setConsommation(cpyConsom);
			cpyPsb.setServiceDate(psb.getServiceDate());*/

			totalAmount = totalAmount.add(serviceBill.getQuantity().multiply(serviceBill.getUnitPrice()));
			totalMaximaTopay=totalMaximaTopay.add(serviceBill.getService().getMaximaToPay());
			//cpyConsom.addBillItem(cpyPsb);
		}
		InsurancePolicy newInsurancePolicy=InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(request.getParameter("newCardNumber")).getInsurancePolicy();

		if (newInsurancePolicy!=null) {
			PatientBill pb = PatientBillUtil.createPatientBill(totalAmount, newInsurancePolicy);
			InsuranceBill ib = InsuranceBillUtil.createInsuranceBill(newInsurancePolicy.getInsurance(), totalAmount);

			//ThirdPartyBill thirdPartyBill =	ThirdPartyBillUtil.createThirdPartyBill(existingConsom.getBeneficiary().getInsurancePolicy(), totalAmount);

			GlobalBill gb = Context.getService(BillingService.class).getOpenGlobalBillByInsuranceCardNo(newInsurancePolicy.getInsuranceCardNo());
			if (gb != null) {


				BigDecimal globalAmount = gb.getGlobalAmount().add(totalMaximaTopay);
				gb.setGlobalAmount(globalAmount);
				gb = GlobalBillUtil.saveGlobalBill(gb);

				GlobalBill oldGb = consommation.getGlobalBill();
				BigDecimal oldGlobalAmount = oldGb.getGlobalAmount().subtract(totalMaximaTopay);
				oldGb = GlobalBillUtil.saveGlobalBill(oldGb);


				consommation.setBeneficiary(InsurancePolicyUtil.getBeneficiaryByPolicyIdNo(request.getParameter("newCardNumber")));
				consommation.setGlobalBill(gb);
				consommation.setPatientBill(pb);
				consommation.setInsuranceBill(ib);
				//consommation.setThirdPartyBill(thirdPartyBill);

				Consommation saveConsommation = ConsommationUtil.saveConsommation(consommation);
			} else {
				// alert on no Admission opened
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"No admission opened by using "+request.getParameter("newCardNumber"));
			}
		}else{
			// alert on ipCardNumber invalid
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					request.getParameter("newCardNumber")+" is invalid, Please it check again");
		}
		BillPayment payment = null;
		return payment;
	}

	//=====================================================
	private BillPayment handleBillTransferItems(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		int lastNoneCons=0;
		for (String  parameterName : parameterMap.keySet()) {

			if (!parameterName.startsWith("item-")) {
				continue;
			}
			PaidServiceBill paidSb = new PaidServiceBill();
			String[] splittedParameterName = parameterName.split("-");
			String psbIdStr = splittedParameterName[2];
			//String psbIdStr = request.getParameter(parameterName);
			Integer  patientServiceBillId = Integer.parseInt(psbIdStr);

			if (request.getParameter("privateCardNumber") !="" && request.getParameter("privateCardNumber") !=null && Context.getService(BillingService.class).getInsurancePolicyByCardNo(request.getParameter("privateCardNumber")).getInsurance().getCategory().equals("NONE")) {

				InsurancePolicy insurancePolicy=Context.getService(BillingService.class).getInsurancePolicyByCardNo(request.getParameter("privateCardNumber"));
				List<GlobalBill> globalBills=GlobalBillUtil.getGlobalBillsByInsurancePolicy(insurancePolicy);
				List<Integer> globalBillsInt=new ArrayList<Integer>();
				for (GlobalBill gb:globalBills) {
					globalBillsInt.add(gb.getGlobalBillId());
				}
				List<Integer> sortedGB = globalBillsInt.stream().sorted().collect(Collectors.toList());
				List<Consommation> consommations= ConsommationUtil.getConsommationsByGlobalBill(GlobalBillUtil.getGlobalBill(sortedGB.get(sortedGB.size()-1)));
				List<Integer> consInt=new ArrayList<Integer>();
				for (Consommation cs:consommations) {
					consInt.add(cs.getConsommationId());
				}
				List<Integer> sortedconsInt = consInt.stream().sorted().collect(Collectors.toList());
				//System.out.println("Last Consssssssssssssssssssssssssssssssssssssssss: "+sortedconsInt.get(sortedconsInt.size()-1));
				lastNoneCons=sortedconsInt.get(sortedconsInt.size()-1);
					}
			PatientServiceBill psb  = ConsommationUtil.getPatientServiceBill(patientServiceBillId);
			Consommation oldCons=psb.getConsommation();
			System.out.println("Consommantion Olddddddddddddddddddddddddddd: "+oldCons.getConsommationId());
			PatientServiceBill psbOld=psb;
			psb.getService().getFacilityServicePrice().getName();
			//System.out.println("checkedddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd: "+psb.getService().getFacilityServicePrice().getName() );
			Consommation noneCons=ConsommationUtil.getConsommation(lastNoneCons);
			System.out.println("Consommantion Newwwwwwwwwwwwwwwwwwwwwwwwwwwwwww: "+noneCons.getConsommationId());
			psb.setConsommation(ConsommationUtil.getConsommation(lastNoneCons));
			PatientBill nonePAtientBill = noneCons.getPatientBill();
			System.out.println("Before Update Patient Bill: "+nonePAtientBill.getAmount());
			BigDecimal psbAmount=psb.getQuantity().multiply(psb.getService().getMaximaToPay());
			nonePAtientBill.setAmount(nonePAtientBill.getAmount().add(psbAmount));
			System.out.println("After Update Patient Bill: "+nonePAtientBill.getAmount());
			PatientBillUtil.savePatientBill(nonePAtientBill);
			System.out.println("After save Patient Bill: "+nonePAtientBill.getAmount()+" With ID: "+ nonePAtientBill.getPatientBillId());
			noneCons.setPatientBill(nonePAtientBill);
			noneCons.addBillItem(psb);
			ConsommationUtil.saveConsommation(noneCons);
			System.out.println("After save Consommation: "+noneCons.getPatientBill().getAmount()+" With ID: "+ noneCons.getPatientBill().getPatientBillId());
			GlobalBill globalBillNone=noneCons.getGlobalBill();
			globalBillNone.setGlobalAmount(globalBillNone.getGlobalAmount().add(psbAmount));
			globalBillNone=GlobalBillUtil.saveGlobalBill(globalBillNone);




			//PatientServiceBill psbOld  = ConsommationUtil.getPatientServiceBill(patientServiceBillId);
			//Consommation oldCons=ConsommationUtil.getConsommation(psbOld.getConsommation().getConsommationId());
			PatientBill oldPAtientBill = oldCons.getPatientBill();
			InsuranceBill oldInsuranceBill=oldCons.getInsuranceBill();


			ThirdParty thirdParty =oldCons.getBeneficiary().getInsurancePolicy().getThirdParty();
			InsuranceRate validRate=oldCons.getBeneficiary().getInsurancePolicy().getInsurance().getRateOnDate(new Date());
			Float rateToPay=null;		//rate based on which the amount is calculated
			Float insRateToPay=null;
			if(thirdParty == null)
				rateToPay = 100-validRate.getRate();
			else
				rateToPay = (100-validRate.getRate())-thirdParty.getRate();

			BigDecimal amountToReduceOnPatientBill=psbOld.getQuantity().multiply(psbOld.getService().getMaximaToPay()).multiply(BigDecimal.valueOf(rateToPay/100));

			oldPAtientBill.setAmount(oldPAtientBill.getAmount().subtract(amountToReduceOnPatientBill));
			oldCons.setPatientBill(oldPAtientBill);
			insRateToPay=100-rateToPay;
			BigDecimal amountToReduceOnInsuranceBill=psbOld.getQuantity().multiply(psbOld.getService().getMaximaToPay().multiply(BigDecimal.valueOf(insRateToPay/100)));
			oldInsuranceBill.setAmount(oldInsuranceBill.getAmount().subtract(amountToReduceOnInsuranceBill));

			oldCons.setPatientBill(oldPAtientBill);
			oldCons.setInsuranceBill(oldInsuranceBill);
			InsuranceBillUtil.saveInsuranceBill(oldInsuranceBill);
			PatientBillUtil.savePatientBill(oldPAtientBill);
			ConsommationUtil.saveConsommation(oldCons);

			GlobalBill globalBillOld=oldCons.getGlobalBill();
			globalBillOld.setGlobalAmount(globalBillOld.getGlobalAmount().subtract(amountToReduceOnPatientBill));
			globalBillOld.setGlobalAmount(globalBillOld.getGlobalAmount().subtract(amountToReduceOnInsuranceBill));
			globalBillOld=GlobalBillUtil.saveGlobalBill(globalBillOld);


			System.out.println("Consommantion Old after Save: "+oldCons.getConsommationId());
			System.out.println("Patient Bill Old after Save: "+oldPAtientBill.getPatientBillId()+" "+oldPAtientBill.getAmount());
			System.out.println("Patient Bill Old after Save: "+oldInsuranceBill.getInsuranceBillId()+" "+oldInsuranceBill.getAmount());


		}
		if(lastNoneCons > 0){
			request.getSession().setAttribute(
					WebConstants.OPENMRS_MSG_ATTR,
					"Transfer of item(s) has been saved successfully !");
		}else {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Transfer of item(s) has been failed! Check if you selected at least one item. ");
		}

		BillPayment payment = null;
		return payment;
	}
	//===================================================

}