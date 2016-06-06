package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

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
		List<HopService> services = HopServiceUtil.getAllHospitalServices();
		String departmentId=request.getParameter("departmentId");
		HopService service =new HopService();
		if((request.getParameter("save")!=null)&& !request.getParameter("save").equals("")){	
		     
	 
			String serviceIdStr = request.getParameter("serviceId");
			System.out.println("parameters++ serviceIdstr"+serviceIdStr+"departement Id"+departmentId);
			Department department = DepartementUtil.getDepartement(Integer.valueOf(departmentId));
			 service = HopServiceUtil.getHopServiceById(Integer.valueOf(serviceIdStr));
		    System.out.println("is this service Hop services>>>>>>"+service);
		    //Iterate over each insurance  and create service category
			
			for (Insurance insurance : InsuranceUtil.getAllInsurances()) {
				ServiceCategory sc = new ServiceCategory();
				
				sc.setName(service.getName());	
				
		
		    	sc.setDescription(service.getDescription());

				sc.setRetired(false);
				sc.setCreatedDate(new Date());
				sc.setCreator(Context.getAuthenticatedUser());
				sc.setDepartment(department);
				sc.setHopService(service);
				
				insurance.addServiceCategory(sc);
				Context.getService(BillingService.class).saveInsurance(insurance);
			}
			
			return new ModelAndView(new RedirectView("departments.list"));	
			
			
		}
		
		
		mav.addObject("services", services);
		mav.addObject("departmentId", departmentId);
	
		
		mav.setViewName(getViewName());
		return mav;
	}

}
