<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<h2>Global Bill Management</h2>

<b class="boxHeader">Global Bill List</b>
<div class="box">
<table>
	<tr>
		<th>#.</th>
		<th>GlobId</th>
		<th>admission type</th>
		<th>admission Date</th>		
		<th>Discharging Date</th>
		<th>Due Amount</th>
		<th>Action</th>
		
	</tr>
	<c:forEach items="${globalBills}" var="globalBill" varStatus="status">
	<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
	<c:set var="globalBillId" value="${globalBill.globalBillId}" />
	<c:set var="insurancePolicyId" value="${globalBill.admission.insurancePolicy.insurancePolicyId}" />
		
		<tr>
			<td>${status.count}</td>
			<td>${globalBill.globalBillId}</td>
			<td>OPD</td>
			<td>${globalBill.admission.admissionDate}</td>
			<td>${globalBill.closingDate}</td>
			<td>100.000</td>			
			<td> &nbsp;<a href="billing.form?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">view/Add</a></td>	
		 </tr>
	</c:forEach>
</table>
</div>


<%@ include file="/WEB-INF/template/footer.jsp"%>