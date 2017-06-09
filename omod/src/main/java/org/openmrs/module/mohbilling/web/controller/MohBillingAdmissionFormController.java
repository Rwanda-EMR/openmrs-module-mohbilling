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
import java.util.ArrayList;
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
				//if(ad.getDischargingDate()==null)
					globalBills.add(Context.getService(BillingService.class).getGlobalBillByAdmission(ad));
			}
			mav.addObject("globalBills", globalBills);
		}
		
	if(discharge==null)
	if (request.getParameter("save") != null && request.getParameter("save").equals("true")) {
		
	     ip = Context.getService(BillingService.class).getInsurancePolicy(Integer.valueOf(request.getParameter("insurancePolicyId")));
	     
		
		Admission admission = new Admission();
		admission.setAdmissionDate(new Date());
		admission.setInsurancePolicy(ip);
		//admission.setDischargingDate(new Date());
		Boolean isAdmitted = null;
		if (request.getParameterValues("isAdmitted")!= null) {
			String [] isAdmittedStr =request.getParameterValues("isAdmitted");
			isAdmitted=Boolean.parseBoolean(isAdmittedStr[0]);
			admission.setIsAdmitted(isAdmitted);
		}	
		admission.setCreator(Context.getAuthenticatedUser());
		admission.setCreatedDate(new Date());
	
		if(!isPatientAdmitted(ip)){
		Admission savedAdmission = AdmissionUtil.savePatientAdmission(admission);
		
		//create new Global bill
		gb =new GlobalBill();
		gb.setAdmission(savedAdmission);
		gb.setBillIdentifier(ip.getInsuranceCardNo()+savedAdmission.getAdmissionId());
		gb.setCreatedDate(new Date());
		gb.setCreator(Context.getAuthenticatedUser());
		
		gb =   GlobalBillUtil.saveGlobalBill(gb);
		request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
				"The patient admission has been saved succefully !");
		mav.addObject("globalBill", gb);
		}
		else{
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Not Saved..The patient is already admitted [Some Global Bills are not yet closed.]");
		}//
		
	}
	if(request.getParameter("discharge")!=null){
		discharge = request.getParameter("discharge");
		gb = GlobalBillUtil.getGlobalBill(Integer.valueOf(request.getParameter("globalBillId")));
		if(request.getParameter("edit")!=null){
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
		//log.info("uuuuuuuuuuuuuuuuuooooooooooooooooooooooooooooo "+ip.getAdmissions().size());
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
