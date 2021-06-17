package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MohBillingViewGlobalBillController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		String globalBillStr = request.getParameter("globalBillId");
		Integer globalBillId = Integer.valueOf(request.getParameter("globalBillId"));
		List<ServiceRevenue> serviceRevenueList = new ArrayList<ServiceRevenue>();
		Consommation consommation =null;

		if(globalBillStr!=null && !globalBillStr.equals("")){
			GlobalBill globalBill= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr));
			PatientIdentifier patientIdentifier= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr)).getAdmission().getInsurancePolicy().getOwner().getPatientIdentifier(3);

			List<Consommation> consommations = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
			List<PatientServiceBill> allItems = new ArrayList<PatientServiceBill>();
			for (Consommation c : consommations) {
				for (PatientServiceBill item : c.getBillItems()) {
					if (!item.getVoided()) {
						allItems.add(item);
					}
				}
			}
			List<HopService> revenuesCategories = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.REVENUE");
			for (HopService hopService : revenuesCategories) {
				if(ReportsUtil.getServiceRevenues(allItems, hopService)!=null)
					serviceRevenueList.add(ReportsUtil.getServiceRevenues(allItems, hopService));

			}
			ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.IMAGING");
			if(imagingRevenue!=null)
				serviceRevenueList.add(imagingRevenue);

			ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.ACTS");
			if(actsRevenue!=null)
				serviceRevenueList.add(actsRevenue);

			ServiceRevenue actsDCPRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.DCPACTS");
			if(actsRevenue!=null)
				serviceRevenueList.add(actsDCPRevenue);

			ServiceRevenue autreRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.AUTRES");
			if(autreRevenue!=null)
				serviceRevenueList.add(autreRevenue);

			mav.addObject("globalBill", globalBill);
			mav.addObject("patientIdentifier",patientIdentifier);
			request.getSession().setAttribute("globalBill" , globalBill);

		}
		mav.addObject("serviceRevenueList", serviceRevenueList);
		request.getSession().setAttribute("serviceRevenueList" , serviceRevenueList);

		if(request.getParameter("print")!=null){
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.valueOf(request.getParameter("globalBillId")));
			FileExporter exp = new FileExporter();
			List<ServiceRevenue> sr = (List<ServiceRevenue>) request.getSession().getAttribute("serviceRevenueList" );
			exp.printGlobalBill(request, response, gb,sr, gb.getBillIdentifier()+".pdf");
		}
		if(request.getParameter("revert_global_bill")!=null && Context.isAuthenticated()){
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.parseInt(request.getParameter("globalBillId")));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String closingDate = df.format(gb.getClosingDate());
			String currDate = df.format(new Date());
			if(closingDate.equals(currDate)==true) {
				gb.setClosed(false);
				gb.setClosedBy(null);
				gb.setClosingDate(null);
				gb.setClosingReason(null);
				gb.setEditedBy(Context.getAuthenticatedUser());
				gb.setEditingReason(request.getParameter("editGBill"));
				GlobalBillUtil.saveGlobalBill(gb);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,"Global Bill Reverted, You are allowed to change it");
				return new ModelAndView(new RedirectView("globalBill.list?insurancePolicyId=" + gb.getAdmission().getInsurancePolicy() +
						"&ipCardNumber=" + gb.getAdmission().getInsurancePolicy().getInsuranceCardNo()));
			}
			else {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"Only Globall Bill closed TODAY can be reverted ");
			}
		}
		return mav;
	}
}