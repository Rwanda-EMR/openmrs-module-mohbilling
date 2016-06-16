/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author EMR@RBC
 *
 */
public class MohBillingDepartmentFormController extends
		ParameterizableViewController {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		
	if(request.getParameter("save")!=null){
		String name =request.getParameter("departmentName");
		String description = request.getParameter("departmentDescription");
		
		//new Departement
		Department dpt = new Department();
		dpt.setName(name);
		dpt.setDescription(description);
		dpt.setCreatedDate(new Date());
		dpt.setCreator(Context.getAuthenticatedUser());			
		//save Departement		
		DepartementUtil.saveDepartement(dpt);
		
		return new ModelAndView(new RedirectView("departments.list"));	
		
	}
	mav.setViewName(getViewName());
		return mav;
	}

}
