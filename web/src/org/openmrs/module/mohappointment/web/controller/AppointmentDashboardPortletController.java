/**
 * 
 */
package org.openmrs.module.mohappointment.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.web.controller.PortletController;

/**
 * @author Yves GAKUBA
 * 
 */
public class AppointmentDashboardPortletController extends PortletController {

	@Override
	protected void populateModel(HttpServletRequest request,
			Map<String, Object> model) {

		IAppointmentService ias = Context.getService(IAppointmentService.class);

		try {

			if (!AppointmentUtil.setAttendedAppointment(request))
				log
						.info("_______________Any appointment has not been attended");

			if (!AppointmentUtil.cancelAppointment(request))
				log
						.info("_______________Any appointment has not been cancelled");

			Patient p = Context.getPatientService().getPatient(
					Integer.valueOf(request.getParameter("patientId")));

			Object[] conditions = { p.getPatientId(), null, null, null, null,
					null, null, null };

			List<Integer> appointmentIds = ias.getAppointmentIdsByMulti(
					conditions, 100);
			List<Appointment> appointments = new ArrayList<Appointment>();
			for (Integer appointmentId : appointmentIds) {
				appointments.add(ias.getAppointmentById(appointmentId));
			}

			// model.put("appointments", appointments);
			request.setAttribute("appointments", appointments);
			request.setAttribute("patientId", p.getPatientId());
			//		
			// request.setAttribute("arraySz", appointments.size());
			//		
			// model.put("sizeyaarray", appointmentIds.size());

		} catch (Exception e) {
			log
					.error(">>>>>>>>>>> APPOINTMENT >> An error occured when trying to load appointments on the patient dashboard");
			e.printStackTrace();
		}
		super.populateModel(request, model);
	}

}
