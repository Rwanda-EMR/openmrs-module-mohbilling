/**
 * 
 */
package org.openmrs.module.mohappointment.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Services;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.module.mohappointment.utils.ConstantValues;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentServiceFormController extends
		ParameterizableViewController {
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav
				.addObject(
						"medicalServices",
						AppointmentUtil
								.createConceptCodedOptions(ConstantValues.PRIMARY_CARE_SERVICE_REQUESTED));
		mav.setViewName(getViewName());

		if (request.getParameter("save") != null) {
			boolean saved = saveService(request);
			if (saved)
				request.getSession().setAttribute(
						WebConstants.OPENMRS_MSG_ATTR, "Form Saved");
			else
				request.getSession().setAttribute(
						WebConstants.OPENMRS_ERROR_ATTR, "Form Not Saved");
		}

		return mav;
	}

	private boolean saveService(HttpServletRequest request) throws Exception {
		IAppointmentService ias = Context.getService(IAppointmentService.class);

		String serviceName = request.getParameter("name");
		String serviceDescription = request.getParameter("description");
		String concept = request.getParameter("serviceRelatedConcept");

		if (serviceName.trim().compareTo("") == 0) {
			return false;
		} else {
			Services service = new Services();
			service.setName(serviceName);
			service.setDescription(serviceDescription);
			if (Context.getConceptService()
					.getConcept(Integer.valueOf(concept)) != null)
				service.setConcept(Context.getConceptService().getConcept(
						Integer.valueOf(concept)));
			service.setCreatedDate(new Date());
			service.setCreator(Context.getAuthenticatedUser());
			service.setRetired(false);

			ias.saveService(service);
		}
		return true;
	}
}
