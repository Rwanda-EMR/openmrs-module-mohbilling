<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<table>
	<tr>
		<td><input type="button" onclick="removeBeneficiary(${param.fieldId})" value="Remove"/></td>
		<td>Patient Name</td>
		<td><openmrs_tag:patientField formFieldName="beneficiary"/></td>
	</tr>
</table>
<!-- _${param.fieldId} -->
<script>
	function removeBeneficiary(objId){
		document.getElementById("beneficiaryDiv").removeChild(document.getElementById("appendedDiv_"+objId));
	}
</script>