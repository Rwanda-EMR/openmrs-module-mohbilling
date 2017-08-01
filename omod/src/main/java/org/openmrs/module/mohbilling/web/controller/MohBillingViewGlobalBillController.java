package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
		List<ServiceRevenue> serviceRevenueList = new ArrayList<ServiceRevenue>();
		
		if(globalBillStr!=null && !globalBillStr.equals("")){
			GlobalBill globalBill= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr));
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
			
			ServiceRevenue autreRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.AUTRES");
			if(autreRevenue!=null)
			serviceRevenueList.add(autreRevenue);
			
			mav.addObject("globalBill", globalBill);
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

		return mav;
	}

}
