<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="mohbilling_tag" 	tagdir="/WEB-INF/tags/module/mohbilling"%>

<c:if test="${discharge!=null }">
<div>
<h2>Discharge Form</h2>


<form action="admission.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&globalBillId=${globalBill.globalBillId}&edit=true&discharge=${discharge}" method="post">

	<div class="box">
		<table>
			<tr>
				<td>Patient</td>
				<td><input type="text" disabled="disabled" name="patientName"
					value="${insurancePolicy.owner.personName}" size="30" /></td>
			</tr>
			<tr>
				<td>Admission Date</td>
				<td><openmrs_tag:dateField formFieldName="admissionDate" startValue="${globalBill.admission.admissionDate}" /> </td>
			</tr>
			<tr>
				<td>Closing Date</td>
				<td><openmrs_tag:dateField formFieldName="closingDate" 	startValue="" /></td>
			</tr>
			<tr>
				<td>Closing Reason</td>
				<td><textarea name="closingReason" ></textarea><td>

			</tr>
		</table>
	</div>
	<input type="submit" value="Save " 	id="submitButtonId" />
	<input type="submit" value="Cancel " 	id="cancelButtonId" />
</form>
</div>
</c:if>


<c:if test="${discharge == null }">

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" 	tagdir="/WEB-INF/tags/module/mohbilling"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<b>Admission Form</b>
${pram.insurancePolicyId}

<form action="admission.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&save=true" method="post">

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
	<c:set var="globalBill" value="${globalBill}" />
	<c:set var="familyName" value="${globalBill.admission.insurancePolicy.owner.familyName}" />
	<c:set var="insuranceName" value="${globalBill.admission.insurancePolicy.insurance.name}" />
	<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
	<c:set var="insurancePolicyId" value="${globalBill.admission.insurancePolicy.insurancePolicyId}" />	
	
		<tr>
			<th>Name</th>
			<th>Bill identif</th>
			<th>Insurance name</th>
			<th>Bill Identifier</th>
			<th>Card Number</th>
			<th>Admission type</th>
			<th>admission date</th>			
			<th>Bill</th>
		</tr>
		<tr>
			<td>${familyName}</td>
			<td>${globalBill.billIdentifier}</td>			
			<td>${insuranceName}</td>
			<td>${insuranceCardNo}</td>
			<td>OPD</td>
			<td>22/12/2015</td>		
			<td><a href="billing.form?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBill.globalBillId}">Add Bill</a></td>
		</tr>

	</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>
</c:if>





