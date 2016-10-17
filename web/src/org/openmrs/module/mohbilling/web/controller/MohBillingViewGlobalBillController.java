package org.openmrs.module.mohbilling.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.model.ServiceRevenue;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

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
		GlobalBill globalBill = null;
		Set<HopService> allHopServices = new HashSet<HopService>();
		
		if(globalBillStr!=null && !globalBillStr.equals("")){
//			log.info("wwwwwwwwwwwwwwwwaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa "+globalBillStr);
			globalBill=GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr));
			Set<PatientServiceBill> allItems = new HashSet<PatientServiceBill>();
			for (Consommation c : ConsommationUtil.getConsommationsByGlobalBill(globalBill)) {
				for (PatientServiceBill item : c.getBillItems()) {
					allItems.add(item);
					allHopServices.add(item.getHopService());
				}
			}
			List<ServiceRevenue> serviceRevenueList = new ArrayList<ServiceRevenue>();
			for (HopService hs : allHopServices) {
				serviceRevenueList.add(ReportsUtil.getServiceRevenues(allItems, hs));
			}
			
			mav.addObject("globalBill", globalBill);
			mav.addObject("serviceRevenueList", serviceRevenueList);
		}

		return mav;
	}

}
