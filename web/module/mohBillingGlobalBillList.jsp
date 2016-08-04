<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<h2>Global Bill List</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<br />

<b class="boxHeader">Global Bill  List </b>
<div class="box">
	<table>
		<tr>
		<th>#.</th>		
		<th>Date of Bill</th>
		<th>Created By</th>		
		<th>Policy ID No.</th>
		<th>Admission Date</th>
		<th>Discharging Date</th>		
		<th>Bill identifier</th>
		<th>Patient Due Amount</th>
		<th>paid Amount</th>		
		<th>Bill</th>					
	</tr>
		<c:forEach items="${globalBills}" var="globalBill" varStatus="status">
	<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
	<c:set var="globalBillId" value="${globalBill.globalBillId}" />
	<c:set var="insurancePolicyId" value="${globalBill.admission.insurancePolicy.insurancePolicyId}" />
	<c:set var="billIdentifier" value="${globalBill.billIdentifier}" />
	<c:set var="admissionDate" value="${globalBill.admission.admissionDate}" />
	<c:set var="dischargingDate" value="${globalBill.closingDate}"/>
				
		<tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${globalBill.createdDate}</td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}">${globalBill.creator.personName}</td>							
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${ipCardNumber}</td>
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${admissionDate}</td>
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${dischargingDate}</td>			
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${billIdentifier}</td>			
			<td class="rowValue center ${(status.count%2!=0)?'even':''}">${globalBill.globalAmount}</td>			
		    <td class="rowValue right ${(status.count%2!=0)?'even':''}">${billingtag:amountPaidByGlobalBill(globalBill.globalBillId)}</td>		
		    <td class="rowValue ${(status.count%2!=0)?'even':''}">&nbsp;<a href="billing.form?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">Add</a>
		    /<a href="consommation.list?insurancePolicyId=${insurancePolicyId }&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">View</a></td>
		 </tr>
		 
	</c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>