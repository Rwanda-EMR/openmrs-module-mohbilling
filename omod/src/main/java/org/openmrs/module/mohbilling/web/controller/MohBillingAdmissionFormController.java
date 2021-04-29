package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.AdmissionUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.InsurancePolicy;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MohBillingAdmissionFormController extends
		ParameterizableViewController {

	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {


		ModelAndView mav = new ModelAndView();
		InsurancePolicy ip =null;
		String discharge = request.getParameter("discharge");
		GlobalBill gb =null;
		if(request.getParameter("insurancePolicyId")!=null){
			ip= Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));
			List<Admission> admissions =  AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);
			List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
			for (Admission ad : admissions) {
				globalBills.add(Context.getService(BillingService.class).getGlobalBillByAdmission(ad));
			}
			mav.addObject("globalBills", globalBills);
		}

		if(discharge==null) {
			if (request.getParameter("save") != null && request.getParameter("save").equals("true")) {

				ip = Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));

				Admission admission = new Admission();
				admission.setAdmissionDate(new Date());
				admission.setInsurancePolicy(ip);
				Boolean isAdmitted = null;
				if (request.getParameterValues("isAdmitted") != null) {
					String[] isAdmittedStr = request.getParameterValues("isAdmitted");
					isAdmitted = Boolean.parseBoolean(isAdmittedStr[0]);
					admission.setIsAdmitted(isAdmitted);
				}
				admission.setCreator(Context.getAuthenticatedUser());
				admission.setCreatedDate(new Date());
				admission.setDiseaseType(request.getParameter("diseaseType"));
				if (ip.getInsurance().getCategory().equals("MUTUELLE")) {
					admission.setAdmissionType(1);
				} else {
					admission.setAdmissionType(Integer.parseInt(request.getParameter("admissionType")));
				}

				DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				Date now = sdf.parse(sdf.format(cal.getTime()));
				Date exp = ip.getExpirationDate();

				Long diffPeriod = exp.getTime() - now.getTime();

				String diseaseType = request.getParameter("diseaseType");
				//String admissionType = request.getParameter("admissionType");
				int admissionType=0;
				if (!ip.getInsurance().getCategory().equals("MUTUELLE")) {
					admissionType = Integer.parseInt(request.getParameter("admissionType"));
				}
				else {
					admissionType = 1;
				}


				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"+admissionType);
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@CBHICBHICBHICBHI");

				if (admissionType==1 || ip.getInsurance().getCategory().equals("MUTUELLE")) {
					if (!isPatientAdmitted(ip) && diffPeriod >= 0 && diseaseType != null && !diseaseType.equals("")) {
						//create patient admission
						Admission savedAdmission = AdmissionUtil.savePatientAdmission(admission);
						//create new Global bill
						gb = new GlobalBill();
						gb.setAdmission(savedAdmission);
						gb.setBillIdentifier(ip.getInsuranceCardNo() + savedAdmission.getAdmissionId());
						gb.setCreatedDate(new Date());
						gb.setCreator(Context.getAuthenticatedUser());
						gb.setInsurance((Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")))).getInsurance());

						gb = GlobalBillUtil.saveGlobalBill(gb);
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
								"The patient admission has been saved succefully !");
						if (request.getParameter("insurancePolicyId") != null) {
							ip = Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));
							List<Admission> admissions = AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);
							List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
							for (Admission ad : admissions) {
								globalBills.add(Context.getService(BillingService.class).getGlobalBillByAdmission(ad));
							}
							mav.addObject("globalBills", globalBills);
						}

						mav.addObject("globalBill", gb);
					} else if (diffPeriod < 0) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"The insurance is Expired. Reception desk can help this patient");
					} else if (diseaseType == null || diseaseType.equals("")) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"Disease Type is required");
					} else {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"Not Saved..The patient is already admitted for Ordinary Clinic Or Dual Clinic!!! [In fact, Global Bill is not yet closed.]");
					}
				}
				if (admissionType==2){
					if (!isPatientAdmitted(ip) && diffPeriod >= 0 && diseaseType != null && !diseaseType.equals("") &&
							Context.getAuthenticatedUser().hasRole("DCP",true)) {
						//create patient admission
						Admission savedAdmission = AdmissionUtil.savePatientAdmission(admission);
						//create new Global bill
						gb = new GlobalBill();
						gb.setAdmission(savedAdmission);
						gb.setBillIdentifier(ip.getInsuranceCardNo() + savedAdmission.getAdmissionId());
						gb.setCreatedDate(new Date());
						gb.setCreator(Context.getAuthenticatedUser());
						gb.setInsurance((Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")))).getInsurance());

						gb = GlobalBillUtil.saveGlobalBill(gb);
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
								"The patient admission has been saved succefully !");
						if (request.getParameter("insurancePolicyId") != null) {
							ip = Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));
							List<Admission> admissions = AdmissionUtil.getPatientAdmissionsByInsurancePolicy(ip);
							List<GlobalBill> globalBills = new ArrayList<GlobalBill>();
							for (Admission ad : admissions) {
								globalBills.add(Context.getService(BillingService.class).getGlobalBillByAdmission(ad));
							}
							mav.addObject("globalBills", globalBills);
						}

						mav.addObject("globalBill", gb);
					} else if (diffPeriod < 0) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"The insurance is Expired. Reception desk can help this patient");
					} else if (diseaseType == null || diseaseType.equals("")) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"Disease Type is required");
					} else if (!Context.getAuthenticatedUser().hasRole("DCP",true)) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"You should have DCP role to initiate this Admission....Contact System Admin");
					} else {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"Not Saved..The patient is already admitted for Ordinary Clinic Or Dual Clinic!!! [In fact, Global Bill is not yet closed.]");
					}
				}
			}
		}
		if(request.getParameter("discharge")!=null && Context.isAuthenticated()){

			discharge = request.getParameter("discharge");
			gb = GlobalBillUtil.getGlobalBill(Integer.valueOf(request.getParameter("globalBillId")));
			if(request.getParameter("edit")!=null){
				if(gb.getAdmission().getInsurancePolicy().getInsurance()==null) {
					gb.setInsurance(gb.getAdmission().getInsurancePolicy().getInsurance());
				}
				gb.setAdmission(gb.getAdmission());
				gb.setBillIdentifier(gb.getBillIdentifier());
				gb.setGlobalAmount(gb.getGlobalAmount());
				gb.setCreator(gb.getCreator());
				gb.setCreatedDate(gb.getCreatedDate());
				gb.setClosingDate(new Date());
				gb.setClosedBy(Context.getAuthenticatedUser());
				gb.setClosed(true);
			}
			mav.addObject("globalBill", gb);
		}
		ip = Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));

		mav.addObject("discharge", discharge);
		mav.addObject("insurancePolicy", ip);
		if(ip.getAdmissions()!=null)
			mav.addObject("admissions", ip.getAdmissions());
		mav.setViewName(getViewName());
		return mav;
	}

	public Boolean isPatientAdmitted(InsurancePolicy ip){
		Boolean found=false;
		List<Admission> admissionsList = Context.getService(BillingService.class).getAdmissionsListByInsurancePolicy(ip);
		if(Context.getService(BillingService.class).getAdmissionsListByInsurancePolicy(ip)!=null)
			for (Admission ad : admissionsList) {
				if(GlobalBillUtil.getGlobalBillByAdmission(ad)!=null&& GlobalBillUtil.getGlobalBillByAdmission(ad).getClosingDate()==null){
					found=true;
					break;
				}
			}
		return found;
	}

}