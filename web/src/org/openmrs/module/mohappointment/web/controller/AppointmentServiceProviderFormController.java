/**
 * 
 */
package org.openmrs.module.mohappointment.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.ServiceProviders;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentServiceProviderFormController extends
		ParameterizableViewController {

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		IAppointmentService ias = Context.getService(IAppointmentService.class);
		mav.addObject("services", ias.getServices());

		if (request.getParameter("save") != null) {
			boolean saved = saveServiceProvider(request);
			if (saved)
				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR, "Form Saved");
			else
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR, "Form Not Saved");
		}

		return mav;
	}

	private boolean saveServiceProvider(HttpServletRequest request)
			throws Exception {
		IAppointmentService ias = Context.getService(IAppointmentService.class);

		Date startDate = (request.getParameter("startDate").trim()
				.compareTo("") != 0) ? Context.getDateFormat().parse(
				request.getParameter("startDate")) : null;
		Person provider = (request.getParameter("provider").trim()
				.compareTo("") != 0) ? Context.getUserService().getUser(
				Integer.valueOf(request.getParameter("provider"))).getPerson()
				: null;
		Services service = (request.getParameter("service").trim()
				.compareTo("") != 0) ? ias.getServiceById(Integer
				.valueOf(request.getParameter("service"))) : null;

		if (startDate == null || provider == null || service == null) {
			return false;
		} else {
			ServiceProviders sp = new ServiceProviders();
			sp.setProvider(provider);
			sp.setService(service);
			sp.setStartDate(startDate);
			sp.setVoided(false);
			sp.setCreatedDate(new Date());
			sp.setCreator(Context.getAuthenticatedUser());

			ias.saveServiceProviders(sp);
		}
		return true;
	}

}
