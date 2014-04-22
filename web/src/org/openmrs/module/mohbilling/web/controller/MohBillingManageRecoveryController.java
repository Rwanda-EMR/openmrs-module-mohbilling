package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.businesslogic.RecoveryUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.model.RecoveryStatus;
import org.openmrs.module.mohbilling.model.ThirdParty;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

public class MohBillingManageRecoveryController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	protected SessionFactory sessionFactory;

	/**
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		mav.addObject("allInsurances", InsuranceUtil.getInsurances(true));
		mav.addObject("allThirdParties",
				InsurancePolicyUtil.getAllThirdParties());

		if (request.getParameter("recordInfo") != null) {

			if (handleSaveRecoveryProcess(request, mav))

				return new ModelAndView(new RedirectView("recovery.list"));
			else
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR,
						"The Recover has not been saved !");
		}

		// handleRecoveryByRegis(request, mav); FROM REGIS...

		return mav;
	}

	/**
	 * Handles Recovery Process record using HttpServletRequest and ModelAndView
	 * 
	 * @param request
	 *            the HttpServletRequest
	 * @param mav
	 *            the ModelAndView
	 * @return true when saved successfully, false otherwise
	 */
	private boolean handleSaveRecoveryProcess(HttpServletRequest request,
			ModelAndView mav) {

		Recovery recovery = null;

		if (request.getParameter("recoveryId") != null
				&& !request.getParameter("recoveryId").equals("")) {

			recovery = RecoveryUtil.getRecovery(Integer.parseInt(request
					.getParameter("recoveryId")));

			try {

				if (request.getParameter("submissionDate") != null
						&& !request.getParameter("submissionDate").equals("")) {

					recovery.setSubmissionDate(OpenmrsUtil.getDateFormat().parse(
							request.getParameter("submissionDate")));
					
					recovery.setStatus(RecoveryStatus.SUBMITTED.getDescription());
					recovery.setChangedBy(Context.getAuthenticatedUser());
				}

				if (request.getParameter("verificationDate") != null
						&& !request.getParameter("verificationDate").equals("")) {

					recovery.setVerificationDate(OpenmrsUtil.getDateFormat().parse(
							request.getParameter("verificationDate")));
					
					recovery.setStatus(RecoveryStatus.VERIFIED.getDescription());
					recovery.setChangedBy(Context.getAuthenticatedUser());
				}

				if (request.getParameter("paidAmount") != null
						&& !request.getParameter("paidAmount").equals("")) {

					recovery.setPaidAmount(new BigDecimal(Float
							.parseFloat(request.getParameter("paidAmount"))));
				}

				if (request.getParameter("paymentDate") != null
						&& !request.getParameter("paymentDate").equals("")) {
					
					recovery.setPaymentDate(OpenmrsUtil.getDateFormat().parse(
							request.getParameter("paymentDate")));
					
					RecoveryUtil.setPaymentStatus(recovery);
				}

				if (request.getParameter("partlyPayReason") != null
						&& !request.getParameter("partlyPayReason").equals("")) {

					recovery.setPartlyPayReason(request
							.getParameter("partlyPayReason"));
				}

				if (request.getParameter("noPaymentReason") != null
						&& !request.getParameter("noPaymentReason").equals("")) {

					recovery.setNoPaymentReason(request
							.getParameter("noPaymentReason"));
				}

				if (request.getParameter("observation") != null
						&& !request.getParameter("observation").equals("")) {

					recovery.setObservation(request.getParameter("observation"));
				}
			} catch (ParseException e) {
				log.info("ERROR WHILE PARSING DATE: \n\n" + e.getMessage());
			}

		} else
			recovery = new Recovery();

		if (request.getParameter("startDate") != null
				&& request.getParameter("endDate") != null)
			if (!request.getParameter("endDate").equals("")
					&& !request.getParameter("startDate").equals("")) {

				Date startPeriod = null, endPeriod = null;
				Insurance insurance = null;
				ThirdParty thirdParty = null;

				try {

					startPeriod = OpenmrsUtil.getDateFormat().parse(
							request.getParameter("startDate"));
					endPeriod = OpenmrsUtil.getDateFormat().parse(
							request.getParameter("endDate"));

					recovery.setStartPeriod(startPeriod);
					recovery.setEndPeriod(endPeriod);

					if (request.getParameter("insurance") != null)
						if (!request.getParameter("insurance").equals("")) {

							insurance = InsuranceUtil
									.getInsurance(Integer.parseInt(request
											.getParameter("insurance")));

							recovery.setInsuranceId(insurance);
							recovery.setDueAmount(RecoveryUtil
									.getInsuranceDueAmount(startPeriod,
											endPeriod, insurance));
						}

					if (request.getParameter("thirdParty") != null)
						if (!request.getParameter("thirdParty").equals("")) {

							thirdParty = InsurancePolicyUtil
									.getThirdParty(Integer.parseInt(request
											.getParameter("thirdParty")));

							recovery.setThirdParty(thirdParty);
							recovery.setDueAmount(RecoveryUtil
									.getThirdPartyDueAmount(startPeriod,
											endPeriod, thirdParty));
						}

					return (RecoveryUtil.createRecovery(recovery) != null) ? true
							: false;

				} catch (ParseException e) {
					log.info("ERROR WHILE PARSING DATE: \n\n" + e.getMessage());
				}
			}

		return false;
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param request
	 * @param mav
	 * @param billingService
	 * @throws ParseException
	 * @throws NumberFormatException
	 * @throws DAOException
	 */
	@SuppressWarnings("unused")
	private void handleRecoveryByRegis(HttpServletRequest request,
			ModelAndView mav) throws ParseException, NumberFormatException,
			DAOException {
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Date startDate = null;
		Date endDate = null;
		Insurance insurance = null;
		String insuranceStr = request.getParameter("insurance"), startDateStr = request
				.getParameter("startDate"), endDateStr = request
				.getParameter("endDate");
		Integer insuranceIdInt = null;
		if (startDateStr != null && !startDateStr.equals("")) {
			startDate = (Date) formatter.parse(startDateStr);
		}

		if (endDateStr != null && !endDateStr.equals("")) {
			endDate = (Date) formatter.parse(endDateStr);

		}

		if (insuranceStr != null && !insuranceStr.equals("")) {
			insuranceIdInt = Integer.parseInt(insuranceStr);
			insurance = InsuranceUtil.getInsurance(insuranceIdInt);
		}

		// getting parameters after payment is done

		String amountPaid = request.getParameter("amountPaid");
		String datePayment = request.getParameter("datePayment");

		Date datePaymentFormatted = null;
		if (datePayment != null && !datePayment.equals("")) {
			datePaymentFormatted = (Date) formatter.parse(datePayment);
		}
		Date startDateNewFormatted = null;
		String startDateNew = request.getParameter("startDateNew");
		if (startDateNew != null && !startDateNew.equals("")) {
			startDateNewFormatted = (Date) formatter.parse(startDateNew);
		}
		Date endDateNewFormatted = null;
		String endDateNew = request.getParameter("endDateNew");
		if (endDateNew != null && !endDateNew.equals("")) {
			endDateNewFormatted = (Date) formatter.parse(endDateNew);
		}
		String insuranceId = request.getParameter("insuranceId");
		if (amountPaid != null) {

			Insurance insuranceToSave = new Insurance();
			insuranceToSave.setInsuranceId(Integer.parseInt(insuranceId));

			Recovery recoveryObj = new Recovery();

			recoveryObj.setInsuranceId(insuranceToSave);

			recoveryObj.setStartPeriod(startDateNewFormatted);
			recoveryObj.setEndPeriod(endDateNewFormatted);
			if (amountPaid != null && !amountPaid.equals(""))
				recoveryObj.setPaidAmount(new BigDecimal(Float
						.parseFloat(amountPaid)));
			recoveryObj.setPaymentDate(datePaymentFormatted);
			recoveryObj.setCreator(Context.getAuthenticatedUser());
			recoveryObj.setCreatedDate(new Date());
			recoveryObj.setRetired(false);
			if (recoveryObj != null && amountPaid != null
					&& !amountPaid.equals("")) {
				BillingService service = Context
						.getService(BillingService.class);

				service.saveRecovery(recoveryObj);

				mav.addObject("amountPaid", Float.parseFloat(amountPaid));
				mav.addObject("startDateNew", startDateNew);
				mav.addObject("endDateNew", endDateNew);

				mav.addObject("msg", "Saved!!!");
			}

		}

		if (insurance != null)
			mav.addObject("insuranceId", insurance.getInsuranceId());
		mav.addObject("insurance", insurance);
		mav.addObject("startDate", startDateStr);
		mav.addObject("endDate", endDateStr);

		// if(insurance != null && startDateNewFormatted != null &&
		// endDateNewFormatted != null){
		// log.info(" wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww insurance "+insurance+" aaaaaaaaaaaaaaaaaaa  the first aaaaaaaaaaaaaa startDate"+startDateNewFormatted+" tttttttttttttttttttttttttttt tttttttttendDate"+endDateNewFormatted);

		// billingService.getPaidAmountPerInsuranceAndPeriod(insurance,
		// startDateNewFormatted, endDateNewFormatted));
		// }
		if (insuranceStr != null && !insuranceStr.equals(""))
			mav.addObject("insuranceName", insurance.getName());
		if (insurance != null && startDate != null && endDate != null) {
			mav.addObject("amount", ReportsUtil.getMonthlyInsuranceDueAmount(
					insurance, startDate, endDate, false));
			mav.addObject("AlreadyPaidAmount", RecoveryUtil
					.getInsuranceDueAmount(startDate, endDate, insurance));
		}

		mav.addObject("amountPaid", amountPaid);
		mav.addObject("datePayment", datePayment);
	}

}
