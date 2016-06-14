<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!--  
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
-->

<!-- script to create a pop up windows for unpaid bills -->
<openmrs:htmlInclude file="/moduleResources/mohbilling/pop_style.css" /> 


<%@ include file="templates/mohBillingLocalHeader.jsp"%>


<%@ include file="templates/mohBillingDepositHeader.jsp"%>

<form action="insurancePolicy.form?save=true" method="post">
<b class="boxHeader">Owner</b>
<div class="box">
	<table>
		<tr>
		 <td>Patient Name</td>
		 <td width="300px;"><openmrs_tag:patientField formFieldName="insurancePolicyOwner" initialValue="${beneficiaryId}" /></td>
			</tr>
		</table>
	</div>
	<br />

<b class="boxHeader">Add Deposit </b>
<div class="box">
		<table>
			<tr>
				<td>Deposit Amount</td>
				<td><input type="text" name="depositAmount"/></td>
			</tr>
			<tr>
				<td>Deposit Date</td>
				<td><input type="text" name="depositDate"/></td><td>(dd/mm/yyyy)</td>
			</tr>
			<tr>
				<td>Deposit Reason</td>
				<td>
				<!-- list to be set in Global Properties -->
				 <select name="depositReason">
					  <option value="opd">OPD</option>
					  <option value="ipd">IPD</option>
				</select> 
				</td>
			</tr>

		</table>
		<br /> <input type="submit" value="Save Deposit" id="submitButtonId" />
</div>
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>
