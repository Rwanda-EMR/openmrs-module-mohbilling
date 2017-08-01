package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

public class MohBillServiceCategoryController extends
		ParameterizableViewController {
	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#
	 * handleRequestInternal(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		
		ModelAndView mav = new ModelAndView();
		BillingService billingSvce = Context.getService(BillingService.class);
		List<HopService> services = HopServiceUtil.getAllHospitalServices();
		Department department = null;
		String departmentId=null;
		if(request.getParameter("departmentId")!=null&&!request.getParameter("departmentId").equals("")){
			departmentId=request.getParameter("departmentId");
			department = DepartementUtil.getDepartement(Integer.valueOf(departmentId));
		}
		HopService service =new HopService();
		if((request.getParameter("save")!=null)&& !request.getParameter("save").equals("")){	
			String serviceIdStr = request.getParameter("serviceId");
			service = HopServiceUtil.getHopServiceById(Integer.valueOf(serviceIdStr));
			 
		    //Iterate over each insurance  and create service category		    
			for (Insurance insurance : InsuranceUtil.getAllInsurances()) {
				ServiceCategory existingSc = billingSvce.getServiceCategoryByName(service.getName(), insurance);
				
				ServiceCategory sc = new ServiceCategory();
				
				sc.setName(service.getName());	
		
		    	sc.setDescription(service.getDescription());

				sc.setRetired(false);
				sc.setCreatedDate(new Date());
				sc.setCreator(Context.getAuthenticatedUser());
				sc.setDepartment(department);
				sc.setHopService(service);			
			    insurance.addServiceCategory(sc);			    
			    billingSvce.saveInsurance(insurance);
				
			 		//Get 
			
				List<BillableService> billableServices = billingSvce.getBillableServiceByCategory(existingSc);
				
				
				for (BillableService bs : billableServices) {
					if(bs.getInsurance()==insurance){
					BillableService bsCopy = new BillableService();
					bsCopy.setFacilityServicePrice(bs.getFacilityServicePrice());
					bsCopy.setInsurance(insurance);
					bsCopy.setServiceCategory(sc);
					bsCopy.setStartDate(bs.getStartDate());
					bsCopy.setMaximaToPay(bs.getMaximaToPay());
					bsCopy.setCreatedDate(new Date());
					bsCopy.setCreator(Context.getAuthenticatedUser());
					bsCopy.setRetired(false);
					
					InsuranceUtil.saveBillableService(bsCopy);
					}					
				}
			 
				
				
			}
			
			return new ModelAndView(new RedirectView("departments.list"));
		}
		
		
		mav.addObject("services", services);
		mav.addObject("departmentId", departmentId);
		mav.addObject("department", department);
		
		mav.setViewName(getViewName());
		return mav;
	}

}
