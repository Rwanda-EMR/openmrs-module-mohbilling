/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.DepartementUtil;
import org.openmrs.module.mohbilling.businesslogic.HopServiceUtil;
import org.openmrs.module.mohbilling.businesslogic.InsuranceUtil;
import org.openmrs.module.mohbilling.model.HopService;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.ServiceCategory;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author rbcemr
 *
 */
public class MohBillingHopserviceFormController extends
		ParameterizableViewController {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		if(request.getParameter("save")!=null){
		
			String serviceName =request.getParameter("serviceName");
			String description = request.getParameter("description");
			HopService service = null;
			if(request.getParameter("serviceId")!=null&&!request.getParameter("serviceId").equals(null)&&!request.getParameter("serviceId").equals("")){
			service = HopServiceUtil.getHopServiceById(Integer.valueOf(request.getParameter("serviceId")));
			}
			else{
				//declare new HopService object to be saved
				service = new HopService();
			}
			service.setName(serviceName);
			service.setDescription(description);		
			service.setCreatedDate(new Date());	
			service.setCreator(Context.getAuthenticatedUser());
			
			HopServiceUtil.createHopService(service);

           Insurance insurance = InsuranceUtil.getInsurance(1);
		   ServiceCategory sc = new ServiceCategory();

			sc.setName(serviceName);
			sc.setDescription(description);

			sc.setRetired(false);
			sc.setCreatedDate(new Date());
			sc.setCreator(Context.getAuthenticatedUser());
			sc.setDepartment(DepartementUtil.getDepartement(1));
			sc.setHopService(service);
			
			insurance.addServiceCategory(sc);
			Context.getService(BillingService.class).saveInsurance(insurance);
			
			mav.addObject("service", service);
			return new ModelAndView(new RedirectView("services.list"));
		}
		if(request.getParameter("serviceId")!=null&&!request.getParameter("serviceId").equals("")){
			mav.addObject("service", HopServiceUtil.getHopServiceById(Integer.valueOf(request.getParameter("serviceId"))));
			}
		mav.setViewName(getViewName());	
		
		return mav;
	}

}
