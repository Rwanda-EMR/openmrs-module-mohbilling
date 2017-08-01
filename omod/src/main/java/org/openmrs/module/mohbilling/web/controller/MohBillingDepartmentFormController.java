/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.model.Department;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author EMR@RBC
 *
 */
public class MohBillingDepartmentFormController extends ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
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
		Department dpt = null;
		if (request.getParameter("departmentId")!=null&&!request.getParameter("departmentId").equals("")){
			dpt = DepartementUtil.getDepartement(Integer.valueOf(request.getParameter("departmentId")));
		}
		else
			dpt = new Department();
		
		dpt.setName(name);
		dpt.setDescription(description);
		dpt.setCreatedDate(new Date());
		dpt.setCreator(Context.getAuthenticatedUser());
		
		//save Departement		
		DepartementUtil.saveDepartement(dpt);
		
		return new ModelAndView(new RedirectView("departments.list"));
		
	}
	if(request.getParameter("departmentId")!=null&&!request.getParameter("departmentId").equals("")){
	mav.addObject("department", DepartementUtil.getDepartement(Integer.valueOf(request.getParameter("departmentId"))));
	}

	mav.setViewName(getViewName());
		return mav;
	}

}
