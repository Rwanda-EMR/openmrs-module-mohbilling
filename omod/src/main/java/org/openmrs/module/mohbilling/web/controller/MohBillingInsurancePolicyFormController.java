/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author EMR@RBC
 * 
 *         This controller backs the
 *         /web/module/mohBillingInsurancePolicyForm.jsp page. This controller
 *         is tied to that jsp page in the
 *         /metadata/moduleApplicationContext.xml file
 */

public class MohBillingInsurancePolicyFormController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null
				&& request.getParameter("save").equals("true")) {

			boolean saved = handleSaveInsurancePolicy(request, mav);
			if (saved)
				return new ModelAndView(new RedirectView(
						"insurancePolicySearch.form"));

		} else {
			try {
				if (null != request.getParameter("patientId")) {

					InsurancePolicy ip = new InsurancePolicy();
					ip.setOwner(Context.getPatientService().getPatient(
							Integer.valueOf(request.getParameter("patientId"))));
					mav.addObject("insurancePolicy", ip);

					mav.addObject("beneficiaryId",
							request.getParameter("patientId"));
				} else if (null != request.getParameter("insurancePolicyId")) {
					// if an insurancePolicyId is specified, then edition of an
					// insurancePolicy is the only option possible
					InsurancePolicy ip = Context.getService(
							BillingService.class).getInsurancePolicy(
							Integer.valueOf(request
									.getParameter("insurancePolicyId")));

					mav.addObject("insurancePolicy", ip);
					mav.addObject("beneficiaryId", ip.getOwner().getPatientId());
				} else
					return new ModelAndView(new RedirectView(
							"insurancePolicySearch.form"));
			} catch (Exception e) {
				
				log.error(">>>MOH>>BILLING>> " + e.getMessage());
				e.printStackTrace();

				return new ModelAndView(new RedirectView(
						"insurancePolicySearch.form"));
			}
		}

		mav.addObject("insurances", InsuranceUtil.getInsurances(true));
		mav.addObject("thirdParties", InsurancePolicyUtil.getAllThirdParties());

		return mav;
	}

	/**
	 * @param request
	 * @param mav
	 * @return
	 */
	private boolean handleSaveInsurancePolicy(HttpServletRequest request,
			ModelAndView mav) {

// Start Edit insurance

		if(request.getParameter("insurancePolicyId") != null
				&& !request.getParameter("insurancePolicyId").equals("")){
			InsurancePolicy card = Context
					.getService(BillingService.class)
					.getInsurancePolicy(
							Integer.parseInt(request.getParameter("insurancePolicyId")));
			for (Beneficiary b:card.getBeneficiaries()){
				if(card.getOwner().getPatientId() == b.getPatient().getPatientId()) {
					b.setPolicyIdNumber(request.getParameter("insurancePolicyOwnerCardNumber"));
					b.setCreator(Context.getAuthenticatedUser());
					b.setCreatedDate(new Date());
					b.setOwnerName(request.getParameter("ownerName"));
					b.setOwnerCode(request.getParameter("ownerCode"));
					b.setLevel(Integer.parseInt(request.getParameter("level")));
					b.setCompany(request.getParameter("company"));
					card.addBeneficiary(b);
				}
			}
			if (request.getParameter("insurancePolicyOwnerCardNumber") != null
					&& !request.getParameter("insurancePolicyOwnerCardNumber")
					.equals("")) {

				card.setInsuranceCardNo(request
						.getParameter("insurancePolicyOwnerCardNumber"));
			}
try {
	if (request.getParameter("insurancePolicyCoverageStartDate") != null
			&& !request
			.getParameter("insurancePolicyCoverageStartDate")
			.equals("")) {

		card.setCoverageStartDate(Context.getDateFormat().parse(request.getParameter("insurancePolicyCoverageStartDate")));
	}

	if (request.getParameter("insurancePolicyExpirationDate") != null
			&& !request.getParameter("insurancePolicyExpirationDate")
			.equals("")) {

		card.setExpirationDate(Context.getDateFormat().parse(
				request.getParameter("insurancePolicyExpirationDate")));
	}
}catch (ParseException pex){
	pex.getMessage();
}
			try {
				if(!InsurancePolicyUtil.isInsurancePolicyExists(request.getParameter("insurancePolicyOwnerCardNumber"))==true){

			card.setCreator(Context.getAuthenticatedUser());
			card.setCreatedDate(new Date());
			InsurancePolicyUtil.createInsurancePolicy(card);
				}
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						"The insurance policy has been Edited successfully !");
				return true;
			}
			catch (Exception e) {

				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"Attention !!!!!!!!!...the insurance policy with card no: "+request.getParameter("insurancePolicyOwnerCardNumber")+" cannot be edited," +
								"because it has been used already");
				log.error(">>>>MOH>>BILLING>> " + e.getMessage());
				e.printStackTrace();

				return false;
			}
		}

