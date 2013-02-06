/**
 * 
 */
package org.openmrs.module.mohappointment.web.dwr;

import java.text.SimpleDateFormat;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohappointment.model.Appointment;
import org.openmrs.module.mohappointment.service.IAppointmentService;
import org.openmrs.web.dwr.PersonListItem;

/**
 * @author Yves GAKUBA
 * 
 */
public class DWRAppointmentUtil {

	public String getPatientListInTable(String searchString, String id) {
		PersonListItem ret = null;

		// VCTModuleService service =
		// Context.getService(VCTModuleService.class);
		// List<Person> persons = service.getPeople(searchString, false);

		IAppointmentService ias = Context.getService(IAppointmentService.class);
		Object[] conditions = { searchString, null, null, null, null, null,
				null, null };
		List<Integer> appointments = ias.getAppointmentIdsByMulti(conditions,
				50);

		StringBuilder sb = new StringBuilder("");
		sb
				.append("<table class='openmrsSearchTable' cellpadding=2 cellspacing=0 style='width:100%; font-size:0.8em'>");
		sb
				.append("<tr><td colspan='12' style='text-align:right; font-style:italic;'>Results for &quot;"
						+ id
						+ "&quot;: "
						+ appointments.size()
						+ " appointments</tr>");
		sb
				.append("<tr class='oddRow'><th>#</th><th>Identifier</th><th>Patient Names</th><th>Age</th><th>Gender</th><th></th><th>Birthdate</th><th>Appointment Date</th><th>Provider</th><th>Reason of Appointment</th><th>State</th><th></th><th></th></tr>");
		int i = 0;
		for (Integer appId : appointments) {
			Appointment app = ias.getAppointmentById(appId);
			Person ps = app.getPatient();
			i++;
			ret = new PersonListItem();
			ret.setPersonId(ps.getPersonId());
			ret.setGivenName(ps.getGivenName());
			ret.setMiddleName(ps.getMiddleName());
			ret.setFamilyName(ps.getFamilyName());
			ret.setGender(ps.getGender());
			ret.setBirthdate(ps.getBirthdate());
			ret.setBirthdateEstimated(ps.getBirthdateEstimated());
			String identifier = "";
			identifier = (ps.isPatient()) ? ((Patient) ps)
					.getPatientIdentifier().getIdentifier() : "";

			String name = ((ret.getGivenName() != null) ? ret.getGivenName()
					.trim() : "")
					+ "&nbsp;"
					+ ((ret.getMiddleName() != null) ? ret.getMiddleName()
							.trim() : "")
					+ "&nbsp;"
					+ ((ret.getFamilyName() != null) ? ret.getFamilyName()
							.trim() : "");
			String provName = app.getProvider().getPersonName().toString();
			String appDate = new SimpleDateFormat("dd-MMM-yyyy").format(app
					.getAppointmentDate());
			String reason = "";
			if (app.getReason() != null
					&& !app.getReason().getValueAsString(Context.getLocale())
							.equals(""))
				reason = app.getReason().getValueAsString(Context.getLocale());

			// sb.append("<tr onclick=personValues('" + ret.getPersonId() +
			// "','" + name.replace(" ", "&nbsp;") + "','" + id +
			// "') class='searchRow "
			// + ((i % 2 == 0) ? "oddRow" : "") + "'>");
			sb.append("<tr class='searchRow " + ((i % 2 == 0) ? "oddRow" : "")
					+ "'>");
			sb.append("<td class='searchIndex'>" + i + ".</td>");
			sb.append("<td class='patientIdentifier'>" + identifier + "</td>");
			sb.append("<td>" + name + "</td>");
			sb.append("<td style='text-align:center'>" + ps.getAge() + "</td>");
			if (ret.getGender().trim().compareToIgnoreCase("f") == 0)
				sb
						.append("<td style='text-align:center'><img src='../../images/female.gif'/></td>");
			else
				sb
						.append("<td style='text-align:center'><img src='../../images/male.gif'/></td>");
			sb.append("<td>" + ((ret.getBirthdateEstimated()) ? "&asymp;" : "")
					+ "</td>");
			sb.append("<td>"
					+ new SimpleDateFormat("dd-MMM-yyyy").format(ret
							.getBirthdate()) + "</td>");
			sb.append("<td>" + appDate + "</td>");
			sb.append("<td>" + app.getProvider().getPersonName() + "</td>");
			sb.append("<td>" + reason + "</td>");
			sb.append("<td>" + app.getAppointmentState().getDescription()
					+ "</td>");
			sb
					.append("<td onclick=showDialog('"
							+ app.getAppointmentId()
							+ "','"
							+ name.replace(" ", "&nbsp;")
							+ "','"
							+ provName.replace(" ", "&nbsp;")
							+ "','"
							+ appDate
							+ "','"
							+ reason.replace(" ", "&nbsp;")
							+ "',1)><input type='button' value='WAITING/INADVANCE'/></td>");
			sb.append("<td onclick=showDialog('" + app.getAppointmentId()
					+ "','" + name.replace(" ", "&nbsp;") + "','"
					+ provName.replace(" ", "&nbsp;") + "','" + appDate + "','"
					+ reason.replace(" ", "&nbsp;")
					+ "',2)><input type='button' value='POSTPONE'/></td>");
			sb.append("</tr>");
		}
		sb.append("</table>");

		return sb.toString();
	}

}
