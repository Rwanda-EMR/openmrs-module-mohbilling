<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<h2>Consommations List</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>


<br />
<div style="text-align: right;"><a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${globalBill.globalBillId}">Add Consommation</a></div>

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
		</tr>
		<c:forEach items="${consommations}" var="consommation" 
			varStatus="status">
			<tr>
				<td>${status.count}</td>
				<td>${consommation.consommationId}</td>
				<td>${consommation.creator.personName}</td>
				<td>${consommation.beneficiary.policyIdNumber}</td>								
				<td>${consommation.insuranceBill.amount}</td>
				<td>${consommation.thirdPartyBill.amount}</td>
				<td>${consommation.patientBill.amount}</td>
				<td>${billingtag:amountPaidForPatientBill(consommation.consommationId)}</td>
				<td>paid</td>
				<td>&nbsp;<a href="patientBillPayment.form?consommationId=${consommation.consommationId}">view payment</a></td>				
			</tr>						
		</c:forEach>
		<tr>
		   <td></td>
		</tr>
			<tr>
			     <td><div style="text-align: left;">Total Due Amount</div></td>			
				<td><div class="amount">${globalBill.globalAmount}</div></td>
				<td></td><td></td> <td></td><td></td><td></td>
				<td><div style="text-align: right;"><b>Total paid  Amount</b></div></td>
				<td><div class="amount">${billingtag:amountPaidByGlobalBill(globalBill.globalBillId)}</div></div></td>
			</tr>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>