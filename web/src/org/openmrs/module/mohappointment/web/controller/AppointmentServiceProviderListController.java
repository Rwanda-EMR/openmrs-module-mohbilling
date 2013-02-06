/**
 * 
 */
package org.openmrs.module.mohappointment.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.ServiceProviders;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.FileExporterUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentServiceProviderListController extends
		ParameterizableViewController {

	private Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		if (request.getParameter("export") != null) {
			FileExporterUtil xprt = new FileExporterUtil();
			xprt.exportToCSVFile(request, response,
					"List of current service providers");
		}

		IAppointmentService ias = Context.getService(IAppointmentService.class);
		List<ServiceProviders> providers = (List<ServiceProviders>) ias.getServiceProviders();
		mav.addObject("serviceProviders", providers);
		log.info("___________________Appointment: SIZE OF Providers is : "
				+ ias.getServiceProviders().size());
		mav.addObject("today", Context.getDateFormat().format(new Date()));
		mav.addObject("creator", Context.getAuthenticatedUser());
		mav.addObject("reportName",
				"mohappointment.appointment.service.provider.current");

		return mav;
	}

}
