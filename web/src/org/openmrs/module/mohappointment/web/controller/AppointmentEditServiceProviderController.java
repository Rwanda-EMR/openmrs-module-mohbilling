/**
 * Auto generated file comment
 */
package org.openmrs.module.mohappointment.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.ServiceProviders;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * This class is the Controller that makes the ServiceProvider Editable (either
 * deletable or updatable)
 */
public class AppointmentEditServiceProviderController extends
		ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		ServiceProviders sp = AppointmentUtil.getServiceProvidersById(Integer
				.parseInt(request.getParameter("serviceProviderId")));
		User user = null;
		// Here I just get the very first User corresponding
		for (User u : Context.getUserService().getUsersByPerson(
				sp.getProvider(), false))
			if (u != null) {
				user = u;
				break;
			}

//		mav.addObject("startDate", Context.getDateFormat().parse(""));
		mav.addObject("services", AppointmentUtil.getAllServices());
		mav.addObject("serviceProvider", sp);

		mav.addObject("user", user);

		if (request.getParameter("edit") != null) {

			log.info("<<<<<<<" + request.getParameter("edit") + ">>>>>>>>");

			if (editServiceProvider(request))
				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR,
						"ServiceProvider updated successfully!");
			else
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR, "Form Not Saved!");
		}

		mav.setViewName(getViewName());
		return mav;
	}

	/**
	 * Edits the Service Provider provided from the Form
	 * 
	 * @param request HttpServletRequest to be set
	 * @return
	 * @throws Exception
	 */
	private boolean editServiceProvider(HttpServletRequest request)
			throws Exception {
		IAppointmentService ias = Context.getService(IAppointmentService.class);

		ServiceProviders serviceProvider = AppointmentUtil
				.getServiceProvidersById(Integer.parseInt(request
						.getParameter("serviceProviderId")));

		Date startDate = (request.getParameter("startDate").trim()
				.compareTo("") != 0) ? Context.getDateFormat().parse(
				request.getParameter("startDate")) : null;
		Person provider = (request.getParameter("provider").trim()
				.compareTo("") != 0) ? Context.getUserService()
				.getUser(Integer.valueOf(request.getParameter("provider")))
				.getPerson() : null;
		Services service = (request.getParameter("service").trim()
				.compareTo("") != 0) ? ias.getServiceById(Integer
				.valueOf(request.getParameter("service"))) : null;

		if (startDate == null || provider == null || service == null) {
			return false;
		} else {
			serviceProvider.setProvider(provider);
			serviceProvider.setService(service);
			serviceProvider.setStartDate(startDate);
			serviceProvider.setVoided(false);

			AppointmentUtil.editServiceProvider(serviceProvider);
		}
		return true;
	}
}
