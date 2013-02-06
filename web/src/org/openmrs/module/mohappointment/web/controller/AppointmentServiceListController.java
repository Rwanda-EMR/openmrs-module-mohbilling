/**
 * 
 */
package org.openmrs.module.mohappointment.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.FileExporterUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 *
 */
public class AppointmentServiceListController extends
		ParameterizableViewController {
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav=new ModelAndView();
		mav.setViewName(getViewName());
		
		if(request.getParameter("export")!=null){
			FileExporterUtil xprt=new FileExporterUtil();
			xprt.exportToCSVFile(request, response, "List of current services");
		}
		
		IAppointmentService ias = Context.getService(IAppointmentService.class);
		mav.addObject("services", ias.getServices());
		mav.addObject("today", Context.getDateFormat().format(new Date()));
		mav.addObject("creator", Context.getAuthenticatedUser());
		mav.addObject("reportName", "mohappointment.appointment.service.current");
				
		return mav;
	}
}
