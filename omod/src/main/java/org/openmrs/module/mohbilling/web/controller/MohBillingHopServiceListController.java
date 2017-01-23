/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.HopService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rbcemr
 * 
 */
public class MohBillingHopServiceListController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
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
		List<HopService> services = new ArrayList<HopService>();
		Department department = null;
		 if(request.getParameter("departmentId")!=null &&!request.getParameter("departmentId").equals("") ){
			 department = DepartementUtil.getDepartement(Integer.valueOf(request.getParameter("departmentId")));
			 mav.addObject("department", department);
			 String[] servicesStr = null;
			 if(GlobalPropertyConfig.getListOfHopServicesByDepartment1(department)!=null){
			  servicesStr = GlobalPropertyConfig.getListOfHopServicesByDepartment1(department).split(",");
			 
			 //services = HopServiceUtil.getHospitalServicesByDepartment(department);
			 
			 for (String s : servicesStr) {
				services.add(HopServiceUtil.getHopServiceById(Integer.valueOf(s)));
			}
		 }
			 mav.addObject("services",services );
		 }
		 else{
			 mav.addObject("services", HopServiceUtil.getAllHospitalServices() );
		 }
		
		//services = HopServiceUtil.getAllHospitalServices();
		
		mav.setViewName(getViewName());
		//mav.addObject("allServices",HopServiceUtil.getAllHospitalServices() );

		return mav;
	}

}
