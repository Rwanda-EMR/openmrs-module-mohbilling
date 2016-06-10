<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag"
	tagdir="/WEB-INF/tags/module/mohbilling"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<b>Admission Form</b>
${pram.insurancePolicyId}

<form action="admission.form?save=true" method="post">

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
<br>
<b>Admission </b>

<div class="box">
	<table cellspacing="0" cellpadding="2" width="98%" id="obs">
		<tr>
			<th>Name</th>
			<th>Insurance name</th>
			<th>Card Number</th>
			<th>Admission type</th>
			<th>admission date</th>
			<th>Action</th>
		</tr>
		<tr>
			<td>${insurancePolicy.owner.familyName}</td>
			<td>${insurancePolicy.insurance.name}</td>
			<td>${insurancePolicy.insuranceCardNo}</td>
			<td>OPD</td>
			<td>22/12/2015</td>
			<td><a href="billing.form?admissionId=${admission.admissionId}">Make Bill<a></a></td>
		</tr>

	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>


