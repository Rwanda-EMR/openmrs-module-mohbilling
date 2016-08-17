
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag"
	tagdir="/WEB-INF/tags/module/mohbilling"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<div id="modal_dialog" style="display: none">
form action="admission.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&save=true" method="post">

	<div class="box">
		<table>
			<tr>
				<td>Name</td>
				<td><input type="text" name="patientName"
					value="${insurancePolicy.owner.familyName}" size="30" /></td>
			</tr>
			<tr>
				<td>Insurance Name</td>
				<td><input type="text" name="insuranceName"
					value="${insurancePolicy.insurance.name}" size="30" /></td>
			</tr>
			<tr>
				<td>Insurance Card Number</td>
				<td><input type="text" name="ipCardNumber"
					value="${insurancePolicy.insuranceCardNo}" size="30" /></td>
			</tr>
			<tr>
				<td>Is admitted ?</td>
				<td><input 	type="checkbox" name="isAdmitted" value="" /><td>

			</tr>

			<tr>
				<td>admission Date</td>
				<td><openmrs_tag:dateField formFieldName="admissionDate" 	startValue="${startdate}" /></td>
			</tr>

		</table>
	</div>
	<br /> <br /> <input type="submit" value="Save Admission " 	id="submitButtonId" />
</form>
</div>