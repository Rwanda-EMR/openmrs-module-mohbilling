<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="mohbilling_tag" 	tagdir="/WEB-INF/tags/module/mohbilling"%>

<script type="text/javascript">


function closeWindow()
{
   if(false == popupWindow.closed)
   {
      popupWindow.close ();
   }
}
</script>

<script type="text/javascript">
function  saveClosingGlobalBill(){
	if(confirm("Are you sure you want to save?")){
		//save close status
		document.getElementById("closeGB").submit();
	}
}
function  cancelClosingGlobalBill(){
	if(confirm("Are you sure you want to Cancel?"))
		document.getElementById("form_cancel").submit();
		close();
}
</script>

<c:if test="${discharge!=null }">
<div>
<h2>Discharge Form</h2>


<form action="admission.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&globalBillId=${globalBill.globalBillId}&edit=true&discharge=${discharge}" method="post" id="closeGB">
<span id="close"></span>
<div style="text-align:right; "><a href="JavaScript:window.close()">Close</a></div>
	<div class="box">
		<table> 
			<tr>
				<td>Patient</td>
				<td><input type="text" disabled="disabled" name="patientName"
					value="${insurancePolicy.owner.personName}" size="30" /></td>
			</tr>
			<tr>
				<td>Admission Date</td>
				<td><openmrs_tag:dateField formFieldName="admissionDate" startValue="${globalBill.admission.admissionDate}"/> </td>
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
</form>

<table width="99%">
				<tr>
					<td>
						<input type="button" value="Save" onclick="saveClosingGlobalBill();"/>
					</td>
					<td>
						<form action=""  id="form_cancel">
							<input type="button" value="Cancel" onclick="cancelClosingGlobalBill();"/>
						</form>
					</td>
				</tr>
</table>



</div>
</c:if>

<c:if test="${discharge==null }">
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" 	tagdir="/WEB-INF/tags/module/mohbilling"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<b>Admission Form</b>

<form action="admission.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&save=true" method="post">

	<div class="box">
		<table>
			<tr>
				<td>Name</td>
				<td><input type="text" name="patientName"
					value="${insurancePolicy.owner.personName}" size="30" />
				</td>
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


<c:if test="${empty globalBills }">
<div class="box">
   <p style="text-align: center;color: red;">No Admission found!</p>
</div>
</c:if>


<c:if test="${not empty globalBills }">
<br>
<b>Admission </b>
<div class="box">
	<table cellspacing="0" cellpadding="2" width="98%" id="obs">
		<tr>
		<th>Name</th>
		<th>Bill identif</th>
		<th>Insurance name</th>
		<th>Card Number</th>
		<th>Admission Type</th>
		<th>admission date</th>	
		<th>Discharge date</th>			
		<th>Bill</th>
		</tr>
		<c:forEach items="${globalBills}" var="gb" varStatus="status">
		<tr>
			<td>${gb.admission.insurancePolicy.owner.personName }</td>
			<td>${gb.billIdentifier }</td>			
			<td>${gb.admission.insurancePolicy.insurance.name }</td>
			<td>${gb.admission.insurancePolicy.insuranceCardNo }</td>
			<c:if test="${gb.admission.isAdmitted eq true}">
			<td>IPD</td>
			</c:if>
			<c:if test="${gb.admission.isAdmitted eq false}">
			<td>OPD</td>
			</c:if>
			
			<td>${gb.admission.admissionDate}</td>	
			
			<td>${gb.admission.dischargingDate }</td>	
			
			<td><a href="billing.form?insurancePolicyId=${gb.admission.insurancePolicy.insurancePolicyId }&ipCardNumber=${gb.admission.insurancePolicy.insuranceCardNo}&globalBillId=${gb.globalBillId}">Add Bill</a></td>
		</tr>
		
		</c:forEach>
	</table>
</div>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>
</c:if>







