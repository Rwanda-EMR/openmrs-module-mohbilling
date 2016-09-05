<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld"%>
	
	<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<h2>Consommations List</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<script type="text/javascript">
function myFunction() {
    var myWindow = window.open("admission.form?insurancePolicyId=48518&ipCardNumber=1994", "Discharge Form", "width=600,height=600");
}
</script>
<br />
<div style="text-align: right;">

 <a href="javascript:window.open('admission.form?globalBillId=${globalBill.globalBillId}&insurancePolicyId=${insurancePolicy.insurancePolicyId }&ipCardNumber=${insurancePolicy.insuranceCardNo}&discharge=true', 'summaryWindow', 'toolbar=no,width=660,height=600,resizable=yes,scrollbars=yes').focus()">Dischage the Patient</a>

<a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${globalBill.globalBillId}">Add Consommation</a></div>

<b class="boxHeader">consommations List for Global Bill id created
	on #</b>
<div class="box">
	<table>
		<tr>
			<th>#.</th>
			<th>Consom ID</th>
			<th>created By</th>
			<th>card No</th>			
			<th>Insurance due</th>
			<th>Third party due</th>
			<th>Patient due</th>
			<th>Paid Amount</th>
			<th>status</th>			
			<th>Payment</th>
			<th></th>
		</tr>
		<c:forEach items="${consommations}" var="consommation" 
			varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.consommationId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.creator.personName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.beneficiary.policyIdNumber}</td>								
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.insuranceBill.amount}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.thirdPartyBill.amount}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${consommation.patientBill.amount}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billingtag:amountPaidForPatientBill(consommation.consommationId)}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">paid</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">&nbsp;<a href="patientBillPayment.form?consommationId=${consommation.consommationId}">view payment</a></td>				
				<%-- <td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="consommation.form?consommationId=${consommation.consommationId}">Edit</a></td> --%>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="billing.form?consommationId=${consommation.consommationId}&departmentId=${consommation.department.departmentId}&insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${param.ipCardNumber}&edit=true">Edit</a></td>
				
			</tr>						
		</c:forEach>
		<tr>
		   <td></td>
		</tr>
			<tr>
			     <td><div style="text-align: left;">Total Due Amount</div></td>	
			     <c:set var="totalDueAmount" value="${(globalBill.globalAmount)*((100-insurancePolicy.insurance.currentRate.rate)/100)}"/>		
				<td><div class="amount"><fmt:formatNumber value="${totalDueAmount}" type="number" pattern="#.##"/></div></td>
				<td></td><td></td> <td></td><td></td><td></td>
				<td><div style="text-align: right;"><b>Total paid  Amount</b></div></td>
				<td><div class="amount">${billingtag:amountPaidByGlobalBill(globalBill.globalBillId)}</div></div></td>
			</tr>
	</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>