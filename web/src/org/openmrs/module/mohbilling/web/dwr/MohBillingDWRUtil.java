/**
 * 
 */
package org.openmrs.module.mohbilling.web.dwr;

import java.text.SimpleDateFormat;

import org.openmrs.module.mohbilling.businesslogic.InsurancePolicyUtil;
import org.openmrs.module.mohbilling.model.Beneficiary;

/**
 * @author Yves GAKUBA
 * 
 */
public class MohBillingDWRUtil {

	public String getBeneficiaryListInTable(String searchString) {

		BeneficiaryListItem bli = null;

		/** perform a search in the db using existing service method & parameter
			searchString */
		Beneficiary ben = InsurancePolicyUtil
				.getBeneficiaryByPolicyIdNo(searchString);

		if (ben != null)
			bli = new BeneficiaryListItem(ben);
		else
			return "";

		/** building the string to return, the string is a list/table containing
		 beneficiaries*/
		StringBuilder sb = new StringBuilder("");
		sb.append("<table class='openmrsSearchTable' cellpadding=2 cellspacing=0 style='width:100%; font-size:0.8em'>");
		sb.append("<tr><td colspan='10' style='text-align:right; font-style:italic;'>Result for &quot;"
				+ searchString
				+ "&quot;: "
				+ ((ben != null) ? 1 : 0)
				+ " beneficiaries</tr>");
		sb.append("<tr class='oddRow'>" + "<th>#</th>"
				+ "<th>Insurance Policy No.</th>" + "<th>Insurance</th>"
				+ "<th>Insurance Card No.</th>" + "<th>Patient names</th>"
				+ "<th>Age</th>" + "<th>Gender</th>" + "<th></th>"
				+ "<th>Birthdate</th></tr>");

		/** the result should be listed here */

		if (null != bli) {
			sb.append("<tr class='searchRow' onclick=editInsurancePolicy('"
					+ bli.getInsurancePolicyId() + "','" + searchString + "')>"
					+ "<td>1.</td>" + "<td>" + bli.getInsurancePolicyNumber()
					+ "<td>"
					+ ben.getInsurancePolicy().getInsurance().getName()
					+ "</td>" + "</td>" + "<td>" + bli.getInsuranceCardNumber()
					+ "</td>" + "<td>" + bli.getGivenName() + " "
					+ bli.getMiddleName() + " " + bli.getFamilyName() + "</td>");
			sb.append("<td>"
					+ ((bli.getAge().intValue() >= 1) ? bli.getAge() : "<1")
					+ "</td>");
			if (bli.getGender().trim().compareToIgnoreCase("f") == 0)
				sb.append("<td><img src='../../images/female.gif'/></td>");
			else
				sb.append("<td><img src='../../images/male.gif'/></td>");
			sb.append("<td>" + ((bli.getBirthdateEstimated()) ? "&asymp;" : "")
					+ "</td>");
			sb.append("<td>"
					+ new SimpleDateFormat("dd-MMM-yyyy").format(bli
							.getBirthdate()) + "</td>");
			sb.append("</tr>");
		}

		sb.append("</table>");

		return sb.toString();
	}

}