/*end Edit insurance*/

		InsurancePolicy card = null;
		
		try {
			// insurancePolicy
/*			if (request.getParameter("cardId") != null
					&& !request.getParameter("cardId").equals("")) {

				card = Context
						.getService(BillingService.class)
						.getInsurancePolicy(
								Integer.parseInt(request.getParameter("cardId")));
				Beneficiary b = Context
						.getService(BillingService.class)
						.getBeneficiaryByPolicyNumber(card.getInsuranceCardNo());
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> insurance card number: " + request.getParameter("insurancePolicyOwnerCardNumber"));
				if(card.getOwner().getPatientId() == b.getPatient().getPatientId()) {
					b.setPolicyIdNumber(request.getParameter("insurancePolicyOwnerCardNumber"));
					card.addBeneficiary(b);
				}

			} else*/
				card = new InsurancePolicy();

			if (request.getParameter("thirdParty") != null
					&& !request.getParameter("thirdParty").equals("")
					&& !request.getParameter("thirdParty").equals("0")) {
				
				card.setThirdParty(InsurancePolicyUtil.getThirdParty(Integer
						.parseInt(request.getParameter("thirdParty"))));
			}

			if (request.getParameter("insurancePolicyCoverageStartDate") != null
					&& !request
							.getParameter("insurancePolicyCoverageStartDate")
							.equals("")) {

				card.setCoverageStartDate(Context
						.getDateFormat()
						.parse(request
								.getParameter("insurancePolicyCoverageStartDate")));
			}

			if (request.getParameter("insurancePolicyExpirationDate") != null
					&& !request.getParameter("insurancePolicyExpirationDate")
							.equals("")) {

				card.setExpirationDate(Context.getDateFormat().parse(
						request.getParameter("insurancePolicyExpirationDate")));
			}

			if (request.getParameter("insurancePolicyInsurance") != null
					&& !request.getParameter("insurancePolicyInsurance")
							.equals("")) {

				card.setInsurance(Context
						.getService(BillingService.class)
						.getInsurance(
								Integer.valueOf(request
										.getParameter("insurancePolicyInsurance"))));
			}

			if (request.getParameter("insurancePolicyOwnerCardNumber") != null
					&& !request.getParameter("insurancePolicyOwnerCardNumber")
							.equals("")) {

				card.setInsuranceCardNo(request
						.getParameter("insurancePolicyOwnerCardNumber"));
			}

			card.setOwner(Context.getPatientService().getPatient(
					Integer.valueOf(request
							.getParameter("insurancePolicyOwner"))));
			
			//owner update

			for (int i = 1; i < 11; i++) {
				if (request.getParameter("insurancePolicyBeneficiary_" + i) != null
						&& request
								.getParameter("insurancePolicyBeneficiary_" + i)
								.trim().compareTo("") != 0) {

					Beneficiary b = new Beneficiary();

					b.setPatient(Context.getPatientService().getPatient(
							Integer.valueOf(request
									.getParameter("insurancePolicyBeneficiary_"
											+ i))));
					b.setPolicyIdNumber(request
							.getParameter("insurancePolicyBeneficiaryCardNumber_"
									+ i));

					b.setCreatedDate(new Date());
					b.setCreator(Context.getAuthenticatedUser());
					b.setRetired(false);
					b.setOwnerName(request.getParameter("ownerName"));
					b.setOwnerCode(request.getParameter("ownerCode"));
					b.setLevel(Integer.parseInt(request.getParameter("level")));
					b.setCompany(request.getParameter("company"));
					card.addBeneficiary(b);

				}
			}
			card.setCreatedDate(new Date());
			card.setCreator(Context.getAuthenticatedUser());
			card.setRetired(false);			
            /*if the insurance policy already  exist,display the  message*/
			if (InsurancePolicyUtil.isInsurancePolicyExists(request.getParameter("insurancePolicyOwnerCardNumber"))==true) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
				"Attention !!!!!!!!!...the insurance policy with card no: "+request.getParameter("insurancePolicyOwnerCardNumber")+" cannot be created," +
						" it has been used already");
			}
			else {
				/*===================Creating Beneficiary ============================*/
				Beneficiary b = new Beneficiary();

				b.setCreatedDate(new Date());
				b.setCreator(Context.getAuthenticatedUser());
				b.setRetired(false);
				if(!(request.getParameter("ownerName").trim()).isEmpty() && !(request.getParameter("ownerCode").trim()).isEmpty())
				{
					b.setOwnerName(request.getParameter("ownerName"));
					b.setOwnerCode(request.getParameter("ownerCode"));
					b.setLevel(Integer.parseInt(request.getParameter("level")));
					b.setCompany(request.getParameter("company"));
				}else{
					b.setOwnerName(request.getParameter(" "));
					b.setOwnerCode(request.getParameter(" "));
					b.setLevel(0);
					b.setCompany(request.getParameter(" "));
				}
				InsurancePolicyUtil.createInsurancePolicy(card, b);

				DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

				Calendar cal = Calendar.getInstance();
				Date exp=Context.getDateFormat().parse(request.getParameter("insurancePolicyExpirationDate"));
				Date now=Context.getDateFormat().parse(sdf.format(cal.getTime()));

				if(exp.before(now)){
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"Attention!!!!!!!!! The insurance policy has been created successfully! But you selected past date");
				}else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,"The insurance policy has been created successfully !");
				}
			}
		} catch(ConstraintViolationException cve){
			
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance policy number already exists !");
			
			log.error(">>>>MOH>>BILLING>> " + cve.getMessage());
			cve.printStackTrace();
			
			return false;
			
		} catch (Exception e) {
			
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"The insurance policy has not been saved !");
			log.error(">>>>MOH>>BILLING>> " + e.getMessage());
			e.printStackTrace();

			return false;
		}

		return true;
	}
	private void handleRetireInsurancePolicy(InsurancePolicy ip) {
		if(ip!=null){
			ip.setRetired(true);
			ip.setRetiredBy(Context.getAuthenticatedUser());
			ip.setRetiredDate(new Date());
			ip.setRetireReason("void");
			InsurancePolicyUtil.createInsurancePolicy(ip);
		}
		
	}
}